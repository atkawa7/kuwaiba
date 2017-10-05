/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.customization.classhierarchy.scene;

import java.awt.event.ActionEvent;
import java.util.List;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.actions.providers.CustomSelectProvider;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.SelectableVMDNodeWidget;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataNode;
import org.inventory.customization.classhierarchy.scene.menus.ClassMetadataWidgetMenu;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.layout.GraphLayout;
import org.netbeans.api.visual.graph.layout.GraphLayoutFactory;
import org.netbeans.api.visual.graph.layout.GraphLayoutSupport;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.layout.SceneLayout;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.vmd.VMDNodeWidget;
import org.netbeans.api.visual.vmd.VMDPinWidget;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Scene used for the Class Hierarchy Top Component
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ClassHierarchyScene extends AbstractScene<LocalClassMetadata, String> {
    public static final String COMMAND_EXPAND = "expandClassHierarchy";
    private SceneLayout sceneLayout;
    private final WidgetAction selectAction;
        
    public ClassHierarchyScene() {
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
                
        addChild(nodeLayer);
        addChild(edgeLayer);
        
        selectAction = ActionFactory.createSelectAction(new CustomSelectProvider(this), true);
        
        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createPanAction());
    }
        
    public void createSubHierarchyRecursively(LocalClassMetadata rootClass, List<LocalClassMetadata> subclasses, boolean recursive) {
        for (LocalClassMetadata subclass : subclasses) {
            if (findWidget(subclass) == null) {
                VMDNodeWidget nodeWidget = (SelectableVMDNodeWidget) addNode(subclass);
                nodeWidget.collapseWidget();
                
                int length = subclass.getAttributesNames().length;
                for (int i = 0; i < length; i += 1) {
                    String attributeName = subclass.getAttributesNames()[i];
                    String attributeType = subclass.getAttributesTypes()[i];
                    
                    String attributePin = String.format("%s [%s]", attributeName, attributeType);
                    
                    VMDPinWidget pinWidget = new VMDPinWidget(this);
                    pinWidget.setPinName(attributePin);
                    
                    nodeWidget.attachPinWidget(pinWidget);
                }
                String className = subclass.getClassName();
                
                String edge = String.format("%s " + I18N.gm("subclass_of") + " %s", className, rootClass.getClassName());
                
                addEdge(edge);
                setEdgeSource(edge, rootClass);
                setEdgeTarget(edge, subclass);
            }
            if (recursive)
                fireChangeEvent(new ActionEvent(subclass, AbstractScene.SCENE_CHANGE, ClassHierarchyScene.COMMAND_EXPAND));
        }
    }
    
    @Override
    public byte[] getAsXML() {
        return null;
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
    }

    @Override
    public ConnectProvider getConnectProvider() {
        return null;
    }

    @Override
    public boolean supportsConnections() {
        return true;
    }

    @Override
    public boolean supportsBackgrounds() {
        return false;
    }

    @Override
    protected Widget attachNodeWidget(LocalClassMetadata node) {
        VMDNodeWidget nodeWidget = new SelectableVMDNodeWidget(this, new ClassMetadataNode(node));
        
        nodeWidget.setNodeName(node.isAbstract() ?  node.getClassName() + " [" +I18N.gm("abstract")+ "]" : node.getClassName());
        nodeWidget.getActions().addAction(selectAction);
        nodeWidget.getActions().addAction(ActionFactory.createMoveAction());
        nodeWidget.getActions().addAction(ActionFactory.createPopupMenuAction(ClassMetadataWidgetMenu.getInstance()));
        nodeLayer.addChild(nodeWidget);
        
        return nodeWidget;
    }

    @Override
    protected Widget attachEdgeWidget(String edge) {
        ConnectionWidget connectionWidget = new ConnectionWidget(this);
        
        connectionWidget.setEndPointShape(PointShape.SQUARE_FILLED_SMALL);
        connectionWidget.setSourceAnchorShape(AnchorShape.TRIANGLE_HOLLOW);
        connectionWidget.setControlPointShape(PointShape.SQUARE_FILLED_SMALL);
        connectionWidget.setRouter(RouterFactory.createFreeRouter());
        connectionWidget.getActions ().addAction (createObjectHoverAction ());
        connectionWidget.getActions ().addAction (createSelectAction ());
        connectionWidget.setToolTipText(edge);
        edgeLayer.addChild(connectionWidget);
        return connectionWidget;
    }

    @Override
    protected void attachEdgeSourceAnchor(String edge, LocalClassMetadata oldSourceNode, LocalClassMetadata sourceNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget) findWidget(edge);
        Widget sourceWidget = findWidget(sourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createDirectionalAnchor(sourceWidget, AnchorFactory.DirectionalAnchorKind.VERTICAL) : null);
    }

    @Override
    protected void attachEdgeTargetAnchor(String edge, LocalClassMetadata oldTargetNode, LocalClassMetadata targetNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget) findWidget(edge);
        Widget targetWidget = findWidget(targetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createDirectionalAnchor(targetWidget, AnchorFactory.DirectionalAnchorKind.VERTICAL) : null);
    }
        
    public void reorganizeNodes() {
        sceneLayout.invokeLayoutImmediately();
        repaint();
    }
    
    @Override
    public void render(LocalClassMetadata root) {
        // Fix Scene Layout
        int originX = 150; ///TODO: to improve the value of orginX
        int originY = 50; ///TODO: to improve the value of orginX
        GraphLayout<LocalClassMetadata, String> layout = GraphLayoutFactory.createTreeGraphLayout(originX, originY, 100, 50, true, true);
        GraphLayoutSupport.setTreeGraphLayoutRootNode(layout, root);
        sceneLayout = LayoutFactory.createSceneGraphLayout(this, layout);
        // Adding Root Node Class       
        VMDNodeWidget nodeWidget = (VMDNodeWidget) addNode(root);
        nodeWidget.collapseWidget();
        
        VMDPinWidget pinWidget = new VMDPinWidget(this);
        nodeWidget.attachPinWidget(pinWidget);
        
        int length = root.getAttributesNames().length;
        for (int i = 0; i < length; i += 1) {
            String attributeName = root.getAttributesNames()[i];
            String attributeType = root.getAttributesTypes()[i];
            
            String attributePin = String.format("%s [%s]", attributeName, attributeType);
            
            pinWidget.setPinName(attributePin);
        }        
        validate();
        sceneLayout.invokeLayout();
        repaint();
    }
}
