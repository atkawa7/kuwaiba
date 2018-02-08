/**
 * Class level report that shows the Physical Path for a port in a table fashion
 * Neotropic SAS - version 1.1.1
 * Parameters: None
 */
import org.kuwaiba.apis.persistence.PersistenceService;
import com.neotropic.kuwaiba.modules.reporting.html.*;

def report = new HTMLReport(String.format("Physical Path Report for %s", instanceNode.getProperty("name")),
				"Neotropic SAS", "1.1.1");

report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());

//Get the Business Entity Manager instance
def bem = PersistenceService.getInstance().getBusinessEntityManager();

//This method will give us the full physical trace from the selected port
def physicalTraceElements = bem.getPhysicalPath(objectClassName, objectId)

def physicalTraceTable = new HTMLTable(["Name", "Type", "Location"] as String[]);

def i = 0;
physicalTraceElements.each { elementInPhysicalTrace ->
	def parents = bem.getParents(objectClassName, objectId);
	physicalTraceTable.getRows().add(new HTMLRow(null, (i % 2 == 0 ? "odd" : "even"),
									 [ new HTMLColumn(elementInPhysicalTrace.getName()),
									   new HTMLColumn(elementInPhysicalTrace.getClassName()),
									   new HTMLColumn(parents.join(' | '))
									 ] as HTMLColumn[]));
	i++;
}

//Simple function to format the location of an object. This will be added to the reporting API of Kuwaiba 1.5
//def formatLocation

//Assemble the components
report.getComponents().add(physicalTraceTable);

//Return the report
report;

