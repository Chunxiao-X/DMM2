import java.time.{LocalDate, LocalDateTime}
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.io.StdIn
import scala.util.{Failure, Success, Try}

object EnergyPlantSystem extends App {
//  Group 15
//  Chunxiao Ren
//  Xiaoyu Du
//  Yizhou Wang

  val baseUrl = "https://data.fingrid.fi/api/data"
  val apiKey = "ceddafe691b049848ef7d29d26e98e32"
  implicit val ec: ExecutionContext = ExecutionContext.global
  val monitorInterval = 1.hour // Frequency of data fetching for monitoring
  val monitorThreshold = 100.0 // Threshold for alerts

  // Presents a menu to the user to select an energy source, handling user input and validation.
  def selectEnergySource(): Int = {
    var selection = -1  // Default as -1 for exit case
    var valid = false
    while (!valid) {
      println("\nWelcome to the Energy Plant System")
      println("Select the energy source:")
      println("1. Solar Power")
      println("2. Wind Power")
      println("3. Hydropower")
      println("0. Exit")

      val input = StdIn.readLine() // Read input as String
      Try(input.toInt) match {
        case Success(choice) => choice match {
          case 1 =>
            selection = 248
            valid = true
          case 2 =>
            selection = 75
            valid = true
          case 3 =>
            selection = 191
            valid = true
          case 0 =>
            valid = true
            println("Exit.")
          case _ =>
            println("Invalid energy source selected, please try again.")
        }
        case Failure(_) =>
          println("Invalid input. Please enter a number.")
      }
    }
    selection
  }

  def parseDateTime(input: String, inputType: String): LocalDateTime = {
    Try(LocalDateTime.parse(input + "T00:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME)) match {
      case Success(dateTime) => dateTime
      case Failure(_) =>
        println(s"Invalid date format. Please enter the $inputType in the format 'YYYY-MM-DD'.")
        parseDateTime(StdIn.readLine(s"Re-enter $inputType (YYYY-MM-DD): "), inputType)
    }
  }

  def parseDateTime(input: String): LocalDateTime = {
    Try(LocalDateTime.parse(input + "T00:00:00.000Z", DateTimeFormatter.ISO_DATE_TIME)) match {
      case Success(dateTime) => dateTime
      case Failure(_) =>
        println("Invalid date format. Please enter the date in the format 'YYYY-MM-DD'.")
        parseDateTime(StdIn.readLine("Re-enter start time (YYYY-MM-DD): "))
    }
  }

  var continueRunning = true
  while (continueRunning) {
    val datasetId = selectEnergySource()
    if (datasetId == -1) {
      continueRunning = false
    } else {
      println("Enter start time (YYYY-MM-DD): ")
      val startTime = parseDateTime(StdIn.readLine(), "start time")
      println("Enter end time (YYYY-MM-DD): ")
      val endTime = parseDateTime(StdIn.readLine(), "end time")
      val filePath = s"dataset_$datasetId-${startTime.format(DateTimeFormatter.ISO_DATE)}-to-${endTime.format(DateTimeFormatter.ISO_DATE)}.csv"
      val params = s"datasets=$datasetId&startTime=${startTime.format(DateTimeFormatter.ISO_DATE_TIME)}&endTime=${endTime.format(DateTimeFormatter.ISO_DATE_TIME)}&format=json&locale=fi&sortBy=startTime&sortOrder=asc"

      var exit = false
      while (!exit) {
        println("\n1. Start/Stop Monitoring Energy Data")
        println("2. Fetch and Save Data")
        println("3. View Energy Data Using line graphs")
        println("4. Analyze Energy Data")
        println("5. View Energy Data in List Format/Can modify")
        println("0. Exit to main menu")

        val input = StdIn.readLine()
        Try(input.toInt) match {
          case Success(choice) => choice match {
            case 1 =>
              var continueMonitoring = true
              while (continueMonitoring) {
                println("\n1. Start Monitoring")
                println("2. Stop Monitoring")
                println("0. Return to previous menu")

                val subInput = StdIn.readLine()
                Try(subInput.toInt) match {
                  case Success(subChoice) => subChoice match {
                    case 1 =>
                      println("Start monitoring")
                      EnergyMonitor.startMonitoring(baseUrl, datasetId, apiKey, monitorThreshold, monitorInterval)
                      println("Monitoring has been started. Check console for alerts.")
                    case 2 =>
                      EnergyMonitor.stopMonitoring()
                    case 0 =>
                      continueMonitoring = false
                      println("Returning to previous menu.")
                    case _ =>
                      println("Invalid option. Please enter 1, 2, or 0.")
                  }
                  case Failure(_) =>
                    println("Invalid input. Please enter a number.")
                }
              }
            case 2 =>
              try {
                // Call the DataFetcher's fetchData function, with each parenthesis representing a step of Currying passing the
                DataFetcher.fetchData(baseUrl)(params)(apiKey)(filePath)
              } catch {
                case e: Exception => println(s"Failed to fetch data: ${e.getMessage}")
              }
            case 3 =>
              println("Enter the start time for the plot (YYYY-MM-DD):")
              val plotStartTime = parseDateTime(StdIn.readLine(), "plot start time")
              println("Enter the end time for the plot (YYYY-MM-DD):")
              val plotEndTime = parseDateTime(StdIn.readLine(), "plot end time")
              DataManager.plotData(filePath, plotStartTime, plotEndTime)
            case 4 =>
              DataAnalysis.analyzeEnergyData(filePath)
            case 5 =>
              println("Enter the start time for the view (YYYY-MM-DD):")
              val viewStartTime = parseDateTime(StdIn.readLine(), "view start time")
              println("Enter the end time for the view (YYYY-MM-DD):")
              val viewEndTime = parseDateTime(StdIn.readLine(), "view end time")
              EnergyDataViewer.viewData(filePath, viewStartTime, viewEndTime)
            case 0 =>
              exit = true
              println("Exiting to main menu...")
            case _ => println("Invalid option, please try again.")
          }
          case Failure(_) =>
            println("Invalid input. Please enter a number.")
        }
      }
    }
  }
}

