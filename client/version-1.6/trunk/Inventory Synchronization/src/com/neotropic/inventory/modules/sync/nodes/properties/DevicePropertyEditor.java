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
package com.neotropic.inventory.modules.sync.nodes.properties;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DevicePropertyEditor extends PropertyEditorSupport implements InplaceEditor.Factory, ExPropertyEditor {
    private final InplaceEditor inplaceEditor;
        
    public DevicePropertyEditor() {
        
        
        inplaceEditor = new InplaceEditor() {
            private final JComboBox<LocalObjectLight> cboDevices;
            private PropertyModel model;
            private PropertyEditor editor; 
            
            {
                cboDevices = new JComboBox();
                cboDevices.addItem(NoneObject.getInstance());
                                                
                List<LocalObjectLight> devices = CommunicationsStub.getInstance().getObjectsOfClassLight(Constants.CLASS_GENERICNETWORKELEMENT);

                if (devices == null) {
                    NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                        NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                } else {                   
                    for (LocalObjectLight device : devices)
                        cboDevices.addItem(device);
                }
            }

            @Override
            public void connect(PropertyEditor pe, PropertyEnv env) {
                editor = pe;
                reset();
            }

            @Override
            public JComponent getComponent() {
                return cboDevices;
            }

            @Override
            public void clear() {
                editor = null;
                model = null;
            }

            @Override
            public Object getValue() {
                return cboDevices.getSelectedItem();
            }

            @Override
            public void setValue(Object o) {
                cboDevices.setSelectedItem(o);
            }

            @Override
            public boolean supportsTextEntry() {
                return true;
            }

            @Override
            public void reset() {
                LocalObjectLight device = (LocalObjectLight) editor.getValue();
                
                if (device != null)
                    cboDevices.setSelectedItem(device);
            }

            @Override
            public void addActionListener(ActionListener al) { }

            @Override
            public void removeActionListener(ActionListener al) { }

            @Override
            public KeyStroke[] getKeyStrokes() {
                return new KeyStroke[0];
            }

            @Override
            public PropertyEditor getPropertyEditor() {
                return editor;
            }

            @Override
            public PropertyModel getPropertyModel() {
                return model;
            }

            @Override
            public void setPropertyModel(PropertyModel pm) {
                this.model = pm;
            }

            @Override
            public boolean isKnownComponent(Component c) {
                return c == cboDevices || cboDevices.isAncestorOf(c);
            }
        };
    }
        
    @Override
    public InplaceEditor getInplaceEditor() {
        return inplaceEditor;
    }

    @Override
    public void attachEnv(PropertyEnv env) {
        env.registerInplaceEditorFactory(this);
    }
}
