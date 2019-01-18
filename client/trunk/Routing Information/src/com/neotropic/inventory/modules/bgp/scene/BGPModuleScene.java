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
package com.neotropic.inventory.modules.bgp.scene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalLogicalConnectionDetails;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.actions.providers.CustomAcceptActionProvider;
import org.inventory.core.visual.actions.providers.CustomMoveProvider;
import org.inventory.core.visual.actions.providers.CustomSelectProvider;
import org.inventory.core.visual.actions.providers.SceneConnectProvider;
import org.inventory.core.visual.scene.AbstractScene;
import static org.inventory.core.visual.scene.AbstractScene.ACTION_SELECT;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.router.RouterFactory;
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
    private List<LocalLogicalConnectionDetails> bgpMap;
    private List<Long> bgpLinksIds;

    public BGPModuleScene() {
        getActions().addAction(ActionFactory.createAcceptAction(new CustomAcceptActionProvider(this, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT)));
        
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        interactionLayer = new LayerWidget(this);
        backgroundLayer = new LayerWidget(this);
        
        bgpMap = new ArrayList<>();
        bgpLinksIds = new ArrayList<>();
        
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
            xmlew.add(xmlef.createCharacters("BGPModuleView"));
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
                
                xmlew.add(xmlef.createCharacters(Long.toString(nodeObject.getId())));
                
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                LocalObjectLight edgeObject = (LocalObjectLight) findObject(edgeWidget);
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                xmlew.add(xmlef.createAttribute(new QName("id"), Long.toString(edgeObject.getId())));
                xmlew.add(xmlef.createAttribute(new QName("class"), edgeObject.getClassName()));
                
                xmlew.add(xmlef.createAttribute(new QName("aside"), Long.toString(getEdgeSource(edgeObject).getId())));
                xmlew.add(xmlef.createAttribute(new QName("bside"), Long.toString(getEdgeTarget(edgeObject).getId())));
                
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
        
    }

    public void createBGPView(List<LocalLogicalConnectionDetails> bgpMap){
        this.clear();
        this.bgpMap = bgpMap;
        Map<LocalObjectLight, List<LocalObjectLight>> portDevices = new HashMap<>();
        Map<LocalObjectLight, LocalObjectLight> portParent = new HashMap<>();
        Map<LocalObjectLight, List<LocalObjectLight>> bgpLinkPorts = new HashMap<>();
       
        for (LocalLogicalConnectionDetails logicalConnectionDetail : bgpMap) {
            LocalObjectLight endpointA = logicalConnectionDetail.getEndpointA();
            LocalObjectLight endpointB = logicalConnectionDetail.getEndpointB();
            
            List<LocalObjectLight> physicalPathForEndpointA = logicalConnectionDetail.getPhysicalPathForEndpointA();
            List<LocalObjectLight> physicalPathForEndpointB = logicalConnectionDetail.getPhysicalPathForEndpointB();
            List<LocalObjectLight> bgpLink = new ArrayList<>();
            
            if(endpointA != null){ 
                if(portDevices.get(endpointA) == null)
                    portDevices.put(endpointA, new ArrayList<LocalObjectLight>());
            
                portParent.put(endpointA, physicalPathForEndpointA.get(0));
                if(!physicalPathForEndpointB.isEmpty() && 
                        physicalPathForEndpointB.get(0) != null){
                    portDevices.get(endpointA).add(physicalPathForEndpointB.get(0));
                    bgpLink.add(physicalPathForEndpointA.get(0));
                }
            }
                      
            if(endpointB != null){ 
//                if(portDevices.get(endpointB) == null)
//                    portDevices.put(endpointB, new ArrayList<LocalObjectLight>());
            
                portParent.put(endpointB, physicalPathForEndpointB.get(0));
                if(!physicalPathForEndpointA.isEmpty() && 
                        physicalPathForEndpointA.get(0) != null){
//                    portDevices.get(endpointB).add(physicalPathForEndpointA.get(0));                
                    bgpLink.add(physicalPathForEndpointB.get(0));
                }
            }
            
            bgpLinkPorts.put(logicalConnectionDetail.getConnectionObject(), bgpLink);
        }
        
        Random r = new Random(35660);
        for (Map.Entry<LocalObjectLight, List<LocalObjectLight>> entry : portDevices.entrySet()) {
            LocalObjectLight port = entry.getKey();
            List<LocalObjectLight> destinations = entry.getValue();
            LocalObjectLight source = portParent.get(port);

            if(findWidget(source) == null)
                addNode(source);
            if(!destinations.isEmpty()){
                if(destinations.size() == 1){
                    //addNode(destinations.get(0));
                    //validate();
                    //LocalObjectLight tempE = new LocalObjectLight(r.nextLong(), port.getName() + " IX ", "BGPLink");
                    //addEdge(tempE);
                    //validate();
                    //setEdgeSource(tempE, source);
                    validate();
                   // setEdgeTarget(tempE, destinations.get(0)); 
                    //validate();
                }
                else{
                    int ix=0;
                    int notIx=0;
                    for(LocalObjectLight device : destinations){
                        if(!device.getClassName().equals("Cloud")){
                            if(findWidget(device) == null)
                                addNode(device);
                            notIx ++;
                            validate();
                            LocalObjectLight tempE = new LocalObjectLight(r.nextLong(), port.getName() + " IX ", "BGPLink");
                            addEdge(tempE);
                            validate();
                            setEdgeSource(tempE, source);
                            validate();
                            setEdgeTarget(tempE, device); 
                            validate();
                        }
                        ix++;
                    }
                    if(destinations.size() - notIx != 0){
                        LocalObjectLight temp = new LocalObjectLight(50373, "IX", "Cloud");
                        if(findWidget(temp) == null)
                               addNode(temp);
                        validate();
                        LocalObjectLight tempE = new LocalObjectLight(r.nextLong(), port.getName() + " IX ", "BGPLink");
                        addEdge(tempE);
                        validate();
                        setEdgeSource(tempE, source);
                        validate();
                        setEdgeTarget(tempE, temp); 
                        validate();
                    }
                }
            }
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
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
        ObjectNodeWidget newNode;
        if (classMetadata == null) //Should not happen, but this check should always be done
            newNode = new ObjectNodeWidget(this, node);
        else
            newNode = new ObjectNodeWidget(this, node, classMetadata.getIcon());
        
        nodeLayer.addChild(newNode);
        newNode.getActions(ACTION_SELECT).addAction(selectAction);
        newNode.getActions(ACTION_SELECT).addAction(ActionFactory.createMoveAction(moveProvider, moveProvider));
        newNode.setPreferredLocation(new Point(nodeLayer.getChildren().size() * 200, (nodeLayer.getChildren().size() % 2) * 200 ));       
        //newNode.getActions(ACTION_CONNECT).addAction(selectAction);
        //newNode.getActions(ACTION_CONNECT).addAction(ActionFactory.createConnectAction(interactionLayer, connectProvider));
        
        newNode.setHighContrast(true);
        
        return newNode;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        ObjectConnectionWidget newEdge = new ObjectConnectionWidget(this, edge);
        newEdge.getActions().addAction(selectAction);
        newEdge.getActions().addAction(addRemoveControlPointAction);
        newEdge.getActions().addAction(moveControlPointAction);
        
        newEdge.setStroke(new BasicStroke(3));
        newEdge.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        newEdge.setRouter(RouterFactory.createFreeRouter());
        newEdge.setToolTipText(edge.toString());
        LocalClassMetadata connectionClassMetadata = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
        if (connectionClassMetadata == null || connectionClassMetadata.getColor() == null)
            newEdge.setLineColor(Color.BLACK);
        else
            newEdge.setLineColor(connectionClassMetadata.getColor());
        
        edgeLayer.addChild(newEdge);
        return newEdge;
    }
}
