/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.core.services.api.nodes;

import com.toedter.calendar.JDateChooser;
import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

/**
 * Property support for dates. Mostly as seen on https://platform.netbeans.org/tutorials/nbm-property-editors.html#inplace-editor
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class DatePickerPropertyEditorSupport extends PropertyEditorSupport implements InplaceEditor.Factory, ExPropertyEditor {
    private InplaceEditor editor;
    private static final SimpleDateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");

    public DatePickerPropertyEditorSupport(Date date) {
        editor = new DatePickerPropertyEditor(date);
    }
    
    @Override
    public InplaceEditor getInplaceEditor() {           
        return editor;
    }    

    @Override
    public String getAsText() {
        if (getInplaceEditor().getValue() == null)
            return null;

        return formatter.format(editor.getValue());
    }    

    @Override
    public void attachEnv(PropertyEnv env) {
        env.registerInplaceEditorFactory(this);
    }

    public class DatePickerPropertyEditor implements InplaceEditor {
        private final JDateChooser datePicker = new JDateChooser();
        private PropertyModel model;
        private PropertyEditor editor;

        public DatePickerPropertyEditor(Date date) {
            datePicker.setDate(date);
        }

        @Override
        public void connect(PropertyEditor propertyEditor, PropertyEnv env) {
            editor = propertyEditor;
            reset();
        }

        @Override
        public JComponent getComponent() {
            return datePicker;
        }

        @Override
        public void clear() {
            editor = null;
            model = null;
        }

        @Override
        public Object getValue() {
            return datePicker.getDate();
        }

        @Override
        public void setValue(Object object) {
            datePicker.setDate((Date) object);
        }

        @Override
        public boolean supportsTextEntry() {
            return false;
        }

        @Override
        public void reset() {
            Date date = (Date) editor.getValue();
            if (date != null) 
                datePicker.setDate(date);
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
            return component == datePicker || datePicker.isAncestorOf(component);
        }

        @Override
        public void addActionListener(ActionListener actionListener) { /*Not used*/ }

        @Override
        public void removeActionListener(ActionListener actionListener) { /*Not used*/ }    
    }
}
