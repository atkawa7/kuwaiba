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
package com.neotropic.kuwaiba.modules.reporting.img.rackview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * A widget to wrap a RackWidget. Used to fix the position in the scene of the RackWidget
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RackWidgetWrapper extends RackViewWidget {
    private final RemoteObjectLight rackLight;    
    private final boolean showRackInformation;
    
    public RackWidgetWrapper(RackViewScene scene, RemoteObjectLight rackLight, boolean showRackInformation) {
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
            RemoteObject rack = (RemoteObject) rackLight;
            
            boolean ascending = rackWidget.isAscending();
            int rackUnits = rackWidget.getRackUnits();
            int rackUnitsCounter = rackWidget.getRackUnitsCounter();                        
            
            String lblName = "Name: ";
            String lblSerialNumber = "Serial Number: ";
            String lblVendor = "Vendor: ";
            String lblRackNumbering = "Numbering: ";

            String lblAscending = "Ascending";
            String lblDescending = "Descending";

            String lblUsagePercentage = "Usage Percentage";

            String name = rack.getName();
            String serialNumber = rack.getAttribute("serialNumber") == null ? "" : rack.getAttribute("serialNumber"); //NOI18N
            String vendor = rack.getAttribute("vendor") == null ? "" : rack.getAttribute("vendor"); //NOI18N
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

