/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.management.services.nodes.actions.endtoend;

import java.awt.Color;
import java.awt.Point;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalLogicalConnectionDetails;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Shows an end-to-end view of a service by trying to match the endpoints of the logical circuits
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EndToEndViewScene extends AbstractScene<LocalObjectLight, LocalObjectLight>{
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private Router router;
    
    public EndToEndViewScene() {
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        
        addChild(nodeLayer);
        addChild(edgeLayer);
        
        router = RouterFactory.createOrthogonalSearchRouter(nodeLayer);
        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createPanAction());
    }
    
    @Override
    public byte[] getAsXML() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException { }

    @Override
    public void render(LocalObjectLight selectedService) {
        List<LocalObjectLight> serviceResources = com.getServiceResources(selectedService.getClassName(), selectedService.getOid());
        if (serviceResources == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            try {
                for (LocalObjectLight serviceResource : serviceResources) {
                    if (com.isSubclassOf(serviceResource.getClassName(), "GenericLogicalConnection")) {
                        LocalLogicalConnectionDetails logicalCircuitDetails = com.getLogicalLinkDetails(serviceResource.getClassName(), serviceResource.getOid());
                        
                        //Let's create the boxes corresponding to the endpoint A of the logical circuit
                        List<LocalObjectLight> parentsUntilFirstComEquipmentA = com.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getOid(), "GenericCommunicationsElement");
                        
                        Collections.reverse(parentsUntilFirstComEquipmentA);
                        Widget lastParentWidgetA = null;
                        
                        for (LocalObjectLight parentA : parentsUntilFirstComEquipmentA) {
                            Widget aWidget = findWidget(parentA);
                            if (aWidget == null) {
                                aWidget = addNode(parentA);
                                
                                if (lastParentWidgetA != null)
                                    lastParentWidgetA.addChild(aWidget);
                                else {
                                        nodeLayer.addChild(aWidget);
                                        aWidget.getActions().addAction(ActionFactory.createMoveAction());
                                }
                                
                                lastParentWidgetA = aWidget;
                                validate();
                            }
                            else {
                                lastParentWidgetA = aWidget;
                                break;
                            }
                        }
                        
                        Widget aEndpointWidget = findWidget(logicalCircuitDetails.getEndpointA());
                        if (aEndpointWidget == null) {
                            aEndpointWidget = addNode(logicalCircuitDetails.getEndpointA());
                            lastParentWidgetA.addChild(aEndpointWidget);
                            validate();
                        }
                        
                        //Now the other side
                        List<LocalObjectLight> parentsUntilFirstComEquipmentB = com.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                getClassName(), logicalCircuitDetails.getEndpointB().getOid(), "GenericCommunicationsElement");
                        
                        Collections.reverse(parentsUntilFirstComEquipmentB);
                        
                        Widget lastParentWidgetB = null;
                        for (LocalObjectLight parentB : parentsUntilFirstComEquipmentB) {
                            Widget aWidget = findWidget(parentB);
                            if (aWidget == null) {
                                aWidget = addNode(parentB);
                                if (lastParentWidgetB != null)
                                    lastParentWidgetB.addChild(aWidget);
                                else {
                                    nodeLayer.addChild(aWidget);
                                    aWidget.getActions().addAction(ActionFactory.createMoveAction());
                                }
                                
                                lastParentWidgetB = aWidget;
                                validate();
                            }
                            else {
                                lastParentWidgetB = aWidget;
                                break;
                            }
                        }
                        
                        Widget bEndpointWidget = findWidget(logicalCircuitDetails.getEndpointB());
                        if (bEndpointWidget == null) {
                            bEndpointWidget = addNode(logicalCircuitDetails.getEndpointB());
                            lastParentWidgetB.addChild(bEndpointWidget);
                            validate();
                        }
                        
                        
                        
                        //Now the logical link
                        addEdge(logicalCircuitDetails.getConnectionObject());
                        setEdgeSource(logicalCircuitDetails.getConnectionObject(), logicalCircuitDetails.getEndpointA());
                        setEdgeTarget(logicalCircuitDetails.getConnectionObject(), logicalCircuitDetails.getEndpointB());
                        
                        //Now with render the physical part
                        //We start with the A side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) {
                            LocalObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointA().get(logicalCircuitDetails.getPhysicalPathForEndpointA().size() - 1);
                            List<LocalObjectLight> parentsUntilFirstNextComEquipmentA = com.getParentsUntilFirstOfClass(nextPhysicalHop.
                                getClassName(), nextPhysicalHop.getOid(), "GenericCommunicationsElement");
                        
                            Collections.reverse(parentsUntilFirstNextComEquipmentA);

                            Widget lastParentNextWidgetA = null;
                            for (LocalObjectLight parentB : parentsUntilFirstNextComEquipmentA) {
                                Widget aWidget = findWidget(parentB);
                                if (aWidget == null) {
                                    aWidget = addNode(parentB);
                                    if (lastParentNextWidgetA != null)
                                        lastParentNextWidgetA.addChild(aWidget);
                                    else {
                                        nodeLayer.addChild(aWidget);
                                        aWidget.getActions().addAction(ActionFactory.createMoveAction());
                                    }
                                    lastParentNextWidgetA = aWidget;
                                    validate();
                                }
                                else {
                                    lastParentNextWidgetA = aWidget;
                                    break;
                                }
                            }

                            Widget aEndpointNextWidget = findWidget(nextPhysicalHop);
                            if (aEndpointNextWidget == null) {
                                aEndpointNextWidget = addNode(nextPhysicalHop);
                                lastParentNextWidgetA.addChild(aEndpointNextWidget);
                                validate();
                            }
                        }
                        
                        //Now the b side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) {
                            LocalObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointB().get(logicalCircuitDetails.getPhysicalPathForEndpointB().size() - 1);
                            List<LocalObjectLight> parentsUntilFirstNextComEquipmentB = com.getParentsUntilFirstOfClass(nextPhysicalHop.
                                getClassName(), nextPhysicalHop.getOid(), "GenericCommunicationsElement");
                        
                            Collections.reverse(parentsUntilFirstNextComEquipmentB);

                            Widget lastParentNextWidgetB = null;
                            for (LocalObjectLight parentB : parentsUntilFirstNextComEquipmentB) {
                                Widget aWidget = findWidget(parentB);
                                if (aWidget == null) {
                                    aWidget = addNode(parentB);
                                    if (lastParentNextWidgetB != null)
                                        lastParentNextWidgetB.addChild(aWidget);
                                    else {
                                        nodeLayer.addChild(aWidget);
                                        aWidget.getActions().addAction(ActionFactory.createMoveAction());
                                    }
                                    lastParentNextWidgetB = aWidget;
                                    validate();
                                }
                                else {
                                    lastParentNextWidgetB = aWidget;
                                    break;
                                }
                            }

                            Widget bEndpointNextWidget = findWidget(nextPhysicalHop);
                            if (bEndpointNextWidget == null) {
                                bEndpointNextWidget = addNode(nextPhysicalHop);
                                lastParentNextWidgetB.addChild(bEndpointNextWidget);
                                validate();
                            }
                        }
                        
                        //Now the physical links
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) {
                            if (findWidget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1)) == null) { 
                                addEdge(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1));
                                setEdgeSource(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1), logicalCircuitDetails.getEndpointA());
                                setEdgeTarget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1), logicalCircuitDetails.getPhysicalPathForEndpointA().get(logicalCircuitDetails.getPhysicalPathForEndpointA().size() - 1));
                            }
                        }
                        
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) {
                            if (findWidget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1)) == null) { 
                                addEdge(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1));
                                setEdgeSource(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1), logicalCircuitDetails.getEndpointB());
                                setEdgeTarget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1), logicalCircuitDetails.getPhysicalPathForEndpointB().get(logicalCircuitDetails.getPhysicalPathForEndpointB().size() - 1));
                            }
                        }
                        
                    }
                }
            } catch (Exception ex) {
                clear();
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            }
        }
    }

    @Override
    public Color getConnectionColor(LocalObjectLight theConnection) { return Color.BLACK; }

    @Override
    public ConnectProvider getConnectProvider() { return null; }

    @Override
    public boolean supportsConnections() { return true; }

    @Override
    public boolean supportsBackgrounds() { return false; }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {      
        Widget newWidget = new ObjectBoxWidget(this, node, new Color((int)(Math.random() * 0x1000000)));
        newWidget.setPreferredLocation(new Point(nodeLayer.getChildren().size() * 500, 0));
        return newWidget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge);
        newWidget.setRouter(router);
        edgeLayer.addChild(newWidget);
        validate();
        return newWidget;
    }

    @Override
    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight newSourceNode) {
        ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget)findWidget(edge);
        Widget sourceWidget = findWidget(newSourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createCircularAnchor(sourceWidget, 3) : null);
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight newTargetNode) {
        ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget)findWidget(edge);
        Widget targetWidget = findWidget(newTargetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createCircularAnchor(targetWidget, 3) : null);
    }

}
