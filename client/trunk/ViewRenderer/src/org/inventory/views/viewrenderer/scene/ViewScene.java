/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.views.viewrenderer.scene;

import org.inventory.core.services.interfaces.LocalObjectLight;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;


/**
 * This is the main scene for an object's view
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ViewScene extends GraphScene<LocalObjectLight,String>{

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
     * The common connection provider
     */
    private PhysicalConnectionProvider myConnectionProvider;
    /**
     * Constant to represent the selection tool
     */
    public final static String ACTION_SELECT = "selection"; //NOI18
    /**
     * Constant to represent the connection tool
     */
    public final static String ACTION_CONNECT = "connect"; //NOI18

    public ViewScene (){
        interactionLayer = new LayerWidget(this);
        backgroundLayer = new LayerWidget(this);
        nodesLayer = new LayerWidget(this);
        edgesLayer = new LayerWidget(this);
        myConnectionProvider = new PhysicalConnectionProvider();
        addChild(backgroundLayer);
        addChild(nodesLayer);
        addChild(edgesLayer);
        getActions().addAction(ActionFactory.createZoomAction());
        setActiveTool(ACTION_SELECT);
    }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        System.out.println("attacnodewid");
        return null;
    }

    @Override
    protected Widget attachEdgeWidget(String edge) {
        System.out.println("attacedgewid");
        return null;
    }

    @Override
    protected void attachEdgeSourceAnchor(String edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
        System.out.println("attacsourceanch");
    }

    @Override
    protected void attachEdgeTargetAnchor(String edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
        System.out.println("attactargetanch");
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
}
