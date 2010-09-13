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

import java.awt.Image;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.widget.general.IconNodeWidget;
import org.openide.util.ImageUtilities;

/**
 * This widget represents a node (as in the navigation tree)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectNodeWidget extends IconNodeWidget{
    private LocalObjectLight object;

    public ObjectNodeWidget(ViewScene scene, LocalObjectLight _object){
        super(scene);
        this.object = _object;
        setLabel(_object.getDisplayname());
        Image myIcon = CommunicationsStub.getInstance().getMetaForClass(_object.getClassName(), false).getIcon();
        if(myIcon == null)
            myIcon = ImageUtilities.loadImage("org/inventory/views/viewrenderer/res/default_32.png");
        setImage(myIcon);
        createActions(ViewScene.ACTION_SELECT).addAction(ActionFactory.createAlignWithMoveAction(scene.getNodesLayer(), scene.getInteractionLayer(), null));
        createActions(ViewScene.ACTION_CONNECT).addAction(ActionFactory.createConnectAction(scene.getEdgesLayer(), new PhysicalConnectionProvider()));
    }

    /**
     * Returns the wrapped business object
     * @return
     */
    public LocalObjectLight getObject(){
        return this.object;
    }
}
