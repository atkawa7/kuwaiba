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
package com.neotropic.kuwaiba.modules.reporting.img.endtoend;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLEventFactory;
import javax.xml.namespace.QName;
import java.awt.Point;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import java.util.Random;
import javax.imageio.ImageIO;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.interfaces.ws.toserialize.business.RemotePhysicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.services.persistence.util.Constants;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * Shows an end-to-end view of a service by trying to match the endpoints of the logical circuits
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different: It's intended to deal exclusively 
 * with logical connections, though it uses the physical path to fill the gaps between different logical segments
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class EndToEndViewScene extends GraphScene<RemoteObjectLight, RemoteObjectLight> {
    /**
     * Version for the XML document to save views (see http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_Save_Object_Views for details)
     */
    public static final String VIEW_FORMAT_VERSION = "1.2";
    public static final String FREE_FRAME = "freeFrame";
    public static final String CLASS_GENERICLOGICALPORT = "GenericLogicalPort";
    public static final String CLASS_GENERICPORT = "GenericPort";
    public static final String CLASS_GENERICDISTRIBUTIONFRAME = "GenericDistributionFrame";
    
    private final WebserviceBean webserviceBean;
    private final RemoteSession remoteSession;    
    private final String ipAddress;
    
    public final static String VIEW_CLASS = "EndToEndView"; 
        
    private final LayerWidget imagesLayer;
    private final LayerWidget framesLayer;
    protected LayerWidget labelsLayer;
    
    private final LayerWidget nodeLayer;
    private final LayerWidget edgeLayer;
    
    public EndToEndViewScene(String ipAddress, RemoteSession remoteSession, WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;
        this.remoteSession = remoteSession;
        this.ipAddress = ipAddress;
        
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        imagesLayer = new LayerWidget(this);
        framesLayer = new LayerWidget(this);
        
        addChild(framesLayer);
        addChild(imagesLayer);
        addChild(edgeLayer);
        addChild(nodeLayer);
    }
        
    public byte[] getAsXML() { 
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), VIEW_FORMAT_VERSION));
            
            QName qnameClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters(VIEW_CLASS));
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            
            for (Widget nodeWidget : nodeLayer.getChildren()) {
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(nodeWidget.getPreferredLocation().x)));
                xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(nodeWidget.getPreferredLocation().y)));
                RemoteObjectLight lolNode = (RemoteObjectLight) findObject(nodeWidget);
                xmlew.add(xmlef.createAttribute(new QName("class"), lolNode.getClassName()));
                xmlew.add(xmlef.createCharacters(lolNode.getId()));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                RemoteObjectLight lolEdge = (RemoteObjectLight) findObject(edgeWidget);
                ObjectConnectionWidget acwEdge = (ObjectConnectionWidget) edgeWidget;
                if (acwEdge.getSourceAnchor() == null || acwEdge.getTargetAnchor() == null) //This connection is malformed because one of the endpoints does not exist
                    continue;                                                               //probably, it was moved to another parent
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                
                xmlew.add(xmlef.createAttribute(new QName("id"), lolEdge.getId()));
                xmlew.add(xmlef.createAttribute(new QName("class"), lolEdge.getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("asideid"), getEdgeSource(lolEdge).getId()));
                xmlew.add(xmlef.createAttribute(new QName("asideclass"), getEdgeSource(lolEdge).getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("bsideid"), getEdgeTarget(lolEdge).getId()));
                xmlew.add(xmlef.createAttribute(new QName("bsideclass"), getEdgeTarget(lolEdge).getClassName()));
                
                for (Point point : acwEdge.getControlPoints()) {
                    QName qnameControlpoint = new QName("controlpoint");
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(point.x)));
                    xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(point.y)));
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                }
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            }
            xmlew.add(xmlef.createEndElement(qnameEdges, null));
                        
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null; 
    }
    
    public void render(byte[] structure) throws IllegalArgumentException { 
        HashMap<String, String> classIdOfSides =  new HashMap<>();
//<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
        try {
            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/oview_.xml");
            fos.write(structure);
            fos.close();
        } catch(Exception e) {}
//</editor-fold>
        try{
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qZoom = new QName("zoom"); //NOI18N
            QName qCenter = new QName("center"); //NOI18N
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qLabel = new QName("label"); //NOI18N
            QName qPolygon = new QName("polygon"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        String className = reader.getAttributeValue(null, "class");
                        int xCoordinate = Integer.valueOf(reader.getAttributeValue(null,"x"));
                        int yCoordinate = Integer.valueOf(reader.getAttributeValue(null,"y"));
                        String objectId = reader.getElementText();
                        try {
                            RemoteObjectLight rol = webserviceBean.getObjectLight(className, objectId, ipAddress, remoteSession.getSessionId());
                            if (rol != null) {
                                classIdOfSides.put(objectId, className);
                                if (getNodes().contains(rol))
                                    Notifications.showWarning("The view seems to be corrupted. Self-healing measures were taken");
                                else {
                                    Widget widget = addNode(rol);
                                    widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));

                                    widget.setBackground(new Color(webserviceBean.getClass(className, ipAddress, remoteSession.getSessionId()).getColor()));
                                    
                                    validate();
                                }
                            }
                        } catch (ServerSideException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }else {
                        if (reader.getName().equals(qEdge)) {
                            String objectId = reader.getAttributeValue(null, "id"); //NOI18N
                            String className = reader.getAttributeValue(null,"class"); //NOI18N
 
                            String aSide = reader.getAttributeValue(null, "aside"); //NOI18N
                            String bSide = reader.getAttributeValue(null, "bside"); //NOI18N

                            String linkClassName = reader.getAttributeValue(null,"class"); //NOI18N
                            
                            RemoteObjectLight container = null;
                            try {
                                container = webserviceBean.getObjectLight(linkClassName, objectId, ipAddress, remoteSession.getSessionId());
                            } catch (ServerSideException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            
                            if (container != null) { // if the connection exists
                                RemoteObjectLight aSideObject = new RemoteObjectLight(classIdOfSides.get(aSide), aSide, null);
                                ObjectNodeWidget aSideWidget = (ObjectNodeWidget) findWidget(aSideObject);

                                RemoteObjectLight bSideObject = new RemoteObjectLight(classIdOfSides.get(bSide), bSide, null);
                                ObjectNodeWidget bSideWidget = (ObjectNodeWidget) findWidget(bSideObject);

                                if (aSideWidget != null && bSideWidget != null) {//If one of the endpoints is missing, don't render the connection

                                    if (getEdges().contains(container))
                                        Notifications.showWarning("The view seems to be corrupted. Self-healing measures were taken");
                                        //NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, "The view seems to be corrupted. Self-healing measures were taken");
                                    else {
                                        ObjectConnectionWidget newEdge = (ObjectConnectionWidget) addEdge(container);
                                        setEdgeSource(container, aSideWidget.getLookup().lookup(RemoteObjectLight.class));
                                        setEdgeTarget(container, bSideWidget.getLookup().lookup(RemoteObjectLight.class));
                                        
                                        List<Point> localControlPoints = new ArrayList<>();
                                        while(true) {
                                            reader.nextTag();

                                            if (reader.getName().equals(qControlPoint)) {
                                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                                    localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                            } else {
                                                newEdge.setControlPoints(localControlPoints,false);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                       
                        else {
                            if (reader.getName().equals(qLabel)) {
                                //Unavailable for now
                            } else {
                                if (reader.getName().equals(qZoom))
                                    setZoomFactor(Integer.valueOf(reader.getText()));
                                else {
                                    if (reader.getName().equals(qCenter)) {
                                        double x = Double.valueOf(reader.getAttributeValue(null, "x"));
                                        double y = Double.valueOf(reader.getAttributeValue(null, "y"));
                                    } else {
                                        //Place more tags
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        validate();
        repaint();
    }
    
    public void render_old(byte[] structure) throws IllegalArgumentException { 
        try{
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qZoom = new QName("zoom"); //NOI18N
            QName qCenter = new QName("center"); //NOI18N
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qLabel = new QName("label"); //NOI18N
            QName qPolygon = new QName("polygon"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        String objectClass = reader.getAttributeValue(null, "class");

                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        String objectId = reader.getElementText();

                        try {
                            RemoteObjectLight rol = webserviceBean.getObjectLight(objectClass, objectId, ipAddress, remoteSession.getSessionId());
                            
                            if (rol != null) {
                                if (getNodes().contains(rol))
                                    Notifications.showWarning("The view seems to be corrupted. Self-healing measures were taken");
                                else {
                                    Widget widget = addNode(rol);
                                    widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                                    widget.setBackground(new Color(webserviceBean.getClass(objectClass, ipAddress, remoteSession.getSessionId()).getColor()));
                                    validate();
                                }
                            }
                            
                        } catch (ServerSideException ex) {
                        }

                    }else {
                        if (reader.getName().equals(qEdge)) {
                            String objectId = reader.getAttributeValue(null, "id"); //NOI18N

                            String aSide = reader.getAttributeValue(null, "aside"); //NOI18N
                            String bSide = reader.getAttributeValue(null, "bside"); //NOI18N

                            String className = reader.getAttributeValue(null,"class"); //NOI18N
                            
                            RemoteObjectLight container = null;
                            try {
                                container = webserviceBean.getObjectLight(className, objectId, ipAddress, remoteSession.getSessionId());
                            } catch (ServerSideException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            
                            if (container != null) { // if the connection exists
                                RemoteObjectLight aSideObject = new RemoteObjectLight(null, aSide, null);
                                ObjectNodeWidget aSideWidget = (ObjectNodeWidget) findWidget(aSideObject);

                                RemoteObjectLight bSideObject = new RemoteObjectLight(null, bSide, null);
                                ObjectNodeWidget bSideWidget = (ObjectNodeWidget) findWidget(bSideObject);

                                if (aSideWidget != null && bSideWidget != null) {//If one of the endpoints is missing, don't render the connection

                                    if (getEdges().contains(container))
                                        Notifications.showWarning("The view seems to be corrupted. Self-healing measures were taken");
                                        //NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, "The view seems to be corrupted. Self-healing measures were taken");
                                    else {
                                        ObjectConnectionWidget newEdge = (ObjectConnectionWidget) addEdge(container);
                                        setEdgeSource(container, aSideWidget.getLookup().lookup(RemoteObjectLight.class));
                                        setEdgeTarget(container, bSideWidget.getLookup().lookup(RemoteObjectLight.class));
                                        
                                        List<Point> localControlPoints = new ArrayList<>();
                                        while(true) {
                                            reader.nextTag();

                                            if (reader.getName().equals(qControlPoint)) {
                                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                                    localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                            } else {
                                                newEdge.setControlPoints(localControlPoints,false);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            if (reader.getName().equals(qLabel)) {
                                //Unavailable for now
                            } else {
                                if (reader.getName().equals(qZoom))
                                    setZoomFactor(Integer.valueOf(reader.getText()));
                                else {
                                    if (reader.getName().equals(qCenter)) {
                                        double x = Double.valueOf(reader.getAttributeValue(null, "x"));
                                        double y = Double.valueOf(reader.getAttributeValue(null, "y"));
                                    } else {
                                        //Place more tags
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (XMLStreamException ex) {
        }
        validate();
        repaint();
    }
    
    public HashMap<RemoteObjectLight, List<RemoteObjectLight>> getPhysicalPathForVlansEndpointB(RemoteLogicalConnectionDetails remoteCircuitDetails) {
        HashMap<RemoteObjectLight, List<RemoteObjectLight>> physicalPathForVlansEndpointB = new HashMap<>();
        
        if(remoteCircuitDetails.getPhysicalPathForVlansEndpointB() != null) {
            
            List<RemoteObjectLight> objsB = remoteCircuitDetails.getPhysicalPathForVlansEndpointB().getObjs();
            List<RemoteObjectLightList> relatedObjectsB = remoteCircuitDetails.getPhysicalPathForVlansEndpointB().getRelatedObjects();
        
            for (int i = 0; i < objsB.size(); i++) {
                RemoteObjectLightList relatedRemoteObjects = relatedObjectsB.get(i);
                
                physicalPathForVlansEndpointB.put(
                    objsB.get(i),
                    relatedRemoteObjects.getList());
            }
        }
        return physicalPathForVlansEndpointB;
    }
    
    public HashMap<RemoteObjectLight, List<RemoteObjectLight>> getPhysicalPathForVlansEndpointA(RemoteLogicalConnectionDetails remoteCircuitDetails) {
        HashMap<RemoteObjectLight, List<RemoteObjectLight>> physicalPathForVlansEndpointA = new HashMap<>();
        
        if(remoteCircuitDetails.getPhysicalPathForVlansEndpointA() != null) {
            List<RemoteObjectLight> objsA = remoteCircuitDetails.getPhysicalPathForVlansEndpointA().getObjs();
            List<RemoteObjectLightList> relatedObjectsA = remoteCircuitDetails.getPhysicalPathForVlansEndpointA().getRelatedObjects();

            for (int i = 0; i < objsA.size(); i++){
                RemoteObjectLightList relatedRemoteObjects = relatedObjectsA.get(i);
                
                physicalPathForVlansEndpointA.put(
                    objsA.get(i),
                    relatedRemoteObjects.getList());
            }
        }
        return physicalPathForVlansEndpointA;
    }
    
    public void clear() {
        while (!getNodes().isEmpty())
            removeNode(getNodes().iterator().next());

        while (!getEdges().isEmpty())
            removeEdge(getEdges().iterator().next());
        
        if (labelsLayer != null)
            labelsLayer.removeChildren();
        validate();
        repaint();
    }
    
    public void render(RemoteObjectLight selectedService) {
        clear();
        List<RemoteObjectLight> serviceResources = null;
        try {
            serviceResources = webserviceBean.getServiceResources(selectedService.getClassName(), selectedService.getId(), ipAddress, remoteSession.getSessionId());
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        if (serviceResources == null)
            return;
        else {
            try {
                for (RemoteObjectLight serviceResource : serviceResources) {
                    RemoteObjectLight lastAddedASideEquipmentLogical = null, lastAddedBSideEquipmentLogical = null;
                    RemoteObjectLight lastAddedASideEquipmentPhysical = null;
                    RemoteObjectLight lastAddedBSideEquipmentPhysical = null;
                    if (webserviceBean.isSubclassOf(serviceResource.getClassName(), "GenericLogicalConnection", ipAddress, remoteSession.getSessionId())) {
                        
                        RemoteLogicalConnectionDetails logicalCircuitDetails = webserviceBean.getLogicalLinkDetails(serviceResource.getClassName(), serviceResource.getId(), ipAddress, remoteSession.getSessionId());
                        
                        RemoteObjectLight aSideEquipmentLogical = null, bSideEquipmentLogical = null;
                        //Let's create the boxes corresponding to the endpoint A of the logical circuit
                        if(logicalCircuitDetails.getEndpointA() != null){
                            List<RemoteObjectLight> parentsUntilFirstComEquipmentA; 
                            if(webserviceBean.isSubclassOf(logicalCircuitDetails.getEndpointA().getClassName(), CLASS_GENERICLOGICALPORT, ipAddress, remoteSession.getSessionId())){
                                List<RemoteObjectLight> parentsUntilFirstPhysicalPortA = webserviceBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                    getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericPhysicalPort", ipAddress, remoteSession.getSessionId());
                                //This is only for pseudowire and will be removed once the MPLS sync has been finished, because vc ends in the device not a port
                                if(webserviceBean.isSubclassOf(parentsUntilFirstPhysicalPortA.get(0).getClassName(), "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId()))
                                    parentsUntilFirstComEquipmentA = Arrays.asList(parentsUntilFirstPhysicalPortA.get(0));
                                else
                                    parentsUntilFirstComEquipmentA = webserviceBean.getParentsUntilFirstOfClass(parentsUntilFirstPhysicalPortA.get(0).
                                        getClassName(), parentsUntilFirstPhysicalPortA.get(0).getId(), "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId());
                            }
                            else
                                parentsUntilFirstComEquipmentA = webserviceBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                    getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId());

                            aSideEquipmentLogical = parentsUntilFirstComEquipmentA.get(parentsUntilFirstComEquipmentA.size() - 1);

                            lastAddedASideEquipmentLogical = aSideEquipmentLogical;

                            if (findWidget(aSideEquipmentLogical) == null)
                                addNode(aSideEquipmentLogical);
                        }
                        //Now the other side
                        if(logicalCircuitDetails.getEndpointB() != null){
                            List<RemoteObjectLight> parentsUntilFirstComEquipmentB = webserviceBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                    getClassName(), logicalCircuitDetails.getEndpointB().getId(), "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId());
                            
                            bSideEquipmentLogical = parentsUntilFirstComEquipmentB.get(parentsUntilFirstComEquipmentB.size() - 1);

                            lastAddedBSideEquipmentLogical = bSideEquipmentLogical;
                            if (findWidget(bSideEquipmentLogical) == null)
                                addNode(bSideEquipmentLogical);
                        }
                                               
                        //Now the logical link
                        ObjectConnectionWidget logicalLinkWidget = (ObjectConnectionWidget) findWidget(logicalCircuitDetails.getConnectionObject());
                        if(logicalLinkWidget == null && aSideEquipmentLogical != null && bSideEquipmentLogical != null){
                            logicalLinkWidget = (ObjectConnectionWidget) addEdge(logicalCircuitDetails.getConnectionObject());

                            setEdgeSource(logicalCircuitDetails.getConnectionObject(), aSideEquipmentLogical);
                            setEdgeTarget(logicalCircuitDetails.getConnectionObject(), bSideEquipmentLogical);

                            List<Point> thePoints = new ArrayList();
                            thePoints.add(findWidget(aSideEquipmentLogical).getPreferredLocation());
                            thePoints.add(findWidget(bSideEquipmentLogical).getPreferredLocation());
                            logicalLinkWidget.setControlPoints(thePoints, true);

                        }
                        //Now with render the physical part
                        //We start with the A side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) {
                            List<RemoteObjectLight> path = logicalCircuitDetails.getPhysicalPathForEndpointA();
                            RemoteObjectLight connection = null;
                            RemoteObjectLight device = null;
                            RemoteObjectLight tempDevice = null;
                            RemoteObjectLight tempEndPoint = null;
                            for(int i = 0; i < path.size(); i++){
                                if(webserviceBean.isSubclassOf(path.get(i).getClassName(), Constants.CLASS_GENERICPHYSICALLINK, ipAddress, remoteSession.getSessionId()))
                                    connection = path.get(i);
                                else if(path.get(i).getClassName().equals("Pseudowire"))
                                    device = webserviceBean.getParent(path.get(i).getClassName(), path.get(i).getId(), ipAddress, remoteSession.getSessionId());
                                else{
                                    //when two ports are followed the parent could be a GenericDistributionFrame(e.g. an ODF)
                                    if(webserviceBean.isSubclassOf(path.get(i).getClassName(), "GenericPhysicalPort", ipAddress, remoteSession.getSessionId()) && i+1 < path.size() && webserviceBean.isSubclassOf(path.get(i+1).getClassName(), "GenericPhysicalPort", ipAddress, remoteSession.getSessionId())){
                                        device = webserviceBean.getFirstParentOfClass(path.get(i).getClassName(), path.get(i).getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME, ipAddress, remoteSession.getSessionId());
                                        i++;
                                    }//if the parent could not be found it should be aGenericCommunications element(e.g. Router, Cloud, MPLSRouter, etc)
                                    if(device == null){
                                        device = webserviceBean.getFirstParentOfClass(path.get(i).getClassName(), path.get(i).getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, ipAddress, remoteSession.getSessionId());
                                        if(path.get(i).getClassName().equals("VirtualPort") && i+1 < path.size()){
                                            i++;
                                            lastAddedASideEquipmentPhysical = device;
                                        }
                                    }
                                    if (i == 0 && aSideEquipmentLogical != null && device.getId().equals(aSideEquipmentLogical.getId()))
                                        lastAddedASideEquipmentPhysical = device;
                                    
                                    if(connection == null){
                                        tempEndPoint = path.get(i);
                                        tempDevice = device;
                                        device = null;
                                    }else{ //if enters here it means that we have enough information to create the structure RemoteObjectLinkObject 
                                        if(findWidget(device) == null)
                                            addNode(device);
                                        //We add the physical link, we must check if the physical path has more than the end point
                                        if (findWidget(connection) == null){ 
                                            ObjectConnectionWidget physicalLinkWidgetA = (ObjectConnectionWidget) findWidget(connection);
                                            //the link not yet added
                                            if(physicalLinkWidgetA == null)
                                                physicalLinkWidgetA = (ObjectConnectionWidget) addEdge(connection);

                                            physicalLinkWidgetA.getLabelWidget().setLabel(
                                                    lastAddedASideEquipmentPhysical.getName() + ":" + tempEndPoint.getName() + " ** " +
                                                device.getName() + ":" + connection.getName());

                                            setEdgeSource(connection, lastAddedASideEquipmentPhysical);
                                            setEdgeTarget(connection, device);
                                        }

                                        connection = null;
                                        tempEndPoint = path.get(i);
                                        tempDevice = device;
                                        lastAddedASideEquipmentPhysical = device;
                                        device = null;                                        
                                    }
                                }
                            }
                        }
                        //VLANs
                        //we must check if there is something to show with vlans
                        if(!getPhysicalPathForVlansEndpointA(logicalCircuitDetails).isEmpty()){
                            for (HashMap.Entry<RemoteObjectLight, List<RemoteObjectLight>> en : getPhysicalPathForVlansEndpointA(logicalCircuitDetails).entrySet()) {
                                List<RemoteObjectLight> physicalPath = en.getValue();
                                
                                RemoteObjectLight endpointVlan = webserviceBean.getFirstParentOfClass(physicalPath.get(0).getClassName(), 
                                        physicalPath.get(0).getId(), 
                                        "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId());
                                
                                RemoteObjectLight physicalVlan = webserviceBean.getFirstParentOfClass(physicalPath.get(2).getClassName(), 
                                        physicalPath.get(2).getId(), 
                                        "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId());
                                    
                                if(physicalVlan != null){
                                    if(findWidget(physicalVlan) == null)
                                            addNode(physicalVlan);

                                    ObjectConnectionWidget physicalLinkWidget = (ObjectConnectionWidget) findWidget(physicalPath.get(1));

                                    if(physicalLinkWidget == null)
                                        physicalLinkWidget = (ObjectConnectionWidget) addEdge(physicalPath.get(1));

                                    physicalLinkWidget.getLabelWidget().setLabel(physicalPath.get(1) + "  " + physicalPath.get(2));
                                    setEdgeTarget(physicalPath.get(1), physicalVlan);

                                    if(!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()){
                                        if(lastAddedASideEquipmentPhysical != null && endpointVlan.getId().equals(lastAddedASideEquipmentPhysical.getId()))
                                            setEdgeSource(physicalPath.get(1), lastAddedASideEquipmentPhysical);
                                    }
                                    else if(lastAddedASideEquipmentLogical != null){
                                        if(endpointVlan.getId().equals(lastAddedASideEquipmentLogical.getId())) {
                                           setEdgeSource(physicalPath.get(1), lastAddedBSideEquipmentLogical);
                                        }
                                    }
                                }
                            }
                        }
                        //Now the b side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) {
                            List<RemoteObjectLight> path = logicalCircuitDetails.getPhysicalPathForEndpointB();
                            RemoteObjectLight connection = null;
                            RemoteObjectLight device = null;
                            RemoteObjectLight tempDevice = null;
                            RemoteObjectLight tempEndPoint = null;
                            
                            for(int i = 0; i < path.size(); i++){
                                if(webserviceBean.isSubclassOf(path.get(i).getClassName(), Constants.CLASS_GENERICPHYSICALLINK, ipAddress, remoteSession.getSessionId()))
                                    connection = path.get(i);
                                else if(path.get(i).getClassName().equals("Pseudowire"))
                                    device = webserviceBean.getParent(path.get(i).getClassName(), path.get(i).getId(), ipAddress, remoteSession.getSessionId());
                                else{//when two ports are followed the parent could be a GenericDistributionFrame(e.g. an ODF)
                                    if(webserviceBean.isSubclassOf(path.get(i).getClassName(), "GenericPhysicalPort", ipAddress, remoteSession.getSessionId()) && i+1 < path.size() && webserviceBean.isSubclassOf(path.get(i+1).getClassName(), "GenericPhysicalPort", ipAddress, remoteSession.getSessionId())){
                                        device = webserviceBean.getFirstParentOfClass(path.get(i).getClassName(), path.get(i).getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME, ipAddress, remoteSession.getSessionId());
                                        i++;
                                    }//if the parent could not be found it should be aGenericCommunications element(e.g. Router, Cloud, MPLSRouter, etc)
                                    if(device == null){
                                        device = webserviceBean.getFirstParentOfClass(path.get(i).getClassName(), path.get(i).getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, ipAddress, remoteSession.getSessionId());
                                        if(path.get(i).getClassName().equals("VirtualPort") && i+1 < path.size()){
                                            i++;
                                            lastAddedBSideEquipmentPhysical = device;
                                        }
                                    }
                                    if(i == 0 && bSideEquipmentLogical != null && device.getId().equals(bSideEquipmentLogical.getId()))
                                        lastAddedBSideEquipmentPhysical = device;
                                    
                                    if(connection == null){
                                        tempEndPoint = path.get(i);
                                        tempDevice = device;
                                        device = null;
                                    }else{ //if enters here it means that we have enough information to create the structure RemoteObjectLinkObject 
                                        if(findWidget(device) == null)
                                            addNode(device);
                                        //We add the physical link, we must check if the physical path has more than the end point
                                        if (findWidget(connection) == null){ 
                                            ObjectConnectionWidget physicalLinkWidgetA = (ObjectConnectionWidget) findWidget(connection);
                                            //the link not yet added
                                            if(physicalLinkWidgetA == null)
                                                physicalLinkWidgetA = (ObjectConnectionWidget) addEdge(connection);

                                            physicalLinkWidgetA.getLabelWidget().setLabel(
                                                    lastAddedBSideEquipmentPhysical.getName() + ":" + tempEndPoint.getName() + " ** " +
                                                device.getName() + ":" + connection.getName());

                                            setEdgeSource(connection, lastAddedBSideEquipmentPhysical);
                                            setEdgeTarget(connection, device);
                                        }

                                        connection = null;
                                        tempEndPoint = path.get(i);
                                        tempDevice = device;
                                        lastAddedBSideEquipmentPhysical = device;
                                        device = null;         
                                    }
                                }
                            }
                        }//we must check if there is something to show with vlans
                         if(!getPhysicalPathForVlansEndpointB(logicalCircuitDetails).isEmpty()){
                            for (Map.Entry<RemoteObjectLight, List<RemoteObjectLight>> en : getPhysicalPathForVlansEndpointB(logicalCircuitDetails).entrySet()) {

                                List<RemoteObjectLight> physicalPath = en.getValue();
                                
                                RemoteObjectLight endpointVlan = webserviceBean.getFirstParentOfClass(physicalPath.get(0).getClassName(), 
                                        physicalPath.get(0).getId(), 
                                        "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId());
                                
                                RemoteObjectLight physicalVlan = webserviceBean.getFirstParentOfClass(physicalPath.get(2).getClassName(), 
                                        physicalPath.get(2).getId(), 
                                        "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId());
                                    
                                if(physicalVlan != null){ 
                                        if(findWidget(physicalVlan) == null)
                                        addNode(physicalVlan);
                                        
                                    ObjectConnectionWidget physicalLinkWidget = (ObjectConnectionWidget) findWidget(physicalPath.get(1));

                                    if(physicalLinkWidget == null)
                                        physicalLinkWidget = (ObjectConnectionWidget) addEdge(physicalPath.get(1));

                                    physicalLinkWidget.getLabelWidget().setLabel(physicalPath.get(1) + " " + physicalPath.get(2));
                                    setEdgeTarget(physicalPath.get(1), physicalVlan);

                                    if(!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()){
                                        if(lastAddedBSideEquipmentPhysical != null && endpointVlan.getId().equals(lastAddedBSideEquipmentPhysical.getId()))
                                            setEdgeSource(physicalPath.get(1), lastAddedBSideEquipmentPhysical);
                                    }
                                    else if(lastAddedBSideEquipmentLogical != null){
                                        if(endpointVlan.getId().equals(lastAddedBSideEquipmentLogical.getId()))
                                           setEdgeSource(physicalPath.get(1), lastAddedBSideEquipmentLogical);
                                    }
                                }
                            }
                        }
                        
                    }
                    
                    //Physical Links
                    //We check if there are some physical links related with the service
                    else if(webserviceBean.isSubclassOf(serviceResource.getClassName(), "GenericPhysicalConnection", ipAddress, remoteSession.getSessionId())){
                        RemotePhysicalConnectionDetails physicalLinkDetails = webserviceBean.getPhysicalLinkDetails(serviceResource.getClassName(), serviceResource.getId(), ipAddress, remoteSession.getSessionId());
                        //sideA
                        RemoteObjectLight aSideEquipmentPhysical = null, bSideEquipmentPhysical = null;
                        for(RemoteObjectLight sideEquipmentPhysical : physicalLinkDetails.getPhysicalPathForEndpointA()){
                            if(webserviceBean.isSubclassOf(sideEquipmentPhysical.getClassName(), "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId()) || 
                                    webserviceBean.isSubclassOf(sideEquipmentPhysical.getClassName(), "GenericDistributionFrame", ipAddress, remoteSession.getSessionId()))
                            {
                                if (findWidget(sideEquipmentPhysical) == null){
                                    addNode(sideEquipmentPhysical);
                                    aSideEquipmentPhysical = sideEquipmentPhysical;
                                }
                            }
                        }
                        for(RemoteObjectLight sideEquipmentPhysical : physicalLinkDetails.getPhysicalPathForEndpointB()){
                            if(webserviceBean.isSubclassOf(sideEquipmentPhysical.getClassName(), "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId()) || 
                                    webserviceBean.isSubclassOf(sideEquipmentPhysical.getClassName(), "GenericDistributionFrame", ipAddress, remoteSession.getSessionId()))
                            {
                                if (findWidget(sideEquipmentPhysical) == null){
                                    addNode(sideEquipmentPhysical);
                                    bSideEquipmentPhysical = sideEquipmentPhysical;
                                }
                            }
                        }
                        //now we add the physical link 
                        if(aSideEquipmentPhysical != null && bSideEquipmentPhysical != null){
                            ObjectConnectionWidget logicalLinkWidget = (ObjectConnectionWidget) findWidget(physicalLinkDetails.getConnectionObject());
                            if(logicalLinkWidget == null){
                                 logicalLinkWidget = (ObjectConnectionWidget) addEdge(physicalLinkDetails.getConnectionObject());

                                logicalLinkWidget.getLabelWidget().setLabel(physicalLinkDetails.getPhysicalPathForEndpointA().get(0) + ":" + 
                                        physicalLinkDetails.getConnectionObject().getName() + ":" + physicalLinkDetails.getPhysicalPathForEndpointB().get(0));
                                setEdgeSource(physicalLinkDetails.getConnectionObject(), aSideEquipmentPhysical);
                                setEdgeTarget(physicalLinkDetails.getConnectionObject(), bSideEquipmentPhysical);
                            }
                        }
                    }
                }
                validate();
                repaint();
            } catch (Exception ex) {
                clear();
                //NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                ex.printStackTrace();
            }
        }
    }
    
    public boolean supportsConnections() { return true; }
    
    public boolean supportsBackgrounds() { return false; }

    @Override
    protected Widget attachNodeWidget(RemoteObjectLight node) {
        Widget newWidget;
        RemoteClassMetadata classMetadata = null;
        try {
            classMetadata = webserviceBean.getClass(node.getClassName(), ipAddress, remoteSession.getSessionId());
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        if (classMetadata != null) {
            try {
                newWidget = new ObjectNodeWidget(this, node, ImageIO.read(new ByteArrayInputStream(classMetadata.getIcon())));
            } catch (IOException ex) {
                newWidget = new ObjectNodeWidget(this, node);
            }
        } else
            newWidget = new ObjectNodeWidget(this, node);

        newWidget.setPreferredLocation(new Point(nodeLayer.getChildren().size() * 200, (nodeLayer.getChildren().size()) * 200 ));
        nodeLayer.addChild(newWidget);
        
        validate();
        return newWidget;
    }

    @Override
    protected Widget attachEdgeWidget(RemoteObjectLight edge) {
        ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge, 
        edge.getClassName().equals("RadioLink") ? ObjectConnectionWidget.DOT_LINE : ObjectConnectionWidget.LINE);
        newWidget.setRouter(RouterFactory.createFreeRouter());
        newWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        
        RemoteClassMetadata classMetadata = null;
        try {
            classMetadata = webserviceBean.getClass(edge.getClassName(), ipAddress, remoteSession.getSessionId());
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (classMetadata != null)
            newWidget.setLineColor(new Color(classMetadata.getColor()));
                
        edgeLayer.addChild(newWidget);
        validate();
        return newWidget;
    }
    
    @Override
    protected void attachEdgeSourceAnchor(RemoteObjectLight edge, RemoteObjectLight oldSourceNode, RemoteObjectLight newSourceNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget sourceWidget = findWidget(newSourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createCircularAnchor(sourceWidget, 3) : null);
        validate();
    }
    
    @Override
    protected void attachEdgeTargetAnchor(RemoteObjectLight edge, RemoteObjectLight oldTargetNode, RemoteObjectLight newTargetNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget targetWidget = findWidget(newTargetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createCircularAnchor(targetWidget, 3) : null);
        validate();
    }
}
