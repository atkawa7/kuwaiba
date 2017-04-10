/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.communications.core.LocalUserObjectLight;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;
import org.openide.nodes.PropertySupport;
import org.openide.util.Exceptions;

/**
 * The user type property
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */

public class PropertyUserType extends PropertySupport.ReadWrite<LocalUserObjectLight.UserType> {
    private LocalUserObject user;
    
    public PropertyUserType(LocalUserObject user) {
        super("type", LocalUserObjectLight.UserType.class, "Type", "How this user is going to access the system");
        this.user = user;
    }
    
    @Override
    public LocalUserObjectLight.UserType getValue() throws IllegalAccessException, InvocationTargetException {
        return LocalUserObjectLight.UserType.getDefaultUserTypeForRawType(user.getType());
    }

    @Override
    public void setValue(LocalUserObjectLight.UserType val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        user.setType(val.getType());
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new UserTypePropertyEditorSupport(this);
    }
    
    public static class UserTypePropertyEditorSupport extends PropertyEditorSupport {
        PropertyUserType property;

        public UserTypePropertyEditorSupport(PropertyUserType property) {
            this.property = property;
        }
        
        @Override
        public Object getValue() {
            try {
                return property.getValue();
            } catch (Exception ex) {return null;}
        }

        @Override
        public void setValue(Object value) {
            try {
                property.setValue((LocalUserObjectLight.UserType)value); //To change body of generated methods, choose Tools | Templates.
            } catch (Exception e) {
            }
        }
        
        

        @Override
        public void setAsText(String text){
            //throw new RuntimeException("sdsdafsfsdfsd");
        }

        @Override
        public String[] getTags(){
            return new String[] { LocalUserObjectLight.UserType.DEFAULT_USER_TYPES[0].getLabel(),
                    LocalUserObjectLight.UserType.DEFAULT_USER_TYPES[1].getLabel(),
                    LocalUserObjectLight.UserType.DEFAULT_USER_TYPES[2].getLabel() };
        }

        @Override
        public boolean supportsCustomEditor(){
            return false;
        }
    }
        
}


