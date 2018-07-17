/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

import com.neotropic.vaadin.lienzo.LienzoComponent;
import com.neotropic.vaadin.lienzo.client.core.shape.Point;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvEdgeWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvNodeWidget;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetClickListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLightList;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectSpecialRelationships;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.openide.util.Exceptions;

/**
 * Shows an end-to-end view of a service by trying to match the endpoints of the logical circuits
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different: It's intended to deal exclusively 
 * with logical connections, though it uses the physical path to fill the gaps between different logical segments
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EndToEndViewScene extends VerticalLayout {
    
    public final static String VIEW_CLASS = "ServiceSimpleView"; 
    
    private final LienzoComponent lienzoComponent;
    
    private HashMap<RemoteObjectLight, SrvNodeWidget> nodes;
    private HashMap<RemoteObjectLight, SrvEdgeWidget> edges;
    
    private final WebserviceBean wsBean;
    private final String ipAddress;
    private final String sessionId;
    private RemoteObjectLight service ;
    
    public EndToEndViewScene(RemoteObjectLight service, WebserviceBean wsBean, String sessionId, String ipAddress) {
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.wsBean = wsBean;
        this.service = service;
        this.lienzoComponent = new LienzoComponent();
        lienzoComponent.addNodeWidgetClickListener(nodeWidgetClickListener);
        lienzoComponent.addEdgeWidgetClickListener(edgeWidgetClickListener);
    }
    
    EdgeWidgetClickListener edgeWidgetClickListener = new EdgeWidgetClickListener() {

        @Override
        public void edgeWidgetClicked(long id) {
            SrvEdgeWidget srvEdge = lienzoComponent.getEdge(id);
            Window tableInfo = new Window(" ");
            tableInfo.addStyleName("v-window-center");
            try {
                ServManagerFormCreator formView = new ServManagerFormCreator(service, wsBean, ipAddress, sessionId);

                for (RemoteObjectLight edge : edges.keySet()) {
                    if(edge.getId() == id && edge.getClassName().toLowerCase().equals("mplslink")){
                        Component x = formView.createVC(edge);
                        tableInfo.setContent(x);
                        getUI().addWindow(tableInfo);
                    }
                }
            lienzoComponent.updateEdgeWidget(srvEdge.getId());
            } catch (ServerSideException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    };
    
    NodeWidgetClickListener nodeWidgetClickListener = new NodeWidgetClickListener() {

        @Override
        public void nodeWidgetClicked(long id) {
            SrvNodeWidget srvNode = lienzoComponent.getNodeWidget(id);
            Window tableInfo = new Window(" ");
            tableInfo.addStyleName("v-window-center");
            try {
                Component x = null;
                for (RemoteObjectLight device : nodes.keySet()) {
                    if (device.getId() == id){
                        ServManagerFormCreator formView = new ServManagerFormCreator(service, wsBean, ipAddress, sessionId);
                        
                        List<SrvEdgeWidget> connectedEdgeWidgets = lienzoComponent.getNodeEdgeWidgets(srvNode);
                        for(SrvEdgeWidget edge : connectedEdgeWidgets){
                            RemoteObjectLight foundEdge = findEdge(edge.getId());
                            RemoteObjectSpecialRelationships specialAttributes = wsBean.getSpecialAttributes(foundEdge.getClassName(), foundEdge.getId(), ipAddress, sessionId);
                            List<RemoteObjectLightList> relatedObjects = specialAttributes.getRelatedObjects();
                            List<String> relationships = specialAttributes.getRelationships();
                            for(int i=0; i < relationships.size(); i++){
                                String relationShipName = relationships.get(i);
                                if(relationShipName.toLowerCase().contains("endpoint")){
                                    RemoteObjectLightList get = relatedObjects.get(i);
                                    RemoteObjectLight port = get.getList().get(0);
                                    if(port.getClassName().toLowerCase().contains("port")){
                                        List<RemoteObjectLight> parentsUntilFirstOfClass = wsBean.getParentsUntilFirstOfClass(port.getClassName(), port.getId(), device.getClassName(), ipAddress, sessionId);
                                        if(parentsUntilFirstOfClass.contains(device)){
                                            if(device.getClassName().toLowerCase().contains("router"))
                                                x = formView.createRouter(device, port);
                                            else if(device.getClassName().toLowerCase().contains("odf"))
                                                x = formView.createODF(device, port);
                                            else if(device.getClassName().toLowerCase().contains("external"))
                                                x = formView.createExternalEquipment(device);
                                            else if(device.getClassName().toLowerCase().contains("cloud"))
                                                x = formView.createPeering(device);
                                            tableInfo.setCaption(device.toString());
                                            tableInfo.center();
                                            //tableInfo.setWidth("100%");
                                            tableInfo.setContent(x);
                                            getUI().addWindow(tableInfo);
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
               
            } catch (ServerSideException ex) {
                Exceptions.printStackTrace(ex);
            }
            
            lienzoComponent.updateNodeWidget(id);
        }
    };
    
    public void render(byte[] structure) throws IllegalArgumentException { 
       //<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//        try {
//            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/end_to_end_view_.xml");
//            fos.write(structure);
//            fos.close();
//        } catch(Exception e) {}
        //</editor-fold>
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        
                        long nodeId = Long.valueOf(reader.getElementText());
                        SrvNodeWidget aSavedNode = findNodeWidget(nodeId);
                        if (aSavedNode != null) { //If it's null, it means that the node wasn't added by the default rendering method, so the node no longer exists and shouldn't be rendered
                            aSavedNode.setX(xCoordinate);
                            aSavedNode.setY(yCoordinate);
                        }
                        
                    } else {
                        if (reader.getName().equals(qEdge)) {
                            long edgeId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N
                            SrvEdgeWidget aSavedEdge = findEdgeWidget(edgeId);
                            if (aSavedEdge != null) { //If it's null, it means that the node wasn't added by the default rendering method, so the node no longer exists and shouldn't be rendered
                                List<Point> localControlPoints = new ArrayList<>();
                                while(true) {
                                    reader.nextTag();

                                    if (reader.getName().equals(qControlPoint)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                            localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                    } else {
                                        aSavedEdge.setControlPoints(localControlPoints);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (XMLStreamException ex) {
            Notifications.showError(ex.getMessage());
        }
    }

    public void render(RemoteObjectLight service) {
        try {
            List<RemoteObjectLight> serviceResources = wsBean.getServiceResources(service.getClassName(), service.getId(), ipAddress, sessionId);
            if (serviceResources.isEmpty())
                addComponent(new Label(String.format("%s does not have any resources associated to it", service)));
            else {
                
                this.nodes = new HashMap<>();
                this.edges = new HashMap<>();
                for (RemoteObjectLight serviceResource : serviceResources) {
                    if (wsBean.isSubclassOf(serviceResource.getClassName(), "GenericLogicalConnection", ipAddress, sessionId)) {
                        RemoteLogicalConnectionDetails logicalCircuitDetails = wsBean.getLogicalLinkDetails(
                                serviceResource.getClassName(), serviceResource.getId(), ipAddress, sessionId);
                        
                        //Let's create the nodes corresponding to the endpoint A of the logical circuit
                        List<RemoteObjectLight> parentsUntilFirstComEquipmentA; 
                        if(wsBean.isSubclassOf(logicalCircuitDetails.getEndpointA().getClassName(), Constants.CLASS_GENERICLOGICALPORT, ipAddress, sessionId)){
                            List<RemoteObjectLight> parentsUntilFirstPhysicalPortA = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericPhysicalPort", ipAddress, sessionId);

                            parentsUntilFirstComEquipmentA = wsBean.getParentsUntilFirstOfClass(parentsUntilFirstPhysicalPortA.get(0).
                                getClassName(), parentsUntilFirstPhysicalPortA.get(0).getId(), "GenericCommunicationsElement", ipAddress, sessionId);
                        }
                        else
                            parentsUntilFirstComEquipmentA = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericCommunicationsElement", ipAddress, sessionId);


                        RemoteObjectLight aSideEquipmentLogical = parentsUntilFirstComEquipmentA.get(parentsUntilFirstComEquipmentA.size() - 1);
                        SrvNodeWidget aSideEquipmentLogicalWidget = attachNodeWidget(aSideEquipmentLogical);

                        //Now the other side
                        List<RemoteObjectLight> parentsUntilFirstComEquipmentB;
                        if(wsBean.isSubclassOf(logicalCircuitDetails.getEndpointB().getClassName(), Constants.CLASS_GENERICLOGICALPORT, ipAddress, sessionId)){
                             List<RemoteObjectLight> parentsUntilFirstPhysicalPortB = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointA().
                                getClassName(), logicalCircuitDetails.getEndpointA().getId(), "GenericPhysicalPort", ipAddress, sessionId);

                            parentsUntilFirstComEquipmentB = wsBean.getParentsUntilFirstOfClass(parentsUntilFirstPhysicalPortB.get(0).
                                getClassName(), parentsUntilFirstPhysicalPortB.get(0).getId(), "GenericCommunicationsElement", ipAddress, sessionId);
                        }
                        else
                            parentsUntilFirstComEquipmentB = wsBean.getParentsUntilFirstOfClass(logicalCircuitDetails.getEndpointB().
                                getClassName(), logicalCircuitDetails.getEndpointB().getId(), "GenericCommunicationsElement", ipAddress, sessionId);

                        RemoteObjectLight bSideEquipmentLogical = parentsUntilFirstComEquipmentB.get(parentsUntilFirstComEquipmentB.size() - 1);
                        SrvNodeWidget bSideEquipmentLogicalWidget = attachNodeWidget(bSideEquipmentLogical);

                        //Now the logical link
                        SrvEdgeWidget logicalLinkWidget = attachEdgeWidget(logicalCircuitDetails.getConnectionObject(), 
                                aSideEquipmentLogicalWidget, 
                                bSideEquipmentLogicalWidget);
                        
                        logicalLinkWidget.setCaption(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName());

                        //Now we render the physical part
                        //We start with the A side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) {
                            int i = 2;
                            RemoteObjectLight lastAddedASidePhysicalEquipment = null;
                            if (wsBean.isSubclassOf(logicalCircuitDetails.getPhysicalPathForEndpointA().get(0).getClassName(), 
                                    Constants.CLASS_GENERICLOGICALPORT, ipAddress, sessionId))
                                i = 3;
                            
                            for(int index = i; index < logicalCircuitDetails.getPhysicalPathForEndpointA().size(); index += 3){
                                RemoteObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointA().get(index);
                                //If the physical equipment is not a subclass of GenericCommunicationsElement, nothing will be shown.
                                RemoteObjectLight aSidePhysicalEquipment = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                        nextPhysicalHop.getId(), "ConfigurationItem", ipAddress, sessionId);
                                
                                if(aSidePhysicalEquipment != null && !aSidePhysicalEquipment.getClassName().equals("ODF"))
                                    aSidePhysicalEquipment = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                            nextPhysicalHop.getId(), "GenericCommunicationsElement", ipAddress, sessionId);

                                if(aSidePhysicalEquipment == null)
                                    Notifications.showError("No communications equipment was found for this endpoint");
                                else {
                                    SrvNodeWidget aSideEquipmentPhysicalWidget = attachNodeWidget(aSidePhysicalEquipment);

                                    SrvEdgeWidget physicalLinkWidgetA = attachEdgeWidget( logicalCircuitDetails.getPhysicalPathForEndpointA().get(index - 1),
                                            findNodeWidget(index <= 3 ? aSideEquipmentLogical : lastAddedASidePhysicalEquipment), 
                                            aSideEquipmentPhysicalWidget
                                    );
                                    physicalLinkWidgetA.setCaption(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                        aSidePhysicalEquipment.getName() + ":" + nextPhysicalHop.getName());


                                    lastAddedASidePhysicalEquipment = aSidePhysicalEquipment;
                                }
                            }
                        }

                        //Now the b side
                        if (!logicalCircuitDetails.getPhysicalPathForEndpointB().isEmpty()) {
                            int i = 2;
                            RemoteObjectLight lastAddedBSideEquipmentPhysical = null;
                            if (wsBean.isSubclassOf(logicalCircuitDetails.getPhysicalPathForEndpointB().get(0).getClassName(), 
                                    Constants.CLASS_GENERICLOGICALPORT, ipAddress, sessionId))
                                i = 3;
                            for(int index = i; index < logicalCircuitDetails.getPhysicalPathForEndpointB().size(); index += 3){
                                RemoteObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointB().get(index);
                                RemoteObjectLight bSideEquipmentPhysical = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                        nextPhysicalHop.getId(), "ConfigurationItem", ipAddress, sessionId);
                                if(bSideEquipmentPhysical != null && !bSideEquipmentPhysical.getClassName().equals("ODF"))
                                    bSideEquipmentPhysical = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), nextPhysicalHop.getId(), 
                                            "GenericCommunicationsElement", ipAddress, sessionId);
                                //If the equipemt physical is not a subclass of GenericCommunicationsElement, nothing will be shown.
                                if(bSideEquipmentPhysical == null)
                                    Notifications.showError("No communications equipment was found for this endpoint");

                                else {
                                    SrvNodeWidget bSideEquipmentPhysicalWidget = attachNodeWidget(bSideEquipmentPhysical);
                                     
                                    SrvEdgeWidget physicalLinkWidgetB = attachEdgeWidget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(index - 1),
                                            findNodeWidget(index <=3 ? bSideEquipmentLogical : lastAddedBSideEquipmentPhysical),
                                            bSideEquipmentPhysicalWidget
                                    );

                                    physicalLinkWidgetB.setCaption(bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName() + " ** " +
                                        bSideEquipmentPhysical.getName() + ":" + nextPhysicalHop.getName());
                                    
                                    lastAddedBSideEquipmentPhysical = bSideEquipmentPhysical;
                                }
                            }
                        }
                    }
                }
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
        SrvNodeWidget newNode = new SrvNodeWidget(node.getId());
        lienzoComponent.addNodeWidget(newNode);
            
        newNode.setUrlIcon("/icons/" + node.getClassName().toLowerCase() + ".png");

        newNode.setHeight(32);
        newNode.setWidth(32);
        newNode.setCaption(node.toString());
        newNode.setX(nodes.size() * 200);
        newNode.setY((nodes.size() % 2) * 200 );
        nodes.put(node, newNode);
        return newNode;
    }
    
    protected SrvEdgeWidget attachEdgeWidget(RemoteObjectLight edge, SrvNodeWidget sourceNode, SrvNodeWidget targetNode) {
        try {
            SrvEdgeWidget newEdge = new SrvEdgeWidget(edge.getId());
            newEdge.setSource(sourceNode);
            newEdge.setTarget(targetNode);
            lienzoComponent.addEdgeWidget(newEdge);
            RemoteClassMetadata classMetadata = wsBean.getClass(edge.getClassName(), ipAddress, sessionId);
            newEdge.setColor(toHexString(new Color(classMetadata.getColor())));
            newEdge.setCaption(edge.toString());
            edges.put(edge, newEdge);
            return newEdge; 
        } catch (ServerSideException ex) {
            return new SrvEdgeWidget(323927373);
        }
    }
    
    public SrvNodeWidget findNodeWidget(long nodeId) {
        for (RemoteObjectLight aNode : nodes.keySet()) {
            if (aNode.getId() == nodeId)
                return nodes.get(aNode);
        }
        return null;
    }
    
    public SrvNodeWidget findNodeWidget(RemoteObjectLight node) {
        return nodes.get(node);
    }
    
    public SrvEdgeWidget findEdgeWidget(long edgeId) {
        for (RemoteObjectLight anEdge : edges.keySet()) {
            if (anEdge.getId() == edgeId)
                return edges.get(anEdge);
        }
        
        return null;
    }
    
    public SrvEdgeWidget findEdgeWidget(RemoteObjectLight edge) {
        return edges.get(edge);
    }
    
    public static String toHexString(Color c) {
        StringBuilder sb = new StringBuilder("#");

        if (c.getRed() < 16) sb.append('0');
        sb.append(Integer.toHexString(c.getRed()));

        if (c.getGreen() < 16) sb.append('0');
        sb.append(Integer.toHexString(c.getGreen()));

        if (c.getBlue() < 16) sb.append('0');
        sb.append(Integer.toHexString(c.getBlue()));

        return sb.toString();
    }
    
    public RemoteObjectLight findEdge(long id){
        for (RemoteObjectLight edge : edges.keySet()) {
            if(edge.getId() == id)
                return edge;
        }
        return null;
    }
  
}
