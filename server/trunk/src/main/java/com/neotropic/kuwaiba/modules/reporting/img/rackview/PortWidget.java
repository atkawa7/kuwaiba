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
import java.awt.Dimension;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;
/**
 * A widget to represent a port object which in the future will have actions like 
 * connect and disconnect or be a more complex representation
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PortWidget extends SelectableRackViewWidget implements NestedDevice {
    private boolean isNested = false;
    private NestedDeviceWidget parent;
    
    private static final Color SELECTED_COLOR = new Color(255, 255, 255, 230);
    private static final Color COLOR_GREEN = new Color(144, 245, 0);
    private static final Color COLOR_RED = new Color(255, 62, 51);
    
    private boolean free;
    private final Widget innerWidget;
    
    private RemoteClassMetadata portClass;
    
    private Color previousColor;

    public PortWidget(RackViewScene scene, RemoteObjectLight portObject, boolean isNested) {
        super(scene, portObject);
        
        try {
            portClass = RackViewImage.getInstance().getWebserviceBean().getClass(
                    portObject.getClassName(),
                    RackViewImage.getInstance().getIpAddress(),
                    RackViewImage.getInstance().getRemoteSession().getSessionId());
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }        
        free = true; 
        if (isNested) {
            innerWidget = new Widget(scene);
            innerWidget.setBackground(new Color(portClass.getColor()));
            innerWidget.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
            innerWidget.setPreferredSize(new Dimension(25, 25));
            innerWidget.setOpaque(true);

            setOpaque(true);        
            setBackground(COLOR_GREEN);
            setToolTipText(portObject.getName());
            setPreferredSize(new Dimension(25, 25));

            addChild(innerWidget);
        } else
            innerWidget = null;
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
            setBackground(COLOR_GREEN);
        else
            setBackground(COLOR_RED);            
    }
    
    
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        if (previousState.isSelected()) {
            if (isNested)
                innerWidget.setBackground(new Color(portClass.getColor()));
            else
                setBackground(previousColor);
            return;
        }        
        if (state.isSelected()) {
            if (isNested)
                innerWidget.setBackground(SELECTED_COLOR);
            else {
                previousColor = new Color(((Color) this.getBackground()).getRGB());
                setBackground(SELECTED_COLOR);
            }
        }
    }
    
    public boolean isNested() {
        return isNested;
    }
    
    public void setIsNested(boolean isNested) {
        this.isNested = isNested;       
    }
}

