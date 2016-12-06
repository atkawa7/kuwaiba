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
package org.kuwaiba.custom.map.xml;

import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.LatLon;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.connection.Connection;
import org.kuwaiba.custom.overlays.ControlPointMarker;
import org.kuwaiba.custom.overlays.NodeMarker;
import org.kuwaiba.custom.polyline.Edge;
import org.kuwaiba.polygon.MapPolygon;
import org.kuwaiba.polygon.PolygonExt;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MapFileReader {
    private String path = "/data/files/map.xml";
    private List<NodeMarker> nodes;
    
    public MapFileReader() {
        nodes = new ArrayList();
    }
    
    public MapFileReader(String filename) {
        path = filename;
        nodes = new ArrayList();
    }
    
    public  void readMapFile(List<Connection> edges, List<MapPolygon> mapPolygons, GoogleMap googleMap) {
        try {
            File file = new File(path);
            
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = new byte[(int) file.length()];
            fileInputStream.read(bytes);
            fileInputStream.close();  
            
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            QName qNameMap = new QName("map");            
            QName qNameNode = new QName("node");
            QName qNameEdge = new QName("edge");
            QName qNameControlPoint = new QName("controlPoint");
            QName qNamePolygon = new QName("polygon");
            QName qNameVertex = new QName("vertex");
            
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
                
                long polygonIndex = 0;
                
                while (reader.hasNext()) {
                    int event = reader.next();
                    if (event == XMLStreamConstants.START_ELEMENT) {
                        if (reader.getName().equals(qNameMap)) {
                            double lat = Double.valueOf(reader.getAttributeValue(null, "centerLat"));
                            double lon = Double.valueOf(reader.getAttributeValue(null, "centerLon"));
                            LatLon latlon = new LatLon(lat, lon);
                            googleMap.setCenter(latlon);
                        }
                        if (reader.getName().equals(qNameNode)) {
                            double lat = Double.valueOf(reader.getAttributeValue(null, "lat"));
                            double lon = Double.valueOf(reader.getAttributeValue(null, "lon"));
                            String caption = reader.getAttributeValue(null, "caption");
                            
                            NodeMarker node = new NodeMarker(googleMap, caption, new LatLon(lat, lon), true);
                            googleMap.addMarker(node);
                            googleMap.setInfoWindowContents(node.getInfoWindow(), node.getDeleteButton());
                            
                            nodes.add(node);
                        } // end if node
                        if (reader.getName().equals(qNameEdge)) {
                            Connection connection = new Connection(googleMap, edges);
                            connection.setI(2);
                            
                            String sourceCaption = reader.getAttributeValue(null, "sourceCaption");
                            String targetCaption = reader.getAttributeValue(null, "targetCaption");
                            String color = reader.getAttributeValue(null, "color");
                            
                            NodeMarker source = null;
                            NodeMarker target = null;
                            // find node source and target
                            for (NodeMarker node : nodes) {
                                if (node.getCaption().equals(sourceCaption)) {
                                    source = node;
                                    source.getConnections().add(connection);
                                }
                                if (node.getCaption().equals(targetCaption)) {
                                    target = node;
                                    target.getConnections().add(connection);
                                }
                            }
                            connection.setTarget(target);
                            connection.setSource(source);    
                            
                            List<ControlPointMarker> controlPoints = new ArrayList();
                            while (true) {
                                reader.nextTag();
                                if (reader.getName().equals(qNameControlPoint)) {
                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                        double lat = Double.valueOf(reader.getAttributeValue(null, "lat"));
                                        double lon = Double.valueOf(reader.getAttributeValue(null, "lon"));
                                        String iconUrl = reader.getAttributeValue(null, "iconUrl");
                                        
                                        ControlPointMarker controlPoint = new ControlPointMarker(new LatLon(lat, lon), edges);
                                        controlPoint.setIconUrl(iconUrl);
                                        
                                        controlPoints.add(controlPoint);
                                    }
                                }
                                else {
                                    break;
                                }
                            }                            
                            List<LatLon> coordinates = new ArrayList();
                            // intialize coordinates for polylines
                            for (ControlPointMarker controlPoint : controlPoints) {
                                controlPoint.setConnection(connection);
                                coordinates.add(controlPoint.getPosition());
                            }
                            Edge edge = new Edge(connection, coordinates, color, 1, 5);
                                                        
                            connection.setConnection(edge);
                            connection.setControlPoints(controlPoints);
                            
                            edges.add(connection);
                        } // end if edges
                        if (reader.getName().equals(qNamePolygon)) {
                            PolygonExt polygonExt = new PolygonExt();
//                            GoogleMapPolygon polygon = new GoogleMapPolygon();
                            String color = reader.getAttributeValue(null, "color");
                            
                            while (true) {
                                reader.nextTag();
                                if (reader.getName().equals(qNameVertex)) {
                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                        double lat = Double.valueOf(reader.getAttributeValue(null, "lat"));
                                        double lon = Double.valueOf(reader.getAttributeValue(null, "lon"));
                                        
                                        polygonExt.getCoordinates().add(new LatLon(lat, lon));                                        
                                    }
                                }
                                else
                                    break;
                            }                            
                            //TODO: storage the color 
                            polygonExt.setFillColor(color);
                            polygonExt.setFillOpacity(0.5);
                            polygonExt.setStrokeColor(color);
                            polygonExt.setStrokeOpacity(1);
                            polygonExt.setStrokeWeight(1);
                            
                            MapPolygon mapPolygon = new MapPolygon(googleMap);
                            
                            mapPolygon.setPolygon(polygonExt);
                            
                            mapPolygon.setPolygonId(polygonIndex);
                            polygonIndex += 1;
                            
                            mapPolygons.add(mapPolygon);
                            
                            polygonExt.setMapPolygon(mapPolygon);
                        } // end if polygon
                    } // end if 
                }  // end while
                reader.close();
            } catch(Exception ex) {
                System.err.println("error read xml file" + ex.getMessage());
            }
        }
        catch (Exception ex) {
            System.err.println("error read xml file" + ex.getMessage());
        }
    }
    
    public List<NodeMarker> getNodes() {
        return nodes;
    }
}
