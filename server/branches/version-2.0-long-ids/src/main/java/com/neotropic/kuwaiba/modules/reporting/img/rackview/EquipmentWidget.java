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

import com.vaadin.ui.Notification;
import java.awt.Color;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;

/**
 * An equipment is an inventory object which has the "rackUnits" integer attribute 
 * and the "position" integer attribute.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
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
            RemoteObject remoteObject = (RemoteObject) getLookupReplace();            
            try {
                RemoteClassMetadata remoteClassMetadata = RackViewImage.getInstance().getWebserviceBean().getClass(
                    remoteObject.getClassName(), 
                    RackViewImage.getInstance().getIpAddress(), 
                    RackViewImage.getInstance().getRemoteSession().getSessionId());
                
                setBackground(new Color(remoteClassMetadata.getColor()));
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
            }
            setOpaque(true);
            setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 0));
                        
            LabelWidget lblDeviceName = new LabelWidget(getRackViewScene(), remoteObject.toString());
            
            int top, bottom;
            top = bottom = (getMinimumSize().height - rackWidget.getRackUnitHeight()) / 2;

            lblDeviceName.setBorder(BorderFactory.createEmptyBorder(top == 0 ? 3 : top, 0, 0, 0));
            lblDeviceName.setForeground(Color.WHITE);

            LabelWidget lblDeviceInfo = new LabelWidget(getRackViewScene(), 
                "Position: " + remoteObject.getAttribute("position") + " U - " + 
                "Size: " + remoteObject.getAttribute("rackUnits") + " U");
            lblDeviceInfo.setBorder(BorderFactory.createEmptyBorder(0, 0, bottom, 0));
            lblDeviceInfo.setForeground(Color.WHITE);

            this.addChild(lblDeviceName);
            this.addChild(lblDeviceInfo);
        }
    }
}

