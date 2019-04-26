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

package org.kuwaiba.management.services.views.endtoend;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalLogicalConnectionDetails;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPhysicalConnectionDetails;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.actions.providers.CustomResizeProvider;
import org.inventory.core.visual.menu.FrameMenu;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 * Shows an end-to-end view of a service by trying to match the endpoints of the logical circuits
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different: It's intended to deal exclusively 
 * with logical connections, though it uses the physical path to fill the gaps between different logical segments
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class EndToEndViewScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
    
    public final static String VIEW_CLASS = "EndToEndView"; 
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private final LayerWidget imagesLayer;
    private final LayerWidget framesLayer;
    private final CustomResizeProvider resizeProvider;
    /**
     * Default move widget action (shared by all connection widgets)
     */
    private CustomMoveAction moveAction = new CustomMoveAction(this);
    /**
     * Default control point move action (shared by all connection widgets)
     */
    private CustomMoveControlPointAction moveControlPointAction =
            new CustomMoveControlPointAction(this);
    /**
     * Default add/remove control point action (shared by all connection widgets)
     */
    private CustomAddRemoveControlPointAction addRemoveControlPointAction =
            new CustomAddRemoveControlPointAction(this);
    
    private Random randomGenerator; 
    
    public EndToEndViewScene() {
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        imagesLayer = new LayerWidget(this);
        framesLayer = new LayerWidget(this);
        randomGenerator = new Random(1000); 
        
        addChild(framesLayer);
        addChild(imagesLayer);
        addChild(edgeLayer);
        addChild(nodeLayer);
        
        resizeProvider = new CustomResizeProvider(this);
        
        getActions().addAction(ActionFactory.createZoomAction());
        initSelectionListener();
    }
    
    @Override
    public byte[] getAsXML() { 
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
            
            for (Widget nodeWidget : nodeLayer.getChildren()) {
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(nodeWidget.getPreferredLocation().x)));
                xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(nodeWidget.getPreferredLocation().y)));
                LocalObjectLight lolNode = (LocalObjectLight) findObject(nodeWidget);
                xmlew.add(xmlef.createAttribute(new QName("class"), lolNode.getClassName()));
                xmlew.add(xmlef.createCharacters(lolNode.getId()));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                LocalObjectLight lolEdge = (LocalObjectLight) findObject(edgeWidget);
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
            // polygons
            QName qnamePolygons = new QName("polygons");
            xmlew.add(xmlef.createStartElement(qnamePolygons, null, null));
            for (Widget framesWidget : framesLayer.getChildren()) {
                QName qnamePolygon = new QName("polygon");
                LocalObjectLight lolFrame = (LocalObjectLight)findObject(framesWidget);
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
            Exceptions.printStackTrace(ex);
        }
        return null; 
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException { 
       //<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//        try {
//            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/oview_.xml");
//            fos.write(structure);
//            fos.close();
//        } catch(Exception e) {}
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

                        Widget aNodeWidget = findWidget(new LocalObjectLight(objectId, "", className));
                        if (aNodeWidget == null) //See if the object is in the default view. If so, just update its location
                            NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, 
                                    "Some nodes seem to be removed from the last time the view was loaded. Press the Save button to persist the changes");
                        else {
                            aNodeWidget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                            validate();
                        }
                    }else {
                        if (reader.getName().equals(qEdge)) {
                            String objectId = reader.getAttributeValue(null, "id"); //NOI18N
                            String className = reader.getAttributeValue(null,"class"); //NOI18N
 
                            ObjectConnectionWidget anEdgeWidget = (ObjectConnectionWidget)findWidget(new LocalObjectLight(objectId, "", className));
                            if (anEdgeWidget != null) { //The connection exists
                                List<Point> localControlPoints = new ArrayList<>();
                                while(true) {
                                    reader.nextTag();
                                    if (reader.getName().equals(qControlPoint)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                            localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                    } else {
                                        anEdgeWidget.setControlPoints(localControlPoints, false);
                                        validate();
                                        break;
                                    }
                                }
                            } else
                                NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, 
                                    "Some connections seem to be removed from the last time the view was loaded. Press the Save button to persist the changes");
                        }
                        // FREE FRAMES
                        else if (reader.getName().equals(qPolygon)) { 
                                String oid = UUID.randomUUID().toString();
                                LocalObjectLight lol = new LocalObjectLight(oid, oid + FREE_FRAME + reader.getAttributeValue(null, "title"), null);
                                Widget myPolygon = addNode(lol);
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
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_INFO)
                Exceptions.printStackTrace(ex);
        }
        validate();
        repaint();
    }

    @Override
    public void render(LocalObjectLight selectedService) {
        clear();
        List<LocalObjectLight> serviceResources = com.getServiceResources(selectedService.getClassName(), selectedService.getId());
        if (serviceResources == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            try {
                for (LocalObjectLight serviceResource : serviceResources) {
                    LocalObjectLight lastAddedASideEquipmentLogical = null, lastAddedBSideEquipmentLogical = null;
                    LocalObjectLight lastAddedASideEquipmentPhysical = null;
                    LocalObjectLight lastAddedBSideEquipmentPhysical = null;
                    if (com.isSubclassOf(serviceResource.getClassName(), "GenericLogicalConnection")) {
                        
                        LocalLogicalConnectionDetails logicalCircuitDetails = com.getLogicalLinkDetails(serviceResource.getClassName(), serviceResource.getId());
                        
                        LocalObjectLight aSideEquipmentLogical = null, bSideEquipmentLogical = null;
                        //Let's create the boxes corresponding to the endpoint A of the logical circuit
                        if(logicalCircuitDetails.getEndpointA() != null){
                            List<LocalObjectLight> parentsUntilFirstComEquipmentA; 
                            if(com.isSubclassOf(logicalCircuitDetails.getEndpointA().getClassName(), Constants.CLASS_GENERICLOGICALPORT)){
                                List<LocalObjectLight> parentsUntilFirstPhysicalPortA = com.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                    getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericPhysicalPort");
                                //This is only for pseudowire and will be removed once the MPLS sync has been finished, because vc ends in the device not a port
                                if(com.isSubclassOf(parentsUntilFirstPhysicalPortA.get(0).getClassName(), "GenericCommunicationsElement"))
                                    parentsUntilFirstComEquipmentA = Arrays.asList(parentsUntilFirstPhysicalPortA.get(0));
                                else
                                    parentsUntilFirstComEquipmentA = com.getParentsUntilFirstOfClass(parentsUntilFirstPhysicalPortA.get(0).
                                        getClassName(), parentsUntilFirstPhysicalPortA.get(0).getId(), "GenericCommunicationsElement");
                            }
                            else
                                parentsUntilFirstComEquipmentA = com.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                    getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericCommunicationsElement");

                            aSideEquipmentLogical = parentsUntilFirstComEquipmentA.get(parentsUntilFirstComEquipmentA.size() - 1);

                            lastAddedASideEquipmentLogical = aSideEquipmentLogical;

                            if (findWidget(aSideEquipmentLogical) == null)
                                addNode(aSideEquipmentLogical);
                        }
                        //Now the other side
                        if(logicalCircuitDetails.getEndpointB() != null){
                            List<LocalObjectLight> parentsUntilFirstComEquipmentB = com.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                    getClassName(), logicalCircuitDetails.getEndpointB().getId(), "GenericCommunicationsElement");
                            
                            bSideEquipmentLogical = parentsUntilFirstComEquipmentB.get(parentsUntilFirstComEquipmentB.size() - 1);

                            lastAddedBSideEquipmentLogical = bSideEquipmentLogical;
                            if (findWidget(bSideEquipmentLogical) == null)
                                addNode(bSideEquipmentLogical);
                        }
                                               
                        //Now the logical link
                        ObjectConnectionWidget logicalLinkWidget = (ObjectConnectionWidget) findWidget(logicalCircuitDetails.getConnectionObject());
                        if(logicalLinkWidget == null && aSideEquipmentLogical != null && bSideEquipmentLogical != null){
                            logicalLinkWidget = (ObjectConnectionWidget) addEdge(logicalCircuitDetails.getConnectionObject());

                            logicalLinkWidget.getLabelWidget().setLabel(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                    bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName());
                            setEdgeSource(logicalCircuitDetails.getConnectionObject(), aSideEquipmentLogical);
                            setEdgeTarget(logicalCircuitDetails.getConnectionObject(), bSideEquipmentLogical);
                        }
                        //Now with render the physical part
                        //We start with the A side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) {
                            List<LocalObjectLight> path = logicalCircuitDetails.getPhysicalPathForEndpointA();
                            LocalObjectLight connection = null;
                            LocalObjectLight device = null;
                            LocalObjectLight nextDevice = null;

                            for(int i = 0; i < path.size(); i++){
                                if(path.get(i).getClassName().equals("VirtualPort"))
                                    continue;
                                if(com.isSubclassOf(path.get(i).getClassName(), Constants.CLASS_GENERICPHYSICALLINK))
                                    connection = path.get(i);
                                else if(path.get(i).getClassName().equals("Pseudowire"))
                                    device = com.getParent(path.get(i).getClassName(), path.get(i).getId());
                                else{
                                    if(com.isSubclassOf(path.get(i).getClassName(), "GenericPhysicalPort"))
                                        device = com.getFirstParentOfClass(path.get(i).getClassName(), path.get(i).getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                                    //if the parent is still null, it should be an ODF
                                    if(device == null)
                                        device = com.getFirstParentOfClass(path.get(i).getClassName(), path.get(i).getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME);
                                        
                                    if(aSideEquipmentLogical != null && device.equals(aSideEquipmentLogical) || device != null && lastAddedASideEquipmentPhysical.equals(device))
                                        lastAddedASideEquipmentPhysical = device;
                                    else if(nextDevice != device)
                                        nextDevice = device;
                                    //is a mirror port
                                    if(lastAddedASideEquipmentPhysical.equals(device))
                                        continue;
                                }   
                                //if enters here it means that we have enough information to create the structure RemoteObjectLinkObject 
                                if(connection != null && nextDevice != null){
                                    //first we add the link
                                    if (findWidget(connection) == null){ 
                                        ObjectConnectionWidget physicalLinkWidget = (ObjectConnectionWidget) addEdge(connection);
                                         
                                        physicalLinkWidget.getLabelWidget().setLabel(
                                                lastAddedASideEquipmentPhysical.getName() + ":" + lastAddedASideEquipmentPhysical.getName() + " ** " +
                                            nextDevice.getName() + ":" + connection.getName());
                                    }
                                    //the we check if exist the last added noded and create if exists we create the source of the connection
                                    if(findWidget(lastAddedASideEquipmentPhysical) != null)
                                        setEdgeSource(connection, lastAddedASideEquipmentPhysical);
                                    //the we check if exist the next node if doen't exists we add the node and create the target of the connection
                                    if(findWidget(nextDevice) == null){
                                        addNode(nextDevice);
                                        setEdgeTarget(connection, nextDevice);
                                    }
                                    
                                    connection = null;
                                    lastAddedASideEquipmentPhysical = nextDevice;
                                    nextDevice = null; 
                                }
                            }//end for
                        }
                        //VLANs
                        //we must check if there is something to show with vlans
                        if(logicalCircuitDetails.getPhysicalPathForVlansEndpointA() != null && !logicalCircuitDetails.getPhysicalPathForVlansEndpointA().isEmpty()){
                            for (Map.Entry<LocalObjectLight, List<LocalObjectLight>> en : logicalCircuitDetails.getPhysicalPathForVlansEndpointA().entrySet()) {
                                List<LocalObjectLight> physicalPath = en.getValue();
                                
                                LocalObjectLight endpointVlan = com.getFirstParentOfClass(physicalPath.get(0).getClassName(), 
                                        physicalPath.get(0).getId(), 
                                        "GenericCommunicationsElement");
                                
                                LocalObjectLight physicalVlan = com.getFirstParentOfClass(physicalPath.get(2).getClassName(), 
                                        physicalPath.get(2).getId(), 
                                        "GenericCommunicationsElement");
                                    
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
                            List<LocalObjectLight> path = logicalCircuitDetails.getPhysicalPathForEndpointB();
                            LocalObjectLight connection = null;
                            LocalObjectLight device = null;
                            LocalObjectLight nextDevice = null;
                            
                            for(int i = 0; i < path.size(); i++){
                                if(path.get(i).getClassName().equals("VirtualPort"))
                                    continue;
                                if(com.isSubclassOf(path.get(i).getClassName(), Constants.CLASS_GENERICPHYSICALLINK))
                                    connection = path.get(i);
                                else if(path.get(i).getClassName().equals("Pseudowire"))
                                    device = com.getParent(path.get(i).getClassName(), path.get(i).getId());
                                else{
                                    if(com.isSubclassOf(path.get(i).getClassName(), "GenericPhysicalPort"))
                                        device = com.getFirstParentOfClass(path.get(i).getClassName(), path.get(i).getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                                    //if the parent is still null, it should be an ODF
                                    if(device == null)
                                        device = com.getFirstParentOfClass(path.get(i).getClassName(), path.get(i).getId(), Constants.CLASS_GENERICDISTRIBUTIONFRAME);
                                        
                                    if(bSideEquipmentLogical != null && device.equals(bSideEquipmentLogical) || device != null && lastAddedBSideEquipmentPhysical.equals(device))
                                        lastAddedBSideEquipmentPhysical = device;
                                    else if(nextDevice != device)
                                        nextDevice = device;
                                }   
                                //if enters here it means that we have enough information to create the structure RemoteObjectLinkObject 
                                if(connection != null && nextDevice != null){
                                    //first we add the link
                                    if (findWidget(connection) == null){ 
                                        ObjectConnectionWidget physicalLinkWidget = (ObjectConnectionWidget) addEdge(connection);
                                         
                                        physicalLinkWidget.getLabelWidget().setLabel(
                                                lastAddedBSideEquipmentPhysical.getName() + ":" + lastAddedBSideEquipmentPhysical.getName() + " ** " +
                                            nextDevice.getName() + ":" + connection.getName());
                                    }
                                    //the we check if exist the last added noded and create if exists we create the source of the connection
                                    if(findWidget(lastAddedBSideEquipmentPhysical) != null)
                                        setEdgeSource(connection, lastAddedBSideEquipmentPhysical);
                                    //the we check if exist the next node if doen't exists we add the node and create the target of the connection
                                    if(findWidget(nextDevice) == null){
                                        addNode(nextDevice);
                                        setEdgeTarget(connection, nextDevice);
                                    }
                                    
                                    connection = null;
                                    lastAddedBSideEquipmentPhysical = nextDevice;
                                    nextDevice = null; 
                                }
                            }//end for
                        }//we must check if there is something to show with vlans
                        if(logicalCircuitDetails.getPhysicalPathForVlansEndpointB() != null && !logicalCircuitDetails.getPhysicalPathForVlansEndpointB().isEmpty()){
                            for (Map.Entry<LocalObjectLight, List<LocalObjectLight>> en : logicalCircuitDetails.getPhysicalPathForVlansEndpointB().entrySet()) {

                                List<LocalObjectLight> physicalPath = en.getValue();
                                
                                LocalObjectLight endpointVlan = com.getFirstParentOfClass(physicalPath.get(0).getClassName(), 
                                        physicalPath.get(0).getId(), 
                                        "GenericCommunicationsElement");
                                
                                LocalObjectLight physicalVlan = com.getFirstParentOfClass(physicalPath.get(2).getClassName(), 
                                        physicalPath.get(2).getId(), 
                                        "GenericCommunicationsElement");
                                    
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
                    else if(com.isSubclassOf(serviceResource.getClassName(), "GenericPhysicalConnection")){
                        LocalPhysicalConnectionDetails physicalLinkDetails = com.getPhysicalLinkDetails(serviceResource.getClassName(), serviceResource.getId());
                        //sideA
                        LocalObjectLight aSideEquipmentPhysical = null, bSideEquipmentPhysical = null;
                        for(LocalObjectLight sideEquipmentPhysical : physicalLinkDetails.getPhysicalPathForEndpointA()){
                            if(com.isSubclassOf(sideEquipmentPhysical.getClassName(), "GenericCommunicationsElement") || 
                                    com.isSubclassOf(sideEquipmentPhysical.getClassName(), "GenericDistributionFrame"))
                            {
                                if (findWidget(sideEquipmentPhysical) == null){
                                    addNode(sideEquipmentPhysical);
                                    aSideEquipmentPhysical = sideEquipmentPhysical;
                                }
                            }
                        }
                        for(LocalObjectLight sideEquipmentPhysical : physicalLinkDetails.getPhysicalPathForEndpointB()){
                            if(com.isSubclassOf(sideEquipmentPhysical.getClassName(), "GenericCommunicationsElement") || 
                                    com.isSubclassOf(sideEquipmentPhysical.getClassName(), "GenericDistributionFrame"))
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
            } catch (Exception ex) {
                clear();
                //NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                ex.printStackTrace();
            }
        }
    }

    @Override
    public ConnectProvider getConnectProvider() { return null; }

    @Override
    public boolean supportsConnections() { return true; }

    @Override
    public boolean supportsBackgrounds() { return false; }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        Widget newWidget;
        if(node.getName().contains(FREE_FRAME)) {
            IconNodeWidget frameWidget = new IconNodeWidget(this);
            framesLayer.addChild(frameWidget);
            frameWidget.setToolTipText("Double-click to title text, resize on the corners");
            frameWidget.setBorder(BorderFactory.createImageBorder(new Insets (5, 5, 5, 5), ImageUtilities.loadImage ("org/inventory/design/topology/res/shadow_normal.png"))); // NOI18N
            frameWidget.setLayout(LayoutFactory.createVerticalFlowLayout (LayoutFactory.SerialAlignment.LEFT_TOP, 0));
            frameWidget.setPreferredBounds(new Rectangle (200, 200));
            frameWidget.setLabel(node.getName().substring(node.getName().indexOf(FREE_FRAME) + 9));
            
            frameWidget.createActions(AbstractScene.ACTION_SELECT);
            frameWidget.getActions(ACTION_SELECT).addAction(createSelectAction());
            frameWidget.getActions(ACTION_SELECT).addAction(ActionFactory.createResizeAction(resizeProvider, resizeProvider));
            frameWidget.getActions(ACTION_SELECT).addAction(moveAction);
            frameWidget.getActions().addAction(ActionFactory.createPopupMenuAction(FrameMenu.getInstance()));
            frameWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {

                @Override
                public boolean isEnabled(Widget widget) {
                    return true;
                }

                @Override
                public String getText(Widget widget) {
                    if (widget instanceof IconNodeWidget) {
                        LocalObjectLight lol = (LocalObjectLight)findObject(widget);
                        return lol.getName().substring(lol.getName().indexOf(FREE_FRAME) + 9);
                    }
                    return null;
                }

                @Override
                public void setText(Widget widget, String label) {
                    if(widget instanceof IconNodeWidget) {
                        LocalObjectLight lol = (LocalObjectLight)findObject(widget);
                        lol.setName(lol.getId() + FREE_FRAME + label);
                        ((IconNodeWidget) widget).setLabel(label);
                    }

                }
            }));
            fireChangeEvent(new ActionEvent(node, SCENE_CHANGE, "frame-add-operation"));

            return frameWidget;
        }
        else{
            LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
            if (classMetadata != null)
                newWidget = new ObjectNodeWidget(this, node, classMetadata.getIcon());
            else
                newWidget = new ObjectNodeWidget(this, node);

            newWidget.getActions().addAction(createSelectAction());
            newWidget.getActions().addAction(moveAction);
            newWidget.setPreferredLocation(new Point(nodeLayer.getChildren().size() * 200, (nodeLayer.getChildren().size() % 2) * 200 ));
            nodeLayer.addChild(newWidget);
        }
        validate();
        return newWidget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge, 
        edge.getClassName().equals("RadioLink") ? ObjectConnectionWidget.DOT_LINE : ObjectConnectionWidget.LINE);
        newWidget.getActions().addAction(createSelectAction());
        newWidget.getActions().addAction(moveControlPointAction);
        newWidget.getActions().addAction(addRemoveControlPointAction);
        newWidget.setRouter(RouterFactory.createFreeRouter());
        newWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
        if (classMetadata != null)
            newWidget.setLineColor(classMetadata.getColor());
        
        edgeLayer.addChild(newWidget);
        validate();
        return newWidget;
    }
    
    public void addFreeFrame() {
        String oid = UUID.randomUUID().toString();
        LocalObjectLight lol = new LocalObjectLight(oid, oid + FREE_FRAME + "New Frame", null);
        Widget newWidget = addNode(lol);
        newWidget.setPreferredLocation(new Point(100, 100));
        this.validate();
        this.repaint();
    }    
    
}
