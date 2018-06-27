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
import com.vaadin.server.Page;
import com.vaadin.server.Resource;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.resources.ClassIcon;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;

/**
 * Shows an end-to-end view of a service by trying to match the endpoints of the logical circuits
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different: It's intended to deal exclusively 
 * with logical connections, though it uses the physical path to fill the gaps between different logical segments
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EndToEndViewScene extends VerticalLayout {
    
    public final static String VIEW_CLASS = "ServiceSimpleView"; 
    
    private LienzoComponent lienzoComponent;
    
    private HashMap<RemoteObjectLight, SrvNodeWidget> nodes;
    private HashMap<RemoteObjectLight, SrvEdgeWidget> edges;
    
    private WebserviceBean wsBean;
    private String ipAddress;
    private String sessionId;
    
    public EndToEndViewScene(WebserviceBean wsBean, String sessionId, String ipAddress) {
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.wsBean = wsBean;
        this.lienzoComponent = new LienzoComponent();
    }
    
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
                        SrvEdgeWidget logicalLinkWidget = attachEdgeWidget(logicalCircuitDetails.getConnectionObject());
                        logicalLinkWidget.setCaption(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName());

                        logicalLinkWidget.setSource(aSideEquipmentLogicalWidget);
                        logicalLinkWidget.setTarget(bSideEquipmentLogicalWidget);

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

                                    SrvEdgeWidget physicalLinkWidgetA = attachEdgeWidget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(index - 1));
                                    physicalLinkWidgetA.setCaption(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                        aSidePhysicalEquipment.getName() + ":" + nextPhysicalHop.getName());

                                    physicalLinkWidgetA.setSource(findNodeWidget(index <= 3 ? aSideEquipmentLogical : lastAddedASidePhysicalEquipment));
                                    physicalLinkWidgetA.setTarget(aSideEquipmentPhysicalWidget);

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
                                     
                                    SrvEdgeWidget physicalLinkWidgetB = attachEdgeWidget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(index - 1));

                                    physicalLinkWidgetB.setCaption(bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName() + " ** " +
                                        bSideEquipmentPhysical.getName() + ":" + nextPhysicalHop.getName());
                                    
                                    
                                    physicalLinkWidgetB.setSource(findNodeWidget(index <=3 ? bSideEquipmentLogical : lastAddedBSideEquipmentPhysical));
                                    physicalLinkWidgetB.setTarget(bSideEquipmentPhysicalWidget);

                                    lastAddedBSideEquipmentPhysical = bSideEquipmentPhysical;
                                    
                                }
                            }
                        }
                    }
                }
                addComponent(lienzoComponent);
            }
        } catch (Exception ex) {
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
        ClassIcon ci = new ClassIcon();
        try {
            
            newNode.setUrlIcon("/kuwaiba/VAADIN/themes/nuqui/img/node.png");
            RemoteClassMetadata classMetadata = wsBean.getClass(node.getClassName(), ipAddress, sessionId);
            
//            StreamResource resource = new StreamResource(
//            new StreamResource.StreamSource() {
//                private static final long serialVersionUID = 1L;
//
//                @Override
//                public InputStream getStream() {
//                    return new ByteArrayInputStream(classMetadata.getIcon());
//                }
//            }, classMetadata.getClassName());
//            
//            String protocol = Page.getCurrent().getLocation().getScheme();
//            String currentUrl = Page.getCurrent().getLocation().getAuthority();
//            String cid = this.getConnectorId();
//            Integer uiId = Page.getCurrent().getUI().getUIId();
//            String filename = resource.getFilename();
            
            
            //Resource icon = ci.getIcon(classMetadata);
            //ResourceReference rr = ResourceReference.create(resource, Page.getCurrent().getUI(), Long.toString(node.getId()));
            //newNode.setUrlIcon(protocol+"://"+currentUrl+"/APP/connector/"+uiId+"/"+cid+"/source/"+filename);
            newNode.setHeight(32);
            newNode.setWidth(32);
            newNode.setCaption(node.toString());
            newNode.setX(nodes.size() * 200);
            newNode.setY((nodes.size() % 2) * 200 );
            nodes.put(node, newNode);
            return newNode;
        } catch (ServerSideException ex) {
            return null;
        }
    }

    protected SrvEdgeWidget attachEdgeWidget(RemoteObjectLight edge) {
        try {
            SrvEdgeWidget newEdge = new SrvEdgeWidget(edge.getId());
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
}
