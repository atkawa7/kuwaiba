/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
 *
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package org.inventory.views.objectview.scene;

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.Utils;
import org.inventory.views.objectview.scene.actions.CustomAddRemoveControlPointAction;
import org.inventory.views.objectview.scene.actions.CustomMoveAction;
import org.inventory.views.objectview.scene.actions.CustomMoveControlPointAction;
import org.inventory.views.objectview.scene.menus.ObjectWidgetMenu;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectSceneEvent;
import org.netbeans.api.visual.model.ObjectSceneEventType;
import org.netbeans.api.visual.model.ObjectSceneListener;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;


/**
 * This is the main scene for an object's view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class ViewScene extends GraphScene<LocalObjectLight, LocalObjectLight>{
    /**
     * This layer is used to paint the auxiliary elements 
     */
    private LayerWidget interactionLayer;
    /**
     * Used to hold the background (just an image right now)
     */
    private LayerWidget backgroundLayer;
    /**
     * Used to hold the nodes
     */
    private LayerWidget nodesLayer;
    /**
     * Used to hold the connections
     */
    private LayerWidget edgesLayer;
    /**
     * Used to hold misc messages
     */
    private LayerWidget labelsLayer;
    /**
     * The common connection provider
     */
    private PhysicalConnectionProvider myConnectionProvider;
    /**
     * Default free router (shared by all connection widgets)
     */
    private Router freeRouter = RouterFactory.createFreeRouter();
    /**
     * Default control point move action (shared by all connection widgets)
     */
    private CustomMoveControlPointAction moveControlPointAction =
            new CustomMoveControlPointAction(new FreeMoveControlPointProvider(),null);
    /**
     * Default add/remove control point action (shared by all connection widgets)
     */
    private CustomAddRemoveControlPointAction addRemoveControlPointAction =
            new CustomAddRemoveControlPointAction(3.0, 5.0, null);
    /**
     * Default move action (shared by all node widgets)
     */
    private CustomMoveAction moveAction =
            new CustomMoveAction(ActionFactory.createFreeMoveStrategy(),ActionFactory.createDefaultMoveProvider());
    /**
     * Default inplace editor for node widgets
     */
    private LabelInplaceTextEditor inplaceEditor = new LabelInplaceTextEditor();
    /**
     * Object owning the current view
     */
    private LocalObjectLight currentObject;
    /**
     * Current view (if any, null if the current view does is just about to be created)
     */
    private LocalObjectViewLight currentView;
    /**
     * Action listeners
     */
    private List<ActionListener> listeners;
    
    /**
     * Constant to represent the selection tool
     */
    public final static String ACTION_SELECT = "selection"; //NOI18
    /**
     * Constant to represent the connection tool
     */
    public final static String ACTION_CONNECT = "connect"; //NOI18
    /**
     * Event ID to indicate a change in the scene (saving is not mandatory)
     */
    public final static int SCENE_CHANGE = 1;
    /**
     * Event ID to indicate a change in the scene (saving is mandatory)
     */
    public final static int SCENE_CHANGETOSAVE = 2;
    /**
     * Event ID to indicate an object has been selected
     */
    public final static int SCENE_OBJECTSELECTED = 3;
    /**
     * Default notifier
     */
    private NotificationUtil notifier;
    
    public ViewScene (NotificationUtil notifier){
        interactionLayer = new LayerWidget(this);
        backgroundLayer = new LayerWidget(this);
        nodesLayer = new LayerWidget(this);
        edgesLayer = new LayerWidget(this);
        labelsLayer = new LayerWidget(this);
        myConnectionProvider = new PhysicalConnectionProvider();
        
        addChild(backgroundLayer);
        addChild(edgesLayer);
        addChild(nodesLayer);
        addChild(labelsLayer);
	addChild(interactionLayer);
        
        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createPanAction());
        //getActions().addAction(ActionFactory.createRectangularSelectAction(this, backgroundLayer));
        setActiveTool(ACTION_SELECT);
        addObjectSceneListener(new ObjectSceneListener() {

            @Override
            public void objectAdded(ObjectSceneEvent ose, Object o) {}
            @Override
            public void objectRemoved(ObjectSceneEvent ose, Object o) {}
            @Override
            public void objectStateChanged(ObjectSceneEvent ose, Object o, ObjectState os, ObjectState os1) {}
            @Override
            public void selectionChanged(ObjectSceneEvent ose, Set<Object> oldSelection, Set<Object> newSelection) {
                if (newSelection.size() == 1){
                    Widget selectedWidget = findWidget(newSelection.iterator().next());
                    if (selectedWidget instanceof SelectableWidget)
                        fireChangeEvent(new ActionEvent(((SelectableWidget)selectedWidget).getNode(),
                                SCENE_OBJECTSELECTED, "object-selected-operation"));
                }
            }
            @Override
            public void highlightingChanged(ObjectSceneEvent ose, Set<Object> set, Set<Object> set1) {}
            @Override
            public void hoverChanged(ObjectSceneEvent ose, Object o, Object o1) {}
            @Override
            public void focusChanged(ObjectSceneEvent ose, Object o, Object o1) {}
        }, ObjectSceneEventType.OBJECT_SELECTION_CHANGED);
        this.notifier = notifier;
    }

    /**
     * This methods are called if addNode/addEdge instead of "addChild"
     * @param node
     * @return
     */
    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        ObjectNodeWidget widget = new ObjectNodeWidget(this, node);
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(new ObjectWidgetMenu()));
        nodesLayer.addChild(widget);
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        ObjectConnectionWidget widget = new ObjectConnectionWidget(this, edge, freeRouter);
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(new ObjectWidgetMenu()));
        edgesLayer.addChild(widget);
        return widget;
    }

    /**
     * These are called when creating anchors based on the past methods
     * @param edge
     * @param oldSourceNode
     * @param sourceNode
     */
    @Override
    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
    }

    public LayerWidget getInteractionLayer() {
        return interactionLayer;
    }

    public LayerWidget getBackgroundLayer(){
        return backgroundLayer;
    }

    public LayerWidget getNodesLayer(){
        return nodesLayer;
    }

    public LayerWidget getEdgesLayer(){
        return edgesLayer;
    }

    public LayerWidget getLabelsLayer() {
        return labelsLayer;
    }

    public LocalObjectLight getCurrentObject() {
        return currentObject;
    }

    public void setCurrentObject(LocalObjectLight currentObject) {
        this.currentObject = currentObject;
    }

    public LocalObjectViewLight getCurrentView() {
        return currentView;
    }

    public void setCurrentView(LocalObjectViewLight currentView) {
        this.currentView = currentView;
    }

    public CustomMoveControlPointAction getMoveControlPointAction() {
        return moveControlPointAction;
    }

    public CustomAddRemoveControlPointAction getAddRemoveControlPointAction() {
        return addRemoveControlPointAction;
    }

    public CustomMoveAction getMoveAction() {
        return moveAction;
    }

    public LabelInplaceTextEditor getInplaceEditor() {
        return inplaceEditor;
    }

    public Router getFreeRouter() {
        return freeRouter;
    }
    
    public PhysicalConnectionProvider getConnectionProvider(){
        return this.myConnectionProvider;
    }

    public void zoomIn() {
        synchronized (getSceneAnimator()) {
            double zoom = getSceneAnimator().isAnimatingZoomFactor () ? getSceneAnimator().getTargetZoomFactor () : getZoomFactor ();
            if(zoom < 4){
                getSceneAnimator().animateZoomFactor (zoom + 0.1);
                validate();
            }
        }
    }

    public void zoomOut() {
        synchronized (getSceneAnimator()) {
            double zoom = getSceneAnimator().isAnimatingZoomFactor () ? getSceneAnimator().getTargetZoomFactor () : getZoomFactor ();
            if(zoom > 0)
                getSceneAnimator().animateZoomFactor (zoom - 0.1);
        }
    }

    /**
     * Gets the background image
     * @return
     */
    public byte[] getBackgroundImage(){
        if (backgroundLayer.getChildren().isEmpty())
            return null;
        try {
            return Utils.getByteArrayFromImage(((ImageWidget) backgroundLayer.getChildren().iterator().next()).getImage(), "png"); //NOI18n
        } catch (IOException ex) {
            return null;
        }
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

    public void clear(){
        List<LocalObjectLight> clonedNodes = new ArrayList<LocalObjectLight>(getNodes());
        List<LocalObjectLight> clonedEdges = new ArrayList<LocalObjectLight>(getEdges());
        
        for(LocalObjectLight lol : clonedNodes)
            removeNode(lol);
        for(LocalObjectLight lol : clonedEdges)
            removeEdge(lol);

        moveAction.clearActionListeners();
        addRemoveControlPointAction.clearActionListeners();
        moveControlPointAction.clearActionListeners();
        nodesLayer.removeChildren();
        edgesLayer.removeChildren();
        backgroundLayer.removeChildren();
    }

    public void fireChangeEvent(ActionEvent ev){
        for (ActionListener listener : listeners)
            listener.actionPerformed(ev);
    }

    /**
     *
     * @return
     */
    public void setBackgroundImage(Image im){
        if (im == null) //Do nothing
            return;
        backgroundLayer.removeChildren();
        backgroundLayer.addChild(new ImageWidget(this, im));
        validate();
    }
    
    public void removeBackground() {
        backgroundLayer.removeChildren();
        fireChangeEvent(new ActionEvent(this, ViewScene.SCENE_CHANGE, "Remove Background"));
        validate();
    }

    public byte[] getAsXML() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        WAX xmlWriter = new WAX(bas);
        StartTagWAX mainTag = xmlWriter.start("view");
        mainTag.attr("version", Constants.VIEW_FORMAT_VERSION); //NOI18N
        //TODO: Get the class name from some else
        mainTag.start("class").text("DefaultView").end();
        StartTagWAX nodesTag = mainTag.start("nodes");
        for (Widget nodeWidget : nodesLayer.getChildren())
            nodesTag.start("node").attr("x", nodeWidget.getPreferredLocation().x).
            attr("y", nodeWidget.getPreferredLocation().y).
            attr("class", ((ObjectNodeWidget)nodeWidget).getObject().getClassName()).
            text(String.valueOf(((ObjectNodeWidget)nodeWidget).getObject().getOid()) ).end();
        nodesTag.end();

        StartTagWAX edgesTag = mainTag.start("edges");
        for (Widget edgeWidget : edgesLayer.getChildren()){
            StartTagWAX edgeTag = edgesTag.start("edge");
            edgeTag.attr("id", ((ObjectConnectionWidget)edgeWidget).getObject().getOid());
            edgeTag.attr("class", ((ObjectConnectionWidget)edgeWidget).getObject().getClassName());
            //I haven't managed to find out why sometimes the view gets screwed. This is a dirty
            //"solution", but I expect to solve it once we rewrite this module
            if (((ObjectConnectionWidget)edgeWidget).getSourceAnchor() == null)
                continue;
            edgeTag.attr("aside", ((ObjectNodeWidget)((ObjectConnectionWidget)edgeWidget).getSourceAnchor().getRelatedWidget()).getObject().getOid());
            if (((ObjectConnectionWidget)edgeWidget).getTargetAnchor() == null)
                continue;
            edgeTag.attr("bside", ((ObjectNodeWidget)((ObjectConnectionWidget)edgeWidget).getTargetAnchor().getRelatedWidget()).getObject().getOid());
            for (Point point : ((ObjectConnectionWidget)edgeWidget).getControlPoints())
                edgeTag.start("controlpoint").attr("x", point.x).attr("y", point.y).end();
            edgeTag.end();
        }
        edgesTag.end();
        mainTag.end().close();
        return bas.toByteArray();
    }

    public NotificationUtil getNotifier() {
        return notifier;
    }
}
