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
package org.inventory.models.physicalconnections;

import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.models.physicalconnections.scene.ObjectBoxWidget;
import org.inventory.models.physicalconnections.scene.PhysicalPathScene;
import org.inventory.models.physicalconnections.scene.SimpleObjectConnectionWidget;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.widget.Widget;

/**
 * Service class for this module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PhysicalConnectionsService {

    public static PhysicalPathScene buildPhysicalPathView (LocalObjectLight[] trace) {
        CommunicationsStub com = CommunicationsStub.getInstance();
        PhysicalPathScene scene = new PhysicalPathScene();
        ObjectBoxWidget lastPortWidget = null;
        SimpleObjectConnectionWidget lastConnectionWidget = null;
        for (LocalObjectLight element : trace){
            if (com.getMetaForClass(element.getClassName(), false).
                    getValidator(Constants.VALIDATOR_PHYSICAL_LINK) != 1){
                LocalObjectLight[] ancestors = com.getParents(element.getClassName(), element.getOid());
                
                lastPortWidget = (ObjectBoxWidget)scene.addNode(element);
                if (lastConnectionWidget != null)
                    lastConnectionWidget.setTargetAnchor(AnchorFactory.createCenterAnchor(lastPortWidget));
                lastConnectionWidget = null;
                Widget lastWidget = lastPortWidget;
                
                for (int i = 0 ; i < ancestors.length - 1; i++) { //We ignore the dummy root
                    Widget possibleParent = scene.findWidget(ancestors[i]);
                    if (possibleParent == null){
                        Widget node = scene.addNode(ancestors[i]);
                        ((ObjectBoxWidget)node).addBox(lastWidget);
                        lastWidget = node;
                        scene.validate();
                    }else{
                        ((ObjectBoxWidget)possibleParent).addBox(lastWidget);
                        break;
                    }
                    if (com.getMetaForClass(ancestors[i].getClassName(), false).
                            getValidator(Constants.VALIDATOR_PHYSICAL_NODE) == 1){ //Only parents up to the first physical node (say a building) will be displayed
                        scene.addRootWidget(lastWidget);
                        break;
                    }
                }
            }else{
                lastConnectionWidget = (SimpleObjectConnectionWidget)scene.addEdge(element);
                if (lastPortWidget != null)
                    lastConnectionWidget.setSourceAnchor(AnchorFactory.createCenterAnchor(lastPortWidget));
                lastPortWidget = null;
            }
        }
        scene.revalidate();
        return scene;
    }
}
