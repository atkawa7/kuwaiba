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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JDialog;
import org.inventory.communications.CommunicationsStub;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInstall;

/**
 * This installer shows the login window
 * TODO: Inject the user profile into the global lookup
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
                if (e.getSource() == DialogDescriptor.OK_OPTION)
                        connect();
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
        DialogDisplayer.getDefault().notifyLater(dd);
    }

    public void connect(){
        ConnectionSettingsPanel containedPanel = pnlAuthentication.getContainedPanel();
        try {
            CommunicationsStub.setServerURL(
                    new URL("http", containedPanel.getServerAddress() , containedPanel.getServerPort(),
                    containedPanel.getWSDLPath()));
        } catch (MalformedURLException ex) {
            showExceptions("Malformed URL: "+ex.getMessage());
        }
        try{
            if (!CommunicationsStub.getInstance().createSession(pnlAuthentication.getTxtUser().getText(), new String(pnlAuthentication.getTxtPassword().getPassword())))
               showMeAgain(CommunicationsStub.getInstance().getError(),
                       pnlAuthentication.getTxtUser().getText(),
                       containedPanel.getTxtServerAddress().getText(),
                       containedPanel.getTxtServerPort().getText(),
                       containedPanel.getTxtWSDLPath().getText());
        }catch(Exception exp){
            CommunicationsStub.resetInstance();
            showMeAgain(exp.getMessage(),
                       pnlAuthentication.getTxtUser().getText(),
                       containedPanel.getTxtServerAddress().getText(),
                       containedPanel.getTxtServerPort().getText(),
                       containedPanel.getTxtWSDLPath().getText());
        }
    }
    public void showMeAgain(String errorText, String user, String serverAddress, String serverPort, String WSDLPath){
        showExceptions(errorText);

        JDialog dialog = (JDialog)DialogDisplayer.getDefault().createDialog(dd);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setVisible(true);
    }

    private void showExceptions(String errorText){
        if (errorText == null)
            errorText = "Unknown Error";
        //If the message is too long we assigned to the tooltiptext
        if (errorText.length() > 70){
            pnlAuthentication.getLblDetails().setToolTipText(errorText);
            pnlAuthentication.getLblDetails().setVisible(true);
            pnlAuthentication.getLblError().setText(errorText.substring(0, 50)+"..."); //NOI18n
        }else{
            pnlAuthentication.getLblError().setText(errorText);
            pnlAuthentication.getLblDetails().setVisible(false);
        }
        pnlAuthentication.getLblError().setVisible(true);
    }

    @Override
    public boolean closing() {
        CommunicationsStub.getInstance().closeSession();
        return true;
    }

}
