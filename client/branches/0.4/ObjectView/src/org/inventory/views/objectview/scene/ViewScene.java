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
import org.inventory.core.services.api.LocalObject;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.Utils;
import org.inventory.views.objectview.scene.actions.CustomAddRemoveControlPointAction;
import org.inventory.views.objectview.scene.actions.CustomMoveAction;
import org.inventory.views.objectview.scene.actions.CustomMoveControlPointAction;
import org.inventory.views.objectview.scene.menus.EdgeMenu;
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
public final class ViewScene extends GraphScene<LocalObjectLight,LocalObject>{

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
     * Popup menu used for edges
     */
    private EdgeMenu edgeMenu = new EdgeMenu();
    /**
     * Object owning the current view
     */
    private LocalObjectLight currentObject;
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
     * Version of the XML format used to store this view (see getAsXML method)
     */
    private final static String FORMAT_VERSION = "1.0";
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
        addChild(nodesLayer);
        addChild(edgesLayer);
        addChild(labelsLayer);
        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createPanAction());
        //getActions().addAction(ActionFactory.createRectangularSelectAction(this, backgroundLayer));
        setActiveTool(ACTION_SELECT);
        addObjectSceneListener(new ObjectSceneListener() {

            public void objectAdded(ObjectSceneEvent ose, Object o) {}
            public void objectRemoved(ObjectSceneEvent ose, Object o) {}
            public void objectStateChanged(ObjectSceneEvent ose, Object o, ObjectState os, ObjectState os1) {}
            public void selectionChanged(ObjectSceneEvent ose, Set<Object> oldSelection, Set<Object> newSelection) {
                if (newSelection.size() == 1){
                    fireChangeEvent(new ActionEvent(newSelection.iterator().next(),
                            SCENE_OBJECTSELECTED, "object-selected-operation"));
                }
            }
            public void highlightingChanged(ObjectSceneEvent ose, Set<Object> set, Set<Object> set1) {}
            public void hoverChanged(ObjectSceneEvent ose, Object o, Object o1) {}
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
        return null;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObject edge) {
        return null;
    }

    /**
     * These are called when creating anchors based on the past methods
     * @param edge
     * @param oldSourceNode
     * @param sourceNode
     */
    @Override
    protected void attachEdgeSourceAnchor(LocalObject edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObject edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
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

    public EdgeMenu getEdgeMenu() {
        return edgeMenu;
    }
    
    public PhysicalConnectionProvider getConnectionProvider(){
        return this.myConnectionProvider;
    }

    public void zoomIn() {
        synchronized (getSceneAnimator()) {
            double zoom = getSceneAnimator().isAnimatingZoomFactor () ? getSceneAnimator().getTargetZoomFactor () : getZoomFactor ();
            if(zoom < 4){
                getSceneAnimator().animateZoomFactor (zoom+0.5);
                validate();
            }
        }
    }

    public void zoomOut() {
        synchronized (getSceneAnimator()) {
            double zoom = getSceneAnimator().isAnimatingZoomFactor () ? getSceneAnimator().getTargetZoomFactor () : getZoomFactor ();
            if(zoom > 0)
                getSceneAnimator().animateZoomFactor (zoom-0.5);
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
        List myClone = new ArrayList(getObjects());
        for(Object obj : myClone)
            removeObject(obj);

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
        if (!backgroundLayer.getChildren().isEmpty())
            backgroundLayer.removeChildren(); //Clean the layer

        ImageWidget background = new ImageWidget(this,im);
        background.bringToBack();
        backgroundLayer.addChild(background);
    }

    public byte[] getAsXML() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        WAX xmlWriter = new WAX(bas);
        StartTagWAX mainTag = xmlWriter.start("view");
        mainTag.attr("version", FORMAT_VERSION); //NOI18N
        //TODO: Get the class name from some else
        mainTag.start("class").text("DefaultView").end();
        StartTagWAX nodesTag = mainTag.start("nodes");
        for (Widget nodeWidget : nodesLayer.getChildren())
            nodesTag.start("node").attr("x", nodeWidget.getPreferredLocation().getX()).
            attr("y", nodeWidget.getPreferredLocation().getY()).
            attr("class", ((ObjectNodeWidget)nodeWidget).getObject().getClassName()).
            text(((ObjectNodeWidget)nodeWidget).getObject().getOid().toString()).end();
        nodesTag.end();

        StartTagWAX edgesTag = mainTag.start("edges");
        for (Widget edgeWidget : edgesLayer.getChildren()){
            StartTagWAX edgeTag = edgesTag.start("edge");
            edgeTag.attr("id", ((ObjectConnectionWidget)edgeWidget).getObject().getOid());
            edgeTag.attr("class", ((ObjectConnectionWidget)edgeWidget).getObject().getClassName());
            edgeTag.attr("aside", ((ObjectNodeWidget)((ObjectConnectionWidget)edgeWidget).getSourceAnchor().getRelatedWidget()).getObject().getOid());
            edgeTag.attr("bside", ((ObjectNodeWidget)((ObjectConnectionWidget)edgeWidget).getTargetAnchor().getRelatedWidget()).getObject().getOid());
            for (Point point : ((ObjectConnectionWidget)edgeWidget).getControlPoints())
                edgeTag.start("controlpoint").attr("x", point.getX()).attr("y", point.getY()).end();
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