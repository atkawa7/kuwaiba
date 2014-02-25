/*
 *  Copyright 2010 - 2014 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.topology.scene;

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.awt.BasicStroke;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.views.topology.scene.menus.ConnectionMenu;
import org.inventory.views.topology.scene.menus.IconMenu;
import org.inventory.views.topology.scene.menus.LabelMenu;
import org.inventory.views.topology.scene.menus.NodeMenu;
import org.inventory.views.topology.scene.menus.PolygonMenu;
import org.inventory.views.topology.scene.provider.AcceptActionProvider;
import org.inventory.views.topology.scene.provider.LabelTextFieldEditor;
import org.inventory.views.topology.scene.provider.SceneConnectProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Scene used by the GISView component
 * @author Adrian Martinez Molinar <adrian.martinez@kuwaiba.org>
 */
public class TopologyViewScene extends GraphScene<Object, String> implements PropertyChangeListener, Lookup.Provider{

    /**
     * Path to nodes default icon
     */
    private final String GENERIC_ICON_PATH="org/inventory/views/topology/res/default.png"; //NOI18
    /**
     * Default icon
     */
    private final Image defaultIcon = ImageUtilities.loadImage(GENERIC_ICON_PATH);
    /**
     * Path to cloud icon
     */
    private final String CLOUD_ICON_PATH="org/inventory/views/topology/res/cloudBig.png"; //NOI18
    /**
     * default cloud icon
     */
    private final Image cloudIcon = ImageUtilities.loadImage(CLOUD_ICON_PATH);
    /**
     * Frames name
     */
    public String FREE_FRAME = "freeFrame";
    /**
     * Label name
     */
    public String FREE_LABEL = "freeLabel";
    /**
     * Cloud name
     */
    public String CLOUD_ICON = "cloudIcon";
    /**
     * Version of the XML format used to store this view (see getAsXML method)
     */
    private final static String FORMAT_VERSION = "1.0";
    /**
     * Layer to contain the nodes (poles, cabinets, etc)
     */
    private LayerWidget nodesLayer;
    /**
     * Layer to contain the icons(clouds, etc)
     */
    private LayerWidget iconsLayer;
    /**
     * Layer to contain the connections (containers, links, etc)
     */
    private LayerWidget edgesLayer;
    /**
     * Layer to contain additional labels (free text)
     */
    private LayerWidget labelsLayer;
    /**
     * A rectangle to delimit nodes, labels, connections (free frames)
     */
    private LayerWidget framesLayer;
    /**
     * Scene lookup
     */
    private SceneLookup lookup;
    /**
     * Nodes menu
     */
    private NodeMenu nodeMenu;
    /**
     * Connections menu
     */
    private ConnectionMenu connectionMenu;
    /**
     * frames menu
     */
    private PolygonMenu frameMenu;
    /**
     * Labels menu
     */
    private LabelMenu labelMenu;
    /**
     * Icons(cloud) menu
     */
    private IconMenu iconMenu;
    /**
     * Random to name the labels,frames and clouds
     */
    private Random randomGenerator;
    /**
     * Action listeners
     */
    private List<ActionListener> listeners;
    public final static int SCENE_OBJECTADDED = 1;
    /**
     * Default notifier
     */
    private NotificationUtil notifier;

    public TopologyViewScene(NotificationUtil notifier) {
        getActions().addAction(ActionFactory.createAcceptAction(new AcceptActionProvider(this)));

        nodesLayer = new LayerWidget(this);
        edgesLayer = new LayerWidget(this);
        labelsLayer = new LayerWidget(this);
        framesLayer =  new LayerWidget(this);
        iconsLayer = new LayerWidget(this);
        //menus
        nodeMenu = new NodeMenu(this);
        connectionMenu = new ConnectionMenu(this);
        frameMenu =  new PolygonMenu(this);
        labelMenu = new LabelMenu(this);
        iconMenu = new IconMenu(this);
        randomGenerator = new Random();

        addChild(framesLayer);
        addChild(edgesLayer);
        addChild(nodesLayer);
        addChild(iconsLayer);
        addChild(labelsLayer);

        this.lookup = new SceneLookup(Lookup.EMPTY);

        addObjectSceneListener(new ObjectSceneListener() {
            @Override
            public void objectAdded(ObjectSceneEvent event, Object addedObject) { }
            @Override
            public void objectRemoved(ObjectSceneEvent event, Object removedObject) {}
            @Override
            public void objectStateChanged(ObjectSceneEvent event, Object changedObject, ObjectState previousState, ObjectState newState) {}
            @Override
            public void selectionChanged(ObjectSceneEvent event, Set<Object> previousSelection, Set<Object> newSelection) {
                if (newSelection.size() == 1 && !(newSelection.iterator().next() instanceof String))
                    lookup.updateLookup((LocalObjectLight)newSelection.iterator().next());
            }
            @Override
            public void highlightingChanged(ObjectSceneEvent event, Set<Object> previousHighlighting, Set<Object> newHighlighting) {}
            @Override
            public void hoverChanged(ObjectSceneEvent event, Object previousHoveredObject, Object newHoveredObject) {}
            @Override
            public void focusChanged(ObjectSceneEvent event, Object previousFocusedObject, Object newFocusedObject) {}
        }, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        //Actions
        getActions().addAction(ActionFactory.createAcceptAction(new AcceptActionProvider(this)));

        setActiveTool(ObjectNodeWidget.ACTION_SELECT);
        this.notifier = notifier;
    }

    @Override
    protected Widget attachNodeWidget(Object node) {
        if(node instanceof LocalObjectLight){
            if(((LocalObjectLight)node).getName() == null || !((LocalObjectLight)node).getName().contains(CLOUD_ICON)){
                ObjectNodeWidget myWidget = new ObjectNodeWidget(this, (LocalObjectLight)node);
                nodesLayer.addChild(myWidget);
                Image myIcon = CommunicationsStub.getInstance().getMetaForClass(((LocalObjectLight)node).getClassName(), false).getIcon();
                if(myIcon == null)
                    myIcon = ImageUtilities.loadImage("org/inventory/views/topology/res/default.png");
                myWidget.setImage(myIcon);
                myWidget.setLabel(((LocalObjectLight)node).getName());
                myWidget.getActions(ObjectNodeWidget.ACTION_SELECT).addAction(createSelectAction());
                myWidget.getActions(ObjectNodeWidget.ACTION_SELECT).addAction(ActionFactory.createMoveAction());
                myWidget.getActions(ObjectNodeWidget.ACTION_CONNECT).addAction(ActionFactory.createConnectAction(edgesLayer, new SceneConnectProvider(this)));
                myWidget.getActions(ObjectNodeWidget.ACTION_SELECT).addAction(ActionFactory.createPopupMenuAction(nodeMenu));
                fireChangeEvent(new ActionEvent(node, SCENE_OBJECTADDED, "lol-add-operation"));
                return myWidget;
            }
            else{
                LocalObjectLight lol = new LocalObjectLight(randomGenerator.nextInt(1000), null, null);
                ObjectNodeWidget cloudWidget = new ObjectNodeWidget(this, lol);
                iconsLayer.addChild(cloudWidget);
                cloudWidget.setImage(cloudIcon);
                cloudWidget.setLabel(((LocalObjectLight)node).getName().substring(9));
                cloudWidget.getActions().addAction (ActionFactory.createInplaceEditorAction (new LabelTextFieldEditor()));
                cloudWidget.getActions(ObjectNodeWidget.ACTION_SELECT).addAction(ActionFactory.createMoveAction());
                cloudWidget.getActions(ObjectNodeWidget.ACTION_CONNECT).addAction(ActionFactory.createConnectAction(edgesLayer, new SceneConnectProvider(this)));
                cloudWidget.getActions().addAction(ActionFactory.createPopupMenuAction(iconMenu));
                fireChangeEvent(new ActionEvent(node, SCENE_OBJECTADDED, "cloud-add-operation"));
                return cloudWidget;
            }
        }
        //the frame with title
        if((node.toString().contains(FREE_FRAME))){
            ObjectFrameWidget myFrame = new ObjectFrameWidget(this, node.toString().substring(node.toString().indexOf(FREE_FRAME)+9));
            framesLayer.addChild (myFrame);
            myFrame.getActions().addAction(ActionFactory.createPopupMenuAction(frameMenu));
            myFrame.getActions ().addAction (ActionFactory.createResizeAction ());
            myFrame.getActions().addAction(ActionFactory.createMoveAction());
            fireChangeEvent(new ActionEvent(node, SCENE_OBJECTADDED, "frame-add-operation"));
            return myFrame;
        }//labels
        if((node.toString().contains(FREE_LABEL))){
            ObjectLabelWidget myFreeLabel = new ObjectLabelWidget(this,node.toString().substring(node.toString().lastIndexOf(FREE_LABEL)+9));
            labelsLayer.addChild(myFreeLabel);
            myFreeLabel.getActions().addAction(ActionFactory.createMoveAction());
            myFreeLabel.getActions().addAction (ActionFactory.createInplaceEditorAction (new LabelTextFieldEditor()));
            myFreeLabel.getActions().addAction(ActionFactory.createPopupMenuAction(labelMenu));
            fireChangeEvent(new ActionEvent(node, SCENE_OBJECTADDED, "label-add-operation"));
            return myFreeLabel;
        }
        else
            return null;
    }

    @Override
    protected Widget attachEdgeWidget(String edge) {
        ObjectConnectionWidget myWidget =  new ObjectConnectionWidget(this, edge);
        edgesLayer.addChild(myWidget);

        myWidget.getActions().addAction(ActionFactory.createPopupMenuAction(connectionMenu));
        myWidget.getActions().addAction(createSelectAction());
        myWidget.getActions().addAction(ActionFactory.createAddRemoveControlPointAction());
        myWidget.getActions().addAction(ActionFactory.createMoveControlPointAction(ActionFactory.createFreeMoveControlPointProvider()));
        myWidget.setStroke(new BasicStroke(1));
        myWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        myWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        myWidget.setRouter(RouterFactory.createFreeRouter());

        return myWidget;
    }

    @Override
    protected void attachEdgeSourceAnchor(String edge, Object oldSourceNode, Object sourceNode) {
        ObjectConnectionWidget widget = (ObjectConnectionWidget) findWidget(edge);
        Widget sourceNodeWidget = findWidget (sourceNode);
        widget.setSourceAnchor(sourceNodeWidget != null ? AnchorFactory.createCircularAnchor(sourceNodeWidget, 3) : null);
    }

    @Override
    protected void attachEdgeTargetAnchor(String edge, Object oldTargetNode, Object targetNode) {
        ObjectConnectionWidget widget = (ObjectConnectionWidget) findWidget(edge);
        Widget targetNodeWidget =  findWidget (targetNode);
        widget.setTargetAnchor(targetNodeWidget != null ? AnchorFactory.createCircularAnchor(targetNodeWidget, 3) : null);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        validate();
        repaint();
    }

    @Override
    public Lookup getLookup(){
        return this.lookup;
    }

    /**
     * Helper class to let us launch a lookup event every time a widget is selected
     */
    private class SceneLookup extends ProxyLookup{

        public SceneLookup(Lookup initialLookup) {
            super(initialLookup);
        }

        public void updateLookup(Lookup newLookup){
                setLookups(newLookup);
        }

        public void updateLookup(LocalObjectLight newElement){
            setLookups(Lookups.singleton(new ObjectNode(newElement)));
        }
    }

    public void addFreeFrame(){
        Widget f = addNode(randomGenerator.nextInt(1000)+FREE_FRAME+"New Title");
        f.setPreferredLocation (new Point (100, 100));
        this.validate();
        this.repaint();

    }
    public void addFreeLabel(){
        Widget f = addNode(randomGenerator.nextInt(1000)+FREE_LABEL+"New Label");
        f.setPreferredLocation (new Point (100, 100));
        this.validate();
        this.repaint();
    }
    public void addFreeCloud(){
        LocalObjectLight lol = new LocalObjectLight(randomGenerator.nextInt(1000), CLOUD_ICON + "New Icon", null);
        Widget f = addNode(lol);
        f.setPreferredLocation (new Point (100, 100));
        this.validate();
        this.repaint();
    }
   
    public void clear(){
        while (!getNodes().isEmpty())
            removeNode(getNodes().iterator().next());
        while (!getEdges().isEmpty())
            removeEdge(getEdges().iterator().next());
        
    }

    public LayerWidget getNodesLayer() {
        return nodesLayer;
    }

    public LayerWidget getIconLayer() {
        return iconsLayer;
    }

    /**
     * Export the scene to XML
     * @return a byte array
     */
    public byte[] getAsXML() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        WAX xmlWriter = new WAX(bas);
        StartTagWAX mainTag = xmlWriter.start("view");
        mainTag.attr("version", FORMAT_VERSION); //NOI18N
        mainTag.start("class").text("TopologyView").end();
        //nodes
        StartTagWAX nodesTag = mainTag.start("nodes");
        for (Widget nodeWidget : nodesLayer.getChildren())
            nodesTag.start("node").attr("x", nodeWidget.getPreferredLocation().getX()).
            attr("y", nodeWidget.getPreferredLocation().getY()).
            attr("class", ((ObjectNodeWidget)nodeWidget).getObject().getClassName()).
            text(Long.toString(((ObjectNodeWidget)nodeWidget).getObject().getOid())).end();
        nodesTag.end();
        //free icons
        StartTagWAX iconsTag = mainTag.start("icons");
        for (Widget icondWidget : iconsLayer.getChildren()){
             iconsTag.start("icon").attr("type", 1).
                     attr("id",((ObjectNodeWidget)icondWidget).getObject().getOid()).
                     attr("x", icondWidget.getPreferredLocation().getX()).
                     attr("y",icondWidget.getPreferredLocation().getY()).
                     text(((ObjectNodeWidget)icondWidget).getObject().getName()).end();
        }
        iconsTag.end();
        //edges
        StartTagWAX edgesTag = mainTag.start("edges");
        for (Widget edgeWidget : edgesLayer.getChildren()){
            StartTagWAX edgeTag = edgesTag.start("edge");
            edgeTag.attr("id", "");
            edgeTag.attr("class", "");
            edgeTag.attr("name", ((ObjectConnectionWidget)edgeWidget).getName());
            edgeTag.attr("aside", ((ObjectNodeWidget)((ObjectConnectionWidget)edgeWidget).getSourceAnchor().getRelatedWidget()).getObject().getOid());
            edgeTag.attr("bside", ((ObjectNodeWidget)((ObjectConnectionWidget)edgeWidget).getTargetAnchor().getRelatedWidget()).getObject().getOid());

            for (Point point : ((ObjectConnectionWidget)edgeWidget).getControlPoints())
                edgeTag.start("controlpoint").attr("x", point.getX()).attr("y", point.getY()).end();
            edgeTag.end();
        }
        edgesTag.end();
        //free labels
        StartTagWAX labelsTag = mainTag.start("labels");
        for (Widget labelWidget : labelsLayer.getChildren()){
             labelsTag.start("label").attr("x", labelWidget.getPreferredLocation().getX()).
             attr("y", labelWidget.getPreferredLocation().getY()).
             attr("orientation", ((ObjectLabelWidget)labelWidget).getOrientation()).
             text(((ObjectLabelWidget)labelWidget).getLabelText()).end();
        }
        labelsTag.end();
        //free frames
        StartTagWAX polygonsTag = mainTag.start("poligons");
        for (Widget framesWidget : framesLayer.getChildren()){

            StartTagWAX polygonTag = mainTag.start("polygon");
            polygonTag.attr("title", ((ObjectFrameWidget) framesWidget).getTitleLabel().getLabelText());
            polygonTag.attr("color", "#000000");
            polygonTag.attr("border", "8");
            polygonTag.attr("fill", "none");

            polygonTag.attr("x", framesWidget.getPreferredLocation().getX());
            polygonTag.attr("y", framesWidget.getPreferredLocation().getY());
            polygonTag.attr("w", framesWidget.getBounds().getWidth());
            polygonTag.attr("h", framesWidget.getBounds().getHeight());

            polygonTag.start("vertex").attr("x0", framesWidget.getPreferredLocation().getX()).
                    attr("x1", framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().getWidth()).
                    attr("y0", framesWidget.getPreferredLocation().getY()).
                    attr("y1", framesWidget.getPreferredLocation().getY()).end();
            polygonTag.start("vertex").attr("x0", framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().getWidth()).
                    attr("x1", framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().getWidth()).
                    attr("y0", framesWidget.getPreferredLocation().getY()).
                    attr("y1", framesWidget.getPreferredLocation().getY() - framesWidget.getBounds().getHeight()).end();
            polygonTag.start("vertex").attr("x0", framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().getWidth()).
                    attr("x1", framesWidget.getPreferredLocation().getX()).
                    attr("y0", framesWidget.getPreferredLocation().getY()).
                    attr("y1", framesWidget.getPreferredLocation().getY()).end();
            polygonTag.start("vertex").attr("x0", framesWidget.getPreferredLocation().getX()).
                    attr("x1", framesWidget.getPreferredLocation().getX()).
                    attr("y0", framesWidget.getPreferredLocation().getY() - framesWidget.getBounds().getHeight()).
                    attr("y1", framesWidget.getPreferredLocation().getY()).end();
            polygonTag.end();
        }
        polygonsTag.end();
        
        mainTag.end().close();
        return bas.toByteArray();
    }
    
    /**
     * To listen for scene changes implementing the observer design pattern
     * @param listener
     */
    public void addActionListener(ActionListener listener){
        if (listeners == null)
            listeners = new ArrayList<ActionListener>();
        listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener){
        if (listeners == null)
            return;
        listeners.remove(listener);
    }
    
    public void fireChangeEvent(ActionEvent ev){
        for (ActionListener listener : listeners)
            listener.actionPerformed(ev);
    }
    
    public NotificationUtil getNotifier() {
        return notifier;
    }
}
