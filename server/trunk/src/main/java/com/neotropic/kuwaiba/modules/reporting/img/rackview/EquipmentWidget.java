/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.Color;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;

/**
 * An equipment is an inventory object which has the "rackUnits" integer attribute 
 * and the "position" integer attribute.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class EquipmentWidget extends NestedDeviceWidget {
    private RackWidget rackWidget;
    
    public EquipmentWidget(RackViewScene scene, RemoteObjectLight equipment, Color background, boolean hasLayout) {
        super(scene, equipment, hasLayout);
    }
    // The Equipment Widgets not has parent because they are the root in the 
    // containment of devices
    @Override
    public final NestedDeviceWidget getParent() {
        return null;
    }
    // The Equipment Widgets not has parent because they are the root in the 
    // containment of devices
    @Override
    public final void setParent(NestedDeviceWidget parent) {
        super.setParent(null);
    }
        
    public void setRackWidget(RackWidget rackWidget) {
        this.rackWidget = rackWidget;
    }
    
    @Override
    public void paintNestedDeviceWidget() {
        if (getRackViewScene().getShowConnections()) {
            super.paintNestedDeviceWidget();
        } else {
            
            //${TODO}setBackground(getLookup().lookup(LocalObject.class).getObjectMetadata().getColor());
            setBackground(Color.CYAN);
            setOpaque(true);
            setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
////            LabelWidget lblDeviceName = new LabelWidget(getRackViewScene(), getLookup().lookup(LocalObject.class).toString());
            BusinessObject businessObject = (BusinessObject) getLookupReplace();
            
            LabelWidget lblDeviceName = new LabelWidget(getRackViewScene(), businessObject.toString());
            
            int top, bottom;
            top = bottom = (getMinimumSize().height - rackWidget.getRackUnitHeight()) / 2;

            lblDeviceName.setBorder(BorderFactory.createEmptyBorder(top == 0 ? 3 : top, 0, 0, 0));
            lblDeviceName.setForeground(Color.WHITE);

            LabelWidget lblDeviceInfo = new LabelWidget(getRackViewScene(), 
                "Position: " + businessObject.getAttributes().get("position") + " U - " + 
                "Size: " + businessObject.getAttributes().get("rackUnits") + " U");
            lblDeviceInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, bottom, 0));
            lblDeviceInfo.setForeground(Color.WHITE);

            this.addChild(lblDeviceName);
            this.addChild(lblDeviceInfo);
        }
    }
}

