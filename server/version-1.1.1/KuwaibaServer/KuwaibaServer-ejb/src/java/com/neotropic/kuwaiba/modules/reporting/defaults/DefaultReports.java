/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.reporting.defaults;

import com.neotropic.kuwaiba.modules.ipam.IPAMModule;
import com.neotropic.kuwaiba.modules.mpls.MPLSModule;
import com.neotropic.kuwaiba.modules.sdh.SDHContainerLinkDefinition;
import com.neotropic.kuwaiba.modules.sdh.SDHModule;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.AnnotatedRemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectList;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.services.persistence.impl.neo4j.RelTypes;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * Temporary class that provides methods to build class reports
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class DefaultReports {
    
    private MetadataEntityManager mem;
    private ApplicationEntityManager aem;
    private BusinessEntityManager bem;
    public String corporateLogo;
    
    public DefaultReports(MetadataEntityManager mem, BusinessEntityManager bem, ApplicationEntityManager aem) {
        this.mem = mem;
        this.aem = aem;
        this.bem = bem;
        this.corporateLogo = aem.getConfiguration().getProperty("corporateLogo") == null ? "logo.jpg" : aem.getConfiguration().getProperty("corporateLogo");
    }
       
    public RawReport buildRackUsageReport(long rackId) throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
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
                                "  <body><table><tr><td><h1>Rack Usage Report for " + theRack.getName() + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";

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

        return new RawReport("Rack Usage", "Neotropic SAS","1.1", rackUsageReportBody);
    }

    public RawReport buildDistributionFrameDetailReport(String frameClass, long frameId) throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        RemoteBusinessObject theFrame =  bem.getObject(frameClass, frameId);
        List<RemoteBusinessObjectLight> frameChildren = bem.getObjectChildren(frameClass, frameId, -1);
        
        String frameUsageReportText = "<!DOCTYPE html>\n" +
                                "<html lang=\"en\">\n" +
                                "  <head>\n" +
                                "    <meta charset=\"utf-8\">\n" +
                                "    <title>Frame Usage Report for " + theFrame.getName() + "</title>\n" +
                                getStyleSheet() +
                                "  </head>\n" +
                                "  <body><table><tr><td><h1>Frame Usage Report for " + theFrame.getName() + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
        String portList = "";
        int usedPorts = 0;
        
        if (frameChildren.isEmpty())
            portList += "<div class=\"warning\">No ports where found in this frame</div>\n";
        else {
            portList += "<table><tr><th>Port Name</th><th>Operational State</th><th>Connected Equipment</th><th>Services</th></tr>\n";
            int i = 0;
            //Collecti
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
        
        return new RawReport("Distribution Frame Detail", "Neotropic SAS","1.1", frameUsageReportText);
    }

    public RawReport buildTransportLinkUsageReport (String transportLinkClass, long transportLinkId) throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
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
                                "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
            
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
        
        return new RawReport("Transport Link Usage", "Neotropic SAS","1.1", transportLinkUsageReportText);
    }
    
    public RawReport buildLowOrderTributaryLinkDetailReport (String tributaryLinkClass, long tributaryLinkId) throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        String query = String.format("MATCH (customerSuperClass)<-[:%s*]-(customerClass)<-[:%s]-(customer)<-[:%s*]-(service)-[relationA:%s]->(tributaryLink)-[relationB:%s]-(port)-[:%s*]->(equipment)-[:%s]->(class)-[:%s*]->(superClass) "
                + "WHERE id(tributaryLink) = %s AND superClass.name=\"%s\" AND relationA.name = \"%s\" AND (relationB.name = \"%s\" OR relationB.name = \"%s\") AND customerSuperClass.name=\"%s\" RETURN tributaryLink, customer, service, port, equipment", 
                    RelTypes.EXTENDS, RelTypes.INSTANCE_OF, RelTypes.CHILD_OF_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, 
                    RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, tributaryLinkId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, "uses", 
                    SDHModule.RELATIONSHIP_SDHTTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTTLENDPOINTB, Constants.CLASS_GENERICCUSTOMER);
        HashMap<String, RemoteBusinessObjectList> theResult = aem.executeCustomDbCode(query);
        
        String title, tributaryLinkUsageReportText;
        RemoteBusinessObject theTributaryLink;
        
        if (theResult.get("tributaryLink").getList().isEmpty()) {
            title = "Error";
            tributaryLinkUsageReportText = getHeader(title);
            tributaryLinkUsageReportText += "<div class=\"error\">No information about this tributary link could be found</div>";
        }
        else {
            theTributaryLink = theResult.get("tributaryLink").getList().get(0);
            title = "Tributary Link Details Report for " + theTributaryLink.getName();
            tributaryLinkUsageReportText = getHeader(title);
            tributaryLinkUsageReportText += 
                                "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
            
            //Demarcation points
            query = String.format("MATCH (tributaryLink)-[relationA:%s]-(equipmentPort)-[relationB:%s]-(physicalConnection)-[relationC:%s]-(nextEquipmentPort)-[:%s*]->(nextEquipment)-[:%s]->(class)-[:%s*]->(superClass) "
                + "WHERE id(tributaryLink) = %s AND (relationA.name = \"%s\" OR relationA.name = \"%s\") "
                    + "AND (relationB.name = \"%s\" OR relationB.name = \"%s\") "
                    + "AND (relationC.name = \"%s\" OR relationC.name = \"%s\") "
                    + "AND superClass.name=\"%s\" "
                + "RETURN nextEquipmentPort, nextEquipment", RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, 
                                    tributaryLinkId, SDHModule.RELATIONSHIP_SDHTTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTTLENDPOINTB,
                                    "endpointA", "endpointB", "endpointA", "endpointB", Constants.CLASS_GENERICDISTRIBUTIONFRAME);
            
            HashMap<String, RemoteBusinessObjectList> demarcationPoints = aem.executeCustomDbCode(query);
            String demarcationPointsAsSring = "";
            for (int i = 0; i < demarcationPoints.get("nextEquipmentPort").getList().size(); i++)
                demarcationPointsAsSring += "<b>" + demarcationPoints.get("nextEquipment").getList().get(i) + "</b>:" + demarcationPoints.get("nextEquipmentPort").getList().get(i) + "<br/>";
            
            //General Info
            tributaryLinkUsageReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td class=\"generalInfoValue\">" + theTributaryLink.getName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Type</td><td class=\"generalInfoValue\">" + theTributaryLink.getClassName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Endpoint A</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(0) + "</b>:" + theResult.get("port").getList().get(0).getName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Endpoint B</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(1) + "</b>:" + theResult.get("port").getList().get(1).getName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Demarcation Points</td><td class=\"generalInfoValue\">" + demarcationPointsAsSring + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Service</td><td class=\"generalInfoValue\">" + theResult.get("service").getList().get(0).getName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Customer</td><td class=\"generalInfoValue\">" + theResult.get("customer").getList().get(0).getName() + "</td></tr></table>";
        
            //Used resources
            List<RemoteBusinessObjectLight> container = bem.getSpecialAttribute(tributaryLinkClass, tributaryLinkId, SDHModule.RELATIONSHIP_SDHDELIVERS);
            
            String usedResources;
            if (container.isEmpty())
                usedResources = "<div class=\"error\">This tributary link seems malformed and does not have a path</div>";
            else {
                List<AnnotatedRemoteBusinessObjectLight> structured = bem.getAnnotatedSpecialAttribute(container.get(0).getClassName(), 
                        container.get(0).getId(), SDHModule.RELATIONSHIP_SDHCONTAINS);
                usedResources = "<table><tr><th>Structured Name</th><th>Position in Container</th><th>Transport Links</th></tr>";
                int i = 0;
                for (AnnotatedRemoteBusinessObjectLight aStructured : structured) {
                    String transportLinksString = "";
                    
                    List<AnnotatedRemoteBusinessObjectLight> transportLinks = 
                            bem.getAnnotatedSpecialAttribute(aStructured.getObject().getClassName(), aStructured.getObject().getId(), SDHModule.RELATIONSHIP_SDHTRANSPORTS);
                    
                    for (AnnotatedRemoteBusinessObjectLight transportLink : transportLinks)
                        transportLinksString += transportLink.getProperties().get(SDHModule.PROPERTY_SDHPOSITION) + " - " + transportLink.getObject() + "<br/>";
                    
                    usedResources += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + aStructured.getObject() + "</td>"
                                    + "<td>" + asKLM((int)aStructured.getProperties().get(SDHModule.PROPERTY_SDHPOSITION)) + "</td>"
                                    + "<td>" + transportLinksString + "</td></tr>";
                    
                    i ++;
                }
                usedResources += "</table>";
            }
            tributaryLinkUsageReportText += usedResources;
        }
        tributaryLinkUsageReportText += getFooter();
        
        return new RawReport("Tributary Link Details", "Neotropic SAS","1.1", tributaryLinkUsageReportText);
    }

    public RawReport buildHighOrderTributaryLinkDetailReport (String tributaryLinkClass, long tributaryLinkId) throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        String query = String.format("MATCH (customerSuperClass)<-[:%s*]-(customerClass)<-[:%s]-(customer)<-[:%s*]-(service)-[relationA:%s]->(tributaryLink)-[relationB:%s]-(port)-[:%s*]->(equipment)-[:%s]->(class)-[:%s*]->(superClass) "
                + "WHERE id(tributaryLink) = %s AND superClass.name=\"%s\" AND relationA.name = \"%s\" AND (relationB.name = \"%s\" OR relationB.name = \"%s\") AND customerSuperClass.name=\"%s\" RETURN tributaryLink, customer, service, port, equipment", 
                    RelTypes.EXTENDS, RelTypes.INSTANCE_OF, RelTypes.CHILD_OF_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, 
                    RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, tributaryLinkId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, "uses", 
                    SDHModule.RELATIONSHIP_SDHTTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTTLENDPOINTB, Constants.CLASS_GENERICCUSTOMER);
        HashMap<String, RemoteBusinessObjectList> theResult = aem.executeCustomDbCode(query);
        
        String title, tributaryLinkUsageReportText;
        RemoteBusinessObject theTributaryLink;
        
        if (theResult.get("tributaryLink").getList().isEmpty()) {
            title = "Error";
            tributaryLinkUsageReportText = getHeader(title);
            tributaryLinkUsageReportText += "<div class=\"error\">No information about this tributary link could be found</div>";
        }
        else {
            theTributaryLink = theResult.get("tributaryLink").getList().get(0);
            title = "Tributary Link Details Report for " + theTributaryLink.getName();
            tributaryLinkUsageReportText = getHeader(title);
            tributaryLinkUsageReportText += 
                                "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
            
            //Demarcation points
            query = String.format("MATCH (tributaryLink)-[relationA:%s]-(equipmentPort)-[relationB:%s]-(physicalConnection)-[relationC:%s]-(nextEquipmentPort)-[:%s*]->(nextEquipment)-[:%s]->(class)-[:%s*]->(superClass) "
                + "WHERE id(tributaryLink) = %s AND (relationA.name = \"%s\" OR relationA.name = \"%s\") "
                    + "AND (relationB.name = \"%s\" OR relationB.name = \"%s\") "
                    + "AND (relationC.name = \"%s\" OR relationC.name = \"%s\") "
                    + "AND superClass.name=\"%s\" "
                + "RETURN nextEquipmentPort, nextEquipment", RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.RELATED_TO_SPECIAL, RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, 
                                    tributaryLinkId, SDHModule.RELATIONSHIP_SDHTTLENDPOINTA, SDHModule.RELATIONSHIP_SDHTTLENDPOINTB,
                                    "endpointA", "endpointB", "endpointA", "endpointB", Constants.CLASS_GENERICDISTRIBUTIONFRAME);
            
            HashMap<String, RemoteBusinessObjectList> demarcationPoints = aem.executeCustomDbCode(query);
            String demarcationPointsAsSring = "";
            for (int i = 0; i < demarcationPoints.get("nextEquipmentPort").getList().size(); i++)
                demarcationPointsAsSring += "<b>" + demarcationPoints.get("nextEquipment").getList().get(i) + "</b>:" + demarcationPoints.get("nextEquipmentPort").getList().get(i) + "<br/>";

            //General Info
            tributaryLinkUsageReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td class=\"generalInfoValue\">" + theTributaryLink.getName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Type</td><td class=\"generalInfoValue\">" + theTributaryLink.getClassName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Endpoint A</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(0) + "</b>:" + theResult.get("port").getList().get(0).getName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Endpoint B</td><td class=\"generalInfoValue\"><b>" + theResult.get("equipment").getList().get(1) + "</b>:" + theResult.get("port").getList().get(1).getName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Demarcation Points</td><td class=\"generalInfoValue\">" + demarcationPointsAsSring + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Service</td><td class=\"generalInfoValue\">" + theResult.get("service").getList().get(0).getName() + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Customer</td><td class=\"generalInfoValue\">" + theResult.get("customer").getList().get(0).getName() + "</td></tr></table>";
        
            //Used resources
            List<RemoteBusinessObjectLight> container = bem.getSpecialAttribute(tributaryLinkClass, tributaryLinkId, SDHModule.RELATIONSHIP_SDHDELIVERS);
            
            String usedResources;
            if (container.isEmpty())
                usedResources = "<div class=\"error\">This tributary link seems malformed and does not have a path</div>";
            else {
                List<AnnotatedRemoteBusinessObjectLight> transportLinks = bem.getAnnotatedSpecialAttribute(container.get(0).getClassName(), 
                        container.get(0).getId(), SDHModule.RELATIONSHIP_SDHTRANSPORTS);
                usedResources = "<table><tr><th>Transport Link Name</th><th>Transport Link Position</th></tr>";
               
                int i = 0;
                for (AnnotatedRemoteBusinessObjectLight transportLink : transportLinks) {
                    usedResources += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + transportLink.getObject() + "</td>"
                                    + "<td>" + transportLink.getProperties().get(SDHModule.PROPERTY_SDHPOSITION) +"</td></tr>";
                    i ++;
                }
                usedResources += "</table>";
            }
            tributaryLinkUsageReportText += usedResources;
        }
        tributaryLinkUsageReportText += getFooter();
        
        return new RawReport("Tributary Link Details", "Neotropic SAS","1.1", tributaryLinkUsageReportText);
    }
    
    public RawReport buildNetworkEquipmentInLocationReport(String locationClass, long locationId) throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        String query = String.format("MATCH (location)<-[:%s*]-(networkEquipment)-[:%s]->(class)-[:%s*]->(superclass) "
                + "WHERE id(location) = %s AND superclass.name = \"%s\" "
                + "RETURN networkEquipment", RelTypes.CHILD_OF, RelTypes.INSTANCE_OF, RelTypes.EXTENDS, 
                                                            locationId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
        HashMap<String, RemoteBusinessObjectList> theResult = aem.executeCustomDbCode(query);
        
        String title, networkEquipmentInLocationReportText;
        
        RemoteBusinessObjectLight location = bem.getObjectLight(locationClass, locationId);
            
        title = "Network Equipment Report for " + location.getName();
        networkEquipmentInLocationReportText = getHeader(title);
        networkEquipmentInLocationReportText += 
                            "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";

        networkEquipmentInLocationReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td>" + location.getName() + "</td></tr>\n"
                + "<tr><td class=\"generalInfoLabel\">Type</td><td>" + location.getClassName() + "</td></tr>\n"
                + "<tr><td class=\"generalInfoLabel\">Location</td><td>" + formatLocation(bem.getParents(location.getClassName(), location.getId())) + "</td></tr>\n</table>\n";

        if (theResult.get("networkEquipment").getList().isEmpty())
            networkEquipmentInLocationReportText += "<div class=\"warning\">This location does not have any network equipment</div>";
        else {
            networkEquipmentInLocationReportText += "<table><tr><th>Name</th><th>Type</th><th>Serial Number</th><th>Location</th><th>Vendor</th><th>Operational State</th></tr>";
            int i = 0;
            for (RemoteBusinessObject networkEquipment : theResult.get("networkEquipment").getList()) {
                networkEquipmentInLocationReportText += "<tr class=\"" + (i % 2 == 0 ? "even" :"odd") + "\">"
                                                            + "<td>" + networkEquipment.getName() + "</td>"
                                                            + "<td>" + networkEquipment.getClassName() + "</td>"
                                                            + "<td>" + (networkEquipment.getAttributes().get("serialNumber") == null ? asError("Not Set") : networkEquipment.getAttributes().get("serialNumber").get(0)) + "</td>"
                                                            + "<td>" + formatLocation(bem.getParents(networkEquipment.getClassName(), networkEquipment.getId())) + "</td>"
                                                            + "<td>" + (networkEquipment.getAttributes().get("vendor") == null ? asError("Not Set") : bem.getObjectLight("EquipmentVendor", Long.valueOf(networkEquipment.getAttributes().get("vendor").get(0))).getName() ) + "</td>"
                                                            + "<td>" + (networkEquipment.getAttributes().get("state") == null ? asError("Not Set") : bem.getObjectLight("OperationalState", Long.valueOf(networkEquipment.getAttributes().get("state").get(0))).getName() ) + "</td></tr>";
                i ++;
            }
            networkEquipmentInLocationReportText += "</table>";
        }
        
        
        networkEquipmentInLocationReportText += getFooter();
        
        return new RawReport("Network Equipment", "Neotropic SAS","1.1", networkEquipmentInLocationReportText);
    }
    
    public RawReport buildServiceResourcesReport(String className, long serviceId) throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        RemoteBusinessObjectLight service = bem.getObjectLight(className, serviceId);
        String serviceResourcesReportText, title = "Resources Used By " + service.getName();
        serviceResourcesReportText = getHeader(title);
        serviceResourcesReportText += 
                            "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";

        serviceResourcesReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td>" + service.getName() + "</td></tr>\n"
                + "<tr><td class=\"generalInfoLabel\">Type</td><td>" + service.getClassName() + "</td></tr>\n"
                + "<tr><td class=\"generalInfoLabel\">Location</td><td>" + formatLocation(bem.getParents(service.getClassName(), service.getId())) + "</td></tr>\n</table>\n";
        List<RemoteBusinessObjectLight> resources = bem.getSpecialAttribute(service.getClassName(), service.getId(), "uses");
        if (resources.isEmpty()) {
            serviceResourcesReportText += "<div class=\"warning\">This service does not use any network resources</div>";
        } else {
            serviceResourcesReportText += "<table><tr><th>Name</th><th>Type</th><th>Location</th></tr>";
            int i = 0;
            for (RemoteBusinessObjectLight resource : resources) {
                serviceResourcesReportText += "<tr class=\"" + (i % 2 == 0 ? "even" :"odd") + "\">"
                                                            + "<td>" + resource.getName() + "</td>"
                                                            + "<td>" + resource.getClassName() + "</td>"
                                                            + "<td>" + formatLocation(bem.getParents(resource.getClassName(), resource.getId())) + "</td></tr>";
                i ++;
            }
            serviceResourcesReportText += "</table>";
        }
        
        serviceResourcesReportText += getFooter();
        return new RawReport("Service Resources", "Neotropic SAS","1.1", serviceResourcesReportText);
    }
    
    public RawReport subnetUsageReport(String className, long subnetId) throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
    
        RemoteBusinessObject subnet = bem.getObject(className, subnetId);
        List<RemoteBusinessObjectLight> subnetChildren = bem.getObjectSpecialChildren(className, subnetId);
        HashMap<String, List<String>> subnetAttributes = subnet.getAttributes();
        int hosts = Integer.parseInt(subnetAttributes.get("hosts").get(0));
        
        
        int usedIps = 0;
        
        List<RemoteBusinessObjectLight> ips  = new ArrayList<>();
        List<RemoteBusinessObjectLight> subnets  = new ArrayList<>();
        
        for (RemoteBusinessObjectLight children : subnetChildren) {
            if(children.getClassName().equals(Constants.CLASS_IP_ADDRESS))
                ips.add(children);
        }
        
        for (RemoteBusinessObjectLight children : subnetChildren) {
            if(children.getClassName().equals(Constants.CLASS_SUBNET_IPV4) || children.getClassName().equals(Constants.CLASS_SUBNET_IPV6))
                subnets.add(children);
        }
        
        for (RemoteBusinessObjectLight ip : ips) {
            if(ip.getClassName().equals(Constants.CLASS_IP_ADDRESS)){
                List<RemoteBusinessObjectLight> ipDevices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), IPAMModule.RELATIONSHIP_IPAMHASADDRESS);
                if(!ipDevices.isEmpty())
                    usedIps++;
            }
        }

        if(hosts == 0 && usedIps == 0){
            usedIps = ips.size();
            if(usedIps>hosts);
                hosts += 2;
        }
        int freeIps = hosts - usedIps;
        
        String vrf="", vlan = "", service="", title, subnetUsageReportText;
        List<RemoteBusinessObjectLight> vlans = bem.getSpecialAttribute(className, subnetId, IPAMModule.RELATIONSHIP_IPAMBELONGSTOVLAN);
        List<RemoteBusinessObjectLight> vrfs = bem.getSpecialAttribute(className, subnetId, IPAMModule.RELATIONSHIP_IPAMBELONGSTOVRFINSTACE);
        List<RemoteBusinessObjectLight> services = bem.getSpecialAttribute(className, subnetId, "uses");
        
        if(!vlans.isEmpty())
            vlan = "<b>" + vlans.get(0).getName() + " ["+ vlans.get(0).getClassName()+ "]</b> |"+
            formatLocation(bem.getParents(vlans.get(0).getClassName(), vlans.get(0).getId()));
        
        if(!vrfs.isEmpty())
            vrf = "<b>" + vrfs.get(0).getName() + " ["+ vrfs.get(0).getClassName()+ "]</b>";

        if(!services.isEmpty())
            service = services.get(0).getName() + " ["+ services.get(0).getClassName()+ "]";
        
        if (subnet == null) {
            title = "Error";
            subnetUsageReportText = getHeader(title);
            subnetUsageReportText += "<div class=\"error\">No information about this subnet could be found</div>";
        }
        else {
            title = "Subnet Usage Detail Report for " + subnet.getName();
            subnetUsageReportText = getHeader(title);
            subnetUsageReportText += 
                                "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
            
            subnetUsageReportText += pieChartScript(usedIps, freeIps);

            subnetUsageReportText += "<table><tr><td class=\"generalInfoLabel\">Network IP Addres</td><td class=\"generalInfoValue\"><b>" + subnetAttributes.get("networkIp").get(0) + "</b></td>"
                    + "<td rowspan=\"8\"><div id=\"piechart\" style=\"width: 350px; height: 250px;\"></div></td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Broadcast IP Address</td><td class=\"generalInfoValue\"><b>" + subnetAttributes.get("broadcastIp").get(0) + "</b> </td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Description </td><td class=\"generalInfoValue\">" + subnetAttributes.get("description").get(0) + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Number of hosts</td><td class=\"generalInfoValue\">" + hosts + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">IPs Related to some port</td><td class=\"generalInfoValue\"><b>" + (usedIps*100)/hosts + "%</b> ("+ usedIps +")</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Free IPs</td><td class=\"generalInfoValue\"><b>" + (freeIps*100)/hosts +"%</b> ("+ freeIps +")</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">VLAN</td><td class=\"generalInfoValue\">" + vlan + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">VRF</td><td class=\"generalInfoValue\">" + vrf + "</td></tr>"
                    + "<tr><td class=\"generalInfoLabel\">Service</td><td class=\"generalInfoValue\">" + service + "</td></tr></table>";
        }
        
        String nestedSubnets; 
        String ipAddresses; 
        
        if (subnets.isEmpty())
            nestedSubnets = "<div class=\"error\">There are no Subnets nested</div>";
        else {
            nestedSubnets = "<br><h2>Subnets</h2><table><tr><th>Subnet</th><th>Description</th><th>Service</th></tr>";

            int i = 0;
            for (RemoteBusinessObjectLight nestedSubnet : subnets) {
                service = "";
                
                String subSubnet = nestedSubnet.getName() + " [" + nestedSubnet.getClassName()+"]";
                
                List<RemoteBusinessObjectLight> subnetServices = bem.getSpecialAttribute(nestedSubnet.getClassName(), nestedSubnet.getId(), "uses");
                if(!subnetServices.isEmpty())
                    service = subnetServices.get(0).getName() + "[" +  subnetServices.get(0).getClassName() + "]";
                
                RemoteBusinessObject subnetO = bem.getObject(className, nestedSubnet.getId());
                HashMap<String, List<String>> attributes = subnetO.getAttributes();
                
                nestedSubnets += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + nestedSubnet.getName() + "</td>"
                              + "<td>" + attributes.get("description").get(0) +"</td>"
                              + "<td>" + service +"</td></tr>";
                i ++;
            }
            nestedSubnets += "</table>";
        }
        
        subnetUsageReportText += nestedSubnets;
        
        if (ips.isEmpty())
            ipAddresses = "<div class=\"error\">There are no IPs Addresses in use</div>";
        else {
            ipAddresses = "<br><h2>IP Addresses</h2><table><tr><th>IP Address</th><th>Description</th><th>Port</th><th>Location</th><th>Service</th></tr>";

            int i = 0;
            for (RemoteBusinessObjectLight ip : ips) {
                String device = "";
                service = "";
                
                List<RemoteBusinessObjectLight> ipDevices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), IPAMModule.RELATIONSHIP_IPAMHASADDRESS);
                String location = "";
                if(!ipDevices.isEmpty()){
                    device = ipDevices.get(0).getName() + " [" + ipDevices.get(0).getClassName()+"]";
                    List<RemoteBusinessObjectLight> parents = bem.getParents(ipDevices.get(0).getClassName(), ipDevices.get(0).getId());
                    location =  formatLocation(parents);
                }
                
                List<RemoteBusinessObjectLight> ipServices = bem.getSpecialAttribute(Constants.CLASS_IP_ADDRESS, ip.getId(), "uses");
                if(!ipServices.isEmpty())
                    service = ipServices.get(0).getName() + "[" +  ipServices.get(0).getClassName() + "]";
                
                RemoteBusinessObject ipO = bem.getObject(Constants.CLASS_IP_ADDRESS, ip.getId());
                HashMap<String, List<String>> attributes = ipO.getAttributes();
                
                ipAddresses += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + ip.getName() + "</td>"
                              + "<td>" + attributes.get("description").get(0) +"</td>"
                              + "<td>" + device +"</td>"
                              + "<td>" + location +"</td>"
                              + "<td>" + service +"</td></tr>";
                i ++;
            }
            ipAddresses += "</table>";
        }
        
        subnetUsageReportText += ipAddresses;
        subnetUsageReportText += getFooter();
        
        return new RawReport("Subnet Usage", "Neotropic SAS", "1.1", subnetUsageReportText);
    }
    
    public RawReport buildContractStatusReport() throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        
        List<RemoteBusinessObjectLight> contracts = bem.getObjectsOfClassLight(Constants.CLASS_GENERICCONTRACT, 0);
        
        String title = "Contract Status Report";
        String contractStatusReportText = getHeader(title);
        
        contractStatusReportText += 
                            "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
        
        if (contracts.isEmpty())
            contractStatusReportText += "<div class=\"warning\">This pool does not have contracts attached</div>";
        else {
            contractStatusReportText += "<table><tr><th>Name</th><th>Start Date</th><th>Expiration Date</th><th>Equipment</th><th>Provider</th><th>Phone Number</th><th>Email</th></tr>";
            
            int i = 0;
            SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
            for (RemoteBusinessObjectLight aContract : contracts) {
                RemoteBusinessObject fullContractInfo = bem.getObject(aContract.getClassName(), aContract.getId());
                Date startDate =  fullContractInfo.getAttributes().get("startDate") == null ? 
                        null : new Date(Long.valueOf(fullContractInfo.getAttributes().get("startDate").get(0)));
                
                String startDateString;
                
                if (startDate == null) 
                    startDateString = asError("Not Set");
                else 
                    startDateString = formatter.format(startDate);
                                
                String expirationDateString;
                Date expirationDate =  fullContractInfo.getAttributes().get("expirationDate") == null ? 
                        null : new Date(Long.valueOf(fullContractInfo.getAttributes().get("expirationDate").get(0)));
                
                if (expirationDate == null)
                    expirationDateString = asError("Not Set");
                else {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.DAY_OF_YEAR, 60);
                    Date withinTwoMonths = calendar.getTime();
                    
                    if (expirationDate.compareTo(withinTwoMonths) > 0)
                        expirationDateString = formatter.format(expirationDate);
                    else {
                        calendar.add(Calendar.DAY_OF_YEAR, -30);
                        Date withinAMonth = calendar.getTime();
                        if (expirationDate.compareTo(withinAMonth) > 0)
                            expirationDateString = asWarning(formatter.format(expirationDate));
                        else
                            expirationDateString = asError(formatter.format(expirationDate));
                    }
                }
                
                List<RemoteBusinessObjectLight> equipment = bem.getSpecialAttribute(aContract.getClassName(), aContract.getId(), "contractHas"); //NOI18N
                
                String equipmentString = "";
                if (equipment.isEmpty())
                    equipmentString = asError("No Equipment");
                else {
                    for (RemoteBusinessObjectLight anEquipment : equipment)
                        equipmentString += anEquipment + "<br/>";
                }
                
                String providerName = asError("Not Set");
                String providerPhoneNumber = asError("Not Set");
                String providerEmail = asError("Not Set");
                
                List<String> serviceProviderId = fullContractInfo.getAttributes().get("serviceProvider");
                if (serviceProviderId != null) {
                    RemoteBusinessObject serviceProvider = bem.getObject(Constants.CLASS_SERVICEPROVIDER, Long.valueOf(serviceProviderId.get(0)));
                    if (!serviceProvider.getName().isEmpty())
                        providerName = serviceProvider.getName();
                    if (serviceProvider.getAttributes().get(Constants.PROPERTY_SUPPORT_PHONE_NUMBER) != null)
                        providerPhoneNumber = serviceProvider.getAttributes().get(Constants.PROPERTY_SUPPORT_PHONE_NUMBER).get(0);
                    if (serviceProvider.getAttributes().get(Constants.PROPERTY_SUPPORT_EMAIL) != null)
                        providerEmail = serviceProvider.getAttributes().get(Constants.PROPERTY_SUPPORT_EMAIL).get(0);
                }
                
                contractStatusReportText += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") + "\"><td>" + aContract.getName() + "</td>"
                                                + "<td>" + startDateString + "</td><td>" + expirationDateString + "</td><td>" + equipmentString 
                                                + "</td><td>" + providerName + "</td><td>" + providerPhoneNumber + "</td><td>" + providerEmail + "</td></tr>\n";
                i ++;
            }
            contractStatusReportText += "</table>";
        }
        
        contractStatusReportText += getFooter();
        
        return new RawReport("Contract About to Expire", "Neotropic SAS", "1.1", contractStatusReportText);
    }
    
    public RawReport buildMPLSServiceReport(String serviceClass, long serviceId) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException,
            InvalidArgumentException, ApplicationObjectNotFoundException, 
            NotAuthorizedException
    {
        RemoteBusinessObject MPLSService = bem.getObject(serviceClass, serviceId);
        List<RemoteBusinessObjectLight> serviceInstances = bem.getSpecialAttribute(serviceClass, serviceId, "uses");

        String service="", title, MPLSDetailReportText;
        
        if (MPLSService == null) {
            title = "Error";
            MPLSDetailReportText = getHeader(title);
            MPLSDetailReportText += "<div class=\"error\">No information about this MPLS service could be found</div>";
        }
        else {
            title = "MPLS service detail Report for " + MPLSService.getName() + "[" + MPLSService.getClassName() + "]";
            MPLSDetailReportText = getHeader(title);
            MPLSDetailReportText += 
                                "  <body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
        }
            String instance; 
            
            if (serviceInstances.isEmpty())
                instance = "<div class=\"error\">There are no instance asossiate to this service</div>";
            else {
                instance = "<table><tr><th>Service Instance</th><th>Location</th></tr>";

            int i = 0;
            for (RemoteBusinessObjectLight serviceInstance : serviceInstances) {
                String device = "";
                service = "";
                
                RemoteBusinessObject ports = bem.getObject(serviceInstance.getClassName(), serviceInstance.getId());
                String location = "";
                if(ports != null){
                    device = ports.getName() + " [" + ports.getClassName()+"]";
                    List<RemoteBusinessObjectLight> parents = bem.getParents(ports.getClassName(), ports.getId());
                    location =  formatLocation(parents);
                }
                
                instance += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + serviceInstance.getName() + " [" + serviceInstance.getClassName()+"] </td>"
                              + "<td>" + location +"</td>";
                              
                i ++;
            }
            instance += "</table>";
        }
        MPLSDetailReportText += instance;
        MPLSDetailReportText += getFooter();
        
        return new RawReport("MPLS Service Detail", "Neotropic SAS", "1.1", MPLSDetailReportText);
    }
    
    public RawReport buildLogicalConfigurationInterfacesReport(String logicalConfigurationClassName, long logicalConfigurationId) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException,
            InvalidArgumentException, ApplicationObjectNotFoundException, 
            NotAuthorizedException
    {
        String logicalConfiguration, title, DetailReportText = "", instance = "", vlan = "  "; 
        
        title = "Detail Report for all " + logicalConfigurationClassName + " instances";
        DetailReportText = getHeader(title);
        DetailReportText += "<body><table><tr><td><h1>" + title + "</h1></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";
        
        List<RemoteBusinessObjectLight> listOflogicalConfigurations = bem.getObjectsOfClassLight(logicalConfigurationClassName, 0);
        
        for (RemoteBusinessObjectLight listOflogicalConfiguration : listOflogicalConfigurations) {
            instance = "";
            RemoteBusinessObject logicalConfigurationObject = bem.getObject(listOflogicalConfiguration.getClassName(), listOflogicalConfiguration.getId());
            
            HashMap<String, List<String>> attributes = logicalConfigurationObject.getAttributes();
            List<RemoteBusinessObjectLight> ports = bem.getSpecialAttribute(listOflogicalConfiguration.getClassName(), listOflogicalConfiguration.getId(), MPLSModule.RELATIONSHIP_MPLSPORTBELONGSTOINTERFACE);
            
            if (ports == null) 
                DetailReportText += "<div class=\"error\">No information for" + listOflogicalConfiguration.getName() + " could be found</div>";
            
            
            else {
                DetailReportText += "<table><tr><td class=\"generalInfoLabel\">Name</td><td class=\"generalInfoValue\"><b>" + logicalConfigurationObject.getName() + "[" + logicalConfigurationObject.getClassName() + "]</b></td>";
                List<RemoteBusinessObjectLight> vlans = bem.getSpecialAttribute(listOflogicalConfiguration.getClassName(), listOflogicalConfiguration.getId(), IPAMModule.RELATIONSHIP_IPAMBELONGSTOVLAN);
                if (!vlans.isEmpty()){
                    for (RemoteBusinessObjectLight vlanInstance : vlans) 
                        vlan += vlanInstance.toString() + ", ";  
                    DetailReportText +=  "<tr><td class=\"generalInfoLabel\">VLAN</td><td class=\"generalInfoValue\"><b>" + vlan.substring(0, vlan.length()-2) + "</b> </td></tr>";
                }
                DetailReportText +=  "<tr><td class=\"generalInfoLabel\">Creation date</td><td class=\"generalInfoValue\"><b>" + new Date(Long.valueOf(attributes.get("creationDate").get(0))) + "</b> </td></tr></table>";
            }

            if (ports.isEmpty())
                    instance = "<div class=\"error\">There is nothing related to " + listOflogicalConfiguration.toString() +"</div>";
            else {
                    instance = "<table><tr><th>Port / Device</th><th>IP Address</th><th>Device Location</th></tr>";

                int i = 0;
                for (RemoteBusinessObjectLight relatedPort : ports) {
                    String device = "";
                    logicalConfiguration = "";
                    List<RemoteBusinessObjectLight> ipAddresses = bem.getSpecialAttribute(relatedPort.getClassName(), relatedPort.getId(), IPAMModule.RELATIONSHIP_IPAMHASADDRESS);
                    RemoteBusinessObject port = bem.getObject(relatedPort.getClassName(), relatedPort.getId());
                    String location = "";
                    if(port != null){
                        device = port.getName() + " [" + port.getClassName()+"]";
                        List<RemoteBusinessObjectLight> parents = bem.getParents(port.getClassName(), port.getId());
                        location =  formatLocation(parents);
                    }

                    String ips = "  ";
                    for (RemoteBusinessObjectLight ipAddress : ipAddresses) 
                        ips += ipAddress.getName() + ", ";
                    
                    
                    instance += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd")
                            + "\"><td>" + relatedPort.getName() + " [" + relatedPort.getClassName()+"] </td>"
                            + "<td>" + ips.substring(0, ips.length()-2) +"</td>"
                            + "<td>" + location +"</td>";

                    i ++;
                }
                instance += "</table>";
            }
            
            DetailReportText += instance;
            DetailReportText += "<hr>";
        }
        DetailReportText = DetailReportText.substring(0, DetailReportText.length()-4);
        
        DetailReportText += getFooter();
        
        return new RawReport("Logical Configuration", "Neotropic SAS", "1.1", DetailReportText);
    }
    
    public RawReport buildServicesReport(String serviceClassName, long serviceId) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException,
            InvalidArgumentException, ApplicationObjectNotFoundException, 
            NotAuthorizedException
    {
        RemoteBusinessObject theService = bem.getObject(serviceClassName, serviceId);
        
        List<RemoteBusinessObjectLight> serviceInstances = bem.getSpecialAttribute(serviceClassName, serviceId, "uses");
        HashMap<String, List<String>> serviceAttributes = theService.getAttributes();
        Set<AttributeMetadata> serviceClassAttributes = mem.getClass(serviceClassName).getAttributes();
        String service="", title, ServiceDetailReportText;
        
        if (theService == null) {
            title = "Error";
            ServiceDetailReportText = getHeader(title);
            ServiceDetailReportText += "<div class=\"error\">No information about this service could be found</div>";
        }
        else {
            title = "Service detail Report for " + theService.getName() + "[" + theService.getClassName() + "]";
            ServiceDetailReportText = getHeader(title);
            ServiceDetailReportText += "<body>"
                    + "<table><tr><td><h2>" + title +"</h2></td><td align=\"center\"><img src=\"" + corporateLogo + "\"/></td></tr></table>\n";

            ServiceDetailReportText += "<table>";
            String value = "";
            List<RemoteBusinessObjectLight> parents = bem.getParents(serviceClassName, serviceId);
            
            RemoteBusinessObject serviceCustomer = null;
                    
            for (RemoteBusinessObjectLight parent : parents) {
                if(mem.isSubClass(Constants.CLASS_GENERICCUSTOMER, parent.getClassName())){
                    serviceCustomer = bem.getObject(parent.getClassName(), parent.getId());
                    break;
                }
            }
            
            HashMap<String, List<String>> customerAttributes = serviceCustomer.getAttributes();
            ClassMetadata customerClass = mem.getClass(serviceCustomer.getClassName());
            Set<AttributeMetadata> customerClassAttributes = customerClass.getAttributes();
            
            ServiceDetailReportText += createAttributesOfClass(serviceAttributes, serviceClassAttributes);
            ServiceDetailReportText += "<table><tr><td><h2>Customer Details: "+serviceCustomer.toString()+"</h1></td><td></td></tr></table>\n"
                    + "<table>";
            ServiceDetailReportText += createAttributesOfClass(customerAttributes, customerClassAttributes);
            
            ServiceDetailReportText += "</table>";

        }
            String instance; 

            if (serviceInstances.isEmpty())
                instance = "<div class=\"error\">There are no instances asossiate to this service</div>";
            else {
                instance = "<table><tr><th>Related Instances</th><th>Location</th></tr>";

            int i = 0;
            for (RemoteBusinessObjectLight serviceInstance : serviceInstances) {
                String objectName = "";
                service = "";
                
                RemoteBusinessObject inventoryObject = bem.getObject(serviceInstance.getClassName(), serviceInstance.getId());
                String location = "";
                if(inventoryObject != null){
                    objectName = inventoryObject.getName() + " [" + inventoryObject.getClassName()+"]";
                    List<RemoteBusinessObjectLight> parents = bem.getParents(inventoryObject.getClassName(), inventoryObject.getId());
                    location =  formatLocation(parents);
                }
                
                instance += "<tr class=\"" + (i % 2 == 0 ? "even" : "odd") +"\"><td>" + serviceInstance.getName() + " [" + serviceInstance.getClassName()+"] </td>"
                              + "<td>" + location +"</td>";
                              
                i ++;
            }
            instance += "</table>";
        }
        ServiceDetailReportText += instance;
        ServiceDetailReportText += getFooter();
        
        return new RawReport("Service Details", "Neotropic SAS", "1.1", ServiceDetailReportText);
    }
    
    //<editor-fold desc="Helpers" defaultstate="collapsed">
    private String getStyleSheet() {
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
                    "            background-color: #94b155;\n" +
                    "            padding: 7px 7px 7px 7px;\n" +
                    "            color: white;\n" +
                    "            font-weight: normal;\n" +
                    "   }\n" +
                    "   td {\n" +
                    "            padding: 7px 7px 7px 7px;\n" +
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
                    "            background-color: #c2da8e;\n" +
                    "            width: 20%;\n" +
                    "   }\n" +
                    "   td.generalInfoValue {\n" +
                    "            background-color: white;\n" +
                    "   }\n" +
                    "   tr.even {\n" +
                    "            background-color: #f3e270;\n" +
                    "   }\n" +
                    "   tr.odd {\n" +
                    "            background-color: #D1F680;\n" +
                    "   }" +
                    "   hr { \n" +
                    "            display: block; \n"+
                    "            margin-top: 0.5em; \n"+
                    "            margin-bottom: 0.5em; \n"+
                    "            margin-left: auto; \n"+
                    "            margin-right: auto; \n"+
                    "            border-style: inset; \n"+
                    "            border-width: 1px; \n"+
                    "            color: #A5DF00; \n"+
                    "       }  \n"+
                    "</style>\n";
    }
    
    private String getHeader(String title){
        return "<!DOCTYPE html>\n" +
                    "<html lang=\"en\">\n" +
                    "  <head>\n" +
                    "    <meta charset=\"utf-8\">\n" +
                    "    <title>" + title + "</title>\n" +
                    getStyleSheet() +
                    "  </head>\n";
    }
    
    private String getFooter() {
        return "  <div class=\"footer\">This report is powered by <a href=\"http://www.kuwaiba.org\">Kuwaiba Open Network Inventory</a></div></body>\n" +
                                "</html>";
    }
    
    private String formatLocation (List<RemoteBusinessObjectLight> containmentHierarchy) {
        String location = "";
        if (containmentHierarchy.size() == 1 && !(Constants.NODE_DUMMYROOT).equals((containmentHierarchy.get(0).getClassName())))
            location += containmentHierarchy.get(0).toString();
        else{
            for (int i = 0; i < containmentHierarchy.size() - 1; i ++)
                location += containmentHierarchy.get(i).toString() + " | ";
        }
        return location;
    }
    
    private String asOk(String text) {
        return "<span class=\"ok\">" + text + "</span>";
    }
    
    private String asWarning(String text) {
        return "<span class=\"warning\">" + text + "</span>";
    }
    
    private String asError(String text) {
        return "<span class=\"error\">" + text + "</span>";
    }
    
    private String pieChartScript(int usedIps, int freeIps){
        String script = "\n<script type=\"text/javascript\" src=\"https://www.gstatic.com/charts/loader.js\"></script>\n" +
                        "    <script type=\"text/javascript\">\n" +
                        "      google.charts.load('current', {'packages':['corechart']});\n" +
                        "      google.charts.setOnLoadCallback(drawChart);\n" +
                        "      function drawChart() {\n" +
                        "        var data = google.visualization.arrayToDataTable\n" +
                        "                      ([\n"+
                        "                      ['IP', 'Usage %'],\n" +
                        "                      ['Used'," + usedIps + "],\n"+
                        "                      ['Free'," + freeIps + "]\n" +
                        "                   ])\n"+
                        "        var options = {title: 'Subnet Usage'};\n"+
                        "        var chart = new google.visualization.PieChart(document.getElementById('piechart'));\n"+
                        "        chart.draw(data, options);\n"+
                        "       }\n"+
                        "</script>\n";
        return script;
    }
    
    private String asKLM(int position) {
            int k, l, m;
            
            if (position % 21 == 0) {
                k = position / 21;
                l = 7;
                m = 3;
            } else {
                k = (position / 21) + 1;
                if ((position % 21) % 3 == 0)
                    l = (position % 21) / 3;
                else 
                    l = (position % 21) / 3 + 1;
                
                if ((position % 21) % 3 == 0)
                    m = 3;
                else
                    m = (position % 21) % 3;
            }
            
            return String.format("%s [%s - %s - %s]", position, k, l, m);
        }
    
    private String createAttributesOfClass(HashMap<String, List<String>> attributes, Set<AttributeMetadata> classAttributes) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException, 
            InvalidArgumentException, ApplicationObjectNotFoundException, 
            NotAuthorizedException
    {
        String ServiceDetailReportText = "", value = "";
        for (AttributeMetadata a : classAttributes) {
            List<String> values = attributes.get(a.getName());
                
            if(values != null){
                if(!AttributeMetadata.isPrimitive(a.getType()))
                    value = bem.getObject(a.getType(), Long.valueOf(values.get(0))).getName();
                else if(a.getType().equals("Date"))
                    value = new Date(Long.valueOf(values.get(0))).toString();
                else 
                    value = values.get(0);
                ServiceDetailReportText += "<tr><td class=\"generalInfoLabel\"><b></b>"+a.getName()+"</b></td><td class=\"generalInfoValue\"><b>" + value + "</b></td></tr>";
            }
        }
        return ServiceDetailReportText;
    }
    //</editor-fold> 
}
