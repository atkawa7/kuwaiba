/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.management.services.nodes.actions.endtoend;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
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
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.actions.providers.CustomResizeProvider;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.anchor.AnchorFactory;
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
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EndToEndViewSimpleScene extends AbstractScene<LocalObjectLight, LocalObjectLight>{
    
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
    
    public EndToEndViewSimpleScene() {
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
            xmlew.add(xmlef.createCharacters("ServiceSimpleView"));
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
                xmlew.add(xmlef.createCharacters(Long.toString(lolNode.getOid())));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                
                ObjectConnectionWidget acwEdge = (ObjectConnectionWidget) edgeWidget;
                if (acwEdge.getSourceAnchor() == null || acwEdge.getTargetAnchor() == null) //This connection is malformed because one of the endpoints does not exist
                    continue;                                                               //probably, it was moved to another parent
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                
                LocalObjectLight lolEdge = (LocalObjectLight) findObject(acwEdge);
                xmlew.add(xmlef.createAttribute(new QName("id"), Long.toString(lolEdge.getOid())));
                xmlew.add(xmlef.createAttribute(new QName("class"), lolEdge.getClassName()));
                
                xmlew.add(xmlef.createAttribute(new QName("aside"), Long.toString(((LocalObjectLight) findObject(acwEdge.getSourceAnchor().getRelatedWidget())).getOid())));
                xmlew.add(xmlef.createAttribute(new QName("bside"), Long.toString(((LocalObjectLight) findObject(acwEdge.getTargetAnchor().getRelatedWidget())).getOid())));
                
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
        CommunicationsStub com = CommunicationsStub.getInstance();
        
        LocalObjectLight object = (LocalObjectLight) configObject.getProperty("currentObject");
        LocalObjectView currentView = (LocalObjectView) configObject.getProperty("currentView");
        
        /*Comment this out for debugging purposes*/
        try {
            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/oview_"+currentView.getId()+".xml");
            fos.write(currentView.getStructure());
            fos.close();
        } catch(Exception e) {
        }
        
        List<LocalObjectLight> myChildren = com.getObjectChildren(object.getOid(), com.getMetaForClass(object.getClassName(),false).getOid());
        if (myChildren == null)
            throw new IllegalArgumentException();
        
        List<LocalObject> myConnections = com.getChildrenOfClass(object.getOid(),object.getClassName(), Constants.CLASS_GENERICCONNECTION);
        if (myConnections == null)
            throw new IllegalArgumentException();
        
        if (structure == null) 
            render((LocalObjectLight)configObject.getProperty("currentObject"));
        else{
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

                            LocalObjectLight lol = CommunicationsStub.getInstance().getObjectInfoLight(objectClass, objectId);
                            if (lol != null) {
                                if (getNodes().contains(lol))
                                    NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, "The view seems to be corrupted. Self-healing measures were taken");
                                else {
                                    Widget widget = addNode(lol);
                                    widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                                    widget.setBackground(com.getMetaForClass(objectClass, false).getColor());
                                    validate();
                                    myChildren.remove(lol);
                                }
                            }
                            else
                                currentView.setDirty(true);
                        }else {
                            if (reader.getName().equals(qEdge)) {
                                long objectId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N

                                long aSide = Long.valueOf(reader.getAttributeValue(null, "aside")); //NOI18N
                                long bSide = Long.valueOf(reader.getAttributeValue(null, "bside")); //NOI18N

                                String className = reader.getAttributeValue(null,"class"); //NOI18N

                                LocalObjectLight container = com.getObjectInfoLight(className, objectId);
                                
                                boolean hasEndpointA = false;
                                boolean hasEndpointB = false;
                                    
                                if (container != null) { // if the connection exist
                                    HashMap<String, LocalObjectLight[]> specialAttributes = com.getSpecialAttributes(className, objectId);
                                    
                                    for (String key : specialAttributes.keySet()) {
                                        if(key.contains("ndpointA"))
                                            hasEndpointA = true;
                                        if(key.contains("ndpointB"))
                                            hasEndpointB = true;
                                    }
                                }
                                
                                if (hasEndpointA && hasEndpointB ) {
                                    myConnections.remove(container);

                                    LocalObjectLight aSideObject = new LocalObjectLight(aSide, null, null);
                                    ObjectNodeWidget aSideWidget = (ObjectNodeWidget) findWidget(aSideObject);

                                    LocalObjectLight bSideObject = new LocalObjectLight(bSide, null, null);
                                    ObjectNodeWidget bSideWidget = (ObjectNodeWidget) findWidget(bSideObject);

                                    if (aSideWidget == null || bSideWidget == null)
                                        currentView.setDirty(true);
                                    else {
                                        if (getEdges().contains(container))
                                            NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, "The view seems to be corrupted. Self-healing measures were taken");
                                        else {
                                            ObjectConnectionWidget newEdge = (ObjectConnectionWidget) addEdge(container);
                                            newEdge.setSourceAnchor(AnchorFactory.createCenterAnchor(aSideWidget.getNodeWidget()));
                                            newEdge.setTargetAnchor(AnchorFactory.createCenterAnchor(bSideWidget.getNodeWidget()));
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
                                            validate();
                                        }
                                    }
                                } else
                                    currentView.setDirty(true);
                            }
                            // FREE FRAMES
                            else if (reader.getName().equals(qPolygon)) { 
                                    long oid = randomGenerator.nextInt(1000);
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
                                        currentView.setZoom(Integer.valueOf(reader.getText()));
                                    else {
                                        if (reader.getName().equals(qCenter)) {
                                            double x = Double.valueOf(reader.getAttributeValue(null, "x"));
                                            double y = Double.valueOf(reader.getAttributeValue(null, "y"));
                                            currentView.setCenter(new double[]{ x, y });
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
    }

    @Override
    public void render(LocalObjectLight selectedService) {
        List<LocalObjectLight> serviceResources = com.getServiceResources(selectedService.getClassName(), selectedService.getOid());
        if (serviceResources == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            try {
                for (LocalObjectLight serviceResource : serviceResources) {
                    if (com.isSubclassOf(serviceResource.getClassName(), "GenericLogicalConnection")) {
                        LocalLogicalConnectionDetails logicalCircuitDetails = com.getLogicalLinkDetails(serviceResource.getClassName(), serviceResource.getOid());
                        
                        //Let's create the boxes corresponding to the endpoint A of the logical circuit
                        List<LocalObjectLight> parentsUntilFirstComEquipmentA = com.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getOid(), "GenericCommunicationsElement");
                        
                        LocalObjectLight aSideEquipmentLogical = parentsUntilFirstComEquipmentA.get(parentsUntilFirstComEquipmentA.size() - 1);
                        if (findWidget(aSideEquipmentLogical) == null)
                            addNode(aSideEquipmentLogical);
                        
                        //Now the other side
                        List<LocalObjectLight> parentsUntilFirstComEquipmentB = com.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                getClassName(), logicalCircuitDetails.getEndpointB().getOid(), "GenericCommunicationsElement");
                        
                        LocalObjectLight bSideEquipmentLogical = parentsUntilFirstComEquipmentB.get(parentsUntilFirstComEquipmentB.size() - 1);
                        if (findWidget(bSideEquipmentLogical) == null)
                            addNode(bSideEquipmentLogical);
                        
                        //Now the logical link
                        ObjectConnectionWidget logicalLinkWidget = (ObjectConnectionWidget)addEdge(logicalCircuitDetails.getConnectionObject());
                        logicalLinkWidget.getLabelWidget().setLabel(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName());
                        setEdgeSource(logicalCircuitDetails.getConnectionObject(), aSideEquipmentLogical);
                        setEdgeTarget(logicalCircuitDetails.getConnectionObject(), bSideEquipmentLogical);
                        
                        //Now with render the physical part
                        //We start with the A side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) {
                            LocalObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointA().get(logicalCircuitDetails.getPhysicalPathForEndpointA().size() - 1);
                            List<LocalObjectLight> parentsUntilFirstNextComEquipmentA = com.getParentsUntilFirstOfClass(nextPhysicalHop.
                                getClassName(), nextPhysicalHop.getOid(), "GenericCommunicationsElement");
                            
                            LocalObjectLight aSideEquipmentPhysical = parentsUntilFirstNextComEquipmentA.get(parentsUntilFirstNextComEquipmentA.size() - 1);
                            if (findWidget(aSideEquipmentPhysical) == null)
                                addNode(aSideEquipmentPhysical);
                            
                            if (findWidget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1)) == null) { 
                                ObjectConnectionWidget physicalLinkWidgetA = (ObjectConnectionWidget)addEdge(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1));
                                physicalLinkWidgetA.getLabelWidget().setLabel(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                    aSideEquipmentPhysical.getName() + ":" + nextPhysicalHop.getName());
                                setEdgeSource(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1), aSideEquipmentLogical);
                                setEdgeTarget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1), aSideEquipmentPhysical);
                            }
                        }
                        
                        //Now the b side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) {
                            LocalObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointB().get(logicalCircuitDetails.getPhysicalPathForEndpointB().size() - 1);
                            List<LocalObjectLight> parentsUntilFirstNextComEquipmentB = com.getParentsUntilFirstOfClass(nextPhysicalHop.
                                getClassName(), nextPhysicalHop.getOid(), "GenericCommunicationsElement");
                        
                            LocalObjectLight bSideEquipmentPhysical = parentsUntilFirstNextComEquipmentB.get(parentsUntilFirstNextComEquipmentB.size() - 1);
                            if (findWidget(bSideEquipmentPhysical) == null)
                                addNode(bSideEquipmentPhysical);
                            
                            if (findWidget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1)) == null) { 
                                ObjectConnectionWidget physicalLinkWidgetB = (ObjectConnectionWidget)addEdge(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1));
                                physicalLinkWidgetB.getLabelWidget().setLabel(bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName() + " ** " +
                                    bSideEquipmentPhysical.getName() + ":" + nextPhysicalHop.getName());
                                setEdgeSource(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1), bSideEquipmentLogical);
                                setEdgeTarget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1), bSideEquipmentPhysical);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                clear();
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            }
        }
    }

    @Override
    public Color getConnectionColor(LocalObjectLight theConnection) { return Color.BLACK; }

    @Override
    public ConnectProvider getConnectProvider() { return null; }

    @Override
    public boolean supportsConnections() { return true; }

    @Override
    public boolean supportsBackgrounds() { return false; }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        Widget newWidget;
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
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
            //frameWidget.getActions().addAction(ActionFactory.createPopupMenuAction(FrameMenu.getInstance()));
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
                        lol.setName(lol.getOid() + FREE_FRAME + label);
                        ((IconNodeWidget) widget).setLabel(label);
                    }

                }
            }));
            fireChangeEvent(new ActionEvent(node, SCENE_CHANGE, "frame-add-operation"));

            return frameWidget;
        }
        else{
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
        ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge);
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

    @Override
    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight newSourceNode) {
        ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget)findWidget(edge);
        Widget sourceWidget = findWidget(newSourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createCircularAnchor(sourceWidget, 3) : null);
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight newTargetNode) {
        ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget)findWidget(edge);
        Widget targetWidget = findWidget(newTargetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createCircularAnchor(targetWidget, 3) : null);
    }
    
    public void addFreeFrame() {
        long oid = randomGenerator.nextInt(1000);
        LocalObjectLight lol = new LocalObjectLight(oid, oid + FREE_FRAME + "New Frame", null);
        Widget newWidget = addNode(lol);
        newWidget.setPreferredLocation(new Point(100, 100));
        this.validate();
        this.repaint();
    }
}
