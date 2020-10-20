/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.design.topology.scene;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
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
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.actions.providers.CustomAcceptActionProvider;
import org.inventory.core.visual.actions.providers.CustomMoveProvider;
import org.inventory.core.visual.actions.providers.CustomResizeProvider;
import org.inventory.core.visual.actions.providers.CustomSelectProvider;
import org.inventory.core.visual.menu.FrameMenu;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.inventory.design.topology.menus.ObjectConnectionWidgetMenu;
import org.inventory.design.topology.menus.ObjectNodeWidgetMenu;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 * Scene used for the Topology View Top Component
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class TopologyViewScene extends AbstractScene<LocalObjectLight, String> {
    /**
     * Version of the XML format used to store this view (see getAsXML method)
     */
    private final static String FORMAT_VERSION = "1.0";
    private final String CLOUD_ICON_PATH = "org/inventory/design/topology/res/cloudBig.png"; // NOI18
    public final static String CLOUD_ICON = "cloudIcon";
    private final Image cloudIcon = ImageUtilities.loadImage(CLOUD_ICON_PATH);
    
    private final LayerWidget iconsLayer;
    private final LayerWidget framesLayer;
    
    private final CustomResizeProvider resizeProvider;
    private final CustomMoveProvider moveProvider;
    private final CustomAddRemoveControlPointAction addRemoveControlPointAction;
    private final CustomMoveControlPointAction moveControlPointAction;
    private final WidgetAction selectAction;
        
    Random randomGenerator; 
    
    public TopologyViewScene() {
        randomGenerator = new Random();
        
        getActions().addAction(ActionFactory.createAcceptAction(new CustomAcceptActionProvider(this, Constants.CLASS_VIEWABLEOBJECT)));
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        backgroundLayer = new LayerWidget(this);
        
        iconsLayer = new LayerWidget(this);
        framesLayer = new LayerWidget(this);
                
        addChild(backgroundLayer);
        addChild(framesLayer);
        addChild(edgeLayer);
        addChild(iconsLayer);
        addChild(nodeLayer);
        
        resizeProvider = new CustomResizeProvider(this);
        moveProvider = new CustomMoveProvider(this);
        selectAction = ActionFactory.createSelectAction(new CustomSelectProvider(this), true);
        addRemoveControlPointAction = new CustomAddRemoveControlPointAction(this);
        moveControlPointAction = new CustomMoveControlPointAction(this);
        
        initSelectionListener();
    }    
    
    public void addFreeFrame() {
        String oid = UUID.randomUUID().toString();
        LocalObjectLight lol = new LocalObjectLight(oid, oid + FREE_FRAME + "New Frame", "AFrame");
        Widget newWidget = addNode(lol);
        newWidget.setPreferredLocation(new Point(100, 100));
        this.validate();
        this.repaint();
    }
    
    public void addFreeCloud() {
        String oid = UUID.randomUUID().toString();
        LocalObjectLight lol = new LocalObjectLight(oid, oid + CLOUD_ICON + "New Cloud", "ACloud");
        Widget newWidget = addNode(lol);
        newWidget.setPreferredLocation(new Point(100, 100));
        this.validate();
        this.repaint();
    }
    
    @Override
    public void clear(){
        backgroundLayer.removeChildren();
        super.clear();
    }

    @Override
    public byte[] getAsXML() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos, "UTF-8"); // By default the character set is ISO something
            XMLEventFactory xmlef = XMLEventFactory.newInstance();

            QName qnameView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), FORMAT_VERSION));
            QName qnameClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters("TopologyView"));
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            // nodes
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            
            for (Widget nodeWidget : nodeLayer.getChildren()) {
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), Double.toString(nodeWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("y"), Double.toString(nodeWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createAttribute(new QName("class"), ((LocalObjectLight)findObject(nodeWidget)).getClassName()));
                xmlew.add(xmlef.createCharacters(((LocalObjectLight)findObject(nodeWidget)).getId()));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            // free icons
            QName qnameIcons = new QName("icons");
            xmlew.add(xmlef.createStartElement(qnameIcons, null, null));
            for (Widget iconWidget : iconsLayer.getChildren()) {
                QName qnameIcon = new QName("icon");
                xmlew.add(xmlef.createStartElement(qnameIcon, null, null));
                xmlew.add(xmlef.createAttribute(new QName("type"), "1"));
                xmlew.add(xmlef.createAttribute(new QName("id"), ((LocalObjectLight)findObject(iconWidget)).getId()));
                xmlew.add(xmlef.createAttribute(new QName("x"), Double.toString(iconWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("y"), Double.toString(iconWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createCharacters(((LocalObjectLight)findObject(iconWidget)).getName()));
                xmlew.add(xmlef.createEndElement(qnameIcon, null));
            }
            xmlew.add(xmlef.createEndElement(qnameIcons, null));
            // edges
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                xmlew.add(xmlef.createAttribute(new QName("id"), ""));
                xmlew.add(xmlef.createAttribute(new QName("class"), ""));
                xmlew.add(xmlef.createAttribute(new QName("name"), ""));
                
                String edgeObject = (String)findObject(edgeWidget);
                
                xmlew.add(xmlef.createAttribute(new QName("aside"), getEdgeSource(edgeObject).getId()));
                xmlew.add(xmlef.createAttribute(new QName("bside"), getEdgeTarget(edgeObject).getId()));
                
                for (Point point : ((ConnectionWidget)edgeWidget).getControlPoints()) {
                    QName qnameControlpoint = new QName("controlpoint");
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), Double.toString(point.getX())));
                    xmlew.add(xmlef.createAttribute(new QName("y"), Double.toString(point.getY())));
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
        //Here is where we use Woodstox as StAX provider
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        
        QName qNode = new QName("node"); //NOI18N
        QName qView = new QName("view"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qControlPoint = new QName("controlpoint"); //NOI18N
        QName qPolygon = new QName("polygon"); //NOI18N
        QName qIcon = new QName("icon"); //NOI18N
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais, "UTF-8");

            while (reader.hasNext()){
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qView)) {
                         Double version = Double.valueOf(reader.getAttributeValue(null, "version"));
                         if (version != null && version >= 2) {
                              NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.ERROR_MESSAGE, "The view you are opening was saved using the web client and cannot be opened here");
                              clear();
                              break;
                         }
                    } else if (reader.getName().equals(qNode)){
                        String objectClass = reader.getAttributeValue(null, "class");

                        int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        String objectId = reader.getElementText();

                        LocalObjectLight lol = CommunicationsStub.getInstance().
                                getObjectInfoLight(objectClass, objectId);
                        if (lol != null)
                            this.addNode(lol).setPreferredLocation(new Point(x, y));
                        else
                            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, String.format("ViewAbleObject of class %s and id %s could not be found and was removed from the topology view", objectClass, objectId));
                    } else {
                        if (reader.getName().equals(qIcon)){ // FREE CLOUDS
                                if(Integer.valueOf(reader.getAttributeValue(null,"type"))==1){
                                    int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                                    int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                                    
                                    String oid = reader.getAttributeValue(null,"id");
                                    LocalObjectLight lol = new LocalObjectLight(oid, reader.getElementText(), "ACloud");
                                    this.addNode(lol).setPreferredLocation(new Point(x, y));
                                }
                            }
                        else {
                            if (reader.getName().equals(qEdge)) {
                                String aSide = reader.getAttributeValue(null,"aside");
                                String bSide = reader.getAttributeValue(null,"bside");

                                LocalObjectLight aSideObject = new LocalObjectLight(aSide, "", "AnEdge");
                                Widget aSideWidget = this.findWidget(aSideObject);

                                LocalObjectLight bSideObject = new LocalObjectLight(bSide, "", "AnEdge");
                                Widget bSideWidget = this.findWidget(bSideObject);

                                if (aSideWidget == null || bSideWidget == null)
                                    NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, "One or both of the endpoints of a connection could not be found. The connection was removed from the topology view");
                                else {
                                    String edgeName = "topologyEdge" + aSideObject.getId() + bSideObject.getId() + randomGenerator.nextInt(1000);
                                    ConnectionWidget newEdge = (ConnectionWidget)this.addEdge(edgeName);
                                    this.setEdgeSource(edgeName, aSideObject);
                                    this.setEdgeTarget(edgeName, bSideObject);
                                    List<Point> localControlPoints = new ArrayList<>();
                                    while (true) {
                                        reader.nextTag();
                                        if (reader.getName().equals(qControlPoint)) {
                                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                                String cpx = reader.getAttributeValue(null, "x");
                                                String cpy = reader.getAttributeValue(null, "y");
                                                Point point = new Point();
                                                point.setLocation(Double.valueOf(cpx), Double.valueOf(cpy));
                                                localControlPoints.add(point);
                                            }
                                        } else {
                                            newEdge.setControlPoints(localControlPoints, false);
                                            break;
                                        }
                                    }
                                }
                            }// edges endign 
                            else{ // FREE FRAMES
                                if (reader.getName().equals(qPolygon)) { 
                                    String oid = UUID.randomUUID().toString();
                                    LocalObjectLight lol = new LocalObjectLight(oid, oid + FREE_FRAME + reader.getAttributeValue(null, "title"), "AFrame");
                                    Widget myPolygon = addNode(lol);
                                    Point p = new Point();
                                    p.setLocation(Double.valueOf(reader.getAttributeValue(null, "x")), Double.valueOf(reader.getAttributeValue(null, "y")));
                                    myPolygon.setPreferredLocation(p);
                                    Dimension d = new Dimension();
                                    d.setSize(Double.valueOf(reader.getAttributeValue(null, "w")), Double.valueOf(reader.getAttributeValue(null, "h")));
                                    Rectangle r = new Rectangle(d);
                                    myPolygon.setPreferredBounds(r);
                                }
                            }//end qPolygon
                        } //end qIcons
                    } // end qNodes
                } // end if
            } // end while
            reader.close();
            
            this.validate();
            this.repaint();
        } catch (NumberFormatException | XMLStreamException ex) {
            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.ERROR_MESSAGE, "The view seems corrupted and could not be loaded");
            clear();
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
                Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public ConnectProvider getConnectProvider() {
        
        return new ConnectProvider() {

            @Override
            public boolean isSourceWidget(Widget sourceWidget) {
                return sourceWidget instanceof IconNodeWidget || sourceWidget instanceof ObjectNodeWidget;
            }

            @Override
            public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
                if (targetWidget instanceof IconNodeWidget || targetWidget instanceof ObjectNodeWidget) {
                    if(sourceWidget.equals(targetWidget))
                        return ConnectorState.REJECT;
                    
                    return ConnectorState.ACCEPT;
                }
                return ConnectorState.REJECT;
            }

            @Override
            public boolean hasCustomTargetWidgetResolver(Scene scene) {
                return false;
            }

            @Override
            public Widget resolveTargetWidget(Scene scene, Point point) {
                return null;
            }

            @Override
            public void createConnection(Widget sourceWidget, Widget targetWidget) {
                LocalObjectLight lolSource = (LocalObjectLight)findObject(sourceWidget);
                LocalObjectLight lolTarget = (LocalObjectLight)findObject(targetWidget);
                if(!lolSource.getName().contains(FREE_FRAME) && !lolTarget.getName().contains(FREE_FRAME)) {
                    String edge = "topologyEdge" + lolSource.getId() + lolTarget.getId() + randomGenerator.nextInt(1000);
                    addEdge(edge);
                    setEdgeSource(edge, lolSource);
                    setEdgeTarget(edge, lolTarget);
                    fireChangeEvent(new ActionEvent(this, SCENE_CHANGE, "attachEdge")); //NOI18N
                    validate();
                    repaint();
                }
            }
        };
    }

    @Override
    public boolean supportsConnections() {
        return true;
    }

    @Override
    public boolean supportsBackgrounds() {
        return true;
    }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        if (node.getName() == null || 
                (!node.getName().contains(CLOUD_ICON) && 
                    !node.getName().contains(FREE_FRAME))) {
            
            LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
            ObjectNodeWidget newNode;
            if (classMetadata == null) //Should not happen, but this check should always be done
                newNode = new ObjectNodeWidget(this, node);
            else
                newNode = new ObjectNodeWidget(this, node, classMetadata.getIcon());
            
            nodeLayer.addChild(newNode);
            
            newNode.createActions(AbstractScene.ACTION_SELECT);
            newNode.createActions(AbstractScene.ACTION_CONNECT);
            newNode.getActions().addAction(ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {

                @Override
                public boolean isEnabled(Widget widget) {
                    return true;
                }

                @Override
                public String getText(Widget widget) {
                    if (widget instanceof IconNodeWidget) {
                        LocalObjectLight lol = (LocalObjectLight)findObject(widget);
                        return lol.getName();
                    }
                    return null;
                }

                @Override
                public void setText(Widget widget, String label) {
                    if(widget instanceof IconNodeWidget) {
                        LocalObjectLight lol = (LocalObjectLight)findObject(widget);
                        lol.setName(label);
                        ((IconNodeWidget) widget).setLabel(lol.toString());
                    }
                }
            }));
            
            newNode.getActions(ACTION_SELECT).addAction(selectAction);
            newNode.getActions(ACTION_SELECT).addAction(ActionFactory.createMoveAction(moveProvider, moveProvider));
            newNode.getActions(ACTION_CONNECT).addAction(selectAction);
            newNode.getActions(ACTION_CONNECT).addAction(ActionFactory.createConnectAction(edgeLayer, getConnectProvider()));
            newNode.getActions().addAction(ActionFactory.createPopupMenuAction(ObjectNodeWidgetMenu.getInstance()));
            
            fireChangeEvent(new ActionEvent(node, SCENE_CHANGE, "lol-add-operation"));
            
            return newNode;
        }
        if (node.getName().contains(CLOUD_ICON)) {
            IconNodeWidget cloudWidget = new IconNodeWidget(this);
            iconsLayer.addChild(cloudWidget);
            cloudWidget.setImage(cloudIcon);
            cloudWidget.setLabel(node.getName().substring(node.getName().indexOf(CLOUD_ICON) + 9));
            cloudWidget.createActions(AbstractScene.ACTION_SELECT);
            cloudWidget.createActions(AbstractScene.ACTION_CONNECT);
            cloudWidget.getActions().addAction(ActionFactory.createInplaceEditorAction(new TextFieldInplaceEditor() {

                @Override
                public boolean isEnabled(Widget widget) {
                    return true;
                }

                @Override
                public String getText(Widget widget) {
                    if (widget instanceof IconNodeWidget) {
                        LocalObjectLight lol = (LocalObjectLight)findObject(widget);
                        return lol.getName().substring(lol.getName().indexOf(CLOUD_ICON) + 9);
                    }
                    return null;
                }

                @Override
                public void setText(Widget widget, String label) {
                    if(widget instanceof IconNodeWidget) {
                        LocalObjectLight lol = (LocalObjectLight)findObject(widget);
                        lol.setName(lol.getId() + CLOUD_ICON + label);
                        ((IconNodeWidget) widget).setLabel(label);
                    }
                }
            }));
            
            cloudWidget.getActions(ACTION_SELECT).addAction(selectAction);
            cloudWidget.getActions(ACTION_SELECT).addAction(ActionFactory.createMoveAction(moveProvider, moveProvider));
            cloudWidget.getActions(ACTION_CONNECT).addAction(selectAction);
            cloudWidget.getActions(ACTION_CONNECT).addAction(ActionFactory.createConnectAction(edgeLayer, getConnectProvider()));
            cloudWidget.getActions().addAction(ActionFactory.createPopupMenuAction(ObjectNodeWidgetMenu.getInstance()));
            
            fireChangeEvent(new ActionEvent(node, SCENE_CHANGE, "cloud-add-operation"));
            return cloudWidget;
        }
        if(node.getName().contains(FREE_FRAME)) {
            IconNodeWidget frameWidget = new IconNodeWidget(this);
            framesLayer.addChild(frameWidget);
            frameWidget.setToolTipText("Double-click to title text, resize on the corners");
            frameWidget.setBorder(BorderFactory.createImageBorder(new Insets (5, 5, 5, 5), ImageUtilities.loadImage ("org/inventory/design/topology/res/shadow_normal.png"))); // NOI18N
            frameWidget.setLayout(LayoutFactory.createVerticalFlowLayout (LayoutFactory.SerialAlignment.LEFT_TOP, 0));
            frameWidget.setPreferredBounds(new Rectangle (200, 200));
            frameWidget.setLabel(node.getName().substring(node.getName().indexOf(FREE_FRAME) + 9));
            
            frameWidget.createActions(AbstractScene.ACTION_SELECT);
            frameWidget.getActions(ACTION_SELECT).addAction(selectAction);
            frameWidget.getActions(ACTION_SELECT).addAction(ActionFactory.createResizeAction(resizeProvider, resizeProvider));
            frameWidget.getActions(ACTION_SELECT).addAction(ActionFactory.createMoveAction(moveProvider, moveProvider));
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
        return null;
    }

    @Override
    protected Widget attachEdgeWidget(String edge) {
        ConnectionWidget newEdge = new ConnectionWidget(this);
        newEdge.getActions().addAction(selectAction);
        newEdge.getActions().addAction(addRemoveControlPointAction);
        newEdge.getActions().addAction(moveControlPointAction);
        newEdge.setStroke(new BasicStroke(1));
        newEdge.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setRouter(RouterFactory.createFreeRouter());
        newEdge.getActions().addAction(ActionFactory.createPopupMenuAction(ObjectConnectionWidgetMenu.getInstance()));
        edgeLayer.addChild(newEdge);
        return newEdge;
    }

    @Override
    public void render(LocalObjectLight root) { } //Not used for this kind of view
}
