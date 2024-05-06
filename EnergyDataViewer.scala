import scala.swing._
import scala.swing.BorderPanel.Position._
import java.awt.{Color, Dimension, Font}
import javax.swing.table.DefaultTableModel
import scala.io.Source
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import scala.util.Try

object EnergyDataViewer {
  //  Group 15
  //  Chunxiao Ren
  //  Xiaoyu Du
  //  Yizhou Wang

  Locale.setDefault(Locale.ENGLISH)  // Set the default locale to English
  val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")

  // Loads energy data from a CSV file into a sequence of arrays, each containing formatted start time, end time, and value.
  def loadData(filePath: String): Seq[Array[Any]] = {
    val source = Source.fromFile(filePath)
    try {
      // Read each line and assume each line represents data at 15-minute intervals
      source.getLines().drop(1).flatMap { line =>
        val parts = line.split(',')
        if (parts.length == 4) {
          Try {
            val startDateTime = LocalDateTime.parse(parts(1), dateTimeFormatter)
            val endDateTime = startDateTime.plusMinutes(15)
            val value = parts(3).toDouble
            Array[Any](startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
              endDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
              value)
          }.toOption
        } else {
          println(s"Invalid line format: $line")
          None
        }
      }.toSeq
    } finally {
      source.close()
    }
  }

  // Filters and displays energy data within a specified time range using a graphical table in a Swing Frame.
  def viewData(filePath: String, startTime: LocalDateTime, endTime: LocalDateTime): Unit = {
    val data = loadData(filePath).filter {
      case Array(start: String, end: String, value: Double) =>
        val startDateTime = LocalDateTime.parse(start, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        val endDateTime = LocalDateTime.parse(end, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        (startDateTime.isEqual(startTime) || startDateTime.isAfter(startTime)) &&
          (endDateTime.isEqual(endTime) || endDateTime.isBefore(endTime))
    }

    if (data.isEmpty) {
      Dialog.showMessage(null, "No valid data was loaded, unable to view.", title="Data Viewer")
    } else {
      val columnNames = Array("Start Time", "End Time", "Value")
      val rowData = data.toArray // Convert Vector to Array

      val table = new Table(rowData, columnNames) {
        showGrid = true
        gridColor = Color.GRAY
        font = new Font("SansSerif", Font.PLAIN, 18)
      }

      val frame = new Frame {
        title = "Energy Data Viewer"
        contents = new BorderPanel {
          layout(new ScrollPane(table)) = Center
        }
        size = new Dimension(800, 600)
        visible = true
      }
    }
  }
}
