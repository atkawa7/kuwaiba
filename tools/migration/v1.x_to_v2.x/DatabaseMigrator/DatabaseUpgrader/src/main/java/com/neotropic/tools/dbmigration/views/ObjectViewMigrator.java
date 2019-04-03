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
package com.neotropic.tools.dbmigration.views;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * This migrator upgrades the long-ids based views to the new that use strings.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ObjectViewMigrator {

    /**
     * Performs the actual migration
     * @param dbPathReference The reference to the database location.
     */
    public static void migrate(File dbPathReference) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPathReference);
        System.out.println("Migrating Object Views...");
        RelationshipType hasViewRelType = new RelationshipType() {
                    @Override
                    public String name() {
                        return "HAS_VIEW";
                    }
                };
        
        try (Transaction tx = graphDb.beginTx()) {
            graphDb.findNodes(Label.label("inventoryObjects")).stream().forEach((anObjectNode) -> {
                if (anObjectNode.hasRelationship(hasViewRelType))
                    System.out.println(String.format("Processing object view for %s (%s)", anObjectNode.getProperty("name"), anObjectNode.getId()));
                anObjectNode.getRelationships(hasViewRelType).forEach((aHasViewRelationship) -> { // In reality, there should be only one
                    Node aViewNode = aHasViewRelationship.getEndNode();
                    byte[] structure = (byte[])aViewNode.getProperty("structure");
                    try {
                        ViewMap parsedView = parseView(structure);
                        byte[] migratedStructure = migrateViewMap(parsedView, graphDb);
                        try {
                            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/oview_" + anObjectNode.getId() + ".xml");
                            fos.write(migratedStructure);
                            fos.close();
                        } catch(Exception e) {}
                        aViewNode.setProperty("structure", migratedStructure);
                    } catch (XMLStreamException ex) {
                        System.out.println(String.format("Unexpected error processing object view for %s (%s): %s", 
                                anObjectNode.getProperty("name"), anObjectNode.getId(), ex.getMessage()));
                    } catch (OtherKinfOfViewException okovex) { //Ignore all the views that are not object views
                        //System.out.println(okovex.getMessage());
                    }
                });
            });
            tx.success();
        }
    }
    
    /**
     * Parses an existing view into a set of Java objects.
     * @param structure The byte array with the view contents.
     * @return A view as a Java object
     * @throws Exception If there was a problem parsing the XML document.
     */
    private static ViewMap parseView(byte[] structure) throws XMLStreamException, OtherKinfOfViewException {
        ViewMap res = new ViewMap();
        
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        QName qClass = new QName("class"); //NOI18N
        QName qNode = new QName("node"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qControlPoint = new QName("controlpoint"); //NOI18N

        ByteArrayInputStream bais = new ByteArrayInputStream(structure);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (reader.getName().equals(qClass)) {
                    String viewClass = reader.getElementText();
                    if (!viewClass.equals("DefaultView"))
                        throw new OtherKinfOfViewException(String.format("%s detected and ignored (it will be processed later on)", viewClass));
                }
                
                if (reader.getName().equals(qNode)) {
                    String objectClass = reader.getAttributeValue(null, "class"); //NOI18N
                    int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue(); //NOI18N
                    int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue(); //NOI18N
                    long objectId = Long.valueOf(reader.getElementText());
                    
                    res.nodes.add(new ViewNode(objectId, objectClass, new Point(xCoordinate, yCoordinate)));
                    
                } else {
                    if (reader.getName().equals(qEdge)) {
                        long objectId = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N
                        long aSide = Long.valueOf(reader.getAttributeValue(null, "aside")); //NOI18N
                        long bSide = Long.valueOf(reader.getAttributeValue(null, "bside")); //NOI18N
                        String objectClass = reader.getAttributeValue(null,"class"); //NOI18N
                        
                        ViewEdge newEdge = new ViewEdge(objectId, objectClass, aSide, bSide);

                        while(true) {
                            reader.nextTag();
                            if (reader.getName().equals(qControlPoint)) {
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                    newEdge.controlPoints.add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), 
                                            Integer.valueOf(reader.getAttributeValue(null,"y"))));
                            } else 
                                break;
                        }
                        res.edges.add(newEdge);
                    }
                }
            }
        }
        reader.close();
        return res;
    }
    
    /**
     * Converts an in-memory view structure into a saveable XML document that 
     * uses string ids.
     * @param viewMap The input in-memory structure.
     * @return The resulting XML document
     */
    private static byte[] migrateViewMap(ViewMap viewMap, GraphDatabaseService graphDb) throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
        XMLEventFactory xmlef = XMLEventFactory.newInstance();

        QName qnameView = new QName("view"); //NOI18N
        xmlew.add(xmlef.createStartElement(qnameView, null, null));
        xmlew.add(xmlef.createAttribute(new QName("version"), "1.2")); //NOI18N

        QName qnameClass = new QName("class"); //NOI18N
        xmlew.add(xmlef.createStartElement(qnameClass, null, null));
        xmlew.add(xmlef.createCharacters("ObjectView")); //NOI18N
        xmlew.add(xmlef.createEndElement(qnameClass, null));

        QName qnameNodes = new QName("nodes"); //NOI18N
        xmlew.add(xmlef.createStartElement(qnameNodes, null, null));

        for (ViewNode aNode : viewMap.getNodes()) {
            try {
                Node anObjectNode = graphDb.getNodeById(aNode.getId());
                if (!anObjectNode.hasProperty("_uuid"))
                    throw new NodeIdReusedException(String.format("The node with id %s is reusing an id formerly used by another inventory object. This node will be ignored.", 
                        anObjectNode.getId()));
                
                QName qnameNode = new QName("node"); //NOI18N
                xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                xmlew.add(xmlef.createAttribute(new QName("x"), String.valueOf(aNode.getPosition().x))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("y"), String.valueOf(aNode.getPosition().y))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("class"), aNode.getClassName())); //NOI18N
                xmlew.add(xmlef.createCharacters((String)anObjectNode.getProperty("_uuid")));
                xmlew.add(xmlef.createEndElement(qnameNode, null));
            } catch (NotFoundException ex) {
                System.out.println(String.format("The object of class %s and id %s could not be found and will not be added to the view.", 
                        aNode.getClassName(), aNode.getId()));
            } catch (NodeIdReusedException nre) {
                System.out.println(nre.getMessage());
            }
        }
        xmlew.add(xmlef.createEndElement(qnameNodes, null));

        QName qnameEdges = new QName("edges"); //NOI18N
        xmlew.add(xmlef.createStartElement(qnameEdges, null, null));

        for (ViewEdge anEdge : viewMap.getEdges()) {
            try {
                Node anObjectNode = graphDb.getNodeById(anEdge.getId());
                Node aSideObjectNode = graphDb.getNodeById(anEdge.getaSide());
                Node bSideObjectNode = graphDb.getNodeById(anEdge.getbSide());
                
                if (!anObjectNode.hasProperty("_uuid"))
                    throw new NodeIdReusedException(String.format("The node with id %s is reusing an id formerly used by another inventory object. This edge will be ignored.", 
                        anObjectNode.getId()));
                if (!aSideObjectNode.hasProperty("_uuid"))
                    throw new NodeIdReusedException(String.format("The node with id %s is reusing an id formerly used by another inventory object. This edge will be ignored.", 
                        aSideObjectNode.getId()));
                if (!bSideObjectNode.hasProperty("_uuid"))
                    throw new NodeIdReusedException(String.format("The node with id %s is reusing an id formerly used by another inventory object. This edge will be ignored.", 
                        bSideObjectNode.getId()));
                
                QName qnameEdge = new QName("edge"); //NOI18N
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                xmlew.add(xmlef.createAttribute(new QName("id"), (String)anObjectNode.getProperty("_uuid"))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("class"), anEdge.getClassName())); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("aside"), (String)aSideObjectNode.getProperty("_uuid"))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("bside"), (String)bSideObjectNode.getProperty("_uuid"))); //NOI18N
                for (Point point : anEdge.getControlPoints()) {
                    QName qnameControlpoint = new QName("controlpoint"); //NOI18N
                    xmlew.add(xmlef.createStartElement(qnameControlpoint, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(point.x))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(point.y))); //NOI18N
                    xmlew.add(xmlef.createEndElement(qnameControlpoint, null));
                }
                xmlew.add(xmlef.createEndElement(qnameEdge, null));
            } catch (NotFoundException ex) {
                System.out.println(String.format("The object of class %s and id %s will not be added to the view because either itself of one of its endpoints is not present anymore: %s.", 
                        anEdge.getClassName(), anEdge.getId(), ex.getMessage()));
            } catch (NodeIdReusedException nre) {
                System.out.println(nre.getMessage());
            }
        }
        xmlew.add(xmlef.createEndElement(qnameEdges, null));

        xmlew.add(xmlef.createEndElement(qnameView, null));
        xmlew.close();
        return baos.toByteArray();
    }
    
    /**
     * Class that represents the nodes and edges structure of the view as a Java object.
     */
    private static class ViewMap {
        /**
         * The nodes in the view as Java objects.
         */
        private List<ViewNode> nodes;
        /**
         * The list of connections in the view.
         */
        private List<ViewEdge> edges;

        public ViewMap() {
            this.nodes = new ArrayList<>();
            this.edges = new ArrayList<>();
        }

        public List<ViewNode> getNodes() {
            return nodes;
        }

        public void setNodes(List<ViewNode> nodes) {
            this.nodes = nodes;
        }

        public List<ViewEdge> getEdges() {
            return edges;
        }

        public void setEdges(List<ViewEdge> edges) {
            this.edges = edges;
        }
        
    }
    
    private static class ViewNode { 
        /**
         * Legacy business object id.
         */
        private long id;
        /**
         * Business object class.
         */
        private String className;
        /**
         * The Cartesian position of the node.
         */
        private Point position;

        public ViewNode(long id, String className, Point position) {
            this.id = id;
            this.className = className;
            this.position = position;
        }
        
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Point getPosition() {
            return position;
        }

        public void setPosition(Point position) {
            this.position = position;
        }
    }
    
    /**
     * A class representing a connection in the view.
     */
    private static class ViewEdge {
        /**
         * Legacy business object id.
         */
        private long id;
        /**
         * Business object class.
         */
        private String className;
        /**
         * The list of controlpoints of this connection.
         */
        private List<Point> controlPoints;
        /**
         * The legacy id of one end of the connection.
         */
        private long aSide;
        /**
         * The legacy id of the other end of the connection.
         */
        private long bSide;

        public ViewEdge(long id, String className, long aSide, long bSide) {
            this.id = id;
            this.className = className;
            this.aSide = aSide;
            this.bSide = bSide;
            this.controlPoints = new ArrayList<>();
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public List<Point> getControlPoints() {
            return controlPoints;
        }

        public void setControlPoints(List<Point> controlPoints) {
            this.controlPoints = controlPoints;
        }

        public long getaSide() {
            return aSide;
        }

        public void setaSide(long aSide) {
            this.aSide = aSide;
        }

        public long getbSide() {
            return bSide;
        }

        public void setbSide(long bSide) {
            this.bSide = bSide;
        }
    }
    
    /**
     * Dummy exception that is thrown when a view that is not an Object View is detected.
     */
    private static class OtherKinfOfViewException extends Exception {

        public OtherKinfOfViewException(String msg) {
            super(msg);
        }
        
    }
    
    /**
     * Dummy exception that is thrown when a a node that is reusing the id of an old object has been detected.
     */
    private static class NodeIdReusedException extends Exception {

        public NodeIdReusedException(String msg) {
            super(msg);
        }
        
    }
}
