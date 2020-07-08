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

package com.neotropic.kuwaiba.modules.commercial.mpls;

import com.neotropic.kuwaiba.modules.commercial.mpls.widgets.MplsDashboard;
import com.neotropic.vaadin14.component.MxGraphEdge;
import com.neotropic.vaadin14.component.MxGraphNode;
import com.neotropic.vaadin14.component.Point;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
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

/**
 * Custom view implementation for MPLS view module with a mxgraph component as canvas.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class MplsView extends AbstractView<BusinessObjectLight, Component> {
    
    private MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxgraphCanvas;
    private TranslationService ts;
    private ApplicationEntityManager aem;
    private BusinessEntityManager bem;
    private MetadataEntityManager mem;
    private ResourceFactory resourceFactory;

    public MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> getMxgraphCanvas() {
        return mxgraphCanvas;
    }

    public void setMxgraphCanvas(MxGraphCanvas<BusinessObjectLight, BusinessObjectLight> mxgraphCanvas) {
        this.mxgraphCanvas = mxgraphCanvas;
    }

    public MplsView(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem, TranslationService ts, ResourceFactory
             resourceFactory) {
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.resourceFactory = resourceFactory;
        mxgraphCanvas = new MxGraphCanvas();
    }
    

    @Override
    public String getName() {
        return ts.getTranslatedString("module.mpls.mpls-view.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.mpls.mpls-view.description");
    }
    
    @Override
    public String getVersion() {
        return "1.0";
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
            xmlew.add(xmlef.createAttribute(new QName("version"), "1")); // NOI18N
            
            QName qnameClass = new QName("class");
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters("MPLSModuleView"));
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            for ( AbstractViewNode nodeEntry : viewMap.getNodes()) {
                QName qnameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), ((Integer) nodeEntry.getProperties().get("x")).toString()));
                xmlew.add(xmlef.createAttribute(new QName("y"), ((Integer) nodeEntry.getProperties().get("y")).toString()));
                xmlew.add(xmlef.createAttribute(new QName("class"),  ((BusinessObjectLight)nodeEntry.getIdentifier()).getClassName())); //NOI18N
                xmlew.add(xmlef.createCharacters( ((BusinessObjectLight) nodeEntry.getIdentifier()).getId())); //NOI18N
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qnameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
              for (AbstractViewEdge edgeEntry : this.viewMap.getEdges()) {
                
                BusinessObjectLight edgeObject = (BusinessObjectLight)edgeEntry.getIdentifier();         
                BusinessObjectLight sourceObject = (BusinessObjectLight)this.viewMap.getEdgeSource(edgeEntry).getIdentifier();
                BusinessObjectLight targetObject = (BusinessObjectLight)this.viewMap.getEdgeTarget(edgeEntry).getIdentifier();
                
                QName qnameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                xmlew.add(xmlef.createAttribute(new QName("id"), edgeObject.getId()));
                xmlew.add(xmlef.createAttribute(new QName("class"), edgeObject.getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("asideid"), sourceObject.getId().contains("-*") ? 
                        "-1" : sourceObject.getId()));
                xmlew.add(xmlef.createAttribute(new QName("asideclass"), sourceObject.getId().contains("-*") ? 
                        "" : sourceObject.getClassName()));
                xmlew.add(xmlef.createAttribute(new QName("bsideid"), targetObject.getId().contains("-*") ? 
                        "-1" : targetObject.getId()));
                xmlew.add(xmlef.createAttribute(new QName("bsideclass"), targetObject.getId().contains("-*") ? 
                        "" : targetObject.getClassName()));
                
                for (Point point : (List<Point>)edgeEntry.getProperties().get("controlPoints")) {
                    QName qnameControlpoint = new QName("controlpoint");
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), Double.toString(point.getX())));
                    xmlew.add(xmlef.createAttribute(new QName("y"), Double.toString(point.getY())));
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                }
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            }
            xmlew.add(xmlef.createEndElement(qnameEdges, null));
            
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.general.messages.unexpected-error")).open();           
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
            return new Label(String.format("An unexpected error occurred while loading the MPLS view: %s", ex.getLocalizedMessage()));
        }
    }

    @Override
    public void buildWithSavedView(byte[] structure) {
        if (structure == null || structure.length == 0)
            return;
        
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        List<BusinessObjectLight> emptySides = new ArrayList<>();
//      <editor-fold defaultstate="collapsed" desc="Uncomment this for debugging purposes. This outputs the XML view as a file">
        try (FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/oview_ MPLS_VIEW .xml")) {
            fos.write(structure);
        } catch(Exception e) { }
//      </editor-fold>
        QName qNode = new QName("node"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qControlPoint = new QName("controlpoint"); //NOI18N
//        BusinessObjectLight emptyObj;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT){
                    if (reader.getName().equals(qNode)){
                        String objectClass = reader.getAttributeValue(null, "class");

                        int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                        int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                        String objectId = reader.getElementText();
                        //this side is connected
                        BusinessObjectLight lol = bem.getObjectLight(objectClass, objectId);
                        if (lol != null){
                           String uri = StreamResourceRegistry.getURI(resourceFactory.getClassIcon(lol.getClassName())).toString();       
                           
                           Properties props = new Properties();
                           props.put("imageUrl", uri);
                           props.put("x", xCoordinate );
                           props.put("y", yCoordinate );
                           addNode(lol, props);
                        }
                        else if(objectId.equals("-1")){// we create an empty side
//                            emptyObj = new LocalObjectLight(UUID.randomUUID().toString() + "-" + (objectId), null, null);
//                            emptySides.add(emptyObj);
//                            Widget widget = addNode(emptyObj);
//                            widget.setPreferredLocation(new java.awt.Point(xCoordinate, yCoordinate));
                        }
                    }else {
                        if (reader.getName().equals(qEdge)){
                            String mplsLinkId = reader.getAttributeValue(null, "id");
                            
                            String aSideId = reader.getAttributeValue(null, "asideid");
                            String aSideClass = reader.getAttributeValue(null, "asideclass");
                            String bSideId = reader.getAttributeValue(null, "bsideid");
                            String bSideClass = reader.getAttributeValue(null, "bsideclass");
                            
                            String className = reader.getAttributeValue(null, "class");
                            BusinessObjectLight mplsLink = bem.getObject(className, mplsLinkId);
                            BusinessObjectLight endPointA = null;
                            BusinessObjectLight endPointB = null;
                            if (mplsLink != null) {
                                BusinessObjectLight aSideObject, bSideObject;
                                if(!aSideId.equals("-1"))
                                    aSideObject = new BusinessObjectLight(aSideClass, aSideId, null);
                                else{
                                    aSideObject = emptySides.remove(0);
                                    endPointA = aSideObject;
                                }
                                if(!bSideId.equals("-1"))
                                    bSideObject = new BusinessObjectLight(bSideClass, bSideId, null);
                                else{
                                    bSideObject = emptySides.remove(0);
                                    endPointA = bSideObject;
                                }            
                                HashMap<String, List<BusinessObjectLight>> specialAttributes = bem.getSpecialAttributes(mplsLink.getClassName(), mplsLinkId);
                                for (Map.Entry<String, List<BusinessObjectLight>> entry : specialAttributes.entrySet()) {
                                    if(entry.getKey().equals("mplsEndpointA")){
                                        endPointA = entry.getValue().get(0);
                                        BusinessObjectLight parentA = bem.getFirstParentOfClass(endPointA.getClassName(), endPointA.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                                        if(parentA != null && !parentA.getId().equals(aSideId)){
                                            aSideObject = parentA;
                                        }
                                    }
                                    if(entry.getKey().equals("mplsEndpointB")){
                                        endPointB = entry.getValue().get(0);
                                        BusinessObjectLight parentB = bem.getFirstParentOfClass(endPointB.getClassName(), endPointB.getId(), Constants.CLASS_GENERICCOMMUNICATIONSELEMENT);
                                        if(parentB != null && !parentB.getId().equals(bSideId)) {                                        
                                            bSideObject = parentB;
                                        }
                                    }
                                }
                                                             
                                List<Point> controlPoints = new ArrayList<>();
                                while(true){
                                    reader.nextTag();

                                    if (reader.getName().equals(qControlPoint)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                            controlPoints.add(new Point(Double.valueOf(reader.getAttributeValue(null,"x")), Double.valueOf(reader.getAttributeValue(null,"y"))));
                                    } else {
                                        break;
                                    }
                                }
                                Properties props = new Properties();
                                props.put("controlPoints", controlPoints);
                                props.put("sourceLabel", endPointA == null ? "" : endPointA.getName());
                                props.put("targetLabel", endPointB == null ? "" : endPointB.getName());
                                addEdge(mplsLink, aSideObject, bSideObject, props);
                            } else {
//                                fireChangeEvent(new ActionEvent(this, SCENE_CHANGE, "connectionAutomaticallyRemoved")); //NOI18N
//                                NotificationUtil.getInstance().showSimplePopup("Load view", NotificationUtil.INFO_MESSAGE, String.format("Connection of class %s and id %s could not be found and was removed from the view", className, mplsLinkId));
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (NumberFormatException | XMLStreamException ex) {
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ts.getTranslatedString("module.mpls.view-corrupted")).open();
             Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException | ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(MplsDashboard.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    @Override
    public void buildEmptyView() {
                 
        if (this.viewMap == null)
            this.viewMap = new ViewMap();
        else
            this.viewMap.clear();
        
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
                String urlImage = (String) properties.get("imageUrl");

                mxgraphCanvas.addNode(businessObject, businessObject.getId(), x, y, urlImage);                           
            }          
            return newNode;
        } else
            return aNode;   
    }

    @Override
    public AbstractViewEdge addEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceBusinessObject, BusinessObjectLight targetBusinessObject, Properties properties) {
                      
        AbstractViewEdge anEdge = this.viewMap.findEdge(businessObject);
        if (anEdge == null) {
            BusinessObjectViewEdge newEdge = new BusinessObjectViewEdge(businessObject);
            
            //if any of the end points is missing, the edge is not added
            AbstractViewNode aSourceNode = this.viewMap.findNode(sourceBusinessObject.getId());
            if (aSourceNode == null)
                return null;
            
            AbstractViewNode aTargetNode = this.viewMap.findNode(targetBusinessObject.getId());
            if (aTargetNode == null)
                return null;
            
            newEdge.setProperties(properties);
            this.viewMap.addEdge(newEdge);
            this.viewMap.attachSourceNode(anEdge, aSourceNode);
            this.viewMap.attachTargetNode(anEdge, aTargetNode);
        
            if (this.mxgraphCanvas != null) { //The view could be created without a graphical representation (the map). so here we make sure that's not the case
                List<Point> controlPoints = (List<Point>) properties.get("controlPoints");
                String sourceLabel =  (String) properties.get("sourceLabel");
                String targetLabel =  (String) properties.get("targetLabel");               

                mxgraphCanvas.addEdge(businessObject, businessObject.getId(), sourceBusinessObject, targetBusinessObject, controlPoints, sourceLabel, targetLabel);

            }
            return newEdge;
        } else
            return anEdge;
    }
    
    /**
     * The view map is created originally by calling the {@link  #buildWithSavedView(byte[])} 
     * method, but it can change due to user interactions, so it's necessary to update it in order to 
     * export it in other formats. This method wipes the existing view map and builds it again from 
     * whatever it is on the map currently
     */
    public void syncViewMap() {
        this.viewMap.clear();
        if (mxgraphCanvas == null)
            return;
        
        for (Map.Entry<BusinessObjectLight, MxGraphNode> entry : mxgraphCanvas.getNodes().entrySet()) {
            BusinessObjectViewNode aNode = new BusinessObjectViewNode(entry.getKey());
            aNode.getProperties().put("x", entry.getValue().getX());
            aNode.getProperties().put("y", entry.getValue().getY());
            this.viewMap.getNodes().add(aNode);
        };
        
        for (Map.Entry<BusinessObjectLight, MxGraphEdge> entry : mxgraphCanvas.getEdges().entrySet()) {
            BusinessObjectViewEdge anEdge = new BusinessObjectViewEdge(entry.getKey());
            anEdge.getProperties().put("controlPoints", entry.getValue().getPointList());
            anEdge.getProperties().put("sourceLabel", entry.getValue().getSourceLabel());
            anEdge.getProperties().put("targetLabel", entry.getValue().getTargetLabel());
            
            this.viewMap.getEdges().add(anEdge);
            this.viewMap.attachSourceNode(anEdge, new BusinessObjectViewNode(mxgraphCanvas.findSourceEdgeObject(entry.getKey())));
            this.viewMap.attachTargetNode(anEdge, new BusinessObjectViewNode(mxgraphCanvas.findTargetEdgeObject(entry.getKey())));
        };
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
