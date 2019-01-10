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
import java.awt.Dimension;
import java.io.ByteArrayOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLEventFactory;
import javax.xml.namespace.QName;
import java.awt.Point;
import java.awt.Rectangle;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;
import java.io.ByteArrayInputStream;
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
import org.kuwaiba.util.i18n.I18N;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
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
    public static final String VIEW_FORMAT_VERSION = "1.1";
    public static final String FREE_FRAME = "freeFrame";
    public static final String CLASS_GENERICLOGICALPORT = "GenericLogicalPort";
    public static final String CLASS_GENERICPORT = "GenericPort";
    public static final String CLASS_GENERICDISTRIBUTIONFRAME = "GenericDistributionFrame";
    
    private final WebserviceBean webserviceBean;
    private final RemoteSession remoteSession;    
    private final String ipAddress;
    
    public final static String VIEW_CLASS = "ServiceSimpleView"; 
        
    private final LayerWidget imagesLayer;
    private final LayerWidget framesLayer;
    protected LayerWidget labelsLayer;
    
    private final LayerWidget nodeLayer;
    private final LayerWidget edgeLayer;
    
    private final Random randomGenerator; 
    
    public EndToEndViewScene(String ipAddress, RemoteSession remoteSession, WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;
        this.remoteSession = remoteSession;
        this.ipAddress = ipAddress;
        
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        imagesLayer = new LayerWidget(this);
        framesLayer = new LayerWidget(this);
        randomGenerator = new Random(1000); 
        
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
                xmlew.add(xmlef.createCharacters(Long.toString(lolNode.getId())));
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
                
                xmlew.add(xmlef.createAttribute(new QName("id"), Long.toString(lolEdge.getId())));
                xmlew.add(xmlef.createAttribute(new QName("class"), lolEdge.getClassName()));
                
                xmlew.add(xmlef.createAttribute(new QName("aside"), Long.toString(getEdgeSource(lolEdge).getId())));
                xmlew.add(xmlef.createAttribute(new QName("bside"), Long.toString(getEdgeTarget(lolEdge).getId())));
                
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
            // polygons
            QName qnamePolygons = new QName("polygons");
            xmlew.add(xmlef.createStartElement(qnamePolygons, null, null));
            for (Widget framesWidget : framesLayer.getChildren()) {
                QName qnamePolygon = new QName("polygon");
                RemoteObjectLight lolFrame = (RemoteObjectLight)findObject(framesWidget);
                xmlew.add(xmlef.createStartElement(qnamePolygon, null, null));
                xmlew.add(xmlef.createAttribute(new QName("title"), lolFrame.getName().substring(lolFrame.getName().indexOf(FREE_FRAME) + 9)));
                xmlew.add(xmlef.createAttribute(new QName("color"), "#000000"));
                xmlew.add(xmlef.createAttribute(new QName("border"), "8"));
                xmlew.add(xmlef.createAttribute(new QName("fill"), "none"));
                
                xmlew.add(xmlef.createAttribute(new QName("x"), Double.toString(framesWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("y"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createAttribute(new QName("w"), Integer.toString(framesWidget.getBounds().width)));
                xmlew.add(xmlef.createAttribute(new QName("h"), Integer.toString(framesWidget.getBounds().height)));
                
                QName qnameVertex_w = new QName("vertex");
                xmlew.add(xmlef.createStartElement(qnameVertex_w, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x0"), Double.toString(framesWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("x1"), Double.toString(framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().width)));
                xmlew.add(xmlef.createAttribute(new QName("y0"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createAttribute(new QName("y1"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createEndElement(qnameVertex_w, null));
                
                QName qnameVertex_x = new QName("vertex");
                xmlew.add(xmlef.createStartElement(qnameVertex_x, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x0"), Double.toString(framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().getWidth())));
                xmlew.add(xmlef.createAttribute(new QName("x1"), Double.toString(framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().width)));
                xmlew.add(xmlef.createAttribute(new QName("y0"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createAttribute(new QName("y1"), Double.toString(framesWidget.getPreferredLocation().getY() - framesWidget.getBounds().height)));
                xmlew.add(xmlef.createEndElement(qnameVertex_x, null));
                
                QName qnameVertex_y = new QName("vertex");
                xmlew.add(xmlef.createStartElement(qnameVertex_y, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x0"), Double.toString(framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().width)));
                xmlew.add(xmlef.createAttribute(new QName("x1"), Double.toString(framesWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("y0"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createAttribute(new QName("y1"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createEndElement(qnameVertex_y, null));
                
                QName qnameVertex_z = new QName("vertex");
                xmlew.add(xmlef.createStartElement(qnameVertex_z, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x0"), Double.toString(framesWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("x1"), Double.toString(framesWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("y0"), Double.toString(framesWidget.getPreferredLocation().getY() - framesWidget.getBounds().height)));
                xmlew.add(xmlef.createAttribute(new QName("y1"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createEndElement(qnameVertex_z, null));
                
                xmlew.add(xmlef.createEndElement(qnamePolygon, null));
            }
            xmlew.add(xmlef.createEndElement(qnamePolygons, null));
            
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
////            Exceptions.printStackTrace(ex);
        }
        return null; 
    }

    public void render(byte[] structure) throws IllegalArgumentException { 
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
                        long objectId = Long.valueOf(reader.getElementText());

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
                            long objectId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N

                            long aSide = Long.valueOf(reader.getAttributeValue(null, "aside")); //NOI18N
                            long bSide = Long.valueOf(reader.getAttributeValue(null, "bside")); //NOI18N

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
                        // FREE FRAMES
                        else if (reader.getName().equals(qPolygon)) { 
                                long oid = randomGenerator.nextInt(1000);
                                RemoteObjectLight rol = new RemoteObjectLight(null, oid, oid + FREE_FRAME + reader.getAttributeValue(null, "title"));
                                Widget myPolygon = addNode(rol);
                                Point p = new Point();
                                p.setLocation(Double.valueOf(reader.getAttributeValue(null, "x")), Double.valueOf(reader.getAttributeValue(null, "y")));
                                myPolygon.setPreferredLocation(p);
                                Dimension d = new Dimension();
                                d.setSize(Double.valueOf(reader.getAttributeValue(null, "w")), Double.valueOf(reader.getAttributeValue(null, "h")));
                                Rectangle r = new Rectangle(d);
                                myPolygon.setPreferredBounds(r);

                        }//end qPolygon

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
        
        List<RemoteObjectLight> serviceResources = null;
        try {
            serviceResources = webserviceBean.getServiceResources(selectedService.getClassName(), selectedService.getId(), ipAddress, remoteSession.getSessionId());
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        if (serviceResources == null)
            return;
            
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
                            List<RemoteObjectLight> parentsUntilFirstPhysicalPortA = webserviceBean.getParentsUntilFirstOfClass(
                                logicalCircuitDetails.getEndpointA().getClassName(), 
                                logicalCircuitDetails.getEndpointA().getId(), 
                                "GenericPhysicalPort", 
                                ipAddress, 
                                remoteSession.getSessionId());
                            //This is only for pseudowire and will be removed once the MPLS sync has been finished, because vc ends in the device not a port
                            if(webserviceBean.isSubclassOf(parentsUntilFirstPhysicalPortA.get(0).getClassName(), "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId()))
                                parentsUntilFirstComEquipmentA = Arrays.asList(parentsUntilFirstPhysicalPortA.get(0));
                            else
                                parentsUntilFirstComEquipmentA = webserviceBean.getParentsUntilFirstOfClass(
                                    parentsUntilFirstPhysicalPortA.get(0).getClassName(), 
                                    parentsUntilFirstPhysicalPortA.get(0).getId(), 
                                    "GenericCommunicationsElement",
                                    ipAddress, 
                                    remoteSession.getSessionId()
                                );
                        }
                        else
                            parentsUntilFirstComEquipmentA = webserviceBean.getParentsUntilFirstOfClass(
                                logicalCircuitDetails.getEndpointA().getClassName(), 
                                logicalCircuitDetails.getEndpointA().getId(), 
                                "GenericCommunicationsElement", 
                                ipAddress,
                                remoteSession.getSessionId()
                            );

                        aSideEquipmentLogical = parentsUntilFirstComEquipmentA.get(parentsUntilFirstComEquipmentA.size() - 1);

                        lastAddedASideEquipmentLogical = aSideEquipmentLogical;

                        if (findWidget(aSideEquipmentLogical) == null) {
                            Widget w = addNode(aSideEquipmentLogical);
                            w.setPreferredLocation(new Point(100, 100));
                            validate();
                            repaint();
                        }
                    }
                    //Now the other side
                    if(logicalCircuitDetails.getEndpointB() != null){
                        List<RemoteObjectLight> parentsUntilFirstComEquipmentB;
                        if(webserviceBean.isSubclassOf(logicalCircuitDetails.getEndpointB().getClassName(), CLASS_GENERICLOGICALPORT, ipAddress, remoteSession.getSessionId())){
                             List<RemoteObjectLight> parentsUntilFirstPhysicalPortB = webserviceBean.getParentsUntilFirstOfClass(
                                logicalCircuitDetails.getEndpointB().getClassName(), 
                                logicalCircuitDetails.getEndpointB().getId(), 
                                "GenericPhysicalPort", 
                                ipAddress, 
                                remoteSession.getSessionId());
                            //This is only for pseudowire and will be removed once the MPLS sync has been finished, because vc ends in the device not a port
                            if(webserviceBean.isSubclassOf(parentsUntilFirstPhysicalPortB.get(0).getClassName(), "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId()))
                                 parentsUntilFirstComEquipmentB = Arrays.asList(parentsUntilFirstPhysicalPortB.get(0));
                            else
                                parentsUntilFirstComEquipmentB = webserviceBean.getParentsUntilFirstOfClass(
                                    parentsUntilFirstPhysicalPortB.get(0).getClassName(), 
                                    parentsUntilFirstPhysicalPortB.get(0).getId(), 
                                    "GenericCommunicationsElement",
                                    ipAddress,
                                    remoteSession.getSessionId());
                        }
                        else
                            parentsUntilFirstComEquipmentB = webserviceBean.getParentsUntilFirstOfClass(
                                logicalCircuitDetails.getEndpointB().getClassName(), 
                                logicalCircuitDetails.getEndpointB().getId(), 
                                "GenericCommunicationsElement",
                                ipAddress,
                                remoteSession.getSessionId());

                        bSideEquipmentLogical = parentsUntilFirstComEquipmentB.get(parentsUntilFirstComEquipmentB.size() - 1);

                        lastAddedBSideEquipmentLogical = bSideEquipmentLogical;
                        if (findWidget(bSideEquipmentLogical) == null) {
                            Widget w = addNode(bSideEquipmentLogical);
                            w.setPreferredLocation(new Point(100, 100));
                            validate();
                            repaint();
                        }
                    }

                    //Now the logical link
                    ObjectConnectionWidget logicalLinkWidget = (ObjectConnectionWidget) findWidget(logicalCircuitDetails.getConnectionObject());
                    if(logicalLinkWidget == null && aSideEquipmentLogical != null && bSideEquipmentLogical != null){
                        logicalLinkWidget = (ObjectConnectionWidget) addEdge(logicalCircuitDetails.getConnectionObject());

                        logicalLinkWidget.getLabelWidget().setLabel(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName());
                        setEdgeSource(logicalCircuitDetails.getConnectionObject(), aSideEquipmentLogical);
                        setEdgeTarget(logicalCircuitDetails.getConnectionObject(), bSideEquipmentLogical);
                        
                        List<Point> thePoints = new ArrayList();
                        thePoints.add(findWidget(bSideEquipmentLogical).getPreferredLocation());
                        thePoints.add(findWidget(bSideEquipmentLogical).getPreferredLocation());
                        logicalLinkWidget.setControlPoints(thePoints, false);
                        
                    }
                    //Now with render the physical part
                    //We start with the A side
                    if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) {
                        //first we check that the physical path starts in a port and ends in a port.
                        if (webserviceBean.isSubclassOf(logicalCircuitDetails.getPhysicalPathForEndpointA().get(0).getClassName(), CLASS_GENERICPORT, ipAddress, remoteSession.getSessionId())
                                &&
                            webserviceBean.isSubclassOf(logicalCircuitDetails.getPhysicalPathForEndpointA().get(logicalCircuitDetails.getPhysicalPathForEndpointA().size()-1).getClassName(), CLASS_GENERICPORT, ipAddress, remoteSession.getSessionId()))
                        {
                            RemoteObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointA().get(logicalCircuitDetails.getPhysicalPathForEndpointA().size()-1);
                            //If the equipemt physical is not a subclass of GenericCommunicationsElement, nothing will be shown.
                            RemoteObjectLight aSideEquipmentPhysical = webserviceBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), nextPhysicalHop.getId(), "ConfigurationItem", ipAddress, remoteSession.getSessionId());
                            //we check if is an ODF, if is not a GenericDistributionFrame, it should be a GenericCommunicationsElement
                            if(aSideEquipmentPhysical != null && !webserviceBean.isSubclassOf(aSideEquipmentPhysical.getClassName(), CLASS_GENERICDISTRIBUTIONFRAME, ipAddress, remoteSession.getSessionId()))
                                aSideEquipmentPhysical = webserviceBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), nextPhysicalHop.getId(), "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId());

                            if(aSideEquipmentPhysical == null)
                                Notifications.showInfo(I18N.gm("no_physical_part_has_been_set_sides"));
                            else{
                                if(findWidget(aSideEquipmentPhysical) == null) {
                                    Widget w = addNode(aSideEquipmentPhysical);
                                    w.setPreferredLocation(new Point(100, 100));
                                    validate();
                                    repaint();
                                }
                                //We add the physical link, we must check if the physical path has more than the end point
                                if (logicalCircuitDetails.getPhysicalPathForEndpointA().size() > 1 && findWidget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1)) == null){ 
                                    ObjectConnectionWidget physicalLinkWidgetA = (ObjectConnectionWidget) findWidget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1));
                                    //the link not yet added
                                    boolean flag = false;
                                    
                                    if(physicalLinkWidgetA == null) {
                                        physicalLinkWidgetA = (ObjectConnectionWidget) addEdge(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1));
                                        flag = true;
                                    }

                                    physicalLinkWidgetA.getLabelWidget().setLabel(lastAddedASideEquipmentPhysical != null ?
                                            lastAddedASideEquipmentPhysical.getName() : aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                        aSideEquipmentPhysical.getName() + ":" + nextPhysicalHop.getName());
                                    
                                    setEdgeSource(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1), lastAddedASideEquipmentPhysical == null ?
                                            aSideEquipmentLogical : lastAddedASideEquipmentPhysical);
                                    setEdgeTarget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1), aSideEquipmentPhysical);
                                    
                                    if (flag)
                                    {
                                        List<Point> thePoints = new ArrayList();
                                        thePoints.add(findWidget(lastAddedASideEquipmentPhysical == null ? aSideEquipmentLogical : lastAddedASideEquipmentPhysical).getPreferredLocation());
                                        thePoints.add(findWidget(aSideEquipmentPhysical).getPreferredLocation());
                                        physicalLinkWidgetA.setControlPoints(thePoints, false);
                                    }
                                    lastAddedASideEquipmentPhysical = aSideEquipmentPhysical;
                                }
                            }
                        }
                    }
                    //VLANs
                    //we must check if there is something to show with vlans
                    if(!getPhysicalPathForVlansEndpointA(logicalCircuitDetails).isEmpty()){
                        for (HashMap.Entry<RemoteObjectLight, List<RemoteObjectLight>> en : getPhysicalPathForVlansEndpointA(logicalCircuitDetails).entrySet()) {
                            List<RemoteObjectLight> physicalPath = en.getValue();

                            RemoteObjectLight endpointVlan = webserviceBean.getFirstParentOfClass(
                                    physicalPath.get(0).getClassName(), 
                                    physicalPath.get(0).getId(), 
                                    "GenericCommunicationsElement",
                                    ipAddress,
                                    remoteSession.getSessionId());

                            RemoteObjectLight physicalVlan = webserviceBean.getFirstParentOfClass(
                                    physicalPath.get(2).getClassName(), 
                                    physicalPath.get(2).getId(), 
                                    "GenericCommunicationsElement",
                                    ipAddress,
                                    remoteSession.getSessionId());

                            if (findWidget(physicalVlan) == null) {
                                Widget w = addNode(physicalVlan);
                                w.setPreferredLocation(new Point(100, 100));
                                validate();
                                repaint();
                            }

                            ObjectConnectionWidget physicalLinkWidget = (ObjectConnectionWidget) findWidget(physicalPath.get(1));

                            if(physicalLinkWidget == null) {
                                physicalLinkWidget = (ObjectConnectionWidget) addEdge(physicalPath.get(1));
                                
                                List<Point> thePoints = new ArrayList();
                                thePoints.add(new Point(0, 0));
                                thePoints.add(new Point(0, 0));
                                physicalLinkWidget.setControlPoints(thePoints, false);                                
                            }

                            physicalLinkWidget.getLabelWidget().setLabel(physicalPath.get(1) + "  " + physicalPath.get(2));
                            setEdgeTarget(physicalPath.get(1), physicalVlan);

                            if(!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()){
                                if(lastAddedASideEquipmentPhysical != null && endpointVlan.getId() == lastAddedASideEquipmentPhysical.getId())
                                    setEdgeSource(physicalPath.get(1), lastAddedASideEquipmentPhysical);
                            }
                            else if(lastAddedASideEquipmentLogical != null){
                                if(endpointVlan.getId() == lastAddedASideEquipmentLogical.getId()){
                                   setEdgeSource(physicalPath.get(1), lastAddedASideEquipmentLogical);
                                }
                            }
                        }
                    }
                    //Now the b side
                    if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) {

                        if (webserviceBean.isSubclassOf(logicalCircuitDetails.getPhysicalPathForEndpointB().get(0).getClassName(), CLASS_GENERICLOGICALPORT, ipAddress, remoteSession.getSessionId())
                           && webserviceBean.isSubclassOf(logicalCircuitDetails.getPhysicalPathForEndpointA().get(logicalCircuitDetails.getPhysicalPathForEndpointB().size()-1).getClassName(), CLASS_GENERICPORT, ipAddress, remoteSession.getSessionId()))
                        {
                            RemoteObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointB().get(logicalCircuitDetails.getPhysicalPathForEndpointB().size()-1);
                            RemoteObjectLight bSideEquipmentPhysical = webserviceBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), nextPhysicalHop.getId(), "ConfigurationItem", ipAddress, remoteSession.getSessionId());
                            //we check if is an ODF, if is not a GenericDistributionFrame, it should be a GenericCommunicationsElement
                            if(bSideEquipmentPhysical != null && !webserviceBean.isSubclassOf(bSideEquipmentPhysical.getClassName(), CLASS_GENERICDISTRIBUTIONFRAME, ipAddress, remoteSession.getSessionId()))
                                bSideEquipmentPhysical = webserviceBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), nextPhysicalHop.getId(), "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId());
                            //If the equipemt physical is not a subclass of GenericCommunicationsElement, nothing will be shown.
                            if(bSideEquipmentPhysical == null)
                                Notifications.showWarning(I18N.gm("no_physical_part_has_been_set_sides"));
                            else{
                                if (findWidget(bSideEquipmentPhysical) == null) {
                                    Widget w = addNode(bSideEquipmentPhysical);
                                    w.setPreferredLocation(new Point(100, 100));                                    
                                    validate();
                                    repaint();
                                }

                                if (logicalCircuitDetails.getPhysicalPathForEndpointB().size() > 1 && findWidget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(logicalCircuitDetails.getPhysicalPathForEndpointB().size()-1)) == null){ 
                                    ObjectConnectionWidget physicalLinkWidgetB = (ObjectConnectionWidget) findWidget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1));

                                    if(physicalLinkWidgetB == null) {
                                        physicalLinkWidgetB = (ObjectConnectionWidget) addEdge(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1));
                                        
                                        List<Point> thePoints = new ArrayList();
                                        thePoints.add(new Point(0, 0));
                                        thePoints.add(new Point(0, 0));
                                        physicalLinkWidgetB.setControlPoints(thePoints, false);
                                    }

                                    physicalLinkWidgetB.getLabelWidget().setLabel(lastAddedASideEquipmentPhysical != null ?
                                            lastAddedASideEquipmentPhysical.getName() : aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName() + " ** " +
                                        bSideEquipmentPhysical.getName() + ":" + nextPhysicalHop.getName());

                                    setEdgeSource(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1), lastAddedBSideEquipmentPhysical == null ? bSideEquipmentLogical : lastAddedBSideEquipmentPhysical);
                                    setEdgeTarget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1), bSideEquipmentPhysical);

                                    lastAddedBSideEquipmentPhysical = bSideEquipmentPhysical;
                                }
                            }
                        }
                    }
                    //we must check if there is something to show with vlans
                    if(!getPhysicalPathForVlansEndpointB(logicalCircuitDetails).isEmpty()){
                        for (Map.Entry<RemoteObjectLight, List<RemoteObjectLight>> en : getPhysicalPathForVlansEndpointB(logicalCircuitDetails).entrySet()) {

                            List<RemoteObjectLight> physicalPath = en.getValue();

                            RemoteObjectLight endpointVlan = webserviceBean.getFirstParentOfClass(physicalPath.get(0).getClassName(), 
                                    physicalPath.get(0).getId(), 
                                    "GenericCommunicationsElement",
                                    ipAddress,
                                    remoteSession.getSessionId());

                            RemoteObjectLight physicalVlan = webserviceBean.getFirstParentOfClass(physicalPath.get(2).getClassName(), 
                                    physicalPath.get(2).getId(), 
                                    "GenericCommunicationsElement",
                                    ipAddress,
                                    remoteSession.getSessionId());

                            if(findWidget(physicalVlan) == null) {
                                Widget w = addNode(physicalVlan);
                                w.setPreferredLocation(new Point(100, 100));
                                validate();
                                repaint();
                            }

                            ObjectConnectionWidget physicalLinkWidget = (ObjectConnectionWidget) findWidget(physicalPath.get(1));

                            if(physicalLinkWidget == null) {
                                physicalLinkWidget = (ObjectConnectionWidget) addEdge(physicalPath.get(1));
                                
                                List<Point> thePoints = new ArrayList();
                                thePoints.add(new Point(0, 0));
                                thePoints.add(new Point(0, 0));
                                physicalLinkWidget.setControlPoints(thePoints, false);
                            }

                            physicalLinkWidget.getLabelWidget().setLabel(physicalPath.get(1) + " " + physicalPath.get(2));
                            setEdgeTarget(physicalPath.get(1), physicalVlan);

                            if(logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()){
                                if(lastAddedBSideEquipmentPhysical != null && endpointVlan.getId() == lastAddedBSideEquipmentPhysical.getId())
                                    setEdgeSource(physicalPath.get(1), lastAddedBSideEquipmentPhysical);
                            }
                            else if(lastAddedASideEquipmentLogical != null){
                                if(endpointVlan.getId() == lastAddedBSideEquipmentLogical.getId()){
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
                                Widget w = addNode(sideEquipmentPhysical);
                                w.setPreferredLocation(new Point(100, 100));
                                validate();
                                repaint();
                                aSideEquipmentPhysical = sideEquipmentPhysical;
                            }
                        }
                    }
                    for(RemoteObjectLight sideEquipmentPhysical : physicalLinkDetails.getPhysicalPathForEndpointB()){
                        if(webserviceBean.isSubclassOf(sideEquipmentPhysical.getClassName(), "GenericCommunicationsElement", ipAddress, remoteSession.getSessionId()) || 
                                webserviceBean.isSubclassOf(sideEquipmentPhysical.getClassName(), "GenericDistributionFrame", ipAddress, remoteSession.getSessionId()))
                        {
                            if (findWidget(sideEquipmentPhysical) == null){
                                Widget w = addNode(sideEquipmentPhysical);
                                w.setPreferredLocation(new Point(100, 100));
                                validate();
                                repaint();
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
                            
                            List<Point> thePoints = new ArrayList();
                            thePoints.add(new Point(0, 0));
                            thePoints.add(new Point(0, 0));
                            logicalLinkWidget.setControlPoints(thePoints, false);
                        }
                    }
                }
            }
            validate();
            repaint();
        } catch (Exception ex) {
            Notifications.showError(ex.getMessage());
        }
    }
    
    public boolean supportsConnections() { return true; }
    
    public boolean supportsBackgrounds() { return false; }

    @Override
    protected Widget attachNodeWidget(RemoteObjectLight node) {
        Widget newWidget;
        if(node.getName().contains(FREE_FRAME)) {
            IconNodeWidget frameWidget = new IconNodeWidget(this);
            framesLayer.addChild(frameWidget);
            frameWidget.setToolTipText("Double-click to title text, resize on the corners");
////-->        frameWidget.setBorder(BorderFactory.createImageBorder(new Insets (5, 5, 5, 5), ImageUtilities.loadImage ("org/inventory/design/topology/res/shadow_normal.png"))); // NOI18N
            frameWidget.setLayout(LayoutFactory.createVerticalFlowLayout (LayoutFactory.SerialAlignment.LEFT_TOP, 0));
            frameWidget.setPreferredBounds(new Rectangle (200, 200));
            frameWidget.setLabel(node.getName().substring(node.getName().indexOf(FREE_FRAME) + 9));
            
////            frameWidget.createActions(AbstractScene.ACTION_SELECT);
////            frameWidget.getActions(ACTION_SELECT).addAction(createSelectAction());
////            frameWidget.getActions(ACTION_SELECT).addAction(ActionFactory.createResizeAction(resizeProvider, resizeProvider));
////            frameWidget.getActions(ACTION_SELECT).addAction(moveAction);
////            frameWidget.getActions().addAction(ActionFactory.createPopupMenuAction(FrameMenu.getInstance()));
////            frameWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {
////
////                @Override
////                public boolean isEnabled(Widget widget) {
////                    return true;
////                }
////
////                @Override
////                public String getText(Widget widget) {
////                    if (widget instanceof IconNodeWidget) {
////                        LocalObjectLight lol = (LocalObjectLight)findObject(widget);
////                        return lol.getName().substring(lol.getName().indexOf(FREE_FRAME) + 9);
////                    }
////                    return null;
////                }
////
////                @Override
////                public void setText(Widget widget, String label) {
////                    if(widget instanceof IconNodeWidget) {
////                        LocalObjectLight lol = (LocalObjectLight)findObject(widget);
////                        lol.setName(lol.getId() + FREE_FRAME + label);
////                        ((IconNodeWidget) widget).setLabel(label);
////                    }
////
////                }
////            }));
////            fireChangeEvent(new ActionEvent(node, SCENE_CHANGE, "frame-add-operation"));

            return frameWidget;
        }
        else{
            
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
                        
            newWidget.setPreferredLocation(new Point(nodeLayer.getChildren().size() * 200, (nodeLayer.getChildren().size() % 2) * 200 ));
            nodeLayer.addChild(newWidget);
        }
        validate();
        return newWidget;
    }

    @Override
    protected Widget attachEdgeWidget(RemoteObjectLight edge) {
        ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge);
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
    
////    public void addFreeFrame() {
////        long oid = randomGenerator.nextInt(1000);
////        LocalObjectLight lol = new LocalObjectLight(oid, oid + FREE_FRAME + "New Frame", null);
////        Widget newWidget = addNode(lol);
////        newWidget.setPreferredLocation(new Point(100, 100));
////        this.validate();
////        this.repaint();
////    }

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
