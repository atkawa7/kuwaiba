/**
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.core.templates.layouts.providers;

import java.awt.Point;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 * Drop. Accept provider to set the name of shapes from template manager
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ShapeNameAcceptProvider implements AcceptProvider {
    
    public ShapeNameAcceptProvider() {
    }

    @Override
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
        if (!transferable.isDataFlavorSupported(LocalObjectLight.DATA_FLAVOR))
            return ConnectorState.REJECT;
        return ConnectorState.ACCEPT;

    }

    @Override
    public void accept(Widget widget, Point point, Transferable transferable) {
        if (widget.getScene() instanceof DeviceLayoutScene) {
            LocalObjectLight lol = null;
            try {
                lol = (LocalObjectLight) transferable.getTransferData(LocalObjectLight.DATA_FLAVOR);
            } catch (UnsupportedFlavorException | IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            Shape shape = (Shape) ((DeviceLayoutScene) widget.getScene()).findObject(widget);
            
            String oldName = shape.getName();
            shape.setName(lol != null ? lol.getName() : "");
            shape.firePropertyChange(widget, Shape.PROPERTY_NAME, oldName, shape.getName());
            
            boolean oldIsInventoryObj = shape.isEquipment();
            shape.setIsEquipment(true);
            shape.firePropertyChange(widget, Shape.PROPERTY_IS_EQUIPMENT, oldIsInventoryObj, shape.isEquipment());
            
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "Shape name set");
            ((DeviceLayoutScene) widget.getScene()).fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape Paste"));
        }
    }
    
}
