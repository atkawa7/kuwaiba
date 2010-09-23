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
import org.inventory.communications.core.views.LocalEdge;
import org.inventory.communications.core.views.LocalLabel;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalNode;
import org.inventory.core.services.interfaces.LocalObject;
import org.inventory.core.services.interfaces.LocalObjectLight;
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
     * @throws NullPointerException if the LocalObjectView or the ViewScene provided are null
     */
    public ViewBuilder(LocalObjectView localView, ViewScene _scene) throws NullPointerException{
        if (_scene != null){
            this.myView = localView;
            this.scene = _scene;
        }
        else
            throw new NullPointerException("A null ViewScene is not supported for this constructor");
    }

    /**
     * Builds the actual view without refreshing . This method doesn't clean up the scene or refreshes it after building it,
     * that's coder's responsibility
     */
    public void buildView(){

        for (LocalNode node : myView.getNodes()){
            ObjectNodeWidget widget = new ObjectNodeWidget(scene, node);
            widget.setPreferredLocation(node.getPosition());
            scene.getNodesLayer().addChild(widget);
        }

        for (LocalEdge edge : myView.getEdges()){
            ObjectConnectionWidget widget = new ObjectConnectionWidget(scene, edge.getObject());

            //TODO: This is a reprocess... It's already been done when creating the local view
            for (Widget w : scene.getNodesLayer().getChildren()){
                if (((ObjectNodeWidget)w).getObject().equals(edge.getaSide().getObject())){
                    widget.setSourceAnchor(AnchorFactory.createRectangularAnchor(w));
                    break;
                }
                else{
                    if(((ObjectNodeWidget)w).getObject().equals(edge.getbSide().getObject())){
                        widget.setTargetAnchor(AnchorFactory.createRectangularAnchor(w));
                        break;
                    }
                }
            }

            scene.getNodesLayer().addChild(widget);
        }

        scene.setBackgroundImage(myView.getBackground());
    }

    /**
     * Builds a simple default view using the object's children and putting them one after another
     * @param myChildren
     */
    public void buildDefaultView(List<LocalObjectLight> myNodes, LocalObject[] myPhysicalConnections) {
        int lastX = 0;
        List<LocalNode> myLocalNodes = new ArrayList<LocalNode>();
        List<LocalEdge> myLocalEdges = new ArrayList<LocalEdge>();

        for (LocalObjectLight node : myNodes){ //Add the nodes
            //Puts an element after another
            LocalNode ln = new LocalNode(node, lastX, 0);
            myLocalNodes.add(ln);
            lastX +=100;
        }

        //TODO: This algorithm to find the endpoints for a connection could be improved in a lot of way
        for (LocalObject container : myPhysicalConnections){
            LocalEdge le = new LocalEdge(container,null);

            for (LocalNode myNode : myLocalNodes){
                
                if (((Long)container.getAttribute("aSide")).equals(myNode.getObject().getOid())){ //NOI18N
                    le.setaSide(myNode);
                    break;
                }else{
                    if (((Long)container.getAttribute("bSide")).equals(myNode.getObject().getOid())){ //NOI18N
                       le.setbSide(myNode);
                        break;
                    }
                }
            }
            myLocalEdges.add(le);
        }
        myView = new LocalObjectView(myLocalNodes.toArray(new LocalNode[0]), myLocalNodes.toArray(new LocalEdge[0]),new LocalLabel[0]);
        buildView();
    }

}
