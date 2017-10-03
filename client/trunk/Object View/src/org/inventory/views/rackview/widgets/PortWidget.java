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
import java.awt.Dimension;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Widget;

/**
 * A widget to represent a port object which in the future will have actions like 
 * connect and disconnect or be a more complex representation
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PortWidget extends SelectableRackViewWidget implements NestedDevice {
    private NestedDeviceWidget parent;
    
    private static final Color selectedColor = new Color(255, 255, 255, 230);
    private static final Color colorGreen = new Color(144, 245, 0);
    private static final Color colorRed = new Color(255, 62, 51);
    
    private boolean free;
    private final Widget innerWidget;
    
    private final LocalClassMetadata portClass;

    public PortWidget(RackViewScene scene, LocalObjectLight portObject) {
        super(scene, portObject);
        
        portClass = CommunicationsStub.getInstance().getMetaForClass(portObject.getClassName(), false);
        if (portClass == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
        free = true; 
        innerWidget = new Widget(scene);
        innerWidget.setBackground(portClass.getColor());
        innerWidget.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        innerWidget.setPreferredSize(new Dimension(25, 25));
        innerWidget.setOpaque(true);
        
        setOpaque(true);        
        setBackground(colorGreen);
        setToolTipText(portObject.getName());
        setPreferredSize(new Dimension(25, 25));
        
        addChild(innerWidget);
    }
    
    @Override
    public NestedDeviceWidget getParent() {
        return parent;
    }
    
    @Override
    public void setParent(NestedDeviceWidget parent) {
        this.parent = parent;
    }
    
    public boolean isFree() {        
        return free;        
    }
    
    public void setFree(boolean free) {
        this.free = free;
        if (free)
            setBackground(colorGreen);
        else
            setBackground(colorRed);            
    }
    
    
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        if (state.isSelected())
            innerWidget.setBackground(selectedColor);
        if (previousState.isSelected())
            innerWidget.setBackground(portClass.getColor());
    }
}
