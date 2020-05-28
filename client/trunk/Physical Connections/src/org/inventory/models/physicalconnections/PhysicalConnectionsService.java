/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import java.util.HashMap;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.models.physicalconnections.scene.ObjectBoxWidget;
import org.inventory.models.physicalconnections.scene.PhysicalPathScene;
import org.inventory.models.physicalconnections.scene.PhysicalTreeScene;
import org.inventory.models.physicalconnections.scene.SimpleConnectionWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.widget.Widget;

/**
 * Service class for this module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
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
        SceneLayout sceneLayout = LayoutFactory.createDevolveWidgetLayout(scene.getNodeLayer(), 
            LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 50), false);
        
        sceneLayout.invokeLayout();
        return scene;
    }
    
    public static PhysicalTreeScene buildPhysicalPathView(HashMap<LocalObjectLight, List<LocalObjectLight>> tree) {
        PhysicalTreeScene scene = buildPhysicalTreeView(tree.keySet().toArray(new LocalObjectLight[0]));
        return scene;
    }
    
    private static PhysicalTreeScene buildPhysicalTreeView(LocalObjectLight[] trace) {
        CommunicationsStub com = CommunicationsStub.getInstance();
        PhysicalTreeScene scene = new PhysicalTreeScene();
        
        ObjectBoxWidget lastPortWidget = null;
        SimpleConnectionWidget lastConnectionWidget = null;
        LocalObjectLight lastConnection = null;
        LocalObjectLight lastPort = null;
        for (LocalObjectLight element : trace){
            if (!com.isSubclassOf(element.getClassName(), Constants.CLASS_GENERICPHYSICALLINK)) { //It's a port
                List<LocalObjectLight> ancestors = com.getParents(element.getClassName(), element.getId());
                if(scene.findWidget(element) == null){//we should search if the physical parent port its already in the scene
                    lastPortWidget = (ObjectBoxWidget)scene.addNode(element);
                    lastPort = element;
                    if (lastConnectionWidget != null) {
                        List<LocalObjectLight> endpointsB = CommunicationsStub.getInstance().getSpecialAttribute(lastConnection.getClassName(), lastConnection.getId(), "endpointB");
                        for (LocalObjectLight endpointB : endpointsB) {
                            if (endpointB.getId().equals(lastPort.getId())) {
                                lastConnectionWidget.setTargetAnchor(AnchorFactory.createCenterAnchor(lastPortWidget));
                                break;
                            }
                        }
                    }
                    lastConnectionWidget = null;
                    lastConnection = null;
                    lastPortWidget.setBorder(BorderFactory.createLineBorder());
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
                            lastWidget.getActions().addAction(ActionFactory.createMoveAction());
                            scene.addRootWidget(lastWidget);
                            scene.validate();
                            break;
                        }
                    }
                }
            }else{
                if (scene.findWidget(element) == null) {
                    lastConnectionWidget = (SimpleConnectionWidget)scene.addEdge(element);
                    lastConnection = element;
                    if (lastPortWidget != null) {
                        List<LocalObjectLight> endpointsA = CommunicationsStub.getInstance().getSpecialAttribute(lastConnection.getClassName(), lastConnection.getId(), "endpointA");
                        for (LocalObjectLight endpointA : endpointsA) {
                            if (endpointA.getId().equals(lastPort.getId())) {
                                lastConnectionWidget.setSourceAnchor(AnchorFactory.createCenterAnchor(lastPortWidget));
                                break;
                            }
                        }
                    }
                    lastPortWidget = null;
                    lastPort = null;
                }
            }
        }
        SceneLayout sceneLayout = LayoutFactory.createDevolveWidgetLayout(scene.getNodeLayer(), 
            LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 50), false);
        
        sceneLayout.invokeLayout();
        
        return scene;
    }
}
