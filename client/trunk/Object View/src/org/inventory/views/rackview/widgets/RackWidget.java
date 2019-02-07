/*
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
package org.inventory.views.rackview.widgets;

import org.inventory.views.rackview.scene.RackViewScene;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.DeviceLayoutRenderer;
import org.inventory.core.templates.layouts.DeviceLayoutStructure;
import org.inventory.views.rackview.RackViewService;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Is the graphical representation in the scene of a rack object
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RackWidget extends SelectableRackViewWidget {
    private int rackUnitWidth;
    private int rackUnitHeight;
    private int spacingRackUnits;
    
    private static final Color grayColor = new Color(128, 128, 128);
    private static final Color whiteSmokeColor = new Color(245, 245, 245);
    private static final Dimension dimension = new Dimension(39, 39);
    
    private int rackUnits;
    private boolean ascending;
        
    private final Map<Integer, RackUnitWidget> mapRackUnits;
    
    private LayerWidget rackUnitsLayer;
    private LayerWidget equipmentsLayer;
    private LayerWidget edgeLayer;
    
    private List<LocalObject> localEquipments = new ArrayList<>();
    
    private DeviceLayoutStructure deviceLayoutStructure;
    
    public RackWidget(RackViewScene scene, LocalObjectLight businessObject, int rackUnitWidth, int rackUnitHeight, int rackUnitBottomMargin, List<LocalObject> equipments) {
        super(scene, businessObject);
        this.rackUnitWidth = rackUnitWidth;
        this.rackUnitHeight = rackUnitHeight;
        this.spacingRackUnits = rackUnitBottomMargin;
        
        mapRackUnits = new HashMap<>();
        localEquipments = new ArrayList<>();       
        
        if (scene.getShowConnections())
            deviceLayoutStructure = new DeviceLayoutStructure(businessObject);
        buildRack(equipments);
    }
        
    public RackUnitWidget findRackUnitIndex(Point point) {
        for (RackUnitWidget rackUnit : mapRackUnits.values()) {
            Rectangle bounds = rackUnit.getBounds();
            if (bounds == null)
                continue;
            bounds = rackUnit.convertLocalToScene(bounds);
            if (bounds == null)
                continue;        
            // variables used to define the limits of the recatangle
            int a = bounds.x;
            int b = bounds.x + bounds.width;
            int c = bounds.y;
            int d = bounds.y + bounds.height;
            // (a <= x <= b) (c <= y <= d)
            if (a <= point.x && point.x <= b && c <= point.y && point.y <= d)
                return rackUnit;
        }
        return null;
    }
        
    public int getRackUnits() {
        return rackUnits;
    }
    
    public boolean isAscending() {
        return ascending;        
    }
    
    public int getRackUnitsCounter() {
        int rackUsage = 0;
        for (LocalObject equipment : localEquipments) {
            rackUsage += (int) equipment.getAttribute(Constants.PROPERTY_RACK_UNITS);
        }
        return rackUsage;
    }

    public int getRackUnitWidth() {
        return rackUnitWidth;
    }

    public void setRackUnitWidth(int rackUnitWidth) {
        this.rackUnitWidth = rackUnitWidth;
    }

    public int getRackUnitHeight() {
        return rackUnitHeight;
    }

    public void setRackUnitHeight(int rackUnitHeight) {
        this.rackUnitHeight = rackUnitHeight;
    }

    public int getSpacingRackUnits() {
        return spacingRackUnits;
    }

    public void setSpacingRackUnit(int spacingRackUnit) {
        this.spacingRackUnits = spacingRackUnit;
    }
    
    public LayerWidget getEdgetLayer() {
        return edgeLayer;
    }
    
    private void buildRack(List<LocalObject> equipments) {
        LocalObject rack = getLookup().lookup(LocalObject.class);
        if (rack != null) {
            rackUnits = (int) rack.getAttribute(Constants.PROPERTY_RACK_UNITS);            
            ascending = rack.getAttribute(Constants.PROPERTY_RACK_UNITS_NUMBERING) != null ? 
                !((boolean) rack.getAttribute(Constants.PROPERTY_RACK_UNITS_NUMBERING)) : true;
            
            int rackUnit = ascending ? 1 : rackUnits;
            while (ascending ? rackUnit <= rackUnits : rackUnit >= 1) {
                RackUnitWidget rackUnitWidget = new RackUnitWidget(getRackViewScene(), rackUnit, this);
                mapRackUnits.put(rackUnit, rackUnitWidget);
                rackUnit = ascending ? rackUnit + 1 : rackUnit - 1;
            }
            RackViewService.switchToDeterminate(equipments.size());
            RackViewService.setProgress("Loading the devices");            
            
            setLayout(LayoutFactory.createVerticalFlowLayout());
            Widget top = new Widget(getRackViewScene());
            top.setBackground(grayColor);
            top.setMinimumSize(dimension);
            top.setOpaque(true);
            top.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
            
            LabelWidget lblDeviceName = new LabelWidget(getRackViewScene(), rack.toString());
            lblDeviceName.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            lblDeviceName.setForeground(new Color(245, 245, 245));

            top.addChild(lblDeviceName);
            
            Widget middle = new Widget(getRackViewScene());            
            middle.setLayout(LayoutFactory.createHorizontalFlowLayout());
            
            Widget bottom = new Widget(getRackViewScene());
            bottom.setBackground(grayColor);            
            bottom.setMinimumSize(dimension);
            bottom.setOpaque(true);
            
            Widget left = new Widget(getRackViewScene());
            left.setLayout(LayoutFactory.createVerticalFlowLayout());
            left.setBackground(grayColor);
            left.setOpaque(true);
            
            LayerWidget numberLayer = new LayerWidget(getRackViewScene());            
            numberLayer.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, getSpacingRackUnits()));
            
            left.addChild(numberLayer);
            
            rackUnit = ascending ? 1 : rackUnits;
            while (ascending ? rackUnit <= rackUnits : rackUnit >= 1) {
                Widget numberWidget = new Widget(getRackViewScene());
                numberWidget.setPreferredSize(new Dimension(dimension.width, getRackUnitHeight()));
                numberWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
                
                LabelWidget lblRackUnit = new LabelWidget(getRackViewScene(), String.valueOf(rackUnit));
                lblRackUnit.setForeground(whiteSmokeColor);
                lblRackUnit.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
                
                numberWidget.addChild(lblRackUnit);
                
                numberLayer.addChild(numberWidget);
                
                rackUnit = ascending ? rackUnit + 1 : rackUnit - 1;
            }
                                    
            Widget center = new Widget(getRackViewScene());
            center.setBackground(grayColor);
            center.setOpaque(true);
            
            rackUnitsLayer = new LayerWidget(getRackViewScene());
            rackUnitsLayer.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, getSpacingRackUnits()));
            
            center.addChild(rackUnitsLayer);
            
            equipmentsLayer = new LayerWidget(getRackViewScene());
            equipmentsLayer.setLayout(LayoutFactory.createAbsoluteLayout());
            
            center.addChild(equipmentsLayer);
            
            rackUnit = ascending ? 1 : rackUnits;
            while (ascending ? rackUnit <= rackUnits : rackUnit >= 1) {
                rackUnitsLayer.addChild(mapRackUnits.get(rackUnit));                
                rackUnit = ascending ? rackUnit + 1 : rackUnit - 1;
            }
                                    
            for (int i = 0; i < equipments.size(); i += 1) {
                addEquipment(equipments.get(i));
                RackViewService.setProgress("Loading " + equipments.get(i).toString(),i + 1);
            }
            
            edgeLayer = new LayerWidget(getRackViewScene());
            center.addChild(edgeLayer);
                        
            Widget right = new Widget(getRackViewScene());
            right.setBackground(grayColor);
            right.setPreferredSize(new Dimension(dimension));
            right.setOpaque(true);
            
            middle.addChild(left);
            middle.addChild(center);
            middle.addChild(right);
            
            addChild(top);
            addChild(middle);
            addChild(bottom);
        }
    }
    
    
    /**
     * Verifies if an equipment can be added to the rack
     * @param equipment
     * @param position if the value is -1 use the equipment position
     *                 else use the given position to verify if the equipment is rackable
     */
    public boolean isRackable(LocalObject equipment, int position) {
        LocalClassMetadata equipmentClass = equipment.getObjectMetadata();
        
        if (!equipmentClass.hasAttribute(Constants.PROPERTY_POSITION)) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                NotificationUtil.WARNING_MESSAGE, String.format("%s: The %s attribute does not exist", equipment.toString(), Constants.PROPERTY_POSITION));
            return false;
        }
        if (!equipmentClass.hasAttribute(Constants.PROPERTY_RACK_UNITS)) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                NotificationUtil.WARNING_MESSAGE, String.format("%s: The %s attribute does not exist", equipment.toString(), Constants.PROPERTY_RACK_UNITS));
            return false;
        }
        
        if (!"Integer".equals(equipmentClass.getTypeForAttribute(Constants.PROPERTY_POSITION))) { //NOI18N
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                NotificationUtil.WARNING_MESSAGE, String.format("%s: The %s attribute must be an Integer", equipment.toString(), Constants.PROPERTY_POSITION));
            return false;
        }
        if (!"Integer".equals(equipmentClass.getTypeForAttribute(Constants.PROPERTY_RACK_UNITS))) { //NOI18N
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                NotificationUtil.WARNING_MESSAGE, String.format("%s: The %s attribute must be an Integer", equipment.toString(), Constants.PROPERTY_RACK_UNITS));
            return false;
        }
        
        int equipmentPosition = position == -1 ? (equipment.getAttribute(Constants.PROPERTY_POSITION) != null ? (int) equipment.getAttribute(Constants.PROPERTY_POSITION) : -1) : position;
        
        if (!(equipmentPosition >= 0)) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                NotificationUtil.WARNING_MESSAGE, String.format("%s: The position must be greater than or equal to zero", equipment.toString()));
            return false;
        }
        
        if (equipmentPosition > rackUnits) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                NotificationUtil.WARNING_MESSAGE, String.format("%s: The position is greater than the number of rack units", equipment.toString()));
            return false;
        }
        
        if (equipmentPosition == 0)
            return false;
        
        int equipmentRackUnits = equipment.getAttribute(Constants.PROPERTY_RACK_UNITS) != null ? (int) equipment.getAttribute(Constants.PROPERTY_RACK_UNITS) : -1;
        if (!(equipmentRackUnits >= 0)) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                NotificationUtil.WARNING_MESSAGE, String.format("%s: The number of rack units must be greater than or equal to zero", equipment.toString()));
            return false;
        }
        
        if (equipmentRackUnits == 0)
            return false;
        
        for (int i = 0; i < equipmentRackUnits; i += 1) {
            int idx = equipmentPosition + i;
            if (idx > rackUnits) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                    NotificationUtil.WARNING_MESSAGE, String.format("%s: The equipment can not be located in the given %s position", equipment.toString(), equipmentPosition));
                return false;
            }
            if (!mapRackUnits.get(idx).isAvailable()) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("warning"), 
                    NotificationUtil.WARNING_MESSAGE, String.format("%s: The rack unit %s is not available", equipment.toString(), idx));
                return false;
            }            
        }
        return true;        
    }
    
    public String canBeMoved(LocalObject equipment, int newPosition) {
        int equipmentRackUnits = (int) equipment.getAttribute(Constants.PROPERTY_RACK_UNITS);
        int equipmentPosition = (int) equipment.getAttribute(Constants.PROPERTY_POSITION);
        
        List<Integer> currentRackUnits = new ArrayList<>();
        for (int i = 0; i < equipmentRackUnits; i += 1)
            currentRackUnits.add(equipmentPosition + i);
                
        for (int i = 0; i < equipmentRackUnits; i += 1) {
            int idx = newPosition + i;
            if (idx > rackUnits)
                return "The device is too large for the given position";
            
            if (!mapRackUnits.get(idx).isAvailable() && !currentRackUnits.contains(idx))
                return String.format("The position cannot be changed to rack unit %s because rack unit %s is not available", newPosition, idx);
        }
        return null;
    }
    
    public void freeEquipmentRackUnits(LocalObject equipment) {
        int equipmentRackUnits = (int) equipment.getAttribute(Constants.PROPERTY_RACK_UNITS);
        int equipmentPosition = (int) equipment.getAttribute(Constants.PROPERTY_POSITION);
                
        for (int i = 0; i < equipmentRackUnits; i += 1)
            mapRackUnits.get(equipmentPosition + i).setAvailable(true);
    }
    
    public List<LocalObject> getLocalEquipment() {
        return localEquipments;
    }
    
    public boolean containsEquipment(LocalObject equipment) {
        return localEquipments.contains(equipment);
    }
    
    public void addEquipment(LocalObject equipment) {
        int equipmentPosition = (int) equipment.getAttribute(Constants.PROPERTY_POSITION);
        int equipmentRackUnits = (int) equipment.getAttribute(Constants.PROPERTY_RACK_UNITS);
        
        if (equipmentPosition > 0 && equipmentRackUnits > 0) {
            for (int i = 0; i < equipmentRackUnits; i += 1) {
                int idx = equipmentPosition + i;
                mapRackUnits.get(idx).setAvailable(false);
            }
            paintEquipment(equipment);
            localEquipments.add(equipment);
        }
    }
    
    private void paintEquipment(LocalObject equipment) {
        int equipmentPosition = (int) equipment.getAttribute(Constants.PROPERTY_POSITION);
        int equipmentRackUnits = (int) equipment.getAttribute(Constants.PROPERTY_RACK_UNITS);
        
        EquipmentWidget equipmentWidget = (EquipmentWidget) getRackViewScene().findWidget(equipment);
        
        if (equipmentWidget == null) {
            int width = getRackUnitWidth();
            int height = getRackUnitHeight() * equipmentRackUnits + getSpacingRackUnits() * (equipmentRackUnits - 1);
            
            if (getRackViewScene().getShowConnections()) {
                getRackViewScene().setAddingNestedDevice(false);
                
                DeviceLayoutRenderer render = new DeviceLayoutRenderer(equipment, 
                    equipmentsLayer, 
                    new Point(Constants.DEVICE_LAYOUT_RESIZE_BORDER_SIZE, Constants.DEVICE_LAYOUT_RESIZE_BORDER_SIZE), 
                    new Rectangle(0, 0, width, height), deviceLayoutStructure.getHierarchy(), deviceLayoutStructure.getLayouts());
                
                if (render.hasDeviceLayout() || render.hasDefaultDeviceLayout()) {
                    render.render();
                    equipmentWidget = (EquipmentWidget) render.getDeviceLayoutWidget();
                    equipmentWidget.setHasLayout(true);
                }
            }
            if (equipmentWidget == null) {
                equipmentWidget = (EquipmentWidget) (getRackViewScene()).addNode(equipment);
                equipmentWidget.setHasLayout(false);
                
                equipmentsLayer.addChild(equipmentWidget);                
            }
            equipmentWidget.setRackWidget(this);
            equipmentWidget.setMinimumSize(new Dimension(width, height));
            
            if (!equipmentWidget.hasLayout()) {
                getRackViewScene().setAddingNestedDevice(true);
                
                equipmentWidget.paintNestedDeviceWidget();
                
                getRackViewScene().setAddingNestedDevice(false);
            }
        } else {
            equipmentWidget.getLookup().lookup(LocalObject.class)
                .setAttribute(Constants.PROPERTY_POSITION, equipmentPosition);
            equipmentWidget.getLookup().lookup(LocalObject.class)
                .setAttribute(Constants.PROPERTY_RACK_UNITS, equipmentRackUnits);
        }
        int drawPosition = equipmentPosition;
        if (ascending)
            drawPosition -= 1;
        else
            drawPosition = rackUnits - equipmentPosition - (equipmentRackUnits - 1);
        int y = getRackUnitHeight() * drawPosition + getSpacingRackUnits() * drawPosition;
        // The equipment rendered using a layout has borders
        int leftInsets = 0;
        int topInsets = 0;
        Border border = equipmentWidget.getBorder();
        if (border != null) {
            Insets insets = border.getInsets();
            if (insets != null) {
                leftInsets = insets.left;
                topInsets = insets.top;
            }
        }
        equipmentWidget.setPreferredLocation(new Point(0 + leftInsets, y + topInsets));
        getRackViewScene().repaint();
    }
    
    public void resizeRackWidget() {
        if (equipmentsLayer == null)
            return;
        
        Rectangle bounds = equipmentsLayer.getBounds();
        if (bounds != null) {
            for (LocalObjectLight equipment : localEquipments) {
                Widget widget = getRackViewScene().findWidget(equipment);
                if (widget.getBounds() != null) {
                    widget.setPreferredSize(new Dimension(bounds.width, widget.getBounds().height));
                }
            }
            
            for (RackUnitWidget rackUnitWidget : mapRackUnits.values()) {
                if (rackUnitWidget.getBounds() != null) {
                    rackUnitWidget.setPreferredSize(new Dimension(bounds.width, rackUnitWidget.getBounds().height));
                }
            }
        }
    }
}
