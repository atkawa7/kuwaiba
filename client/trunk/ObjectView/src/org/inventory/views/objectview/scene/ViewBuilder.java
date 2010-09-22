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

import java.util.List;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalNode;
import org.inventory.core.services.interfaces.LocalObjectLight;

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
        if (localView != null && _scene != null){
            this.myView = localView;
            this.scene = _scene;
        }
        else
            throw new NullPointerException("A null LocalView/ViewScene is not supported for this constructor");
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
    }

    /**
     * Builds a simple default view using the object's children and putting them one after another
     * @param myChildren
     */
    public static void buildDefaultView(List<LocalObjectLight> myChildren, ViewScene scene) {
        int lastX = 0;
        for (LocalObjectLight child : myChildren){
            //Puts an element after another
            LocalNode ln = new LocalNode(child, lastX, 0);
            ObjectNodeWidget widget = new ObjectNodeWidget(scene, ln);
            widget.setPreferredLocation(ln.getPosition());
            scene.getNodesLayer().addChild(widget);
            lastX +=100;
        }
    }

}
