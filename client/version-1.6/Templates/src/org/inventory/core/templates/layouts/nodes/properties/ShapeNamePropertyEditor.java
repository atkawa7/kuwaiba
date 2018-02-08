/**
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.templates.layouts.nodes.properties;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

/**
 * Property editor support to the name in shapes
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ShapeNamePropertyEditor extends PropertyEditorSupport implements InplaceEditor.Factory, ExPropertyEditor {
    private static final String [] REGEX_NONE = new String [] {"None", ""};
    private static final String [] REGEX_CISCO_PORT_NAMING = new String [] {"Cisco Port Naming", "[/?[0-9]+]*/0$"};

    private final InplaceEditor inplaceEditor;
    
    public ShapeNamePropertyEditor() {
        
        inplaceEditor = new InplaceEditor() {
            private final JComboBox cboName;
            private PropertyModel model;
            private PropertyEditor editor;
            
            {
                cboName = new JComboBox();
                cboName.setEditable(true);
                
                cboName.addItem(REGEX_NONE[0]);
                cboName.addItem(REGEX_CISCO_PORT_NAMING[0]);
            }

            @Override
            public void connect(PropertyEditor pe, PropertyEnv env) {
                editor = pe;
                reset();
            }

            @Override
            public JComponent getComponent() {
                return cboName;
            }

            @Override
            public void clear() {
                editor = null;
                model = null;
            }

            @Override
            public Object getValue() {
                return (String) cboName.getSelectedItem();
            }

            @Override
            public void setValue(Object o) {
                cboName.setSelectedItem(o);
            }

            @Override
            public boolean supportsTextEntry() {
                return true;
            }

            @Override
            public void reset() {
                String name = (String) editor.getValue();
                if (name != null)
                    cboName.setSelectedItem(name);
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
                return c == cboName || cboName.isAncestorOf(c);
            }


        };
        
    }
    
    @Override
    public void setAsText(String text){
        setValue(getRegex(text));
    }
    
    private String getRegex(String text) {
        if (text.equals(REGEX_NONE[0]))
            return REGEX_NONE[1];
        else if (text.equals(REGEX_CISCO_PORT_NAMING[0]))
            return REGEX_CISCO_PORT_NAMING[1];
        else
            return text;
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
