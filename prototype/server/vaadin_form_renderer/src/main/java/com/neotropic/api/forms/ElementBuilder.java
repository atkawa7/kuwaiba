/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.api.forms;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * This class reads an structure xml and create the form elements containment 
 * hierarchy.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ElementBuilder {
    private final QName tagForm = new QName(Constants.Tag.FORM);
    private final QName tagGridLayout = new QName(Constants.Tag.GRID_LAYOUT);
    private final QName tagI18N = new QName(Constants.Tag.I18N);
    private final QName tagLabel = new QName(Constants.Tag.LABEL);
    private final QName tagTextField = new QName(Constants.Tag.TEXT_FIELD);
    private final QName tagVerticalLayout = new QName(Constants.Tag.VERTICAL_LAYOUT);
    private final QName tagTextArea = new QName(Constants.Tag.TEXT_AREA);
    private final QName tagDateField = new QName(Constants.Tag.DATE_FIELD);
    private final QName tagComboBox = new QName(Constants.Tag.COMBO_BOX);
    private final QName tagGrid = new QName(Constants.Tag.GRID);
    private final QName tagButton = new QName(Constants.Tag.BUTTON);
    private final QName tagSubform = new QName(Constants.Tag.SUBFORM);
    private final QName tagHorizontalLayout = new QName(Constants.Tag.HORIZONTAL_LAYOUT);
    private final QName tagImage = new QName(Constants.Tag.IMAGE);
    
    private final List<QName> containers;
    
    private ElementForm root;
    private ElementI18N i18n;
    private Evaluator evaluator;
        
    public ElementBuilder() {
        containers = new ArrayList();
        containers.add(tagGridLayout);
        containers.add(tagVerticalLayout);
        containers.add(tagSubform);
        containers.add(tagHorizontalLayout);
    }
    
    public ElementForm getRoot() {
        return root;
    }
    
    public ElementI18N getI18N() {
        return i18n;
    }
    
    public Evaluator getEvaluator() {
        return evaluator == null ? evaluator = new Evaluator(i18n) : evaluator;
    }
    
    private void createFormContaimentHierarchy(AbstractElement parent, XMLStreamReader reader) throws XMLStreamException {
        
        while (reader.hasNext()) {
            int event = reader.next();
            
            if (event == XMLStreamConstants.END_ELEMENT) {
                if (containers.contains(reader.getName()))
                    return;
            }
            
            if (event == XMLStreamConstants.START_ELEMENT) {
                
                if (reader.getName().equals(tagGridLayout)) {
                    ElementGridLayout gridLayout = new ElementGridLayout();
                    gridLayout.initFromXMl(reader);
                    
                    parent.getChildren().add(gridLayout);
                    createFormContaimentHierarchy(gridLayout, reader);
                    
                } else if (reader.getName().equals(tagVerticalLayout)) {
                    ElementVerticalLayout verticalLayout = new ElementVerticalLayout();
                    verticalLayout.initFromXMl(reader);
                    
                    parent.getChildren().add(verticalLayout);
                    createFormContaimentHierarchy(verticalLayout, reader);
                    
                } else if (reader.getName().equals(tagSubform)) { 
                    ElementSubform subform = new ElementSubform();
                    subform.initFromXMl(reader);
                    
                    parent.getChildren().add(subform);
                    createFormContaimentHierarchy(subform, reader);
                    
                } else if (reader.getName().equals(tagHorizontalLayout)) {
                    ElementHorizontalLayout horizontalLayout = new ElementHorizontalLayout();
                    horizontalLayout.initFromXMl(reader);
                    
                    parent.getChildren().add(horizontalLayout);
                    createFormContaimentHierarchy(horizontalLayout, reader);
                                        
                } else if (reader.getName().equals(tagLabel)) {
                    ElementLabel label = new ElementLabel();
                    label.initFromXMl(reader);
                    
                    parent.getChildren().add(label);
                    
                } else if (reader.getName().equals(tagTextField)) {
                    ElementTextField textField = new ElementTextField();
                    textField.initFromXMl(reader);
                    
                    parent.getChildren().add(textField);
                    
                } else if (reader.getName().equals(tagTextArea)) {
                    ElementTextArea textArea = new ElementTextArea();
                    textArea.initFromXMl(reader);
                    
                    parent.getChildren().add(textArea);
                    
                } else if (reader.getName().equals(tagDateField)) {
                    ElementDateField dateField = new ElementDateField();
                    dateField.initFromXMl(reader);
                    
                    parent.getChildren().add(dateField);
                                        
                } else if (reader.getName().equals(tagComboBox)) {
                    ElementComboBox comboBox = new ElementComboBox();
                    comboBox.initFromXMl(reader);
                    
                    parent.getChildren().add(comboBox);
                    
                } else if (reader.getName().equals(tagGrid)) {
                    ElementGrid grid = new ElementGrid();
                    grid.initFromXMl(reader);
                    
                    parent.getChildren().add(grid);
                                        
                } else if (reader.getName().equals(tagButton)) { 
                    ElementButton button = new ElementButton();
                    button.initFromXMl(reader);
                    
                    parent.getChildren().add(button);
                    
                } else if (reader.getName().equals(tagImage)) {
                    ElementImage image = new ElementImage();
                    image.initFromXMl(reader);
                    
                    parent.getChildren().add(image);
                    
                } else if (reader.getName().equals(tagI18N))
                    return;
            }
        }
    }    
    
    public void build(byte[] structure) {

        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
                                    
            while (reader.hasNext()) {
                int event = reader.next();
                
                if (event == XMLStreamConstants.START_ELEMENT) {
                    
                    if (reader.getName().equals(tagForm)) {
                        root = new ElementForm();
                        root.initFromXMl(reader);
                        
                        createFormContaimentHierarchy(root, reader);
                    }
                    
                    if (reader.getName().equals(tagI18N)) {
                        i18n = new ElementI18N();
                        i18n.initFromXMl(reader);
                    }
                }
            }
            reader.close();
            
        } catch (XMLStreamException ex) {
            Logger.getLogger(ElementBuilder.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
/*
XMLInputFactory inputFactory = XMLInputFactory.newInstance();

QName qNode = new QName("node"); //NOI18N
QName qEdge = new QName("edge"); //NOI18N
QName qControlPoint = new QName("controlpoint"); //NOI18N
QName qPolygon = new QName("polygon"); //NOI18N
QName qIcon = new QName("icon"); //NOI18N

try {
    ByteArrayInputStream bais = new ByteArrayInputStream(structure);
    XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

    while (reader.hasNext()){
        int event = reader.next();
        if (event == XMLStreamConstants.START_ELEMENT) {
            if (reader.getName().equals(qNode)){
                String objectClass = reader.getAttributeValue(null, "class");

                int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                Long objectId = Long.valueOf(reader.getElementText());

                LocalObjectLight lol = CommunicationsStub.getInstance().
                        getObjectInfoLight(objectClass, objectId);
                if (lol != null)
                    this.addNode(lol).setPreferredLocation(new Point(x, y));
                else
                    NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, String.format("ViewAbleObject of class %s and id %s could not be found and was removed from the topology view", objectClass, objectId));
            } else {
                if (reader.getName().equals(qIcon)){ // FREE CLOUDS
                        if(Integer.valueOf(reader.getAttributeValue(null,"type"))==1){
                            int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                            int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();

                            long oid = Long.valueOf(reader.getAttributeValue(null,"id"));
                            LocalObjectLight lol = new LocalObjectLight(oid, reader.getElementText(), null);
                            this.addNode(lol).setPreferredLocation(new Point(x, y));
                        }
                    }
                else {
                    if (reader.getName().equals(qEdge)) {
                        Long aSide = Long.valueOf(reader.getAttributeValue(null,"aside"));
                        Long bSide = Long.valueOf(reader.getAttributeValue(null,"bside"));

                        LocalObjectLight aSideObject = new LocalObjectLight(aSide, null, null);
                        Widget aSideWidget = this.findWidget(aSideObject);

                        LocalObjectLight bSideObject = new LocalObjectLight(bSide, null, null);
                        Widget bSideWidget = this.findWidget(bSideObject);

                        if (aSideWidget == null || bSideWidget == null)
                            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, "One or both of the endpoints of a connection could not be found. The connection was removed from the topology view");
                        else {
                            String edgeName = "topologyEdge" + aSideObject.getOid() + bSideObject.getOid() + randomGenerator.nextInt(1000);
                            ConnectionWidget newEdge = (ConnectionWidget)this.addEdge(edgeName);
                            this.setEdgeSource(edgeName, aSideObject);
                            this.setEdgeTarget(edgeName, bSideObject);
                            List<Point> localControlPoints = new ArrayList<>();
                            while (true) {
                                reader.nextTag();
                                if (reader.getName().equals(qControlPoint)) {
                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                        String cpx = reader.getAttributeValue(null, "x");
                                        String cpy = reader.getAttributeValue(null, "y");
                                        Point point = new Point();
                                        point.setLocation(Double.valueOf(cpx), Double.valueOf(cpy));
                                        localControlPoints.add(point);
                                    }
                                } else {
                                    newEdge.setControlPoints(localControlPoints, false);
                                    break;
                                }
                            }
                        }
                    }// edges endign 
                    else{ // FREE FRAMES
                        if (reader.getName().equals(qPolygon)) { 
                            long oid = randomGenerator.nextInt(1000);
                            LocalObjectLight lol = new LocalObjectLight(oid, oid + FREE_FRAME + reader.getAttributeValue(null, "title"), null);
                            Widget myPolygon = addNode(lol);
                            Point p = new Point();
                            p.setLocation(Double.valueOf(reader.getAttributeValue(null, "x")), Double.valueOf(reader.getAttributeValue(null, "y")));
                            myPolygon.setPreferredLocation(p);
                            Dimension d = new Dimension();
                            d.setSize(Double.valueOf(reader.getAttributeValue(null, "w")), Double.valueOf(reader.getAttributeValue(null, "h")));
                            Rectangle r = new Rectangle(d);
                            myPolygon.setPreferredBounds(r);
                        }
                    }//end qPolygon
                } //end qIcons
            } // end qNodes
        } // end if
    } // end while
    reader.close();

    this.validate();
    this.repaint();
} catch (NumberFormatException | XMLStreamException ex) {
    NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.ERROR_MESSAGE, "The view seems corrupted and could not be loaded");
    clear();
    if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
        Exceptions.printStackTrace(ex);
}
/*
XMLInputFactory inputFactory = XMLInputFactory.newInstance();

QName qNode = new QName("node"); //NOI18N
QName qEdge = new QName("edge"); //NOI18N
QName qControlPoint = new QName("controlpoint"); //NOI18N
QName qPolygon = new QName("polygon"); //NOI18N
QName qIcon = new QName("icon"); //NOI18N

try {
    ByteArrayInputStream bais = new ByteArrayInputStream(structure);
    XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

    while (reader.hasNext()){
        int event = reader.next();
        if (event == XMLStreamConstants.START_ELEMENT) {
            if (reader.getName().equals(qNode)){
                String objectClass = reader.getAttributeValue(null, "class");

                int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();
                Long objectId = Long.valueOf(reader.getElementText());

                LocalObjectLight lol = CommunicationsStub.getInstance().
                        getObjectInfoLight(objectClass, objectId);
                if (lol != null)
                    this.addNode(lol).setPreferredLocation(new Point(x, y));
                else
                    NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, String.format("ViewAbleObject of class %s and id %s could not be found and was removed from the topology view", objectClass, objectId));
            } else {
                if (reader.getName().equals(qIcon)){ // FREE CLOUDS
                        if(Integer.valueOf(reader.getAttributeValue(null,"type"))==1){
                            int x = Double.valueOf(reader.getAttributeValue(null,"x")).intValue();
                            int y = Double.valueOf(reader.getAttributeValue(null,"y")).intValue();

                            long oid = Long.valueOf(reader.getAttributeValue(null,"id"));
                            LocalObjectLight lol = new LocalObjectLight(oid, reader.getElementText(), null);
                            this.addNode(lol).setPreferredLocation(new Point(x, y));
                        }
                    }
                else {
                    if (reader.getName().equals(qEdge)) {
                        Long aSide = Long.valueOf(reader.getAttributeValue(null,"aside"));
                        Long bSide = Long.valueOf(reader.getAttributeValue(null,"bside"));

                        LocalObjectLight aSideObject = new LocalObjectLight(aSide, null, null);
                        Widget aSideWidget = this.findWidget(aSideObject);

                        LocalObjectLight bSideObject = new LocalObjectLight(bSide, null, null);
                        Widget bSideWidget = this.findWidget(bSideObject);

                        if (aSideWidget == null || bSideWidget == null)
                            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.INFO_MESSAGE, "One or both of the endpoints of a connection could not be found. The connection was removed from the topology view");
                        else {
                            String edgeName = "topologyEdge" + aSideObject.getOid() + bSideObject.getOid() + randomGenerator.nextInt(1000);
                            ConnectionWidget newEdge = (ConnectionWidget)this.addEdge(edgeName);
                            this.setEdgeSource(edgeName, aSideObject);
                            this.setEdgeTarget(edgeName, bSideObject);
                            List<Point> localControlPoints = new ArrayList<>();
                            while (true) {
                                reader.nextTag();
                                if (reader.getName().equals(qControlPoint)) {
                                    if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                                        String cpx = reader.getAttributeValue(null, "x");
                                        String cpy = reader.getAttributeValue(null, "y");
                                        Point point = new Point();
                                        point.setLocation(Double.valueOf(cpx), Double.valueOf(cpy));
                                        localControlPoints.add(point);
                                    }
                                } else {
                                    newEdge.setControlPoints(localControlPoints, false);
                                    break;
                                }
                            }
                        }
                    }// edges endign 
                    else{ // FREE FRAMES
                        if (reader.getName().equals(qPolygon)) { 
                            long oid = randomGenerator.nextInt(1000);
                            LocalObjectLight lol = new LocalObjectLight(oid, oid + FREE_FRAME + reader.getAttributeValue(null, "title"), null);
                            Widget myPolygon = addNode(lol);
                            Point p = new Point();
                            p.setLocation(Double.valueOf(reader.getAttributeValue(null, "x")), Double.valueOf(reader.getAttributeValue(null, "y")));
                            myPolygon.setPreferredLocation(p);
                            Dimension d = new Dimension();
                            d.setSize(Double.valueOf(reader.getAttributeValue(null, "w")), Double.valueOf(reader.getAttributeValue(null, "h")));
                            Rectangle r = new Rectangle(d);
                            myPolygon.setPreferredBounds(r);
                        }
                    }//end qPolygon
                } //end qIcons
            } // end qNodes
        } // end if
    } // end while
    reader.close();

    this.validate();
    this.repaint();
} catch (NumberFormatException | XMLStreamException ex) {
    NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.ERROR_MESSAGE, "The view seems corrupted and could not be loaded");
    clear();
    if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
        Exceptions.printStackTrace(ex);
}
*/
