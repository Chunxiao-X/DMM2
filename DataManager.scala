import scala.io.Source
import scala.util.Try
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.jfree.chart.{ChartFactory, ChartFrame}
import org.jfree.data.category.DefaultCategoryDataset
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.axis.CategoryAxis
import org.jfree.chart.axis.CategoryLabelPositions
import scala.io.StdIn
import scala.util.{Try, Success, Failure}
import java.io.FileNotFoundException
import java.io.IOException


object DataManager {
  //  Group 15
  //  Chunxiao Ren
  //  Xiaoyu Du
  //  Yizhou Wang

  // Loads data from a CSV file, skipping the header, and filters out invalid lines.
  def loadData(filePath: String): Seq[(String, Double)] = {
    Try(Source.fromFile(filePath)) match {
      case Success(source) =>
        try {
          val lines = source.getLines().drop(1) // Skip header line
          lines.flatMap { line =>
            val parts = line.split(',')
            if (parts.length == 4) Try((parts(1), parts(3).toDouble)).toOption
            else {
              println(s"Invalid line format: $line")
              None
            }
          }.toSeq
        } finally {
          source.close()
        }

      case Failure(e: FileNotFoundException) =>
        println(s"File not found: $filePath. Please make sure you have Fetch and Save Data")
        Seq.empty[(String, Double)]  // Return an empty sequence if file is not found

      case Failure(e: IOException) =>
        println(s"Error reading from file: $e")
        Seq.empty[(String, Double)]

      case Failure(e) =>
        println(s"An unexpected error occurred: $e")
        Seq.empty[(String, Double)]
    }
  }

  // Plots a line chart for power generation data within a specified time range.
  def plotData(filePath: String, startTime: LocalDateTime, endTime: LocalDateTime): Unit = {
    val data = loadData(filePath).filter {
      case (date, _) =>
        val dateTime = LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME)
        dateTime.isAfter(startTime) && dateTime.isBefore(endTime)
    }

    if (data.isEmpty) {
      println("No valid data was loaded, unable to plot.")
    } else {
      val dataset = new DefaultCategoryDataset()

      // Calculate step size to show only 20 labels
      val stepSize = Math.max(1, data.length / 30)
      data.zipWithIndex.foreach {
        case ((date, value), index) if index % stepSize == 0 =>
          val formattedDate = date.replace("T", " ").substring(0, date.lastIndexOf(':'))
          dataset.addValue(value, "Power Generation", formattedDate)
        case _ =>
      }

      val chart = ChartFactory.createLineChart(
        "Power Generation Over Time",
        "Time",
        "Power generation: megawatt hours (MW*H)",
        dataset,
        PlotOrientation.VERTICAL,
        true, true, false
      )

      val plot = chart.getCategoryPlot()
      val xAxis = plot.getDomainAxis().asInstanceOf[CategoryAxis]
      xAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)) // Rotate labels by 45 degrees

      val frame = new ChartFrame("Energy Data Visualization", chart)
      frame.pack()
      frame.setVisible(true)
    }
  }

  // Entry point of the program; it prompts user to enter a time range and plots the data.
  def main(args: Array[String]): Unit = {
    println("Enter the custom start time for analysis (e.g., 2023-01-01T00:00:00.000Z):")
    val startTime = LocalDateTime.parse(StdIn.readLine(), DateTimeFormatter.ISO_DATE_TIME)
    println("Enter the custom end time for analysis (e.g., 2023-01-02T00:00:00.000Z):")
    val endTime = LocalDateTime.parse(StdIn.readLine(), DateTimeFormatter.ISO_DATE_TIME)

    val filePath = "path/to/your/datafile.csv"
    println("Custom Time Range")
    plotData(filePath, startTime, endTime)
  }
}

