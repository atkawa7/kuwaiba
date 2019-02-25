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

package org.kuwaiba.web.modules.osp;

import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolyline;
import com.vaadin.ui.AbstractComponent;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.views.AbstractView;
import org.kuwaiba.apis.web.gui.views.BusinessObjectViewEdge;
import org.kuwaiba.apis.web.gui.views.BusinessObjectViewNode;
import org.kuwaiba.apis.web.gui.views.ViewMap;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.osp.dashboard.OutsidePlantViewDashboardWidget;
import static org.kuwaiba.web.modules.osp.dashboard.OutsidePlantViewDashboardWidget.VIEW_FORMAT_VERSION;

/**
 * Places a set of selected elements on a map and allows the user to connect and explore them.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class OSPView extends AbstractView<RemoteObjectLight> {

    public OSPView(MetadataEntityManager mem, ApplicationEntityManager aem, BusinessEntityManager bem) {
        super(mem, aem, bem);
    }

    @Override
    public String getName() {
        return "Outside Plant View";
    }

    @Override
    public String getDescription() {
        return "Places a set of selected elements on a map and allows the user to connect and explore them";
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
    public byte[] getAsXML() {
//        try {
//            ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
//            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
//            XMLEventFactory xmlef = XMLEventFactory.newInstance();
//
//            QName qnameView = new QName("view"); //NOI18N
//            xmlew.add(xmlef.createStartElement(qnameView, null, null));
//            xmlew.add(xmlef.createAttribute(new QName("version"), VIEW_FORMAT_VERSION)); //NOI18N
//
//            QName qnameClass = new QName("class"); //NOI18N
//            xmlew.add(xmlef.createStartElement(qnameClass, null, null));
//            xmlew.add(xmlef.createCharacters("OSPView")); //NOI18N
//            xmlew.add(xmlef.createEndElement(qnameClass, null));
//            
//            QName qCenter = new QName("center"); //NOI18N
//            xmlew.add(xmlef.createStartElement(qCenter, null, null));
//            xmlew.add(xmlef.createAttribute(new QName("lon"), Double.toString(mapMain.getCenter().getLon()))); //NOI18N
//            xmlew.add(xmlef.createAttribute(new QName("lat"), Double.toString(mapMain.getCenter().getLat()))); //NOI18N
//            xmlew.add(xmlef.createEndElement(qCenter, null));
//            
//            QName qZoom = new QName("zoom"); //NOI18N
//            xmlew.add(xmlef.createStartElement(qZoom, null, null));
//            xmlew.add(xmlef.createCharacters(String.valueOf(mapMain.getZoom()))); //NOI18N
//            xmlew.add(xmlef.createEndElement(qZoom, null));
//
//            QName qnameNodes = new QName("nodes"); //NOI18N
//            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
//            
//            //First the nodes
//            for (OutsidePlantViewDashboardWidget.OSPNode node : nodes) {
//                QName qnameNode = new QName("node"); //NOI18N
//                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
//                xmlew.add(xmlef.createAttribute(new QName("lon"), Double.toString(node.getMarker().getPosition().getLon()))); //NOI18N
//                xmlew.add(xmlef.createAttribute(new QName("lat"), Double.toString(node.getMarker().getPosition().getLat()))); //NOI18N
//                xmlew.add(xmlef.createAttribute(new QName("class"), node.getBusinessObject().getClassName())); //NOI18N
//                xmlew.add(xmlef.createCharacters(Long.toString(node.getBusinessObject().getId())));
//                xmlew.add(xmlef.createEndElement(qnameNode, null));
//            }
//            xmlew.add(xmlef.createEndElement(qnameNodes, null));
//
//            //Now the connections
//            QName qnameEdges = new QName("edges"); //NOI18N
//            xmlew.add(xmlef.createStartElement(qnameEdges, null, null));
//            
//            for (OutsidePlantViewDashboardWidget.OSPEdge edge : edges) {
//                GoogleMapPolyline lnEdge = edge.getPolyline();
//                QName qnameEdge = new QName("edge"); //NOI18N
//                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
//                xmlew.add(xmlef.createAttribute(new QName("id"), Long.toString(edge.getBusinessObject().getId()))); //NOI18N
//                xmlew.add(xmlef.createAttribute(new QName("class"), edge.getBusinessObject().getClassName())); //NOI18N
//
//                xmlew.add(xmlef.createAttribute(new QName("aside"), String.valueOf(edge.getSourceObject().getBusinessObject().getId()))); //NOI18N
//                xmlew.add(xmlef.createAttribute(new QName("bside"), String.valueOf(edge.getTargetObject().getBusinessObject().getId()))); //NOI18N
//                
//                for (LatLon point : lnEdge.getCoordinates()) {
//                    QName qnameControlpoint = new QName("controlpoint"); //NOI18N
//                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
//                    xmlew.add(xmlef.createAttribute(new QName("lon"), Double.toString(point.getLon()))); //NOI18N
//                    xmlew.add(xmlef.createAttribute(new QName("lat"), Double.toString(point.getLat()))); //NOI18N
//                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
//                }
//                xmlew.add(xmlef.createEndElement(qnameEdge, null));
//            }
//            xmlew.add(xmlef.createEndElement(qnameEdges, null));
//            xmlew.add(xmlef.createEndElement(qnameView, null));
//            xmlew.close();
//            return baos.toByteArray();
//        } catch (XMLStreamException ex) { 
//            //Should not happen
//            return new byte[0];
//        }
return null;
    }

    @Override
    public byte[] getAsImage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractComponent getAsComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void buildWithBusinessObject(RemoteObjectLight businessObject) {
        throw new UnsupportedOperationException("This view can not be built from an object"); 
    }

    @Override
    public void buildWithSavedView(byte[] theSavedView) {
        this.viewMap = new ViewMap();
        
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
                        double lon = Double.valueOf(reader.getAttributeValue(null,"lon")); //NOI18N
                        double lat = Double.valueOf(reader.getAttributeValue(null,"lat")); //NOI18N
                        long objectId = Long.valueOf(reader.getElementText());

                        try {
                            //We check if the object still exists
                            BusinessObjectLight anOject = bem.getObjectLight(objectClass, objectId);
                            BusinessObjectViewNode aNode = new BusinessObjectViewNode(anOject);
                            aNode.getProperties().put("lat", lat);
                            aNode.getProperties().put("lon", lon);
                            this.viewMap.addNode(aNode);
                        } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                            Notifications.showError(String.format("The object of class %s and id %s could not be found", objectClass, objectId));
                        }
                    } else {
                        if (reader.getName().equals(qEdge)) {
                            long objectId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N
                            long aSide = Long.valueOf(reader.getAttributeValue(null, "aside")); //NOI18N
                            long bSide = Long.valueOf(reader.getAttributeValue(null, "bside")); //NOI18N
                            String objectClass = reader.getAttributeValue(null,"class"); //NOI18N
                            try {
                                BusinessObjectLight anObject = bem.getObjectLight(objectClass, objectId);
                                BusinessObjectViewEdge anEdge = new BusinessObjectViewEdge(anObject);

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
                                this.viewMap.addEdge(anEdge);
                            } catch (MetadataObjectNotFoundException | BusinessObjectNotFoundException ex) {
                                Notifications.showError(String.format("The object of class %s and id %s could not be found", objectClass, objectId));
                            }

                        } else {
                            if (reader.getName().equals(qLabel)) {
                                //Unused for now
                            } else {
                                if (reader.getName().equals(qZoom))
                                    this.viewMap.getSettings().put("zoom", Integer.valueOf(reader.getElementText())); //NOI18N
                                else {
                                    if (reader.getName().equals(qCenter)) {
                                        double lon = Double.valueOf(reader.getAttributeValue(null, "lon")); //NOI18N
                                        double lat = Double.valueOf(reader.getAttributeValue(null, "lat")); //NOI18N
                                        this.viewMap.getSettings().put("center", new GeoCoordinate(lat, lon)); //NOI18N
                                    } 
                                }
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception ex) {
            Notifications.showError(String.format("An unexpected error appeared while parsing the OSP view: " + ex.getLocalizedMessage()));
        }
    }
    
    
}
