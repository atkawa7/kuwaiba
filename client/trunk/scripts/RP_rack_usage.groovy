/**
 * Calculates the occupation of a given rack.
 * Neotropic SAS - version 1.0
 * Parameters: None
 */
import com.neotropic.kuwaiba.modules.reporting.HTMLReport;

def report = new HTMLReport(String.format("Rack Usage Report for %s", instanceNode.getProperty("name")),
				null, "Neotropic SAS", "1.1");

report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());

//Detailed table
def totalRackUnits = instanceNode.hasProperty("rackUnits") ? instanceNode.getProperty("rackUnits") : 0;
def usedRackUnits = 0;

def detailsTable = new HTMLReport.HTMLTable(null, null, ["Name", "Serial Number", "Rack Units", "Operational State"] as String[]);

instanceNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF).each { childOfRelationship ->
	def rackableNode = childOfRelationship.getStartNode();
	def rackableRackUnits = rackableNode.hasProperty("rackUnits") ? rackableNode.getProperty("rackUnits") : 0;
	usedRackUnits += rackableRackUnits;
	detailsTable.getRows().add(new HTMLReport.HTMLRow(
		      [ new HTMLReport.HTMLColumn(rackableNode.getProperty("name")),
		      new HTMLReport.HTMLColumn(rackableNode.hasProperty("serialNumber") ? rackableNode.getProperty("serialNumber") : "Not Set"),
		      new HTMLReport.HTMLColumn(rackableRackUnits),
		      new HTMLReport.HTMLColumn("Jo jo jo")] as HTMLReport.HTMLColumn[]));
	
}

//Basic info table
def headerTable = new HTMLReport.HTMLTable();
headerTable.getRows().add(new HTMLReport.HTMLRow([ new HTMLReport.HTMLColumn("Name"), new HTMLReport.HTMLColumn(instanceNode.getProperty("name")) ] as HTMLReport.HTMLColumn[]));

headerTable.getRows().add(new HTMLReport.HTMLRow([ new HTMLReport.HTMLColumn("Serial Number"), new HTMLReport.HTMLColumn(instanceNode.hasProperty("serialNumber") ? instanceNode.getProperty("serialNumber") : "Not Set") ] as HTMLReport.HTMLColumn[]));

headerTable.getRows().add(new HTMLReport.HTMLRow([ new HTMLReport.HTMLColumn("Total Rack Units"), new HTMLReport.HTMLColumn(totalRackUnits) ] as HTMLReport.HTMLColumn[]));

headerTable.getRows().add(new HTMLReport.HTMLRow([ new HTMLReport.HTMLColumn("Used Rack Units"), new HTMLReport.HTMLColumn(usedRackUnits + " (" + (usedRackUnits == 0 ? 0 : Math.round(100 * usedRackUnits/totalRackUnits)) + "%)") ] as HTMLReport.HTMLColumn[]));

//Assemble the components
report.getComponents().add(headerTable);
report.getComponents().add(detailsTable);

//Return the report
report;

