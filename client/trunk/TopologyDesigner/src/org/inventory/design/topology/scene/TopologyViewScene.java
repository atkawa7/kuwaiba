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
package org.inventory.design.topology.scene;

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.actions.providers.CustomAcceptActionProvider;
import org.inventory.core.visual.actions.providers.CustomMoveProvider;
import org.inventory.core.visual.actions.providers.CustomResizeProvider;
import org.inventory.core.visual.actions.providers.CustomSelectProvider;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.design.topology.actions.TopologyViewActions;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.action.TextFieldInplaceEditor;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;

/**
 * Scene used for the Topology View Top Component
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class TopologyViewScene extends AbstractScene<LocalObjectLight, String> {
    /**
     * Version of the XML format used to store this view (see getAsXML method)
     */
    private final static String FORMAT_VERSION = "1.0";
    private final String CLOUD_ICON_PATH = "org/inventory/design/topology/res/cloudBig.png"; // NOI18
    public final static String CLOUD_ICON = "cloudIcon";
    public final static String FREE_FRAME = "freeFrame";
    private final Image cloudIcon = ImageUtilities.loadImage(CLOUD_ICON_PATH);
    
    private LayerWidget iconsLayer;
    private LayerWidget framesLayer;
    
    private CustomResizeProvider resizeProvider;
    private CustomMoveProvider moveProvider;
    private CustomAddRemoveControlPointAction addRemoveControlPointAction;
    private CustomMoveControlPointAction moveControlPointAction;
    private WidgetAction selectAction;
    private TopologyViewActions topologyViewActions;
    
    Random randomGenerator; 
    
    public TopologyViewScene() {
        randomGenerator = new Random();
        
        getActions().addAction(ActionFactory.createAcceptAction(new CustomAcceptActionProvider(this, Constants.CLASS_VIEWABLEOBJECT)));
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        backgroundLayer = new LayerWidget(this);
        
        iconsLayer = new LayerWidget(this);
        framesLayer = new LayerWidget(this);
        
        topologyViewActions = new TopologyViewActions(this);
        
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
        long oid = randomGenerator.nextInt(1000);
        LocalObjectLight lol = new LocalObjectLight(oid, oid + FREE_FRAME + "New Frame", null);
        Widget newWidget = addNode(lol);
        newWidget.setPreferredLocation(new Point(100, 100));
        this.validate();
        this.repaint();
    }
    
    public void addFreeCloud() {
        long oid = randomGenerator.nextInt(1000);
        LocalObjectLight lol = new LocalObjectLight(oid, oid + CLOUD_ICON + "New Cloud", null);
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
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        WAX xmlWriter = new WAX(bas);
        StartTagWAX mainTag = xmlWriter.start("view");
        mainTag.attr("version", FORMAT_VERSION); //NOI18N
        mainTag.start("class").text("TopologyView").end();
        //nodes
        StartTagWAX nodesTag = mainTag.start("nodes");
        for (Widget nodeWidget : nodeLayer.getChildren())
            nodesTag.start("node").attr("x", nodeWidget.getPreferredLocation().getX()).
            attr("y", nodeWidget.getPreferredLocation().getY()).
            attr("class", ((LocalObjectLight)findObject(nodeWidget)).getClassName()).
            text(Long.toString(((LocalObjectLight)findObject(nodeWidget)).getOid())).end();
        nodesTag.end();
        //free icons
        StartTagWAX iconsTag = mainTag.start("icons");
        for (Widget iconWidget : iconsLayer.getChildren()){
             iconsTag.start("icon").attr("type", 1).
                     attr("id",((LocalObjectLight)findObject(iconWidget)).getOid()).
                     attr("x", iconWidget.getPreferredLocation().getX()).
                     attr("y",iconWidget.getPreferredLocation().getY()).
                     text(((LocalObjectLight)findObject(iconWidget)).getName()).end();
        }
        iconsTag.end();
        //edges
        StartTagWAX edgesTag = mainTag.start("edges");
        for (Widget edgeWidget : edgeLayer.getChildren()){
            StartTagWAX edgeTag = edgesTag.start("edge");
            edgeTag.attr("id", "");
            edgeTag.attr("class", "");
            edgeTag.attr("name", ((ObjectConnectionWidget)edgeWidget).getName());
            
            String edgeObject = (String)findObject(edgeWidget);
            
            edgeTag.attr("aside", getEdgeSource(edgeObject).getOid());
            edgeTag.attr("bside", getEdgeTarget(edgeObject).getOid());
            
            for (Point point : ((ObjectConnectionWidget)edgeWidget).getControlPoints())
                edgeTag.start("controlpoint").attr("x", point.getX()).attr("y", point.getY()).end();
            edgeTag.end();
        }
        edgesTag.end();
        StartTagWAX polygonsTag = mainTag.start("poligons");
        for (Widget framesWidget : framesLayer.getChildren()){

            StartTagWAX polygonTag = mainTag.start("polygon");
            LocalObjectLight lolFrame = (LocalObjectLight)findObject(framesWidget);
            polygonTag.attr("title", lolFrame.getName().substring(lolFrame.getName().indexOf(FREE_FRAME) + 9));
            polygonTag.attr("color", "#000000");
            polygonTag.attr("border", "8");
            polygonTag.attr("fill", "none");

            polygonTag.attr("x", framesWidget.getPreferredLocation().getX());
            polygonTag.attr("y", framesWidget.getPreferredLocation().getY());
            polygonTag.attr("w", framesWidget.getBounds().width);
            polygonTag.attr("h", framesWidget.getBounds().height);

            polygonTag.start("vertex").attr("x0", framesWidget.getPreferredLocation().getX()).
                    attr("x1", framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().width).
                    attr("y0", framesWidget.getPreferredLocation().getY()).
                    attr("y1", framesWidget.getPreferredLocation().getY()).end();
            polygonTag.start("vertex").attr("x0", framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().getWidth()).
                    attr("x1", framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().width).
                    attr("y0", framesWidget.getPreferredLocation().getY()).
                    attr("y1", framesWidget.getPreferredLocation().getY() - framesWidget.getBounds().height).end();
            polygonTag.start("vertex").attr("x0", framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().width).
                    attr("x1", framesWidget.getPreferredLocation().getX()).
                    attr("y0", framesWidget.getPreferredLocation().getY()).
                    attr("y1", framesWidget.getPreferredLocation().getY()).end();
            polygonTag.start("vertex").attr("x0", framesWidget.getPreferredLocation().getX()).
                    attr("x1", framesWidget.getPreferredLocation().getX()).
                    attr("y0", framesWidget.getPreferredLocation().getY() - framesWidget.getBounds().height).
                    attr("y1", framesWidget.getPreferredLocation().getY()).end();
            polygonTag.end();
        }
        polygonsTag.end();
        
        mainTag.end().close();
        return bas.toByteArray();
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        //Here is where we use Woodstox as StAX provider
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        QName qNode = new QName("node"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qControlPoint = new QName("controlpoint"); //NOI18N
        QName qPolygon = new QName("polygon"); //NOI18N
        QName qIcon = new QName("icon"); //NOI18N
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()){
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT){
                    if (reader.getName().equals(qNode)){
                        String objectClass = reader.getAttributeValue(null, "class");

                        int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        Long objectId = Long.valueOf(reader.getElementText());

                        LocalObjectLight lol = CommunicationsStub.getInstance().
                                getObjectInfoLight(objectClass, objectId);
                        if (lol != null){
                            IconNodeWidget widget = (IconNodeWidget)this.addNode(lol);
                            widget.setPreferredLocation(new Point(x, y));
                        }
                        else
                            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, String.format("ViewAbleObject of class %s and id %s could not be found and was removed from the topology view", objectClass, objectId));
                    }else{
                        if (reader.getName().equals(qIcon)){ // FREE CLOUDS
                                if(Integer.valueOf(reader.getAttributeValue(null,"type"))==1){
                                    int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                                    int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                                    
                                    long oid = Long.valueOf(reader.getAttributeValue(null,"id"));
                                    LocalObjectLight lol = new LocalObjectLight(oid, reader.getElementText(), null);
                                    IconNodeWidget myCloud = (IconNodeWidget)this.addNode(lol);
                                    myCloud.setPreferredLocation(new Point(x, y));
                                }
                            }
                        else{
                            if (reader.getName().equals(qEdge)){
                                String edgeName = reader.getAttributeValue(null,"name");

                                Long aSide = Long.valueOf(reader.getAttributeValue(null,"aside"));
                                Long bSide = Long.valueOf(reader.getAttributeValue(null,"bside"));

                                if (edgeName != null){
                                    LocalObjectLight aSideObject = new LocalObjectLight(aSide, null, null);
                                    IconNodeWidget aSideWidget = (IconNodeWidget)this.findWidget(aSideObject);

                                    LocalObjectLight bSideObject = new LocalObjectLight(bSide, null, null);
                                    IconNodeWidget bSideWidget = (IconNodeWidget)this.findWidget(bSideObject);

                                    if (aSideWidget == null || bSideWidget == null)
                                        NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, "One or both of the endpoints of connection could not be found, so the connection was removed from the topology view");
                                    else{
                                        ObjectConnectionWidget newEdge = (ObjectConnectionWidget)this.addEdge(edgeName);
                                        this.setEdgeSource(edgeName, aSideObject);
                                        this.setEdgeTarget(edgeName, bSideObject);
                                        List<Point> localControlPoints = new ArrayList<>();
                                        while(true){
                                            reader.nextTag();
                                            if (reader.getName().equals(qControlPoint)){
                                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT){
                                                    String cpx = reader.getAttributeValue(null, "x");
                                                    String cpy = reader.getAttributeValue(null, "y");
                                                    Point point = new Point();
                                                    point.setLocation(Double.valueOf(cpx), Double.valueOf(cpy));
                                                    localControlPoints.add(point);
                                                }
                                            }else{
                                                newEdge.setControlPoints(localControlPoints, false);
                                                break;
                                            }
                                        }
                                    }
                                }else
                                    NotificationUtil.getInstance().showSimplePopup("Load view", NotificationUtil.INFO_MESSAGE, "Connection could not be found and was removed from the topology view");
                            }// edges endign 
                            else{
                                if (reader.getName().equals(qPolygon)) { // FREE FRAMES
                                    long oid = randomGenerator.nextInt(1000);
                                    LocalObjectLight lol = new LocalObjectLight(oid, oid + FREE_FRAME + reader.getAttributeValue(null, "title"), null);
                                    IconNodeWidget myPolygon = (IconNodeWidget)addNode(lol);
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
    public Color getConnectionColor(LocalObjectLight theConnection) {
        return null;
    }
    
    @Override
    public ConnectProvider getConnectProvider() {
        
        return new ConnectProvider() {

            @Override
            public boolean isSourceWidget(Widget sourceWidget) {
                return sourceWidget instanceof IconNodeWidget;
            }

            @Override
            public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
                if (targetWidget instanceof IconNodeWidget) {
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
                    String edge = "topologyEdge" + lolSource.getName() + lolTarget.getName() + randomGenerator.nextInt(1000);
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
            
            ObjectNodeWidget newNode = new ObjectNodeWidget(this, node);
            nodeLayer.addChild(newNode);
            
            newNode.setImage(CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false).getIcon());
            newNode.setLabel(node.toString());
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
            newNode.getActions().addAction(ActionFactory.createPopupMenuAction(topologyViewActions.createMenuForNode()));
            
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
                        lol.setName(lol.getOid() + CLOUD_ICON + label);
                        ((IconNodeWidget) widget).setLabel(label);
                    }
                }
            }));
            
            cloudWidget.getActions(ACTION_SELECT).addAction(selectAction);
            cloudWidget.getActions(ACTION_SELECT).addAction(ActionFactory.createMoveAction(moveProvider, moveProvider));
            cloudWidget.getActions(ACTION_CONNECT).addAction(selectAction);
            cloudWidget.getActions(ACTION_CONNECT).addAction(ActionFactory.createConnectAction(edgeLayer, getConnectProvider()));
            cloudWidget.getActions().addAction(ActionFactory.createPopupMenuAction(topologyViewActions.createMenuForNode()));
            
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
            frameWidget.getActions().addAction(ActionFactory.createPopupMenuAction(topologyViewActions.createMenuForFrame()));
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
        return null;
    }

    @Override
    protected Widget attachEdgeWidget(String edge) {
        ObjectConnectionWidget newEdge = new ObjectConnectionWidget(this, edge);
        newEdge.getActions().addAction(selectAction);
        newEdge.getActions().addAction(addRemoveControlPointAction);
        newEdge.getActions().addAction(moveControlPointAction);
        newEdge.setStroke(new BasicStroke(1));
        newEdge.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setRouter(RouterFactory.createFreeRouter());
        newEdge.getActions().addAction(ActionFactory.createPopupMenuAction(topologyViewActions.createMenuForConnection()));
        edgeLayer.addChild(newEdge);
        return newEdge;
    }

    @Override
    protected void attachEdgeSourceAnchor(String edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
        ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget) findWidget(edge);
        Widget sourceWidget = findWidget(sourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createCircularAnchor(sourceWidget, 3) : null);
    }

    @Override
    protected void attachEdgeTargetAnchor(String edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
        ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget) findWidget(edge);
        Widget targetWidget = findWidget(targetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createCircularAnchor(targetWidget, 3) : null);
    }
}
