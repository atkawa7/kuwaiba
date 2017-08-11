/*
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
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalLogicalConnectionDetails;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Shows an end-to-end view of a service by trying to match the endpoints of the logical circuits
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EndToEndViewScene extends AbstractScene<LocalObjectLight, LocalObjectLight>{
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    public EndToEndViewScene() {
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        
        addChild(edgeLayer);
        addChild(nodeLayer);
        
        getActions().addAction(ActionFactory.createZoomAction());
        initSelectionListener();
    }
    
    @Override
    public byte[] getAsXML() { return null; }

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
                        
                        LocalObjectLight aSideEquipmentLogical = parentsUntilFirstComEquipmentA.get(parentsUntilFirstComEquipmentA.size() - 1);
                        
                        addEquipmentPortPair(aSideEquipmentLogical, logicalCircuitDetails.getEndpointA());
                        
                        //Now the other side
                        List<LocalObjectLight> parentsUntilFirstComEquipmentB = com.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                getClassName(), logicalCircuitDetails.getEndpointB().getOid(), "GenericCommunicationsElement");
                        
                        LocalObjectLight bSideEquipmentLogical = parentsUntilFirstComEquipmentB.get(parentsUntilFirstComEquipmentB.size() - 1);
                        
                        addEquipmentPortPair(bSideEquipmentLogical, logicalCircuitDetails.getEndpointB());
                        
                        
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
                            
                            LocalObjectLight aSideEquipmentPhysical = parentsUntilFirstNextComEquipmentA.get(parentsUntilFirstNextComEquipmentA.size() - 1);
                            addEquipmentPortPair(aSideEquipmentPhysical, nextPhysicalHop);
                            
                            if (findWidget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1)) == null) { 
                                addEdge(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1));
                                setEdgeSource(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1), logicalCircuitDetails.getEndpointA());
                                setEdgeTarget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(1), nextPhysicalHop);
                            }
                        }
                        
                        //Now the b side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) {
                            LocalObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointB().get(logicalCircuitDetails.getPhysicalPathForEndpointB().size() - 1);
                            List<LocalObjectLight> parentsUntilFirstNextComEquipmentB = com.getParentsUntilFirstOfClass(nextPhysicalHop.
                                getClassName(), nextPhysicalHop.getOid(), "GenericCommunicationsElement");
                        
                            LocalObjectLight bSideEquipmentPhysical = parentsUntilFirstNextComEquipmentB.get(parentsUntilFirstNextComEquipmentB.size() - 1);
                            addEquipmentPortPair(bSideEquipmentPhysical, nextPhysicalHop);
                            
                            if (findWidget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1)) == null) { 
                                addEdge(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1));
                                setEdgeSource(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1), logicalCircuitDetails.getEndpointB());
                                setEdgeTarget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(1), bSideEquipmentPhysical);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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
        Widget newWidget;
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
        if (classMetadata != null)
            newWidget = new ObjectBoxWidget(this, node, classMetadata.getColor());
        else
            newWidget = new ObjectNodeWidget(this, node, new Color((int)(Math.random() * 0x1000000)));
        
        newWidget.getActions().addAction(createSelectAction());
        
        newWidget.setPreferredLocation(new Point(nodeLayer.getChildren().size() * 200, (nodeLayer.getChildren().size() % 2) * 200 ));
        
        validate();
        return newWidget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge);
        newWidget.getActions().addAction(createSelectAction());
        newWidget.getActions().addAction(ActionFactory.createAddRemoveControlPointAction());
        newWidget.getActions().addAction(ActionFactory.createMoveControlPointAction(ActionFactory.createFreeMoveControlPointProvider()));
        newWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
        if (classMetadata != null)
            newWidget.setLineColor(classMetadata.getColor());
        
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
    
    public void addEquipmentPortPair(LocalObjectLight equipment, LocalObjectLight port) {
        Widget newEquipmentWidget = findWidget(equipment);
        if (newEquipmentWidget == null) {
            newEquipmentWidget = addNode(equipment);
            nodeLayer.addChild(newEquipmentWidget);
            newEquipmentWidget.getActions().addAction(ActionFactory.createMoveAction());
        } 

        Widget newPortWidget = findWidget(port);
        if (newPortWidget == null) {
            newPortWidget = addNode(port);
            newEquipmentWidget.addChild(newPortWidget);
        }
    }
}
