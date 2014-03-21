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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.Utils;
import org.inventory.core.visual.widgets.AbstractScene;
import org.inventory.views.objectview.scene.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.menu.ObjectWidgetMenu;
import org.inventory.core.visual.widgets.AbstractConnectionWidget;
import org.inventory.core.visual.widgets.AbstractNodeWidget;
import org.inventory.core.visual.widgets.TagLabelWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * This is the main scene for an object's view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class ViewScene extends AbstractScene {
    /**
     * Used to hold the background (just an image right now)
     */
    private LayerWidget backgroundLayer;
    /**
     * The common connection provider
     */
    private PhysicalConnectionProvider myConnectionProvider;
    /**
     * Default control point move action (shared by all connection widgets)
     */
    private CustomMoveControlPointAction moveControlPointAction =
            new CustomMoveControlPointAction(this);
    /**
     * Default add/remove control point action (shared by all connection widgets)
     */
//    private CustomAddRemoveControlPointAction addRemoveControlPointAction =
//            new CustomAddRemoveControlPointAction(this);
    /**
     * Object owning the current view
     */
    private LocalObjectLight currentObject;
    /**
     * Current view (if any, null if the current view does is just about to be created)
     */
    private LocalObjectViewLight currentView;
    
    public ViewScene (){
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
        getInputBindings ().setZoomActionModifiers(0); //No keystroke combinations
        getActions().addAction(ActionFactory.createPanAction());

        defaultPopupMenuProvider = new ObjectWidgetMenu();
        
        setActiveTool(ACTION_SELECT);
        initSelectionListener();
    }

    /**
     * This methods are called if addNode/addEdge instead of "addChild"
     * @param node
     * @return
     */
    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        AbstractNodeWidget widget = new AbstractNodeWidget(this, node);
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(defaultPopupMenuProvider));
        //The order the actions are added to a widget matters, if Select goes
        //after Move, you will need a double click to select the widget
        widget.getActions(ACTION_SELECT).addAction(createSelectAction());
        widget.getActions(ACTION_SELECT).addAction(ActionFactory.createMoveAction());
        widget.getActions(ACTION_CONNECT).addAction(ActionFactory.createConnectAction(interactionLayer, myConnectionProvider));
        TagLabelWidget aLabelWidget = new TagLabelWidget(this, widget);
        widget.addDependency(aLabelWidget);
        labelsLayer.addChild(aLabelWidget);
        nodesLayer.addChild(widget);
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        AbstractConnectionWidget widget = new AbstractConnectionWidget(this, edge);
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(defaultPopupMenuProvider));
        widget.getActions(ACTION_SELECT).addAction(createSelectAction());
        widget.getActions().addAction(ActionFactory.createAddRemoveControlPointAction());
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

    public LayerWidget getBackgroundLayer(){
        return backgroundLayer;
    }

    public LayerWidget getNodesLayer(){
        return nodesLayer;
    }

    public LayerWidget getEdgesLayer(){
        return edgesLayer;
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
    
    public PhysicalConnectionProvider getConnectionProvider(){
        return this.myConnectionProvider;
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

    @Override
    public void clear(){
        backgroundLayer.removeChildren();
        removeAllListeners();
        super.clear();
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

    @Override
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
            attr("class", ((AbstractNodeWidget)nodeWidget).getObject().getClassName()).
            text(String.valueOf(((AbstractNodeWidget)nodeWidget).getObject().getOid()) ).end();
        nodesTag.end();

        StartTagWAX edgesTag = mainTag.start("edges");
        for (Widget edgeWidget : edgesLayer.getChildren()){
            StartTagWAX edgeTag = edgesTag.start("edge");
            edgeTag.attr("id", ((AbstractConnectionWidget)edgeWidget).getObject().getOid());
            edgeTag.attr("class", ((AbstractConnectionWidget)edgeWidget).getObject().getClassName());
            //I haven't managed to find out why sometimes the view gets screwed. This is a dirty
            //"solution", but I expect to solve it once we rewrite this module
            if (((AbstractConnectionWidget)edgeWidget).getSourceAnchor() == null)
                continue;
            edgeTag.attr("aside", ((AbstractNodeWidget)((AbstractConnectionWidget)edgeWidget).getSourceAnchor().getRelatedWidget()).getObject().getOid());
            if (((AbstractConnectionWidget)edgeWidget).getTargetAnchor() == null)
                continue;
            edgeTag.attr("bside", ((AbstractNodeWidget)((AbstractConnectionWidget)edgeWidget).getTargetAnchor().getRelatedWidget()).getObject().getOid());
            for (Point point : ((AbstractConnectionWidget)edgeWidget).getControlPoints())
                edgeTag.start("controlpoint").attr("x", point.x).attr("y", point.y).end();
            edgeTag.end();
        }
        edgesTag.end();
        mainTag.end().close();
        return bas.toByteArray();
    }

    public NotificationUtil getNotifier() {
        return NotificationUtil.getInstance();
    }
}
