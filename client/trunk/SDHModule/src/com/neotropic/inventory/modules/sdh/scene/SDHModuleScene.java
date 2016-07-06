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

import com.neotropic.inventory.modules.sdh.actions.SDHModuleActions;
import com.neotropic.inventory.modules.sdh.wizard.SDHConnectionWizard;
import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.actions.providers.CustomAcceptActionProvider;
import org.inventory.core.visual.actions.providers.CustomMoveProvider;
import org.inventory.core.visual.actions.providers.CustomSelectProvider;
import org.inventory.core.visual.actions.providers.SceneConnectProvider;
import org.inventory.core.visual.scene.AbstractConnectionWidget;
import org.inventory.core.visual.scene.AbstractNodeWidget;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

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
    /**
     * Custom move provider
     */
    private CustomMoveProvider moveProvider;
    /**
     * Custom add/remove control point action
     */
    private CustomAddRemoveControlPointAction addRemoveControlPointAction;
    /**
     * Custom move control point action
     */
    private CustomMoveControlPointAction moveControlPointAction;
    /**
     * Custom select provider
     */
    private WidgetAction selectAction;
    /**
     * Reference to the action factory used to assign actions to the nodes and connections
     */
    private SDHModuleActions moduleActions;

    public SDHModuleScene() {
        getActions().addAction(ActionFactory.createAcceptAction(new CustomAcceptActionProvider(this, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT)));

        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        
        moduleActions = new SDHModuleActions(this);
        
        addChild(edgeLayer);
        addChild(nodeLayer);
        moveProvider = new CustomMoveProvider(this);
        selectAction = ActionFactory.createSelectAction(new CustomSelectProvider(this), true);
        addRemoveControlPointAction = new CustomAddRemoveControlPointAction(this);
        moveControlPointAction = new CustomMoveControlPointAction(this);
        
        connectProvider = new SDHModuleConnectProvider();
        
        setState (ObjectState.createNormal ());
    }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {        
        Widget newNode = new AbstractNodeWidget(this, node);
        nodeLayer.addChild(newNode);
        newNode.getActions(ACTION_SELECT).addAction(selectAction);
        newNode.getActions(ACTION_SELECT).addAction(ActionFactory.createMoveAction(moveProvider, moveProvider));
        newNode.getActions(ACTION_CONNECT).addAction(selectAction);
        newNode.getActions(ACTION_CONNECT).addAction(ActionFactory.createConnectAction(edgeLayer, connectProvider));
        newNode.getActions().addAction(ActionFactory.createPopupMenuAction(moduleActions.createMenuForNode()));
        return newNode;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        AbstractConnectionWidget newEdge = new AbstractConnectionWidget(this, edge);
        newEdge.getActions().addAction(selectAction);
        newEdge.getActions().addAction(addRemoveControlPointAction);
        newEdge.getActions().addAction(moveControlPointAction);
        newEdge.setStroke(new BasicStroke(1));
        newEdge.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setRouter(RouterFactory.createFreeRouter());
        newEdge.getActions().addAction(ActionFactory.createPopupMenuAction(moduleActions.createMenuForConnection()));
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
        //TODO: Get the class name from somewhere else
        mainTag.start("class").text("SDHModuleView").end();
        StartTagWAX nodesTag = mainTag.start("nodes");
        for (Widget nodeWidget : nodeLayer.getChildren())
            nodesTag.start("node").attr("x", nodeWidget.getPreferredLocation().x).
            attr("y", nodeWidget.getPreferredLocation().y).
            attr("class", ((AbstractNodeWidget)nodeWidget).getNode().getObject().getClassName()).
            text(String.valueOf(((AbstractNodeWidget)nodeWidget).getNode().getObject().getOid())).end();
        nodesTag.end();

        StartTagWAX edgesTag = mainTag.start("edges");
        for (Widget edgeWidget : edgeLayer.getChildren()) {
            StartTagWAX edgeTag = edgesTag.start("edge");
            edgeTag.attr("id", ((AbstractConnectionWidget)edgeWidget).getNode().getObject().getOid());
            edgeTag.attr("class", ((AbstractConnectionWidget)edgeWidget).getNode().getObject().getClassName());
            
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
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        QName qNode = new QName("node"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qControlPoint = new QName("controlpoint"); //NOI18N
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT){
                    if (reader.getName().equals(qNode)){
                        String objectClass = reader.getAttributeValue(null, "class");

                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        long objectId = Long.valueOf(reader.getElementText());

                        LocalObjectLight lol = CommunicationsStub.getInstance().getObjectInfoLight(objectClass, objectId);
                        if (lol != null){
                            Widget widget = addNode(lol);
                            widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                        }
                        else
                            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, String.format("Equipment of class %s and id %s could not be found and was removed from the view", objectClass, objectId));
                    }else {
                        if (reader.getName().equals(qEdge)){
                            long objectId = Long.valueOf(reader.getAttributeValue(null, "id"));
                            long aSide = Long.valueOf(reader.getAttributeValue(null, "aside"));
                            long bSide = Long.valueOf(reader.getAttributeValue(null, "bside"));

                            String className = reader.getAttributeValue(null,"class");
                            LocalObjectLight container = CommunicationsStub.getInstance().getObjectInfoLight(className, objectId);
                            if (container != null) {
                                LocalObjectLight aSideObject = new LocalObjectLight(aSide, null, null);
                                Widget aSideWidget = findWidget(aSideObject);

                                LocalObjectLight bSideObject = new LocalObjectLight(bSide, null, null);
                                Widget bSideWidget = findWidget(bSideObject);

                                if (aSideWidget == null || bSideWidget == null)
                                    NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, String.format("One or both of the endpoints of connection of class %s and id %s could not be found, so the connection was removed from the view", className, objectId));
                                else {
                                    ConnectionWidget newEdge = (AbstractConnectionWidget)addEdge(container);
                                    newEdge.setLineColor(getConnectionColor(container));
                                    setEdgeSource(container, aSideObject);
                                    setEdgeTarget(container, bSideObject);
                                    List<Point> localControlPoints = new ArrayList<>();
                                    while(true){
                                        reader.nextTag();

                                        if (reader.getName().equals(qControlPoint)){
                                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                                localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                        }else{
                                            newEdge.setControlPoints(localControlPoints,false);
                                            break;
                                        }
                                    }
                                }
                            } else
                                NotificationUtil.getInstance().showSimplePopup("Load view", NotificationUtil.INFO_MESSAGE, String.format("Connection of class %s and id %s could not be found and was removed from the view", className, objectId));
                        }
                    }
                }
            }
            reader.close();
            validate();
            repaint();
        } catch (NumberFormatException | XMLStreamException ex) {
            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.ERROR_MESSAGE, "The view seems corrupted and could not be loaded");
            clear();
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
                Exceptions.printStackTrace(ex);
        }
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
    
    @Override
    public Color getConnectionColor(LocalObjectLight theConnection) {
        
        LocalClassMetadata connectionClassMetadata = CommunicationsStub.getInstance().getMetaForClass(theConnection.getClassName(), false);
        if (connectionClassMetadata == null)
            return Color.BLACK;
        
        return connectionClassMetadata.getColor() == null ? Color.BLACK : connectionClassMetadata.getColor();
    }
    
    /**
     * Own implementation of a connection provider
     */
    private class SDHModuleConnectProvider extends SceneConnectProvider {
        @Override
        public void createConnection(Widget sourceWidget, Widget targetWidget) {
            SDHConnectionWizard wizard = new SDHConnectionWizard();
            LocalObjectLight sourceObject = (LocalObjectLight)findObject(sourceWidget);
            LocalObjectLight targetObject = (LocalObjectLight)findObject(targetWidget);
            LocalObjectLight newConnection = wizard.run(sourceObject, targetObject);

            if (newConnection != null) {
                //Only create edges in the scene if the connection is a TransportLink
                if (CommunicationsStub.getInstance().isSubclassOf(newConnection.getClassName(), "GenericSDHTransportLink")) {
                    AbstractConnectionWidget newConnectionWidget = (AbstractConnectionWidget)addEdge(newConnection);
                    setEdgeSource(newConnection, sourceObject);
                    setEdgeTarget(newConnection, targetObject);
                    newConnectionWidget.setLineColor(getConnectionColor(newConnection));
                    fireChangeEvent(new ActionEvent(this, SCENE_CHANGE, "attachEdge")); //NOI18N
                }
            }
            validate();
            repaint();
        }
    }
}
