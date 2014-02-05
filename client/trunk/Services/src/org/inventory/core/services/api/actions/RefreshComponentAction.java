/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.services.api.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.openide.windows.WindowManager;


/**
 * Refreshes the focused component
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class RefreshComponentAction extends AbstractAction implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        Object activeTopComponent = WindowManager.getDefault().getRegistry().getActivated();
        if (activeTopComponent == null)
            return;
//        boolean refreshable = false;
//        //Ignore the TopComponent that doesn't implement the RefreshableTopComponent interface
//        for (Class intz : activeTopComponent.getClass().getInterfaces()){
//            if (intz.equals(RefreshableTopComponent.class)){
//                refreshable = true;
//                break;
//            }
//        }
//        if (refreshable)
        if (activeTopComponent instanceof Refreshable)
            ((Refreshable)activeTopComponent).refresh();
    }
}
