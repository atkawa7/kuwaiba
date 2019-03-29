/**
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.modules.reporting.img.rackview;

import com.vaadin.ui.Notification;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObject;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteViewObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;
/**
 * Class used to render a model type portWidget in any scene
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeviceLayoutRenderer {
    /**
     * List of classes whose instances are enable to have a default device layout
     */
    private static final String[] CLASSES_WITH_DEFAULT_DEVICE_LAYOUT = new String [] { "GenericDistributionFrame", "GenericBoard", "GenericCommunicationsElement", "Slot", "CableManager" };
    /**
     * List of classes of ports that can be shown in the layout
     */
    private static final String[] PORTS_ENABLED = new String[] { "ElectricalPort", "OpticalPort" };
    
    private static final List<String> NO_VISIBLE_DEVICES = Arrays.asList(new String [] { "PowerBoard", "VirtualPort", "ServiceInstance", "PowerPort", "Transceiver" }); //NOI18N
    
    private String errorMessage;
    /**
     * The widget that must contain the device layout
     */
    private final Widget parentWidget;
    /**
     * The device layout widget preferred bounds in the parent widget
     */
    private final Rectangle deviceLayoutBounds;
    /**
     * The device layout widget preferred location in the parent widget
     */
    private final Point deviceLayoutLocation;
    /**
     * A widget that represent the device layout in the scene
     */
    private Widget deviceLayoutWidget;
    /**
     * Defines if the device to render has a default layout
     */
    private boolean hasDefaultDeviceLayout = false;
    
    private final RemoteObjectLight deviceToRender;
    /**
     * The value of the attribute model for the device to render
     */
    private RemoteObjectLight deviceModelValue;
    /**
     * The related view to the model for the device to render
     */
    private RemoteViewObject deviceLayoutObjView;
    /**
     * List of shapes obtained from the xml structure
     */
    private final List<Shape> shapes = new ArrayList();
    /**
     * Defines if the device layout widget has the size defined in the device layout editor
     */
    private boolean originalSize = false;
    /**
     * Hierarchy of the device to render
     */
    private HashMap<RemoteObjectLight, List<RemoteObjectLight>> nodes;
    /**
     * Repository to storage the custom shapes layout structures
     */
    private HashMap<RemoteObjectLight, RemoteViewObject> structureRepository;
        
    public DeviceLayoutRenderer(RemoteObjectLight deviceToRender, Widget parentWidget, Point deviceLayoutLocation, Rectangle deviceLayoutBounds, 
        HashMap<RemoteObjectLight, List<RemoteObjectLight>> nodes, 
        HashMap<RemoteObjectLight, RemoteViewObject> structureRepository) {
        
        this.deviceToRender = deviceToRender;
        this.parentWidget = parentWidget;
        this.deviceLayoutBounds = deviceLayoutBounds;
        this.deviceLayoutLocation = deviceLayoutLocation;
        if (nodes == null && structureRepository == null) {
            DeviceLayoutStructure deviceLayoutStructrue = new DeviceLayoutStructure(deviceToRender);
            
            this.nodes = deviceLayoutStructrue.getHierarchy();
            this.structureRepository = deviceLayoutStructrue.getLayouts();
        } else {
            this.nodes = nodes;
            this.structureRepository = structureRepository;
        }
        errorMessage = null;
        
        initializeRenderDeviceLayout();
    }
    
    public void setOriginalSize(boolean originalSize) {
        this.originalSize = originalSize;
    }
    
    public boolean hasDeviceLayout() {
        return deviceLayoutObjView != null;
    }
    
    public boolean hasDefaultDeviceLayout() {
        return hasDefaultDeviceLayout;
    }
    
    public RemoteViewObject getDeviceLayoutObjectView() {
        return deviceLayoutObjView;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
        
    private RemoteObjectLight getDeviceModelValue() {
        if (deviceToRender == null)
            return null;
        
        hasDefaultDeviceLayout = hasDefaultDeviceLayout(deviceToRender);
        try {
            PersistenceService.getInstance().getMetadataEntityManager().getClass("CustomShape"); //NOI18N
        } catch (MetadataObjectNotFoundException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            Exceptions.printStackTrace(ex);
            return null;
        }
        boolean hasDeviceLayout = false;
        
        try {
            hasDeviceLayout = RackViewImage.classMayHaveDeviceLayout(deviceToRender.getClassName());
        } catch (Exception ex) {
            errorMessage = ex.getMessage();
        }
        if (hasDeviceLayout) {
            RemoteObject model = RackViewImage.getListTypeItemAttributeValue(deviceToRender.getClassName(), deviceToRender.getId(), "model"); //NOI18N

            if (model == null)
                errorMessage = String.format("Attribute \"model\" not set in object %s", deviceToRender);
            return model;
        } else 
            errorMessage = String.format("Attribute \"model\" not set in object %s", deviceToRender);
        
        return null;
    }
    
    private void initializeRenderDeviceLayout() {
        deviceModelValue = getDeviceModelValue();
        
        if (deviceModelValue == null)
            return;
        
        if (structureRepository != null) {
            for (RemoteObjectLight key : structureRepository.keySet()) {
                
                if (key.getId() != null && deviceModelValue.getId() != null && key.getId().equals(deviceModelValue.getId()))
                    deviceLayoutObjView = structureRepository.get(key);
            }
        }
    }
    
    private void renderDefaultDeviceLayout(RemoteObjectLight device, Widget parentWidget, Widget parentChildren) {
        if (!hasDefaultDeviceLayout(device))
            return;
        
        Widget deviceWidget;
        Widget children;
        if (!NO_VISIBLE_DEVICES.contains(device.getClassName())) {
            deviceWidget = parentWidget.getScene() instanceof GraphScene ? 
                ((GraphScene) parentWidget.getScene()).addNode(device) : 
                new Widget(parentWidget.getScene());

            deviceWidget.setLayout(LayoutFactory.createVerticalFlowLayout());

            LabelWidget lblName = new LabelWidget(parentWidget.getScene());
            lblName.setBorder(BorderFactory.createEmptyBorder(0, 5 ,0 , 5));
            lblName.setLabel(device.getName());
            lblName.setForeground(Color.WHITE);

            children = new Widget(parentWidget.getScene());
            children.setBorder(BorderFactory.createEmptyBorder(5, 5 ,5 , 5));
            children.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 5));

            deviceWidget.addChild(lblName);
            deviceWidget.addChild(children);        
            
            RemoteClassMetadata deviceClass = null;
            try {
                // Gets the class color to set the device widget background
                deviceClass = RackViewImage.getInstance().getWebserviceBean().getClass(
                        device.getClassName(), 
                        RackViewImage.getInstance().getIpAddress(), 
                        RackViewImage.getInstance().getRemoteSession().getSessionId());
            } catch (ServerSideException ex) {
                Exceptions.printStackTrace(ex);
                
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
            
            if (deviceClass == null) {
                deviceWidget.setBackground(Color.BLACK);
            }
            else
                deviceWidget.setBackground(new Color(deviceClass.getColor()));
            
            deviceWidget.setOpaque(true);
            deviceWidget.setToolTipText(device.getName());

            deviceWidget.revalidate();
            deviceWidget.getScene().validate();
            deviceWidget.getScene().repaint();
            // Sets the children of the device
            if (parentWidget.getChildren().size() >= 2 && device != deviceToRender)
                parentWidget.getChildren().get(1).addChild(deviceWidget);
            else
                parentWidget.addChild(deviceWidget);

            parentWidget.getScene().validate();
            parentWidget.getScene().repaint();
        } else {
            deviceWidget = parentWidget;
            children = parentChildren;
        }
        List<RemoteObjectLight> ports = new ArrayList();
        findPortsEnabled(device, ports);     
        // If the device has ports then render
        if (!ports.isEmpty()) {
            boolean addRow = false;
            // If has more than six ports, then create two rows
            if (ports.size() > 6)
                addRow = true;

            int numCols = ports.size();
            int numRows = 1;

            if (addRow) {
                numCols += - (int) Math.round(ports.size() / 2);
                numRows = 2;
            }                
            int span = 8;
            int portWidth = (int) Math.round(deviceLayoutBounds.width / (numCols == 0 ? numCols = 1 : numCols)) - span;
            int portHeight = (int) Math.round(deviceLayoutBounds.height / numRows) - span;

            if (portWidth < portHeight)
                portHeight = portWidth;
            else
                portWidth = portHeight;

            if (portWidth > 25) {
                portWidth = 25;
                portHeight = 25;
            }
            // The ports widget is used to contain the set of port widget
            Widget portsWidget = new Widget(parentWidget.getScene());
            
            for (int i = 0; i < numRows; i += 1) {
                int y = 4 + (portHeight + span) * i;

                for (int j = 0; j < numCols; j += 1) {
                    int x = 4 + (portWidth + span) * j;

                    int idx = i * numCols + j;

                    if (idx < ports.size()) {
                        RemoteObjectLight port = ports.get(idx);
                        
                        if (((GraphScene) deviceWidget.getScene()).findWidget(port) != null)
                            continue;
                        
                        Widget portWidget = ((GraphScene) deviceWidget.getScene()).addNode(port);

                        deviceWidget.getScene().validate();
                        deviceWidget.getScene().repaint();

                        portWidget.setPreferredLocation(new Point(x, y));
                        portWidget.setPreferredBounds(new Rectangle(0, 0, portWidth, portHeight));
                        // Gets the class color to set the port widget background
                        RemoteClassMetadata portClass = null;
                        try {
                            portClass = RackViewImage.getInstance().getWebserviceBean().getClass(
                                    port.getClassName(),
                                    RackViewImage.getInstance().getIpAddress(),
                                    RackViewImage.getInstance().getRemoteSession().getSessionId());
                        } catch (ServerSideException ex) {
                            Exceptions.printStackTrace(ex);
                            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                        }
                        //CommunicationsStub.getInstance().getMetaForClass(port.getClassName(), false);
                        if (portClass == null) {
                            portWidget.setBackground(Color.BLACK);
                        }
                        else
                            portWidget.setBackground(new Color(portClass.getColor()));
                        
                        portWidget.setOpaque(true);
                        portWidget.setToolTipText(port.getName());
                        
                        portWidget.revalidate();
                        portsWidget.addChild(portWidget);
                    } else
                        break;
                }
            }
            children.addChild(portsWidget);
        }    
        List<RemoteObjectLight> lst = null;//nodes.get(device);
        
        for (RemoteObjectLight aNode : nodes.keySet()) {
            
            if (aNode.getId() != null && device.getId() != null && aNode.getId().equals(device.getId()))
                lst = nodes.get(aNode);
        }
        
        if (lst != null) {
            for (RemoteObjectLight child : lst) {
                // Used to no make new calls to the ports added in the previous step
                if (((GraphScene) deviceWidget.getScene()).findWidget(child) == null)
                    renderDefaultDeviceLayout(child, deviceWidget, children);
            }
        } else {
            int i = 0;
        }
    }
        
    public void render() {
        if (deviceLayoutObjView == null) {
            if (hasDefaultDeviceLayout) {
                renderDefaultDeviceLayout(deviceToRender, parentWidget, null);
                
                deviceLayoutWidget = ((GraphScene) parentWidget.getScene()).findWidget(deviceToRender);
                deviceLayoutWidget.setPreferredLocation(new Point(deviceLayoutLocation));
                deviceLayoutWidget.setPreferredBounds(new Rectangle(deviceLayoutBounds));
            }
            return;
        }
        byte[] structure = deviceLayoutObjView.getStructure();
        
        if (structure == null)
            return;
        // Adding the device to render in the scene
        deviceLayoutWidget = parentWidget.getScene() instanceof GraphScene ? 
            ((GraphScene) parentWidget.getScene()).addNode(deviceToRender) : 
            new Widget(parentWidget.getScene());
        
        deviceLayoutWidget.setPreferredLocation(new Point(deviceLayoutLocation));
        deviceLayoutWidget.setPreferredBounds(new Rectangle(deviceLayoutBounds));
        deviceLayoutWidget.setOpaque(false);
        deviceLayoutWidget.revalidate();
        parentWidget.addChild(deviceLayoutWidget);
        // Gets the set of shapes
        render(structure, originalSize, deviceLayoutLocation, deviceLayoutBounds);
        // In recursive calls to render nested devices layouts is necessary update the current hierarchy
        HashMap<RemoteObjectLight, List<RemoteObjectLight>> subHierarchy = new HashMap();
        getSubHierarchy(nodes, deviceToRender, subHierarchy);
        nodes = subHierarchy;
        // Comparing the names of shapes and object to render the layout
        addNodes();
    }
    
    /**
     * Fixes the set of shapes
     */
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
                            String id = reader.getAttributeValue(null, "id");
                            
                            for (RemoteObjectLight listItem : structureRepository.keySet()) {
                                if (listItem.getId() != null && id != null && listItem.getId().equals(id) && listItem instanceof RemoteObject) {
                                    shape = ShapeFactory.getInstance().getCustomShape((RemoteObject) listItem);
                                    break;
                                }
                            }
                            
                            if (shape == null) {
                                
                                RemoteObject lol = null;
                                try {
                                    lol = RackViewImage.getInstance().getWebserviceBean().getObject(
                                        "CustomShape",
                                        id,
                                        RackViewImage.getInstance().getIpAddress(),
                                        RackViewImage.getInstance().getRemoteSession().getSessionId());
                                    
                                } catch (ServerSideException ex) {
                                    Exceptions.printStackTrace(ex);
                                    Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                                }
                                
                                if (lol == null) {
                                }
                                else {
                                    RemoteObject listItem = null;
                                    try {
                                        listItem = RackViewImage.getInstance().getWebserviceBean().getObject(
                                                lol.getClassName(),
                                                lol.getId(),
                                                RackViewImage.getInstance().getIpAddress(),
                                                RackViewImage.getInstance().getRemoteSession().getSessionId());
                                        
                                        shape = ShapeFactory.getInstance().getCustomShape(listItem);
                                        
                                    } catch (ServerSideException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
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
                                RemoteObject customShapeModel = ((CustomShape) shape).getListItem();
                                
                                RemoteViewObject layoutView = null;
                                
                                try {
                                    RemoteViewObjectLight[] views = RackViewImage.getInstance().getWebserviceBean().getListTypeItemRelatedViews(
                                            customShapeModel.getId(),
                                            customShapeModel.getClassName(),
                                            -1,
                                            RackViewImage.getInstance().getIpAddress(),
                                            RackViewImage.getInstance().getRemoteSession().getSessionId());
                                    
                                    if (views != null && views.length > 0) {
                                        layoutView = RackViewImage.getInstance().getWebserviceBean().getListTypeItemRelatedView(
                                            customShapeModel.getId(), 
                                            customShapeModel.getClassName(), 
                                            views[0].getId(),
                                            RackViewImage.getInstance().getIpAddress(), 
                                            RackViewImage.getInstance().getRemoteSession().getSessionId());
                                    }
                                    
                                } catch (ServerSideException ex) {
                                    Exceptions.printStackTrace(ex);                                    
                                }
                                                                
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
        } catch (XMLStreamException ex) {
            Notification.show("The view seems corrupted and could not be loaded", Notification.Type.ERROR_MESSAGE);
        }
    }
    
    /**
     * Adds the nodes that has a shape in the scene
     */
    private void addNodes() {
        GraphScene scene = (GraphScene) parentWidget.getScene();
        
        for (Shape shape : shapes) {
            
            if (!shape.getName().equals("")) {
                List<RemoteObjectLight> nodesToShape = compareObjectNameAndShapeName(shape);

                if (nodesToShape.isEmpty()) {
                    Widget widget = new Widget(deviceLayoutWidget.getScene());
                    ShapeWidgetUtil.shapeToWidget(shape, widget, true);                    
                    
                    widget.setBackground(Color.LIGHT_GRAY);
                    widget.setOpaque(true);
                    deviceLayoutWidget.addChild(widget);
                    scene.validate();
                    scene.repaint();
                    continue;
                }
                RemoteObjectLight node = nodesToShape.get(0);

                if (scene.findWidget(node) == null) {
                    Widget widget = scene.addNode(node);
                    widget.getScene().validate();
                    widget.getScene().repaint();
                    boolean isSubclassOf = false;
                    
                    try {
                        isSubclassOf = RackViewImage.getInstance().getWebserviceBean().isSubclassOf(
                            node.getClassName(),
                            "GenericPort",
                            RackViewImage.getInstance().getIpAddress(),
                            RackViewImage.getInstance().getRemoteSession().getSessionId());
                                
                    } catch (ServerSideException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    
                    if (isSubclassOf)
                        widget.setToolTipText(node.toString());

                    ShapeWidgetUtil.shapeToWidget(shape, widget, true);
                    deviceLayoutWidget.addChild(widget);
                    scene.validate();
                    scene.repaint();
                    
                    if (shape instanceof RectangleShape) {
                        if (((RectangleShape) shape).isSlot()) {
                            renderSlot(node, widget);
                            scene.validate();
                            scene.repaint();
                        }
                    }
                } else {
                    if (node.equals(deviceToRender)) {
                        Widget widget = new Widget(deviceLayoutWidget.getScene());
                        widget.setOpaque(false);
                        ShapeWidgetUtil.shapeToWidget(shape, widget, true);
                        deviceLayoutWidget.addChild(widget);
                        scene.validate();
                        scene.repaint();
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
                    
                    widget.setPreferredLocation(new Point(shape.getX() - Shape.DEFAULT_BORDER_SIZE, shape.getY() - Shape.DEFAULT_BORDER_SIZE));
                    widget.setBackground(shape.getColor());
                    widget.setOpaque(shape.isOpaque());
                    if (widget.isOpaque())
                        widget.setBorder(BorderFactory.createLineBorder(0, shape.getBorderColor()));
                    widget.setPreferredSize(new Dimension(shape.getWidth() - Shape.DEFAULT_BORDER_SIZE, shape.getHeight() - Shape.DEFAULT_BORDER_SIZE));
                                                            
                    ((LabelShape) shape).setFontSize((int) (((LabelShape) shape).getFontSize() * Math.abs(shape.getHeight() - 0.30)));
                    Font font = new Font(null, 0, ((LabelShape) shape).getFontSize());
                    ((ResizableLabelWidget) widget).setFont(font);
                    ((ResizableLabelWidget) widget).setLabel(((LabelShape) shape).getLabel());
                    ((ResizableLabelWidget) widget).setForeground(((LabelShape) shape).getTextColor());
                    scene.validate();
                    scene.repaint();
                                        
                } else if (CircleShape.SHAPE_TYPE.equals(type)) {
                    widget = new CircleShapeWidget(parentWidget.getScene(), (CircleShape) shape);                    
                    
                    widget.setPreferredLocation(new Point(shape.getX() - Shape.DEFAULT_BORDER_SIZE, shape.getY() - Shape.DEFAULT_BORDER_SIZE));
                    widget.setBackground(shape.getColor());
                    widget.setOpaque(shape.isOpaque());
                    if (widget.isOpaque())
                        widget.setBorder(BorderFactory.createLineBorder(0, shape.getBorderColor()));
                    widget.setPreferredSize(new Dimension(shape.getWidth() - Shape.DEFAULT_BORDER_SIZE, shape.getHeight() - Shape.DEFAULT_BORDER_SIZE));
                    scene.validate();
                    scene.repaint();
                    
                } else if (PolygonShape.SHAPE_TYPE.equals(type)) {
                    widget = new PolygonShapeWidget(parentWidget.getScene(), (PolygonShape) shape);
                    
                    widget.setPreferredLocation(new Point(shape.getX() - Shape.DEFAULT_BORDER_SIZE, shape.getY() - Shape.DEFAULT_BORDER_SIZE));
                    widget.setBackground(shape.getColor());
                    widget.setOpaque(shape.isOpaque());
                    if (widget.isOpaque())
                        widget.setBorder(BorderFactory.createLineBorder(0, shape.getBorderColor()));
                    
                    widget.setPreferredSize(new Dimension(shape.getWidth() - Shape.DEFAULT_BORDER_SIZE, shape.getHeight() - Shape.DEFAULT_BORDER_SIZE));
                    scene.validate();
                    scene.repaint();
                }
                if (widget == null)
                    continue;
                deviceLayoutWidget.addChild(widget);
                widget.revalidate();
                widget.repaint();
                
                scene.validate();
                scene.repaint();
            }
        }
    }
    
    public void renderSlot(RemoteObjectLight slotObj, Widget widget) {
        List<RemoteObjectLight> children = null;
        
        for (RemoteObjectLight key : nodes.keySet()) {
            
            if (key.getId() != null && slotObj.getId() != null && key.getId().equals(slotObj.getId())) {
                children = nodes.get(key);
                break;
            }
        }
        
        if (children == null)
            return;
        
        for (RemoteObjectLight child : children) {
            DeviceLayoutRenderer render = new DeviceLayoutRenderer(child, widget, new Point(0, 0), widget.getPreferredBounds(), nodes, structureRepository);
            render.render();
        }
    }
    
    private void getSubHierarchy(
        HashMap<RemoteObjectLight, List<RemoteObjectLight>> hierarchy, 
        RemoteObjectLight object, 
        HashMap<RemoteObjectLight, List<RemoteObjectLight>> subHierarchy) {
        
        for (RemoteObjectLight key : hierarchy.keySet()) {
            
            if (object.getId() != null && key.getId() != null && object.getId().equals(key.getId())) {
                
                List<RemoteObjectLight> children = hierarchy.get(key);

                subHierarchy.put(object, children);

                for (RemoteObjectLight child : children)
                    getSubHierarchy(hierarchy, child, subHierarchy);
                
                return;
            }
        }
        subHierarchy.put(object, new ArrayList());
    }
    
    /**
     * Gets the list of nodes that match with the shape name
     * @param shape The shape to compare
     * @return list of nodes that match with the shape name
     */
    private List<RemoteObjectLight> compareObjectNameAndShapeName(Shape shape) {
        List<RemoteObjectLight> result = new ArrayList();
        
        for(RemoteObjectLight node : nodes.keySet()) {
           // The shape name can be a regular expression. The procedure below are 
           // used to verify if the node name match with the shape name
            String shapeName = shape.getName();
            String nodeName = node.getName();

            Pattern pattern = Pattern.compile(shapeName);
            Matcher matcher = pattern.matcher(nodeName);
        
            if (matcher.find()) {
                if (nodeName.equals(matcher.group()))
                    result.add(node);
            }
        }
        return result;
    }
    
    /**
     * Gets a widget that represent the device layout in the scene
     * @return a widget that represent the device layout in the scene
     */
    public Widget getDeviceLayoutWidget() {
        return deviceLayoutWidget;
    }
    
    private boolean hasDefaultDeviceLayout(RemoteObjectLight device) {
        for (String classes : CLASSES_WITH_DEFAULT_DEVICE_LAYOUT) {
            try {
                boolean isSubclassOf = RackViewImage.getInstance().getWebserviceBean().isSubclassOf(
                        device.getClassName(),
                        classes,
                        RackViewImage.getInstance().getIpAddress(),
                        RackViewImage.getInstance().getRemoteSession().getSessionId());
                
                if (isSubclassOf)
                    return true;
                
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                Exceptions.printStackTrace(ex);
                return false;
            }
        }
        return false;
    }
    
    private boolean isPortEnabled(RemoteObjectLight device) {
        for (String portClass : PORTS_ENABLED) {
            try {
                boolean isSubclassOf = RackViewImage.getInstance().getWebserviceBean().isSubclassOf(
                        device.getClassName(),
                        portClass,
                        RackViewImage.getInstance().getIpAddress(),
                        RackViewImage.getInstance().getRemoteSession().getSessionId());
                                
                if (isSubclassOf)
                    return true;
                                
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                Exceptions.printStackTrace(ex);
                return false;
            }
        }
        return false;                
    }
    
    private void findPortsEnabled(RemoteObjectLight device, List<RemoteObjectLight> result) {
        
        List<RemoteObjectLight> lst = null;
        
        for (RemoteObjectLight aNode : nodes.keySet()) {
                        
            if (aNode.getId() != null && device.getId() != null && aNode.getId().equals(device.getId()))
                lst = nodes.get(aNode);                        
        }
                
        if (lst != null) {
            for (RemoteObjectLight child :  lst) {

                if (isPortEnabled(child))
                    result.add(child);                
            }
        }
    }
}

