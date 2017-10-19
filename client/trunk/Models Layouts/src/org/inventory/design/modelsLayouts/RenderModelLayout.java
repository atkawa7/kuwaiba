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
package org.inventory.design.modelsLayouts;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.io.ByteArrayInputStream;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.design.modelsLayouts.model.LabelShape;
import org.inventory.design.modelsLayouts.model.RectangleShape;
import org.inventory.design.modelsLayouts.model.Shape;
import org.inventory.design.modelsLayouts.scene.ModelLayoutScene;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * Class used to render a model type widget in any scene
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RenderModelLayout {
    private String errorMessage;
    private final Widget parentWidget;
    private Rectangle rectangle;
    private double widthPercentage;
    private double heightPercentage;
    
    private Widget modelLayoutWidget;
    
    private final LocalObjectLight objectLight;
    private LocalObjectListItem equipmentModel;
    private LocalObjectView equipmentModelView;
    
    private boolean originalSize = false;
    
    public RenderModelLayout(LocalObjectLight objectLight, Widget parentWidget, int x, int y, int width, int height) {
        this.objectLight = objectLight;
        this.parentWidget = parentWidget;
        rectangle = new Rectangle(x, y, width, height);
        errorMessage = null;
        initializeRenderEquipmentModelLayout();
    }
    
    public void setOriginalSize(boolean originalSize) {
        this.originalSize = originalSize;
    }
    
    public boolean hasEquipmentModelLayout() {
        return equipmentModelView != null;
    }
    
    public LocalObjectView getEquipmentModelView() {
        return equipmentModelView;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    private void initializeRenderEquipmentModelLayout() {
        if (objectLight == null)
            return;
        if (!CommunicationsStub.getInstance().isSubclassOf(objectLight.getClassName(), "GenericCommunicationsElement")) //NOI18N
            return;
                
        LocalObject localObject = CommunicationsStub.getInstance().getObjectInfo(objectLight.getClassName(), objectLight.getOid());
        if (localObject == null) {
            this.errorMessage = CommunicationsStub.getInstance().getError();
            return;
        }
        equipmentModel = null;
        for (Object attrValue : localObject.getAttributes().values()) {
            if (attrValue instanceof LocalObjectListItem) {
                LocalObjectListItem loli = (LocalObjectListItem) attrValue;
                if ("EquipmentModel".equals(loli.getClassName())) { //NOI18N
                    equipmentModel = loli;
                    break;
                }
            }
        }
        if (equipmentModel == null) {
            this.errorMessage = String.format("The object %s no has or not set the attribute \"model\"", localObject);
            return;
        }
        equipmentModelView = null;
        List<LocalObjectViewLight> relatedViews = CommunicationsStub.getInstance().getListTypeItemRelatedViews(equipmentModel.getId(), equipmentModel.getClassName());
        if (relatedViews == null) {
            this.errorMessage = CommunicationsStub.getInstance().getError();
            return;
        }
        if (!relatedViews.isEmpty()) {
            equipmentModelView = CommunicationsStub.getInstance().getListTypeItemRelatedView(equipmentModel.getId(), equipmentModel.getClassName(), relatedViews.get(0).getId());
            if (equipmentModelView == null) {
                this.errorMessage = CommunicationsStub.getInstance().getError();
            }
        } else {
            this.errorMessage = String.format("The EquipmentModel %s no has associate a layout", equipmentModel);
        }
    }
        
    public void render() {
        if (equipmentModelView == null)
            return;
        
        byte[] structure = equipmentModelView.getStructure();
        if (structure == null)
            return;
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        QName tagShape = new QName("shape"); //NOI18N
        
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(structure);
            XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLStreamConstants.START_ELEMENT) {                    
                    if (reader.getName().equals(tagShape)) {
                        Shape model = ModelLayoutScene.XMLtoShape(reader, null);
                        if (model.getWidth() == null || model.getHeight() == null)
                            return;
                        widthPercentage = originalSize ? 1 : Double.valueOf(Integer.toString(rectangle.width)) / Double.valueOf(Integer.toString(model.getWidth()));
                        heightPercentage = originalSize ? 1 : Double.valueOf(Integer.toString(rectangle.height)) / Double.valueOf(Integer.toString(model.getHeight()));
                        
                        List<LocalObjectLight> children = getObjectChildren(objectLight);                                                
                        recursiveRender(true, objectLight, children, reader, tagShape, null, parentWidget, widthPercentage, heightPercentage);
                    }
                }
            }
            reader.close();
            
            parentWidget.getScene().validate();
            parentWidget.getScene().repaint();
            
        } catch (NumberFormatException | XMLStreamException ex) {
            NotificationUtil.getInstance().showSimplePopup("Load View", NotificationUtil.ERROR_MESSAGE, "The view seems corrupted and could not be loaded");
            parentWidget.getScene().removeChildren();
            if (Constants.DEBUG_LEVEL == Constants.DEBUG_LEVEL_FINE)
                Exceptions.printStackTrace(ex);
        }
    }
    
    private void recursiveRender(boolean parentEnable, LocalObjectLight object, List<LocalObjectLight> objChildren, XMLStreamReader reader, QName tagShape, Shape parentShape, Widget parentWidget, double widthPercentage, double heightPercentage) throws XMLStreamException {
        String type = reader.getAttributeValue(null, "type"); //NOI18N
        Shape shape = ModelLayoutScene.XMLtoShape(reader, parentShape);
        
        if (shape == null)
            return;
        
        boolean shapeEnable = parentEnable;
        if (parentEnable) {
            if (shape.isEquipment()) {
                shapeEnable = shape.getName() != null && object != null && shape.getName().equals(object.getName());                                
            } else
                shapeEnable = true;
        }
        if (parentShape == null) {
            shapeEnable = shape.getName() != null && shape.getName().equals(equipmentModel.getName());
        }        
        Widget shapeWidget = null;
        
        if (RectangleShape.SHAPE_TYPE.equals(type)) {            
            shapeWidget = shapeEnable && object != null ? ((AbstractScene) parentWidget.getScene()).addNode(object) : new Widget(parentWidget.getScene());
        }
        if (LabelShape.SHAPE_TYPE.equals(type)) {
            shapeWidget = new LabelWidget(parentWidget.getScene());
            //TODO: for future feature the use of the width or height percentage 
            //depend of the orientation of the text
            ((LabelShape) shape).setFontSize((int) (((LabelShape) shape).getFontSize() * Math.abs(heightPercentage - 0.30)));
            Font font = new Font(null, 0, ((LabelShape) shape).getFontSize());
            ((LabelWidget) shapeWidget).setFont(font);
            ((LabelWidget) shapeWidget).setLabel(((LabelShape) shape).getLabel());
            
            if (shapeEnable)
                ((LabelWidget) shapeWidget).setForeground(((LabelShape) shape).getTextColor());
            else
                ((LabelWidget) shapeWidget).setForeground(Color.GRAY);
        }     
        
        if (parentShape != null) {
            shape.setX((int) (shape.getX() * widthPercentage));
            shape.setY((int) (shape.getY() * heightPercentage));
        } else {
            modelLayoutWidget = shapeWidget;
            shape.setX(rectangle.x);
            shape.setY(rectangle.y);
        }        
        shape.setWidth((int) (shape.getWidth() * widthPercentage));
        shape.setHeight((int) (shape.getHeight() * heightPercentage));
        
        ModelLayoutScene.shapeToWidget(shape, shapeWidget);
        if (!shapeEnable) {
            shapeWidget.setBorder(BorderFactory.createLineBorder(shape.getBorderWidth(), Color.GRAY));
            shapeWidget.setBackground(Color.LIGHT_GRAY);
        }
        shapeWidget.setOpaque(true);
        
        shapeWidget.revalidate();
        parentWidget.addChild(shapeWidget);
        parentWidget.getScene().validate();
        parentWidget.getScene().paint();
        
        while (reader.hasNext()) {
            
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT) {
                if (reader.getName().equals(tagShape)) {
                    
                    Shape shapeChild = ModelLayoutScene.XMLtoShape(reader, null);
                    LocalObjectLight objChild = findTheObjectForTheShape(shapeChild, objChildren);
                    List<LocalObjectLight> children = getObjectChildren(objChild);
                    
                    if (children == null) {
                        children = objChildren;
                    }
                    recursiveRender(shapeEnable, objChild, children, reader, tagShape, shape, shapeWidget, widthPercentage, heightPercentage);
                }                                              
            } else if (event == XMLStreamConstants.END_ELEMENT)
                return;
        }
    }
    
    private LocalObjectLight findTheObjectForTheShape(Shape shape, List<LocalObjectLight> children) {
        if (shape.getName() == null || "".equals(shape.getName()) || children == null)
            return null;
        
        for (LocalObjectLight child : children) {
            if (shape.getName().equals(child.getName())) {
                return child;                
            }
        }
        
        return null;
    }
    
    private List<LocalObjectLight> getObjectChildren(LocalObjectLight lol) {
        if (lol == null)
            return null;
        
        List<LocalObjectLight> children = CommunicationsStub.getInstance().getObjectChildren(lol.getOid(), lol.getClassName());
        
        if (children == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return null;
        } else
            return children;
    }
    
    public Widget getModelLayoutWidget() {
        return modelLayoutWidget;
    }
}