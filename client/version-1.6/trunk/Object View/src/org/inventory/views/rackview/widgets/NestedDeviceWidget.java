/*
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
package org.inventory.views.rackview.widgets;

import org.inventory.views.rackview.NestedDevice;
import org.inventory.views.rackview.scene.RackViewScene;
import java.awt.Color;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObjectLight;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;

/**
 * A widget used to represent a containment hierarchy in a device
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NestedDeviceWidget extends SelectableRackViewWidget implements NestedDevice {
    private NestedDeviceWidget parent;
        
    private static final Color selectedColor = new Color(255, 255, 255, 230);
    private LabelWidget lblName;
    private RackViewWidget widgetToChildren;
    
    private final LocalClassMetadata nestedDeviceClass;
    
    private Color previousBackground;
    private boolean hasLayout;
    
    public NestedDeviceWidget(RackViewScene scene, LocalObjectLight businessObject, boolean hasLayout) {
        super(scene, businessObject);
        nestedDeviceClass = CommunicationsStub.getInstance().getMetaForClass(businessObject.getClassName(), false);
        this.hasLayout = hasLayout;
    }
    
    public boolean hasLayout() {
        return hasLayout;
    }
    
    public void setHasLayout(boolean hasLayout) {
        this.hasLayout = hasLayout;        
    }
    
    @Override
    public NestedDeviceWidget getParent() {
        return parent;
    }
    
    @Override
    public void setParent(NestedDeviceWidget parent) {
        this.parent = parent;
    }
    
    public void paintNestedDeviceWidget() {
        if (!hasLayout()) {
            setLayout(LayoutFactory.createVerticalFlowLayout());
            Color backgroundColor = nestedDeviceClass.getColor();
            setBackground(backgroundColor != null ? backgroundColor : Color.WHITE);
            setOpaque(true);

            lblName = new LabelWidget(getRackViewScene());
            lblName.setBorder(BorderFactory.createEmptyBorder(0, 5 ,0 , 5));
            lblName.setLabel(getLookup().lookup(LocalObjectLight.class).getName());
            lblName.setForeground(Color.WHITE);

            widgetToChildren = new RackViewWidget(getRackViewScene());
            widgetToChildren.setBorder(BorderFactory.createEmptyBorder(5, 5 ,5 , 5));
            widgetToChildren.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 2));

            addChild(lblName);
            addChild(widgetToChildren);
        }
    }
    
    public void addChildDevice(SelectableRackViewWidget child) {
        widgetToChildren.addChild(child);
        ((NestedDevice) child).setParent(this);
    }
    
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        if (previousState.isSelected()) {
            setBackground(previousBackground);
            if (lblName != null)
                lblName.setForeground(Color.WHITE);
            return;
        }
        
        if (state.isSelected()) {
            previousBackground = new Color(((Color) getBackground()).getRGB());
            setBackground(selectedColor);
            if (lblName != null)
                lblName.setForeground(Color.ORANGE);
        }
    }
}
