/**
 * Class Level Report to GenericCommunicationsElement:
 * Shows the services associated to a given generic communications element, its children and physical connections
 * The results are also displayed as a pie chart
 * Neotropic SAS - version 1.0
 * Parameters: None
 */
import org.kuwaiba.apis.persistence.PersistenceService;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLReport;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLHx;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLTable;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLRow;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLColumn;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLColumnHeader;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLDiv;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLBR;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLImage;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.services.persistence.util.Util;
import java.text.DateFormat;
import java.util.Date;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLDiv;
import com.neotropic.kuwaiba.modules.reporting.javascript.DataTable;
import com.neotropic.kuwaiba.modules.reporting.javascript.DataTable.DataType;
import com.neotropic.kuwaiba.modules.reporting.plugins.gcharts.GChartsFactory;
import com.neotropic.kuwaiba.modules.reporting.plugins.gcharts.GChartsFactory.ChartType;
// Gets Metadata Entity Manager
def mem = PersistenceService.getInstance().getMetadataEntityManager();
// Gets Application Entity Manager
def aem = PersistenceService.getInstance().getApplicationEntityManager();
// Gets Business Entity Manager
def bem = PersistenceService.getInstance().getBusinessEntityManager();

def report = new HTMLReport(String.format("Services in Node %s", instanceNode.getProperty("name")), "Neotropic SAS", "1.0");

// def linkStyleSheet = "http://localhost/reports.css";
// Change this for the url of an style sheet or replace the line below with this line report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());
// report.getLinkedStyleSheets().add(linkStyleSheet);
report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());

def resultTable = new HTMLTable(null, "reportTable", null);
resultTable.getRows().add(new HTMLRow(
    [
    new HTMLColumnHeader(null, null, "Service Name"),
    new HTMLColumnHeader(null, null, "Service Type"),
    new HTMLColumnHeader(null, null, "Customer"),
    new HTMLColumnHeader(null, null, "Related Object"),
    ] as HTMLColumnHeader[]));

def currentObj = bem.getObject(objectClassName, objectId);

def hierarchy(RemoteBusinessObjectLight parent, ArrayList allChildren) {
def bem = PersistenceService.getInstance().getBusinessEntityManager();
    def objChildren = bem.getObjectChildren(parent.getClassName(), parent.getId(), -1);
    objChildren.each { child ->
        allChildren.add(child); 
    }
    objChildren.each { child ->
        def child_ = bem.getObject(child.getClassName(), child.getId());
        hierarchy(child_, allChildren);
    }
}
def allChildren_ = new ArrayList<>();
allChildren_.add(currentObj);

hierarchy(currentObj, allChildren_);

def allChildren_2 = new ArrayList<>();
allChildren_.each { child -> 
    def containersA = bem.getSpecialAttribute(child.getClassName(), child.getId(), "endpointA");
    if (containersA != null) {
        containersA.each { containerA ->
            allChildren_2.add(containerA);
        }
    }
    
    def containersB = bem.getSpecialAttribute(child.getClassName(), child.getId(), "endpointB");
    if (containersB != null) {
        containersB.each { containerB ->
            allChildren_2.add(containerB);
        }
    }
}
allChildren_2.each { child -> 
    allChildren_.add(child);
}

def connections = new ArrayList<>();
def communicationsPort = bem.getChildrenOfClassLightRecursive(currentObj.getId(), currentObj.getClassName(), "ConfigurationItem", -1);
communicationsPort.each { child ->
    def physicalPath = bem.getPhysicalPath(child.getClassName(), child.getId());
    physicalPath.each { aPath ->
        if (mem.isSubClass("GenericPort", aPath.getClassName())) {
            
        } else 
            allChildren_.add(aPath);        
    }
}
def serviceMap = [:];
def serviceList = new ArrayList<>();

allChildren_.each { child ->
    def associatedServices = bem.getSpecialAttribute(child.getClassName(), child.getId(), "uses"); 
    if (associatedServices == null) 
        return;
    
    associatedServices.each { associatedSrv ->   
        def theParent = bem.getFirstParentOfClass(associatedSrv.getClassName(), associatedSrv.getId(), "GenericCustomer");;

        if (theParent == null)
            return;
    
        resultTable.getRows().add(new HTMLRow(
            [
            new HTMLColumn(associatedSrv.getName() == null ? "<Not Set>" : associatedSrv.getName()),
            new HTMLColumn(associatedSrv.getClassName() == null ? "<Not Set>" : associatedSrv.getClassName()),
            new HTMLColumn(theParent.getName()),
            new HTMLColumn(child.getName() == null ? "<No set>" : child.getName() + " [" + child.getClassName() + "]"),
            ] as HTMLColumn[]));
        

        if (serviceMap[associatedSrv.getClassName()] != null) 
            serviceMap[associatedSrv.getClassName()]['quantity'] ++;
        else {
            serviceList.add(associatedSrv);
            serviceMap.put(associatedSrv.getClassName(),['quantity': 1]);            
        }
    }
}

def dataTable = new DataTable([DataType.STRING, DataType.NUMBER] as DataType[], ["Service", "Percentage"] as String[]);

serviceList.each { child ->
    def quantity = serviceMap[child.getClassName()]['quantity'];
    dataTable.addRow([child.getClassName(), Integer.toString(quantity)] as String[]);
}
def chartsFactory = new GChartsFactory(report);
def htmlDivPieChart = chartsFactory.createHTMLDivWrapperChart(ChartType.PIECHART, "divPieChart", "Summary", dataTable);

def location = Util.formatObjectList(bem.getParents(objectClassName, objectId), true, 4);

def informationTable = new HTMLTable("width: 80%;", "infoTable", null);
informationTable.getRows().add(new HTMLRow([new HTMLColumn(null, "generalInfoLabel", "<b>Element Name</b>"), new HTMLColumn(null, "generalInfoValue", currentObj.getName())] as HTMLColumn []));
informationTable.getRows().add(new HTMLRow([new HTMLColumn(null, "generalInfoLabel", "<b>Element Type</b>"), new HTMLColumn(null, "generalInfoValue", currentObj.getClassName())] as HTMLColumn []));
informationTable.getRows().add(new HTMLRow([new HTMLColumn(null, "generalInfoLabel", "<b>Location</b>"), new HTMLColumn(null, "generalInfoValue", location)] as HTMLColumn []));
informationTable.getRows().add(new HTMLRow([new HTMLColumn(null, "generalInfoLabel", "<b>Date</b>"), new HTMLColumn(null, "generalInfoValue", DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date()))] as HTMLColumn []));


def headerTable = new HTMLTable("width: 100%;", "headerTable", null);
headerTable.getRows().add(new HTMLRow([new HTMLColumn(informationTable), new HTMLColumn("width: 550px; height: 250px;", "", htmlDivPieChart)] as HTMLColumn []));

report.getComponents().add(new HTMLHx(1, String.format("Services in Node %s", instanceNode.getProperty("name"))));
report.getComponents().add(headerTable);
report.getComponents().add(resultTable);
report.getComponents().add(new HTMLDiv("", "footer", "", "This report is powered by <a href=\"http://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a>"));
report.getComponents().add(new HTMLDiv("", "footer", "", new HTMLImage("width:82px;height:62px;", null, "http://neotropic.co/img/logo_blue.png")));
return report;
