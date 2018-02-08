/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.navigation.navigationtree.nodes.properties;

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
 * Property support for list types. Mostly as seen on https://platform.netbeans.org/tutorials/nbm-property-editors.html#inplace-editor
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 * @param <T> The kind of objects in the drop down list
 */
public class ComboBoxPropertyEditorSupport<T> extends PropertyEditorSupport implements InplaceEditor.Factory, ExPropertyEditor {
    private InplaceEditor editor;

    public ComboBoxPropertyEditorSupport(T[] tags) {
        editor = new ComboBoxPropertyEditor<>(tags);
    }

    @Override
    public void setValue(Object value) {
        editor.setValue(value);
        super.setValue(value);
    }
    
    @Override
    public InplaceEditor getInplaceEditor() {           
        return editor;
    }    

    @Override
    public String getAsText() {
        if (getInplaceEditor().getValue() == null)
            return null;

        return String.valueOf(editor.getValue());
    }    

    @Override
    public void attachEnv(PropertyEnv env) {
        env.registerInplaceEditorFactory(this);
    }

    public class ComboBoxPropertyEditor<T> implements InplaceEditor {
        private final JComboBox<T> comboBox;
        private PropertyModel model;
        private PropertyEditor editor;

        public ComboBoxPropertyEditor(T[] tags) {
            comboBox = new JComboBox(tags);
        }

        @Override
        public void connect(PropertyEditor propertyEditor, PropertyEnv env) {
            editor = propertyEditor;
            reset();
        }

        @Override
        public JComponent getComponent() {
            return comboBox;
        }

        @Override
        public void clear() {
            editor = null;
            model = null;
        }

        @Override
        public Object getValue() {
            return comboBox.getSelectedItem();
        }

        @Override
        public void setValue(Object object) {
            comboBox.setSelectedItem(object);
        }

        @Override
        public boolean supportsTextEntry() {
            return false;
        }

        @Override
        public void reset() {
            T selectedObject = (T) editor.getValue();
            if (selectedObject != null) 
                comboBox.setSelectedItem(selectedObject);
        }

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
        public void setPropertyModel(PropertyModel propertyModel) {
            this.model = propertyModel;
        }

        @Override
        public boolean isKnownComponent(Component component) {
            return component == comboBox || comboBox.isAncestorOf(component);
        }

        @Override
        public void addActionListener(ActionListener actionListener) { }

        @Override
        public void removeActionListener(ActionListener actionListener) { }    
    }
}
