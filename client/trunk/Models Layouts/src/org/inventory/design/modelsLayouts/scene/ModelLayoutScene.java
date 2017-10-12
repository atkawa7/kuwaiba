/**
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.design.modelsLayouts.scene;

import org.inventory.design.modelsLayouts.scene.widgets.actions.ResizeShapeProvider;
import org.inventory.design.modelsLayouts.scene.widgets.actions.MoveShapeProvider;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.design.modelsLayouts.scene.widgets.actions.GroupShapesAction;
import org.inventory.design.modelsLayouts.lookup.SharedContent;
import org.inventory.design.modelsLayouts.lookup.SharedContentLookup;
import org.inventory.design.modelsLayouts.menus.ShapeWidgetMenu;
import org.inventory.design.modelsLayouts.model.LabelShape;
import org.inventory.design.modelsLayouts.model.RectangleShape;
import org.inventory.design.modelsLayouts.providers.ModelLayoutAcceptProvider;
import org.inventory.design.modelsLayouts.model.Shape;
import org.inventory.design.modelsLayouts.model.ShapeFactory;
import org.inventory.design.modelsLayouts.providers.ShapeNameAcceptProvider;
import org.inventory.design.modelsLayouts.providers.ShapeSelectProvider;
import org.inventory.design.modelsLayouts.scene.widgets.LabelShapeWidget;
import org.inventory.design.modelsLayouts.scene.widgets.RectangleShapeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Scene used to create and custom a model layout
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ModelLayoutScene extends AbstractScene<Shape, String> implements SharedContentLookup {
    private final LayerWidget shapesLayer;
    private final ModelLayoutAcceptProvider modelLayoutAcceptProvider = new ModelLayoutAcceptProvider();
    private final ShapeNameAcceptProvider shapeNameAcceptProvider = new ShapeNameAcceptProvider();
    
    private final LocalObjectListItem listItem;
    private Widget rootWidget;
            
    public ModelLayoutScene(LocalObjectListItem listItem) {
        this.listItem = listItem;
        shapesLayer = new LayerWidget(this);
        addChild(shapesLayer);
        getActions().addAction(ActionFactory.createAcceptAction(modelLayoutAcceptProvider));
        //setBackground(Color.WHITE);
        
        getActions().addAction(ActionFactory.createZoomAction());
        getInputBindings().setZoomActionModifiers(0); //No keystroke combinations
        getActions().addAction(ActionFactory.createPanAction());
        
        initSelectionListener();
    }
    
    public LocalObjectListItem getListItem() {
        return listItem;        
    }

    @Override
    protected Widget attachNodeWidget(Shape node) {
        Widget widget = null;
        if (node instanceof LabelShape) {
            widget = new LabelShapeWidget(this, (LabelShape) node);
            Font font = new Font(null, 0, ((LabelShape) node).getFontSize());
            ((LabelShapeWidget) widget).setFont(font);
            ((LabelShapeWidget) widget).setLabel(((LabelShape) node).getLabel());
            ((LabelShapeWidget) widget).setForeground(((LabelShape) node).getTextColor());
        }
        if (node instanceof RectangleShape) {
            widget = new RectangleShapeWidget(this, (RectangleShape) node);
        }        
        widget.setOpaque(true);
        
        shapeToWidget(node, widget);
        
        ResizeShapeProvider resizeShapeProvider = new ResizeShapeProvider();
        widget.getActions().addAction(ActionFactory.createSelectAction(new ShapeSelectProvider()));
        widget.getActions().addAction(ActionFactory.createResizeAction(resizeShapeProvider, resizeShapeProvider));
        widget.getActions().addAction(ActionFactory.createMoveAction(null, new MoveShapeProvider()));
        widget.getActions().addAction(ActionFactory.createAcceptAction(modelLayoutAcceptProvider));
        widget.getActions().addAction(ActionFactory.createAcceptAction(shapeNameAcceptProvider));
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(ShapeWidgetMenu.getInstance()));
        
        
        findWidget(node.getParent());
        if (node.getParent() == null) {
            if (shapesLayer.getChildren().size() == 1) {
                NotificationUtil.getInstance().showSimplePopup("Information", 
                    NotificationUtil.INFO_MESSAGE, "The scene can only contain one shape to be the root");
                return null;
            } else {
                shapesLayer.addChild(widget);
                rootWidget = widget;
            }
        } else {
            Widget parent = findWidget(node.getParent());
            parent.addChild(widget);
        }
        fireChangeEvent(new ActionEvent(this, ModelLayoutScene.SCENE_CHANGE, "Shape crated"));
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(String edge) {
        return null;
    }

    @Override
    protected void attachEdgeSourceAnchor(String edge, Shape oldSourceNode, Shape sourceNode) {
    }

    @Override
    protected void attachEdgeTargetAnchor(String edge, Shape oldTargetNode, Shape targetNode) {
    }
    
    @Override
    public byte[] getAsXML() {
        Shape rootShape = null;
        
//        for (Shape shape : getNodes()) {
//            if (listItem.getName().equals(shape.getName())) {
//                rootShape = shape;                
//            }
//        }
//        Widget rootWidget = shapesLayer.getChildren().isEmpty() ? null : shapesLayer.getChildren().get(0);
        rootShape = (Shape) findObject(rootWidget);
        // The list item has no assigned none shape
        if (rootShape == null)
            return null;
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameView = new QName("view");
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), Constants.VIEW_FORMAT_VERSION));
            
            QName qnLayout = new QName("layout");
            xmlew.add(xmlef.createStartElement(qnLayout, null, null));
            
            Widget parent = findWidget(rootShape);
            createXMLLayout(parent, xmlew, xmlef);
            
            xmlew.add(xmlef.createEndElement(qnLayout, null));
            
            xmlew.add(xmlef.createEndElement(qnameView, null));
            xmlew.close();
            return baos.toByteArray();
                        
        } catch(XMLStreamException xse) {
            
        }
        return null;
    }
    
    private void createXMLLayout(Widget parent, XMLEventWriter xmlew, XMLEventFactory xmlef) throws XMLStreamException {
        Shape parentShape = (Shape) findObject(parent);
        if (parentShape == null)
            return;        
        
        QName qnShape = new QName("shape");        
        xmlew.add(xmlef.createStartElement(qnShape, null, null));
        
        String type = "none";
        
        if (parentShape instanceof RectangleShape)
            type = RectangleShape.SHAPE_TYPE;
        
        if (parentShape instanceof LabelShape)
            type = LabelShape.SHAPE_TYPE;
        
        xmlew.add(xmlef.createAttribute(new QName("type"), type)); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("name"), parentShape.getName() != null ? parentShape.getName() : "")); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(parentShape.getX()))); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(parentShape.getY()))); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("width"), Integer.toString(parentShape.getWidth()))); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("height"), Integer.toString(parentShape.getHeight()))); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("color"), Integer.toString(parentShape.getColor().getRGB()))); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("boderWidth"), Integer.toString(parentShape.getBorderWidth()))); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("boderColor"), Integer.toString(parentShape.getBorderColor().getRGB()))); //NOI18N
        xmlew.add(xmlef.createAttribute(new QName("isEquipment"), Boolean.toString(parentShape.isEquipment()))); //NOI18N
        
        if (type.equals(RectangleShape.SHAPE_TYPE)) {
            
        }
        if (type.equals(LabelShape.SHAPE_TYPE)) {
            xmlew.add(xmlef.createAttribute(new QName("label"), ((LabelShape) parentShape).getLabel())); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("textColor"), Integer.toString(((LabelShape) parentShape).getTextColor().getRGB()))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("fontSize"), Integer.toString(((LabelShape) parentShape).getFontSize()))); //NOI18N
        }
        
        List<Widget> children = parent.getChildren();
        
        if (children.isEmpty()) {
            xmlew.add(xmlef.createEndElement(qnShape, null));
            return;
        }
        
        for (Widget child : children) {            
            createXMLLayout(child, xmlew, xmlef);
        }
        xmlew.add(xmlef.createEndElement(qnShape, null));
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        if (structure == null)
            return;
        
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        QName tagShape = new QName("shape"); //NOI18N
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()){
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {                    
                    if (reader.getName().equals(tagShape)) {
                        recursiveRender(reader, tagShape, null);
                    }
                }
            }
            reader.close();
            
            this.validate();
            this.repaint();
            
        } catch (NumberFormatException | XMLStreamException ex) {
            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.ERROR_MESSAGE, "The view seems corrupted and could not be loaded");
            clear();
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
                Exceptions.printStackTrace(ex);
        }
    }
    
    public static Shape XMLtoShape(XMLStreamReader reader, Shape parent) {
        String type = reader.getAttributeValue(null, "type"); //NOI18N
        Shape shape = ShapeFactory.getInstance().getShape(type);
        if (shape == null)
            return null;
        shape.setParent(parent);
        
        String name = reader.getAttributeValue(null, "name"); //NOI18N
        if (name != null)
            shape.setName(name);
        
        String x = reader.getAttributeValue(null, "x"); //NOI18N
        if (x != null)
            shape.setX(Integer.valueOf(x));
            
        String y = reader.getAttributeValue(null, "y"); //NOI18N
        if (y != null)
            shape.setY(Integer.valueOf(y));
        
        String width = reader.getAttributeValue(null, "width"); //NOI18N
        if (width != null)
            shape.setWidth(Integer.valueOf(width));
                        
        String height = reader.getAttributeValue(null, "height"); //NOI18N
        if (height != null)
            shape.setHeight(Integer.valueOf(height));
                
        String color = reader.getAttributeValue(null, "color"); //NOI18N
        if (color != null)
            shape.setColor(new Color(Integer.valueOf(color)));
        
        String borderWidth = reader.getAttributeValue(null, "boderWidth"); //NOI18N
        if (borderWidth != null)
            shape.setBorderWidth(Integer.valueOf(borderWidth));
            
        String borderColor = reader.getAttributeValue(null, "boderColor"); //NOI18N
        if (borderColor != null)
            shape.setBorderColor(new Color(Integer.valueOf(borderColor)));
        
        String isEquipment = reader.getAttributeValue(null, "isEquipment"); //NOI18N
        if (isEquipment != null)
            shape.setIsEquipment(Boolean.valueOf(isEquipment));
        
        if (RectangleShape.SHAPE_TYPE.equals(type)) {
        }
        if (LabelShape.SHAPE_TYPE.equals(type)) {
            String label = reader.getAttributeValue(null, "label"); //NOI18N
            if (label != null)
                ((LabelShape) shape).setLabel(label);
            
            String textColor = reader.getAttributeValue(null, "textColor"); //NOI18N
            if (textColor != null)
                ((LabelShape) shape).setTextColor(new Color(Integer.valueOf(textColor)));
            
            String fontSize = reader.getAttributeValue(null, "fontSize"); //NOI18N
            if (fontSize != null)
                ((LabelShape) shape).setFontSize(Integer.valueOf(fontSize));                
        }
        
        return shape;                        
    }
    
    public static void shapeToWidget(Shape sourceShape, Widget targetWidget) {
        //TODO: Calculate the width and height using the dimension in the parent
        /*
        Widget parentWidget = sourceShape.getParent() == null ? this : findWidget(sourceShape.getParent());
        if (parentWidget == null) { return; } 
        
        Rectangle parentBounds = parentWidget.getPreferredBounds();
        
        if (parentBounds == null) { return; }
        */
        if (sourceShape.getX() == null) { sourceShape.setX(0); }
        
        if (sourceShape.getY() == null) { sourceShape.setY(0); }
        
        if (sourceShape.getWidth() == null)
            sourceShape.setWidth(64 /*parentBounds.width / 2*/);
        if (sourceShape.getHeight() == null)
            sourceShape.setHeight(32 /*parentBounds.height / 2*/);
        
        targetWidget.setPreferredLocation(new Point(sourceShape.getX(), sourceShape.getY()));
        targetWidget.setPreferredSize(new Dimension(sourceShape.getWidth(), sourceShape.getHeight()));
        targetWidget.setBackground(sourceShape.getColor());
        targetWidget.setBorder(BorderFactory.createLineBorder(sourceShape.getBorderWidth(), sourceShape.getBorderColor()));
    }
    
    private void recursiveRender(XMLStreamReader reader, QName tagShape, Shape parent) throws XMLStreamException {
        String type = reader.getAttributeValue(null, "type"); //NOI18N
        Shape shape = XMLtoShape(reader, parent);
        
        if (shape == null)
            return;
                
        Widget shapeWidget = addNode(shape);
        
        if (RectangleShape.SHAPE_TYPE.equals(type)) {
        }
        if (LabelShape.SHAPE_TYPE.equals(type)) {
            ((LabelShapeWidget) shapeWidget).setLabel(((LabelShape) shape).getLabel());
            ((LabelShapeWidget) shapeWidget).setForeground(((LabelShape) shape).getTextColor());
        }        
        shapeToWidget(shape, shapeWidget);
        validate();
        paint();
        
        while (reader.hasNext()) {
            
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (reader.getName().equals(tagShape)) {
                    recursiveRender(reader, tagShape, shape);
                }                                              
            } else if (event == XMLStreamConstants.END_ELEMENT)
                return;
        }
    }
    

    @Override
    public void render(Shape root) {
    }

    @Override
    public ConnectProvider getConnectProvider() {
        return null;
    }

    @Override
    public boolean supportsConnections() {
        return false;
    }

    @Override
    public boolean supportsBackgrounds() {
        return false;
    }

    @Override
    public Lookup fixLookup() {
        PaletteController pallete = SharedContent.getInstance().getAbstractLookup().lookup(PaletteController.class);
        SharedContent.getInstance().getInstanceContent().set(Collections.singleton(pallete), null);                
        return SharedContent.getInstance().getAbstractLookup();
    }
    
    public void fireShapeBoundsChange(Widget widget, Rectangle oldShapeBounds, Rectangle newShapeBounds) {
        Shape shape = (Shape) ((ModelLayoutScene) widget.getScene()).findObject(widget);
        if (shape != null) {
            shape.setX(newShapeBounds.x);
            shape.firePropertyChange(widget, Shape.PROPERTY_X, oldShapeBounds.x, newShapeBounds.x);
            shape.setY(newShapeBounds.y);
            shape.firePropertyChange(widget, Shape.PROPERTY_Y, oldShapeBounds.y, newShapeBounds.y);
            shape.setWidth(newShapeBounds.width);
            shape.firePropertyChange(widget, Shape.PROPERTY_WIDTH, oldShapeBounds.width, newShapeBounds.width);
            shape.setHeight(newShapeBounds.height);
            shape.firePropertyChange(widget, Shape.PROPERTY_HEIGHT, oldShapeBounds.height, newShapeBounds.height);
        }
    }
    
    private void getShapeHierarchy(ModelLayoutScene scene, Widget widget, Map<Shape, List<Shape>> hierarchy) {
        Shape shape = (Shape) scene.findObject(widget);                                                                        
        
        List<Shape> shapeChildren = new ArrayList();
        for (Widget widgetChild : widget.getChildren()) {
            Shape shapeChild = (Shape) scene.findObject(widgetChild);
            getShapeHierarchy(scene, widgetChild, hierarchy);
            shapeChildren.add(shapeChild);           
        }
        hierarchy.put(shape, shapeChildren);        
    }
    
    private void addNewWidgetChildren(ModelLayoutScene scene, Map<Shape, List<Shape>> hierarchy, Shape shape, double widthPercentage, double heightPercentage) {
        for (Shape child : hierarchy.get(shape)) {
            Widget widget = scene.addNode(child);
            
            int x = (int) (child.getX() * widthPercentage);                
            int y = (int) (child.getY() * heightPercentage);
            int width = (int) (child.getWidth() * widthPercentage);
            int height = (int) (child.getHeight() * heightPercentage);
            
            widget.setPreferredLocation(new Point(x, y));
            widget.setPreferredSize(new Dimension(width, height));
            
            scene.validate();
            scene.paint();
            
            addNewWidgetChildren(scene, hierarchy, child, widthPercentage, heightPercentage);
            
            scene.fireShapeBoundsChange(widget, 
                new Rectangle(child.getX(), child.getY(), child.getWidth(), child.getHeight()), 
                new Rectangle(x, y, width, height));
        }
    }
    
    private void removeRecursive(ModelLayoutScene scene, Widget oldWidget) {
        while (!oldWidget.getChildren().isEmpty()) {
            Widget firstChild = oldWidget.getChildren().get(0);
            
            removeRecursive(scene, firstChild);
            Shape shapeChild = (Shape) scene.findObject(firstChild);
            shapeChild.removePropertyChangeListener((PropertyChangeListener) firstChild);
            scene.removeNode(shapeChild);            
        }
    }
    
    public Widget changeWidget(Dimension newSize, Widget oldWidget, Rectangle oldBounds, double widthPercentage, double heightPercentage, ResizeProvider.ControlPoint controlPoint) {
        if (oldWidget.getParentWidget() == null)
            return null;
        
        Widget newWidget = oldWidget;
        ModelLayoutScene scene = (ModelLayoutScene) oldWidget.getScene();
        // Gets the current shapes hierarchy
        Map<Shape, List<Shape>> hierarchy = new HashMap();
        getShapeHierarchy(scene, oldWidget, hierarchy);
        // If shapes are group then remove children recursive
        if (GroupShapesAction.getInstance().isGroup())
            removeRecursive(scene, oldWidget);
        
        Shape shape = (Shape) scene.findObject(oldWidget);
        //BOTTOM_CENTER, CENTER_RIGHT, BOTTOM_RIGHT don't has resize problem
        if (!(ResizeProvider.ControlPoint.BOTTOM_CENTER.equals(controlPoint) || 
            ResizeProvider.ControlPoint.CENTER_RIGHT.equals(controlPoint) || 
            ResizeProvider.ControlPoint.BOTTOM_RIGHT.equals(controlPoint))) {
            
            Point sceneLocation = oldWidget.getParentWidget().convertLocalToScene(oldWidget.getLocation());
            
            shape.removePropertyChangeListener((PropertyChangeListener) oldWidget);        
            scene.removeNode(shape);
            scene.validate();
            scene.paint();                
            
            int x = sceneLocation.x;
            int y = sceneLocation.y;                

            Rectangle newBounds = oldWidget.getBounds();

            if (ResizeProvider.ControlPoint.TOP_CENTER.equals(controlPoint) || 
                ResizeProvider.ControlPoint.TOP_RIGHT.equals(controlPoint))
                y += oldBounds.height - newBounds.height;
            if (ResizeProvider.ControlPoint.CENTER_LEFT.equals(controlPoint) || 
                ResizeProvider.ControlPoint.BOTTOM_LEFT.equals(controlPoint))
                x += oldBounds.width - newBounds.width;
            if (ResizeProvider.ControlPoint.TOP_LEFT.equals(controlPoint)) {
                x += oldBounds.width - newBounds.width;
                y += oldBounds.height - newBounds.height;            
            }
            newWidget = scene.addNode(shape);
            Point localLocation = newWidget.convertSceneToLocal(new Point(x, y));

            newWidget.setPreferredLocation(localLocation);
        }
        newWidget.setPreferredSize(newSize);
        
        scene.validate();
        scene.paint();
        // If shapes are group then resizing recursive
        if (GroupShapesAction.getInstance().isGroup())
            addNewWidgetChildren(scene, hierarchy, shape, widthPercentage, heightPercentage);
        return newWidget;
    }
}
