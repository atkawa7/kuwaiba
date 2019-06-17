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

package com.neotropic.inventory.modules.mpls.views;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
import org.inventory.communications.core.LocalMPLSConnectionDetails;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.actions.CustomAddRemoveControlPointAction;
import org.inventory.core.visual.actions.CustomMoveAction;
import org.inventory.core.visual.actions.CustomMoveControlPointAction;
import org.inventory.core.visual.actions.providers.CustomResizeProvider;
import org.inventory.core.visual.scene.AbstractScene;
import static org.inventory.core.visual.scene.AbstractScene.FREE_FRAME;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * Shows the endPoints of a Mplslink
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MplsLinkEndpointsViewScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
    public final static String VIEW_CLASS = "MPLSLinkEndpointsView"; 
    
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private final LayerWidget imagesLayer;
    private final LayerWidget framesLayer;
    private final CustomResizeProvider resizeProvider;
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
    
    private Random randomGenerator; 
    
    public MplsLinkEndpointsViewScene() {
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        imagesLayer = new LayerWidget(this);
        framesLayer = new LayerWidget(this);
        randomGenerator = new Random(1000); 
        
        addChild(framesLayer);
        addChild(imagesLayer);
        addChild(edgeLayer);
        addChild(nodeLayer);
        
        resizeProvider = new CustomResizeProvider(this);
        
        getActions().addAction(ActionFactory.createZoomAction());
        initSelectionListener();
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
                xmlew.add(xmlef.createCharacters(lolNode.getId()));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                LocalObjectLight lolEdge = (LocalObjectLight) findObject(edgeWidget);
                ObjectConnectionWidget acwEdge = (ObjectConnectionWidget) edgeWidget;
                if (acwEdge.getSourceAnchor() == null || acwEdge.getTargetAnchor() == null) //This connection is malformed because one of the endpoints does not exist
                    continue;                                                               //probably, it was moved to another parent
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                
                xmlew.add(xmlef.createAttribute(new QName("id"), lolEdge.getId()));
                xmlew.add(xmlef.createAttribute(new QName("class"), lolEdge.getClassName()));
                
                xmlew.add(xmlef.createAttribute(new QName("aside"), getEdgeSource(lolEdge).getId()));
                xmlew.add(xmlef.createAttribute(new QName("bside"), getEdgeTarget(lolEdge).getId()));
                
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
            for (Widget framesWidget : framesLayer.getChildren()) {
                QName qnamePolygon = new QName("polygon");
                LocalObjectLight lolFrame = (LocalObjectLight)findObject(framesWidget);
                xmlew.add(xmlef.createStartElement(qnamePolygon, null, null));
                xmlew.add(xmlef.createAttribute(new QName("title"), lolFrame.getName().substring(lolFrame.getName().indexOf(FREE_FRAME) + 9)));
                xmlew.add(xmlef.createAttribute(new QName("color"), "#000000"));
                xmlew.add(xmlef.createAttribute(new QName("border"), "8"));
                xmlew.add(xmlef.createAttribute(new QName("fill"), "none"));
                
                xmlew.add(xmlef.createAttribute(new QName("x"), Double.toString(framesWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("y"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createAttribute(new QName("w"), Integer.toString(framesWidget.getBounds().width)));
                xmlew.add(xmlef.createAttribute(new QName("h"), Integer.toString(framesWidget.getBounds().height)));
                
                QName qnameVertex_w = new QName("vertex");
                xmlew.add(xmlef.createStartElement(qnameVertex_w, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x0"), Double.toString(framesWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("x1"), Double.toString(framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().width)));
                xmlew.add(xmlef.createAttribute(new QName("y0"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createAttribute(new QName("y1"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createEndElement(qnameVertex_w, null));
                
                QName qnameVertex_x = new QName("vertex");
                xmlew.add(xmlef.createStartElement(qnameVertex_x, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x0"), Double.toString(framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().getWidth())));
                xmlew.add(xmlef.createAttribute(new QName("x1"), Double.toString(framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().width)));
                xmlew.add(xmlef.createAttribute(new QName("y0"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createAttribute(new QName("y1"), Double.toString(framesWidget.getPreferredLocation().getY() - framesWidget.getBounds().height)));
                xmlew.add(xmlef.createEndElement(qnameVertex_x, null));
                
                QName qnameVertex_y = new QName("vertex");
                xmlew.add(xmlef.createStartElement(qnameVertex_y, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x0"), Double.toString(framesWidget.getPreferredLocation().getX() + framesWidget.getBounds().width)));
                xmlew.add(xmlef.createAttribute(new QName("x1"), Double.toString(framesWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("y0"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createAttribute(new QName("y1"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createEndElement(qnameVertex_y, null));
                
                QName qnameVertex_z = new QName("vertex");
                xmlew.add(xmlef.createStartElement(qnameVertex_z, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x0"), Double.toString(framesWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("x1"), Double.toString(framesWidget.getPreferredLocation().getX())));
                xmlew.add(xmlef.createAttribute(new QName("y0"), Double.toString(framesWidget.getPreferredLocation().getY() - framesWidget.getBounds().height)));
                xmlew.add(xmlef.createAttribute(new QName("y1"), Double.toString(framesWidget.getPreferredLocation().getY())));
                xmlew.add(xmlef.createEndElement(qnameVertex_z, null));
                
                xmlew.add(xmlef.createEndElement(qnamePolygon, null));
            }
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
                        String objectId = reader.getElementText();

                        LocalObjectLight lol = CommunicationsStub.getInstance().getObjectInfoLight(objectClass, objectId);
                        if (lol != null) {
                            if (getNodes().contains(lol))
                                NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, "The view seems to be corrupted. Self-healing measures were taken");
                            else {
                                Widget widget = addNode(lol);
                                widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                                widget.setBackground(com.getMetaForClass(objectClass, false).getColor());
                                validate();
                            }
                        }
                    }else {
                        if (reader.getName().equals(qEdge)) {
                            String objectId = reader.getAttributeValue(null, "id"); //NOI18N

                            String aSide = reader.getAttributeValue(null, "aside"); //NOI18N
                            String bSide = reader.getAttributeValue(null, "bside"); //NOI18N

                            String className = reader.getAttributeValue(null,"class"); //NOI18N

                            LocalObjectLight container = com.getObjectInfoLight(className, objectId);

                            if (container != null) { // if the connection exists
                                LocalObjectLight aSideObject = new LocalObjectLight(aSide, null, null);
                                ObjectNodeWidget aSideWidget = (ObjectNodeWidget) findWidget(aSideObject);

                                LocalObjectLight bSideObject = new LocalObjectLight(bSide, null, null);
                                ObjectNodeWidget bSideWidget = (ObjectNodeWidget) findWidget(bSideObject);

                                if (aSideWidget != null && bSideWidget != null) {//If one of the endpoints is missing, don't render the connection

                                    if (getEdges().contains(container))
                                        NotificationUtil.getInstance().showSimplePopup("Warning", NotificationUtil.WARNING_MESSAGE, "The view seems to be corrupted. Self-healing measures were taken");
                                    else {
                                        ObjectConnectionWidget newEdge = (ObjectConnectionWidget) addEdge(container);
                                        setEdgeSource(container, aSideWidget.getLookup().lookup(LocalObjectLight.class));
                                        setEdgeTarget(container, bSideWidget.getLookup().lookup(LocalObjectLight.class));
                                        
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
    public void render(LocalObjectLight mplsLink) {
        LocalMPLSConnectionDetails e2EMPLSConnections = com.getMPLSLinkEndpoints(mplsLink.getId());
        
//        for (LocalMPLSConnectionDetails mplsLinkDetail : e2EMPLSConnections) {
//            if(mplsLinkDetail.getDeviceA() != null && findWidget(mplsLinkDetail.getDeviceA()) == null)
//                addNode(mplsLinkDetail.getDeviceA());
//            
//            if(mplsLinkDetail.getDeviceB() != null && findWidget(mplsLinkDetail.getDeviceB()) == null)
//                addNode(mplsLinkDetail.getDeviceB());
//            
//            if(findWidget(mplsLinkDetail.getConnectionObject()) == null){
//                addEdge(mplsLinkDetail.getConnectionObject());
//                validate();
//                if(mplsLinkDetail.getDeviceA() != null)
//                    setEdgeSource(mplsLinkDetail.getConnectionObject(), mplsLinkDetail.getDeviceA());
//                if(mplsLinkDetail.getDeviceB() !=null)
//                    setEdgeTarget(mplsLinkDetail.getConnectionObject(), mplsLinkDetail.getDeviceB());
//            }
//            validate();
//            repaint();
//        }
    }

    @Override
    public ConnectProvider getConnectProvider() { return null; }

    @Override
    public boolean supportsConnections() { return true; }

    @Override
    public boolean supportsBackgrounds() { return false; }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        Widget newWidget;
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(node.getClassName(), false);
        if (classMetadata != null)
            newWidget = new ObjectNodeWidget(this, node, classMetadata.getIcon());
        else
            newWidget = new ObjectNodeWidget(this, node);

        newWidget.getActions().addAction(createSelectAction());
        newWidget.getActions().addAction(moveAction);
        newWidget.setPreferredLocation(new Point(nodeLayer.getChildren().size() * 200, (nodeLayer.getChildren().size() % 2) * 200 ));
        nodeLayer.addChild(newWidget);

        return newWidget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge, ObjectConnectionWidget.LINE);
        
        newWidget.getActions().addAction(createSelectAction());
        newWidget.getActions().addAction(moveControlPointAction);
        newWidget.getActions().addAction(addRemoveControlPointAction);
        newWidget.setRouter(RouterFactory.createFreeRouter());
        newWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        
        LocalClassMetadata classMetadata = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
        if (classMetadata != null)
            newWidget.setLineColor(classMetadata.getColor());
        
        edgeLayer.addChild(newWidget);
        return newWidget;
    }
}
