/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.views.objectview;

import java.awt.event.ActionEvent;
import org.inventory.core.services.i18n.I18N;
import static org.inventory.navigation.navigationtree.nodes.actions.DeleteBusinessObjectAction.ACTION_MAP_KEY;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallbackSystemAction;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RefreshWindowAction extends CallbackSystemAction {
    public static String ACTION_MAP_KEY = "RefreshWindowAction"; //NOI18N
    
    public RefreshWindowAction() {
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        int i = 0;
    }

    @Override
    public String getName() {
        return I18N.gm("refresh"); //NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(I18N.gm("refresh")); //NOI18N
    }
    
    @Override
    protected void initialize() {
        super.initialize();
    }

    @Override
    public Object getActionMapKey() {
        return ACTION_MAP_KEY;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
