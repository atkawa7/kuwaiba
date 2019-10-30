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
package com.neotropic.kuwaiba.modules.reporting.img.endtoend;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLEventFactory;
import javax.xml.namespace.QName;
import java.awt.Point;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLInputFactory;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.imageio.ImageIO;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.router.RouterFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * Shows an end-to-end view of a service by trying to match the endpoints of the logical circuits
 * directly associated to the selected instance. The view looks a lot like the Physical Path view, but they're totally different: It's intended to deal exclusively 
 * with logical connections, though it uses the physical path to fill the gaps between different logical segments
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class EndToEndViewScene extends GraphScene<RemoteObjectLight, RemoteObjectLight> {
    /**
     * Version for the XML document to save views (see http://neotropic.co/kuwaiba/wiki/index.php?title=XML_Documents#To_Save_Object_Views for details)
     */
    public static final String VIEW_FORMAT_VERSION = "1.2";
    public static final String FREE_FRAME = "freeFrame";
    public static final String CLASS_GENERICLOGICALPORT = "GenericLogicalPort";
    public static final String CLASS_GENERICPORT = "GenericPort";
    public static final String CLASS_GENERICDISTRIBUTIONFRAME = "GenericDistributionFrame";
    
    private final WebserviceBean webserviceBean;
    private final RemoteSession remoteSession;    
    private final String ipAddress;
    
    public final static String VIEW_CLASS = "EndToEndView"; 
        
    private final LayerWidget imagesLayer;
    protected LayerWidget labelsLayer;
    
    private final LayerWidget nodeLayer;
    private final LayerWidget edgeLayer;
    
    public EndToEndViewScene(String ipAddress, RemoteSession remoteSession, WebserviceBean webserviceBean) {
        this.webserviceBean = webserviceBean;
        this.remoteSession = remoteSession;
        this.ipAddress = ipAddress;
        
        nodeLayer = new LayerWidget(this);
        edgeLayer = new LayerWidget(this);
        imagesLayer = new LayerWidget(this);
        setBackground(Color.WHITE);
        addChild(imagesLayer);
        addChild(edgeLayer);
        addChild(nodeLayer);
    }
        
    public byte[] getAsXML() { 
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), VIEW_FORMAT_VERSION));
            
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
                RemoteObjectLight lolNode = (RemoteObjectLight) findObject(nodeWidget);
                xmlew.add(xmlef.createAttribute(new QName("class"), lolNode.getClassName()));
                xmlew.add(xmlef.createCharacters(lolNode.getId()));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            
            for (Widget edgeWidget : edgeLayer.getChildren()) {
                RemoteObjectLight lolEdge = (RemoteObjectLight) findObject(edgeWidget);
                ObjectConnectionWidget acwEdge = (ObjectConnectionWidget) edgeWidget;
                if (acwEdge.getSourceAnchor() == null || acwEdge.getTargetAnchor() == null) //This connection is malformed because one of the endpoints does not exist
                    continue;                                                               //probably, it was moved to another parent
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                
                xmlew.add(xmlef.createAttribute(new QName("id"), lolEdge.getId()));
                xmlew.add(xmlef.createAttribute(new QName("class"), lolEdge.getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("asideid"), getEdgeSource(lolEdge).getId()));
                xmlew.add(xmlef.createAttribute(new QName("asideclass"), getEdgeSource(lolEdge).getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("bsideid"), getEdgeTarget(lolEdge).getId()));
                xmlew.add(xmlef.createAttribute(new QName("bsideclass"), getEdgeTarget(lolEdge).getClassName()));
                
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
                        
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null; 
    }
    
    public void render(byte[] structure) throws IllegalArgumentException { 
//<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//        try {
//            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/e2eviewExporter_.xml");
//            fos.write(structure);
//            fos.close();
//        } catch(Exception e) {}
//</editor-fold>
        try{
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qZoom = new QName("zoom"); //NOI18N
            QName qCenter = new QName("center"); //NOI18N
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qLabel = new QName("label"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        String className = reader.getAttributeValue(null, "class");
                        int xCoordinate = Integer.valueOf(reader.getAttributeValue(null,"x"));
                        int yCoordinate = Integer.valueOf(reader.getAttributeValue(null,"y"));
                        String objectId = reader.getElementText();
                        try {
                            RemoteObjectLight lol = webserviceBean.getObjectLight(className, objectId, ipAddress, remoteSession.getSessionId());
                            if (lol != null) {
                                if(findWidget(lol) == null){
                                    Widget widget = addNode(lol);
                                    widget.setPreferredLocation(new Point(xCoordinate, yCoordinate));
                                    widget.setBackground(new Color(webserviceBean.getClass(className, ipAddress, remoteSession.getSessionId()).getColor()));
                                    this.notifyNodeAdded(lol, widget);
                                    validate();
                                }
                            }
                        } catch (ServerSideException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }else {
                        if (reader.getName().equals(qEdge)) {
                            String linkId = reader.getAttributeValue(null, "id"); //NOI18N
                            String linkClassName = reader.getAttributeValue(null,"class"); //NOI18N
                            String aSideid = reader.getAttributeValue(null, "asideid"); //NOI18N
                            String aSideClassName = reader.getAttributeValue(null, "asideclass"); //NOI18N
                            String bSideid = reader.getAttributeValue(null, "bsideid"); //NOI18N
                            String bSideClassName = reader.getAttributeValue(null, "bsideclass"); //NOI18N
                            
                            RemoteObjectLight container = null;
                            try {
                                container = webserviceBean.getObjectLight(linkClassName, linkId, ipAddress, remoteSession.getSessionId());
                            } catch (ServerSideException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            
                            if (container != null) { // if the connection exists
                                try {
                                    RemoteObjectLight aSideObject = webserviceBean.getObjectLight(aSideClassName, aSideid, ipAddress, remoteSession.getSessionId());
                                    ObjectNodeWidget aSideWidget = (ObjectNodeWidget) findWidget(aSideObject);

                                    RemoteObjectLight bSideObject = webserviceBean.getObjectLight(bSideClassName, bSideid, ipAddress, remoteSession.getSessionId());
                                    ObjectNodeWidget bSideWidget = (ObjectNodeWidget) findWidget(bSideObject);

                                    if (aSideWidget != null && bSideWidget != null) {//If one of the endpoints is missing, don't render the connection

                                        if (!getEdges().contains(container)){
                                            Widget source = findWidget(aSideObject);
                                            Widget target = findWidget(bSideObject);
                                            ObjectConnectionWidget newEdge = (ObjectConnectionWidget) addEdge(container);
                                            setEdgeSource(container, aSideObject);
                                            setEdgeTarget(container, bSideObject);
                                            
                                            List<Point> localControlPoints = new ArrayList<>();
                                            while(true) {
                                                reader.nextTag();

                                                if (reader.getName().equals(qControlPoint)) {
                                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                                        localControlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), Integer.valueOf(reader.getAttributeValue(null,"y"))));
                                                } else{
                                                    newEdge.setControlPoints(localControlPoints,false);
                                                    break;
                                                }
                                            }
                                            if(newEdge.getControlPoints().isEmpty() && localControlPoints.isEmpty()){
                                                String aName = aSideObject.getName();
                                                String bName = bSideObject.getName();
                                                int aNameLength = aName != null && !aName.isEmpty() ? aName.length() : 1;
                                                int bNameLength = bName != null && !bName.isEmpty() ? bName.length() : 1;
                                                localControlPoints.add(newEdge.convertLocalToScene(new Point(source.getPreferredLocation().x + aNameLength * 7, source.getPreferredLocation().y + 16)));
                                                localControlPoints.add(newEdge.convertLocalToScene(new Point(target.getPreferredLocation().x + bNameLength * 7, target.getPreferredLocation().y + 16)));
                                                newEdge.setControlPoints(localControlPoints,false);
                                                validate();
                                            }

                                        }
                                    }
                                } catch(Exception ex) {
                                    Exceptions.printStackTrace(ex);                                    
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
            Exceptions.printStackTrace(ex);
        }
        edgeLayer.revalidate();
        edgeLayer.repaint();
        validate();
        repaint();
    }
    
    public void clear() {
        while (!getNodes().isEmpty())
            removeNode(getNodes().iterator().next());

        while (!getEdges().isEmpty())
            removeEdge(getEdges().iterator().next());
        
        if (labelsLayer != null)
            labelsLayer.removeChildren();
        validate();
        repaint();
    }
    
    public boolean supportsConnections() { return true; }
    
    public boolean supportsBackgrounds() { return false; }

    @Override
    protected Widget attachNodeWidget(RemoteObjectLight node) {
        Widget newWidget;
        RemoteClassMetadata classMetadata = null;
        try {
            classMetadata = webserviceBean.getClass(node.getClassName(), ipAddress, remoteSession.getSessionId());
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        if (classMetadata != null) {
            try {
                newWidget = new ObjectNodeWidget(this, node, ImageIO.read(new ByteArrayInputStream(classMetadata.getIcon())));
            } catch (IOException ex) {
                newWidget = new ObjectNodeWidget(this, node);
            }
        } else{
            newWidget = new ObjectNodeWidget(this, node);
        }
        newWidget.repaint();
        newWidget.revalidate();
        nodeLayer.addChild(newWidget);
        return newWidget;
    }

    @Override
    protected Widget attachEdgeWidget(RemoteObjectLight edge) {
        ObjectConnectionWidget newWidget = new ObjectConnectionWidget(this, edge, 
        edge.getClassName().equals("RadioLink") ? ObjectConnectionWidget.DOT_LINE : ObjectConnectionWidget.LINE);
        
        newWidget.getActions().addAction(createSelectAction());
        newWidget.setRouter(RouterFactory.createFreeRouter());
        newWidget.setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        newWidget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        
        RemoteClassMetadata classMetadata = null;
        try {
            classMetadata = webserviceBean.getClass(edge.getClassName(), ipAddress, remoteSession.getSessionId());
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (classMetadata != null)
            newWidget.setLineColor(new Color(classMetadata.getColor()));
          
        newWidget.getActions().addAction(createSelectAction());
        edgeLayer.addChild(newWidget);
        return newWidget;
    }
    
    @Override
    protected void attachEdgeSourceAnchor(RemoteObjectLight edge, RemoteObjectLight oldSourceNode, RemoteObjectLight newSourceNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget sourceWidget = findWidget(newSourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createCircularAnchor(sourceWidget, 3) : null);
    }
    
    @Override
    protected void attachEdgeTargetAnchor(RemoteObjectLight edge, RemoteObjectLight oldTargetNode, RemoteObjectLight newTargetNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget targetWidget = findWidget(newTargetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createCircularAnchor(targetWidget, 3) : null);
    }
}
