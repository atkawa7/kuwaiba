/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.views;

import com.neotropic.kuwaiba.modules.GenericCommercialModule;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.application.ViewObject;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.util.i18n.I18N;
import org.openide.util.Exceptions;

/**
 * This class implements the functionality corresponding to manage QinQ technology
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class ViewModule  implements GenericCommercialModule {
    /**
     * The MetadataEntityManager instance
     */
    private MetadataEntityManager mem;
    /**
     * The BusinessEntityManager instance
     */
    private BusinessEntityManager bem;
    /**
     * The ApplicationEntityManager instance
     */
    private ApplicationEntityManager aem;
     
    private List<E2ENode> nodes;
    private List<E2EEdge> edges;
    private HashMap<E2EEdge, E2ENode> edgeSource;
    private HashMap<E2EEdge, E2ENode> edgeTarget;
      
    private ViewObject savedView;
    
    public final static String VIEW_CLASS = "EndToEndView"; 

    @Override
    public String getName() {
        return "E2E Views Module"; //NOI18N
    }

    @Override
    public String getDescription() {
        return "Generates views of the inventory";
    }
    
    @Override
    public String getVersion() {
        return "1.0";
    }
    
    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>"; //NOI18N
    }

    @Override
    public String getCategory() {
        return "routing/views";
    }

    @Override
    public GenericCommercialModule.ModuleType getModuleType() {
        return GenericCommercialModule.ModuleType.TYPE_PERPETUAL_LICENSE;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void configureModule(ApplicationEntityManager aem, MetadataEntityManager mem, BusinessEntityManager bem) {
        this.mem = mem;
        this.bem = bem;
        this.aem = aem;
        nodes = new ArrayList<>();
        edges = new ArrayList<>();
        edgeSource = new HashMap<>();
        edgeTarget = new HashMap<>();
        savedView = null;
    }

    /**
     * validates a saved end to end view
     * @param linkClasses the classes of the links, in the view
     * @param linkIds the ids of the links in the view
     * @param savedView saved structure, possibly outdated the end to end view
     * @return an updated end to end view 
     */
    public ViewObject validateSavedE2EView(List<String> linkClasses, List<String> linkIds, ViewObject savedView){
        try{
            this.savedView = savedView;
            //first we create the current view
            createE2EView(linkClasses, linkIds, true, true, true, true);
            //the we load the saved view to compare
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(savedView.getStructure());
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        String objectClass = reader.getAttributeValue(null, "class");

                        String xCoordinate = reader.getAttributeValue(null,"x");
                        String yCoordinate = reader.getAttributeValue(null,"y");
                        String objectId = reader.getElementText();

                        E2ENode node = new E2ENode(new BusinessObjectLight(objectClass, objectId, null));
                        if(nodes.contains(node)){
                           nodes.get(nodes.indexOf(node)).getProperties().put("x", xCoordinate);
                           nodes.get(nodes.indexOf(node)).getProperties().put("y", yCoordinate);
                        }
                    }else {
                        if (reader.getName().equals(qEdge)) {
                            String objectId = reader.getAttributeValue(null, "id"); //NOI18N
                            String className = reader.getAttributeValue(null,"class"); //NOI18N
                            
                            E2EEdge connection = new E2EEdge(new BusinessObjectLight(className, objectId, null));

                            if (edges.contains(connection)){//the connection exists
                                List<Point> localControlPoints = new ArrayList<>();
                                while(true) {
                                    reader.nextTag();

                                    if (reader.getName().equals(qControlPoint)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                            localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                    } else {
                                        edges.get(edges.indexOf(connection)).getProperties().put("controlPoints", localControlPoints);
                                        break;
                                    }
                                }//end while
                            }
                        }
                    }//end if edge 
                }//end xml
            }//end while
            return getAsXML();
        } catch (XMLStreamException | ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    /**
     * Calls the methods that creates the end to end view
     * @param linkClasses list of the links classes
     * @param linkIds list of the links ids 
     * @param includePhyscalPaths include physical path continuity for the logical links in the end to end view
     * @param includeVlans include VLANs continuity in the view
     * @param includePhyscialLinks include physical links in the view
     * @param includeBDis include bridge domains in the end view
     * @throws ServerSideException 
     */
    public void createE2EView(List<String> linkClasses, List<String> linkIds, 
            boolean includePhyscalPaths, boolean includeVlans, 
            boolean includePhyscialLinks, boolean includeBDis) throws ServerSideException
    {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            List<ObjectLinkObjectDefinition> e2eMap = new ArrayList<>();
            nodes = new ArrayList<>();
            edges = new ArrayList<>();
            edgeSource = new HashMap<>();
            edgeTarget = new HashMap<>();
            for(int x=0 ; x < linkIds.size(); x++){
                if(mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALCONNECTION, linkClasses.get(x)))
                    e2eMap.add(getPhysicalLinkDetail(linkClasses.get(x), linkIds.get(x)));
                
                else if(mem.isSubclassOf(Constants.CLASS_GENERICLOGICALCONNECTION, linkClasses.get(x))){
                    //firts we get the logical link details
                    GenericConnectionDefinition logicalCircuitDetails = getLogicalLinkDetail(linkClasses.get(x), linkIds.get(x));
                    BusinessObjectLight physicalEndpointA = null, logicalEndpointA = null, physicalEndpointB = null, logicalEndpointB = null, deviceA = null, deviceB = null;
                    //start logical part         
                    if(logicalCircuitDetails.getEndpointA() != null){
                        //Side A, first we must check the endpoint if is physical or logical
                        if(mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, logicalCircuitDetails.getEndpointA().getClassName()))
                            physicalEndpointA = logicalCircuitDetails.getEndpointA();
                        
                        else if(mem.isSubclassOf(Constants.CLASS_GENERICLOGICALPORT, logicalCircuitDetails.getEndpointA().getClassName())){
                            logicalEndpointA = logicalCircuitDetails.getEndpointA();
                            
                            if(!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty() && mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, logicalCircuitDetails.getPhysicalPathForEndpointA().get(0).getClassName()))
                                physicalEndpointA = logicalCircuitDetails.getPhysicalPathForEndpointA().get(0);
                        }
                        //we need the parent of the endpoint, it should be a GenericCommunicationsElmenet because only between these you can create logical connections 
                        BusinessObjectLight parentEquipmentA = bem.getFirstParentOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);

                        //now we found the device
                        if(parentEquipmentA != null &&  mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, parentEquipmentA.getClassName()))
                            deviceA = parentEquipmentA; //GenericCommunicationsElement
                    }//logical part side B   
                    if(logicalCircuitDetails.getEndpointB() != null){
                        if(mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, logicalCircuitDetails.getEndpointB().getClassName()))
                            physicalEndpointB = logicalCircuitDetails.getEndpointA();
                        
                        else if(mem.isSubclassOf(Constants.CLASS_GENERICLOGICALPORT, logicalCircuitDetails.getEndpointB().getClassName())){
                            logicalEndpointB = logicalCircuitDetails.getEndpointA();
                            
                            if(!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty() && mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, logicalCircuitDetails.getPhysicalPathForEndpointB().get(0).getClassName()))
                                physicalEndpointB = logicalCircuitDetails.getPhysicalPathForEndpointB().get(0);
                        }
                        //we must found the parent device of the end point
                        BusinessObjectLight parentEquipmentB = bem.getFirstParentOfClass(logicalCircuitDetails.getEndpointB().
                                getClassName(), logicalCircuitDetails.getEndpointB().getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                        
                        //the parent will be the last found in te list of parents
                        if(parentEquipmentB != null &&  mem.isSubclassOf(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, parentEquipmentB.getClassName()))
                            deviceB = parentEquipmentB; //GenericCommunicationsElement
                    }
                    //here we have have the first part of the logical connection [deviceA -- logical link -- deviceB]
                    e2eMap.add(new ObjectLinkObjectDefinition(deviceA, physicalEndpointA, logicalEndpointA, 
                            logicalCircuitDetails.getConnectionObject(), logicalEndpointB, physicalEndpointB, deviceB));
                    //physical part, side A
                    if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) 
                        e2eMap.addAll(physicalPathReader(logicalCircuitDetails.getPhysicalPathForEndpointA()));
                    //physical part, side B
                    if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) 
                        e2eMap.addAll(physicalPathReader(logicalCircuitDetails.getPhysicalPathForEndpointB()));

                    if(includeVlans){
                        //VLANs side A
                        HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForVlansEndpointA = new HashMap<>(); 
                        if(logicalCircuitDetails.getEndpointA() != null && !logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty())
                            physicalPathForVlansEndpointA = getVLANContinuity(logicalCircuitDetails.getPhysicalPathForEndpointA().get(logicalCircuitDetails.getPhysicalPathForEndpointA().size() -1));
                        else
                            physicalPathForVlansEndpointA = getVLANContinuity(physicalEndpointA == null ? logicalCircuitDetails.getEndpointA() : physicalEndpointA);
                        
                        for (Map.Entry<BusinessObjectLight, List<BusinessObjectLight>> entry : physicalPathForVlansEndpointA.entrySet())
                            e2eMap.addAll(physicalPathReader(entry.getValue()));
                        
                        
                        //VLANs side B
                        HashMap<BusinessObjectLight, List<BusinessObjectLight>> physicalPathForVlansEndpointB = new HashMap<>();    
                        if(logicalCircuitDetails.getEndpointB() != null && !logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty())
                            physicalPathForVlansEndpointB = getVLANContinuity(logicalCircuitDetails.getPhysicalPathForEndpointB().get(logicalCircuitDetails.getPhysicalPathForEndpointB().size() -1));
                        else if(logicalCircuitDetails.getEndpointB() != null)
                            physicalPathForVlansEndpointB = getVLANContinuity(physicalEndpointB == null ? logicalCircuitDetails.getEndpointB() : physicalEndpointB);
                        
                        for (Map.Entry<BusinessObjectLight, List<BusinessObjectLight>> entry : physicalPathForVlansEndpointB.entrySet()) 
                            e2eMap.addAll(physicalPathReader(entry.getValue()));
                        
                    }
                    //if(includeBDis){//TODO include the BDIs logic}
                }//end if is logical link
            }//end for
            
            e2eMap.forEach(def -> {
                if(def.getDeviceA() != null && !nodes.contains(new E2ENode(def.getDeviceA())))
                    nodes.add(new E2ENode(def.getDeviceA()));
                if(def.getDeviceB() != null && !nodes.contains(new E2ENode(def.getDeviceB())))
                    nodes.add(new E2ENode(def.getDeviceB()));
                if(def.getConnectionObject() != null)
                    edges.add(new E2EEdge(def.getConnectionObject()));
                if(def.getConnectionObject() != null && def.getDeviceA() != null)
                    edgeSource.put(new E2EEdge(def.getConnectionObject()), new E2ENode(def.getDeviceA()));
                if(def.getConnectionObject() != null && def.getDeviceB() != null)
                    edgeTarget.put(new E2EEdge(def.getConnectionObject()), new E2ENode(def.getDeviceB()));
            });
                   
        } catch (Exception ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * The XML representation of the view. Typically used to serialize it
     * @return a XML representation of request end to end view
     */
    public ViewObject getAsXML(){
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
        
            QName qnameView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), Constants.VIEW_FORMAT_VERSION));
        
            QName qnameClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters(VIEW_CLASS));
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            int x = 15, y = 180, i = 2;
            for(E2ENode node : nodes){
                
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                
                xmlew.add(xmlef.createAttribute(new QName("x"), node.getProperties().get("x") != null ?
                        (String)node.getProperties().get("x") : Integer.toString(x)));
                //we do this with the y in order to set the node one up an one down in the end to end view
                y += (i % 2 != 0) ? 175 + i * 2 : (-145);
                xmlew.add(xmlef.createAttribute(new QName("y"), node.getProperties().get("y") != null ? 
                        (String)node.getProperties().get("y") : Integer.toString(y)));
                
                xmlew.add(xmlef.createAttribute(new QName("class"), node.getBussinesObject().getClassName()));
                xmlew.add(xmlef.createCharacters(node.getBussinesObject().getId()));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
                x += 115; i++;
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            
            for (E2EEdge edge : edges) {
                if (!edgeSource.containsKey(edge) || !edgeTarget.containsKey(edge)) //This connection is malformed because one of the endpoints does not exist
                    continue;                                                       //probably, it was moved to another parent

                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                
                xmlew.add(xmlef.createAttribute(new QName("id"), edge.getEdge().getId()));
                xmlew.add(xmlef.createAttribute(new QName("class"), edge.getEdge().getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("asideid"), edgeSource.get(edge).getBussinesObject().getId()));
                xmlew.add(xmlef.createAttribute(new QName("asideclass"), edgeSource.get(edge).getBussinesObject().getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("bsideid"), edgeTarget.get(edge).getBussinesObject().getId()));
                xmlew.add(xmlef.createAttribute(new QName("bsideclass"), edgeTarget.get(edge).getBussinesObject().getClassName()));
                
                List<Point> points = (List<Point>)edge.getProperties().get("controlPoints");
                if(points != null){
                    for(Point point : points){
                        QName qnameControlpoint = new QName("controlpoint");
                        xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                        xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(point.x)));
                        xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(point.y)));
                        xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                    }
                }
                
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            }
            xmlew.add(xmlef.createEndElement(qnameEdges, null));
                        
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            ViewObject updatedViewObject = new ViewObject(savedView != null ? savedView.getId() : -1, 
                    savedView != null ? savedView.getName() : "", 
                    savedView != null ? savedView.getDescription() : "Generated by e2e method", 
                    savedView != null ? savedView.getViewClassName() : "EndToEndView"); //TODO the ViewClassName should be managed
            
            updatedViewObject.setStructure(baos.toByteArray());
            return updatedViewObject;
            
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null; 
    }
        
    /**
     * Gets the details of logical connection (SDHTributaryLink or MPLSLink)
     * it gets the endpoints of the logical connections and the physical path 
     * from the end point until it finds continuity
     * @param linkClass the logical link class
     * @param linkId the logical link id
     * @return a GenericConnectionDefinition that contains the connection, end points, and their physical paths
     * @throws ServerSideException if the given link is not supported
     *                             if the the given id and class could not be found
     */
    public GenericConnectionDefinition getLogicalLinkDetail(String linkClass,  String linkId) throws ServerSideException {
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            BusinessObject linkObject = bem.getObject(linkClass, linkId);
            
            BusinessObjectLight endpointA = null;
            BusinessObjectLight endpointB = null;
            List<BusinessObjectLight> physicalPathA = null, physicalPathB = null;
            String endpointARelationshipName, endpointBRelationshipName;
            
            if (mem.isSubclassOf("GenericSDHTributaryLink", linkClass)) { //NOI18N
                endpointARelationshipName = "sdhTTLEndpointA"; //NOI18N
                endpointBRelationshipName = "sdhTTLEndpointB"; //NOI18N
            } else if (Constants.CLASS_MPLSLINK.equals(linkClass)) {
                endpointARelationshipName = "mplsEndpointA"; //NOI18N
                endpointBRelationshipName = "mplsEndpointB"; //NOI18N
            } else
                 throw new ServerSideException(String.format("Class %s is not a supported logical link", linkClass)); 
            
            List<BusinessObjectLight> endpointARelationship = bem.getSpecialAttribute(linkClass, linkId, endpointARelationshipName);
            if (!endpointARelationship.isEmpty()) {
                endpointA = endpointARelationship.get(0);
                //if the port is a GenericlogicalPort we need its physical parent port
                if(endpointA != null && mem.isSubclassOf(Constants.CLASS_GENERICLOGICALPORT, endpointA.getClassName())){
                    BusinessObjectLight physicalPort = bem.getParent(endpointA.getClassName(), endpointA.getId());
                    if(physicalPort != null)
                        physicalPathA = bem.getPhysicalPath(physicalPort.getClassName(), physicalPort.getId());
                }
                else if(endpointA != null && mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, endpointA.getClassName()))
                    physicalPathA = bem.getPhysicalPath(endpointA.getClassName(), endpointA.getId());
            }
            
            List<BusinessObjectLight> endpointBRelationship = bem.getSpecialAttribute(linkClass, linkId, endpointBRelationshipName);
            if (!endpointBRelationship.isEmpty()) {
                endpointB = endpointBRelationship.get(0);
                //if the port is a GenericlogicalPort we need its physical parent port
                if(endpointB != null && mem.isSubclassOf(Constants.CLASS_GENERICLOGICALPORT, endpointB.getClassName())){
                    BusinessObjectLight physicalPort = bem.getParent(endpointB.getClassName(), endpointB.getId());
                    if(physicalPort != null)
                        physicalPathB = bem.getPhysicalPath(physicalPort.getClassName(), physicalPort.getId());
                }
                else if(endpointB != null && mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, endpointB.getClassName()))
                    physicalPathB = bem.getPhysicalPath(endpointB.getClassName(), endpointB.getId());
            }

            return new GenericConnectionDefinition(linkObject, endpointA, endpointB, 
                    physicalPathA == null ? new ArrayList<>() : physicalPathA, 
                    physicalPathB == null ? new ArrayList<>() : physicalPathB);

        } catch (Exception ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Gets the details of physical connection (GenericPhysicalLink)
     * it gets the endpoints of the connection and the physical path 
     * from the endpoint until it finds continuity
     * @param linkClass the physical link class
     * @param linkId the physical link id
     * @return a GenericConnectionDefinition that contains the connection, end points, and their physical paths
     * @throws ServerSideException if the given link is not supported
     *                             if the the given id and class could not be found
     */
    public ObjectLinkObjectDefinition getPhysicalLinkDetail(String linkClass, String linkId) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            BusinessObject linkObject = bem.getObject(linkClass, linkId);
            
            BusinessObjectLight endpointA = null, endpointB = null;
           
            String endpointARelationshipName, endpointBRelationshipName;
            if (mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, linkClass)) { //NOI18N
                endpointARelationshipName = "endpointA"; //NOI18N
                endpointBRelationshipName = "endpointB"; //NOI18N
            }else 
                throw new ServerSideException(String.format("Class %s is not a supported physical link", linkClass)); //NOI18N
            
            List<BusinessObjectLight> endpointARelationship = bem.getSpecialAttribute(linkClass, linkId, endpointARelationshipName); //NOI18N
            BusinessObjectLight deviceA = null;
            if (!endpointARelationship.isEmpty()){ 
                endpointA = endpointARelationship.get(0);
                deviceA = bem.getParentOfClass(endpointA.getClassName(), endpointA.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                if(deviceA == null)
                    deviceA = bem.getParentOfClass(endpointA.getClassName(), endpointA.getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME);
            }

            List<BusinessObjectLight> endpointBRelationship = bem.getSpecialAttribute(linkClass, linkId, endpointBRelationshipName); //NOI18N
            BusinessObjectLight deviceB = null;
            if (!endpointBRelationship.isEmpty()) {
                endpointB = endpointBRelationship.get(0);
                deviceB = bem.getParentOfClass(endpointB.getClassName(), endpointB.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                if(deviceB == null)
                    deviceB = bem.getParentOfClass(endpointB.getClassName(), endpointB.getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME);
            
            }
            return new ObjectLinkObjectDefinition(deviceA, endpointA, linkObject, endpointB, deviceB);
        } catch (Exception ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }
    
    /**
     * Checks the continuity throw ports that belongs to the same VLAN
     * @param endpoint a given port to check if belong to a vlan
     * @return a map with key: port, value: physical path of that port
     * @throws ServerSideException 
     */
    private HashMap<BusinessObjectLight, List<BusinessObjectLight>> getVLANContinuity(BusinessObjectLight endpoint) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> vlansPhysicalPath = new HashMap<>();
            if(endpoint != null){
                //we get the the vlans to which the port belongs
                List<BusinessObjectLight> vlans = bem.getSpecialAttribute(endpoint.getClassName(), endpoint.getId(), "portBelongsToVlan");
                for (BusinessObjectLight vlan : vlans) { //We get all the port of every vlan
                    List<BusinessObjectLight> vlanPorts = bem.getSpecialAttribute(vlan.getClassName(), vlan.getId(), "portBelongsToVlan");
                    for (BusinessObjectLight vlanPort : vlanPorts) {
                        if(vlanPort.getId() != null && endpoint.getId() != null && !vlanPort.getId().equals(endpoint.getId())){//we get the physical path for every port of the vlan except of the given endpoint 
                            List<BusinessObjectLight> vlanPhysicalPath = bem.getPhysicalPath(vlanPort.getClassName(), vlanPort.getId());
                            if(!vlanPhysicalPath.isEmpty())
                                vlansPhysicalPath.put(vlanPort, vlanPhysicalPath);
                        }
                    }
                }
            }
            return vlansPhysicalPath;
        } catch (Exception ex) {
            throw new ServerSideException(ex.getMessage());
        }
    
    }
    
    /**
     * Given an endPoint and a relationship with other logical interfaces it retrieves 
     * a path of continuity.
     * @param endpoint a given port to check if belong to a set of other interfaces
     * @param relationshipName a relationship that groups interfaces.
     * @return a map with key: port, value: physical path of that port
     * @throws ServerSideException 
     */
    private HashMap<BusinessObjectLight, List<BusinessObjectLight>> getEndPointContinuity(BusinessObjectLight endpoint, String relationshipName) throws ServerSideException{
        if (bem == null)
            throw new ServerSideException(I18N.gm("cannot_reach_backend")); //NOI18N
        try {
            HashMap<BusinessObjectLight, List<BusinessObjectLight>> portsPhysicalPath = new HashMap<>();
            if(endpoint != null){
                //we get the the parents to which the port belongs
                List<BusinessObjectLight> logicalInterfaces = bem.getSpecialAttribute(endpoint.getClassName(), endpoint.getId(), relationshipName);
                for (BusinessObjectLight interface_ : logicalInterfaces) { //We get all the port of related with this relationship
                    List<BusinessObjectLight> parentsOfinterfaces = bem.getSpecialAttribute(interface_.getClassName(), interface_.getId(), relationshipName);
                    for (BusinessObjectLight parentsOfinterface : parentsOfinterfaces) {
                        if(interface_.getId() != null && endpoint.getId() != null && !interface_.getId().equals(endpoint.getId())){//we get the physical path for every port of the vlan except of the given endpoint 
                            List<BusinessObjectLight> portPhysicalPath = bem.getPhysicalPath(interface_.getClassName(), interface_.getId());
                            if(!portPhysicalPath.isEmpty())
                                portsPhysicalPath.put(interface_, portPhysicalPath);
                        }
                    }
                    
                }
            }
            return portsPhysicalPath;
        } catch (Exception ex) {
            throw new ServerSideException(ex.getMessage());
        }
    }

    /**
     * Provides a generic way to get the parents(the GenericCommunicationsElement or 
     * the GenericBox) parents of the GenericPorts in a given path, then it parser 
     * between physical path and a simplified list, of objects connections objects
     * [Node1 - Node2]
     * [Node1 - Node3]
     * [Node3 - Node4]
     * @param path a physical path with endpoint, connection, endpoint(one or several ports, mirror, virtual, service instances)
     * @return a list of ConfigurationItem-connection-ConfigurationItem
     * @throws MetadataObjectNotFoundException
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws InvalidArgumentException 
     */
    private List<ObjectLinkObjectDefinition> physicalPathReader(List<BusinessObjectLight> path) 
            throws MetadataObjectNotFoundException, MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, InvalidArgumentException
    {
        BusinessObject connection  = null;
        BusinessObject device = null;
        BusinessObjectLight endpoint = null;

        BusinessObject sourceDevice = null;
        List<ObjectLinkObjectDefinition> connectionsMap = new ArrayList<>();
        //with this for we are rearing the path 3 at a time, endpoint - connection -endpoint (ignoring the mirror ports)
        for (BusinessObjectLight obj : path) {
            if(mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALLINK, obj.getClassName()))
                connection = bem.getObject(obj.getClassName(), obj.getId());
            else if(mem.isSubclassOf(Constants.CLASS_GENERICPHYSICALPORT, obj.getClassName())) {
                    device = bem.getParentOfClass(obj.getClassName(), obj.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                //if the parent could not be found it should be aGenericCommunications element(e.g. Router, Cloud, MPLSRouter, etc)
                if(device == null)
                    device = bem.getParentOfClass(obj.getClassName(), obj.getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME);
            }
            if(sourceDevice == null){
                sourceDevice = device;
                endpoint = obj;
            }
            else if(sourceDevice.equals(device))//this is in case of mirror ports to ignore the case
                continue;
            //if we found the deviceA, connection, deviceB we are available to save the data and create connecttion set
            if(connection != null && sourceDevice != null && device != null){//TODO check what happend with unconnected things
                connectionsMap.add(new ObjectLinkObjectDefinition(sourceDevice, endpoint, connection, obj, device));
                connection = null;
                device = null;
                endpoint = null;
                sourceDevice = null;
            }
        }//end for
        
        return connectionsMap;
    }
}
