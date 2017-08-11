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
 */
package org.inventory.views.rackview.scene;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.SelectableNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * Scene for Rack view
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RackViewScene extends AbstractScene<LocalObject, LocalObject> {
    public static final int STANDARD_RACK_WIDTH = 300;
    public static final int RACK_UNIT_IN_PX = 35;
    
    public static final int RACK_Y_OFFSET = 5;
    
    private LayerWidget numberingInRackLayer;
    private LayerWidget rackLayer;
    private LayerWidget deviceLayer;
    private LayerWidget infoLayer;
    
    private boolean ascending = true;
    private int rackUnits;
    
    private final Color boxColor = new Color(128, 128, 128);
    
    public RackViewScene() {
        getActions().addAction(ActionFactory.createZoomAction());
        getInputBindings ().setZoomActionModifiers(0); //No keystroke combinations
        getActions().addAction(ActionFactory.createPanAction());
                
        setActiveTool(ACTION_SELECT);
        initSelectionListener();
    }
        
    private void buildScene(LocalObject rack) {
        int margin = 50;
        
        Widget topWidget = new Widget(this);        
        topWidget.setPreferredSize(new Dimension(margin, margin));
        addChild(topWidget);
        
        Widget middleWidget = new Widget(this);
        middleWidget.setLayout(LayoutFactory.createHorizontalFlowLayout());
        addChild(middleWidget);
        
        Widget leftWidget = new Widget(this);
        leftWidget.setPreferredSize(new Dimension(margin, margin));
        
        Widget centerWidget = new Widget(this);
        centerWidget.setLayout(LayoutFactory.createHorizontalFlowLayout());
                
        Widget rightWidget = new Widget(this);
        rightWidget.setPreferredSize(new Dimension(margin, margin));
        
        middleWidget.addChild(leftWidget);
        middleWidget.addChild(centerWidget);
        middleWidget.addChild(rightWidget);
        
        Widget bottomWidget = new Widget(this);
        bottomWidget.setPreferredSize(new Dimension(margin, margin));
        addChild(bottomWidget);
        
        buildBox(rack, centerWidget);
        
        Widget infoWidget = new Widget(this);
        infoWidget.setLayout(LayoutFactory.createVerticalFlowLayout());
                
        centerWidget.addChild(infoWidget);
        infoWidget.addChild(infoLayer);
        
        validate();
    }
    
    private void buildBox(LocalObject rack, Widget centerWidget) {
        Widget verticalBoxWidget = new Widget(this);
        verticalBoxWidget.setLayout(LayoutFactory.createVerticalFlowLayout());
        
        SelectableDeviceWidget topBoxWidget = new SelectableDeviceWidget(this, rack);
        topBoxWidget.createActions(AbstractScene.ACTION_SELECT);
        topBoxWidget.getActions(ACTION_SELECT).addAction(createSelectAction());
        topBoxWidget.setBackground(boxColor);
        topBoxWidget.setPreferredSize(new Dimension(39, 39));
        topBoxWidget.setOpaque(true);
        topBoxWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
        
        LabelWidget lblDeviceName = new LabelWidget(this, rack.toString());
        lblDeviceName.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        lblDeviceName.setForeground(new Color(245, 245, 245));
        
        topBoxWidget.addChild(lblDeviceName);
        
        Widget bottomBoxWidget = new Widget(this);
        bottomBoxWidget.setBackground(boxColor);
        bottomBoxWidget.setPreferredSize(new Dimension(39, 39));
        bottomBoxWidget.setOpaque(true);
        
        Widget horizontalBoxWidget = new Widget(this);
        horizontalBoxWidget.setLayout(LayoutFactory.createHorizontalFlowLayout());
        
        Widget numberingInRackWidget = new Widget(this);
        numberingInRackWidget.setLayout(LayoutFactory.createVerticalFlowLayout());
        numberingInRackWidget.setBackground(boxColor);
        numberingInRackWidget.setOpaque(true);
        numberingInRackWidget.addChild(numberingInRackLayer);
        horizontalBoxWidget.addChild(numberingInRackWidget);
        
        Widget rackWidget = new Widget(this);
        rackWidget.setLayout(LayoutFactory.createAbsoluteLayout());
        rackWidget.setBackground(boxColor);
        rackWidget.setOpaque(true);
                
        rackWidget.addChild(rackLayer);
        rackWidget.addChild(deviceLayer);
        
        horizontalBoxWidget.addChild(rackWidget);
                
        Widget rightSideBoxWidget = new Widget(this);
        rightSideBoxWidget.setPreferredSize(new Dimension(39, 39));
        rightSideBoxWidget.setBackground(boxColor);
        rightSideBoxWidget.setOpaque(true);
        horizontalBoxWidget.addChild(rightSideBoxWidget);
        
        verticalBoxWidget.addChild(topBoxWidget);
        verticalBoxWidget.addChild(horizontalBoxWidget);
        verticalBoxWidget.addChild(bottomBoxWidget);
        
        centerWidget.addChild(verticalBoxWidget);        
    }
    
    @Override
    public void clear() {
        removeChildren();
        validate();
    }
    
    public void createNumberingInRack(final Boolean ascending, final int rackUnits) {
        this.ascending = ascending;
        this.rackUnits = rackUnits;
        
        int U = ascending ? 1 : rackUnits;
                
        while (ascending ? U <= rackUnits : U >= 1) {
            
            Widget widget = new Widget(this);
            widget.setPreferredSize(new Dimension(39, RackViewScene.RACK_UNIT_IN_PX));
                        
            widget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
            
            LabelWidget lblU = new LabelWidget(this, Integer.toString(U));
            lblU.setForeground(new Color(245, 245, 245));
            lblU.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
            
            widget.addChild(lblU);
            
            numberingInRackLayer.addChild(widget);
                        
            U = ascending ? U + 1 : U - 1;                        
        }
        validate();
    }
    
    public void addRackInfoLabel(String infoLabel, boolean emphasis) {
        LabelWidget infoLblWidget = new LabelWidget(this, infoLabel);
        infoLblWidget.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
        
        if (emphasis)
            infoLblWidget.setForeground(Color.RED);
        
        infoLayer.addChild(infoLblWidget);
        validate();
    }
    
    @Override
    public byte[] getAsXML() {
        return null;
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
    }
    
    @Override
    public Color getConnectionColor(LocalObjectLight theConnection) {
        return null;
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
    public void render(LocalObject root) {
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
        
        rackLayer = new LayerWidget(this);
        rackLayer.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, RACK_Y_OFFSET));
        
        deviceLayer = new LayerWidget(this);
        deviceLayer.setLayout(LayoutFactory.createAbsoluteLayout());
        
        infoLayer = new LayerWidget(this);
        infoLayer.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 10));
        
        numberingInRackLayer = new LayerWidget(this);
        numberingInRackLayer.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, RACK_Y_OFFSET));
        buildScene(root);
    }

    @Override
    protected Widget attachNodeWidget(LocalObject node) {
        if (node.getOid() <= -1) {
            Widget widget = new Widget(this);

            widget.setOpaque(true);
            widget.setBackground(new Color(112, 112, 112));
            widget.setPreferredSize(new Dimension(RackViewScene.STANDARD_RACK_WIDTH, RackViewScene.RACK_UNIT_IN_PX));
            rackLayer.addChild(widget);
            validate();
            
            return widget;
        } else {
            Integer U = (Integer) node.getAttribute(Constants.PROPERTY_RACK_UNITS);
            Integer position = (Integer) node.getAttribute(Constants.PROPERTY_POSITION);
            
            SelectableDeviceWidget deviceWidget = new SelectableDeviceWidget(this, node);
            deviceWidget.createActions(AbstractScene.ACTION_SELECT);
            deviceWidget.getActions(ACTION_SELECT).addAction(createSelectAction());
            
            int width = STANDARD_RACK_WIDTH;
            int height = RACK_UNIT_IN_PX * U + RACK_Y_OFFSET * (U - 1);
                        
            deviceWidget.setBackground(new Color(136, 170, 0));
            deviceWidget.setPreferredSize(new Dimension(width, height));
            deviceWidget.setOpaque(true);
            
            
            deviceWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
            
            LabelWidget lblDeviceName = new LabelWidget(this, node.toString());
            
            int top, bottom;
            top = bottom = (height - RACK_UNIT_IN_PX) / 2;
            
            lblDeviceName.setBorder(BorderFactory.createEmptyBorder(top == 0 ? 3 : top, 0, 0, 0));
            lblDeviceName.setForeground(Color.WHITE);
            
            LabelWidget lblDeviceInfo = new LabelWidget(this, "Position: " + position + "U - " + "Size: " + U + "U");
            lblDeviceInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, bottom, 0));
            lblDeviceInfo.setForeground(Color.WHITE);
            
            int drawPosition = position;
            if (ascending)
                drawPosition -= 1;
            else
                drawPosition = rackUnits - position - (U - 1);
            int y = RACK_UNIT_IN_PX * drawPosition + RACK_Y_OFFSET * drawPosition;
            
            deviceWidget.setPreferredLocation(new Point(0, y));
            
            deviceWidget.addChild(lblDeviceName);
            deviceWidget.addChild(lblDeviceInfo);
            
            deviceLayer.addChild(deviceWidget);
            validate();
            return deviceWidget;
        }
    }

    @Override
    protected Widget attachEdgeWidget(LocalObject edge) {
        return null;
    }

    @Override
    protected void attachEdgeSourceAnchor(LocalObject edge, LocalObject oldSourceNode, LocalObject sourceNode) {
    }

    @Override
    protected void attachEdgeTargetAnchor(LocalObject edge, LocalObject oldTargetNode, LocalObject targetNode) {
    }
    /**
     * Class to wrap a Rack Object in a Widget. 
     */
    private class SelectableDeviceWidget extends SelectableNodeWidget {
        private boolean selected = false;

        public SelectableDeviceWidget(Scene scene, LocalObjectLight businessObject) {
            super(scene, businessObject);
            setToolTipText(null);
        }
        
        @Override
        public void notifyStateChanged(ObjectState previousState, ObjectState state) {
            if (!selected) {
                selected = true;
                setBackground(new Color(136, 170, 0));
            } else {
                selected = false;                
                setBackground(Color.LIGHT_GRAY);
            }
        }
    }
}
