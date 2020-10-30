/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.optional.topoman;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.neotropic.flow.component.mxgraph.Point;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.util.visual.mxgraph.MxGraphCanvas;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractView;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewEdge;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewEventListener;
import org.neotropic.kuwaiba.core.apis.integration.views.ViewMap;
import org.neotropic.kuwaiba.modules.optional.topoman.tools.BasicStyleEditor;
import org.neotropic.util.visual.notifications.AbstractNotification;

/**
 * Custom view implementation for Topology view module with a mxgraph component as
 * canvas.
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class TopologyView extends AbstractView<BusinessObjectLight, Component> {

    /**
     * Reference to the main canvas of the view
     */
    private MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxgraphCanvas;
    /**
     * Reference to the translation service.
     */
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager
     */
    private BusinessEntityManager bem;
    /**
     * Reference to the Metadata Entity Manager
     */
    private MetadataEntityManager mem;
    /**
     * Utility class that help to load resources like icons and images
     */
    private ResourceFactory resourceFactory;
    /**
     * Constant to represent the free shapes that can be added to some views
     */
    public final static String FREE_SHAPE = "freeShape";
    /**
     * Constant to represent the free shapes that can be added to some views
     */
    public final static String ICON = "icon";

    public final static String URL_IMG_CLOUD = "images/cloud64.png";

    public MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> getMxgraphCanvas() {
        return mxgraphCanvas;
    }

    public void setMxgraphCanvas(MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxgraphCanvas) {
        this.mxgraphCanvas = mxgraphCanvas;
    }

    public TopologyView(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, TranslationService ts, ResourceFactory resourceFactory) {
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        mxgraphCanvas = new MxGraphCanvas("100%", "80%");
//        mxgraphCanvas.getMxGraph().setConnectable(true);
        mxgraphCanvas.getMxGraph().setOutlineHeight("100px");
        mxgraphCanvas.getMxGraph().setOverflow("scroll");
        mxgraphCanvas.getMxGraph().setGrid("");
        mxgraphCanvas.getMxGraph().setHasOutline(true);
    }

    @Override
    public String getName() {
        return ts.getTranslatedString("module.topoman.topology-view.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.topoman.topology-view.description");
    }

    @Override
    public String getVersion() {
        return "2.0";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>"; //NOI18N
    }

    @Override
    public byte[] getAsXml() {
        try {
            //First we make sure that the view map reflects the graph in the graphic component. 
            //If syncViewMap is not called, the XML document generated by this method will 
            //correspond to the latest loaded version of the map.
            this.syncViewMap();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();

            QName qnameView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), getVersion())); // NOI18N

            QName qnameClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters("TopologyView"));
            xmlew.add(xmlef.createEndElement(qnameClass, null));

            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            for (AbstractViewNode nodeEntry : viewMap.getNodes()) {
                if (!((BusinessObjectLight) nodeEntry.getIdentifier()).getClassName().equals(ICON)
                        && !((BusinessObjectLight) nodeEntry.getIdentifier()).getClassName().equals(FREE_SHAPE)) {

                    QName qnameNode = new QName("node");
                    xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), ((Double) nodeEntry.getProperties().get("x")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("y"), ((Double) nodeEntry.getProperties().get("y")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("class"), ((BusinessObjectLight) nodeEntry.getIdentifier()).getClassName())); //NOI18N
                    xmlew.add(xmlef.createCharacters(((BusinessObjectLight) nodeEntry.getIdentifier()).getId())); //NOI18N
                    xmlew.add(xmlef.createEndElement(qnameNode, null));
                }
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));

            QName qnameIcons = new QName("icons");
            xmlew.add(xmlef.createStartElement(qnameIcons, null, null));
            for (AbstractViewNode nodeEntry : viewMap.getNodes()) {
                if (((BusinessObjectLight) nodeEntry.getIdentifier()).getClassName().equals(ICON)) {

                    QName qnameIcon = new QName("icon");
                    xmlew.add(xmlef.createStartElement(qnameIcon, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), ((Double) nodeEntry.getProperties().get("x")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("y"), ((Double) nodeEntry.getProperties().get("y")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("w"), ((Double) nodeEntry.getProperties().get("w")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("h"), ((Double) nodeEntry.getProperties().get("h")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("id"), ((BusinessObjectLight) nodeEntry.getIdentifier()).getId()));
                    xmlew.add(xmlef.createAttribute(new QName("label"), ((String) nodeEntry.getProperties().get("label"))));
                    xmlew.add(xmlef.createEndElement(qnameIcon, null));
                }
            }
            xmlew.add(xmlef.createEndElement(qnameIcons, null));

            QName qnamePolygons = new QName("polygons");
            xmlew.add(xmlef.createStartElement(qnamePolygons, null, null));
            for (AbstractViewNode nodeEntry : viewMap.getNodes()) {
                if (((BusinessObjectLight) nodeEntry.getIdentifier()).getClassName().equals(FREE_SHAPE)) {

                    QName qnamePolygon = new QName("polygon");
                    xmlew.add(xmlef.createStartElement(qnamePolygon, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), ((Double) nodeEntry.getProperties().get("x")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("y"), ((Double) nodeEntry.getProperties().get("y")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("w"), ((Double) nodeEntry.getProperties().get("w")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("h"), ((Double) nodeEntry.getProperties().get("h")).intValue() + ""));
                    xmlew.add(xmlef.createAttribute(new QName("id"), ((BusinessObjectLight) nodeEntry.getIdentifier()).getId()));
                    xmlew.add(xmlef.createAttribute(new QName("label"), ((String) nodeEntry.getProperties().get("label"))));
                    xmlew.add(xmlef.createAttribute(new QName("shape"), (String) nodeEntry.getProperties().get("shape")));
                    for (String style : BasicStyleEditor.supportedNodeStyles) {
                        xmlew.add(xmlef.createAttribute(new QName(style), (String) nodeEntry.getProperties().get(style)));             
                    }
                    xmlew.add(xmlef.createEndElement(qnamePolygon, null));
                }
            }
            xmlew.add(xmlef.createEndElement(qnamePolygons, null));

            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            for (AbstractViewEdge edgeEntry : this.viewMap.getEdges()) {

                BusinessObjectLight edgeObject = (BusinessObjectLight) edgeEntry.getIdentifier();
                BusinessObjectLight sourceObject = (BusinessObjectLight) this.viewMap.getEdgeSource(edgeEntry).getIdentifier();
                BusinessObjectLight targetObject = (BusinessObjectLight) this.viewMap.getEdgeTarget(edgeEntry).getIdentifier();

                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                xmlew.add(xmlef.createAttribute(new QName("id"), edgeObject.getId()));
                xmlew.add(xmlef.createAttribute(new QName("class"), edgeObject.getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("label"), edgeEntry.getProperties().getProperty("label")));
                xmlew.add(xmlef.createAttribute(new QName("aside"), sourceObject.getId().contains("-*")
                        ? "-1" : sourceObject.getId()));
                xmlew.add(xmlef.createAttribute(new QName("bside"), targetObject.getId().contains("-*")
                        ? "-1" : targetObject.getId()));

                for (String style : BasicStyleEditor.supportedEdgeStyles) {
                    xmlew.add(xmlef.createAttribute(new QName(style), (String) edgeEntry.getProperties().get(style)));             
                }
               
                
                for (Point point : (List<Point>) edgeEntry.getProperties().get("controlPoints")) {
                    QName qnameControlpoint = new QName("controlpoint");
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), ((int) point.getX()) + ""));
                    xmlew.add(xmlef.createAttribute(new QName("y"), ((int) point.getY()) + ""));
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                }
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            }
            xmlew.add(xmlef.createEndElement(qnameEdges, null));

            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            Logger.getLogger(TopologyView.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
        }
        return null;
    }

    @Override
    public byte[] getAsImage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Component getAsComponent() {
        try {
            if (this.mxgraphCanvas == null) 
                mxgraphCanvas = new MxGraphCanvas<>();
            
            return this.mxgraphCanvas.getMxGraph();
        } catch (Exception ex) {
            return new Label(String.format(ts.getTranslatedString("module.topoman.unexpected-error-loading-view"), ex.getLocalizedMessage()));
        }
    }

    @Override
    public void buildWithSavedView(byte[] structure) {
        if (structure == null || structure.length == 0) {
            return;
        }

        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
//      <editor-fold defaultstate="collapsed" desc="Uncomment this for debugging purposes. This outputs the XML view as a file">
//        try (FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/TOPO_VIEW_V2.1.xml")) {
//            fos.write(structure);
//        } catch (Exception e) {
//        }
//      </editor-fold>
        QName qNode = new QName("node"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qControlPoint = new QName("controlpoint"); //NOI18N
        QName qPolygon = new QName("polygon"); //NOI18N
        QName qIcon = new QName("icon"); //NOI18N
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        String objectClass = reader.getAttributeValue(null, "class");

                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null, "x")).intValue();
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null, "y")).intValue();
                        String objectId = reader.getElementText();
                        //this side is connected
                        BusinessObjectLight lol = bem.getObjectLight(objectClass, objectId);
                        if (lol != null) {
                            String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(lol.getClassName())).toString();

                            Properties props = new Properties();
                            props.put("imageUrl", uri);
                            props.put("x", xCoordinate);
                            props.put("y", yCoordinate);
                            addNode(lol, props);
                        } else {
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), String.format(ts.getTranslatedString("module.topoman.object-not-found"), objectClass, objectId), 
                                AbstractNotification.NotificationType.INFO, ts).open();
                        }

                    } else if (reader.getName().equals(qIcon)) { // FREE CLOUDS

                        Properties props = new Properties();
                        int x = Double.valueOf(reader.getAttributeValue(null, "x")).intValue();
                        int y = Double.valueOf(reader.getAttributeValue(null, "y")).intValue();
                        if (reader.getAttributeValue(null, "w") != null) {
                            int w = Double.valueOf(reader.getAttributeValue(null, "w")).intValue();
                            props.put("w", w);
                        }
                        if (reader.getAttributeValue(null, "w") != null) {
                            int h = Double.valueOf(reader.getAttributeValue(null, "h")).intValue();
                            props.put("h", h);
                        }
                        String label = reader.getAttributeValue(null, "label");
                        String oid = reader.getAttributeValue(null, "id");

                        BusinessObjectLight bol = new BusinessObjectLight(ICON, oid, ICON);
                        props.put("imageUrl", URL_IMG_CLOUD);
                        props.put("x", x);
                        props.put("y", y);
                        props.put("w", 64);
                        props.put("h", 64);

                        if (label != null) {
                            props.put("label", label);
                        }
                        addNode(bol, props);

                    } else if (reader.getName().equals(qPolygon)) {
                        String shape = reader.getAttributeValue(null, "shape");
                        String oid = reader.getAttributeValue(null, "id") == null ? UUID.randomUUID().toString() : reader.getAttributeValue(null, "id");
                        int x = Double.valueOf(reader.getAttributeValue(null, "x")).intValue();
                        int y = Double.valueOf(reader.getAttributeValue(null, "y")).intValue();
                        int w = Double.valueOf(reader.getAttributeValue(null, "w")).intValue();
                        int h = Double.valueOf(reader.getAttributeValue(null, "h")).intValue();
                        String label = reader.getAttributeValue(null, "label");
                        BusinessObjectLight bol = new BusinessObjectLight(FREE_SHAPE, oid, FREE_SHAPE);
                        Properties props = new Properties();
                        props.put("x", x);
                        props.put("y", y);
                        props.put("w", w);
                        props.put("h", h);
                        for (String style : BasicStyleEditor.supportedNodeStyles) {
                             props.put(style, reader.getAttributeValue(null, style) != null ? reader.getAttributeValue(null, style) : "");
                        }

                        if (shape == null) {
                            shape = "rectangle";
                        }
                        props.put("shape", shape);
                        if (label != null) {
                            props.put("label", label);
                        }
                        addNode(bol, props);
                    } else if (reader.getName().equals(qEdge)) {

                        String aSideId = reader.getAttributeValue(null, "aside");
                        String bSideId = reader.getAttributeValue(null, "bside");
                        String label = reader.getAttributeValue(null, "label");

                        BusinessObjectLight edge = new BusinessObjectLight("edge", aSideId + bSideId, "edge");
                        BusinessObjectLight endPointA = new BusinessObjectLight("", aSideId, "");
                        BusinessObjectLight endPointB = new BusinessObjectLight("", bSideId, "");
                        Properties props = new Properties();
                        for (String style : BasicStyleEditor.supportedEdgeStyles) {
                             props.put(style, reader.getAttributeValue(null, style) != null ? reader.getAttributeValue(null, style) : "");
                        }
                        List<Point> controlPoints = new ArrayList<>();
                        while (true) {
                            reader.nextTag();

                            if (reader.getName().equals(qControlPoint)) {
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                    controlPoints.add(new Point(Double.valueOf(reader.getAttributeValue(null, "x")), Double.valueOf(reader.getAttributeValue(null, "y"))));
                                }
                            } else {
                                break;
                            }
                        }                       
                        if (controlPoints.size() > 2) // ignore default control points of desktop client
                        {
                            controlPoints = controlPoints.subList(1, controlPoints.size() - 1);
                        }
                        props.put("controlPoints", controlPoints);
                        props.put("label", label == null ? "" : label);                     
                       
                        AbstractViewEdge viewEdge = addEdge(edge, endPointA, endPointB, props);
                        if (viewEdge == null)
                            new SimpleNotification(ts.getTranslatedString("module.general.messages.warning"), 
                                    ts.getTranslatedString("module.topoman.enpoint-not-found"), 
                                        AbstractNotification.NotificationType.WARNING, ts).open();
                        
                    }

                }
            }
            reader.close();
            MxGraphNode dummyNode = new MxGraphNode();
            dummyNode.setGeometry(0, 0, 0, 0);
            dummyNode.setMovable(false);
            mxgraphCanvas.getMxGraph().addNode(dummyNode);
            //  execute the layout and disable moving when the last cell is added
            dummyNode.addCellAddedListener(eventListener -> {
                mxgraphCanvas.getMxGraph().refreshGraph();
            });
        } catch (NumberFormatException | XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.topoman.view-corrupted"), 
                            AbstractNotification.NotificationType.ERROR, ts).open();
            Logger.getLogger(TopologyView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(TopologyView.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void buildEmptyView() {

        if (this.viewMap == null) {
            this.viewMap = new ViewMap();
        } else {
            this.viewMap.clear();
        }

        mxgraphCanvas.setNodes(new HashMap<>());
        mxgraphCanvas.setEdges(new HashMap<>());
        mxgraphCanvas.getMxGraph().removeAllCells();
    }

    @Override
    public AbstractViewNode addNode(BusinessObjectLight businessObject, Properties properties) {

        AbstractViewNode aNode = this.viewMap.findNode(businessObject);
        if (aNode == null) {
            BusinessObjectViewNode newNode = new BusinessObjectViewNode(businessObject);
            newNode.setProperties(properties);
            this.viewMap.addNode(newNode);

            if (this.mxgraphCanvas != null) { //The view could be created without a graphical representation (the canvas). so here we make sure that's not the case
                int x = (int) properties.get("x");
                int y = (int) properties.get("y");
                Integer width = (Integer) properties.get("w");
                Integer height = (Integer) properties.get("h");
                String urlImage = (String) properties.get("imageUrl");
                String shape = (String) properties.get("shape");
                String label = (!businessObject.getClassName().equals(ICON)
                        && !businessObject.getClassName().equals(FREE_SHAPE)) ? 
                         businessObject.getName() : (String) properties.get("label");

                MxGraphNode newMxNode = new MxGraphNode();
                newMxNode.setUuid(businessObject.getId());
                newMxNode.setLabel(label);
                newMxNode.setWidth(width == null ? ((int) Constants.DEFAULT_ICON_WIDTH) : width);
                newMxNode.setHeight(height == null ? ((int) Constants.DEFAULT_ICON_HEIGHT) : height);
                newMxNode.setX(x);
                newMxNode.setY(y);
                newMxNode.setUsePortToConnect(true);
                if (urlImage == null) {// is a Free shape 
                    newMxNode.setStrokeColor("black");
                    newMxNode.setFillColor(MxConstants.NONE);
                    newMxNode.setShape(shape);
                    LinkedHashMap<String, String> mapStyle = new LinkedHashMap();
                    for (String style : BasicStyleEditor.supportedNodeStyles) {
                        String prop = (String) properties.get(style);
                        if ( prop != null && !prop.isEmpty() )
                           mapStyle.put(style, prop);
                    }
                    newMxNode.setRawStyle(mapStyle);
                    newMxNode.addCellAddedListener(eventListener -> {
                       newMxNode.setSelfPosition(0);
                       mxgraphCanvas.getMxGraph().refreshGraph();
                    });
                } else {
                    newMxNode.setShape(MxConstants.SHAPE_IMAGE);
                    newMxNode.setImage(urlImage);
                    newMxNode.setIsResizable(false);
                }

                mxgraphCanvas.addNode(businessObject, newMxNode);
            }
            return newNode;
        } else {
            return aNode;
        }
    }

    @Override
    public AbstractViewEdge addEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceBusinessObject, BusinessObjectLight targetBusinessObject, Properties properties) {

        AbstractViewEdge anEdge = this.viewMap.findEdge(businessObject);
        if (anEdge == null) {
            BusinessObjectViewEdge newEdge = new BusinessObjectViewEdge(businessObject);

            //if any of the end points is missing, the edge is not added
            AbstractViewNode aSourceNode = this.viewMap.findNode(sourceBusinessObject.getId());
            if (aSourceNode == null) {
                return null;
            }

            AbstractViewNode aTargetNode = this.viewMap.findNode(targetBusinessObject.getId());
            if (aTargetNode == null) {
                return null;
            }

            newEdge.setProperties(properties);
            this.viewMap.addEdge(newEdge);
            this.viewMap.attachSourceNode(anEdge, aSourceNode);
            this.viewMap.attachTargetNode(anEdge, aTargetNode);

            if (this.mxgraphCanvas != null) { //The view could be created without a graphical representation (the map). so here we make sure that's not the case
                List<Point> controlPoints = (List<Point>) properties.get("controlPoints");
                String label = (String) properties.get("label");
                MxGraphEdge newMxEdge = new MxGraphEdge();

                newMxEdge.setUuid(businessObject.getId());
                newMxEdge.setSource(sourceBusinessObject.getId());
                newMxEdge.setTarget(targetBusinessObject.getId());
                newMxEdge.setLabel(label);
                newMxEdge.setPoints(controlPoints);
                LinkedHashMap<String, String> mapStyle = new LinkedHashMap();
                for (String style : BasicStyleEditor.supportedEdgeStyles) {
                        String prop = (String) properties.get(style);
                        if ( prop != null && !prop.isEmpty() )
                           mapStyle.put(style, prop);
                    }
                newMxEdge.setRawStyle(mapStyle);

                MxGraphNode sourceNode = mxgraphCanvas.findMxGraphNode(sourceBusinessObject);
                MxGraphNode targetNode = mxgraphCanvas.findMxGraphNode(targetBusinessObject);
                if ((sourceNode != null && MxConstants.SHAPE_LABEL.equals(sourceNode.getShape()))
                        || (targetNode != null && MxConstants.SHAPE_LABEL.equals(targetNode.getShape()))) {
                    newMxEdge.setIsDashed(Boolean.TRUE);
                }
                mxgraphCanvas.addEdge(businessObject, (BusinessObjectLight) aSourceNode.getIdentifier(), (BusinessObjectLight) aTargetNode.getIdentifier(), newMxEdge);
            }
            return newEdge;
        } else {
            return anEdge;
        }
    }

    /**
     * The view map is created originally by calling the
     * {@link  #buildWithSavedView(byte[])} method, but it can change due to
     * user interactions, so it's necessary to update it in order to export it
     * in other formats. This method wipes the existing view map and builds it
     * again from whatever it is on the map currently
     */
    public void syncViewMap() {
        this.viewMap.clear();
        if (mxgraphCanvas == null) {
            return;
        }

        for (Map.Entry<BusinessObjectLight, MxGraphNode> entry : mxgraphCanvas.getNodes().entrySet()) {
            BusinessObjectViewNode aNode = new BusinessObjectViewNode(entry.getKey());
            aNode.getProperties().put("x", entry.getValue().getX());
            aNode.getProperties().put("y", entry.getValue().getY());
            aNode.getProperties().put("w", entry.getValue().getWidth());
            aNode.getProperties().put("h", entry.getValue().getHeight());
            aNode.getProperties().put("label", entry.getValue().getLabel() == null ? "" : entry.getValue().getLabel());
            HashMap mapStyle = entry.getValue().getRawStyleAsMap();
            for (String style : BasicStyleEditor.supportedNodeStyles) {
                aNode.getProperties().put(style, mapStyle.containsKey(style) ? mapStyle.get(style) : "");
            }
            
            if (entry.getKey().getClassName().equals(FREE_SHAPE)) {
                aNode.getProperties().put("shape", entry.getValue().getShape());
            }
            this.viewMap.getNodes().add(aNode);
        }

        for (Map.Entry<BusinessObjectLight, MxGraphEdge> entry : mxgraphCanvas.getEdges().entrySet()) {
            BusinessObjectViewEdge anEdge = new BusinessObjectViewEdge(entry.getKey());
            anEdge.getProperties().put("label", entry.getValue().getLabel() == null ? "" : entry.getValue().getLabel());
            anEdge.getProperties().put("controlPoints", entry.getValue().getPointList());
            HashMap mapStyle = entry.getValue().getRawStyleAsMap();
            for (String style : BasicStyleEditor.supportedEdgeStyles) {
                anEdge.getProperties().put(style, mapStyle.containsKey(style) ? mapStyle.get(style) : "");
            }

            this.viewMap.getEdges().add(anEdge);
            this.viewMap.attachSourceNode(anEdge, new BusinessObjectViewNode(mxgraphCanvas.findSourceEdgeObject(entry.getKey())));
            this.viewMap.attachTargetNode(anEdge, new BusinessObjectViewNode(mxgraphCanvas.findTargetEdgeObject(entry.getKey())));
        }
    }

    @Override
    public void addNodeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addEdgeClickListener(ViewEventListener listener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buildWithBusinessObject(BusinessObjectLight businessObject) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeNode(BusinessObjectLight businessObject) {
        mxgraphCanvas.removeNode(businessObject);
        syncViewMap();
    }

    @Override
    public void removeEdge(BusinessObjectLight businessObject) {
        mxgraphCanvas.removeEdge(businessObject);
        syncViewMap();
    }

}
