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
import java.beans.PropertyEditorSupport;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.openide.util.Lookup;

/**
 * This is the editor used for changing the password
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class PasswordEditorSupport extends PropertyEditorSupport{

    /**
     * The panel shown in the editor
     */
    private ChangePasswordPanel myPanel;
    /**
     * A reference to the notification mechanism
     */
    private NotificationUtil nu;


    @Override
    public Component getCustomEditor(){
        this.myPanel = new ChangePasswordPanel();
        nu = Lookup.getDefault().lookup(NotificationUtil.class);
        return myPanel;
    }

    @Override
    public boolean supportsCustomEditor(){
        return true;
    }

    @Override
    public Object getValue(){
        return "****";
    }
}