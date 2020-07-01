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
package com.neotropic.inventory.modules.routing.bgp.scene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.inventory.communications.core.LocalLogicalConnectionDetails;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.actions.providers.CustomAcceptActionProvider;
import org.inventory.core.visual.actions.providers.CustomMoveProvider;
import org.inventory.core.visual.actions.providers.CustomSelectProvider;
import org.inventory.core.visual.actions.providers.SceneConnectProvider;
import org.inventory.core.visual.scene.AbstractScene;
import static org.inventory.core.visual.scene.AbstractScene.ACTION_SELECT;
import org.inventory.core.visual.scene.EmptyNodeWidget;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.EditProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * This is the scene used in the BGP Module
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class BGPModuleScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
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
    //private List<LocalLogicalConnectionDetails> bgpMap;
    //private List<Long> bgpLinksIds;

    public BGPModuleScene() {
        getActions().addAction(ActionFactory.createAcceptAction(new CustomAcceptActionProvider(this, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT)));
        
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        interactionLayer = new LayerWidget(this);
        backgroundLayer = new LayerWidget(this);
        
        //bgpMap = new ArrayList<>();
        //bgpLinksIds = new ArrayList<>();
        
        addChild(backgroundLayer);
        addChild(interactionLayer);
        addChild(edgeLayer);
        addChild(nodeLayer);
        
        
        moveProvider = new CustomMoveProvider(this);
        selectAction = ActionFactory.createSelectAction(new CustomSelectProvider(this), true);
        addRemoveControlPointAction = new CustomAddRemoveControlPointAction(this);
        moveControlPointAction = new CustomMoveControlPointAction(this);

        getActions().addAction(ActionFactory.createZoomAction());
        getActions().addAction(ActionFactory.createPanAction());
        getInputBindings ().setZoomActionModifiers(0); //No keystroke combinations
        getInputBindings ().setPanActionButton(MouseEvent.BUTTON1); //Pan using the left click
        
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
            xmlew.add(xmlef.createCharacters("BGPModuleView")); // NOI18N
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            for (Widget nodeWidget : nodeLayer.getChildren()) {
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(nodeWidget.getPreferredLocation().x)));
                xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(nodeWidget.getPreferredLocation().y)));
                
                LocalObjectLight nodeObject = (LocalObjectLight) findObject(nodeWidget);
                if(nodeObject.getClassName().equals("BGPPeer"))
                    xmlew.add(xmlef.createAttribute(new QName("bgppeers"), String.join("~", ((EmptyNodeWidget)nodeWidget).getExtraInfo())));
                
                xmlew.add(xmlef.createAttribute(new QName("class"), nodeObject.getClassName()));
                
                xmlew.add(xmlef.createCharacters(nodeObject.getId()));
                
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
                
                xmlew.add(xmlef.createAttribute(new QName("asideid"), getEdgeSource(edgeObject).getId()));
                xmlew.add(xmlef.createAttribute(new QName("asideclassname"), getEdgeSource(edgeObject).getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("bsideid"), getEdgeTarget(edgeObject).getId()));
                xmlew.add(xmlef.createAttribute(new QName("bsideclassname"), getEdgeSource(edgeObject).getClassName()));
                
                for (Point point : ((ConnectionWidget)edgeWidget).getControlPoints()) {
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
//<editor-fold defaultstate="collapsed" desc="Uncomment this for debugging purposes. This outputs the XML view as a file">
//        try (FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/oview_ MPLS_VIEW .xml")) {
//            fos.write(structure);
//        } catch(Exception e) { }
//</editor-fold>
        
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
                        
                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        String bgpPeer = reader.getAttributeValue(null, "bgppeers");
                        String objectClass = reader.getAttributeValue(null, "class");
                        String objectId = reader.getElementText();
                        
                        LocalObjectLight lol;
                        
                        if(objectClass.equals("BGPPeers"))
                            lol = new LocalObjectLight(objectId, bgpPeer, objectClass);
                        else
                            lol = CommunicationsStub.getInstance().getObjectInfoLight(objectClass, objectId);

                        if (lol != null){
                            Widget widget = addNode(lol);
                            widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                        }else {
                            NotificationUtil.getInstance().showSimplePopup("Load View", 
                                    NotificationUtil.INFO_MESSAGE, String.format("Equipment of class %s and id %s could not be found and was removed from the view", 
                                    objectClass, objectId));
                            fireChangeEvent(new ActionEvent(this, SCENE_CHANGE, "nodeAutomaticallyRemoved")); //NOI18N
                        }
                    }else {
                        if (reader.getName().equals(qEdge)){
                            String objectId = reader.getAttributeValue(null, "id");
                            String aSideId = reader.getAttributeValue(null, "asideid");
                            String aSideClassName = reader.getAttributeValue(null, "asideclassname");
                            String bSideId = reader.getAttributeValue(null, "bsideid");
                            String bSideClassName = reader.getAttributeValue(null, "bsideclassname");

                            String className = reader.getAttributeValue(null,"class");
                            LocalObjectLight bgpLink;
                            if(className.isEmpty())
                                bgpLink = new LocalObjectLight(objectId, "", "");
                            else
                                bgpLink = CommunicationsStub.getInstance().getObjectInfoLight(className, objectId);
                            if (bgpLink != null) {
                                LocalObjectLight aSideObject = new LocalObjectLight(aSideId, null, aSideClassName);
                                Widget aSideWidget = findWidget(aSideObject);

                                LocalObjectLight bSideObject = new LocalObjectLight(bSideId, null, bSideClassName);
                                Widget bSideWidget = findWidget(bSideObject);

                                if (aSideWidget == null || bSideWidget == null) {
                                    NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, 
                                            String.format("One or both of the endpoints of the connection of class %s and id %s could not be found. The connection was removed from the view", className, objectId));
                                    fireChangeEvent(new ActionEvent(this, SCENE_CHANGE, "connectionAutomaticallyRemoved")); //NOI18N
                                }
                                else {
                                    ConnectionWidget newEdge = (ConnectionWidget)addEdge(bgpLink);
                                    setEdgeSource(bgpLink, aSideObject);
                                    setEdgeTarget(bgpLink, bSideObject);
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
                                NotificationUtil.getInstance().showSimplePopup("Load view", 
                                        NotificationUtil.INFO_MESSAGE, String.format("Connection of class %s and id %s could not be found and was removed from the view", className, objectId));
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

    public void createBGPView(List<LocalLogicalConnectionDetails> bgpMap){
         // To keep tack of the device an its peerings
        Map<LocalObjectLight, List<LocalObjectLight>> devicePeerings = new HashMap<>();
        //To keep tack of the port and its destiny devices
        Map<LocalObjectLight, List<LocalObjectLight>> portDevices = new HashMap<>();
        //To keep track of the port/parent, to avoid a server call 
        Map<LocalObjectLight, LocalObjectLight> portParent = new HashMap<>();
        
        for (LocalLogicalConnectionDetails logicalConnectionDetail : bgpMap) {
            LocalObjectLight endpointA = logicalConnectionDetail.getEndpointA();
            LocalObjectLight endpointB = logicalConnectionDetail.getEndpointB();
            
            List<LocalObjectLight> physicalPathForEndpointA = logicalConnectionDetail.getPhysicalPathForEndpointA();
            List<LocalObjectLight> physicalPathForEndpointB = logicalConnectionDetail.getPhysicalPathForEndpointB();
            
            boolean sideAIsPeering = false, sideBIsPeering = false;
            if(endpointA != null && !physicalPathForEndpointA.isEmpty() && physicalPathForEndpointA.get(0) != null){
                //we add one side if has not been added
                if(!physicalPathForEndpointA.get(0).getClassName().equals("BGPPeer")){
                    if(findWidget(physicalPathForEndpointA.get(0)) == null)
                        addNode(physicalPathForEndpointA.get(0));
                    validate();
                }else{
                    sideAIsPeering = true;
                    if(portDevices.get(endpointA) == null)
                        portDevices.put(endpointA, new ArrayList<>());
                    portDevices.get(endpointA).add(physicalPathForEndpointB.get(0));
                }
                portParent.put(endpointA, physicalPathForEndpointA.get(0));
            }
                      
            if(endpointB != null && !physicalPathForEndpointB.isEmpty() && physicalPathForEndpointB.get(0) != null){ 
                //we add one side if has not been added
                if(!physicalPathForEndpointB.get(0).getClassName().equals("BGPPeer")){
                    if(findWidget(physicalPathForEndpointB.get(0)) == null)
                        addNode(physicalPathForEndpointB.get(0));
                    validate();
                }else{
                    sideBIsPeering = true;
                    if(portDevices.get(endpointB) == null)
                        portDevices.put(endpointB, new ArrayList<>());
                    portDevices.get(endpointB).add(physicalPathForEndpointA.get(0));
                }
                portParent.put(endpointB, physicalPathForEndpointB.get(0));
            }
            
            if(!sideAIsPeering && !sideBIsPeering && findWidget(logicalConnectionDetail.getConnectionObject()) == null){
                addEdge(logicalConnectionDetail.getConnectionObject());
                validate();
                setEdgeSource(logicalConnectionDetail.getConnectionObject(), physicalPathForEndpointA.get(0));
                setEdgeTarget(logicalConnectionDetail.getConnectionObject(), physicalPathForEndpointB.get(0)); 
            }
        }
        validate();
        //now we deal with the peerigns 
        for (Map.Entry<LocalObjectLight, List<LocalObjectLight>> entry : portDevices.entrySet()) {
            LocalObjectLight port = entry.getKey();
            List<LocalObjectLight> destinations = entry.getValue();
            LocalObjectLight source = portParent.get(port);
            
            if(source.getClassName().equals("BGPPeer") && destinations.size() == 1 && !destinations.get(0).getClassName().equals("BGPPeer")){
                if(devicePeerings.get(destinations.get(0)) == null)
                    devicePeerings.put(destinations.get(0), new ArrayList<>());
                
                devicePeerings.get(destinations.get(0)).add(source);
            }
            else if(!source.getClassName().equals("BGPPeer")){
                if(devicePeerings.get(source) == null)
                    devicePeerings.put(source, new ArrayList<>());
                
                for(LocalObjectLight destiny : destinations){
                    if(!destiny.getClassName().equals("BGPPeer"))
                        devicePeerings.get(source).add(destiny);
                }
            }
        }
        validate();
        
        for (Map.Entry<LocalObjectLight, List<LocalObjectLight>> entry : devicePeerings.entrySet()) {
            LocalObjectLight source = entry.getKey();
            List<LocalObjectLight> peerings = entry.getValue();
            String peeringsNames = "";
            
            for(LocalObjectLight peering : peerings)
              peeringsNames += peering.getName() + "~";
            
            LocalObjectLight peering = new LocalObjectLight(UUID.randomUUID().toString(), peeringsNames, "BGPPeer");
            addNode(peering);
            validate();
            LocalObjectLight tempE = new LocalObjectLight(UUID.randomUUID().toString(), "", "");
            addEdge(tempE);
            validate();
            setEdgeSource(tempE, source);
            validate();
            setEdgeTarget(tempE, peering); 
            validate();
        }
        validate();
        repaint();
    }
    
    @Override
    public void render(LocalObjectLight root) { }

    @Override
    public ConnectProvider getConnectProvider() {
        return connectProvider;
    }

    @Override
    public boolean supportsConnections() {
        return false;
    }

    @Override
    public boolean supportsBackgrounds() {
        return true;
    }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        Widget newNode;
        if(node.getClassName().equals("BGPPeer")){
            String[] pearringsList = node.getName().split("~");
            node.setName("ix");
            newNode = new EmptyNodeWidget(this, node, pearringsList);
            newNode.getActions().addAction(ActionFactory.createEditAction(new EditProvider() {
                @Override
                public void edit(Widget widget) {
                    ((EmptyNodeWidget)widget).showExtraInfo("Available Syncrhonization Providers", "Detail List of BGPPeers");
                }
            }));
        }
        else{
            LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);

            if (classMetadata == null) //Should not happen, but this check should always be done
                newNode = new ObjectNodeWidget(this, node);
            else
                newNode = new ObjectNodeWidget(this, node, classMetadata.getIcon());
             
            ((ObjectNodeWidget)newNode).setHighContrast(true);
            
        }
        nodeLayer.addChild(newNode);
        newNode.getActions(ACTION_SELECT).addAction(selectAction);
        newNode.getActions(ACTION_SELECT).addAction(ActionFactory.createMoveAction(moveProvider, moveProvider));
        newNode.setPreferredLocation(new Point(nodeLayer.getChildren().size() * 200, (nodeLayer.getChildren().size() % 2) * 200 ));       

        return newNode;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
       ConnectionWidget newEdge;
        if(edge.getName().isEmpty())
            newEdge = new ConnectionWidget(this);
        else{
            newEdge = new ObjectConnectionWidget(this, edge, ObjectConnectionWidget.LINE);
            newEdge.setToolTipText(edge.toString());
            LocalClassMetadata connectionClassMetadata = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
            if (connectionClassMetadata == null || connectionClassMetadata.getColor() == null)
                newEdge.setLineColor(Color.BLACK);
            else
                newEdge.setLineColor(connectionClassMetadata.getColor());
        }
        newEdge.getActions().addAction(selectAction);
        newEdge.getActions().addAction(addRemoveControlPointAction);
        newEdge.getActions().addAction(moveControlPointAction);
        
        newEdge.setStroke(new BasicStroke(3));
        newEdge.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setRouter(RouterFactory.createFreeRouter());
        
        edgeLayer.addChild(newEdge);
        return newEdge;
    }
}
