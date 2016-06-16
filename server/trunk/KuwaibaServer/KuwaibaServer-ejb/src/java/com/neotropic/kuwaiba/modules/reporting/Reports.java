/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.reporting;

import com.neotropic.kuwaiba.modules.sdh.SDHContainerLinkDefinition;
import com.neotropic.kuwaiba.modules.sdh.SDHModule;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectList;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.services.persistence.impl.neo4j.RelTypes;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * Temporary class that provides methods to build class reports
 * @author gir
 */
public class Reports {
    public static byte[] buildRackUsageReport(BusinessEntityManager bem, ApplicationEntityManager aem, long rackId) throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        RemoteBusinessObject theRack = bem.getObject("Rack", rackId);
                    
        String query = String.format("MATCH (rack)<-[:%s*1..2]-(rackable)-[:%s]->(childClass)-[:%s*]->(superClass) "
                + "WHERE id(rack) = %s AND (superClass.name=\"%s\" OR superClass.name=\"%s\") "
                + "RETURN rackable", RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, rackId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICBOX);
        HashMap<String, RemoteBusinessObjectList> result = aem.executeCustomDbCode(query);

        String rackUsageReportBody = "<!DOCTYPE html>\n" +
                                "<html lang=\"en\">\n" +
                                "  <head>\n" +
                                "    <meta charset=\"utf-8\">\n" +
                                "    <title>Rack Usage Report " + theRack.getName() + "</title>\n" +
                                getStyleSheet() +
                                "  </head>\n" +
                                "  <body><table><tr><td><h1>Rack Usage Report for " + theRack.getName() + "</h1></td><td><img src=\"http://afr-ix.com/wp-content/themes/twentyfourteen/images/afrix_logo.png\"/></td></tr></table>\n";

        int totalRackUnits;
        int usedRackUnits = 0;
        float usedPercentage = 0;
        String equipmentList = "";
        String rackInfo = "";
        String rackLevelIndicator = "ok";


        List<RemoteBusinessObjectLight> parents = bem.getParents(theRack.getClassName(), theRack.getId());
        String location = formatLocation(parents);

        totalRackUnits = theRack.getAttributes().get("rackUnits") == null ? 0 : Integer.valueOf(theRack.getAttributes().get("rackUnits").get(0));

        if (!result.get("rackable").getList().isEmpty()) {
            equipmentList += "<table><tr><th>Name</th><th>Serial Number</th><th>Rack Units</th><th>Operational State</th></tr>\n";
            int i = 0;
            for (RemoteBusinessObject leaf : result.get("rackable").getList()) { //This row should contain the equipment
                usedRackUnits += leaf.getAttributes().get("rackUnits") == null ? 0 : Integer.valueOf(leaf.getAttributes().get("rackUnits").get(0));

                String operationalState = leaf.getAttributes().get("state") == null ? "<span class=\"error\">Not Set</span>" : 
                        bem.getObjectLight("OperationalState", Long.valueOf(leaf.getAttributes().get("state").get(0))).getName();

                equipmentList += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\"><td>" + leaf + "</td>"
                        + "<td>" + (leaf.getAttributes().get("serialNumber") == null ? "<span class=\"error\">Not Set</span>" : leaf.getAttributes().get("serialNumber").get(0)) + "</td>"
                        + "<td>" + (leaf.getAttributes().get("rackUnits") == null ? "<span class=\"error\">Not Set</span>" : leaf.getAttributes().get("rackUnits").get(0)) + "</td>"
                        + "<td>" + operationalState + "</td></tr>";
                i++;
            }
            usedPercentage = totalRackUnits == 0 ? 0 : usedRackUnits * 100 / totalRackUnits;

            if (usedPercentage > 50 && usedPercentage < 80)
                rackLevelIndicator = "warning";
            else
                if (usedPercentage > 80)
                    rackLevelIndicator = "error";

            equipmentList += "</table>\n";

        } else
            equipmentList += "<div class=\"warning\">No elements where found in this rack</div>\n";

        //General Info
        rackInfo += "<table>" +
            "<tr><td class=\"generalInfoLabel\">Name</td><td class=\"generalInfoValue\">" + theRack.getName() + "</td></tr>\n" +
            "<tr><td class=\"generalInfoLabel\">Serial Number</td><td class=\"generalInfoValue\">" + (theRack.getAttributes().get("serialNumber") == null ? "<span class=\"error\">Not Set</span>" : theRack.getAttributes().get("serialNumber").get(0)) + "</td></tr>\n" +
            "<tr><td class=\"generalInfoLabel\">Location</td><td class=\"generalInfoValue\">" + location  + "</td></tr>\n" +
            "<tr><td class=\"generalInfoLabel\">Total Rack Units</td><td class=\"generalInfoValue\">" + (totalRackUnits == 0 ? "<span class=\"error\">Not Set</span>" : totalRackUnits)  + "</td></tr>\n" +
            "<tr><td class=\"generalInfoLabel\">Used Rack Units</td><td class=\"generalInfoValue\">" + usedRackUnits + "</td></tr>\n" +
            "<tr><td class=\"generalInfoLabel\">Use Percentage</td><td class=\"generalInfoValue\"><span class=\"" + rackLevelIndicator + "\">" + usedPercentage + "&#37;</span></td></tr>\n"
            + "</table>";


        rackUsageReportBody += rackInfo;
        rackUsageReportBody += equipmentList;

        rackUsageReportBody += "  <div class=\"footer\">This report is powered by <a href=\"http://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a></div></body>\n" +
                                "</html>";

        return rackUsageReportBody.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] buildDistributionFrameDetailReport(BusinessEntityManager bem, ApplicationEntityManager aem, String frameClass, long frameId) throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        RemoteBusinessObject theFrame =  bem.getObject(frameClass, frameId);
        List<RemoteBusinessObjectLight> frameChildren = bem.getObjectChildren(frameClass, frameId, -1);
        
        String frameUsageReportText = "<!DOCTYPE html>\n" +
                                "<html lang=\"en\">\n" +
                                "  <head>\n" +
                                "    <meta charset=\"utf-8\">\n" +
                                "    <title>Frame Usage Report for " + theFrame.getName() + "</title>\n" +
                                getStyleSheet() +
                                "  </head>\n" +
                                "  <body><table><tr><td><h1>Frame Usage Report for " + theFrame.getName() + "</h1></td><td><img src=\"http://afr-ix.com/wp-content/themes/twentyfourteen/images/afrix_logo.png\"/></td></tr></table>\n";
        String portList = "";
        int usedPorts = 0;
        
        if (frameChildren.isEmpty())
            portList += "<div class=\"warning\">No ports where found in this frame</div>\n";
        else {
            portList += "<table><tr><th>Port Name</th><th>Operational State</th><th>Connected Equipment</th><th>Services</th></tr>\n";
            int i = 0;
            for (RemoteBusinessObjectLight aPort : frameChildren) {
                String serviceString = "", connectedEquipmentString;
                
                //Next equipment
                String query = String.format("MATCH (framePort)-[relationA:%s]-(connection)-[relationB:%s]-(equipmentPort)-[:%s*]->(equipment)-[:%s]->(childClass)-[:%s*]->(superClass) "
                            + "WHERE id(framePort) = %s  AND (relationA.name =\"%s\" OR  relationA.name =\"%s\") AND (relationB.name =\"%s\" OR  relationB.name =\"%s\")  AND (superClass.name=\"%s\" OR superClass.name=\"%s\") "
                            + "RETURN equipment, equipmentPort", RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.CHILD_OF, 
                                    RelTypes.INSTANCE_OF, RelTypes.EXTENDS, aPort.getId(), "endpointA", "endpointB", "endpointA", "endpointB", 
                                    Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, Constants.CLASS_GENERICBOX);
                
                HashMap<String, RemoteBusinessObjectList> nextEquipmentResult = aem.executeCustomDbCode(query);
                
                if (nextEquipmentResult.get("equipment").getList().isEmpty())
                    connectedEquipmentString  = "Free";
                else {
                    connectedEquipmentString = "<b>" + nextEquipmentResult.get("equipment").getList().get(0) + "</b>:" + nextEquipmentResult.get("equipmentPort").getList().get(0).getName();
                    usedPorts ++;
                }
                
                //Services
                query = String.format("MATCH (framePort)<-[relation:%s]-(service)-[:%s*]->(customer)-[:%s]->(customerClass)-[:%s*]->(customerSuperClass) "
                        + "WHERE id(framePort) = %s AND relation.name = \"%s\" AND customerSuperClass.name=\"%s\""
                        + "RETURN service, customer", RelTypes.RELATED_TO_SPECIAL, 
                                RelTypes.CHILD_OF_SPECIAL, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, 
                                aPort.getId(), "uses", Constants.CLASS_GENERICCUSTOMER);
                
                HashMap<String, RemoteBusinessObjectList> serviceResult = aem.executeCustomDbCode(query);
                
                for (int j = 0; j < serviceResult.get("service").getList().size(); j++)
                    serviceString += "<b>" + serviceResult.get("service").getList().get(j) + "</b> - " + serviceResult.get("customer").getList().get(j) + "<br/>";
                
                //Operational State
                query = String.format("MATCH (framePort)-[relation:%s]->(listType) "
                        + "WHERE id(framePort) = %s AND relation.name=\"%s\" RETURN listType", RelTypes.RELATED_TO, aPort.getId(), "state");
                
                HashMap<String, RemoteBusinessObjectList> operationalStateResult = aem.executeCustomDbCode(query);
                
                String operationalStateString = "<span class=\"error\">Not Set</span>";
                
                if (!operationalStateResult.get("listType").getList().isEmpty())
                    operationalStateString = operationalStateResult.get("listType").getList().get(0).getName();
                
                portList += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\"><td>" + aPort.getName() + "</td>\n"
                        + "<td>" + operationalStateString + "</td>\n"
                        + "<td>" + connectedEquipmentString + "</td>\n"
                        + "<td>" + serviceString + "</td></tr>\n";
                i++;
            }
            portList += "</table>\n";
            
            List<RemoteBusinessObjectLight> parents = bem.getParents(theFrame.getClassName(), theFrame.getId());
            String location = formatLocation(parents);
            float usePercentage = frameChildren.isEmpty() ? 0 : (usedPorts * 100 / frameChildren.size());
            
            frameUsageReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td class=\"generalInfoValue\">" + theFrame.getName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Serial Number</td><td class=\"generalInfoValue\"></td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Location</td><td class=\"generalInfoValue\">" + location + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Number of Ports</td><td class=\"generalInfoValue\">" + frameChildren.size() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Available Ports</td><td class=\"generalInfoValue\">" + (frameChildren.size() - usedPorts) + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Use Percentage</td><td class=\"generalInfoValue\"><span class=\"" + (usePercentage < 50 ? "ok" : (usePercentage < 80 ? "warning" : "error"))+ "\">" + usePercentage + "&#37;</span></td></tr>"
                    + "</table>\n";
            
        }
        
        frameUsageReportText += portList;
        frameUsageReportText += "  <div class=\"footer\">This report is powered by <a href=\"http://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a></div></body>\n" +
                                "</html>";
        
        return frameUsageReportText.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] buildTransportLinkUsageReport (BusinessEntityManager bem, ApplicationEntityManager aem, String transportLinkClass, long transportLinkId) throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        String query = String.format("MATCH (transportLink)-[relation:%s]-(port)-[:%s*]->(equipment)-[:%s]->(class)-[:%s*]->(superClass) "
                    + "WHERE id(transportLink) = %s AND superClass.name = \"%s\" AND (relation.name = \"%s\" OR relation.name = \"%s\")"
                    + "RETURN transportLink, equipment, port",  RelTypes.RELATED_TO_SPECIAL, RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, 
                            RelTypes.EXTENDS, transportLinkId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, 
                            SDHModule.RELATIONSHIP_SDHTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTLENDPOINTB);
        HashMap<String, RemoteBusinessObjectList> theResult = aem.executeCustomDbCode(query);
        
        String title, transportLinkUsageReportText;
        RemoteBusinessObject theTransportLink;
        
        if (theResult.get("transportLink").getList().isEmpty()) {
            title = "Error";
            transportLinkUsageReportText = getHeader(title);
            transportLinkUsageReportText += "<div class=\"error\">No information about this transport link could be found</div>";
        }
        else {
            theTransportLink = theResult.get("transportLink").getList().get(0);
            title = "Transport Link Usage Report for " + theTransportLink.getName();
            transportLinkUsageReportText = getHeader(title);
            transportLinkUsageReportText += 
                                "  <body><table><tr><td><h1>" + title + "</h1></td><td><img src=\"http://afr-ix.com/wp-content/themes/twentyfourteen/images/afrix_logo.png\"/></td></tr></table>\n";
            
            //General Info
            transportLinkUsageReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td class=\"generalInfoValue\">" + theTransportLink.getName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Type</td><td class=\"generalInfoValue\">" + theTransportLink.getClassName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Endpoint A</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(0) + "</b>:" + theResult.get("port").getList().get(0).getName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Endpoint B</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(1) + "</b>:" + theResult.get("port").getList().get(1).getName() + "</td></tr></table>";
            
            //Structure
            String transportLinkStructure;
            SDHModule sdhModule = (SDHModule)aem.getCommercialModule("SDH Networks Module"); //NOI18N
            List<SDHContainerLinkDefinition> sdhTransportLinkStructure = sdhModule.getSDHTransportLinkStructure(transportLinkClass, transportLinkId);
            int totalLinkCapacity = SDHModule.calculateTransportLinkCapacity(transportLinkClass);
            if (totalLinkCapacity == 0)
                transportLinkStructure = "<div class=\"error\">The transport link class does not allow automatic capacity calculation</div>";
            else {
                transportLinkStructure = "<table><tr><th>Position</th><th>Container Name</th><th>Structured</th></tr>\n";
                SDHContainerLinkDefinition[] allContainers = new SDHContainerLinkDefinition[totalLinkCapacity];
                for (SDHContainerLinkDefinition containerDefinition : sdhTransportLinkStructure) {
                    int firstPosition = containerDefinition.getPositions().get(0).getPosition();
                    //Handles concatenated containers
                    allContainers[firstPosition - 1] = containerDefinition;
                    int adjacentPositions = SDHModule.calculateContainerLinkCapacity(containerDefinition.getContainer().getClassName()) - 1;
                    for (int i = firstPosition; i < firstPosition + adjacentPositions; i ++ )
                        allContainers[i] = containerDefinition;
                }
                
                for (int i = 0; i < allContainers.length; i++) {
                    if (allContainers[i] == null)
                        transportLinkStructure += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\"><td>" + (i + 1) + "</td><td>Free</td><td>NA</td></tr>";
                    else
                        transportLinkStructure += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\"><td>" + (i + 1) + "</td><td>" + allContainers[i].getContainer() + "</td><td>" + (allContainers[i].isStructured() ? "Yes" : "No") + "</td></tr>";
                }
                
                transportLinkStructure += "</table>";
            }
            transportLinkUsageReportText += transportLinkStructure;
        }
        
        transportLinkUsageReportText += getFooter();
        
        return transportLinkUsageReportText.getBytes(StandardCharsets.UTF_8);
    }

    //Helpers
    private static String getStyleSheet() {
        return "<style> " +
                    "   body {\n" +
                    "            font-family: Helvetica, Arial, sans-serif;\n" +
                    "            font-size: small;\n" +
                    "            padding: 5px 10px 5px 10px;\n" +
                    "   }\n" +
                    "   table {\n" +
                    "            border: hidden;\n" +
                    "            width: 100%;\n" +
                    "          }\n" +
                    "   th {\n" +
                    "            background-color: #B1D2F3;\n" +
                    "   }\n" +
                    "   td {\n" +
                    "            padding: 5px 5px 5px 5px;\n" +
                    "   }\n" +
                    "   div {\n" +
                    "            padding: 5px 5px 5px 5px;\n" +
                    "   }\n" +
                    "   div.warning {\n" +
                    "            background-color: #FFF3A2;\n" +
                    "            text-align: center;\n" +
                    "   }\n" +
                    "   div.error {\n" +
                    "            background-color: #FFD9C7;\n" +
                    "            text-align: center;\n" +
                    "   }\n" +
                    "   div.footer {\n" +
                    "            width: 100%;\n" +
                    "            text-align: center;\n" +
                    "            font-style: italic;\n" +
                    "            font-size: x-small;\n" +
                    "            color: #848484;\n" +
                    "   }\n" +
                    "   span.ok {\n" +
                    "            color: green;\n" +
                    "   }\n" +
                    "   span.warning {\n" +
                    "            color: orange;\n" +
                    "   }\n" +
                    "   span.error {\n" +
                    "            color: red;\n" +
                    "   }\n" +
                    "   td.generalInfoLabel {\n" +
                    "            background-color: #E8E8E8;\n" +
                    "            width: 20%;\n" +
                    "            font-weight: bold;\n" +
                    "   }\n" +
                    "   td.generalInfoValue {\n" +
                    "            background-color: white;\n" +
                    "   }\n" +
                    "   tr.even {\n" +
                    "            background-color: #AAE033;\n" +
                    "   }\n" +
                    "   tr.odd {\n" +
                    "            background-color: #D1F680;\n" +
                    "   }" +
                     "</style>\n";
    }
    
    private static String getHeader(String title){
        return "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"utf-8\">\n" +
                    "    <title>" + title + "</title>\n" +
                    getStyleSheet() +
                    "  </head>\n";
    }
    
    private static String getFooter() {
        return "  <div class=\"footer\">This report is powered by <a href=\"http://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a></div></body>\n" +
                                "</html>";
    }
    
    private static String formatLocation (List<RemoteBusinessObjectLight> containmentHierarchy) {
        String location = "";
        for (int i = 0; i < containmentHierarchy.size() - 1; i ++)
            location += containmentHierarchy.get(i).toString() + " | ";
        
        return location;
    }
}
