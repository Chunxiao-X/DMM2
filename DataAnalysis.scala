import java.time.LocalDateTime
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import scala.io.StdIn
import scala.util.Try
import scala.util.{Try, Success, Failure}
import java.io.File
import scala.io.Source

object DataAnalysis {
  //  Group 15
  //  Chunxiao Ren
  //  Xiaoyu Du
  //  Yizhou Wang

  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  // Loads and parses data from a specified CSV file into a sequence of DataRecord objects, handling errors gracefully.
  def loadData(filePath: String): Seq[DataRecord] = {
    val file = new File(filePath)
    if (!file.exists()) {
      println(s"File not found: $filePath. Please make sure you have 'Fetch and Save Data' and try again.")
      Seq.empty
    } else {
      val source = Source.fromFile(filePath)
      val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      try {
        val dataRecords = source.getLines().drop(1).flatMap { line =>
          val cols = line.split(",")
          if (cols.length == 4) {
            Try {
              DataRecord(
                cols(0).toInt,
                LocalDateTime.parse(cols(1), dateTimeFormatter),
                LocalDateTime.parse(cols(2), dateTimeFormatter),
                cols(3).toDouble
              )
            }.toOption
          } else {
            println(s"Invalid line format: $line")
            None
          }
        }.toSeq
        dataRecords
      } finally {
        source.close()
      }
    }
  }

  // Provides an interactive analysis session where the user can select different time granularities for analyzing energy data.
  def analyzeEnergyData(filePath: String): Unit = {
    var continueAnalysis = true
    while (continueAnalysis) {
      println("\nSelect the granularity of the data analysis:")
      println("1. Hourly")
      println("2. Daily")
      println("3. Weekly")
      println("4. Monthly")
      println("5. Custom Time Range")
      println("0. Exit Analysis")

      val input = StdIn.readLine("Enter your choice (0 to exit, 1-5 for analysis options): ")
      Try(input.toInt) match {
        case Success(choice) =>
          if (choice == 0) {
            continueAnalysis = false
          } else if (choice >= 1 && choice <= 5) {
            var validTime = false
            var startTime = LocalDateTime.now()
            while (!validTime) {
              println("Enter the start time for analysis (e.g., 2023-01-01T00:00:00.000Z):")
              Try(LocalDateTime.parse(StdIn.readLine(), DateTimeFormatter.ISO_DATE_TIME)) match {
                case Success(parsedTime) =>
                  startTime = parsedTime
                  validTime = true
                case Failure(_) =>
                  println("Invalid date format. Please enter the date and time in the format 'YYYY-MM-DDTHH:MM:SS.SSSZ'. Try again.")
              }
            }

            // Determination of the end time
            val endTime = choice match {
              case 1 => startTime.plusHours(1)
              case 2 => startTime.plusDays(1)
              case 3 => startTime.plusWeeks(1)
              case 4 => startTime.plusMonths(1)
              case 5 =>
                println("Enter the custom end time for analysis (e.g., 2023-01-02T00:00:00.000Z):")
                LocalDateTime.parse(StdIn.readLine(), DateTimeFormatter.ISO_DATE_TIME)
            }
            val data = loadData(filePath)
            if (data.isEmpty) {
              println(s"File not found: $filePath. Please make sure you have 'Fetch and Save Data' and try again.")
            } else {
              // Filter data based on selected time granularity
              val filteredData = data.filter(record => record.startTime.isEqual(startTime) || (record.startTime.isAfter(startTime) && record.startTime.isBefore(endTime)))
              if (filteredData.isEmpty) {
                println("No available data for the selected date. Please choose another date.")
              } else {
                println(f"Mean: ${mean(filteredData.map(_.value))}%.2f")
                println(f"Median: ${median(filteredData.map(_.value))}%.2f")
                println(f"Mode: ${mode(filteredData.map(_.value))}%.2f")
                println(f"Range: ${range(filteredData.map(_.value))}%.2f")
                println(f"Midrange: ${midrange(filteredData.map(_.value))}%.2f")
              }
            }
          } else {
            println("Unreachable number, please enter 0-5.")
          }
        case Failure(_) =>
          println("Invalid input. Please enter a number.")
      }
    }
  }

  case class DataRecord(datasetId: Int, startTime: LocalDateTime, endTime: LocalDateTime, value: Double)

  def mean(data: Seq[Double]): Double = if (data.isEmpty) 0 else data.sum / data.length
  def median(data: Seq[Double]): Double = {
    val sorted = data.sorted
    if (sorted.size % 2 == 1) sorted(sorted.size / 2)
    else (sorted(sorted.size / 2 - 1) + sorted(sorted.size / 2)) / 2.0
  }
  def mode(data: Seq[Double]): Double = {
    data.groupBy(identity).mapValues(_.size).maxBy(_._2)._1
  }
  def range(data: Seq[Double]): Double = data.max - data.min
  def midrange(data: Seq[Double]): Double = (data.max + data.min) / 2
}