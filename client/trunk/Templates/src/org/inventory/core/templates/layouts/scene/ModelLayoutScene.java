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
package org.inventory.core.templates.layouts.scene;

import org.inventory.core.templates.layouts.scene.widgets.actions.ResizeShapeProvider;
import org.inventory.core.templates.layouts.scene.widgets.actions.MoveShapeProvider;
import java.awt.Color;
import java.awt.Dimension;
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
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.templates.layouts.scene.widgets.actions.GroupShapesAction1;
import org.inventory.core.templates.layouts.lookup.SharedContent;
import org.inventory.core.templates.layouts.lookup.SharedContentLookup;
import org.inventory.core.templates.layouts.menus.ShapeWidgetMenu;
import org.inventory.core.templates.layouts.model.CircleShape;
import org.inventory.core.templates.layouts.model.LabelShape;
import org.inventory.core.templates.layouts.model.PolygonShape;
import org.inventory.core.templates.layouts.model.RectangleShape;
import org.inventory.core.templates.layouts.providers.ModelLayoutAcceptProvider;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.model.ShapeFactory;
import org.inventory.core.templates.layouts.providers.ShapeNameAcceptProvider;
import org.inventory.core.templates.layouts.providers.ShapeSelectProvider;
import org.inventory.core.templates.layouts.widgets.CircleShapeWidget;
import org.inventory.core.templates.layouts.widgets.LabelShapeWidget;
import org.inventory.core.templates.layouts.widgets.PolygonShapeWidget;
import org.inventory.core.templates.layouts.widgets.RectangleShapeWidget;
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
    
    private boolean isNewCustomShape = false;
    private Shape newCustomShapeParent;
    private Shape newCustomShape;
            
    public ModelLayoutScene(LocalObjectListItem listItem) {
        this.listItem = listItem;
        shapesLayer = new LayerWidget(this);
        addChild(shapesLayer);
        getActions().addAction(ActionFactory.createAcceptAction(modelLayoutAcceptProvider));
                
        getActions().addAction(ActionFactory.createZoomAction());
        getInputBindings().setZoomActionModifiers(0); //No keystroke combinations
        getActions().addAction(ActionFactory.createPanAction());
        
        initSelectionListener();
    }
    
    public boolean isNewCustomShape() {
        return isNewCustomShape;
    }
    
    public void setIsNewCustomShape(boolean isNewCustomShape) {
        this.isNewCustomShape = isNewCustomShape;
    }
    
    public Shape getNewCustomShapeParent() {
        return newCustomShapeParent;
    }
    
    public void setNewCustomShapeParent(Shape newCustomShapeParent) {
        this.newCustomShapeParent = newCustomShapeParent;
    }
        
    public Shape getNewCustomShape() {
        return newCustomShape;
    }
    
    public void setNewCustomShape(Shape newCustomShape) {
        this.newCustomShape = newCustomShape;
    }
    
    public LocalObjectListItem getListItem() {
        return listItem;        
    }
    
    public Widget getRootWidget() {
        return rootWidget;
    }

    @Override
    protected Widget attachNodeWidget(Shape node) {
        Widget widget = null;
        if (node instanceof LabelShape)
            widget = new LabelShapeWidget(this, (LabelShape) node);
        else if (node instanceof RectangleShape)
            widget = new RectangleShapeWidget(this, (RectangleShape) node);
        else if (node instanceof CircleShape)
            widget = new CircleShapeWidget(this, (CircleShape) node);
        else if (node instanceof PolygonShape)
            widget = new PolygonShapeWidget(this, (PolygonShape) node);
        
        if (widget == null)
            throw new UnsupportedOperationException("The " + node.getShapeType() + " is not supported yet.");
        
        widget.setOpaque(node.isOpaque());
        
        shapeToWidget(node, widget);
        
        ResizeShapeProvider resizeShapeProvider = new ResizeShapeProvider();
        widget.getActions().addAction(ActionFactory.createSelectAction(new ShapeSelectProvider()));
        widget.getActions().addAction(ActionFactory.createResizeAction(resizeShapeProvider, resizeShapeProvider));
        widget.getActions().addAction(ActionFactory.createMoveAction(null, new MoveShapeProvider()));
////        widget.getActions().addAction(ActionFactory.createAcceptAction(modelLayoutAcceptProvider));
        widget.getActions().addAction(ActionFactory.createAcceptAction(shapeNameAcceptProvider));
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(ShapeWidgetMenu.getInstance()));
        
        if (node.getParent() == null) {
            /*
            if (shapesLayer.getChildren().size() == 1) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, I18N.gm("equipment_model_layout_scene_message"));
                return null;
            } else {
                */                
                shapesLayer.addChild(widget);
                rootWidget = widget;
////            }
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
        
        rootShape = (Shape) findObject(rootWidget);
        // The list item has no assigned none shape
        if (rootShape == null)
            return null;
        rootShape.setName(listItem.getName());
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameView = new QName("view"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnameView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), Constants.VIEW_FORMAT_VERSION)); //NOI18N
            
            QName qnLayout = new QName("layout"); //NOI18N
            xmlew.add(xmlef.createStartElement(qnLayout, null, null));
            
            createXMLLayout(rootWidget, xmlew, xmlef);
            
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
        
        QName qnShape = new QName("shape"); //NOI18N  
        xmlew.add(xmlef.createStartElement(qnShape, null, null));
        
        String type = parentShape.getShapeType();
        
        xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_TYPE), type));
        xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_NAME), parentShape.getName() != null ? parentShape.getName() : ""));
        xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_X), Integer.toString(parentShape.getX())));
        xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_Y), Integer.toString(parentShape.getY())));
        xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_WIDTH), Integer.toString(parentShape.getWidth())));
        xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_HEIGHT), Integer.toString(parentShape.getHeight())));
        xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_COLOR), Integer.toString(parentShape.getColor().getRGB())));
        xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_BORDER_COLOR), Integer.toString(parentShape.getBorderColor().getRGB())));
        xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_IS_EQUIPMENT), Boolean.toString(parentShape.isEquipment())));
        xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_OPAQUE), Boolean.toString(parentShape.isOpaque())));
        
        
        if (type.equals(RectangleShape.SHAPE_TYPE)) {
            
        }
        if (type.equals(LabelShape.SHAPE_TYPE)) {
            xmlew.add(xmlef.createAttribute(new QName("label"), ((LabelShape) parentShape).getLabel())); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("textColor"), Integer.toString(((LabelShape) parentShape).getTextColor().getRGB()))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("fontSize"), Integer.toString(((LabelShape) parentShape).getFontSize()))); //NOI18N
        }
        if (type.equals(CircleShape.SHAPE_TYPE)) {
            xmlew.add(xmlef.createAttribute(
                new QName(CircleShape.PROPERTY_ELLIPSE_COLOR), 
                Integer.toString(((CircleShape) parentShape).getEllipseColor().getRGB())));
            
            xmlew.add(xmlef.createAttribute(
                new QName(CircleShape.PROPERTY_OVAL_COLOR), 
                Integer.toString(((CircleShape) parentShape).getOvalColor().getRGB())));
        }
        if (type.equals(PolygonShape.SHAPE_TYPE)) {
            xmlew.add(xmlef.createAttribute(
                new QName(PolygonShape.PROPERTY_INTERIOR_COLOR), 
                Integer.toString(((PolygonShape) parentShape).getInteriorColor().getRGB())));
            
            xmlew.add(xmlef.createAttribute(
                new QName(PolygonShape.PROPERTY_OUTLINE_COLOR), 
                Integer.toString(((PolygonShape) parentShape).getOutlineColor().getRGB())));
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
        renderCustomShape(structure, null);
    }
        
    public void renderCustomShape(byte[] structure, Shape parent) {
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
                        recursiveRender(reader, tagShape, parent);
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
        String type = reader.getAttributeValue(null, Shape.PROPERTY_TYPE);
        Shape shape = ShapeFactory.getInstance().getShape(type);
        if (shape == null)
            return null;
        shape.setParent(parent);
        
        String attrValue;
        
        attrValue = reader.getAttributeValue(null, Shape.PROPERTY_NAME);
        if (attrValue != null)
            shape.setName(attrValue);
        
        attrValue = reader.getAttributeValue(null, Shape.PROPERTY_X);
        if (attrValue != null)
            shape.setX(Integer.valueOf(attrValue));
            
        attrValue = reader.getAttributeValue(null, Shape.PROPERTY_Y);
        if (attrValue != null)
            shape.setY(Integer.valueOf(attrValue));
        
        attrValue = reader.getAttributeValue(null, Shape.PROPERTY_WIDTH);
        if (attrValue != null)
            shape.setWidth(Integer.valueOf(attrValue));
                        
        attrValue = reader.getAttributeValue(null, Shape.PROPERTY_HEIGHT);
        if (attrValue != null)
            shape.setHeight(Integer.valueOf(attrValue));
                
        attrValue = reader.getAttributeValue(null, Shape.PROPERTY_COLOR);
        if (attrValue != null)
            shape.setColor(new Color(Integer.valueOf(attrValue)));
        
        attrValue = reader.getAttributeValue(null, Shape.PROPERTY_BORDER_COLOR);
        if (attrValue != null)
            shape.setBorderColor(new Color(Integer.valueOf(attrValue)));
        
        attrValue = reader.getAttributeValue(null, Shape.PROPERTY_IS_EQUIPMENT);
        if (attrValue != null)
            shape.setIsEquipment(Boolean.valueOf(attrValue));
        
        attrValue = reader.getAttributeValue(null, Shape.PROPERTY_OPAQUE);
        if (attrValue != null)
            shape.setOpaque(Boolean.valueOf(attrValue));
        
        if (RectangleShape.SHAPE_TYPE.equals(type)) {
        }
        if (LabelShape.SHAPE_TYPE.equals(type)) {
            attrValue = reader.getAttributeValue(null, "label"); //NOI18N
            if (attrValue != null)
                ((LabelShape) shape).setLabel(attrValue);
            
            attrValue = reader.getAttributeValue(null, "textColor"); //NOI18N
            if (attrValue != null)
                ((LabelShape) shape).setTextColor(new Color(Integer.valueOf(attrValue)));
            
            attrValue = reader.getAttributeValue(null, "fontSize"); //NOI18N
            if (attrValue != null)
                ((LabelShape) shape).setFontSize(Integer.valueOf(attrValue));                
        }
        if (CircleShape.SHAPE_TYPE.equals(type)) {
            attrValue = reader.getAttributeValue(null, CircleShape.PROPERTY_ELLIPSE_COLOR);
            if (attrValue != null)
                ((CircleShape) shape).setEllipseColor(new Color(Integer.valueOf(attrValue)));
            
            attrValue = reader.getAttributeValue(null, CircleShape.PROPERTY_OVAL_COLOR);
            if (attrValue != null)
                ((CircleShape) shape).setOvalColor(new Color(Integer.valueOf(attrValue)));
        }
        if (PolygonShape.SHAPE_TYPE.equals(type)) {
            attrValue = reader.getAttributeValue(null, PolygonShape.PROPERTY_INTERIOR_COLOR);
            if (attrValue != null)
                ((PolygonShape) shape).setInteriorColor(new Color(Integer.valueOf(attrValue)));
            
            attrValue = reader.getAttributeValue(null, PolygonShape.PROPERTY_OUTLINE_COLOR);
            if (attrValue != null)
                ((PolygonShape) shape).setOutlineColor(new Color(Integer.valueOf(attrValue)));
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
        
        if (targetWidget == null)
            return;            
        targetWidget.setPreferredLocation(new Point(sourceShape.getX(), sourceShape.getY()));
        targetWidget.setPreferredSize(new Dimension(sourceShape.getWidth(), sourceShape.getHeight()));
        targetWidget.setBackground(sourceShape.getColor());
        if (sourceShape.isOpaque()) {
            targetWidget.setBorder(BorderFactory.createLineBorder(sourceShape.getBorderWidth(), sourceShape.getBorderColor()));
            
        } else {
            targetWidget.setBorder(BorderFactory.createOpaqueBorder(
                sourceShape.getBorderWidth(), sourceShape.getBorderWidth(), 
                sourceShape.getBorderWidth(), sourceShape.getBorderWidth()));
        }
    }
    
    private void recursiveRender(XMLStreamReader reader, QName tagShape, Shape parent) throws XMLStreamException {
        Shape shape = XMLtoShape(reader, parent);
        
        if (shape == null)
            return;
        
        if (isNewCustomShape && parent == newCustomShapeParent)
            newCustomShape = shape;
                
        Widget shapeWidget = addNode(shape);
        
        if (shapeWidget == null)
            return;
        
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
    /*
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
    */
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
            
////            scene.fireShapeBoundsChange(widget, 
////                new Rectangle(child.getX(), child.getY(), child.getWidth(), child.getHeight()), 
////                new Rectangle(x, y, width, height));
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
    /*
    public void updateWidget(Widget oldWidget, double widthPercentage, double heightPercentage) {
        ModelLayoutScene scene = (ModelLayoutScene) oldWidget.getScene();
        // Gets the current shapes hierarchy
        Map<Shape, List<Shape>> hierarchy = new HashMap();
        getShapeHierarchy(scene, oldWidget, hierarchy);
        
        removeRecursive(scene, oldWidget);        
        if (!GroupShapesAction1.getInstance().isGroup()) {
            // If shapes are ungroup then no resize
            widthPercentage = 1;
            heightPercentage = 1;
        }
        Shape shape = (Shape) scene.findObject(oldWidget);
        addNewWidgetChildren(scene, hierarchy, shape, widthPercentage, heightPercentage);
    }
    */
    /*
    public Widget changeWidget(Dimension newSize, Widget oldWidget, Rectangle oldBounds, double widthPercentage, double heightPercentage, ResizeProvider.ControlPoint controlPoint) {
        if (oldWidget.getParentWidget() == null)
            return null;
        
        ModelLayoutScene scene = (ModelLayoutScene) oldWidget.getScene();
        // Gets the current shapes hierarchy
        Map<Shape, List<Shape>> hierarchy = new HashMap();
        getShapeHierarchy(scene, oldWidget, hierarchy);
        
        removeRecursive(scene, oldWidget);        
        if (!GroupShapesAction1.getInstance().isGroup()) {
            // If shapes are ungroup then no resize
            widthPercentage = 1;
            heightPercentage = 1;
        }
        Shape shape = (Shape) scene.findObject(oldWidget);
        
        Point sceneLocation = oldWidget.getParentWidget().convertLocalToScene(oldWidget.getLocation());
        
        shape.removePropertyChangeListener((PropertyChangeListener) oldWidget);        
        scene.removeNode(shape);
        scene.validate();
        scene.paint();
        
        Widget newWidget = scene.addNode(shape);
        
        Point localLocation = oldWidget.getLocation();
        //BOTTOM_CENTER, CENTER_RIGHT, BOTTOM_RIGHT don't has resize problem
        if (!(ResizeProvider.ControlPoint.BOTTOM_CENTER.equals(controlPoint) || 
            ResizeProvider.ControlPoint.CENTER_RIGHT.equals(controlPoint) || 
            ResizeProvider.ControlPoint.BOTTOM_RIGHT.equals(controlPoint))) {
            
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
            localLocation = newWidget.convertSceneToLocal(new Point(x, y));
        }
        newWidget.setPreferredLocation(localLocation);
        newWidget.setPreferredSize(newSize);
        
        scene.validate();
        scene.paint();
        
        addNewWidgetChildren(scene, hierarchy, shape, widthPercentage, heightPercentage);
        return newWidget;
    }
    */
}
