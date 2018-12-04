/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.models.physicalconnections.scene.ObjectBoxWidget;
import org.inventory.models.physicalconnections.scene.PhysicalPathScene;
import org.inventory.models.physicalconnections.scene.SimpleConnectionWidget;
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
        SimpleConnectionWidget lastConnectionWidget = null;
        for (LocalObjectLight element : trace){
            if (!com.isSubclassOf(element.getClassName(), Constants.CLASS_GENERICPHYSICALLINK)) { //It's a port
                List<LocalObjectLight> ancestors = com.getParents(element.getClassName(), element.getId());
                if(scene.findWidget(element) == null){//we should search if the physical parent port its already in the scene
                    lastPortWidget = (ObjectBoxWidget)scene.addNode(element);
                    if (lastConnectionWidget != null)
                        lastConnectionWidget.setTargetAnchor(AnchorFactory.createCenterAnchor(lastPortWidget));
                    lastConnectionWidget = null;
                    Widget lastWidget = lastPortWidget;

                    for (int i = 0 ; i < ancestors.size() - 1; i++) { //We ignore the dummy root
                        Widget possibleParent = scene.findWidget(ancestors.get(i));
                        if (possibleParent == null){
                            Widget node = scene.addNode(ancestors.get(i));
                            ((ObjectBoxWidget)node).addBox(lastWidget);
                            lastWidget = node;
                        }else{
                            ((ObjectBoxWidget)possibleParent).addBox(lastWidget);
                            break;
                        }
                        if (com.isSubclassOf(ancestors.get(i).getClassName(), Constants.CLASS_GENERICPHYSICALNODE) || //Only parents up to the first physical node (say a building) will be displayed
                                                i == ancestors.size() - 2){ //Or if the next level is the dummy root
                            scene.addRootWidget(lastWidget);
                            scene.validate();
                            break;
                        }
                    }
                }
            }else{
                lastConnectionWidget = (SimpleConnectionWidget)scene.addEdge(element);
                if (lastPortWidget != null)
                    lastConnectionWidget.setSourceAnchor(AnchorFactory.createCenterAnchor(lastPortWidget));
                lastPortWidget = null;
            }
        }
        return scene;
    }
}
