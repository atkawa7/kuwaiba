/**
 * Show case how to create different types of charts
 * Neotropic SAS - version 1.1.1
 * Parameters: None
 */
import org.neo4j.graphdb.Direction;
import org.kuwaiba.services.persistence.impl.neo4j.RelTypes;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLReport
import com.neotropic.kuwaiba.modules.reporting.html.HTMLDiv;
import com.neotropic.kuwaiba.modules.reporting.javascript.DataTable;
import com.neotropic.kuwaiba.modules.reporting.javascript.DataTable.DataType;
import com.neotropic.kuwaiba.modules.reporting.javascript.wrappers.googlecharts.ChartsFactory;
import com.neotropic.kuwaiba.modules.reporting.javascript.wrappers.googlecharts.ChartsFactory.ChartType;

def report = new HTMLReport("Sample Pie Chart", "Neotropic SAS", "1.1");

def dataTable = new DataTable([DataType.STRING, DataType.NUMBER] as DataType[], ["Column 1", "Column 2"] as String[]);
dataTable.addRow(["Row 1", "20"] as String[]);
dataTable.addRow(["Row 2", "30"] as String[]);
dataTable.addRow(["Row 3", "50"] as String[]);
def chartsFactory = new ChartsFactory(report);
def htmlDivPieChart = chartsFactory.createHTMLDivWrapperChart(ChartType.PIECHART, "divPieChart", "Chart Pie Sample", dataTable);
def htmlDivLineChart = chartsFactory.createHTMLDivWrapperChart(ChartType.LINECHART, "divLinechart", "Chart Line Sample", dataTable);
def htmlDivColumnChart = chartsFactory.createHTMLDivWrapperChart(ChartType.COLUMNCHART, "divColumChart", "Chart Column Sample", dataTable);

report.getComponents().add(htmlDivPieChart);
report.getComponents().add(htmlDivLineChart);
report.getComponents().add(htmlDivColumnChart);

//Return the report
report;

