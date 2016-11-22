/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.connection;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapMarker;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.overlays.NodeMarker;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MapFileWritter {
    String path = "/data/files/map.xml";
    
    public MapFileWritter() {

    }
    
    public void mapWriteMap(List<Connection> edges) {
        GoogleMap googleMap = edges.get(0).getMap();
        List<NodeMarker> nodes = new ArrayList();
        for (GoogleMapMarker marker : googleMap.getMarkers())
            if (marker instanceof NodeMarker)
                nodes.add((NodeMarker) marker);
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew;
            xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qNameMap = new QName("map");
            xmlew.add(xmlef.createStartElement(qNameMap, null, null));
            xmlew.add(xmlef.createAttribute(new QName("centerLat"), Double.toString(googleMap.getCenter().getLat())));
            xmlew.add(xmlef.createAttribute(new QName("centerLon"), Double.toString(googleMap.getCenter().getLon())));
            
            QName qnameNodes = new QName("nodes");
            xmlew.add(xmlef.createStartElement(qnameNodes, null, null));
            for (NodeMarker node : nodes) {
                QName qNameNode = new QName("node");
                xmlew.add(xmlef.createStartElement(qNameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("lat"), Double.toString(node.getPosition().getLat())));
                xmlew.add(xmlef.createAttribute(new QName("lon"), Double.toString(node.getPosition().getLon())));
                xmlew.add(xmlef.createAttribute(new QName("caption"), node.getCaption()));
                xmlew.add(xmlef.createEndElement(qNameNode, null));
            }
            xmlew.add(xmlef.createEndElement(qnameNodes, null));
            
            QName qNameEdges = new QName("edges");
            xmlew.add(xmlef.createStartElement(qNameEdges, null, null));
            
            for (Connection edge : edges) {
                QName qNameEdge = new QName("edge");
                xmlew.add(xmlef.createStartElement(qNameEdge, null, null));
                
                xmlew.add(xmlef.createAttribute(new QName("sourceCaption"), edge.getSource().getCaption()));
                xmlew.add(xmlef.createAttribute(new QName("targetCaption"), edge.getTarget().getCaption()));
                xmlew.add(xmlef.createAttribute(new QName("color"), edge.getConnection().getStrokeColor()));
                
                for (ControlPointMarker controlPoint : edge.getControlPoints()) {
                    QName qNameControlPoint = new QName("controlPoint");
                    xmlew.add(xmlef.createStartElement(qNameControlPoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("lat"), Double.toString(controlPoint.getPosition().getLat())));
                    xmlew.add(xmlef.createAttribute(new QName("lon"), Double.toString(controlPoint.getPosition().getLon())));
                    xmlew.add(xmlef.createAttribute(new QName("iconUrl"), controlPoint.getIconUrl()));
                    xmlew.add(xmlef.createEndElement(qNameControlPoint, null));
                }                
                xmlew.add(xmlef.createEndElement(qNameEdge, null));
            }            
            xmlew.add(xmlef.createEndElement(qNameEdges, null));
            
            xmlew.add(xmlef.createEndElement(qNameMap, null));
            xmlew.close();
            /*
                            List<GoogleMapMarker> markers = new ArrayList();
                for (GoogleMapMarker marker : googleMap.getMarkers())
                    markers.add(marker);
                    
                for (Connection edge : edges) {
                    if (!googleMap.getMarkers().contains(edge.getSource()))
                        googleMap.addMarker(edge.getSource());
                    if (!googleMap.getMarkers().contains(edge.getTarget()))
                        googleMap.addMarker(edge.getTarget());
                        
                    for (GoogleMapMarker controlPoint : edge.getControlPoints())
                        if (!googleMap.getMarkers().contains(controlPoint))
                            googleMap.addMarker(controlPoint);
                        
                    googleMap.removePolyline(edge.getConnection());
                    googleMap.addPolyline(edge.getConnection());
                }
            }
            */
            
            File file = new File(path);            
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
