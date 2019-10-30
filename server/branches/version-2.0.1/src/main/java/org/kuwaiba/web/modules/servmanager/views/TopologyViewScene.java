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
package org.kuwaiba.web.modules.servmanager.views;

import com.neotropic.vaadin.lienzo.client.core.shape.SrvEdgeWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvNodeWidget;
import com.vaadin.server.Page;
import com.vaadin.ui.Label;
import java.awt.Color;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.kuwaiba.apis.web.gui.navigation.views.AbstractScene;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.views.util.UtilHtml;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;

/**
 * Shows a topology view of a service by detecting the STMX and physical connections between the GenericCOmmunicationsElement instances 
 * with logical connections, though it uses the physical path to fill the gaps between different logical segments
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TopologyViewScene extends AbstractScene {
    
    protected static final String VIEW_CLASS = "TopologyView"; //NOI18N
    /**
     * The service this view is associated to
     */
    private RemoteObjectLight service;
    
    public TopologyViewScene(RemoteObjectLight service, WebserviceBean wsBean, RemoteSession session) {
        super (wsBean, session);
        this.service = service;
    }
        
    @Override
    public void render(byte[] structure) throws IllegalArgumentException { 
//        try {
//            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
//            QName qNode = new QName("node"); //NOI18N
//            QName qEdge = new QName("edge"); //NOI18N
//            QName qControlPoint = new QName("controlpoint"); //NOI18N
//
//            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
//            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
//
//            while (reader.hasNext()) {
//                int event = reader.next();
//                if (event == XMLStreamConstants.START_ELEMENT) {
//                    if (reader.getName().equals(qNode)) {
//                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
//                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
//                        
//                        long nodeId = Long.valueOf(reader.getElementText());
//                        SrvNodeWidget aSavedNode = findNodeWidget(nodeId);
//                        if (aSavedNode != null) { //If it's null, it means that the node wasn't added by the default rendering method, so the node no longer exists and shouldn't be rendered
//                            aSavedNode.setX(xCoordinate);
//                            aSavedNode.setY(yCoordinate);
//                        }
//                        
//                    } else {
//                        if (reader.getName().equals(qEdge)) {
//                            long edgeId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N
//                            SrvEdgeWidget aSavedEdge = findEdgeWidget(edgeId);
//                            if (aSavedEdge != null) { //If it's null, it means that the node wasn't added by the default rendering method, so the node no longer exists and shouldn't be rendered
//                                List<Point> localControlPoints = new ArrayList<>();
//                                while(true) {
//                                    reader.nextTag();
//
//                                    if (reader.getName().equals(qControlPoint)) {
//                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
//                                            localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
//                                    } else {
//                                        aSavedEdge.setControlPoints(localControlPoints);
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (XMLStreamException ex) {
//            Notifications.showError(ex.getMessage());
//        }
    }

    @Override
    public void render() {
        try {
            List<RemoteObjectLight> serviceResources = wsBean.getServiceResources(service.getClassName(), 
                    service.getId(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
            if (serviceResources.isEmpty())
                addComponent(new Label(String.format("%s does not have any resources associated to it", service)));
            else {

                Map<String, RemoteObjectLight> portsInDevice = new HashMap<>();
                //We will ignore all resources that are not GenericCommunicationsElement
                for (RemoteObjectLight serviceResource : serviceResources) {
                    if (wsBean.isSubclassOf(serviceResource.getClassName(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, 
                            Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId())) {
                        
                        if (findNodeWidget(serviceResource) == null)
                            attachNodeWidget(serviceResource);

                        List<RemoteObjectLight> physicalPorts = wsBean.getChildrenOfClassLightRecursive(serviceResource.getId(), 
                                serviceResource.getClassName(), "GenericPhysicalPort", -1,  Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                        
                        for (RemoteObjectLight physicalPort : physicalPorts)
                            portsInDevice.put(physicalPort.getId(), serviceResource);
                    }
                }

                //Once the nodes have been added, we retrieve the physical and logical (STMX) connections between them and ignore those that end in other elements
                for (RemoteObjectLight aNode : nodes.keySet()) {
                    List<RemoteObjectLightList> physicalConnections = wsBean.getPhysicalConnectionsInObject(aNode.getClassName(), aNode.getId(), 
                            Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                    List<RemoteObjectLight> logicalConnections = wsBean.getSpecialAttribute(aNode.getClassName(), aNode.getId(), "sdhTransportLink",
                            Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId()); //NOI18N
                    
                    //First the physical connections
                    for (RemoteObjectLightList aConnection : physicalConnections) {
                        RemoteObjectLight sourcePort = aConnection.getList().get(0);
                        RemoteObjectLight targetPort = aConnection.getList().get(aConnection.getList().size() - 1);

                        SrvNodeWidget sourceEquipment = findNodeWidget(aNode);
                        SrvNodeWidget targetEquipment = findNodeWidget(portsInDevice.get(targetPort.getId()));

                        if (targetEquipment != null) {

                            SrvEdgeWidget connectionWidget = findEdgeWidget(aConnection.getList().get(1));
                            if (connectionWidget == null) 
                                connectionWidget = findEdgeWidget(aConnection.getList().get(aConnection.getList().size() - 2));

                            if (connectionWidget == null) 
                                connectionWidget = attachEdgeWidget(aConnection.getList().get(1), sourceEquipment, targetEquipment);
                            

                            connectionWidget.setCaption(aNode.getName() + ":" + sourcePort.getName() + 
                                        " ** " + portsInDevice.get(targetPort.getId()).getName() + ":" + targetPort.getName());
                        } //Else, we just ignore this connection trace
                    }

                    //Now the logical connections
                    for (RemoteObjectLight aConnection : logicalConnections) {
                        SrvEdgeWidget logicalConnectionWidget = findEdgeWidget(aConnection);
                        
                        if (logicalConnectionWidget == null) 
                            attachEdgeWidget(aConnection, findNodeWidget(aNode), null);
                        else
                            logicalConnectionWidget.setTarget(findNodeWidget(aNode));
                    }
                }
                //Now we delete the connections to elements that are not in the view (they will only have a source, not a target widget). Granted, this is a reprocess, but I prefer and save a few
                //calls to the server doing this at client-side only
//                for (RemoteObjectLight aConnection : new ArrayList<>(edges.keySet())) {
//                    lienzoComponent.removeEdgeWidget(aConnection.getId());
//                    edges.remove(aConnection);
//                }
                
                addComponent(lienzoComponent);
            }
        } catch (ServerSideException ex) {
            clear();
            Notifications.showError(ex.getMessage());
        }
        
    }
    
    public void clear() {
        //this.lienzoComponent.remove
    }

    protected SrvNodeWidget attachNodeWidget(RemoteObjectLight node) {
        SrvNodeWidget newNode = new SrvNodeWidget();
        newNode.setUrlIcon("/icons/" + node.getClassName() + ".png");
        newNode.setCaption(node.toString());
        newNode.setX(nodes.size() * 20);
        newNode.setY((nodes.size() % 2) * 20);
        nodes.put(node, newNode);
        lienzoComponent.addNodeWidget(node, newNode);
        return newNode;
    }
    
    protected SrvEdgeWidget attachEdgeWidget(RemoteObjectLight edge, SrvNodeWidget sourceNode, SrvNodeWidget targetNode) {
        try {
            SrvEdgeWidget newEdge = new SrvEdgeWidget();
            newEdge.setSource(sourceNode);
            newEdge.setTarget(targetNode);
            
            RemoteClassMetadata classMetadata = wsBean.getClass(edge.getClassName(), session.getIpAddress(), session.getSessionId());
            newEdge.setColor(UtilHtml.toHexString(new Color(classMetadata.getColor())));
            newEdge.setCaption(edge.toString());
            edges.put(edge, newEdge);
            lienzoComponent.addEdgeWidget(edge, newEdge);
            return newEdge; 
        } catch (ServerSideException ex) {
            SrvEdgeWidget srvEdgeWidget = new SrvEdgeWidget();
            srvEdgeWidget.setId(UUID.randomUUID().toString());
            return srvEdgeWidget;
        }
    }
}
