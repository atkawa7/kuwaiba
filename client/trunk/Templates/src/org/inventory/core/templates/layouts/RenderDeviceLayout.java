/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package org.inventory.core.templates.layouts;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.templates.layouts.model.CircleShape;
import org.inventory.core.templates.layouts.model.ContainerShape;
import org.inventory.core.templates.layouts.model.CustomShape;
import org.inventory.core.templates.layouts.model.LabelShape;
import org.inventory.core.templates.layouts.model.PolygonShape;
import org.inventory.core.templates.layouts.model.RectangleShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.model.ShapeFactory;
import org.inventory.core.templates.layouts.widgets.CircleShapeWidget;
import org.inventory.core.templates.layouts.widgets.PolygonShapeWidget;
import org.inventory.core.templates.layouts.widgets.ResizableLabelWidget;
import org.inventory.core.templates.layouts.widgets.ShapeWidgetUtil;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * Class used to render a model type portWidget in any scene
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RenderDeviceLayout {
    /**
     * List of classes that has a default device layout
     */
    private static final String[] classesWithDefaultDeviceLayout = new String [] {"GenericDistributionFrame", "GenericBoard", "GenericCommunicationsElement", "Slot"};
    /**
     * List of classes of ports that can be shown in the layout
     */
    private static final String[] portsEnabled = new String[] {"ElectricalPort", "OpticalPort"};
    
    private String errorMessage;
    private final Widget parentWidget;
    private final Rectangle bounds;
    private final Point location;
    /**
     * A portWidget that represent the device layout in the scene
     */
    private Widget deviceLayoutWidget;
    
    private boolean hasDefaultDeviceLayout = false;
    
    private final LocalObjectLight deviceToRender;
    private LocalObjectListItem deviceModel;
    private LocalObjectView deviceLayoutObj;
    /**
     * List of shapes obtained from the xml structure
     */
    private final List<Shape> shapes = new ArrayList();
    
    private boolean originalSize = false;
    /**
     * Hierarchy of the device to render
     */
    private final HashMap<LocalObjectLight, List<LocalObjectLight>> nodes = new HashMap();
        
    public RenderDeviceLayout(LocalObjectLight deviceToRender, Widget parentWidget, Point location, Rectangle bounds) {
        this.deviceToRender = deviceToRender;
        this.parentWidget = parentWidget;
        this.bounds = bounds;
        
        this.location = location;
        errorMessage = null;
        initializeRenderDeviceLayout();
    }
    
    public void setOriginalSize(boolean originalSize) {
        this.originalSize = originalSize;
    }
    
    public boolean hasDeviceLayout() {
        return deviceLayoutObj != null;
    }
    
    public boolean hasDefaultDeviceLayout() {
        return hasDefaultDeviceLayout;
    }
    
    public LocalObjectView getEquipmentModelView() {
        return deviceLayoutObj;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
        
    private LocalObjectListItem getEquipmentModel() {
        if (deviceToRender == null)
            return null;
        
        hasDefaultDeviceLayout = hasDefaultDeviceLayout(deviceToRender);
        
        if(CommunicationsStub.getInstance().getMetaForClass(Constants.CLASS_CUSTOMSHAPE, true) == null) {
            JOptionPane.showMessageDialog(null, 
                "This database seems outdated. Contact your administrator to apply the necessary patches to add the CustomShape class", 
                I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
            return null;            
        }
        LocalClassMetadata lcm = CommunicationsStub.getInstance().getMetaForClass(deviceToRender.getClassName(), false);
        if (lcm == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            errorMessage = String.format("The object %s does not have an attribute \"model\" or is not set", deviceToRender);
            return null;
        }
        if (lcm.hasAttribute("model")) {
            LocalObject localObj = CommunicationsStub.getInstance().getObjectInfo(deviceToRender.getClassName(), deviceToRender.getOid());
            if (localObj == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                errorMessage = String.format("The object %s does not have an attribute \"model\" or is not set", deviceToRender);
                return null;
            }
            Object model = localObj.getAttribute("model");
            if (model instanceof LocalObjectListItem)
                return (LocalObjectListItem) model;
        }          
        errorMessage = String.format("The object %s does not have an attribute \"model\" or is not set", deviceToRender);
        return null;
    }
    
    private void initializeRenderDeviceLayout() {
        deviceModel = getEquipmentModel();
        
        if (deviceModel == null)
            return;

        deviceLayoutObj = null;
        List<LocalObjectViewLight> relatedViews = CommunicationsStub.getInstance().getListTypeItemRelatedViews(deviceModel.getId(), deviceModel.getClassName());
        if (relatedViews == null) {
            this.errorMessage = CommunicationsStub.getInstance().getError();
            return;
        }
        if (!relatedViews.isEmpty()) {
            deviceLayoutObj = CommunicationsStub.getInstance().getListTypeItemRelatedView(deviceModel.getId(), deviceModel.getClassName(), relatedViews.get(0).getId());
            if (deviceLayoutObj == null) {
                this.errorMessage = CommunicationsStub.getInstance().getError();
            }
        } else {
            this.errorMessage = String.format("The EquipmentModel %s does not have a layout associated to it", deviceModel);
        }
        
        
    }
    
    private boolean hasDefaultDeviceLayout(LocalObjectLight device) {
        for (String classes : classesWithDefaultDeviceLayout) {
            if (CommunicationsStub.getInstance().isSubclassOf(device.getClassName(), classes))
                return true;
        }
        return false;
    }
    
    private boolean isPortEnable(LocalObjectLight device) {
        for (String portClass : portsEnabled) {
            if (CommunicationsStub.getInstance().isSubclassOf(device.getClassName(), portClass))
                return true;
        }
        return false;                
    }
    
    private void findPortsEnabled(LocalObjectLight device, List<LocalObjectLight> result) {
        if (isPortEnable(device))
            result.add(device);
        
        for (LocalObjectLight child : nodes.get(device)) {
            
            if (!hasDefaultDeviceLayout(child))
                findPortsEnabled(child, result);
        }
    }
    
    private void renderDefaultDeviceLayout(LocalObjectLight device, Widget parentWidget) {
        if (!hasDefaultDeviceLayout(device))
            return;
        
        Widget deviceWidget = parentWidget.getScene() instanceof AbstractScene ? 
            ((AbstractScene) parentWidget.getScene()).addNode(device) : 
            new Widget(parentWidget.getScene());
        deviceWidget.getScene().validate();
        deviceWidget.getScene().paint();
        
        if (device != deviceToRender)
            parentWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 2));
        
        LocalClassMetadata deviceClass = CommunicationsStub.getInstance().getMetaForClass(device.getClassName(), false);
        if (deviceClass == null) {
            deviceWidget.setBackground(Color.BLACK);
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        } else
            deviceWidget.setBackground(deviceClass.getColor() == null ? Color.BLACK : deviceClass.getColor());
        
        deviceWidget.setOpaque(true);
        deviceWidget.setToolTipText(device.getName());
        deviceWidget.revalidate();
                
        parentWidget.addChild(deviceWidget);
            parentWidget.getScene().validate();
            parentWidget.getScene().repaint();
        
        initNodes(device, getObjectChildren(device));
        
        List<LocalObjectLight> ports = new ArrayList();
        
        findPortsEnabled(device, ports);     
        
        boolean addRow = false;
        if (ports.size() > 6)
            addRow = true;
        
        int numCols = ports.size();
        int numRows = 1;
        
        if (addRow) {
            numCols += - (int) Math.round(ports.size() / 2);
            numRows = 2;
        }                
        int span = 8;
        int portWidth = (int) Math.round(bounds.width / (numCols == 0 ? numCols = 1 : numCols)) - span;
        int portHeight = (int) Math.round(bounds.height / numRows) - span;
        
        if (portWidth < portHeight)
            portHeight = portWidth;
        else
            portWidth = portHeight;
        
        if (portWidth > 25) {
            portWidth = 25;
            portHeight = 25;
        }
        
        for (int i = 0; i < numRows; i += 1) {
            int y = 4 + (portHeight + span) * i;
            
            for (int j = 0; j < numCols; j += 1) {
                int x = 4 + (portWidth + span) * j;
                
                int idx = i * numCols + j;
                
                if (idx < ports.size()) {
                    LocalObjectLight port = ports.get(idx);
                    
                    Widget portWidget = ((AbstractScene) deviceWidget.getScene()).addNode(port);

                    deviceWidget.getScene().validate();
                    deviceWidget.getScene().paint();

                    portWidget.setPreferredLocation(new Point(x, y));
                    portWidget.setPreferredBounds(new Rectangle(0, 0, portWidth, portHeight));
                                        
                    LocalClassMetadata portClass = CommunicationsStub.getInstance().getMetaForClass(port.getClassName(), false);
                    if (portClass == null) {
                        portWidget.setBackground(Color.BLACK);
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                            NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    } else
                        portWidget.setBackground(portClass.getColor() == null ? Color.BLACK : portClass.getColor());
                    portWidget.setOpaque(true);
                    deviceWidget.setToolTipText(port.getName());
                    
                    portWidget.revalidate();
                    deviceWidget.addChild(portWidget);
                } else
                    break;
            }
        }        
        for (LocalObjectLight child : nodes.get(device))
            renderDefaultDeviceLayout(child, deviceWidget);
    }
    
    public void render() {
        if (deviceLayoutObj == null) {
            if (hasDefaultDeviceLayout) {
                renderDefaultDeviceLayout(deviceToRender, parentWidget);
                
                deviceLayoutWidget = ((AbstractScene) parentWidget.getScene()).findWidget(deviceToRender);
                deviceLayoutWidget.setPreferredLocation(new Point(location));
                deviceLayoutWidget.setPreferredBounds(new Rectangle(bounds));
            }                
            return;
        }
        
        byte[] structure = deviceLayoutObj.getStructure();
        
        if (structure == null)
            return;
                
        deviceLayoutWidget = parentWidget.getScene() instanceof AbstractScene ? 
            ((AbstractScene) parentWidget.getScene()).addNode(deviceToRender) : 
            new Widget(parentWidget.getScene());
        
        deviceLayoutWidget.setPreferredLocation(new Point(location));
        deviceLayoutWidget.setPreferredBounds(new Rectangle(bounds));
        deviceLayoutWidget.setOpaque(false);
        deviceLayoutWidget.revalidate();
        parentWidget.addChild(deviceLayoutWidget);
        
        boolean isAbstractScene = false;
        
        if (parentWidget.getScene() instanceof AbstractScene)
            isAbstractScene = true;
        
        if (isAbstractScene)
            initNodes(deviceToRender, getObjectChildren(deviceToRender));
        
        render(structure, originalSize, location, bounds);
        
        if (isAbstractScene)
            addNodes();
    }
    
    private void render(byte[] structure, boolean originalSize, Point renderPoint, Rectangle renderBounds) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);
                        
            QName tagLayout = new QName("layout"); //NOI18N
            QName tagShape = new QName("shape"); //NOI18N
            String attrValue;
            
            Rectangle layoutBounds = null;
            
            double percentWidth = 1;
            double percentHeight = 1;
                                    
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getName().equals(tagLayout)) {
                        int x = renderPoint.x;
                        int y = renderPoint.y;
                                                
                        attrValue = reader.getAttributeValue(null, "width"); //NOI18N
                        int width = 0;
                        if (attrValue != null)
                            width = Integer.valueOf(attrValue); 
                        
                        int height = 0;
                        attrValue = reader.getAttributeValue(null, "height"); //NOI18N
                        if (attrValue != null)
                            height = Integer.valueOf(attrValue);
                        
                        layoutBounds = new Rectangle(x, y, width, height);
                        
                        if (!originalSize) {
                            percentWidth = renderBounds.getWidth() / layoutBounds.getWidth();
                            percentHeight = renderBounds.getHeight() / layoutBounds.getHeight();
                        }
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
                            shape.setBorderWidth(-Shape.DEFAULT_BORDER_SIZE);
                            
                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_X);
                            if (attrValue != null)
                                shape.setX((int) Math.round(Integer.valueOf(attrValue) * percentWidth) + (layoutBounds == null ? 0 : layoutBounds.x));

                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_Y);
                            if (attrValue != null)
                                shape.setY((int) Math.round(Integer.valueOf(attrValue) * percentHeight) + (layoutBounds == null ? 0 : layoutBounds.y));

                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_WIDTH);
                            if (attrValue != null)
                                shape.setWidth((int) Math.round(Integer.valueOf(attrValue) * percentWidth));

                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_HEIGHT);
                            if (attrValue != null)
                                shape.setHeight((int) Math.round(Integer.valueOf(attrValue) * percentHeight));

                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_OPAQUE);
                            if (attrValue != null)
                                shape.setOpaque(Boolean.valueOf(attrValue));
                            
                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_NAME);
                            if (attrValue != null)
                                shape.setName(attrValue);
                            
                            attrValue = reader.getAttributeValue(null, Shape.PROPERTY_IS_EQUIPMENT);
                            if (attrValue != null)
                                shape.setIsEquipment(Boolean.valueOf(attrValue));

                            if (ContainerShape.SHAPE_TYPE.equals(shapeType)) {
                            } else if (CustomShape.SHAPE_TYPE.equals(shapeType)) {
                                LocalObjectListItem customShapeModel = ((CustomShape) shape).getListItem();

                                List<LocalObjectViewLight> relatedViews = CommunicationsStub.getInstance().getListTypeItemRelatedViews(customShapeModel.getId(), customShapeModel.getClassName());
                                if (relatedViews != null) {
                                    if (!relatedViews.isEmpty()) {
                                        LocalObjectView layoutView = CommunicationsStub.getInstance().getListTypeItemRelatedView(customShapeModel.getId(), customShapeModel.getClassName(), relatedViews.get(0).getId());
                                        if (layoutView != null) {
                                            byte [] customShapeStructure = layoutView.getStructure();
                                            if (customShapeStructure != null) {
                                                render(customShapeStructure, false, 
                                                    new Point(shape.getX(), shape.getY()), 
                                                    new Rectangle(-Shape.DEFAULT_BORDER_SIZE, -Shape.DEFAULT_BORDER_SIZE, shape.getWidth(), shape.getHeight())
                                                    );
                                            }
                                            shapes.add(shape);
                                        }
                                    }
                                } else {
                                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
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
                                } if (CircleShape.SHAPE_TYPE.equals(shapeType)) {
                                    attrValue = reader.getAttributeValue(null, CircleShape.PROPERTY_ELLIPSE_COLOR);
                                    if (attrValue != null)
                                        ((CircleShape) shape).setEllipseColor(new Color(Integer.valueOf(attrValue)));

                                    attrValue = reader.getAttributeValue(null, CircleShape.PROPERTY_OVAL_COLOR);
                                    if (attrValue != null)
                                        ((CircleShape) shape).setOvalColor(new Color(Integer.valueOf(attrValue)));
                                } if (PolygonShape.SHAPE_TYPE.equals(shapeType)) {
                                    attrValue = reader.getAttributeValue(null, PolygonShape.PROPERTY_INTERIOR_COLOR);
                                    if (attrValue != null)
                                        ((PolygonShape) shape).setInteriorColor(new Color(Integer.valueOf(attrValue)));

                                    attrValue = reader.getAttributeValue(null, PolygonShape.PROPERTY_OUTLINE_COLOR);
                                    if (attrValue != null)
                                        ((PolygonShape) shape).setOutlineColor(new Color(Integer.valueOf(attrValue)));
                                }
                                shapes.add(shape);
                            }
                        }
                    }
                }
            }
            reader.close();
            
            parentWidget.getScene().validate();
            parentWidget.getScene().repaint();
            
        } catch (XMLStreamException ex) {
            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.ERROR_MESSAGE, "The view seems corrupted and could not be loaded");
            
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
            Exceptions.printStackTrace(ex);
        }
        
    }
    
    private void addNodes() {
        AbstractScene scene = (AbstractScene) parentWidget.getScene();
        
        for (Shape shape : shapes) {
            
            if (!shape.getName().equals("")) {
                List<LocalObjectLight> nodesToShape = compareObjectNameAndShapeName(shape);

                if (nodesToShape.isEmpty()) {
                    Widget widget = new Widget(deviceLayoutWidget.getScene());
                    ShapeWidgetUtil.shapeToWidget(shape, widget, true);                    
                    
                    widget.setBackground(Color.LIGHT_GRAY);
                    widget.setOpaque(true);
                    deviceLayoutWidget.addChild(widget);
                    continue;
                }

                LocalObjectLight node = nodesToShape.get(0);

                if (scene.findWidget(node) == null) {
                    Widget widget = scene.addNode(node);
                    widget.getScene().validate();
                    widget.getScene().paint();

                    ShapeWidgetUtil.shapeToWidget(shape, widget, true);
                    deviceLayoutWidget.addChild(widget);
                    
                    if (shape instanceof RectangleShape) {
                        if (((RectangleShape) shape).isSlot()) {
                            renderSlot(node, widget);
                            scene.validate();
                            scene.paint();
                        }
                    }
                } else {
                    if (node.equals(deviceToRender)) {
                        Widget widget = new Widget(deviceLayoutWidget.getScene());
                        widget.setOpaque(false);
                        ShapeWidgetUtil.shapeToWidget(shape, widget, true);
                        deviceLayoutWidget.addChild(widget);
                    }
                }
            } else {
                Widget widget = null;
                
                String type = shape.getShapeType();
                if (RectangleShape.SHAPE_TYPE.equals(type)) {
                    widget = new Widget(deviceLayoutWidget.getScene());
                    ShapeWidgetUtil.shapeToWidget(shape, widget, true);                
                                        
                } else if (LabelShape.SHAPE_TYPE.equals(type)) {                    
                    widget = new ResizableLabelWidget(deviceLayoutWidget.getScene());
                    widget.setPreferredSize(new Dimension(shape.getWidth(), shape.getHeight()));
                    ShapeWidgetUtil.shapeToWidget(shape, widget, true);
                                                            
                    ((LabelShape) shape).setFontSize((int) (((LabelShape) shape).getFontSize() * Math.abs(shape.getHeight() - 0.30)));
                    Font font = new Font(null, 0, ((LabelShape) shape).getFontSize());
                    ((ResizableLabelWidget) widget).setFont(font);
                    ((ResizableLabelWidget) widget).setLabel(((LabelShape) shape).getLabel());
                    ((ResizableLabelWidget) widget).setForeground(((LabelShape) shape).getTextColor());
                    widget.revalidate();
                    
                } else if (CircleShape.SHAPE_TYPE.equals(type)) {
                    widget = new CircleShapeWidget(parentWidget.getScene(), (CircleShape) shape);
                    widget.setPreferredSize(new Dimension(shape.getWidth(), shape.getHeight()));
                    ShapeWidgetUtil.shapeToWidget(shape, widget, true);
                    
                } else if (PolygonShape.SHAPE_TYPE.equals(type)) {
                    widget = new PolygonShapeWidget(parentWidget.getScene(), (PolygonShape) shape);
                    widget.setPreferredSize(new Dimension(shape.getWidth(), shape.getHeight()));
                    ShapeWidgetUtil.shapeToWidget(shape, widget, true);
                }
                deviceLayoutWidget.addChild(widget);
            }
        }
    }
    
    private List<LocalObjectLight> compareObjectNameAndShapeName(Shape shape) {
        List<LocalObjectLight> result = new ArrayList();
        
        for(LocalObjectLight node : nodes.keySet()) {
            String name = node.getName();
            
            if (shape.getName().equals(name))
                result.add(node);
        }
        return result;
    }
    
    public void renderSlot(LocalObjectLight slotObj, Widget widget) {
        List<LocalObjectLight> children = CommunicationsStub.getInstance().getObjectChildren(slotObj.getOid(), slotObj.getClassName());
        if (children == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        for (LocalObjectLight child : children) {
            LocalClassMetadata lcm = CommunicationsStub.getInstance().getMetaForClass(child.getClassName(), false);
            if (lcm == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                continue;
            }
            RenderDeviceLayout render = new RenderDeviceLayout(child, widget, new Point(0, 0), widget.getPreferredBounds());
            
            if (lcm.hasAttribute("model")) {
                LocalObject localObj = CommunicationsStub.getInstance().getObjectInfo(child.getClassName(), child.getOid());
                if (localObj == null) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    continue;
                }
                Object model = localObj.getAttribute("model");
                
                if (model instanceof LocalObjectListItem) {
                    render.render();
                    continue;
                }
            }
            if (render.hasDefaultDeviceLayout()) {
                render.render();
            }          
        }
    }
    
    public LocalObjectListItem getModel(LocalObjectLight device) {
        LocalClassMetadata lcm = CommunicationsStub.getInstance().getMetaForClass(device.getClassName(), false);
        
        if (lcm == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;
        }                

        if (lcm.hasAttribute("model")) {
            LocalObject localObj = CommunicationsStub.getInstance().getObjectInfo(device.getClassName(), device.getOid());
        
            if (localObj == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return null;
            }
            Object model = localObj.getAttribute("model");
            
            if (model instanceof LocalObjectListItem)
                return (LocalObjectListItem) model;
        }
        return null;
    }
    
    private void initNodes(LocalObjectLight node, List<LocalObjectLight> nodeChildren) {
        nodes.put(node, nodeChildren == null ? new ArrayList() : nodeChildren);
        
        if (nodeChildren != null) {
            
            for (LocalObjectLight nodeChild : nodeChildren)
                initNodes(nodeChild, getObjectChildren(nodeChild));                                    
        }
    }
    
    private List<LocalObjectLight> getObjectChildren(LocalObjectLight lol) {
        if (lol == null)
            return null;
        
        List<LocalObjectLight> children = CommunicationsStub.getInstance().getObjectChildren(lol.getOid(), lol.getClassName());
        
        if (children == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;
        } else
            return children;
    }
    
    public Widget getDeviceLayoutWidget() {
        return deviceLayoutWidget;
    }
}
