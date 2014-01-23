/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import org.inventory.communications.CommunicationsStub;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInstall;
import org.openide.windows.WindowManager;

/**
 * This installer shows the login window
 * TODO: Inject the user profile into the global lookup
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Installer extends ModuleInstall {
    private AuthenticationPanel pnlAuthentication;
    private DialogDescriptor dd;

    @Override
    public void restored() {
      pnlAuthentication = new AuthenticationPanel();
      dd = new DialogDescriptor(pnlAuthentication, "Login Window", true, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                //Surprisingly, pressing the "OK" button doesn't fire a property change, but
                //only an action event
                if (e.getSource() == DialogDescriptor.OK_OPTION){
                    //This is done instead of the older approach to prompt for the user credentials
                    //again in case of error (versions 0.3 beta and earlier). With the past approach, a new
                    //Dialog was created every time, creating a new window over the past ones
                    //With this approach, errors are painted in the same dialog, but we have to tell the
                    //descriptor to not close if something went wrong
                        if (connect())
                            dd.setClosingOptions(new Object[]{DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION, DialogDescriptor.CLOSED_OPTION});
                        else
                            dd.setClosingOptions(new Object[]{DialogDescriptor.CANCEL_OPTION, DialogDescriptor.CLOSED_OPTION});
                }
            }
        });
      dd.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if(evt.getNewValue() == DialogDescriptor.CANCEL_OPTION ||
                        evt.getNewValue()==DialogDescriptor.CLOSED_OPTION)
                    LifecycleManager.getDefault().exit();
            }
        });
        dd.setClosingOptions(new Object[]{DialogDescriptor.CANCEL_OPTION});
        DialogDisplayer.getDefault().notifyLater(dd);
    }

    public boolean connect(){
        ConnectionSettingsPanel containedPanel = pnlAuthentication.getContainedPanel();
        try {
            CommunicationsStub.setServerURL(
                    new URL("http", containedPanel.getServerAddress() , containedPanel.getServerPort(),
                    containedPanel.getWSDLPath()));

        } catch (MalformedURLException ex) {
            showExceptions("Malformed URL: "+ex.getMessage());
            return false;
        }
        try{
            if (!CommunicationsStub.getInstance().createSession(pnlAuthentication.getTxtUser().getText(), new String(pnlAuthentication.getTxtPassword().getPassword()))){
               showExceptions(CommunicationsStub.getInstance().getError());
               return false;
            }else
                //The title can't be set directly since it's overwritten right after the startup. We have to wait till the window is open
                WindowManager.getDefault().getMainWindow().addWindowListener(new WindowListener() {

                @Override
                public void windowOpened(WindowEvent e) {
                        WindowManager.getDefault().getMainWindow().setTitle(String.format("%1s - [%2s]",
                        WindowManager.getDefault().getMainWindow().getTitle(), CommunicationsStub.getInstance().getSession().getUsername()));
                }

                @Override
                public void windowClosing(WindowEvent e) {  }

                @Override
                public void windowClosed(WindowEvent e) { }

                @Override
                public void windowIconified(WindowEvent e) {  }

                @Override
                public void windowDeiconified(WindowEvent e) {  }

                @Override
                public void windowActivated(WindowEvent e) { }

                @Override
                public void windowDeactivated(WindowEvent e) { }
            });


        }catch(Exception exp){
            CommunicationsStub.resetInstance();
            showExceptions(exp.getMessage());
            return false;
        }
        return true;
    }

    private void showExceptions(String errorText){
        if (errorText == null)
            errorText = "Unknown Error";
        //If the message is too long we better display it on a JOPtionPane
        if (errorText.length() > 50)
            pnlAuthentication.getLblError().setText("Error connecting to the server. Click here for further details");
        else
            pnlAuthentication.getLblError().setText(errorText);

        pnlAuthentication.setDetailedError(errorText);
        pnlAuthentication.getLblError().setVisible(true);
    }

    @Override
    public boolean closing() {
        CommunicationsStub.getInstance().closeSession();
        return true;
    }

}
