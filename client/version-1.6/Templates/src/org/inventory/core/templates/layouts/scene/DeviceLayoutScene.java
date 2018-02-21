/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
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

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.JPopupMenu;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.lookup.SharedContent;
import org.inventory.core.templates.layouts.lookup.SharedContentLookup;
import org.inventory.core.templates.layouts.menus.ShapeWidgetMenu;
import org.inventory.core.templates.layouts.model.CircleShape;
import org.inventory.core.templates.layouts.model.ContainerShape;
import org.inventory.core.templates.layouts.model.CustomShape;
import org.inventory.core.templates.layouts.model.LabelShape;
import org.inventory.core.templates.layouts.model.PolygonShape;
import org.inventory.core.templates.layouts.model.RectangleShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.model.ShapeFactory;
import org.inventory.core.templates.layouts.providers.ShapeNameAcceptProvider;
import org.inventory.core.templates.layouts.providers.ShapeSelectProvider;
import org.inventory.core.templates.layouts.scene.widgets.actions.PasteShapeAction;
import org.inventory.core.templates.layouts.widgets.ContainerShapeWidget;
import org.inventory.core.templates.layouts.widgets.ShapeWidgetFactory;
import org.inventory.core.templates.layouts.widgets.providers.DeviceLayoutAcceptProviderToDevices;
import org.inventory.core.templates.layouts.widgets.providers.MoveContainerShapeProvider;
import org.inventory.core.templates.layouts.widgets.providers.MoveShapeWidgetProvider;
import org.inventory.core.templates.layouts.widgets.providers.ResizeContainerShapeProvider;
import org.inventory.core.templates.layouts.widgets.providers.ResizeShapeWidgetProvider;
import org.inventory.core.templates.layouts.widgets.providers.DeviceLayoutAcceptProviderToShapes;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.PopupMenuProvider;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Scene used to design an Equipment Layout 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeviceLayoutScene extends AbstractScene<Shape, String> implements SharedContentLookup {
    private final List<CustomShape> customShapes;
    private final ShapeNameAcceptProvider shapeNameAcceptProvider = new ShapeNameAcceptProvider();
    
    private LayerWidget guideLayer;
    private final LocalObjectListItem model;
    
    private boolean addContainer = false;
    /**
     * Repository to storage the custom shapes layout structures
     */
    private HashMap<LocalObjectListItem, byte[]> structureRepository = new HashMap();
    
    public DeviceLayoutScene(LocalObjectListItem model) {
        this.model = model;
        
        nodeLayer = new LayerWidget(this);        
        guideLayer = new LayerWidget(this);
        addChild(guideLayer);
        addChild(nodeLayer);
        
        getActions().addAction(ActionFactory.createSelectAction(new SelectProvider() {

            @Override
            public boolean isAimingAllowed(Widget widget, Point localLocation, boolean invertSelection) {
                return false;
            }

            @Override
            public boolean isSelectionAllowed(Widget widget, Point localLocation, boolean invertSelection) {
                return addContainer;
            }

            @Override
            public void select(Widget widget, Point localLocation, boolean invertSelection) {
                addContainer = false;
                addContainerShape(widget.convertLocalToScene(localLocation));
            }
        }));
        
        getActions().addAction(ActionFactory.createZoomAction());
        getInputBindings().setZoomActionModifiers(0); //No keystroke combinations
        getActions().addAction(ActionFactory.createPanAction());
                
        getActions().addAction(ActionFactory.createPopupMenuAction(new PopupMenuProvider() {
            private JPopupMenu popupMenu = null;

            @Override
            public JPopupMenu getPopupMenu(Widget widget, Point localLocation) {
                if (popupMenu == null) {
                    popupMenu = new JPopupMenu();
                    popupMenu.add(PasteShapeAction.getInstance());
                }
                PasteShapeAction.getInstance().setSelectedWidget(widget);
                PasteShapeAction.getInstance().setLocation(localLocation);
                return popupMenu;
            }
        }));                
        getActions().addAction(ActionFactory.createAcceptAction(new DeviceLayoutAcceptProviderToShapes()));
        getActions().addAction(ActionFactory.createAcceptAction(new DeviceLayoutAcceptProviderToDevices()));
        
        customShapes = new ArrayList();
        initSelectionListener();
        initGuideLayer();
    }    
    
    
    public LayerWidget getGuideLayer() {
        return guideLayer;
    }
    
    public LocalObjectListItem getModel() {
        return model;        
    }
    
    private void initGuideLayer() {
        guideLayer.setLayout(LayoutFactory.createVerticalFlowLayout());
        
        int rackUnitWidth = 1086 * 3;
        int rackUnitHeight = 100 * 3;
        int spanHeight = 15;
        
        for (int i = 0; i < 8; i += 1) {
            Widget rackUnitGuide = new Widget(this);
            rackUnitGuide.setOpaque(true);
            rackUnitGuide.setBorder(BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 
                Shape.DEFAULT_BORDER_SIZE, Shape.DEFAULT_BORDER_SIZE, true));
            rackUnitGuide.setPreferredBounds(new Rectangle(-Shape.DEFAULT_BORDER_SIZE, -Shape.DEFAULT_BORDER_SIZE, rackUnitWidth, rackUnitHeight));

            Widget span = new Widget(this);
            span.setOpaque(false);
            rackUnitGuide.setBorder(BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 
                Shape.DEFAULT_BORDER_SIZE, Shape.DEFAULT_BORDER_SIZE, true));
            span.setPreferredBounds(new Rectangle(-Shape.DEFAULT_BORDER_SIZE, -Shape.DEFAULT_BORDER_SIZE, rackUnitWidth, spanHeight));

            guideLayer.addChild(rackUnitGuide);
            guideLayer.addChild(span);
        }
    }
    
    public void setAddContainerShape(boolean addContainer) {
        this.addContainer = addContainer;
    }
    
    private void addContainerShape(Point location) {
        Shape shape = new ContainerShape();
        shape.setX(location.x);
        shape.setY(location.y);
        shape.setWidth(Shape.DEFAULT_WITH);
        shape.setHeight(Shape.DEFAULT_HEIGHT);
        shape.setBorderWidth(-Shape.DEFAULT_BORDER_SIZE);
        
        addNode(shape);
    }
    
    private boolean isInnerShape(Shape shape) {
        for (CustomShape customShape : customShapes) {
            ContainerShapeWidget customShapeWidget = (ContainerShapeWidget) findWidget(customShape);
            if (customShapeWidget != null) {
                for (Shape innerShape : customShapeWidget.getShapesSet()) {
                    if (shape.equals(innerShape))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public byte[] getAsXML() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
            XMLEventWriter xmlew = xmlof.createXMLEventWriter(baos);
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName tagView = new QName("view"); //NOI18N
            xmlew.add(xmlef.createStartElement(tagView, null, null));
            xmlew.add(xmlef.createAttribute(new QName("version"), Constants.VIEW_FORMAT_VERSION)); //NOI18N
            
            QName tagLayout = new QName("layout"); //NOI18N
            xmlew.add(xmlef.createStartElement(tagLayout, null, null));
            
            Rectangle layoutBounds = getLayoutBounds(nodeLayer.getChildren());
            
            xmlew.add(xmlef.createAttribute(new QName("x"), Integer.toString(layoutBounds.x))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("y"), Integer.toString(layoutBounds.y))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("width"), Integer.toString(layoutBounds.width))); //NOI18N
            xmlew.add(xmlef.createAttribute(new QName("height"), Integer.toString(layoutBounds.height))); //NOI18N
            
            List<Widget> children = nodeLayer.getChildren();
            for (Widget child : children) {
                Shape shape = (Shape) findObject(child);
                if (shape == null)
                    continue;
                
                if (isInnerShape(shape))
                    continue;

                QName tagShape = new QName("shape"); //NOI18N
                xmlew.add(xmlef.createStartElement(tagShape, null, null));
                
                String shapeType = shape.getShapeType();
                
                xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_TYPE), shapeType));
                xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_X), Integer.toString(shape.getX() - layoutBounds.x)));
                xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_Y), Integer.toString(shape.getY() - layoutBounds.y)));
                xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_WIDTH), Integer.toString(shape.getWidth())));
                xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_HEIGHT), Integer.toString(shape.getHeight())));
                xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_OPAQUE), Boolean.toString(shape.isOpaque())));
                xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_IS_EQUIPMENT), Boolean.toString(shape.isEquipment())));
                xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_NAME), shape.getName() != null ? shape.getName() : ""));
                                
                if (ContainerShape.SHAPE_TYPE.equals(shapeType)) {
                    
                    List<Shape> shapesSet = ((ContainerShapeWidget) child).getShapesSet();
                    for (Shape innerShape : shapesSet) {
                        Widget innerShapeWidget = findWidget(innerShape);
                        
                        if (innerShapeWidget == null)
                            continue;
                        
                        int index = nodeLayer.getChildren().indexOf(innerShapeWidget);
                        
                        QName qnameChild = new QName("child"); //NOI18N
                        xmlew.add(xmlef.createStartElement(qnameChild, null, null));
                        xmlew.add(xmlef.createAttribute(new QName("index"), Integer.toString(index))); //NOI18N                        
                        xmlew.add(xmlef.createEndElement(qnameChild, null));
                    }                    
                } else if (CustomShape.SHAPE_TYPE.equals(shape.getShapeType())) {
                    xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_ID), Long.toString(((CustomShape) shape).getListItem().getId())));
                    xmlew.add(xmlef.createAttribute(new QName(Constants.PROPERTY_CLASSNAME), ((CustomShape) shape).getListItem().getClassName()));
                    
                } else {                    
                    xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_COLOR), Integer.toString(shape.getColor().getRGB())));
                    xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_BORDER_COLOR), Integer.toString(shape.getBorderColor().getRGB())));
//                    xmlew.add(xmlef.createAttribute(new QName(Shape.PROPERTY_IS_EQUIPMENT), Boolean.toString(shape.isEquipment())));
                                        
                    if (RectangleShape.SHAPE_TYPE.equals(shapeType)) {
                        xmlew.add(xmlef.createAttribute(new QName(RectangleShape.PROPERTY_IS_SLOT), Boolean.toString(((RectangleShape) shape).isSlot())));
                    } else if (LabelShape.SHAPE_TYPE.equals(shapeType)) {
                        xmlew.add(xmlef.createAttribute(new QName("label"), ((LabelShape) shape).getLabel())); //NOI18N
                        xmlew.add(xmlef.createAttribute(new QName("textColor"), Integer.toString(((LabelShape) shape).getTextColor().getRGB()))); //NOI18N
                        xmlew.add(xmlef.createAttribute(new QName("fontSize"), Integer.toString(((LabelShape) shape).getFontSize()))); //NOI18N
                    } else if (CircleShape.SHAPE_TYPE.equals(shapeType)) {
                        xmlew.add(xmlef.createAttribute(
                            new QName(CircleShape.PROPERTY_ELLIPSE_COLOR), 
                            Integer.toString(((CircleShape) shape).getEllipseColor().getRGB())));

                        xmlew.add(xmlef.createAttribute(
                            new QName(CircleShape.PROPERTY_OVAL_COLOR), 
                            Integer.toString(((CircleShape) shape).getOvalColor().getRGB())));
                    } else if (PolygonShape.SHAPE_TYPE.equals(shapeType)) {
                        xmlew.add(xmlef.createAttribute(
                            new QName(PolygonShape.PROPERTY_INTERIOR_COLOR), 
                            Integer.toString(((PolygonShape) shape).getInteriorColor().getRGB())));

                        xmlew.add(xmlef.createAttribute(
                            new QName(PolygonShape.PROPERTY_OUTLINE_COLOR), 
                            Integer.toString(((PolygonShape) shape).getOutlineColor().getRGB())));
                    }
                }
                xmlew.add(xmlef.createEndElement(tagShape, null));
            }
            xmlew.add(xmlef.createEndElement(tagLayout, null));
            
            xmlew.add(xmlef.createEndElement(tagView, null));
            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            return null;
        }
    }
    
    public Rectangle getLayoutBounds(List<Widget> children) {
        int xmin = Integer.MAX_VALUE;
        int ymin = Integer.MAX_VALUE;
        int xmax = Integer.MIN_VALUE;
        int ymax = Integer.MIN_VALUE;
                
        for (Widget child : children) {
            Point childPoint = child.getPreferredLocation();
            Rectangle childBounds = child.getPreferredBounds();
            /*
                0-----1
                |     |
                |     |
                3-----2
            */
            Point [] points = new Point[4];
            points[0] = new Point(childPoint.x, childPoint.y);
            points[1] = new Point(childPoint.x + childBounds.width, childPoint.y);
            points[2] = new Point(childPoint.x + childBounds.width, childPoint.y + childBounds.height);
            points[3] = new Point(childPoint.x, childPoint.y + childBounds.height);
                        
            for (Point point : points) {
                if (xmin > point.x) {xmin = point.x;}

                if (ymin > point.y) {ymin = point.y;}

                if (xmax < point.x) {xmax = point.x;}

                if (ymax < point.y) {ymax = point.y;}
            }
        }
        return new Rectangle(xmin, ymin, xmax - xmin, ymax - ymin);
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        clear();
        structureRepository.clear();
        render(structure, null, null);
    }

    @Override
    public void render(Shape root) {
    }
    
    private void render(byte[] structure, CustomShape customShape, ContainerShapeWidget customShapeWidget) throws IllegalArgumentException {
        if (structure == null)
            return;
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
                        
            QName tagLayout = new QName("layout"); //NOI18N
            QName tagShape = new QName("shape"); //NOI18N
            QName tagChild = new QName("child"); //NOI18N
            String attrValue;
            
            Rectangle layoutBounds = null;
            
            int childrenSize = nodeLayer.getChildren().size();
            
            while (reader.hasNext()) {
                int event = reader.next();                
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagLayout)) {
                        int x = 0;
                        int y = 0;
                        
                        if (customShape != null) {
                            x = customShape.getX();
                            y = customShape.getY();
                            
                            customShape.setX(x);
                            customShape.setY(y);
                        } else {
                            attrValue = reader.getAttributeValue(null, "x"); //NOI18N

                            if (attrValue != null)
                                x = Integer.valueOf(attrValue); 

                            attrValue = reader.getAttributeValue(null, "y"); //NOI18N

                            if (attrValue != null)
                                y = Integer.valueOf(attrValue);
                        }
                        attrValue = reader.getAttributeValue(null, "width"); //NOI18N
                        int width = 0;
                        if (attrValue != null)
                            width = Integer.valueOf(attrValue); 
                        
                        int height = 0;
                        attrValue = reader.getAttributeValue(null, "height"); //NOI18N
                        if (attrValue != null)
                            height = Integer.valueOf(attrValue);
                        
                        if (customShape != null) {
                            customShape.setWidth(width);
                            customShape.setHeight(height);
                        }                        
                        layoutBounds = new Rectangle(x, y, width, height);
                    }
                    if (reader.getName().equals(tagShape)) {
                        String shapeType = reader.getAttributeValue(null, Shape.PROPERTY_TYPE);
                        
                        Shape shape = null;
                        
                        if (CustomShape.SHAPE_TYPE.equals(shapeType)) {
                            String id = reader.getAttributeValue(null, Constants.PROPERTY_ID);
                            String className= reader.getAttributeValue(null, Constants.PROPERTY_CLASSNAME);
                            
                            LocalObjectLight lol = CommunicationsStub.getInstance().getObjectInfoLight(className, Long.valueOf(id));
                            if (lol == null)
                                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                            else {
                                LocalObjectListItem listItem = new LocalObjectListItem(lol.getOid(), lol.getClassName(), lol.getName());
                                shape = ShapeFactory.getInstance().getCustomShape(listItem);
                            }
                            
                        } else
                            shape = ShapeFactory.getInstance().getShape(shapeType);
                        
                        if (shape != null) {
                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_X);
                            if (attrValue != null)
                                shape.setX(Integer.valueOf(attrValue) + (layoutBounds == null ? 0 : layoutBounds.x));

                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_Y);
                            if (attrValue != null)
                                shape.setY(Integer.valueOf(attrValue) + (layoutBounds == null ? 0 : layoutBounds.y));

                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_WIDTH);
                            if (attrValue != null)
                                shape.setWidth(Integer.valueOf(attrValue));

                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_HEIGHT);
                            if (attrValue != null)
                                shape.setHeight(Integer.valueOf(attrValue));

                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_OPAQUE);
                            if (attrValue != null)
                                shape.setOpaque(Boolean.valueOf(attrValue));
                            
//                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_IS_EQUIPMENT);
//                            if (attrValue != null)
//                                shape.setOpaque(Boolean.valueOf(attrValue));
                            
                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_NAME);
                            if (attrValue != null)
                                shape.setName(attrValue);
                            
                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_IS_EQUIPMENT);
                            if (attrValue != null)
                                shape.setIsEquipment(Boolean.valueOf(attrValue));
                            
                            if (ContainerShape.SHAPE_TYPE.equals(shapeType)) {
                                ContainerShapeWidget containerShapeWidget = (ContainerShapeWidget) addNode(shape);
                                if (customShapeWidget != null)
                                    customShapeWidget.getShapesSet().add(shape);

                                while (true) {
                                    reader.nextTag();
                                    if (reader.getName().equals(tagChild)) {
                                        if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {                                        
                                            attrValue = reader.getAttributeValue(null, "index");

                                            if (attrValue != null) {
                                                int childIndex = Integer.valueOf(attrValue);

                                                Widget childWidget = nodeLayer.getChildren().get(childrenSize + childIndex);

                                                if (childWidget != null) {
                                                    Shape childShape = (Shape) findObject(childWidget);
                                                    if (childShape != null)
                                                        containerShapeWidget.getShapesSet().add(childShape);
                                                }
                                            }
                                        }
                                    } else
                                        break;
                                }
                            } else if (CustomShape.SHAPE_TYPE.equals(shapeType)) {
                                CustomShape tempCustomShape = (CustomShape) shape.shapeCopy();
                                                                
                                Widget widget = addNode(shape);
                                
                                Point widgetPoint = new Point(tempCustomShape.getX(), tempCustomShape.getY());
                                Rectangle widgetBounds = new Rectangle(
                                    -Shape.DEFAULT_BORDER_SIZE, -Shape.DEFAULT_BORDER_SIZE, 
                                    tempCustomShape.getWidth(), tempCustomShape.getHeight());
                                
                                if (!widgetPoint.equals(widgetPoint) || !widget.getPreferredBounds().equals(widgetBounds)) {
                                    
                                    ResizeContainerShapeProvider resizeContainerShapeProvider = new ResizeContainerShapeProvider();
                                    resizeContainerShapeProvider.resizingStarted(widget);

                                    widget.setPreferredLocation(widgetPoint);
                                    widget.setPreferredBounds(widgetBounds);
                                    widget.revalidate();

                                    resizeContainerShapeProvider.resizingFinished(widget);
                                }
                                
                            } else {
                                attrValue = reader.getAttributeValue(null, Shape.PROPERTY_COLOR);
                                if (attrValue != null)
                                    shape.setColor(new Color(Integer.valueOf(attrValue)));

                                attrValue = reader.getAttributeValue(null, Shape.PROPERTY_BORDER_COLOR);
                                if (attrValue != null)
                                    shape.setBorderColor(new Color(Integer.valueOf(attrValue)));

                                if (RectangleShape.SHAPE_TYPE.equals(shapeType)) {
                                    attrValue = reader.getAttributeValue(null, RectangleShape.PROPERTY_IS_SLOT);
                                    
                                    if (attrValue != null)
                                        ((RectangleShape) shape).setIsSlot(Boolean.valueOf(attrValue));
                                    
                                } else if (LabelShape.SHAPE_TYPE.equals(shapeType)) {
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
                                if (CircleShape.SHAPE_TYPE.equals(shapeType)) {
                                    attrValue = reader.getAttributeValue(null, CircleShape.PROPERTY_ELLIPSE_COLOR);
                                    if (attrValue != null)
                                        ((CircleShape) shape).setEllipseColor(new Color(Integer.valueOf(attrValue)));

                                    attrValue = reader.getAttributeValue(null, CircleShape.PROPERTY_OVAL_COLOR);
                                    if (attrValue != null)
                                        ((CircleShape) shape).setOvalColor(new Color(Integer.valueOf(attrValue)));
                                }
                                if (PolygonShape.SHAPE_TYPE.equals(shapeType)) {
                                    attrValue = reader.getAttributeValue(null, PolygonShape.PROPERTY_INTERIOR_COLOR);
                                    if (attrValue != null)
                                        ((PolygonShape) shape).setInteriorColor(new Color(Integer.valueOf(attrValue)));

                                    attrValue = reader.getAttributeValue(null, PolygonShape.PROPERTY_OUTLINE_COLOR);
                                    if (attrValue != null)
                                        ((PolygonShape) shape).setOutlineColor(new Color(Integer.valueOf(attrValue)));
                                }
                                addNode(shape);
                                if (customShapeWidget != null)
                                    customShapeWidget.getShapesSet().add(shape);
                            }
                        }
                    }
                }
            }
            reader.close();
            this.validate();
            this.repaint();
            
        } catch (XMLStreamException ex) {
            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.ERROR_MESSAGE, "The view seems corrupted and could not be loaded");
            clear();
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
            Exceptions.printStackTrace(ex);
        }
    }
    
    private boolean renderCustomShape(CustomShape customShape, ContainerShapeWidget containerShapeWidget) {
        if (customShape == null)
            return false;
        
        LocalObjectListItem customShapeModel = customShape.getListItem();
        if (structureRepository.containsKey(customShapeModel)) {
            render(structureRepository.get(customShapeModel), customShape, containerShapeWidget);            
            return true;
        } else {
            List<LocalObjectViewLight> relatedViews = CommunicationsStub.getInstance().getListTypeItemRelatedViews(customShapeModel.getId(), customShapeModel.getClassName());
            if (relatedViews != null) {
                if (!relatedViews.isEmpty()) {
                    LocalObjectView layoutView = CommunicationsStub.getInstance().getListTypeItemRelatedView(customShapeModel.getId(), customShapeModel.getClassName(), relatedViews.get(0).getId());
                    if (layoutView != null) {
                        byte [] structure = layoutView.getStructure();
                        if (structure != null) {  
                            structureRepository.put(customShapeModel, structure);
                            render(structure, customShape, containerShapeWidget);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
    protected Widget attachNodeWidget(Shape node) {
        Widget shapeWidget = ShapeWidgetFactory.getInstance().getShapeWidget(this, node);
        shapeWidget.getActions().addAction(ActionFactory.createSelectAction(new ShapeSelectProvider()));
        
        if (shapeWidget instanceof ContainerShapeWidget) {
            ResizeContainerShapeProvider resizeContainerShapeProvider = new ResizeContainerShapeProvider();
            shapeWidget.getActions().addAction(ActionFactory.createResizeAction(resizeContainerShapeProvider, resizeContainerShapeProvider));
            shapeWidget.getActions().addAction(ActionFactory.createMoveAction(null, new MoveContainerShapeProvider()));
            
            if (((ContainerShapeWidget) shapeWidget).isCustomShape()) {
                CustomShape customShape = node instanceof CustomShape ? (CustomShape) node : null;
                if (renderCustomShape(customShape, (ContainerShapeWidget) shapeWidget)) {
                    shapeWidget.setPreferredBounds(new Rectangle(
                        -Shape.DEFAULT_BORDER_SIZE, -Shape.DEFAULT_BORDER_SIZE, 
                        customShape.getWidth(), customShape.getHeight()));
                } else
                    return null;
            }
        } else {
            ResizeShapeWidgetProvider resizeShapeWidgetProvider = new ResizeShapeWidgetProvider();
            shapeWidget.getActions().addAction(ActionFactory.createResizeAction(resizeShapeWidgetProvider, resizeShapeWidgetProvider));
            shapeWidget.getActions().addAction(ActionFactory.createMoveAction(null, new MoveShapeWidgetProvider()));
        }
        shapeWidget.getActions().addAction(ActionFactory.createAcceptAction(shapeNameAcceptProvider));
        shapeWidget.getActions().addAction(ActionFactory.createPopupMenuAction(ShapeWidgetMenu.getInstance()));
                
        nodeLayer.addChild(shapeWidget);
        
        if (node instanceof CustomShape)
            customShapes.add((CustomShape) node);
                        
        validate();
        paint();                        
        return shapeWidget;
    }

    @Override
    protected Widget attachEdgeWidget(String e) {
        //ToReview: If in the future add edges in the device layout review the render and export/import classes
        return null;
    }

    @Override
    public Lookup fixLookup() {
        PaletteController pallete = SharedContent.getInstance().getAbstractLookup().lookup(PaletteController.class);
        SharedContent.getInstance().getInstanceContent().set(Collections.singleton(pallete), null);                
        return SharedContent.getInstance().getAbstractLookup();
    }
    
}
