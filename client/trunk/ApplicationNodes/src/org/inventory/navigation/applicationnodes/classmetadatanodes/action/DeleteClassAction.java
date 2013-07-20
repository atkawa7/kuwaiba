/*
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.classmetadatanodes.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadata;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.classmetadatanodes.ClassMetadataNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Action to delete a class metadata
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class DeleteClassAction extends AbstractAction {

    private Node node;
    private CommunicationsStub com;

    public DeleteClassAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_CLASS"));
        com = CommunicationsStub.getInstance();
    }

    public DeleteClassAction(ClassMetadataNode node) {
        this();
        this.node = node;
    }
        
    @Override
    public void actionPerformed(ActionEvent ae) {
        LocalClassMetadata classMetaData = com.getMetaForClass(((JMenuItem)ae.getSource()).getName(), false);
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        if(classMetaData.isCustom()){
            if (com.deleteClassMetadata(classMetaData.getOid()))
                nu.showSimplePopup("Operation Result", NotificationUtil.INFO, "The class was deleted successfully");
            else
                nu.showSimplePopup("Operation Result", NotificationUtil.ERROR, com.getError());
        }else
            nu.showSimplePopup("Operation Result", NotificationUtil.ERROR, "Core classes can not be deleted");
    }
}
