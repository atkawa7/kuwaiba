/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.usermanager.nodes.properties;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.inventory.communications.core.LocalUserObject;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.nodes.PropertySupport;


/**
 * The user enabled property
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class PropertyUserType extends PropertySupport.ReadWrite<Integer>{
    private LocalUserObject user;
    public PropertyUserType(LocalUserObject user) {
        super("type", Integer.class, "Type", "User's type");
        this.user = user;
    }

    @Override
    public Integer getValue() throws IllegalAccessException, InvocationTargetException {
        return user.getType();
    }

    @Override
    public void setValue(Integer val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        this.user.setType(val);
    }

//    @Override
//    public PropertyEditor getPropertyEditor() {
//        return new PropertyUserTypeEditorSupport();
//    }
//    
    
//    
//    private class PropertyUserTypeEditorSupport extends PropertyEditorSupport implements ExPropertyEditor, InplaceEditor.Factory {
//
//        @Override
//        public void attachEnv(PropertyEnv env) {
//            env.registerInplaceEditorFactory(this);
//        }
//
//        @Override
//        public InplaceEditor getInplaceEditor() {
//            return new InplaceEditor() {
//
//                @Override
//                public void connect(PropertyEditor pe, PropertyEnv env) {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//
//                @Override
//                public JComponent getComponent() {
//                    return new JComboBox(new String[] { "sdsa", "khjkhkhj", "ououiuou"});
//                }
//
//                @Override
//                public void clear() {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//
//                @Override
//                public Object getValue() {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//
//                @Override
//                public void setValue(Object o) {
//                    System.out.println("Fijamos el valor");
//                }
//
//                @Override
//                public boolean supportsTextEntry() {
//                    return false;
//                }
//
//                @Override
//                public void reset() {
//                    System.out.println("Reseteando");
//                }
//
//                @Override
//                public void addActionListener(ActionListener al) {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//
//                @Override
//                public void removeActionListener(ActionListener al) {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//
//                @Override
//                public KeyStroke[] getKeyStrokes() {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//
//                @Override
//                public PropertyEditor getPropertyEditor() {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//
//                @Override
//                public PropertyModel getPropertyModel() {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//
//                @Override
//                public void setPropertyModel(PropertyModel pm) {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//
//                @Override
//                public boolean isKnownComponent(Component c) {
//                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//                }
//            };
//        }
//    }

}
