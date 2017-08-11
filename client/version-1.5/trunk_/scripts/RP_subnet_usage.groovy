/**
 * Subnet Usage report that shows information about related ip addresses and subnets
 * Neotropic SAS - version 1.2
 * Parameters: None
 */
import org.kuwaiba.apis.persistence.PersistenceService;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLReport
import com.neotropic.kuwaiba.modules.reporting.html.HTMLDiv;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLHx;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLBR;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLTable;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLRow;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLColumn;
import com.neotropic.kuwaiba.modules.reporting.html.HTMLImage;
import com.neotropic.kuwaiba.modules.reporting.javascript.DataTable;
import com.neotropic.kuwaiba.modules.reporting.javascript.DataTable.DataType;
import com.neotropic.kuwaiba.modules.reporting.plugins.gcharts.GChartsFactory;
import com.neotropic.kuwaiba.modules.reporting.plugins.gcharts.GChartsFactory.ChartType;
import com.neotropic.kuwaiba.modules.reporting.defaults.Util;
import org.kuwaiba.services.persistence.util.Constants;
import com.neotropic.kuwaiba.modules.ipam.IPAMModule;

def report = new HTMLReport("Subnet Usage", "Neotropic SAS", "1.2");

report.setEmbeddedStyleSheet(HTMLReport.getDefaultStyleSheet());

def chartsFactory = new GChartsFactory(report);

// Get Application Entity Manager reference
def aem = PersistenceService.getInstance().getApplicationEntityManager();

// Get Business Entity Manager reference
def bem = PersistenceService.getInstance().getBusinessEntityManager();

// Place the company logo
def corporateLogo = aem.getConfiguration().getProperty("corporateLogo");
if (corporateLogo == null) {
    corporateLogo = "logo.jpg"
} else {
    corporateLogo = aem.getConfiguration().getProperty("corporateLogo");
}
// Get the current subnet object
def subnet = bem.getObject(objectClassName, objectId);

def subnetChildren = bem.getObjectSpecialChildren(objectClassName, objectId);

def subnetAttributes = subnet.getAttributes();
def hosts = Integer.parseInt(subnetAttributes.get("hosts").get(0));
def usedIps = 0;

def ips = [];
def subnets = [];

ips.each {ip -> 
    def ipDevices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), IPAMModule.RELATIONSHIP_IPAMHASADDRESS);
    
    if (ipDevices.size() > isEmpty())
        usedIps++;
}
// There are not host but the
// gateway and the broadcast
// are in use
if (hosts == 0 && usedIps == 0) {
    usedIps = ips.size();
    
    if (usedIps > hosts) {
        hosts += 2;
    }
}


int freeIps = hosts - usedIps;

def vrf = "";
def vlan = "";
def service = "";
def title = "";
def subnetUsageReportText = "";

def vlans = bem.getSpecialAttribute(objectClassName, objectId, IPAMModule.RELATIONSHIP_IPAMBELONGSTOVLAN);
def vrfs = bem.getSpecialAttribute(objectClassName, objectId, IPAMModule.RELATIONSHIP_IPAMBELONGSTOVRFINSTACE);
def services = bem.getSpecialAttribute(objectClassName, objectId, "uses");
        
if(!vlans.isEmpty())
    vlan = "<b>" + vlans.get(0).getName() + " ["+ vlans.get(0).getClassName()+ "]</b> |"+
    Util.formatLocation(bem.getParents(vlans.get(0).getClassName(), vlans.get(0).getId()));
        
if(!vrfs.isEmpty())
    vrf = "<b>" + vrfs.get(0).getName() + " ["+ vrfs.get(0).getClassName()+ "]</b>";

if(!services.isEmpty())
    service = services.get(0).getName() + " ["+ services.get(0).getClassName()+ "]";


def tblInfo = new HTMLTable(null);

if (subnet == null) {
    report.setTitle("Error");
    report.getComponents().add(new HTMLDiv("", "error", "", "No information about this subnet could be found"));
} else {
    tblInfo.getRows().add(new HTMLRow([new HTMLColumn(new HTMLHx(1, "Subnet Usage Detail Report for " + subnet.getName())), new HTMLColumn(new HTMLImage(corporateLogo))] as HTMLColumn[]));
    report.getComponents().add(tblInfo);
    
    HTMLTable tblSubnets = new HTMLTable(null);
    HTMLColumn columnLabelIP = new HTMLColumn("", "generalInfoLabel", "Network IP Addres");
    HTMLColumn columnIP = new HTMLColumn("<b>" + subnetAttributes.get("networkIp").get(0) + "</b>");

    def dataTable = new DataTable([DataType.STRING, DataType.NUMBER] as DataType[], ["IP", "Usage %"] as String[]);
    dataTable.addRow(["Used", "" + usedIps] as String[]);
    dataTable.addRow(["Free", "" + freeIps] as String[]);
    // Creating the Pie Chart
    def htmlDivPieChart = chartsFactory.createHTMLDivWrapperChart(ChartType.PIECHART, "divPieChart", "Subnet Usage", dataTable);
    HTMLColumn columnChart = new HTMLColumn("width: 350px; height: 250px;", "", htmlDivPieChart);
    columnChart.setRowspan("8");

    tblSubnets.getRows().add(new HTMLRow([columnLabelIP, columnIP, columnChart] as HTMLColumn[]));

    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "Broadcast IP Address"), new HTMLColumn("<b>" + subnetAttributes.get("broadcastIp").get(0) + "</b>")] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "Description"), new HTMLColumn(subnetAttributes.get("description").get(0))] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "Number of hosts"), new HTMLColumn(hosts)] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "IPs Related to some port"), new HTMLColumn("<b>" + (usedIps*100)/hosts + "%</b> (" + usedIps + ")")] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "Free IPs"), new HTMLColumn("<b>" + (freeIps*100)/hosts + "%</b> (" + freeIps + ")")] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "VLAN"), new HTMLColumn(vlan)] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "VRF"), new HTMLColumn(vrf)] as HTMLColumn[]));
    tblSubnets.getRows().add(new HTMLRow([new HTMLColumn("", "generalInfoLabel", "Service"), new HTMLColumn(service)] as HTMLColumn[]));
    report.getComponents().add(tblSubnets);
}
subnetChildren.each { subnetChild ->
    if (subnetChild.getClassName() == Constants.CLASS_IP_ADDRESS) {
        ips.add(subnetChild);
    }
    if (subnetChild.getClassName() == Constants.CLASS_SUBNET_IPV4 || subnetChild.getClassName() == Constants.CLASS_SUBNET_IPV6) {
        subnets.add(subnetChild);
    }
}
// i is an index to define if a row are even or odd
def i = 0;
// Table for Subnets
def tblSubnets= new HTMLTable(["Subnet", "Description", "Service"] as String[]);
if (subnets.isEmpty()) {
    report.getComponents().add(new HTMLDiv("", "error", "", "There are no Subnets nested"));
} else {
    subnets.each { subnetElement ->
        service = "";
        def subnetServices = bem.getSpecialAttribute(subnetElement.getClassName(), subnetElement.getId(), "uses");
        if(subnetServices.size() > 0) {
            service = subnetServices.get(0).getName() + "[" +  subnetServices.get(0).getClassName() + "]";
        }
        def subnetObj = bem.getObject(objectClassName, subnetElement.getId());
        def attributes = subnetObj.getAttributes();

        def subnetSubnet = new HTMLColumn(subnetElement.getName());
        def subnetDescription = new HTMLColumn(attributes.get("description").getAt(0));
        def subnetElementService = new HTMLColumn(service);

        def row = new HTMLRow(i % 2 == 0 ? "even" :"odd", [subnetSubnet, subnetDescription, subnetElementService] as HTMLColumn[]);
        tblSubnets.getRows().add(row);

        i += 1;
    }
    //Assemble the components
    report.getComponents().add(new HTMLBR());
    report.getComponents().add(new HTMLHx(2, "Subnets"));
    report.getComponents().add(tblSubnets);
}
// Table for IP Addresses
def tblIPAddresses= new HTMLTable(["IP Address", "Description", "Port", "Location", "Service"] as String[]);
if (ips.isEmpty()) {
    report.getComponents().add(new HTMLDiv("", "error", "", "There are no IPs Addresses in use"));
} else {
    i = 0;
    ips.each { ip -> 
        service = "";
        def device = "";

        def ipDevices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), IPAMModule.RELATIONSHIP_IPAMHASADDRESS);
        def location = "";
        if (ipDevices.size() > 0) {
            device = ipDevices.get(0).getName() + " [" + ipDevices.get(0).getClassName()+"]";
            def parents = bem.getParents(ipDevices.get(0).getClassName(), ipDevices.get(0).getId());
            location =  Util.formatLocation(parents);
        }
        def ipServices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), "uses");
        if(ipServices.size() > 0)
            service = ipServices.get(0).getName() + "[" +  ipServices.get(0).getClassName() + "]";
        def ipObject = bem.getObject(Constants.CLASS_IP_ADDRESS, ip.getId());
        def attributes = ipObject.getAttributes();

        def ipAddress = new HTMLColumn(ip.getName());
        def ipDescription = new HTMLColumn(attributes.get("description").getAt(0));
        def ipPort = new HTMLColumn(device);
        def ipLocation = new HTMLColumn(location);
        def ipService = new HTMLColumn(service);

        def row = new HTMLRow(i % 2 == 0 ? "even" :"odd", [ipAddress, ipDescription, ipPort, ipLocation, ipService] as HTMLColumn[]);
        tblIPAddresses.getRows().add(row);

        i++;
    }
    //Assemble the components
    report.getComponents().add(new HTMLBR());
    report.getComponents().add(new HTMLHx(2, "IP Addresses"));
    report.getComponents().add(tblIPAddresses);
    report.getComponents().add(new HTMLDiv("", "footer", "", "This report is powered by <a href=\"http://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a>"));
}
//Return the report
return report;