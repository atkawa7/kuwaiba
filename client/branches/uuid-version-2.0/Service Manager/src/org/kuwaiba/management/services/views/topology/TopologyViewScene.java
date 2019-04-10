/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
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
import org.inventory.core.services.i18n.I18N;
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
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class TopologyViewScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
    
    protected static final String VIEW_CLASS = "TopologyView"; //NOI18N
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
        
        getActions().addAction(ActionFactory.createZoomAction());
        getInputBindings().setZoomActionModifiers(0); //No keystroke combinations
        getActions().addAction(ActionFactory.createPanAction());
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
            xmlew.add(xmlef.createAttribute(new QName("version"), Constants.TOPOLOGYVIEW_FORMAT_VERSION));
            
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
                xmlew.add(xmlef.createCharacters(lolNode.getId()));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                
                LocalObjectLight lolEdge = (LocalObjectLight) findObject(edgeWidget);
                ObjectConnectionWidget acwEdge = (ObjectConnectionWidget) edgeWidget;
                
                if (getEdgeSource(lolEdge) == null || getEdgeTarget(lolEdge) == null) //This connection is malformed because one of the endpoints does not exist
                    continue;                                                         //probably, it was moved to another parent
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                
                
                xmlew.add(xmlef.createAttribute(new QName("id"), lolEdge.getId()));
                xmlew.add(xmlef.createAttribute(new QName("class"), lolEdge.getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("asideid"), getEdgeSource(lolEdge).getId()));
                xmlew.add(xmlef.createAttribute(new QName("asideclass"), getEdgeSource(lolEdge).getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("bsideid"), getEdgeTarget(lolEdge).getId()));
                xmlew.add(xmlef.createAttribute(new QName("bsideclass"), getEdgeTarget(lolEdge).getClassName()));
                
                //Note that the edges will all be saved, whether they're STMX, physical connnections or ContainerLinks, 
                //The expanded STMX will be marked as such, but when the view is rendered, they will be invisible by default, 
                //while the ContainerLinks within will be displayed.
                xmlew.add(xmlef.createAttribute(new QName("expanded"), Boolean.toString(expandedTransportLinks.containsKey(lolEdge))));
                
                QName qnameControlpoints = new QName("controlpoints"); //NOI18N

                xmlew.add(xmlef.createStartElement(qnameControlpoints, null, null));

                QName qnameControlpoint = new QName("controlpoint"); //NOI18N
                for (Point point : acwEdge.getControlPoints()) {
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(point.x)));
                    xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(point.y)));
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                }

                xmlew.add(xmlef.createEndElement(qnameControlpoints, null));
                
                if (expandedTransportLinks.containsKey(lolEdge)) {
                    QName qnameExpandedTLs = new QName("expandedTransportLinks"); //NOI18N
                    
                    xmlew.add(xmlef.createStartElement(qnameExpandedTLs, null, null));
                    
                    QName qnameExpandedTL = new QName("expandedTransportLink"); //NOI18N
                    for (LocalObjectLight extandedTransportLink : expandedTransportLinks.get(lolEdge)) {
                        xmlew.add(xmlef.createStartElement(qnameExpandedTL, null, null));
                        
                        xmlew.add(xmlef.createAttribute(new QName("id"), extandedTransportLink.getId()));
                        xmlew.add(xmlef.createAttribute(new QName("class"), extandedTransportLink.getClassName()));
                        xmlew.add(xmlef.createAttribute(new QName("aside"), getEdgeSource(extandedTransportLink).getId()));
                        xmlew.add(xmlef.createAttribute(new QName("bside"), getEdgeTarget(extandedTransportLink).getId()));
                        
                        xmlew.add(xmlef.createEndElement(qnameExpandedTL, null));
                    }
                    
                    xmlew.add(xmlef.createEndElement(qnameExpandedTLs, null));
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


    //This method MUST be called after render(LocalObjectLight), since it assumes that the required nodes and connections already exist and it's only
    //necessary to set their position and control points
    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        //<editor-fold defaultstate="collapsed" desc="Uncomment this for debugging purposes. This outputs the XML view as a file">
//        try (FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/tview.xml")) {
//            fos.write(structure);
//        } catch(Exception e) { }
        //</editor-fold>
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N
            QName qControlPoints = new QName("controlpoints"); //NOI18N
            QName qExtendedTLs = new QName("extendedTransportLinks"); //NOI18N
            QName qExtendedTL = new QName("extendedTransportLink"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        String objectClass = reader.getAttributeValue(null, "class");

                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        String objectId = reader.getElementText();
                        
                        Widget dummyNodeWidget = findWidget(new LocalObjectLight(objectId, "", objectClass));
                        
                        if (dummyNodeWidget != null) { //If the node does not exist, it's ignored because that means that the resource is no longer associated to the service
                            dummyNodeWidget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                            dummyNodeWidget.setBackground(com.getMetaForClass(objectClass, false).getColor());
                            revalidate();
                        } else
                            NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, String.format("Node with id %s could not be found. Save the view to keep the changes", objectId));
                    }else {
                        if (reader.getName().equals(qEdge)) {
                            String objectId = reader.getAttributeValue(null, "id"); //NOI18N
                            String objectClass = reader.getAttributeValue(null, "class"); //NOI18N
                            String expanded = reader.getAttributeValue("", "expanded"); //NOI18N

                            ObjectConnectionWidget connectionWidget = (ObjectConnectionWidget)findWidget(new LocalObjectLight(objectId, "", objectClass));

                            if (connectionWidget != null) { //if the connection exists, update its control points
                                reader.nextTag();
                                
                                if (reader.getName().equals(qControlPoints)) {
                                    List<Point> localControlPoints = new ArrayList<>();
                                    while(true) {
                                        reader.nextTag();

                                        if (reader.getName().equals(qControlPoint)) {
                                            if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                                localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                        } else {
                                            connectionWidget.setControlPoints(localControlPoints, true);
                                            revalidate();
                                            break;
                                        }
                                    }
                                }
                                                                
                                //If the current connection is an expanded STMX, make it invisible and trigger an expansion
                                if (expanded != null && expanded.equals("true")) {
                                    expandTransportLinks(Arrays.asList(connectionWidget.getLookup().lookup(LocalObjectLight.class)));
                                    reader.nextTag();
                                    if (reader.getName().equals(qExtendedTLs)) {
                                        while(true) {
                                            reader.nextTag();

                                            if (reader.getName().equals(qExtendedTL)) {
                                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                                    String expandedLinkId = reader.getAttributeValue(null, "id"); //NOI18N
                                                    String expandedLinkClass = reader.getAttributeValue(null, "class"); //NOI18N
                                                    
                                                    ObjectConnectionWidget expandedConnectionWidget = 
                                                            (ObjectConnectionWidget)findWidget(new LocalObjectLight(expandedLinkId, "", expandedLinkClass));
                                                    
                                                    if (expandedConnectionWidget != null) {
                                                        //Set the control points here
                                                    }
                                                }
                                            } else 
                                                break;
                                        }
                                    }
                                }
                                
                            } else
                                NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, String.format("The connection with id %s could not be found. Save the view to keep the changes", objectId));
                        }
                        else  {
                            //More tags
                        }
                    }
                }
            }
        } catch (XMLStreamException ex) {
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_INFO)
                Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void render(LocalObjectLight service) {
        List<LocalObjectLight> serviceResources = com.getServiceResources(service.getClassName(), service.getId());
        if (serviceResources == null)
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        else {
            Map<String, LocalObjectLight> portsInDevice = new HashMap<>();
            //We will ignore all resources that are not GenericCommunicationsElement
            for (LocalObjectLight serviceResource : serviceResources) {
                if (com.isSubclassOf(serviceResource.getClassName(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT)) {
                    if (findWidget(serviceResource) == null)
                        addNode(serviceResource);
                    
                    List<LocalObjectLight> physicalPorts = com.getChildrenOfClassLightRecursive(serviceResource.getId(), serviceResource.getClassName(), "GenericPhysicalPort");
                    if (physicalPorts == null) 
                        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                    else {
                        for (LocalObjectLight physicalPort : physicalPorts)
                            portsInDevice.put(physicalPort.getId(), serviceResource);
                    }
                }
            }
            
            
            //Once the nodes have been added, we retrieve the physical and logical (STMX) connections between them and ignore those that end in other elements
            for (LocalObjectLight aNode : getNodes()) {
                List<LocalObjectLightList> physicalConnections = com.getPhysicalConnectionsInObject(aNode.getClassName(), aNode.getId());
                List<LocalObjectLight> logicalConnections = com.getSpecialAttribute(aNode.getClassName(), aNode.getId(), "sdhTransportLink"); //NOI18N
                
                if (physicalConnections == null || logicalConnections == null) 
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                else {
                    //First the physical connections
                    for (LocalObjectLightList aConnection : physicalConnections) {
                        LocalObjectLight sourcePort = aConnection.get(0);
                        LocalObjectLight targetPort = aConnection.get(aConnection.size() - 1);
                        
                        LocalObjectLight sourceEquipment = aNode;
                        LocalObjectLight targetEquipment = portsInDevice.get(targetPort.getId());

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
                        ObjectConnectionWidget logicalConnectionWidget = (ObjectConnectionWidget)findWidget(aConnection);
                        if (logicalConnectionWidget == null) {
                            addEdge(aConnection);
                            setEdgeSource(aConnection, aNode);
                        } else
                            setEdgeTarget(aConnection, aNode);
                    }
                    
                }
            }
            //Now we delete the connections to elements that are not in the view (they will only have a source, not a target widget). Granted, this is a reprocess, but I prefer and save a few
            //calls to the server doing this at client-side only
            for (LocalObjectLight aConnection : new ArrayList<>(getEdges())) {
                if (getEdgeTarget(aConnection) == null)
                    removeEdge(aConnection);
            }
        }
        validate();
        repaint();
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
        ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge, ObjectConnectionWidget.LINE);
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

    @Override
    public void clear() {
        super.clear(); 
        expandedTransportLinks.clear();
    }
    
    

    public HashMap<LocalObjectLight, List<LocalObjectLight>> getExpandedTransportLinks() {
        return expandedTransportLinks;
    }
    
    public void expandTransportLinks(List<LocalObjectLight> transportLinks) {
        for (LocalObjectLight selectedObject : transportLinks) {
            LocalObjectLight castedTransportLink = (LocalObjectLight)selectedObject;
            if (!CommunicationsStub.getInstance().isSubclassOf(castedTransportLink.getClassName(), "GenericSDHTransportLink")) {
                JOptionPane.showMessageDialog(null, String.format("%s is not a transport link", castedTransportLink), 
                        I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
                continue;
            }
            
            
            List<LocalObjectLight> containerLinks = CommunicationsStub.getInstance().
                    getSpecialAttribute(castedTransportLink.getClassName(), castedTransportLink.getId(), "sdhTransports"); //NOI18N
            
            if (containerLinks != null) {
                if (!containerLinks.isEmpty()) {
                    getExpandedTransportLinks().put(castedTransportLink, containerLinks); //This map will be used to gracefully collapse all the STMX that will be expanded in this operation

                    for (LocalObjectLight containerLink : containerLinks) {
                        if (findWidget(containerLink) == null) { //This validation should not be necessary, but just in case
                            addEdge(containerLink);
                            setEdgeSource(containerLink, getEdgeSource(castedTransportLink));
                            setEdgeTarget(containerLink, getEdgeTarget(castedTransportLink));
                        }
                    }
                    //The STMX is only set invisible if it transports something
                    findWidget(castedTransportLink).setVisible(false);
                    validate();
                } else
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                            NotificationUtil.ERROR_MESSAGE, "The selected STM is not transporting any container"); //NOI18N
                    
            } else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError()); //NOI18N
            
        }
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
