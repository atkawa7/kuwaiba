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
import java.util.HashMap;
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
 * This class manages the migration of all End To End views.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class EndToEndAndTopologyViewMigrator {
    private static HashMap<String, Long> uuidToIdMap;
    /**
     * Performs the actual migration
     * @param dbPathReference The reference to the database location.
     */
    public static void migrate(File dbPathReference) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPathReference);
        System.out.println(">>> Migrating End to End and Topology Views...");
        uuidToIdMap = new HashMap<>();
        
        try (Transaction tx = graphDb.beginTx()) {
            graphDb.findNodes(Label.label("inventoryObjects")).stream().forEach((anObjectNode) -> {
                if (anObjectNode.hasProperty("_uuid"))
                    uuidToIdMap.put((String)anObjectNode.getProperty("_uuid"), anObjectNode.getId());
            });
            
            graphDb.findNodes(Label.label("inventoryObjects")).stream().forEach((anObjectNode) -> {
                if (anObjectNode.hasRelationship(ViewUtil.RELTYPE_HASVIEW)) {
                    System.out.println(String.format("Processing views for %s (%s)", anObjectNode.getProperty("name"), anObjectNode.getId()));
                    anObjectNode.getRelationships(ViewUtil.RELTYPE_HASVIEW).forEach((aHasViewRelationship) -> { 
                        Node aViewNode = aHasViewRelationship.getEndNode();

                        byte[] structure = (byte[])aViewNode.getProperty("structure");
                        
                        try {
                            FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/e2eview-"+anObjectNode.getProperty("name")+"_before.xml");
                            fos.write(structure);
                            fos.close();
                        } catch(Exception e) {}
                        
                        try {                            
                            ViewUtil.ViewMap parsedView = parseView(structure);
                            byte[] migratedStructure = migrateViewMap(parsedView, graphDb);
                            aViewNode.setProperty("structure", migratedStructure);
                            aViewNode.setProperty("className", parsedView.getViewClass());
                            aViewNode.setProperty("name", parsedView.getViewClass());
                            
                            //<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
                             try {
                                 FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/e2eview_" + anObjectNode.getId() + ".xml");
                                 fos.write(migratedStructure);
                                 fos.close();
                             } catch(Exception e) {}
                             //</editor-fold>
                            
                        } catch (XMLStreamException ex) {
                            System.out.println(String.format("Unexpected error processing object view for %s (%s): %s.", 
                                    anObjectNode.getProperty("name"), anObjectNode.getId(), ex.getMessage()));
                        } catch (ViewUtil.OtherKinfOfViewException okovex) { //Ignore all the views that are not object views
                            //System.out.println(okovex.getMessage());
                        }
                    });
                }
            });
            System.out.println(">>> End to End and Topology Views migration finished");
            tx.success();
        }
        graphDb.shutdown();
    }
    
    /**
     * Parses an existing view into a set of Java objects.
     * @param structure The byte array with the view contents.
     * @return A view as a Java object
     * @throws Exception If there was a problem parsing the XML document.
     */
    private static ViewUtil.ViewMap parseView(byte[] structure) throws XMLStreamException, ViewUtil.OtherKinfOfViewException {
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
                    String viewClass = reader.getElementText();
                    switch (viewClass) {
                        case "EndToEndView":
                            res.setViewClass("EndToEndView"); //NOI18N
                            System.out.println("End to End view detected...");
                            break;
                        case "ServiceTopologyView":
                            res.setViewClass("TopologyView"); //NOI18N
                            System.out.println("Topology view detected...");
                            break;
                        default:
                            throw new ViewUtil.OtherKinfOfViewException(String.format("%s detected and ignored (it will be processed later on)", viewClass));
                    }
                }
                
                if (reader.getName().equals(qNode)) {
                    String objectClass = reader.getAttributeValue(null, "class"); //NOI18N
                    int xCoordinate = Double.valueOf(reader.getAttributeValue(null,"x")).intValue(); //NOI18N
                    int yCoordinate = Double.valueOf(reader.getAttributeValue(null,"y")).intValue(); //NOI18N
                    String textElement = reader.getElementText();
                    ViewUtil.ViewNode viewNode;
                    if(ViewUtil.isNumeric(textElement)) {  
                        long objectId = Long.valueOf(reader.getElementText());
                        viewNode = new ViewUtil.ViewNode(objectId, objectClass, new Point(xCoordinate, yCoordinate));
                    } else//to create a node with id for new migrations
                        viewNode = new ViewUtil.ViewNode(textElement, objectClass, new Point(xCoordinate, yCoordinate));
                    
                    res.getNodes().add(viewNode);
                } else {
                    if (reader.getName().equals(qEdge)) {
                        String objectClass = reader.getAttributeValue(null,"class"); //NOI18N
                        String id = reader.getAttributeValue(null, "id");
                        ViewUtil.ViewEdge newEdge;    
                        
                       if(ViewUtil.isNumeric(id)){  
                            long objectId = Long.valueOf(id); //NOI18N
                            long aSide = Long.valueOf(reader.getAttributeValue(null, "aside")); //NOI18N
                            long bSide = Long.valueOf(reader.getAttributeValue(null, "bside")); //NOI18N
                            newEdge = new ViewUtil.ViewEdge(objectId, objectClass, aSide, bSide);
                            
                        } else{  
                            String uuid = id; //NOI18N
                            String aSide = reader.getAttributeValue(null, "aside"); //NOI18N
                            String bSide = reader.getAttributeValue(null, "bside"); //NOI18N
                            //to create a node with id for new migrations
                            newEdge = new ViewUtil.ViewEdge(uuid, objectClass, aSide, bSide);
                        } 
                         
                        while(true) {
                            reader.nextTag();
                            if (reader.getName().equals(qControlPoint)) {
                                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT)
                                    newEdge.getControlPoints().add(new Point(Integer.valueOf(reader.getAttributeValue(null,"x")), 
                                            Integer.valueOf(reader.getAttributeValue(null,"y"))));
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
                    QName qnameNode = new QName("node"); //NOI18N
                    xmlew.add(xmlef.createStartElement(qnameNode, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("x"), String.valueOf(aNode.getPosition().x))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("y"), String.valueOf(aNode.getPosition().y))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("class"), aNode.getClassName())); //NOI18N
                    
                    if(aNode.getId() > 0 && aNode.getUuid() == null){
                        Node anObjectNode = graphDb.getNodeById(aNode.getId());
                        if (!anObjectNode.hasProperty("_uuid"))
                            throw new ViewUtil.NodeIdReusedException(String.format("The node with id %s is reusing an id formerly used by another inventory object. This node will be ignored.", 
                                anObjectNode.getId()));

                        xmlew.add(xmlef.createCharacters((String)anObjectNode.getProperty("_uuid")));
                    } else
                        xmlew.add(xmlef.createCharacters(aNode.getUuid()));
                    
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
                QName qnameEdge = new QName("edge"); //NOI18N
                
                if(anEdge.getId() > 0 && anEdge.getUuid() == null){
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

                    xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("id"), (String)anObjectNode.getProperty("_uuid"))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("class"), anEdge.getClassName())); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("asideid"), (String)aSideObjectNode.getProperty("_uuid"))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("asideclass"), 
                            (String)aSideObjectNode.getSingleRelationship(ViewUtil.RELTYPE_INSTANCEOF, Direction.OUTGOING).getEndNode().getProperty("name"))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("bsideid"), (String)bSideObjectNode.getProperty("_uuid"))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("bsideclass"), 
                            (String)bSideObjectNode.getSingleRelationship(ViewUtil.RELTYPE_INSTANCEOF, Direction.OUTGOING).getEndNode().getProperty("name"))); //NOI18N
                }
                else if(uuidToIdMap.containsKey(anEdge.getaSideUuid()) && uuidToIdMap.containsKey(anEdge.getbSideUuid())){
                   
                    Node aSideObjectNode = graphDb.getNodeById(uuidToIdMap.get(anEdge.getaSideUuid()));
                    Node bSideObjectNode = graphDb.getNodeById(uuidToIdMap.get(anEdge.getbSideUuid()));    
                    
                    xmlew.add(xmlef.createStartElement(qnameEdge, null, null));
                    xmlew.add(xmlef.createAttribute(new QName("id"), anEdge.getUuid())); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("class"), anEdge.getClassName())); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("asideid"), anEdge.getaSideUuid())); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("asideclass"), 
                            (String)aSideObjectNode.getSingleRelationship(ViewUtil.RELTYPE_INSTANCEOF, Direction.OUTGOING).getEndNode().getProperty("name"))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("bsideid"), anEdge.getaSideUuid())); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("bsideclass"), 
                            (String)bSideObjectNode.getSingleRelationship(ViewUtil.RELTYPE_INSTANCEOF, Direction.OUTGOING).getEndNode().getProperty("name"))); //NOI18N
                }
                    
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
