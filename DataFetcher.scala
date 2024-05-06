import java.net.{HttpURLConnection, URL}
import java.io.{File, PrintWriter, FileWriter}
import scala.io.Source
import scala.util.Using
import play.api.libs.json._

object DataFetcher {
  //  Group 15
  //  Chunxiao Ren
  //  Xiaoyu Du
  //  Yizhou Wang

  implicit val httpURLConnectionReleasable: Using.Releasable[HttpURLConnection] = _.disconnect()

  // Passing parameters in steps using the Currying technique
  def fetchData(baseUrl: String)(params: String)(apiKey: String)(filePath: String): Unit = {
    fetchDataRecursive(baseUrl, params, apiKey, filePath, 1, true)
  }

  // Using Recursive Functions for Pagination
  private def fetchDataRecursive(baseUrl: String, params: String, apiKey: String, filePath: String, page: Int, continueFetching: Boolean): Unit = {
    if (continueFetching) {
      val urlString = s"$baseUrl?$params&pageSize=20000&page=$page&api_key=$apiKey"
      val url = new URL(urlString)
      Using.resource(url.openConnection().asInstanceOf[HttpURLConnection]) { connection =>
        connection.setRequestMethod("GET")
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("x-api-key", apiKey)
        connection.connect()

        if (connection.getResponseCode == HttpURLConnection.HTTP_OK) {
          Using.resource(Source.fromInputStream(connection.getInputStream)) { source =>
            val content = source.mkString
            val json = Json.parse(content)
            val newDataCount = saveDataAsCsv(json, filePath, page == 1, transformData, formatData)  // Pass whether headers should be written
            println(s"Page $page: $newDataCount records fetched and saved.")
            fetchDataRecursive(baseUrl, params, apiKey, filePath, page + 1, newDataCount == 20000)  // Recursive calls to itself
          }
        } else {
          throw new IllegalStateException(s"Failed to fetch data: HTTP ${connection.getResponseCode}")
        }
      }
    }
  }

  // Type parameterised saveDataAsCsv method
  def saveDataAsCsv[T](json: JsValue, filePath: String, writeHeaders: Boolean, transform: JsValue => scala.collection.Seq[T], format: T => String): Int = {
    val data = transform(json) // Apply the data transformation function

    Using.resource(new PrintWriter(new FileWriter(new File(filePath), true))) { writer =>
      if (writeHeaders && new File(filePath).length() == 0) {
        writer.println("datasetId,startTime,endTime,value") // Optionally parameterize or adjust column headers
      }
      data.foreach(record => writer.println(format(record))) // Applying the formatting function to write data into the file
    }
    data.length
  }


  // Data Conversion Functions
  def transformData(json: JsValue): Seq[Seq[String]] = {
    (json \ "data").as[JsArray].value.map { entry =>
      Seq(
        (entry \ "datasetId").as[Int].toString,
        (entry \ "startTime").as[String],
        (entry \ "endTime").as[String],
        (entry \ "value").as[Double].toString
      )
    }.toSeq  // Explicitly converting to Seq
  }

  // Data Formatting Functions
  def formatData(record: Seq[String]): String = record.mkString(",")
}
