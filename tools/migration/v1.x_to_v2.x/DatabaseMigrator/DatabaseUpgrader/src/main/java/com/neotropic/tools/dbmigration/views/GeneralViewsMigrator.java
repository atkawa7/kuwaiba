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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

/**
 * Migrates all the non object-specific views (as opposed to Object, Rack or E2E views), such as SDH, MPLS and Topology views.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class GeneralViewsMigrator {
    /**
     * Performs the actual migration
     * @param dbPathReference The reference to the database location.
     */
    public static void migrate(File dbPathReference) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPathReference);
        System.out.println(">>> Migrating General Views...");
        
        try (Transaction tx = graphDb.beginTx()) {
            graphDb.findNodes(Label.label("generalViews")).stream().forEach((aViewNode) -> {
                System.out.println(String.format("Processing %s (%s)", aViewNode.getProperty("name"), aViewNode.getProperty("className")));
                byte[] structure = (byte[])aViewNode.getProperty("structure");
                try {                            
                    ViewUtil.ViewMap parsedView = parseView(structure);
                    byte[] migratedStructure = migrateViewMap(parsedView, graphDb);
                    aViewNode.setProperty("structure", migratedStructure);
                    aViewNode.setProperty("className", parsedView.getViewClass());

                    //<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//                             try {
//                                 FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/e2eview_" + anObjectNode.getId() + ".xml");
//                                 fos.write(migratedStructure);
//                                 fos.close();
//                             } catch(Exception e) {}
                     //</editor-fold>

                } catch (XMLStreamException ex) {
                    System.out.println(String.format("Unexpected error processing general view %s (%s): %s.", 
                            aViewNode.getProperty("name"), aViewNode.getId(), ex.getMessage()));
                }
            });
            System.out.println(">>> General Views migration finished");
            tx.success();
        }
        graphDb.shutdown();
    }
    
    /**
     * Parses an existing view into a set of Java objects.
     * @param structure The byte array with the view contents.
     * @return A view as a Java object
     * @throws XMLStreamException If there was a problem parsing the XML document.
     */
    private static ViewUtil.ViewMap parseView(byte[] structure) throws XMLStreamException {
        ViewUtil.ViewMap res = new ViewUtil.ViewMap();
        
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        QName qClass = new QName("class"); //NOI18N
        QName qNode = new QName("node"); //NOI18N
        QName qEdge = new QName("edge"); //NOI18N
        QName qControlPoint = new QName("controlpoint"); //NOI18N
        QName qpolygon = new QName("polygon"); //NOI18N

        ByteArrayInputStream bais = new ByteArrayInputStream(structure);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (reader.getName().equals(qClass)) {
                    res.setViewClass(reader.getElementText());
                    continue;
                }
                
                if (reader.getName().equals(qNode)) {
                    String objectClass = reader.getAttributeValue(null, "class"); //NOI18N
                    int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue(); //NOI18N
                    int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue(); //NOI18N
                    long objectId = Long.valueOf(reader.getElementText());
                    
                    res.getNodes().add(new ViewUtil.ViewNode(objectId, objectClass, new Point(xCoordinate, yCoordinate)));
                    
                } else {
                    if (reader.getName().equals(qEdge)) {
                        String rawObjectId = reader.getAttributeValue(null, "id");
                        long objectId = rawObjectId.isEmpty() ? -1 : Long.valueOf(rawObjectId);
                        long aSide = Long.valueOf(reader.getAttributeValue(null, "aside")); //NOI18N
                        long bSide = Long.valueOf(reader.getAttributeValue(null, "bside")); //NOI18N
                        String objectClass = reader.getAttributeValue(null,"class"); //NOI18N
                        
                        ViewUtil.ViewEdge newEdge = new ViewUtil.ViewEdge(objectId, objectClass, aSide, bSide);

                        while(true) {
                            reader.nextTag();
                            if (reader.getName().equals(qControlPoint)) {
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                    newEdge.getControlPoints().add(new Point(Float.valueOf(reader.getAttributeValue(null,"x")).intValue(), 
                                            Float.valueOf(reader.getAttributeValue(null,"y")).intValue()));
                            } else 
                                break;
                        }
                        res.getEdges().add(newEdge);
                    } else {
                        if (reader.getName().equals(qpolygon)) {
                            //TODO
                        }
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
    private static byte[] migrateViewMap(ViewUtil.ViewMap viewMap, GraphDatabaseService graphDb) throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
        XMLEventFactory xmlef = XMLEventFactory.newInstance();

        QName qnameView = new QName("view"); //NOI18N
        xmlew.add(xmlef.createStartElement(qnameView, null, null));
        xmlew.add(xmlef.createAttribute(new QName("version"), "1.2")); //NOI18N

        QName qnameClass = new QName("class"); //NOI18N
        xmlew.add(xmlef.createStartElement(qnameClass, null, null));
        xmlew.add(xmlef.createCharacters(viewMap.getViewClass())); //NOI18N
        xmlew.add(xmlef.createEndElement(qnameClass, null));

        QName qnameNodes = new QName("nodes"); //NOI18N
        xmlew.add(xmlef.createStartElement(qnameNodes, null, null));

        for (ViewUtil.ViewNode aNode : viewMap.getNodes()) {
            try {
                Node anObjectNode = graphDb.getNodeById(aNode.getId());
                if (!anObjectNode.hasProperty("_uuid"))
                    throw new ViewUtil.NodeIdReusedException(String.format("The node with id %s is reusing an id formerly used by another inventory object. This node will be ignored.", 
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
            } catch (ViewUtil.NodeIdReusedException nre) {
                System.out.println(nre.getMessage());
            }
        }
        xmlew.add(xmlef.createEndElement(qnameNodes, null));

        QName qnameEdges = new QName("edges"); //NOI18N
        xmlew.add(xmlef.createStartElement(qnameEdges, null, null));

        for (ViewUtil.ViewEdge anEdge : viewMap.getEdges()) {
            try {
                Node anObjectNode = graphDb.getNodeById(anEdge.getId());
                Node aSideObjectNode = graphDb.getNodeById(anEdge.getaSide());
                Node bSideObjectNode = graphDb.getNodeById(anEdge.getbSide());
                
                if (!anObjectNode.hasProperty("_uuid"))
                    throw new ViewUtil.NodeIdReusedException(String.format("The node with id %s is reusing an id formerly used by another inventory object. This edge will be ignored.", 
                        anObjectNode.getId()));
                if (!aSideObjectNode.hasProperty("_uuid"))
                    throw new ViewUtil.NodeIdReusedException(String.format("The node with id %s is reusing an id formerly used by another inventory object. This edge will be ignored.", 
                        aSideObjectNode.getId()));
                if (!bSideObjectNode.hasProperty("_uuid"))
                    throw new ViewUtil.NodeIdReusedException(String.format("The node with id %s is reusing an id formerly used by another inventory object. This edge will be ignored.", 
                        bSideObjectNode.getId()));
                
                QName qnameEdge = new QName("edge"); //NOI18N
                xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                xmlew.add(xmlef.createAttribute(new QName("id"), (String)anObjectNode.getProperty("_uuid"))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("class"), anEdge.getClassName())); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("asideid"), (String)aSideObjectNode.getProperty("_uuid"))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("asideclass"), 
                        (String)aSideObjectNode.getSingleRelationship(ViewUtil.RELTYPE_INSTANCEOF, Direction.OUTGOING).getEndNode().getProperty("name"))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("bsideid"), (String)bSideObjectNode.getProperty("_uuid"))); //NOI18N
                xmlew.add(xmlef.createAttribute(new QName("bsideclass"), 
                        (String)bSideObjectNode.getSingleRelationship(ViewUtil.RELTYPE_INSTANCEOF, Direction.OUTGOING).getEndNode().getProperty("name"))); //NOI18N
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
            } catch (ViewUtil.NodeIdReusedException nre) {
                System.out.println(nre.getMessage());
            }
        }
        xmlew.add(xmlef.createEndElement(qnameEdges, null));

        xmlew.add(xmlef.createEndElement(qnameView, null));
        xmlew.close();
        return baos.toByteArray();
    }
}
