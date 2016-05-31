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
import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.awt.BasicStroke;
import java.awt.Point;
import java.io.ByteArrayOutputStream;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.visual.actions.providers.AcceptActionProvider;
import org.inventory.core.visual.actions.providers.SceneConnectProvider;
import org.inventory.core.visual.scene.AbstractConnectionWidget;
import org.inventory.core.visual.scene.AbstractNodeWidget;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * This is the scene used in the SDH Module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */

public class SDHModuleScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {

    /**
     * Version of the XML format used to store this view (see getAsXML method)
     */
    private final static String VIEW_FORMAT_VERSION = "1.0";
    /**
     * Connect provider
     */
    private SceneConnectProvider connectProvider;

    public SDHModuleScene() {
        getActions().addAction(ActionFactory.createAcceptAction(new AcceptActionProvider(this, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT)));

        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        
        addChild(edgeLayer);
        addChild(nodeLayer);
        
        connectProvider = new SceneConnectProvider(this) {
                    @Override
                    public void createConnection(Widget sourceWidget, Widget targetWidget) {
                        SDHConnectionWizard wizard = new SDHConnectionWizard();
                        LocalObjectLight sourceObject = (LocalObjectLight)findObject(sourceWidget);
                        LocalObjectLight targetObject = (LocalObjectLight)findObject(targetWidget);
                        LocalObjectLight newConnection = wizard.run(sourceObject, targetObject);
                        if (newConnection != null) {
                            AbstractConnectionWidget newConnectionWidget = (AbstractConnectionWidget)addEdge(newConnection);
                            newConnectionWidget.setSourceAnchor(AnchorFactory.createCircularAnchor(sourceWidget, 5));
                            newConnectionWidget.setTargetAnchor(AnchorFactory.createCircularAnchor(targetWidget, 5));
                        }
                    }
                };
    }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {        
        Widget newNode = new AbstractNodeWidget(this, node);
        nodeLayer.addChild(newNode);
        newNode.getActions(ACTION_SELECT).addAction(createSelectAction());
        newNode.getActions(ACTION_SELECT).addAction(ActionFactory.createMoveAction());
        newNode.getActions(ACTION_CONNECT).addAction(ActionFactory.createConnectAction(edgeLayer, connectProvider));
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

    public void showLabels(boolean visible) {
        for (Widget aWidget : nodeLayer.getChildren()) 
            ((AbstractNodeWidget)aWidget).showLabel(visible);
        
        if (getView() != null)
            getView().repaint();
    }

    @Override
    public byte[] getAsXML() {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        WAX xmlWriter = new WAX(bas);
        StartTagWAX mainTag = xmlWriter.start("view");
        mainTag.attr("version", VIEW_FORMAT_VERSION); //NOI18N
        //TODO: Get the class name from some else
        mainTag.start("class").text("SDHView").end();
        StartTagWAX nodesTag = mainTag.start("nodes");
        for (Widget nodeWidget : nodeLayer.getChildren())
            nodesTag.start("node").attr("x", nodeWidget.getPreferredLocation().x).
            attr("y", nodeWidget.getPreferredLocation().y).
            attr("class", ((AbstractNodeWidget)nodeWidget).getObject().getClassName()).
            text(String.valueOf(((AbstractNodeWidget)nodeWidget).getObject().getOid()) ).end();
        nodesTag.end();

        StartTagWAX edgesTag = mainTag.start("edges");
        for (Widget edgeWidget : edgeLayer.getChildren()) {
            StartTagWAX edgeTag = edgesTag.start("edge");
            edgeTag.attr("id", ((AbstractConnectionWidget)edgeWidget).getObject().getOid());
            edgeTag.attr("class", ((AbstractConnectionWidget)edgeWidget).getObject().getClassName());
            
            LocalObjectLight edgeObject = (LocalObjectLight)findObject(edgeWidget);
            
            edgeTag.attr("aside", getEdgeSource(edgeObject).getOid());
            edgeTag.attr("bside", getEdgeTarget(edgeObject).getOid());

            for (Point point : ((AbstractConnectionWidget)edgeWidget).getControlPoints())
                edgeTag.start("controlpoint").attr("x", point.x).attr("y", point.y).end();
            edgeTag.end();
        }
        edgesTag.end();
        mainTag.end().close();
        return bas.toByteArray();
    }
    
    @Override
    public void render(byte[] structure) throws IllegalArgumentException {

    }

    @Override
    public ConnectProvider getConnectProvider() {
        return connectProvider;
    }

    @Override
    public boolean supportsConnections() {
        return true;
    }

    @Override
    public boolean supportsBackgrounds() {
        return false;
    }
}
