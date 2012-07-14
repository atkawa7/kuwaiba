/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.core.usermanager.nodes.customeditor;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditorSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.usermanager.nodes.properties.UserProperty;
import org.openide.explorer.propertysheet.ExPropertyEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * This is the editor used for changing the password
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class PasswordEditorSupport extends PropertyEditorSupport
    implements ExPropertyEditor, VetoableChangeListener{

    /**
     * The panel shown in the editor
     */
    private ChangePasswordPanel myPanel = null;
    /**
     * A reference to the notification mechanism
     */
    private NotificationUtil nu;

    /**
     * PropertyEnv instance
     */
    private PropertyEnv env;

    /**
     * Reference to the UserProperty
     */
    private UserProperty property;

    public PasswordEditorSupport(UserProperty _property){
        nu = Lookup.getDefault().lookup(NotificationUtil.class);
        this.property = _property;
    }

    @Override
    public Component getCustomEditor(){
        if (myPanel == null ){
            this.myPanel = new ChangePasswordPanel(env);
            env.addVetoableChangeListener(this);
            return myPanel;
        }else return myPanel;
    }

    @Override
    public boolean supportsCustomEditor(){
        return true;
    }

    @Override
    public String getAsText(){
        return "****";
    }

    @Override
    public void setValue(Object o){
        //Do nothing, because we set the password and make the validations in the vetoable event
    }
    
    @Override
    public void attachEnv(PropertyEnv pe) {
        env = pe;
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if(evt.getNewValue().equals(PropertyEnv.STATE_VALID)){
            //property.setPassword(String.valueOf(myPanel.getTxtPassword().getPassword()));
            try {
                //property.setPassword(String.valueOf(myPanel.getTxtPassword().getPassword()));
                property.setValue(String.valueOf(myPanel.getTxtPassword().getPassword()));
            } catch (IllegalAccessException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}