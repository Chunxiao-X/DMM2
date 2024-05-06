In total, we used six Scala files to co-construct a Renewable Energy Plant System, these modules work in tandem to provide operators with the ability to monitor, analyse data and optimise systems in real time to improve energy production efficiency and reliability.

EnergyPlantSystem.scala
Energy Plant System: integrates all functional modules to provide an object-oriented system framework for the total management of renewable energy production and monitoring.
Overall Management: Includes coordinated management of wind turbines, solar panels and hydroelectric plants to ensure optimal energy production and utilisation.

DataFetcher.scala
Data crawling module: used to fetch energy production data from a remote API (FINGRID) and save it as a CSV file for subsequent analysis.
Efficient data scraping: recursively fetches a large amount of paged data and continues to scrape the next page when needed to maintain data continuity and integrity.
Paging Error Handling: When paging data, if a page of data is not successfully captured, the module will record the details of the failed capture and automatically retry or restore to the previous successful state to ensure the continuity and integrity of the data.
API limitations and timeout processing: set appropriate timeout parameters to prevent the programme from hanging due to delayed API response. Also, deal with API call frequency limitations by adjusting the request frequency appropriately or using fallback strategies.

DataManager.scala
Data Management Module: this module is responsible for managing the data in the Renewable Energy Plant system, including loading, filtering and transforming the data. It loads data from CSV files saved by DataFetcher.scala and transforms them into Scala data structures that are easy to handle.
Data Filtering: It is able to filter energy production data based on different time periods and equipment types.
Data Storage: It saves the converted data to a new file when needed to ensure data integrity and traceability.
Data Validation: When loading data from a CSV file, the module checks the format and integrity of each line of data. If the data is formatted incorrectly or key information is missing, the line of data is ignored and an error message is output to the console informing the operator that there is a problem loading the data.
Exception handling: Scala's Try and Catch mechanisms are used to handle possible I/O errors during file read and write operations, ensuring that the program gracefully handles exceptions and provides feedback when problems such as unreadable files or insufficient disk space are encountered.

EnergyDataViewer.scala
Data Viewer Module: this module provides an interactive graphical interface to present energy production data in a visual way.
Customisable Queries: Users can specify a time range and data filters, the code will draw a line graph with 30 average samples over the time range, and operators can view detailed information about energy production, equipment status, etc. for that time period.
Graphical Interface Presentation: Data is displayed in a tabular format, making it easy for operators to browse and compare data from different time periods.
User Input Validation: When the user queries the data, the system validates whether the date and time entered are in the correct format. If the user enters an invalid time range or an incorrect format, the system will pop up an error message through the graphical interface to prompt the user to make corrections.
Data Visualisation Error Handling: During the data visualisation process, if the loaded data is empty or does not meet the display requirements, "No Valid Data" or a corresponding error message will be displayed on the interface to avoid misleading view display.

EnergyMonitor.scala
Monitoring module: responsible for periodically grabbing data from remote APIs to monitor the status of renewable energy production equipment in real time.
Concurrent Data Grabbing: Using concurrent and asynchronous programming techniques, it implements timed and recursive data grabbing to ensure the integrity of the production system data.
Failure Alerts: Immediately alerts the operator in the event of equipment failure or abnormal energy production, such as when a piece of equipment is producing less than 100 MW of electricity.
Remote Data Capture Exception Handling: When fetching data from the API, if network problems are encountered or the API returns an error status code, such as an HTTP 500 or 404 error, the module will capture these exceptions and record an error log while retrying the request or notifying the operator to check the network connection.
Automatic Fault Detection and Alerts: The system automatically detects device operating status by analysing real-time data captured from sensors and APIs. In the event of abnormal or faulty device output, the system will automatically trigger an alert mechanism to notify the operator to take maintenance or adjustment measures.

DataAnalysis.scala
Data Analysis Module: Responsible for analysing the stored energy production data and providing statistical analysis functions.
Multi-dimensional analysis: Includes statistical indicators such as mean, median, plurality, range and midpoint values to help operators identify trends in energy production.
Data Retrieval and Sorting: Provides filters to search by time period or equipment type, and sorts data in a user-friendly manner.
Data Analysis Error Handling: When performing statistical analyses such as calculating mean, median, etc., if the data set is empty or non-numeric data exists, the module will handle these exceptions to avoid calculation errors and ensure the accuracy of the analysis results.
Result validity check: Before outputting the analysis results, a validity check of the results is performed to ensure that the statistical indicators are calculated correctly and that data issues do not lead to incorrect business decision support.

On top of this, all 'inputs after menu queries' include an error correction mechanism, whether it is entering letters or inaccessible numbers, the operator is asked to re-enter the correct response and an example of the correct input format is provided.
