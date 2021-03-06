/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.listmanagernodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.listmanagernodes.ListTypeItemChildren;
import org.inventory.navigation.applicationnodes.listmanagernodes.ListTypeItemNode;
import org.inventory.navigation.applicationnodes.listmanagernodes.ListTypeNode;
import org.openide.util.Lookup;

/**
 * Action to create a new list type item
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class CreateListTypeAction extends AbstractAction{
    private ListTypeNode node;
    private CommunicationsStub com;

    public CreateListTypeAction(){
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW"));
        com = CommunicationsStub.getInstance();
    }

    public CreateListTypeAction(ListTypeNode _node) {
        this();
        this.node = _node;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        LocalObjectLight myLol = com.createListTypeItem(node.getObject().getClassName());
            if (myLol == null)
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.ERROR,
                    CommunicationsStub.getInstance().getError());
        else{
            ((ListTypeItemChildren)node.getChildren()).add(new ListTypeItemNode[]{new ListTypeItemNode(myLol)});
            //Refreshes the cache
            /**
             * TODO: Provide a method only for this instead of calling the standard
             */
            CommunicationsStub.getInstance().getList(node.getObject().getClassName(), false, true);
        }
    }
}