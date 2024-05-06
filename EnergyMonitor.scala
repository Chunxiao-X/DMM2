import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.atomic.AtomicBoolean
import java.net.{HttpURLConnection, URL}
import scala.io.Source
import play.api.libs.json.{JsValue, Json, Reads}
import scala.swing._
import scala.swing.Dialog.Message
import java.util.Locale


object EnergyMonitor {
  //  Group 15
  //  Chunxiao Ren
  //  Xiaoyu Du
  //  Yizhou Wang

  // Set the default locale to English at the start of the object
  Locale.setDefault(Locale.ENGLISH)

  @volatile var lastFetchedData: Option[(String, Double)] = None
  private val keepRunning = new AtomicBoolean(false)
  private val displayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  private val alertThreshold = 500.0

  // Starts monitoring energy data by repeatedly fetching data at a defined interval and displaying it, potentially alerting if below a threshold.
  def startMonitoring(baseUrl: String, datasetId: Int, apiKey: String, threshold: Double, interval: FiniteDuration)(implicit ec: ExecutionContext): Unit = {
    fetchAndDisplayLatestData(baseUrl, datasetId, apiKey, threshold)
    keepRunning.set(true)
    Future {
      while (keepRunning.get) {
        Thread.sleep(interval.toMillis)
        fetchAndDisplayLatestData(baseUrl, datasetId, apiKey, threshold)
      }
    }
  }

  // Fetches the latest energy data from a specified URL, logs the current value, and displays an alert if the value is below the set threshold.
  private def fetchAndDisplayLatestData(baseUrl: String, datasetId: Int, apiKey: String, threshold: Double): Unit = {
    val startDate = "2024-05-05T00:00:00Z"  // Start date for data fetching
    val currentDateTime = LocalDateTime.now()
    val formattedEndTime = currentDateTime.format(DateTimeFormatter.ISO_DATE_TIME)
    val url = new URL(s"$baseUrl?datasets=$datasetId&startTime=$startDate&endTime=$formattedEndTime&format=json&locale=fi&sortBy=startTime&sortOrder=desc&api_key=$apiKey")

    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    connection.setRequestProperty("Accept", "application/json")
    connection.setRequestProperty("x-api-key", apiKey)
    connection.connect()

    try {
      val responseCode = connection.getResponseCode
      if (responseCode == HttpURLConnection.HTTP_OK) {
        val content = Source.fromInputStream(connection.getInputStream).mkString


        val json = Json.parse(content)
        val data = (json \ "data").as[List[JsValue]]
        val latestEntry = data.headOption

        val time = latestEntry.flatMap { d =>
          (d \ "startTime").validate[String].asOpt
        }.getOrElse("No time available")

        val value = latestEntry.flatMap { d =>
          (d \ "value").validate[Double].asOpt
        }.getOrElse(0.0)

        val displayTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME).format(displayFormatter)
        println(s"Latest fetched value at $displayTime\nValue: $value MW")

        if (value < alertThreshold) {
          println(s"Value $value MW is below the threshold $alertThreshold MW at $displayTime.")
          Swing.onEDT {
            try {
              Dialog.showMessage(
                message = s"Warning: Low energy output detected at $displayTime with value $value MW, which may indicate equipment malfunction.",
                title = "Energy Alert",
                messageType = Message.Warning
              )
            } catch {
              case e: Exception => println("Failed to show dialog: " + e.getMessage)
            }
          }
        }
      } else {
        println(s"Failed to fetch data: HTTP $responseCode")
      }
    } catch {
      case e: Exception => println("Error during data fetch: " + e.getMessage)
    } finally {
      connection.disconnect()
    }
  }

  // Stops the ongoing monitoring process.
  def stopMonitoring(): Unit = {
    keepRunning.set(false)
    println("Monitoring stopped.")
  }

  // Displays the most recently fetched energy data, if available.
  def showLatestData(): Unit = {
    lastFetchedData match {
      case Some((time, value)) => println(s"Latest fetched data:\nTime: $time\nValue: $value MW")
      case None => println("No data fetched yet.")
    }
  }
}
