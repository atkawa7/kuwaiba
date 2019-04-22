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

import java.awt.Dimension;
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
 * A migrator for the device layouts. Device layouts are real-life representations 
 * of devices, designed in the desktop client and stored as XML documents that may 
 * make reference to list type items.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LayoutMigrator {
    /**
     * Performs the actual migration
     * @param dbPathReference The reference to the database location.
     */
    public static void migrate(File dbPathReference) {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(dbPathReference);
        System.out.println(">>> Migrating Device Layouts...");
        
        try (Transaction tx = graphDb.beginTx()) {
            graphDb.findNodes(Label.label("listTypeItems")).stream().forEach((aListTypeItemNode) -> {
                System.out.println(String.format("Processing List Type Item %s [%s] (%s)", aListTypeItemNode.getProperty("name"), 
                        aListTypeItemNode.getSingleRelationship(ViewUtil.RELTYPE_INSTANCEOF, Direction.OUTGOING).getEndNode().getProperty("name"), 
                        aListTypeItemNode.getId()));
                
                aListTypeItemNode.getRelationships(Direction.OUTGOING, ViewUtil.RELTYPE_HASVIEW).forEach((aLayoutRelationship) -> {
                    Node layoutNode = aLayoutRelationship.getEndNode();
                    System.out.println("Processing layout " + layoutNode.getId());
                    try {
                        LayoutUtil.LayoutMap parsedLayout = parseLayout((byte[])layoutNode.getProperty("structure"));
                        byte[] migratedStructure = migrateLayoutMap(parsedLayout, graphDb);
                        //<editor-fold defaultstate="collapsed" desc="uncomment this for debugging purposes, write the XML view into a file">
//                             try {
//                                 FileOutputStream fos = new FileOutputStream(System.getProperty("user.home") + "/layout_migrated" + layoutNode.getId() + ".xml");
//                                 fos.write(migratedStructure);
//                                 fos.close();
//                             } catch(Exception e) {}
             //</editor-fold>
                        layoutNode.setProperty("structure", migratedStructure);
                        layoutNode.addLabel(Label.label("layouts"));
                    } catch (XMLStreamException ex) {
                        System.out.println(String.format("Unexpected error processing layout with id %s: %s", 
                            layoutNode.getProperty("name"), layoutNode.getId(), ex.getMessage()));
                    }
                    
                    
                });
            });
            System.out.println(">>> Layout migration finished");
            tx.success();
        }
        graphDb.shutdown();
    }
    
    /**
     * Parses an existing layout into a set of Java objects.
     * @param structure The byte array with the view contents.
     * @return A layout as a Java object
     * @throws XMLStreamException If there was a problem parsing the XML document.
     */
    private static LayoutUtil.LayoutMap parseLayout(byte[] structure) throws XMLStreamException {
        LayoutUtil.LayoutMap res = new LayoutUtil.LayoutMap();
        
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        QName qView = new QName("view"); //NOI18N
        QName qLayout = new QName("layout"); //NOI18N
        QName qShape = new QName("shape"); //NOI18N

        ByteArrayInputStream bais = new ByteArrayInputStream(structure);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

        while (reader.hasNext()) {
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (reader.getName().equals(qView)) 
                    continue;
                if (reader.getName().equals(qLayout)) {
                    int xCoordinate = Integer.valueOf(reader.getAttributeValue(null, "x")); //NOI18N
                    int yCoordinate = Integer.valueOf(reader.getAttributeValue(null, "y")); //NOI18N
                    int width = Integer.valueOf(reader.getAttributeValue(null, "width")); //NOI18N
                    int height = Integer.valueOf(reader.getAttributeValue(null, "height")); //NOI18N
                    
                    res.setCoordinates(new Point(xCoordinate, yCoordinate));
                    res.setDimensions(new Dimension(width, height));
                    continue;
                }
                
                if (reader.getName().equals(qShape)) {
                    String shapeType = reader.getAttributeValue(null, "type"); //NOI18N
                    int xCoordinate = Integer.valueOf(reader.getAttributeValue(null, "x")); //NOI18N
                    int yCoordinate = Integer.valueOf(reader.getAttributeValue(null, "y")); //NOI18N
                    int width = Integer.valueOf(reader.getAttributeValue(null, "width")); //NOI18N
                    int height = Integer.valueOf(reader.getAttributeValue(null, "height")); //NOI18N
                    boolean opaque = Boolean.valueOf(reader.getAttributeValue(null, "opaque")); //NOI18N
                    boolean isEquipment = Boolean.valueOf(reader.getAttributeValue(null, "isEquipment")); //NOI18N
                    String name = reader.getAttributeValue(null, "name"); //NOI18N
                    
                    switch (shapeType) { //The way the XML parser works keeps us from trying to read the "color" and "borderColor" attributes in the general section
                        case LayoutUtil.Rectangle.TYPE: {
                            int color = Integer.valueOf(reader.getAttributeValue(null, "color")); //NOI18N
                            int borderColor = Integer.valueOf(reader.getAttributeValue(null, "borderColor")); //NOI18N
                            boolean isSlot = Boolean.valueOf(reader.getAttributeValue(null, "isSlot")); //NOI18N
                            res.getShapes().add(new LayoutUtil.Rectangle(new Point(xCoordinate, yCoordinate), 
                                    new Dimension(width, height), opaque, isEquipment, name, color, borderColor, isSlot));
                            break;
                        }
                        case LayoutUtil.Ellipse.TYPE: {
                            int color = Integer.valueOf(reader.getAttributeValue(null, "color")); //NOI18N
                            int borderColor = Integer.valueOf(reader.getAttributeValue(null, "borderColor")); //NOI18N
                            int ellipseColor = Integer.valueOf(reader.getAttributeValue(null, "ellipseColor")); //NOI18N
                            int ovalColor = Integer.valueOf(reader.getAttributeValue(null, "ovalColor")); //NOI18N
                            res.getShapes().add(new LayoutUtil.Ellipse(new Point(xCoordinate, yCoordinate), 
                                    new Dimension(width, height), opaque, isEquipment, name, color, borderColor, ellipseColor, ovalColor));
                            break;
                        }
                        case LayoutUtil.Label.TYPE: {
                            int color = Integer.valueOf(reader.getAttributeValue(null, "color")); //NOI18N
                            int borderColor = Integer.valueOf(reader.getAttributeValue(null, "borderColor")); //NOI18N
                            String label = reader.getAttributeValue(null, "label"); //NOI18N
                            int textColor = Integer.valueOf(reader.getAttributeValue(null, "textColor")); //NOI18N
                            int fontSize = Integer.valueOf(reader.getAttributeValue(null, "fontSize")); //NOI18N
                            res.getShapes().add(new LayoutUtil.Label(new Point(xCoordinate, yCoordinate), 
                                    new Dimension(width, height), opaque, isEquipment, name, color, borderColor, label, textColor, fontSize));
                            break;
                        }
                        case LayoutUtil.Container.TYPE: {
                            res.getShapes().add(new LayoutUtil.Container(new Point(xCoordinate, yCoordinate), 
                                    new Dimension(width, height), opaque, isEquipment, name));
                            break;
                        }
                        case LayoutUtil.CustomShape.TYPE:
                            long id = Long.valueOf(reader.getAttributeValue(null, "id")); //NOI18N
                            String className = reader.getAttributeValue(null, "className"); //NOI18N
                            res.getShapes().add(new LayoutUtil.CustomShape(new Point(xCoordinate, yCoordinate), 
                                    new Dimension(width, height), opaque, isEquipment, name, id, className));
                            break;    
                        default:
                            System.out.println(String.format("Unknown shape type %s", shapeType));
                    }
                } else 
                    System.out.println(String.format("Unexpected tag %s", reader.getName()));
                
            }
        }
        reader.close();
        return res;
    }
    
    /**
     * Converts an in-memory layout structure into a saveable XML document that 
     * uses string ids.
     * @param layoutMap  The input in-memory structure.
     * @return The resulting XML document.
     */
    private static byte[] migrateLayoutMap(LayoutUtil.LayoutMap layoutMap, GraphDatabaseService graphDb) throws XMLStreamException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
        XMLEventFactory xmlef = XMLEventFactory.newInstance();

        QName qnameView = new QName("view"); //NOI18N
        xmlew.add(xmlef.createStartElement(qnameView, null, null));
        xmlew.add(xmlef.createAttribute(new QName("version"), "1.2")); //NOI18N

        QName qnameLayout = new QName("layout"); //NOI18N
        xmlew.add(xmlef.createStartElement(qnameLayout, null, null));
        xmlew.add(xmlef.createAttribute(new QName("x"), String.valueOf(layoutMap.getCoordinates().x))); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("y"), String.valueOf(layoutMap.getCoordinates().y))); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("width"), String.valueOf(layoutMap.getDimensions().width))); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("height"), String.valueOf(layoutMap.getDimensions().height))); //NOI18N

        QName qnameShape = new QName("shape"); //NOI18N
        
        for (LayoutUtil.Shape aShape : layoutMap.getShapes()) {
            xmlew.add(xmlef.createStartElement(qnameShape, null, null));
            xmlew.add(xmlef.createAttribute(new QName("type"), aShape.getType())); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("x"), String.valueOf(aShape.getCoordinates().x))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("y"), String.valueOf(aShape.getCoordinates().y))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("width"), String.valueOf(aShape.getDimensions().width))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("height"), String.valueOf(aShape.getDimensions().height))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("opaque"), String.valueOf(aShape.opaque))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("isEquipment"), String.valueOf(aShape.isEquipment))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("name"), aShape.name)); //NOI18N
            switch (aShape.getType()) {
                case LayoutUtil.Rectangle.TYPE:
                    xmlew.add(xmlef.createAttribute(new QName("color"), String.valueOf(aShape.color))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("borderColor"), String.valueOf(aShape.borderColor))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("isSlot"), String.valueOf(((LayoutUtil.Rectangle)aShape).isSlot))); //NOI18N
                    break;
                case LayoutUtil.Ellipse.TYPE:
                    xmlew.add(xmlef.createAttribute(new QName("color"), String.valueOf(aShape.color))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("borderColor"), String.valueOf(aShape.borderColor))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("ellipseColor"), String.valueOf(((LayoutUtil.Ellipse)aShape).ellipseColor))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("ovalColor"), String.valueOf(((LayoutUtil.Ellipse)aShape).ovalColor))); //NOI18N
                    break;
                case LayoutUtil.Label.TYPE:
                    xmlew.add(xmlef.createAttribute(new QName("color"), String.valueOf(aShape.color))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("borderColor"), String.valueOf(aShape.borderColor))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("label"), ((LayoutUtil.Label)aShape).label)); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("textColor"), String.valueOf(((LayoutUtil.Label)aShape).textColor))); //NOI18N
                    xmlew.add(xmlef.createAttribute(new QName("fontSize"), String.valueOf(((LayoutUtil.Label)aShape).fontSize))); //NOI18N
                    break;
                case LayoutUtil.Container.TYPE:
                    // To do something in the future, or simply remove it.
                    break;
                case LayoutUtil.CustomShape.TYPE:
                    try {
                        Node listTypeNode = graphDb.getNodeById(((LayoutUtil.CustomShape)aShape).id);
                        if (!listTypeNode.hasProperty("_uuid"))
                            System.out.println(String.format("A list type item was expected (%s), but a node without UUID was found. "
                                    + "This database was either not correctly migrated or it is inconsistent. Fix the problem and run "
                                    + "Stage 2 again.", listTypeNode.getId()));
                        else {
                            xmlew.add(xmlef.createAttribute(new QName("id"), (String)listTypeNode.getProperty("_uuid"))); //NOI18N
                            xmlew.add(xmlef.createAttribute(new QName("className"), String.valueOf(((LayoutUtil.CustomShape)aShape).className))); //NOI18N
                        }
                    } catch (NotFoundException ex) {
                        System.out.println(String.format("The referenced list type item with id %s could not be found", ((LayoutUtil.CustomShape)aShape).id));
                    }
                    break;
            }
            xmlew.add(xmlef.createEndElement(qnameShape, null));
        }
        
        xmlew.add(xmlef.createEndElement(qnameLayout, null));
        xmlew.add(xmlef.createEndElement(qnameView, null));
        xmlew.close();
        return baos.toByteArray();
    }
}
