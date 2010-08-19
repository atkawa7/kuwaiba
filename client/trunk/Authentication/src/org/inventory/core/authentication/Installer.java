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
package org.inventory.core.authentication;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.modules.ModuleInstall;

/**
 * This installer shows the login window
 * TODO: Inject the user profile into the global lookup
 */
public class Installer extends ModuleInstall {

    @Override
    public void restored() {

        //This is a workaround. See http://old.nabble.com/DialogDisplayer-td15197060.html
        //NotifyDisplayer doesn't allow to disable the close button, son you can easily bypass
        //the login form. DialogDisplayer is much more versatile, but has a downside: stops thread execution
        //until it gets the user input, that means it won't let all modules to get loaded, so you don't know
        //if the Communications module is available to begin the auth process. My solution was to show it in another thread
        //while everything else is loaded and then show the modals till the credentials are valid.
        //Please note that showMeAgain at AuthenticationPanel doesn't use this trick
        SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    AuthenticationPanel pnlAuthentication = new AuthenticationPanel();
                    DialogDescriptor dd = new DialogDescriptor(pnlAuthentication, "Login Window", true, pnlAuthentication.getOptions(), null, DialogDescriptor.BOTTOM_ALIGN, null, null);
                    JDialog dialog = (JDialog) DialogDisplayer.getDefault().createDialog(dd);
                    dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
                    dialog.setVisible(true);
                }
            });        
    }
}
