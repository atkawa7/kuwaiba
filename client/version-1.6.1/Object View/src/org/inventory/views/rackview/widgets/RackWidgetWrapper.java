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

import org.inventory.views.rackview.scene.RackViewScene;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ResourceBundle;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * A widget to wrap a RackWidget. Used to fix the position in the scene of the RackWidget
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RackWidgetWrapper extends RackViewWidget {
    private final LocalObjectLight rackLight;    
    private final boolean showRackInformation;
    
    public RackWidgetWrapper(RackViewScene scene, LocalObjectLight rackLight, boolean showRackInformation) {
        super(scene);
        this.rackLight = rackLight;    
        this.showRackInformation = showRackInformation;
    }
    
    public void paintRack() {
        if (rackLight != null) {
            Point point = getPreferredLocation();
            if (point == null)
                point = new Point(0, 0);

            this.setLayout(LayoutFactory.createVerticalFlowLayout());     

            Widget xWidget = new Widget(getRackViewScene());
            xWidget.setPreferredSize(new Dimension(point.x, point.y));

            Widget centerWidget = new Widget(getRackViewScene());
            centerWidget.setLayout(LayoutFactory.createHorizontalFlowLayout());

            Widget yWidget = new Widget(getRackViewScene());
            yWidget.setPreferredSize(new Dimension(point.x, point.y));

            centerWidget.addChild(yWidget);
            
            RackWidget rackWidget = (RackWidget) (getRackViewScene()).addNode(rackLight);
            rackWidget.setOpaque(true);
            
            centerWidget.addChild(rackWidget);
            
            Widget informationWidget = new Widget(getRackViewScene());
            informationWidget.setLayout(LayoutFactory.createVerticalFlowLayout());
                        
            centerWidget.addChild(informationWidget);
            setInformation(informationWidget, rackWidget);
            
            this.addChild(xWidget);
            this.addChild(centerWidget);        
            getRackViewScene().validate();
        }
    }
    
    public void setInformation(Widget informationWidget, RackWidget rackWidget) {
        if (showRackInformation) {
            LocalObject rack = (LocalObject) rackLight;
            
            boolean ascending = rackWidget.isAscending();
            int rackUnits = rackWidget.getRackUnits();
            int rackUnitsCounter = rackWidget.getRackUnitsCounter();                        
            
            String lblName = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_NAME"); //NOI18N
            String lblSerialNumber = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_SERIAL_NUMBER"); //NOI18N
            String lblVendor = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_VENDOR"); //NOI18N
            String lblRackNumbering = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_NUMBERING"); //NOI18N

            String lblAscending = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_NUMBERING_ASCENDING"); //NOI18N
            String lblDescending = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_NUMBERING_DESCENDING"); //NOI18N

            String lblUsagePercentage = ResourceBundle.getBundle("org/inventory/views/rackview/Bundle").getString("LBL_RACK_USAGE_PERCENTAGE"); //NOI18N

            String name = rack.getName();
            String serialNumber = rack.getAttribute("serialNumber") == null ? "" : rack.getAttribute("serialNumber").toString(); //NOI18N
            String vendor = rack.getAttribute("vendor") == null ? "" : rack.getAttribute("vendor").toString(); //NOI18N
            String rackNumbering = ascending ? lblAscending : lblDescending;
            String usagePercentage = "" + Math.round((float)rackUnitsCounter * 100/rackUnits) +"% (" + rackUnitsCounter + "U/" + rackUnits + "U)";

            addRackInfoLabel(informationWidget, String.format("%s: %s", lblName, name), false);
            addRackInfoLabel(informationWidget, String.format("%s: %s", lblSerialNumber, serialNumber), false);
            addRackInfoLabel(informationWidget, String.format("%s: %s", lblVendor, vendor), false);
            addRackInfoLabel(informationWidget, String.format("%s: %s", lblRackNumbering, rackNumbering), false);
            addRackInfoLabel(informationWidget, String.format("%s: %s", lblUsagePercentage, usagePercentage), true);
        }
    }
    
    private void addRackInfoLabel(Widget informationWidget, String infoLabel, boolean emphasis) {
        LabelWidget infoLblWidget = new LabelWidget(getRackViewScene(), infoLabel);
        infoLblWidget.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 0));
        
        if (emphasis)
            infoLblWidget.setForeground(Color.RED);
        infoLblWidget.setOpaque(true);
        
        informationWidget.addChild(infoLblWidget);
    }
}
