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

package org.inventory.views.objectview.scene;

import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.LocalStuffFactory;
import org.inventory.core.services.api.LocalObject;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.visual.LocalEdge;
import org.inventory.core.services.api.visual.LocalLabel;
import org.inventory.core.services.api.visual.LocalNode;
import org.inventory.core.services.api.visual.LocalObjectView;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.Widget;

/**
 * This class builds every view so it can be rendered by the scene
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ViewBuilder {

    /**
     * Wraps the view to be built
     */
    private LocalObjectView myView;
    /**
     * Reference to the scene
     */
    private ViewScene scene;

    /**
     * This constructor should be used if there's already a view
     * @param localView
     * @throws NullPointerException if the LocalObjectViewImpl or the ViewScene provided are null
     */
    public ViewBuilder(LocalObjectView localView, ViewScene _scene) throws NullPointerException{
        if (_scene != null){
            this.myView = localView;
            this.scene = _scene;
        }
        else
            throw new NullPointerException("A null ViewScene is not supported by this constructor");
    }

    /**
     * Builds the actual view without refreshing . This method doesn't clean up the scene or refreshes it after building it,
     * that's coder's responsibility
     */
    public void buildView(){
        
        //We clean the object-widget mapping has in order to fill it again. So we do with listeners
        scene.clear();

        for (LocalNode node : myView.getNodes()){
            ObjectNodeWidget widget = new ObjectNodeWidget(scene, node);
            widget.setPreferredLocation(node.getPosition());
            scene.getNodesLayer().addChild(widget);
            if (scene.findObject(widget)==null)
                scene.addObject(widget.getObject(), widget);
        }

        for (LocalEdge edge : myView.getEdges()){
            ObjectConnectionWidget widget = new ObjectConnectionWidget(scene,
                    edge.getObject(),scene.getFreeRouter(), ObjectConnectionWidget.getConnectionColor(edge.getObject().getClassName()));

            //TODO: This is a reprocess... It's already been done when creating the local view
            for (Widget w : scene.getNodesLayer().getChildren()){
                if (((ObjectNodeWidget)w).getObject().equals(edge.getaSide().getObject()))
                    widget.setSourceAnchor(AnchorFactory.createFreeRectangularAnchor(w, true));
                else{
                    if(((ObjectNodeWidget)w).getObject().equals(edge.getbSide().getObject()))
                        widget.setTargetAnchor(AnchorFactory.createFreeRectangularAnchor(w, true));
                }
                if (widget.getSourceAnchor() != null && widget.getTargetAnchor() != null)
                    break;
            }
            if (widget.getSourceAnchor() == null || widget.getTargetAnchor() == null)
                scene.getNotifier().showSimplePopup("View Generation",
                        NotificationUtil.WARNING, "The connection "+
                        edge.getObject().getAttribute("name")+" ["+edge.getObject().getClassName()+"], id="+edge.getObject().getOid()+" has a side missing, please check if it was moved and refresh the view"); //NOI18N
            else{
                widget.setControlPoints(edge.getControlPoints(), true);
                scene.getEdgesLayer().addChild(widget);
                if (scene.findObject(widget)==null)
                    scene.addObject(widget.getObject(), widget);
            }
        }

        scene.setBackgroundImage(myView.getBackground());
    }

    /**
     * Builds a simple default view using the object's children and putting them one after another
     * @param myChildren
     */
    public void buildDefaultView(List<LocalObjectLight> myNodes,
            List<LocalObject> myPhysicalConnections) {
        int lastX = 0;
        List<LocalNode> myLocalNodes = new ArrayList<LocalNode>();
        List<LocalEdge> myLocalEdges = new ArrayList<LocalEdge>();

        for (LocalObjectLight node : myNodes){ //Add the nodes
            //Puts an element after another
            LocalNode ln = LocalStuffFactory.createLocalNode(node, lastX, 0);
            myLocalNodes.add(ln);
            lastX +=100;
        }

        //TODO: This algorithm to find the endpoints for a connection could be improved in many ways
        for (LocalObject container : myPhysicalConnections){
            LocalEdge le = LocalStuffFactory.createLocalEdge(container,null);

            for (LocalNode myNode : myLocalNodes){
                
                if (((Long)container.getAttribute("nodeA")).equals(myNode.getObject().getOid())) //NOI18N
                    le.setaSide(myNode);
                else{
                    if (((Long)container.getAttribute("nodeB")).equals(myNode.getObject().getOid())) //NOI18N
                       le.setbSide(myNode);
                }
                if (le.getaSide() != null && le.getbSide() != null)
                    break;
            }
            myLocalEdges.add(le);
        }
        myView = LocalStuffFactory.createLocalObjectView(myLocalNodes.toArray(new LocalNode[0]), myLocalEdges.toArray(new LocalEdge[0]),new LocalLabel[0]);
        buildView();
    }

    /**
     * This method takes the current view and adds/removes the nodes/connections according to a recalculation
     * of the view
     * @param myNodes
     * @param myPhysicalConnections
     */
    public void refreshView(List<LocalObjectLight> newNodes, List<LocalObject> newPhysicalConnections,
            List<LocalObjectLight> nodesToDelete, List<LocalObject> physicalConnectionsToDelete){

        scene.getNodesLayer().removeChildren();
        scene.getEdgesLayer().removeChildren();
        scene.getLabelsLayer().removeChildren();
        scene.getInteractionLayer().removeChildren();
        
        if (nodesToDelete != null){
            for (LocalObjectLight toDelete : nodesToDelete)
                myView.getNodes().remove(LocalStuffFactory.createLocalNode(toDelete, 0, 0));
        }

        if (physicalConnectionsToDelete != null){
            for (LocalObject toDelete : physicalConnectionsToDelete)
                myView.getEdges().remove(LocalStuffFactory.createLocalEdge(toDelete));
        }

        int i = 0;
        if (newNodes != null){
            for (LocalObjectLight toAdd : newNodes){
                myView.getNodes().add(LocalStuffFactory.createLocalNode(toAdd, i, 0));
                i+=100;
            }
        }

        if (newPhysicalConnections != null)
            for (LocalObject toAdd : newPhysicalConnections){
                LocalNode nodeA = getNodeMatching(myView.getNodes(), (Long)toAdd.getAttribute("nodeA"));
                if (nodeA == null)
                    continue;
                LocalNode nodeB = getNodeMatching(myView.getNodes(), (Long)toAdd.getAttribute("nodeB"));
                if (nodeB == null)
                    continue;
                myView.getEdges().add(LocalStuffFactory.createLocalEdge(toAdd, nodeA, nodeB, null));
            }

        buildView();
    }

    public LocalObjectView getMyView(){
        return this.myView;
    }

    /**
     * Helper to get a localnode which inner object has a given id
     * @param list
     * @param id
     * @return the node matching the oid or null if the object is not present
     */
    private LocalNode getNodeMatching(List<LocalNode> list, Long id){
        for (LocalNode node : list){
            if (node.getObject().getOid().equals(id))
                return node;
        }
        return null;
    }
}
