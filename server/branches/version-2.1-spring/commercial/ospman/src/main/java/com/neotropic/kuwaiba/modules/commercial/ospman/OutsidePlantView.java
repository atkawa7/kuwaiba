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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import com.neotropic.kuwaiba.modules.commercial.ospman.commands.CommandAddMarker;
import com.neotropic.kuwaiba.modules.commercial.ospman.commands.CommandAddConnection;
import com.neotropic.kuwaiba.modules.commercial.ospman.dialogs.DialogDeleteOSPView;
import org.neotropic.util.visual.views.AbstractView;
import org.neotropic.util.visual.views.AbstractViewEdge;
import org.neotropic.util.visual.views.AbstractViewNode;
import org.neotropic.kuwaiba.visualization.views.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.views.BusinessObjectViewNode;
import org.neotropic.util.visual.views.ViewEventListener;
import org.neotropic.util.visual.views.ViewMap;
import org.neotropic.util.visual.views.util.UtilHtml;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
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
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.views.ViewNodeIconGenerator;
import org.neotropic.util.visual.dialog.ConfirmDialog;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.tools.ToolRegister;

/**
 * Places a set of selected elements on a map and allows the user to connect and explore them.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class OutsidePlantView extends AbstractView<BusinessObjectLight> {
    /**
     * The map provider used to render this view.
     */
    private AbstractMapProvider mapProvider;
    private final TranslationService ts;
    private final ApplicationEntityManager aem;
    private final BusinessEntityManager bem;
    private final MetadataEntityManager mem;
    private final ViewNodeIconGenerator iconGenerator;
    private final Command cmdParentBack;
    
    public OutsidePlantView(
        MetadataEntityManager mem, 
        ApplicationEntityManager aem, 
        BusinessEntityManager bem, 
        TranslationService ts, 
        ViewNodeIconGenerator iconGenerator,
        Command cmdParentBack) {
        
        this.aem = aem;
        this.bem = bem;
        this.mem = mem;
        this.ts = ts;
        this.iconGenerator = iconGenerator;
        this.cmdParentBack = cmdParentBack;
    }

    @Override
    public String getName() {
        return "Outside Plant View";
    }

    @Override
    public String getDescription() {
        return "Places a set of selected elements on a map and allows the user to connect and explore them.";
    }

    @Override
    public String getVersion() {
        return "1.2";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }

    @Override
    public byte[] getAsXml() {        
        //First we make sure that the view map reflects the graph in the map. 
        //If syncViewMap is not called, the XML document generated by this method will 
        //correspond to the latest loaded version of the map.
        this.syncViewMap();
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();

            QName qnameView = new QName("view"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), OutsidePlantConstants.VIEW_VERSION)); //NOI18N

            QName qnameClass = new QName("class"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
            xmlew.add(xmlef.createCharacters("OSPView")); //NOI18N
            xmlew.add(xmlef.createEndElement(qnameClass, null));
            
            QName qCenter = new QName("center"); //NOI18N
            xmlew.add(xmlef.createStartElement(qCenter, null, null));
            xmlew.add(xmlef.createAttribute(new QName("lon"), Double.toString(((GeoCoordinate)this.viewMap.getProperties().get("center")).getLongitude()))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("lat"), Double.toString(((GeoCoordinate)this.viewMap.getProperties().get("center")).getLatitude()))); //NOI18N
            xmlew.add(xmlef.createEndElement(qCenter, null));
            
            QName qZoom = new QName("zoom"); //NOI18N
            xmlew.add(xmlef.createStartElement(qZoom, null, null));
            xmlew.add(xmlef.createCharacters(String.valueOf(this.viewMap.getProperties().get("zoom")))); //NOI18N
            xmlew.add(xmlef.createEndElement(qZoom, null));

            QName qnameNodes = new QName("nodes"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            
            //First the nodes
            for (AbstractViewNode node : this.viewMap.getNodes()) {
                QName qnameNode = new QName("node"); //NOI18N
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("lon"), String.valueOf(node.getProperties().get("lon")))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("lat"), String.valueOf(node.getProperties().get("lat")))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("class"), ((BusinessObjectLight)node.getIdentifier()).getClassName())); //NOI18N
                xmlew.add(xmlef.createCharacters(String.valueOf(((BusinessObjectLight)node.getIdentifier()).getId())));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));

            //Now the connections
            QName qnameEdges = new QName("edges"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
            
            for (AbstractViewEdge edge : this.viewMap.getEdges()) {
                QName qnameEdge = new QName("edge"); //NOI18N
                BusinessObjectLight theObject = (BusinessObjectLight)edge.getIdentifier();
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                xmlew.add(xmlef.createAttribute(new QName("id"), String.valueOf(theObject.getId()))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("class"), theObject.getClassName())); //NOI18N
                
                BusinessObjectLight sourceObject = (BusinessObjectLight)this.viewMap.getEdgeSource(edge).getIdentifier();
                BusinessObjectLight targetObject = (BusinessObjectLight)this.viewMap.getEdgeTarget(edge).getIdentifier();
                xmlew.add(xmlef.createAttribute(new QName("asideid"), String.valueOf(sourceObject.getId()))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("asideclass"), sourceObject.getClassName())); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("bsideid"), String.valueOf(targetObject.getId()))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("bsideclass"), targetObject.getClassName())); //NOI18N
                
                for (GeoCoordinate controlPoint : (List<GeoCoordinate>)edge.getProperties().get("controlPoints")) {
                    QName qnameControlpoint = new QName("controlpoint"); //NOI18N
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("lon"), String.valueOf(controlPoint.getLongitude()))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("lat"), String.valueOf(controlPoint.getLatitude()))); //NOI18N
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                }
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            }
            xmlew.add(xmlef.createEndElement(qnameEdges, null));
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            
            return baos.toByteArray();
        } catch (XMLStreamException ex) { 
            //Should not happen
            return new byte[0];
        }
    }

    @Override
    public byte[] getAsImage() { //Should use Google Maps Static API in the future
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Component getAsComponent() throws InvalidArgumentException {
        try {
            Properties mapProperties = new Properties();
            mapProperties.put("center", this.viewMap.getProperties().get("center"));
            mapProperties.put("zoom", this.viewMap.getProperties().get("zoom"));
                    
            if (this.mapProvider == null) {
                Class mapsProviderClass = Class.forName((String)aem.getConfigurationVariableValue("general.maps.provider"));
                if (AbstractMapProvider.class.isAssignableFrom(mapsProviderClass)) {
                    
                    try {
                        mapProperties.put("apiKey", aem.getConfigurationVariableValue("general.maps.apiKey"));
                    } catch (InventoryException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            "The configuration variable general.maps.apiKey has not been set. The default map will be used"
                        ).open();
                    }

                    try {
                        mapProperties.put("language", aem.getConfigurationVariableValue("general.maps.language"));
                    } catch (InventoryException ex) {
                        mapProperties.put("language", OutsidePlantConstants.DEFAULT_LANGUAGE);
                    }
                    mapProperties.put("bem", bem);
                    
                    this.mapProvider = (AbstractMapProvider)mapsProviderClass.newInstance();
                    this.mapProvider.initialize(mapProperties);
                } else
                    return new Label(String.format("Class %s is not a valid map provider", mapsProviderClass.getCanonicalName()));
            } else {
                this.mapProvider.clear();
                this.mapProvider.reload(mapProperties);
            }
            for (AbstractViewNode node : this.viewMap.getNodes()) {
                BusinessObjectLight businessObject = (BusinessObjectLight)node.getIdentifier();
                this.mapProvider.addMarker(businessObject, 
                    new GeoCoordinate((double)node.getProperties().get("lat"),  //NOI18N
                    (double)node.getProperties().get("lon")), 
                    StreamResourceRegistry.getURI(iconGenerator.apply((BusinessObjectViewNode) node)).toString()); //NOI18N
            }

            for (AbstractViewEdge edge : this.viewMap.getEdges()) {
                BusinessObjectLight businessObject = (BusinessObjectLight)edge.getIdentifier();
                this.mapProvider.addPolyline(businessObject, (BusinessObjectLight)this.viewMap.getEdgeSource(edge).getIdentifier(), 
                        (BusinessObjectLight)this.viewMap.getEdgeTarget(edge).getIdentifier(), (List<GeoCoordinate>)edge.getProperties().get("controlPoints"), edge.getProperties()); //NOI18N
            }
            addToolManager(this.mapProvider);
            return this.mapProvider.getComponent();
        } catch (Exception ex) {
            return new Label(String.format("An unexpected error occurred while loading the OSP view: %s", ex.getLocalizedMessage()));
        }
    }
    
    private void addToolManager(AbstractMapProvider mapProvider) {
        if (mapProvider instanceof ToolRegister && 
            mapProvider.getComponent() instanceof HasComponents) {
            
            OutsidePlantTools outsidePlantTools = new OutsidePlantTools(bem, ts, (AbstractMapProvider) mapProvider, (ToolRegister) mapProvider);
            ((HasComponents) mapProvider.getComponent()).add(outsidePlantTools);
            
            outsidePlantTools.setAddmarkerCommand(new CommandAddMarker() {
                @Override
                public void execute() {
                    BusinessObjectViewNode viewNode = new BusinessObjectViewNode(getBussinesObject());
                    viewNode.getProperties().put("lat", getLat()); //NOI18N
                    viewNode.getProperties().put("lng", getLng()); //NOI18N
                    
                    viewMap.addNode(viewNode);
                    
                    mapProvider.addMarker(getBussinesObject(), 
                        new GeoCoordinate(getLat(), getLng()), 
                        StreamResourceRegistry.getURI(iconGenerator.apply(viewNode)).toString()
                    );
                }
            });
            outsidePlantTools.setAddPolylineCommand(new CommandAddConnection() {
                @Override
                public void execute() {
                try {
                    BusinessObjectLight tmp = new BusinessObjectLight("OpticalLink", UUID.randomUUID().toString(), null); //NOI18N

                    BusinessObjectViewEdge viewEdge = new BusinessObjectViewEdge(tmp);
                    viewEdge.getProperties().put("controlPoints", getPath()); //NOI18N
                    viewEdge.getProperties().put("color", //NOI18N
                        UtilHtml.toHexString(new Color(mem.getClass(tmp.getClassName()).getColor())));

                    viewMap.addEdge(viewEdge);
                    viewMap.attachSourceNode(viewEdge, viewMap.getNode(getSource()));
                    viewMap.attachTargetNode(viewEdge, viewMap.getNode(getTarget()));

                    mapProvider.addPolyline(tmp, getSource(), getTarget(), getPath(), properties);
                } catch (MetadataObjectNotFoundException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()
                    ).open();
                }
                }
            });
            
            outsidePlantTools.setSaveCommand(() -> {
                if (viewMap.getNodes().isEmpty()) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.information"), 
                        ts.getTranslatedString("module.ospman.empty-view")
                    ).open();
                } else
                    saveOSPView();
            });
            outsidePlantTools.setBackCommand(() -> {
                if (!viewMap.getNodes().isEmpty())
                    saveOSPView();
                cmdParentBack.execute();
            });
            outsidePlantTools.setDeleteCommand(() -> {
                if (!this.getProperties().get(Constants.PROPERTY_ID).equals(-1)) {
                    DialogDeleteOSPView confirmDialog = new DialogDeleteOSPView((long) this.getProperties().get(Constants.PROPERTY_ID), ts, aem);
                    confirmDialog.open();
                    confirmDialog.addDialogCloseActionListener(event -> 
                        cmdParentBack.execute()
                    );
                }
            });
        }
    }
    
    private void saveOSPView() {
        FormLayout fly = new FormLayout();
        TextField txtName = new TextField();
        txtName.setValue(this.getProperties().getProperty(Constants.PROPERTY_NAME) == null ? 
            "" : this.getProperties().getProperty(Constants.PROPERTY_NAME));
        TextField txtDescription = new TextField();
        txtDescription.setValue(this.getProperties().getProperty(Constants.PROPERTY_DESCRIPTION) == null ? 
            "" : this.getProperties().getProperty(Constants.PROPERTY_DESCRIPTION));
        fly.addFormItem(txtName, ts.getTranslatedString("module.general.labels.name"));
        fly.addFormItem(txtDescription, ts.getTranslatedString("module.general.labels.description"));

        ConfirmDialog confirmDialog = new ConfirmDialog(ts, 
            ts.getTranslatedString("module.ospman.save-view"), fly, 
            ts.getTranslatedString("module.general.messages.ok"), () -> {
                try {
                    if (this.properties.get(Constants.PROPERTY_ID).equals(-1)) {
                        long newOSPViewId = aem.createOSPView(txtName.getValue(), txtDescription.getValue(), this.getAsXml());
                        this.getProperties().put(Constants.PROPERTY_ID, newOSPViewId);
                    } else {
                        aem.updateOSPView((long) this.getProperties().get(Constants.PROPERTY_ID), 
                            txtName.getValue(), txtDescription.getValue(), this.getAsXml());
                    }
                    this.getProperties().put(Constants.PROPERTY_NAME, txtName.getValue());
                    this.getProperties().put(Constants.PROPERTY_DESCRIPTION, txtDescription.getValue());
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.success"), 
                        ts.getTranslatedString("module.ospman.view-saved")
                    ).open();
                } catch (InventoryException ex) {
                    new SimpleNotification(
                        ts.getTranslatedString("module.general.messages.error"), 
                        ex.getLocalizedMessage()
                    ).open();
                }
            }
        );
        confirmDialog.open();
    }
    
    @Override
    public void buildWithBusinessObject(BusinessObjectLight businessObject) {
        throw new UnsupportedOperationException("This view can not be built from an object. Use buildWithSavedView instead."); 
    }

    @Override
    public void buildWithSavedView(byte[] theSavedView) {
        if (this.viewMap == null) 
            this.viewMap = new ViewMap();
        else 
            this.viewMap.clear();
        
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qZoom = new QName("zoom"); //NOI18N
            QName qCenter = new QName("center"); //NOI18N
            QName qNode = new QName("node"); //NOI18N
            QName qEdge = new QName("edge"); //NOI18N
            QName qLabel = new QName("label"); //NOI18N
            QName qControlPoint = new QName("controlpoint"); //NOI18N

            ByteArrayInputStream bais = new ByteArrayInputStream(theSavedView);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(qNode)) {
                        String objectClass = reader.getAttributeValue(null, "class"); //NOI18N
                        double lon = Double.valueOf(reader.getAttributeValue(null, "lon")); //NOI18N
                        double lat = Double.valueOf(reader.getAttributeValue(null, "lat")); //NOI18N
                        String objectId = reader.getElementText();

                        try {
                            //We check if the object still exists
                            BusinessObjectLight anOject = bem.getObjectLight(objectClass, objectId);
                            BusinessObjectViewNode aNode = new BusinessObjectViewNode(anOject);
                            aNode.getProperties().put("lat", lat);
                            aNode.getProperties().put("lon", lon);
                            this.viewMap.addNode(aNode);
                        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                            new SimpleNotification(
                                ts.getTranslatedString("module.general.messages.error"), 
                                String.format("The object of class %s and id %s could not be found", objectClass, objectId)
                            ).open();
                        }
                    } else {
                        if (reader.getName().equals(qEdge)) {
                            String objectId = reader.getAttributeValue(null, "id"); //NOI18N
                            String aSideId = reader.getAttributeValue(null, "asideid"); //NOI18N
                            String aSideClass = reader.getAttributeValue(null, "asideclass"); //NOI18N
                            String bSideId = reader.getAttributeValue(null, "bsideid"); //NOI18N
                            String bSideClass = reader.getAttributeValue(null, "bsideclass"); //NOI18N
                            String objectClass = reader.getAttributeValue(null,"class"); //NOI18N
                            try {
                                BusinessObjectViewEdge anEdge = new BusinessObjectViewEdge(bem.getObjectLight(objectClass, objectId));
                                AbstractViewNode sourceNode = this.viewMap.getNode(bem.getObjectLight(aSideClass, aSideId));
                                AbstractViewNode targetNode = this.viewMap.getNode(bem.getObjectLight(bSideClass, bSideId));
                                
                                List<GeoCoordinate> controlPoints = new ArrayList<>();

                                while(true) {
                                    reader.nextTag();
                                    if (reader.getName().equals(qControlPoint)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                            controlPoints.add(new GeoCoordinate(Double.valueOf(reader.getAttributeValue(null,"lat")), 
                                                    Double.valueOf(reader.getAttributeValue(null,"lon"))));
                                    } else
                                        break;
                                }
                                anEdge.getProperties().put("controlPoints", controlPoints); //NOI18N
                                anEdge.getProperties().put("color", UtilHtml.toHexString(new Color(mem.getClass(objectClass).getColor())));
                                
                                this.viewMap.addEdge(anEdge);
                                this.viewMap.attachSourceNode(anEdge, sourceNode);
                                this.viewMap.attachTargetNode(anEdge, targetNode);
                            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                                new SimpleNotification(
                                    ts.getTranslatedString("module.general.messages.error"), 
                                    String.format("The object of class %s and id %s could not be found", objectClass, objectId)
                                ).open();
                            }
                        } else {
                            if (reader.getName().equals(qLabel)) {
                                //Unused for now
                            } else {
                                if (reader.getName().equals(qZoom))
                                    this.viewMap.getProperties().put("zoom", Integer.valueOf(reader.getElementText())); //NOI18N
                                else {
                                    if (reader.getName().equals(qCenter)) {
                                        double lon = Double.valueOf(reader.getAttributeValue(null, "lon")); //NOI18N
                                        double lat = Double.valueOf(reader.getAttributeValue(null, "lat")); //NOI18N
                                        this.viewMap.getProperties().put("center", new GeoCoordinate(lat, lon)); //NOI18N
                                    } 
                                }
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                String.format("An unexpected error appeared while parsing the OSP view: " + ex.getLocalizedMessage())
            ).open();
        }
    }
    
    @Override
    public void buildEmptyView() {
        this.getProperties().put(Constants.PROPERTY_ID, -1);
        this.getProperties().put(Constants.PROPERTY_NAME, "");
        this.getProperties().put(Constants.PROPERTY_DESCRIPTION, "");
        
        if (this.viewMap == null)
            this.viewMap = new ViewMap();
        else
            this.viewMap.clear();
        
        double mapCenterLatitude, mapCenterLongitude;
                
        try {
            mapCenterLatitude = (double)aem.getConfigurationVariableValue("widgets.simplemap.centerLatitude");
        } catch (InventoryException | NumberFormatException ex) {
            mapCenterLatitude = OutsidePlantConstants.DEFAULT_CENTER_LATITUDE;
        }

        try {
            mapCenterLongitude = (double)aem.getConfigurationVariableValue("widgets.simplemap.centerLongitude");
        } catch (InventoryException | NumberFormatException ex) {
            mapCenterLongitude = OutsidePlantConstants.DEFAULT_CENTER_LONGITUDE;
        }
        
        this.viewMap.getProperties().put("center", new GeoCoordinate(mapCenterLatitude, mapCenterLongitude));
        
        try {
            this.viewMap.getProperties().put("zoom", aem.getConfigurationVariableValue("widgets.simplemap.zoom"));
        } catch (InventoryException | NumberFormatException ex) {
            this.viewMap.getProperties().put("zoom", OutsidePlantConstants.DEFAULT_ZOOM);
        }
    }
    
    /**
     * The view map is created originally by calling the {@link  #buildWithSavedView(byte[])} 
     * method, but it can change due to user interactions, so it's necessary to update it in order to 
     * export it in other formats. This method wipes the existing view map and builds it again from 
     * whatever it is on the map currently
     */
    private void syncViewMap() {
        this.viewMap.clear();
        if (mapProvider == null)
            return;
        
        this.viewMap.getProperties().put("center", new GeoCoordinate(mapProvider.getCenter().getLatitude(), 
                mapProvider.getCenter().getLongitude()));
        
        this.viewMap.getProperties().put("zoom", mapProvider.getZoom());
        
        mapProvider.getMarkers().forEach((aMarker) -> {
            BusinessObjectViewNode aNode = new BusinessObjectViewNode(aMarker.getBusinessObject());
            aNode.getProperties().put("lat", aMarker.getLocation().getLatitude());
            aNode.getProperties().put("lon", aMarker.getLocation().getLongitude());
            this.viewMap.getNodes().add(aNode);
        });
        
        mapProvider.getPolylines().forEach((aPolyline) -> {
            BusinessObjectViewEdge anEdge = new BusinessObjectViewEdge(aPolyline.getBusinessObject());
            anEdge.getProperties().put("controlPoints", aPolyline.getControlPoints());
            
            this.viewMap.getEdges().add(anEdge);
            this.viewMap.attachSourceNode(anEdge, new BusinessObjectViewNode(aPolyline.getSourceObject()));
            this.viewMap.attachTargetNode(anEdge, new BusinessObjectViewNode(aPolyline.getTargetObject()));
        });
    }

    @Override
    public AbstractViewNode addNode(BusinessObjectLight businessObject, Properties properties) {
        AbstractViewNode aNode = this.viewMap.findNode(businessObject.getId());
        if (aNode == null) {
            BusinessObjectLight localObject = new BusinessObjectLight(businessObject.getClassName(), businessObject.getId(), businessObject.getName());
            BusinessObjectViewNode newNode = new BusinessObjectViewNode(localObject);
            this.viewMap.addNode(newNode);
            if (this.mapProvider != null) { //The view could be created without a graphical representation (the map). so here we make sure that's not the case
                GeoCoordinate position = (GeoCoordinate)properties.get("position");
                this.mapProvider.addMarker(localObject, 
                    position == null ? this.mapProvider.getCenter() : position, 
                    StreamResourceRegistry.getURI(iconGenerator.apply(newNode)).toString());
            }
            return newNode;
        } else
            return aNode;
    }

    @Override
    public AbstractViewEdge addEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceBusinessObject, BusinessObjectLight targetBusinessObject, Properties properties) {
        AbstractViewEdge anEdge = this.viewMap.findEdge(businessObject.getId());
        if (anEdge == null) {
            BusinessObjectLight localObject = new BusinessObjectLight(businessObject.getClassName(), businessObject.getId(), businessObject.getName());
            BusinessObjectViewEdge newEdge = new BusinessObjectViewEdge(localObject);
            
            //if any of the end points is missing, the edge is not added
            AbstractViewNode aSourceNode = this.viewMap.findNode(sourceBusinessObject.getId());
            if (aSourceNode == null)
                return null;
            
            AbstractViewNode aTargetNode = this.viewMap.findNode(targetBusinessObject.getId());
            if (aTargetNode == null)
                return null;
            
            this.viewMap.addEdge(newEdge);
            this.viewMap.attachSourceNode(anEdge, aSourceNode);
            this.viewMap.attachTargetNode(anEdge, aTargetNode);
            if (this.mapProvider != null) { //The view could be created without a graphical representation (the map). so here we make sure that's not the case
                List<GeoCoordinate> controlPoints = (List<GeoCoordinate>)properties.get("controlPoints");
                this.mapProvider.addPolyline(localObject, (BusinessObjectLight)aSourceNode.getIdentifier(), 
                        (BusinessObjectLight)aTargetNode.getIdentifier(), controlPoints == null ? new ArrayList<>() : controlPoints, properties);
            }
            return newEdge;
        } else
            return anEdge;
    }

    @Override
    public void addNodeClickListener(ViewEventListener listener) {
        this.mapProvider.addMarkerClickListener(listener);
    }

    @Override
    public void addEdgeClickListener(ViewEventListener listener) {
        this.mapProvider.addPolylineClickListener(listener);
    }
    
}