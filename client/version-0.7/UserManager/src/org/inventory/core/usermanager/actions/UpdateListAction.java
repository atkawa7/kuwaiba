/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.usermanager.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JTabbedPane;
import org.inventory.core.usermanager.UserManagerService;

/**
 * This action updates the user or group list
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class UpdateListAction extends AbstractAction{

    /**
     * Reference to the tabbed panel just to know which tab is focused
     */
    private JTabbedPane tabbedPanel;
    /**
     * Reference to the UserManagerService, so it can update the UI
     */
    private UserManagerService ums;

    public UpdateListAction(JTabbedPane _tabbedPane, UserManagerService _ums){
        this.tabbedPanel = _tabbedPane;
        this.ums = _ums;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch(tabbedPanel.getSelectedIndex()){
            case 0:
                //The users list is focused
                ums.populateUsersList();
                break;
            case 1:
                //The groups list is focused
                ums.populateGroupsList();
                break;
        }
    }

}
