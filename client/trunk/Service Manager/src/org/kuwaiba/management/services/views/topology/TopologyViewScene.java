/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.management.services.views.topology;

import java.awt.Point;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectLightList;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * This scene renders a view where the communications equipment associated directly to a service and the physical connections between them are displayed in a topology fashion
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TopologyViewScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
    private CommunicationsStub com = CommunicationsStub.getInstance();
    /**
     * Default move widget action (shared by all connection widgets)
     */
    private CustomMoveAction moveAction = new CustomMoveAction(this);
    /**
     * Default control point move action (shared by all connection widgets)
     */
    private CustomMoveControlPointAction moveControlPointAction =
            new CustomMoveControlPointAction(this);
    /**
     * Default add/remove control point action (shared by all connection widgets)
     */
    private CustomAddRemoveControlPointAction addRemoveControlPointAction =
            new CustomAddRemoveControlPointAction(this);

    public TopologyViewScene() {
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        
        addChild(edgeLayer);
        addChild(nodeLayer);
        
        initSelectionListener();
    }
    
    @Override
    public byte[] getAsXML() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        throw new IllegalArgumentException("This view needs a service to be rendered");
    }

    @Override
    public void render(LocalObjectLight service) {
        List<LocalObjectLight> serviceResources = com.getServiceResources(service.getClassName(), service.getOid());
        if (serviceResources == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            //We will ignore all resources that are not either GenericCommunicationsEquipment
            for (LocalObjectLight serviceResource : serviceResources) {
                if (com.isSubclassOf(serviceResource.getClassName(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT))
                    addNode(serviceResource);
            }
            //Once the nodes have been added, we retrieve the physical connections between them and ignore those that end in other elements
            for (LocalObjectLight aNode : getNodes()) {
                List<LocalObjectLightList> physicalConnections = com.getPhysicalConnectionsInObject(aNode.getClassName(), aNode.getOid());
                    if (physicalConnections == null) 
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    else {
                        for (LocalObjectLightList aConnection : physicalConnections) {
                            LocalObjectLight sourcePort = aConnection.get(0);
                            LocalObjectLight targetPort = aConnection.get(aConnection.size() - 1);
                            
                            List<LocalObjectLight> parentsUntilFirstOfClass = com.getParentsUntilFirstOfClass(targetPort.getClassName(), targetPort.getOid(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                            if (parentsUntilFirstOfClass == null)
                                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                            else {
                                LocalObjectLight sourceEquipment = aNode;
                                LocalObjectLight targetEquipment = parentsUntilFirstOfClass.get(parentsUntilFirstOfClass.size() - 1);
                                
                                if (findWidget(targetEquipment) != null) {
                                    if (findWidget(aConnection.get(1)) == null) {
                                        ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget)addEdge(aConnection.get(1));
                                        setEdgeSource(aConnection.get(1), sourceEquipment);
                                        setEdgeTarget(aConnection.get(1), targetEquipment);
                                        connectionWidget.getLabelWidget().setLabel(sourceEquipment.getName() + ":" + sourcePort.getName() + 
                                                " ** " +targetEquipment.getName() + ":" + targetPort.getName());
                                    }
                                } //Else, we just ignore this connection trace
                            }
                        }
                    }
            }
        }
    }

    @Override
    public ConnectProvider getConnectProvider() { return null; }

    @Override
    public boolean supportsConnections() { return true; }

    @Override
    public boolean supportsBackgrounds() { return false; }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
        Widget newWidget;
        if (classMetadata != null)
            newWidget = new ObjectNodeWidget(this, node, classMetadata.getIcon());
        else //Should not happen
            newWidget = new ObjectNodeWidget(this, node);

        newWidget.getActions().addAction(createSelectAction());
        newWidget.getActions().addAction(moveAction);
        newWidget.setPreferredLocation(new Point(nodeLayer.getChildren().size() * 200, (nodeLayer.getChildren().size() % 2) * 200 ));
        nodeLayer.addChild(newWidget);
        
        validate();
        return newWidget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge);
        newWidget.getActions().addAction(createSelectAction());
        newWidget.getActions().addAction(moveControlPointAction);
        newWidget.getActions().addAction(addRemoveControlPointAction);
        newWidget.setRouter(RouterFactory.createFreeRouter());
        newWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
        if (classMetadata != null)
            newWidget.setLineColor(classMetadata.getColor());
        
        edgeLayer.addChild(newWidget);
        validate();
        return newWidget;
    }
}
