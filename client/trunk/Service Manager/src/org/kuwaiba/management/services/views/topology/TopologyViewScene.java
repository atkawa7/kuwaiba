/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.management.services.views.topology;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPopupMenu;
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
import org.inventory.communications.core.LocalObjectLightList;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.kuwaiba.management.services.views.topology.actions.DisaggregateTransportLinkAction;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * This scene renders a view where the communications equipment associated 
 * directly to a service and the physical connections between them are 
 * displayed in a topology fashion
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TopologyViewScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
    
    protected static final String VIEW_CLASS = "ServiceTopologyView"; //NOI18N
    /**
     * A map that contains the expanded transport links and their container links, so they can be collapsed when the user needs it (see {@link #expandTransportLinks() } and {@link #collapseTransportLinks() })
     */
    private HashMap<LocalObjectLight, List<LocalObjectLight>> expandedTransportLinks;
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    /**
     * Default move widget action (shared by all connection widgets)
     */
    private CustomMoveAction moveAction = new CustomMoveAction(this);
    /**
     * Default control point move action (shared by all connection widgets)
     */
    private CustomMoveControlPointAction moveControlPointAction =
            new CustomMoveControlPointAction(this);
    /**
     * Default add/remove control point action (shared by all connection widgets)
     */
    private CustomAddRemoveControlPointAction addRemoveControlPointAction =
            new CustomAddRemoveControlPointAction(this);
    /**
     * The disaggregate action instance used in all connection widget objects
     */
    private DisaggregateTransportLinkAction disaggregateTransportLinkAction = new DisaggregateTransportLinkAction(this);
    /**
     *  A simple pop menu provider for connection widgets
     */
    private PopupMenuProvider popupMenuProviderForConnections = new EdgePopupMenuProvider();
    
    public TopologyViewScene() {
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        
        addChild(edgeLayer);
        addChild(nodeLayer);
        
        initSelectionListener();
        
        expandedTransportLinks = new HashMap<>();
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
            xmlew.add(xmlef.createAttribute(new QName("version"), Constants.VIEW_FORMAT_VERSION));
            
            QName qnameClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters(VIEW_CLASS));
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            
            for (Widget nodeWidget : nodeLayer.getChildren()) {
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(nodeWidget.getPreferredLocation().x)));
                xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(nodeWidget.getPreferredLocation().y)));
                LocalObjectLight lolNode = (LocalObjectLight) findObject(nodeWidget);
                xmlew.add(xmlef.createAttribute(new QName("class"), lolNode.getClassName()));
                xmlew.add(xmlef.createCharacters(Long.toString(lolNode.getOid())));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                //Ignore the expanded edges, that is the container links thet were created during the disaggregation of a transporlink
                if (isExpandedEdge((LocalObjectLight)findObject(edgeWidget)))
                    continue;
                
                LocalObjectLight lolEdge = (LocalObjectLight) findObject(edgeWidget);
                ObjectConnectionWidget acwEdge = (ObjectConnectionWidget) edgeWidget;
                
                if (getEdgeSource(lolEdge) == null || getEdgeTarget(lolEdge) == null) //This connection is malformed because one of the endpoints does not exist
                    continue;                                                         //probably, it was moved to another parent
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                
                
                xmlew.add(xmlef.createAttribute(new QName("id"), Long.toString(lolEdge.getOid())));
                xmlew.add(xmlef.createAttribute(new QName("class"), lolEdge.getClassName()));
                
                xmlew.add(xmlef.createAttribute(new QName("aside"), Long.toString(getEdgeSource(lolEdge).getOid())));
                xmlew.add(xmlef.createAttribute(new QName("bside"), Long.toString(getEdgeTarget(lolEdge).getOid())));
                
                for (Point point : acwEdge.getControlPoints()) {
                    QName qnameControlpoint = new QName("controlpoint");
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(point.x)));
                    xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(point.y)));
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                }
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            }
            xmlew.add(xmlef.createEndElement(qnameEdges, null));
            // polygons
            QName qnamePolygons = new QName("polygons");
            xmlew.add(xmlef.createStartElement(qnamePolygons, null, null));
            //Add frames if necessary
            xmlew.add(xmlef.createEndElement(qnamePolygons, null));
            
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
//        try (FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/oview_" + VIEW_CLASS + ".xml")) {
//            fos.write(structure);
//        } catch(Exception e) { }
        //</editor-fold>
        try {
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

                        LocalObjectLight lol = CommunicationsStub.getInstance().getObjectInfoLight(objectClass, objectId);
                        if (lol != null) {
                            if (getNodes().contains(lol)) //The node in the saved view already exists in the canvas, so either is duplicated or the view was not cleared previously 
                                NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, "The view seems to be corrupted. Self-healing measures were taken");
                            else {
                                Widget widget = addNode(lol);
                                widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                                widget.setBackground(com.getMetaForClass(objectClass, false).getColor());
                                validate();
                            }
                        } //In case of error, ignore the node
                    }else {
                        if (reader.getName().equals(qEdge)) {
                            long objectId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N

                            long aSide = Long.valueOf(reader.getAttributeValue(null, "aside")); //NOI18N
                            long bSide = Long.valueOf(reader.getAttributeValue(null, "bside")); //NOI18N

                            String className = reader.getAttributeValue(null, "class"); //NOI18N

                            LocalObjectLight container = com.getObjectInfoLight(className, objectId);

                            if (container != null) { // if the connection exist
                                LocalObjectLight aSideObject = new LocalObjectLight(aSide, null, null);
                                ObjectNodeWidget aSideWidget = (ObjectNodeWidget) findWidget(aSideObject);

                                LocalObjectLight bSideObject = new LocalObjectLight(bSide, null, null);
                                ObjectNodeWidget bSideWidget = (ObjectNodeWidget) findWidget(bSideObject);

                                
                                if (aSideWidget != null && bSideWidget != null) {//If one of the endpoints is missing, don't render the connection

                                    if (getEdges().contains(container))
                                        NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, "The view seems to be corrupted. Self-healing measures were taken");
                                    else {
                                        ObjectConnectionWidget newEdge = (ObjectConnectionWidget) addEdge(container);
                                        setEdgeSource(container, aSideObject);
                                        setEdgeTarget(container, bSideObject);
                                        
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
                        // FREE FRAMES
                        else if (reader.getName().equals(qPolygon)) { /*Nothing for now*/ }//end qPolygon

                        else {
                            if (reader.getName().equals(qLabel)) {
                                //Unavailable for now
                            } else {
                                if (reader.getName().equals(qZoom))
                                    setZoomFactor(Integer.valueOf(reader.getText()));
                                else {
                                    if (reader.getName().equals(qCenter)) {
                                        double x = Double.valueOf(reader.getAttributeValue(null, "x"));
                                        double y = Double.valueOf(reader.getAttributeValue(null, "y"));
                                    } else {
                                        //Place more tags
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (XMLStreamException ex) {
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_INFO)
                Exceptions.printStackTrace(ex);
        }
        validate();
        repaint();
        
    }

    @Override
    public void render(LocalObjectLight service) {
        List<LocalObjectLight> serviceResources = com.getServiceResources(service.getClassName(), service.getOid());
        if (serviceResources == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            List<LocalObjectLight> nodesToBeDeleted = new ArrayList<>(getNodes()); 
            //We clone the existing nodes to synchronize the view, so saved nodes that are no longer listed as service resources are removed
            //We assume that render(byte[]) was called before calling render(LocalObjectLight)
            Map<Long, LocalObjectLight> equipmentByPort = new HashMap<>();
            //We will ignore all resources that are not GenericCommunicationsElement
            for (LocalObjectLight serviceResource : serviceResources) {
                if (com.isSubclassOf(serviceResource.getClassName(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT)) {
                    if (findWidget(serviceResource) == null)
                        addNode(serviceResource);
                    
                    nodesToBeDeleted.remove(serviceResource);
                    
                    List<LocalObjectLight> physicalPorts = com.getChildrenOfClassLightRecursive(serviceResource.getOid(), serviceResource.getClassName(), "GenericPhysicalPort");
                    if (physicalPorts == null) 
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());

                    for (LocalObjectLight physicalPort : physicalPorts)
                        equipmentByPort.put(physicalPort.getOid(), serviceResource);
                }
            }
            
            for (LocalObjectLight nodeToBeDeleted : nodesToBeDeleted) {
                removeNodeWithEdges(nodeToBeDeleted);
                validate();
            }
            
            //Once the nodes have been added, we retrieve the physical and logical (STMX) connections between them and ignore those that end in other elements
            for (LocalObjectLight aNode : getNodes()) {
                List<LocalObjectLightList> physicalConnections = com.getPhysicalConnectionsInObject(aNode.getClassName(), aNode.getOid());
                List<LocalObjectLight> logicalConnections = com.getSpecialAttribute(aNode.getClassName(), aNode.getOid(), "sdhTransportLink"); //NOI18N
                
                if (physicalConnections == null || logicalConnections == null) 
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                else {
                    //First the physical connections
                    for (LocalObjectLightList aConnection : physicalConnections) {
                        LocalObjectLight sourcePort = aConnection.get(0);
                        LocalObjectLight targetPort = aConnection.get(aConnection.size() - 1);
                        
                        LocalObjectLight sourceEquipment = aNode;
                        LocalObjectLight targetEquipment = equipmentByPort.get(targetPort.getOid());

                        if (findWidget(targetEquipment) != null) {
                            
                            ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget)findWidget(aConnection.get(1));
                            if (connectionWidget == null) 
                                connectionWidget = (ObjectConnectionWidget)findWidget(aConnection.get(aConnection.size() - 2));

                            if (connectionWidget == null) {
                                connectionWidget = (ObjectConnectionWidget)addEdge(aConnection.get(1));
                                setEdgeSource(aConnection.get(1), sourceEquipment);
                                setEdgeTarget(aConnection.get(1), targetEquipment);
                            }
                            
                            connectionWidget.getLabelWidget().setLabel(sourceEquipment.getName() + ":" + sourcePort.getName() + 
                                        " ** " +targetEquipment.getName() + ":" + targetPort.getName());
                        } //Else, we just ignore this connection trace
                    }
                    
                    //Now the logical connections
                    for (LocalObjectLight aConnection : logicalConnections) {
                        ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget)findWidget(aConnection);
                        if (connectionWidget == null) { //The connection hasn't already added, so we add it and set the source anchor
                            addEdge(aConnection);
                            setEdgeSource(aConnection, aNode);
                        } else { //The connection already exists, check the endpoints and connect whatever is left to connect
                            if (getEdgeTarget(aConnection) == null)
                                setEdgeTarget(aConnection, aNode);
                        }
                    }
                }
            }            
            //Now we delete the connections to elements that are not in the view. Granted, this is a reprocess, but I prefer and save a few
            //calls to the server doing this at client-side only
            for (LocalObjectLight aConnection : new ArrayList<>(getEdges())) {
                if (getEdgeTarget(aConnection) == null)
                    removeEdge(aConnection);
            }
        }
    }
    
    @Override
    public ConnectProvider getConnectProvider() { return null; }

    @Override
    public boolean supportsConnections() { return true; }

    @Override
    public boolean supportsBackgrounds() { return false; }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
        Widget newWidget;
        if (classMetadata != null)
            newWidget = new ObjectNodeWidget(this, node, classMetadata.getIcon());
        else //Should not happen
            newWidget = new ObjectNodeWidget(this, node);

        newWidget.getActions().addAction(createSelectAction());
        newWidget.getActions().addAction(moveAction);
        newWidget.setPreferredLocation(new Point(nodeLayer.getChildren().size() * 200, (nodeLayer.getChildren().size() % 2) * 200 ));
        nodeLayer.addChild(newWidget);
        
        validate();
        return newWidget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge);
        newWidget.getActions().addAction(createSelectAction());
        newWidget.getActions().addAction(moveControlPointAction);
        newWidget.getActions().addAction(addRemoveControlPointAction);
        newWidget.getActions().addAction(ActionFactory.createPopupMenuAction(popupMenuProviderForConnections));
        newWidget.setRouter(RouterFactory.createFreeRouter());
        newWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
        if (classMetadata != null)
            newWidget.setLineColor(classMetadata.getColor());
        
        edgeLayer.addChild(newWidget);
        validate();
        return newWidget;
    }

    public HashMap<LocalObjectLight, List<LocalObjectLight>> getExpandedTransportLinks() {
        return expandedTransportLinks;
    }
    
    /**
     * Tells if a given object is a container link product of expanding (a.k.a. disaggregating) a transport link (STMX)
     * @param edge An object representing the container link
     * @return A boolean saying if the edge is an expanded edge or not
     */
    private boolean isExpandedEdge(LocalObjectLight edge) {
        for (LocalObjectLight expandedTransportLink : expandedTransportLinks.keySet()) {
            if (expandedTransportLinks.get(expandedTransportLink).contains(edge))
                return true;
        }
        return false;
    }
    
    /**
     * A simple pop menu provider for connection widgets
     */
    private class EdgePopupMenuProvider implements PopupMenuProvider {
                
        @Override
            public JPopupMenu getPopupMenu(Widget arg0, Point arg1) {
                JPopupMenu mnuActions = new JPopupMenu();
                mnuActions.add(disaggregateTransportLinkAction);
                return mnuActions;
            }
    }
}
