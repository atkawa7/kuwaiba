/*
 * Copyright (c) 2016 gir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    gir - initial API and implementation and/or initial documentation
 */
package com.neotropic.inventory.modules.sdh.scene;

import com.neotropic.inventory.modules.sdh.wizard.SDHConnectionWizard;
import java.awt.BasicStroke;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.visual.actions.providers.AcceptActionProvider;
import org.inventory.core.visual.actions.providers.SceneConnectProvider;
import org.inventory.core.visual.export.ExportableScene;
import org.inventory.core.visual.export.Layer;
import org.inventory.core.visual.scene.AbstractConnectionWidget;
import org.inventory.core.visual.scene.AbstractNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * This is the scene used in the SDH Module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */

public class SDHModuleScene extends GraphScene<LocalObjectLight, LocalObjectLight> implements ExportableScene {

    /**
     * String for Selection tool
     */
    public final static String ACTION_SELECT = "selection"; //NOI18
    /**
     * String for Connect tool
     */
    public final static String ACTION_CONNECT = "connect"; //NOI18
    /**
     * Version of the XML format used to store this view (see getAsXML method)
     */
    private final static String FORMAT_VERSION = "1.0";
    /**
     * Layer to contain the nodes (network equipment, mostly)
     */
    private LayerWidget nodeLayer;
    /**
     * Layer to contain the connections (STMXXX)
     */
    private LayerWidget edgeLayer;

    public SDHModuleScene() {
        getActions().addAction(ActionFactory.createAcceptAction(new AcceptActionProvider(this, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT)));

        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        
        addChild(nodeLayer);
        addChild(edgeLayer);
    }

    /**
     * 
     * @param node
     * @return 
     */
    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        Widget newNode = new AbstractNodeWidget(this, node);
        nodeLayer.addChild(newNode);
        newNode.getActions(ACTION_SELECT).addAction(createSelectAction());
        newNode.getActions(ACTION_SELECT).addAction(ActionFactory.createMoveAction());
        newNode.getActions(ACTION_CONNECT).addAction(ActionFactory.createConnectAction(edgeLayer, new SceneConnectProvider(this) {
                    @Override
                    public void createConnection(Widget sourceWidget, Widget targetWidget) {
                        SDHConnectionWizard wizard = new SDHConnectionWizard();
                        LocalObjectLight sourceObject = (LocalObjectLight)findObject(sourceWidget);
                        LocalObjectLight targetObject = (LocalObjectLight)findObject(targetWidget);
                        LocalObjectLight newConnection = wizard.run(sourceObject, targetObject);
                        if (newConnection != null) {
                            AbstractConnectionWidget newConnectionWidget = (AbstractConnectionWidget)addEdge(newConnection);
                            newConnectionWidget.setSourceAnchor(AnchorFactory.createRectangularAnchor(sourceWidget));
                            newConnectionWidget.setTargetAnchor(AnchorFactory.createRectangularAnchor(targetWidget));
                        }
                    }
                }));
        newNode.getActions(ACTION_CONNECT).addAction(createSelectAction());
        return newNode;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        AbstractConnectionWidget newEdge = new AbstractConnectionWidget(this, edge);
        newEdge.getActions().addAction(createSelectAction());
        newEdge.getActions().addAction(ActionFactory.createAddRemoveControlPointAction());
        newEdge.getActions().addAction(ActionFactory.createMoveControlPointAction(ActionFactory.createFreeMoveControlPointProvider()));
        newEdge.setStroke(new BasicStroke(1));
        newEdge.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setRouter(RouterFactory.createFreeRouter());
        edgeLayer.addChild(newEdge);
        return newEdge;
    }

    @Override
    protected void attachEdgeSourceAnchor(LocalObjectLight edge, LocalObjectLight oldSourceNode, LocalObjectLight sourceNode) {
        AbstractConnectionWidget connectionWidget = (AbstractConnectionWidget)findWidget(edge);
        Widget sourceWidget = findWidget(sourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createCircularAnchor(sourceWidget, 3) : null);
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObjectLight edge, LocalObjectLight oldTargetNode, LocalObjectLight targetNode) {
        AbstractConnectionWidget connectionWidget = (AbstractConnectionWidget)findWidget(edge);
        Widget targetWidget = findWidget(targetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createCircularAnchor(targetWidget, 3) : null);
    }

    @Override
    public Scene getExportable() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Layer[] getLayers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void clear(){
        while (!getNodes().isEmpty())
            removeNode(getNodes().iterator().next());
        while (!getEdges().isEmpty())
            removeEdge(getEdges().iterator().next());
        validate();
        
    }
    
    public void showLabels(boolean visible) {
        for (Widget aWidget : nodeLayer.getChildren()) 
            ((AbstractNodeWidget)aWidget).showLabel(visible);
        
        if (getView() != null)
            getView().repaint();
    }
}
