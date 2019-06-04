/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import com.neotropic.inventory.modules.mpls.providers.MPLSSceneAcceptProvider;
import com.neotropic.inventory.modules.mpls.windows.EditMPLSLinkEnpointsFrame;
import com.neotropic.inventory.modules.mpls.wizard.MPLSConnectionWizard;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
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
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.actions.providers.CustomAcceptActionProvider;
import org.inventory.core.visual.actions.providers.CustomMoveProvider;
import org.inventory.core.visual.actions.providers.CustomSelectProvider;
import org.inventory.core.visual.actions.providers.SceneConnectProvider;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.inventory.core.visual.scene.AbstractScene;
import static org.inventory.core.visual.scene.AbstractScene.SCENE_CHANGE;
import org.inventory.core.visual.scene.EmptyNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
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
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MPLSModuleScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
    /**
     * Version of the XML format used to store this view (see getAsXML method)
     */
    private final static String VIEW_FORMAT_VERSION = "1.0";
    /**
     * Connect provider
     */
    private final SceneConnectProvider connectProvider;
    /**
     * Custom move provider
     */
    private final CustomMoveProvider moveProvider;
    /**
     * Custom add/remove control point action
     */
    private final CustomAddRemoveControlPointAction addRemoveControlPointAction;
    /**
     * Custom move control point action
     */
    private final CustomMoveControlPointAction moveControlPointAction;
    /**
     * Custom select provider
     */
    private final WidgetAction selectAction;
    /**
     * Reference to the action factory used to assign actions to the nodes and connections
     */
    private final MPLSModuleActions moduleActions;
    /**
     * Reference to links and its ports created in the scene
     */
    private Map<LocalObjectLight, LocalObjectLight[]> connections;
    
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
        this.initSelectionListener();
        connectProvider = new MPLSModuleConnectProvider();
        connections = new HashMap<>();
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
                xmlew.add(xmlef.createAttribute(new QName("class"), nodeObject.getId().contains("-*") ? "unknown" : nodeObject.getClassName()));
                xmlew.add(xmlef.createCharacters(nodeObject.getId().contains("-*") ? "-1" : nodeObject.getId()));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                LocalObjectLight edgeObject = (LocalObjectLight) findObject(edgeWidget);
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                xmlew.add(xmlef.createAttribute(new QName("id"), edgeObject.getId()));
                xmlew.add(xmlef.createAttribute(new QName("class"), edgeObject.getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("asideid"), getEdgeSource(edgeObject).getId().contains("-*") ? 
                        "-1" : getEdgeSource(edgeObject).getId()));
                xmlew.add(xmlef.createAttribute(new QName("asideclass"), getEdgeSource(edgeObject).getId().contains("-*") ? 
                        "" : getEdgeSource(edgeObject).getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("bsideid"), getEdgeTarget(edgeObject).getId().contains("-*") ? 
                        "-1" : getEdgeTarget(edgeObject).getId()));
                xmlew.add(xmlef.createAttribute(new QName("bsideclass"), getEdgeTarget(edgeObject).getId().contains("-*") ? 
                        "" : getEdgeTarget(edgeObject).getClassName()));
                
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
        List<LocalObjectLight> emptySides = new ArrayList<>();
//      <editor-fold defaultstate="collapsed" desc="Uncomment this for debugging purposes. This outputs the XML view as a file">
//        try (FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/oview_ MPLS_VIEW .xml")) {
//            fos.write(structure);
//        } catch(Exception e) { }
//      </editor-fold>
        QName qNode = new QName("node"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qControlPoint = new QName("controlpoint"); //NOI18N
        LocalObjectLight[] endpoints = new LocalObjectLight[2];
        LocalObjectLight emptyObj;
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
                        String objectId = reader.getElementText();
                        //this side is connected
                        LocalObjectLight lol = CommunicationsStub.getInstance().getObjectInfoLight(objectClass, objectId);
                        if (lol != null){
                            Widget widget = addNode(lol);
                            widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                        }
                        else if(objectId.equals("-1")){// we create an empty side
                            emptyObj = new LocalObjectLight(UUID.randomUUID().toString() + "-" + (objectId), null, null);
                            emptySides.add(emptyObj);
                            Widget widget = addNode(emptyObj);
                            widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                        }
                    }else {
                        if (reader.getName().equals(qEdge)){
                            String mplsLinkId = reader.getAttributeValue(null, "id");
                            
                            String aSideId = reader.getAttributeValue(null, "asideid");
                            String aSideClass = reader.getAttributeValue(null, "asideclass");
                            String bSideId = reader.getAttributeValue(null, "bsideid");
                            String bSideClass = reader.getAttributeValue(null, "bsideclass");
                            

                            String className = reader.getAttributeValue(null, "class");
                            LocalObjectLight mplsLink = CommunicationsStub.getInstance().getObjectInfoLight(className, mplsLinkId);
                            if (mplsLink != null) {
                                LocalObjectLight aSideObject, bSideObject;
                                if(!aSideId.equals("-1"))
                                    aSideObject = new LocalObjectLight(aSideId, null, aSideClass);
                                else{
                                    aSideObject = emptySides.remove(0);
                                    endpoints[0] = aSideObject;
                                }
                                if(!bSideId.equals("-1"))
                                    bSideObject = new LocalObjectLight(bSideId, null, bSideClass);
                                else{
                                    bSideObject = emptySides.remove(0);
                                    endpoints[1] = bSideObject;
                                }            
                                HashMap<String, LocalObjectLight[]> specialAttributes = CommunicationsStub.getInstance().getSpecialAttributes(mplsLink.getClassName(), mplsLinkId);
                                for (Map.Entry<String, LocalObjectLight[]> entry : specialAttributes.entrySet()) {
                                    if(entry.getKey().equals("mplsEndpointA")){
                                        endpoints[0] = entry.getValue()[0];
                                        LocalObjectLight parentA = CommunicationsStub.getInstance().getFirstParentOfClass(endpoints[0].getClassName(), endpoints[0].getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                                        if(parentA != null && !parentA.getId().equals(aSideId)){
                                            Widget widget = findWidget(aSideObject);
                                            detachNodeWidget(aSideObject, widget);
                                            aSideObject = parentA;
                                            addNode(aSideObject);
                                        }
                                    }
                                    if(entry.getKey().equals("mplsEndpointB")){
                                        endpoints[1] = entry.getValue()[0];
                                        LocalObjectLight parentB = CommunicationsStub.getInstance().getFirstParentOfClass(endpoints[1].getClassName(), endpoints[1].getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);                                        
                                        if(parentB != null && !parentB.getId().equals(bSideId)){
                                            Widget widget = findWidget(bSideObject);
                                            detachNodeWidget(bSideObject, widget);
                                            bSideObject = parentB;
                                            addNode(bSideObject);
                                        }
                                    }
                                }
                                connections.put(mplsLink, endpoints);
                                
                                ConnectionWidget newEdge = (ObjectConnectionWidget)addEdge(mplsLink);
                                setEdgeSource(mplsLink, aSideObject);
                                setEdgeTarget(mplsLink, bSideObject);
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
                            } else {
                                fireChangeEvent(new ActionEvent(this, SCENE_CHANGE, "connectionAutomaticallyRemoved")); //NOI18N
                                NotificationUtil.getInstance().showSimplePopup("Load view", NotificationUtil.INFO_MESSAGE, String.format("Connection of class %s and id %s could not be found and was removed from the view", className, mplsLinkId));
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

    public Map<LocalObjectLight, LocalObjectLight[]> getConnections() {
        return connections;
    }
    
    public void update(LocalObjectLight mplsLink){
        LocalObjectLight edgeSource = getEdgeSource(mplsLink);
        LocalObjectLight edgeTarget = getEdgeTarget(mplsLink);
        boolean isSideA = false;
        boolean isSideB = false;
        HashMap<String, LocalObjectLight[]> specialAttributes = CommunicationsStub.getInstance().getSpecialAttributes(mplsLink.getClassName(), mplsLink.getId());
        for (Map.Entry<String, LocalObjectLight[]> entry : specialAttributes.entrySet()) {
            if(entry.getKey().equals("mplsEndpointA")){
                LocalObjectLight parentA = CommunicationsStub.getInstance().getFirstParentOfClass(
                        entry.getValue()[0].getClassName(), entry.getValue()[0].getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                
                if(edgeSource.getId().contains("-*") && parentA != null && !parentA.getId().equals(edgeTarget.getId())){
                    isSideA = true;
                    Widget unconnectedSideWidget = findWidget(edgeSource);
                    detachNodeWidget(edgeSource, unconnectedSideWidget);
                    Widget widget = findWidget(parentA);
                    if(widget == null)
                        addNode(parentA);
                    setEdgeSource(mplsLink, parentA);
                }
            }
            if(entry.getKey().equals("mplsEndpointB")){
                LocalObjectLight parentB = CommunicationsStub.getInstance().getFirstParentOfClass(
                        entry.getValue()[0].getClassName(), entry.getValue()[0].getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
               
                if(edgeTarget.getId().contains("-*") && parentB != null && !parentB.getId().equals(edgeSource.getId())){
                    isSideB = true;
                    Widget unconnectedSideWidget = findWidget(edgeTarget);
                    detachNodeWidget(edgeTarget, unconnectedSideWidget);
                    Widget widget = findWidget(parentB);
                    if(widget == null)
                        addNode(parentB);
                    setEdgeTarget(mplsLink, parentB);
                }
            }
            if(!isSideA){
                Widget widget = findWidget(edgeSource);
                detachNodeWidget(edgeSource, widget);
                Random rand = new Random();
                LocalObjectLight emptyObj = new LocalObjectLight(UUID.randomUUID().toString() + "-*", null, null);
                addNode(emptyObj);
                setEdgeSource(mplsLink, emptyObj);
            }
            
            if(!isSideB){
                Widget widget = findWidget(edgeTarget);
                detachNodeWidget(edgeTarget, widget);
                Random rand = new Random();
                LocalObjectLight emptyObj = new LocalObjectLight(UUID.randomUUID().toString() + "-*", null, null);
                addNode(emptyObj);
                setEdgeTarget(mplsLink, emptyObj);
            }
            validate();
            repaint();
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
        Widget newNode;
        if(node.getName() != null && node.getClassName() != null){
            LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
            if (classMetadata == null) //Should not happen, but this check should always be done
                newNode = new ObjectNodeWidget(this, node);
            else
                newNode = new ObjectNodeWidget(this, node, classMetadata.getIcon());
            newNode.getActions(ACTION_CONNECT).addAction(selectAction);
            newNode.getActions(ACTION_CONNECT).addAction(ActionFactory.createConnectAction(edgeLayer, connectProvider));
            newNode.getActions().addAction(ActionFactory.createPopupMenuAction(moduleActions.createMenuForNode()));
        }else{
            node.setName("unconnectedSide");
            newNode = new EmptyNodeWidget(this , node, null);
            newNode.getActions().addAction(ActionFactory.createAcceptAction(new MPLSSceneAcceptProvider(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT)));
            
            newNode.getActions().addAction(ActionFactory.createHoverAction(new TwoStateHoverProvider() {
                @Override
                public void unsetHovering(Widget widget) { }

                @Override
                public void setHovering(Widget widget) {
                    LocalObjectLight deviceToConnect =  MPLSModuleScene.this.getLookup().lookup(LocalObjectLight.class);
                    if (deviceToConnect != null) {
                        Widget widgetToConnect = MPLSModuleScene.this.findWidget(deviceToConnect);

                        Point toConnectLocation = widgetToConnect.convertLocalToScene(widgetToConnect.getLocation());
                        double xStart = toConnectLocation.getX();
                        double yStart = toConnectLocation.getY();
                        double xEnd = widgetToConnect.getBounds().getWidth() + toConnectLocation.getX();
                        double yEnd = widgetToConnect.getBounds().getHeight() + toConnectLocation.getY();

                        Point emptyLocation = newNode.convertLocalToScene(newNode.getLocation());
                        double emptyXStart = emptyLocation.getX();
                        double emptyXEnd = newNode.getBounds().getWidth() + emptyXStart;
                        double emptyYStart = emptyLocation.getY();
                        double emptyYEnd = newNode.getBounds().getHeight() + emptyYStart;

                        if (xStart > emptyXStart && xStart < emptyXEnd && yStart > emptyYStart && yStart < emptyYEnd
                                || xEnd > emptyXStart && xEnd < emptyXEnd && yStart > emptyYStart && yStart < emptyYEnd
                                || xEnd > emptyXStart && xEnd < emptyXEnd && yEnd > emptyYStart && yEnd < emptyYEnd
                                || xStart > emptyXStart && xStart < emptyXEnd && yEnd > emptyYStart && yEnd < emptyYEnd)
                        {
                            Collection<LocalObjectLight> findNodeEdges = MPLSModuleScene.this.findNodeEdges(((EmptyNodeWidget)newNode).getEmptyObj(), true, true);
                            final List<LocalObjectLight>  mplsLinks = new ArrayList<>(findNodeEdges);
                            HashMap<String, LocalObjectLight[]> specialAttributes = CommunicationsStub.getInstance().getSpecialAttributes(mplsLinks.get(0).getClassName(), mplsLinks.get(0).getId());

                            if (specialAttributes == null) {
                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                                return;
                            }

                            final LocalObjectLight aSideRoot;
                            final LocalObjectLight bSideRoot;

                            if (specialAttributes.containsKey("mplsEndpointA")) {//NOI18N
                                aSideRoot = CommunicationsStub.getInstance().getFirstParentOfClass(specialAttributes.get("mplsEndpointA")[0].getClassName(), specialAttributes.get("mplsEndpointA")[0].getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT); //NOI18N  specialAttributes.get("endpointA")[0]; //NOI18N
                                if (aSideRoot == null) {
                                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                                    return;
                                }
                            }
                            else
                                aSideRoot = deviceToConnect;
                           
                            if (specialAttributes.containsKey("mplsEndpointB")) { //NOI18N
                                bSideRoot = CommunicationsStub.getInstance().getFirstParentOfClass(specialAttributes.get("mplsEndpointB")[0].getClassName(), specialAttributes.get("mplsEndpointB")[0].getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT); //NOI18N  specialAttributes.get("endpointA")[0]; //NOI18N
                                if (bSideRoot == null) {
                                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                                    return;
                                }
                            }
                            else
                                 bSideRoot = deviceToConnect;

                            EditMPLSLinkEnpointsFrame frame = new EditMPLSLinkEnpointsFrame(mplsLinks.get(0), aSideRoot, bSideRoot);
                            frame.setLocationRelativeTo(null);
                            frame.setVisible(true);
                            frame.addWindowListener(new WindowAdapter(){
                                @Override
                                public void windowClosing(WindowEvent e){
                                    e.getWindow().dispose();
                                    update(mplsLinks.get(0));
                                }
                            });
                        }
                    }
                }
            }));
        }
        nodeLayer.addChild(newNode);
        newNode.getActions(ACTION_SELECT).addAction(selectAction);
        newNode.getActions(ACTION_SELECT).addAction(ActionFactory.createMoveAction(moveProvider, moveProvider));
        
        return newNode;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        ObjectConnectionWidget newEdge = new ObjectConnectionWidget(this, edge, ObjectConnectionWidget.LINE);
        newEdge.getActions().addAction(selectAction);
        newEdge.getActions().addAction(addRemoveControlPointAction);
        newEdge.getActions().addAction(moveControlPointAction);
        newEdge.setStroke(new BasicStroke(1));
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
                //Only create edges in the scene if the connection is a MPLSLink
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
