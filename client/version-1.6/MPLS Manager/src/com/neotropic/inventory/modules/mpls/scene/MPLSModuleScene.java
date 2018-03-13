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
package com.neotropic.inventory.modules.mpls.scene;

import com.neotropic.inventory.modules.mpls.actions.MPLSModuleActions;
import com.neotropic.inventory.modules.mpls.wizard.MPLSConnectionWizard;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
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
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * This is the scene used in the MPLS Module
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MPLSModuleScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
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
    private MPLSModuleActions moduleActions;
    
    public MPLSModuleScene() {
        getActions().addAction(ActionFactory.createAcceptAction(new CustomAcceptActionProvider(this, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT)));
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        
        moduleActions = new MPLSModuleActions(this);
        addChild(edgeLayer);
        addChild(nodeLayer);
        moveProvider = new CustomMoveProvider(this);
        selectAction = ActionFactory.createSelectAction(new CustomSelectProvider(this), true);
        addRemoveControlPointAction = new CustomAddRemoveControlPointAction(this);
        moveControlPointAction = new CustomMoveControlPointAction(this);
        
        connectProvider = new MPLSModuleConnectProvider();
        
        setState (ObjectState.createNormal ());
    }

    @Override
    public byte[] getAsXML() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), VIEW_FORMAT_VERSION)); // NOI18N
            
            QName qnameClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters("MPLSModuleView"));
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            for (Widget nodeWidget : nodeLayer.getChildren()) {
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(nodeWidget.getPreferredLocation().x)));
                xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(nodeWidget.getPreferredLocation().y)));
                
                LocalObjectLight nodeObject = (LocalObjectLight) findObject(nodeWidget);
                
                xmlew.add(xmlef.createAttribute(new QName("class"), nodeObject.getClassName()));
                
                xmlew.add(xmlef.createCharacters(Long.toString(nodeObject.getOid())));
                
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                LocalObjectLight edgeObject = (LocalObjectLight) findObject(edgeWidget);
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                xmlew.add(xmlef.createAttribute(new QName("id"), Long.toString(edgeObject.getOid())));
                xmlew.add(xmlef.createAttribute(new QName("class"), edgeObject.getClassName()));
                
                xmlew.add(xmlef.createAttribute(new QName("aside"), Long.toString(getEdgeSource(edgeObject).getOid())));
                xmlew.add(xmlef.createAttribute(new QName("bside"), Long.toString(getEdgeTarget(edgeObject).getOid())));
                
                for (Point point : ((ObjectConnectionWidget)edgeWidget).getControlPoints()) {
                    QName qnameControlpoint = new QName("controlpoint");
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(point.x)));
                    xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(point.y)));
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                }
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            }
            xmlew.add(xmlef.createEndElement(qnameEdges, null));
            
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
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
                        else {
                            fireChangeEvent(new ActionEvent(this, SCENE_CHANGE, "equipmentAutomaticallyRemoved")); //NOI18N
                            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, String.format("Equipment of class %s and id %s could not be found and was removed from the view", objectClass, objectId));
                        }
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

                                if (aSideWidget == null || bSideWidget == null) {
                                    fireChangeEvent(new ActionEvent(this, SCENE_CHANGE, "connectionAutomaticallyRemoved")); //NOI18N
                                    NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, String.format("One or both of the endpoints of the connection of class %s and id %s could not be found. The connection was removed from the view", className, objectId));
                                }
                                else {
                                    ConnectionWidget newEdge = (ObjectConnectionWidget)addEdge(container);
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
                            } else {
                                fireChangeEvent(new ActionEvent(this, SCENE_CHANGE, "connectionAutomaticallyRemoved")); //NOI18N
                                NotificationUtil.getInstance().showSimplePopup("Load view", NotificationUtil.INFO_MESSAGE, String.format("Connection of class %s and id %s could not be found and was removed from the view", className, objectId));
                            }
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
    protected Widget attachNodeWidget(LocalObjectLight node) {        
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
        ObjectNodeWidget newNode;
        if (classMetadata == null) //Should not happen, but this check should always be done
            newNode = new ObjectNodeWidget(this, node);
        else
            newNode = new ObjectNodeWidget(this, node, classMetadata.getIcon());
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
        ObjectConnectionWidget newEdge = new ObjectConnectionWidget(this, edge);
        newEdge.getActions().addAction(selectAction);
        newEdge.getActions().addAction(addRemoveControlPointAction);
        newEdge.getActions().addAction(moveControlPointAction);
        newEdge.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setRouter(RouterFactory.createFreeRouter());
        newEdge.getActions().addAction(ActionFactory.createPopupMenuAction(moduleActions.createMenuForConnection()));
        newEdge.setToolTipText(edge.toString());
        LocalClassMetadata connectionClassMetadata = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
        if (connectionClassMetadata == null || connectionClassMetadata.getColor() == null)
            newEdge.setLineColor(Color.BLACK);
        else
            newEdge.setLineColor(connectionClassMetadata.getColor());

        edgeLayer.addChild(newEdge);
        return newEdge;
    }

    @Override
    public void render(LocalObjectLight root) {
    }
 
    /**
     * Own implementation of a connection provider
     */
    private class MPLSModuleConnectProvider extends SceneConnectProvider {
        @Override
        public void createConnection(Widget sourceWidget, Widget targetWidget) {
            MPLSConnectionWizard wizard = new MPLSConnectionWizard();
            LocalObjectLight sourceObject = (LocalObjectLight)findObject(sourceWidget);
            LocalObjectLight targetObject = (LocalObjectLight)findObject(targetWidget);
            LocalObjectLight newConnection = wizard.run(sourceObject, targetObject);

            if (newConnection != null) {
                //Only create edges in the scene if the connection is a TransportLink
                if (newConnection.getClassName().equals("MPLSLink")) {
                    addEdge(newConnection);
                    setEdgeSource(newConnection, sourceObject);
                    setEdgeTarget(newConnection, targetObject);
                    fireChangeEvent(new ActionEvent(this, SCENE_CHANGE, "attachEdge")); //NOI18N
                }
            }
            validate();
            repaint();
        }
    }
}
