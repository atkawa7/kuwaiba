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

package org.kuwaiba.web.modules.services.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteLogicalConnectionDetails;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.services.persistence.util.Constants;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.api.visual.widget.general.IconNodeWidget;

/**
 * Shows an end-to-end view of a service by trying to match the endpoints of the logical circuits
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different: It's intended to deal exclusively 
 * with logical connections, though it uses the physical path to fill the gaps between different logical segments
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EndToEndViewSimpleScene extends GraphScene<RemoteObjectLight, RemoteObjectLight> {
    
    public final static String VIEW_CLASS = "ServiceSimpleView"; 
    
    private final LayerWidget nodeLayer;
    private final LayerWidget edgeLayer;
    
    private WebserviceBeanLocal wsBean;
    private String ipAddress;
    private String sessionId;
    
    public EndToEndViewSimpleScene(WebserviceBeanLocal wsBean, String sessionId, String ipAddress) {
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        
        addChild(edgeLayer);
        addChild(nodeLayer); 
        
        this.sessionId = sessionId;
        this.ipAddress = ipAddress;
        this.wsBean = wsBean;
    }
    
    public void render(byte[] structure) throws IllegalArgumentException { 
       //<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
       // try {
       //     FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/oview_.xml");
       //     fos.write(structure);
       //     fos.close();
       // } catch(Exception e) {}
        //</editor-fold>
        try{
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qZoom = new QName("zoom"); //NOI18N
            QName qCenter = new QName("center"); //NOI18N
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qLabel = new QName("label"); //NOI18N
            QName qPolygon = new QName("polygon"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        String objectClass = reader.getAttributeValue(null, "class");

                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        long objectId = Long.valueOf(reader.getElementText());

                        RemoteObjectLight lol = wsBean.getObjectLight(objectClass, objectId, ipAddress, sessionId);
                        if (lol != null) {
                            if (getNodes().contains(lol))
                                NotificationsUtil.showError("The view seems to be corrupted. Self-healing measures were taken");
                            else {
                                Widget widget = addNode(lol);
                                widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                                widget.setBackground(new Color(wsBean.getClass(objectClass, ipAddress, sessionId).getColor()));
                                validate();
                            }
                        }
                    }else {
                        if (reader.getName().equals(qEdge)) {
                            long objectId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N

                            long aSide = Long.valueOf(reader.getAttributeValue(null, "aside")); //NOI18N
                            long bSide = Long.valueOf(reader.getAttributeValue(null, "bside")); //NOI18N

                            String className = reader.getAttributeValue(null,"class"); //NOI18N

                            RemoteObjectLight container = wsBean.getObjectLight(className, objectId, ipAddress, sessionId);

                            if (container != null) { // if the connection exists
                                RemoteObjectLight aSideObject = new RemoteObjectLight(aSide, null, null);
                                IconNodeWidget aSideWidget = (IconNodeWidget) findWidget(aSideObject);

                                RemoteObjectLight bSideObject = new RemoteObjectLight(bSide, null, null);
                                IconNodeWidget bSideWidget = (IconNodeWidget) findWidget(bSideObject);

                                if (aSideWidget != null && bSideWidget != null) {//If one of the endpoints is missing, don't render the connection

                                    if (getEdges().contains(container))
                                        NotificationsUtil.showError("The view seems to be corrupted. Self-healing measures were taken");
                                    else {
                                        ObjectConnectionWidget newEdge = (ObjectConnectionWidget) addEdge(container);
                                        setEdgeSource(container, aSideWidget.getLookup().lookup(RemoteObjectLight.class));
                                        setEdgeTarget(container, bSideWidget.getLookup().lookup(RemoteObjectLight.class));
                                        
                                        List<Point> localControlPoints = new ArrayList<>();
                                        while(true) {
                                            reader.nextTag();

                                            if (reader.getName().equals(qControlPoint)) {
                                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                                    localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                            } else {
                                                newEdge.setControlPoints(localControlPoints,false);
                                                break;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (XMLStreamException | ServerSideException ex) {
            NotificationsUtil.showError(ex.getMessage());
        }
        validate();
        repaint();
    }

    public void render(RemoteObjectLight selectedService) {
        try {
        List<RemoteObjectLight> serviceResources = wsBean.getServiceResources(selectedService.getClassName(), selectedService.getId(), ipAddress, sessionId);
        
        List<RemoteObjectLight> nodesToBeDeleted = new ArrayList<>(getNodes()); 

            for (RemoteObjectLight serviceResource : serviceResources) {
                if (wsBean.isSubclassOf(serviceResource.getClassName(), "GenericLogicalConnection", ipAddress, sessionId)) {
                    RemoteLogicalConnectionDetails logicalCircuitDetails = wsBean.getLogicalLinkDetails(
                            serviceResource.getClassName(), serviceResource.getId(), ipAddress, sessionId);
                    //Let's create the boxes corresponding to the endpoint A of the logical circuit
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

                    if (findWidget(aSideEquipmentLogical) != null)
                        nodesToBeDeleted.remove(aSideEquipmentLogical);
                    else
                        addNode(aSideEquipmentLogical);

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

                    if (findWidget(bSideEquipmentLogical) != null)
                        nodesToBeDeleted.remove(bSideEquipmentLogical);
                    else
                        addNode(bSideEquipmentLogical);

                    //Now the logical link
                    ObjectConnectionWidget logicalLinkWidget = (ObjectConnectionWidget) findWidget(logicalCircuitDetails.getConnectionObject());
                    if(logicalLinkWidget == null){
                        logicalLinkWidget = (ObjectConnectionWidget) addEdge(logicalCircuitDetails.getConnectionObject());

                        logicalLinkWidget.getLabelWidget().setLabel(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName());
                        setEdgeSource(logicalCircuitDetails.getConnectionObject(), aSideEquipmentLogical);
                        setEdgeTarget(logicalCircuitDetails.getConnectionObject(), bSideEquipmentLogical);
                    }
                    //Now with render the physical part
                    //We start with the A side
                    if (!logicalCircuitDetails.getPhysicalPathForEndpointA().isEmpty()) {
                        int i = 2;
                        RemoteObjectLight lastAddedASideEquipmentPhysical = null;
                        if (wsBean.isSubclassOf(logicalCircuitDetails.getPhysicalPathForEndpointA().get(0).getClassName(), 
                                Constants.CLASS_GENERICLOGICALPORT, ipAddress, sessionId))
                            i = 3;
                        for(int index = i; index < logicalCircuitDetails.getPhysicalPathForEndpointA().size(); index += 3){
                            RemoteObjectLight nextPhysicalHop = logicalCircuitDetails.getPhysicalPathForEndpointA().get(index);
                            //If the equipemt physical is not a subclass of GenericCommunicationsElement, nothing will be shown.
                            RemoteObjectLight aSideEquipmentPhysical = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                    nextPhysicalHop.getId(), "ConfigurationItem", ipAddress, sessionId);
                            if(aSideEquipmentPhysical != null && !aSideEquipmentPhysical.getClassName().equals("ODF"))
                                aSideEquipmentPhysical = wsBean.getFirstParentOfClass(nextPhysicalHop.getClassName(), 
                                        nextPhysicalHop.getId(), "GenericCommunicationsElement", ipAddress, sessionId);

                            if(aSideEquipmentPhysical == null)
                                NotificationsUtil.showError("No communications equipment was found for this endpoint");
                            else{
                                if(findWidget(aSideEquipmentPhysical) != null)
                                    nodesToBeDeleted.remove(aSideEquipmentPhysical);
                                else
                                    addNode(aSideEquipmentPhysical);

                                if (findWidget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(index-1)) != null) 
                                    nodesToBeDeleted.remove(logicalCircuitDetails.getPhysicalPathForEndpointA().get(index-1));
                                else{ 
                                    ObjectConnectionWidget physicalLinkWidgetA = (ObjectConnectionWidget) findWidget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(index - 1));

                                    if(physicalLinkWidgetA == null)
                                        physicalLinkWidgetA = (ObjectConnectionWidget) addEdge(logicalCircuitDetails.getPhysicalPathForEndpointA().get(index - 1));

                                    physicalLinkWidgetA.getLabelWidget().setLabel(aSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointA().getName() + " ** " +
                                        aSideEquipmentPhysical.getName() + ":" + nextPhysicalHop.getName());

                                    setEdgeSource(logicalCircuitDetails.getPhysicalPathForEndpointA().get(index-1), index <= 3 ? aSideEquipmentLogical : lastAddedASideEquipmentPhysical);
                                    setEdgeTarget(logicalCircuitDetails.getPhysicalPathForEndpointA().get(index-1), aSideEquipmentPhysical);

                                    lastAddedASideEquipmentPhysical = aSideEquipmentPhysical;
                                }
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
                                NotificationsUtil.showError("No communications equipment was found for this endpoint");

                            else{
                                if (findWidget(bSideEquipmentPhysical) != null)
                                    nodesToBeDeleted.remove(bSideEquipmentPhysical);
                                else
                                    addNode(bSideEquipmentPhysical);

                                if (findWidget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(index-1)) != null) 
                                    nodesToBeDeleted.remove(logicalCircuitDetails.getPhysicalPathForEndpointB().get(index-1));
                                else{ 
                                    ObjectConnectionWidget physicalLinkWidgetB = (ObjectConnectionWidget) findWidget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(index-1));

                                    if(physicalLinkWidgetB == null)
                                        physicalLinkWidgetB = (ObjectConnectionWidget) addEdge(logicalCircuitDetails.getPhysicalPathForEndpointB().get(index - 1));

                                    physicalLinkWidgetB.getLabelWidget().setLabel(bSideEquipmentLogical.getName() + ":" + logicalCircuitDetails.getEndpointB().getName() + " ** " +
                                        bSideEquipmentPhysical.getName() + ":" + nextPhysicalHop.getName());

                                    setEdgeSource(logicalCircuitDetails.getPhysicalPathForEndpointB().get(index-1), index <=3 ? bSideEquipmentLogical : lastAddedBSideEquipmentPhysical);
                                    setEdgeTarget(logicalCircuitDetails.getPhysicalPathForEndpointB().get(index-1), bSideEquipmentPhysical);

                                    lastAddedBSideEquipmentPhysical = bSideEquipmentPhysical;
                                }
                            }
                        }
                    }
                }
            }
            for (RemoteObjectLight node : nodesToBeDeleted) 
                removeNodeWithEdges(node);
            
            validate();
            repaint();

        } catch (Exception ex) {
            clear();
            NotificationsUtil.showError(ex.getMessage());
        }
        
    }

     public void clear() {
        while (!getNodes().isEmpty())
            removeNode(getNodes().iterator().next());

        while (!getEdges().isEmpty())
            removeEdge(getEdges().iterator().next());
        
        validate();
        repaint();
    }

    @Override
    protected Widget attachNodeWidget(RemoteObjectLight node) {
        try {
            RemoteClassMetadata classMetadata = wsBean.getClass(node.getClassName(), ipAddress, sessionId);
            IconNodeWidget newWidget = new IconNodeWidget(this);
            if (classMetadata.getIcon() == null) {
                newWidget.setOpaque(true);
                newWidget.setBackground(new Color(classMetadata.getColor()));
                newWidget.setPreferredSize(new Dimension(40, 40));
            } else {
                try { 
                    BufferedImage classIcon = ImageIO.read(new ByteArrayInputStream(classMetadata.getIcon())); 
                    newWidget.setImage(classIcon);
                } catch (IOException ioe) {
                    newWidget.setOpaque(true);
                    newWidget.setBackground(new Color(classMetadata.getColor()));
                    newWidget.setPreferredSize(new Dimension(40, 40));
                }
            }
            newWidget.getLabelWidget().setLabel(node.toString());
            newWidget.setPreferredLocation(new Point(nodeLayer.getChildren().size() * 200, (nodeLayer.getChildren().size() % 2) * 200 ));
            nodeLayer.addChild(newWidget);
            validate();
            return newWidget;
        } catch (ServerSideException ex) {
            validate();
            return new IconNodeWidget(this);
        }
    }

    @Override
    protected Widget attachEdgeWidget(RemoteObjectLight edge) {
        try {
            ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge);
            newWidget.setRouter(RouterFactory.createFreeRouter());
            newWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
            newWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);

            RemoteClassMetadata classMetadata = wsBean.getClass(edge.getClassName(), ipAddress, sessionId);
            newWidget.setLineColor(new Color(classMetadata.getColor()));

            edgeLayer.addChild(newWidget);
            validate();
            return newWidget; 
        } catch (ServerSideException ex) {
            validate();
            return new ObjectConnectionWidget(this);
        }
    }
    
     @Override
    protected void attachEdgeSourceAnchor(RemoteObjectLight edge, RemoteObjectLight oldSourceNode, RemoteObjectLight newSourceNode) {
        ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget)findWidget(edge);
        Widget sourceWidget = findWidget(newSourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createCircularAnchor(sourceWidget, 3) : null);
    }

    @Override
    protected void attachEdgeTargetAnchor(RemoteObjectLight edge, RemoteObjectLight oldTargetNode, RemoteObjectLight newTargetNode) {
        ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget)findWidget(edge);
        Widget targetWidget = findWidget(newTargetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createCircularAnchor(targetWidget, 3) : null);
    }
}
