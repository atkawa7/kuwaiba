/*
 *  Copyright 2010-2015, 2013 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.navigation.applicationnodes.classmetadatanodes.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalAttributeMetadata;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.communications.core.caching.Cache;
import org.inventory.navigation.applicationnodes.classmetadatanodes.ClassMetadataNode;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class DeleteAttributeAction extends AbstractAction implements Presenter.Popup{

    private ClassMetadataNode classNode;
    private CommunicationsStub com;

    public DeleteAttributeAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_ATTRIBUTE"));
        com = CommunicationsStub.getInstance();
    }

    public DeleteAttributeAction(ClassMetadataNode classNode) {
        this();
        this.classNode = classNode;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to perform this operation? All subclasses will be modified as well", 
                "Class metadata operation", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION){
            if (com.deleteAttribute(this.classNode.getClassMetadata().getOid(), ((JMenuItem)ae.getSource()).getName())){
                NotificationUtil.getInstance().showSimplePopup("Success", NotificationUtil.INFO_MESSAGE, "Attribute deleted successfully");
                //Force a cache reload
                Cache.getInstace().resetAll();
                //Refresh the class node
                classNode.refresh();
            }
            else
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu deleteAttributeMenu = new JMenu (java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_ATTRIBUTE"));
        LocalClassMetadata metaForThisClass = com.getMetaForClass(this.classNode.getClassMetadata().getOid(), false);
        
        if (metaForThisClass == null){
            deleteAttributeMenu.setEnabled(false);
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        }else{
            for (LocalAttributeMetadata anAttribute : metaForThisClass.getAttributes()){
                JMenuItem menuEntry = new JMenuItem(anAttribute.getName());
                menuEntry.setName(anAttribute.getName());
                menuEntry.addActionListener(this);
                deleteAttributeMenu.add (menuEntry);
            }
        }
                      
        return deleteAttributeMenu;
    }
    
}
