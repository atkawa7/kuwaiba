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
package org.inventory.core.usermanager.nodes.properties;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.core.LocalUserObject;
import org.inventory.communications.core.LocalUserObjectLight;
import org.openide.nodes.PropertySupport;

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
        //To avoid double sets, this is handled in the PropertyEditorSupport
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return new UserTypePropertyEditorSupport(this);
    }
    
    public static class UserTypePropertyEditorSupport extends PropertyEditorSupport {
        private PropertyUserType property;

        public UserTypePropertyEditorSupport(PropertyUserType property) {
            this.property = property;
        }
        
        @Override
        public String getAsText() {
            try {
                return property.getValue().getLabel();
            } catch (IllegalAccessException | InvocationTargetException ex) {return null;}
        }
        
        

        //setValue is never called (?), instead, this one is called 
        @Override
        public void setAsText(String text){
            for (LocalUserObjectLight.UserType userType : LocalUserObjectLight.UserType.DEFAULT_USER_TYPES) {
                if (userType.getLabel().equals(text)) {
                    try {
                        property.user.setType(userType.getType());
                    } catch (Exception ex) { } //Should never happen, however, if it does, do nothing
                }
            }
        }

        @Override
        public String[] getTags(){
            return new String[] { LocalUserObjectLight.UserType.DEFAULT_USER_TYPES[0].getLabel(),
                    LocalUserObjectLight.UserType.DEFAULT_USER_TYPES[1].getLabel(),
                    LocalUserObjectLight.UserType.DEFAULT_USER_TYPES[2].getLabel() };
        }

        @Override
        public boolean supportsCustomEditor() {
            return false;
        }
    }
}


