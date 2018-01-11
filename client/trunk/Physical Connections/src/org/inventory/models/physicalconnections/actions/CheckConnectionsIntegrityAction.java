/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.models.physicalconnections.actions;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.openide.util.lookup.ServiceProvider;

/**
 * This actions verifies that all connections inside a given object (not recursively) are well formed and have the proper parent
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ServiceProvider(service = GenericObjectNodeAction.class)
public class CheckConnectionsIntegrityAction extends GenericObjectNodeAction {

    public CheckConnectionsIntegrityAction() {
        putValue(NAME, I18N.gm("check_connections_integrity"));
    }
    
    @Override
    public String[] getValidators() {
        return null;
    }

    @Override
    public String[] appliesTo() {
        return new String[] { "ViewableObject" };
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_PHYSICAL_VIEW, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<LocalObjectLight> specialChildren = CommunicationsStub.getInstance().
                getSpecialChildrenOfClassLight(selectedObjects.get(0).getOid(), selectedObjects.get(0).getClassName(), Constants.CLASS_GENERICPHYSICALCONNECTION);
        
        if (specialChildren == null) 
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        else {
            //The connections with a loose end
            List<LocalObjectLight> ignored = new ArrayList<>();
            //The connections whose endpoints' common parent is different from its current parent object (which happens to be the currently selcted object)
            //The key of the hashmap is the actual parent, while the value is an array of connections that should be moved to said parent 
            HashMap<LocalObjectLight, List<LocalObjectLight>> toBeProcessed = new HashMap<>();
            
            //Now we make sure that the common parent of every connection endpoints is the right one, if not, we will try to move them
            for (LocalObjectLight specialChild : specialChildren) {
                
                //If any of the endpoints is loose, it might just not be connected, so we ignore it and go on
                List<LocalObjectLight> endpointA = CommunicationsStub.getInstance().
                        getSpecialAttribute(specialChild.getClassName(), specialChild.getOid(), "endpointA");
                if (endpointA == null || endpointA.isEmpty()) {
                    ignored.add(specialChild);
                    continue;
                } 
                
                List<LocalObjectLight> endpointB = CommunicationsStub.getInstance().
                        getSpecialAttribute(specialChild.getClassName(), specialChild.getOid(), "endpointB");
                if (endpointB == null || endpointB.isEmpty()) {
                    ignored.add(specialChild);
                    continue;
                }
                
                LocalObjectLight commonParent = CommunicationsStub.getInstance().getCommonParent(endpointA.get(0).getClassName(), endpointA.get(0).getOid(), 
                        endpointB.get(0).getClassName(), endpointB.get(0).getOid());
                
                if (commonParent == null) {
                    ignored.add(specialChild);
                    continue;
                }
                
                if (!selectedObjects.get(0).equals(commonParent)) {  //If the common parent is different, out it on a list and wait for user authorization to move it
                    if (toBeProcessed.containsKey(commonParent))
                        toBeProcessed.get(commonParent).add(specialChild);
                    else 
                        toBeProcessed.put(commonParent, new ArrayList<>(Arrays.asList(specialChild))); //Note that asList is not enough as it produces an inmutable list
                }
            }
            
            if (toBeProcessed.isEmpty())
                JOptionPane.showMessageDialog(null, "All connections seem to be consistent", I18N.gm("information"), JOptionPane.INFORMATION_MESSAGE);
            else {
                JPanel pnlConnectionsToBeprocessed = new JPanel(new BorderLayout(0, 10));
                pnlConnectionsToBeprocessed.add(new JLabel("The following connections should have another parent.\n Perhaps one of their endpoints was moved. Do you want to correct this issue?"), BorderLayout.NORTH);
                
                JList<LocalObjectLight> lstToBeProcessed = new JList<>();
                
                JScrollPane pnlScrollMain = new JScrollPane(lstToBeProcessed);
                
                pnlConnectionsToBeprocessed.add(pnlScrollMain);
                
                if (JOptionPane.showConfirmDialog(null, pnlConnectionsToBeprocessed, 
                        I18N.gm("information"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                    
                    for (LocalObjectLight commonParent : toBeProcessed.keySet()) {
                        if (CommunicationsStub.getInstance().moveSpecialObjects(
                                commonParent.getClassName(), commonParent.getOid(), toBeProcessed.get(commonParent)))
                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                                    NotificationUtil.INFO_MESSAGE, String.format("Connections sucessfully moved to %s", commonParent));
                        else
                            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    }
                    
                }
            }
        }
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }    
}
