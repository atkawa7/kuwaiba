/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
import org.inventory.navigation.applicationnodes.listmanagernodes.ListElementChildren;
import org.inventory.navigation.applicationnodes.listmanagernodes.ListElementNode;
import org.inventory.navigation.applicationnodes.listmanagernodes.ListTypeNode;
import org.openide.util.Lookup;


public final class Create extends AbstractAction{
    private ListTypeNode node;
    private CommunicationsStub com;

    public Create(){
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW"));
        com = CommunicationsStub.getInstance();
    }

    public Create(ListTypeNode _node) {
        this();
        this.node = _node;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        LocalObjectLight myLol = com.createListType(node.getObject().getClassName());
            if (myLol == null)
                nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.ERROR,
                    CommunicationsStub.getInstance().getError());
        else{
            ((ListElementChildren)node.getChildren()).add(new ListElementNode[]{new ListElementNode(myLol)});
            //Refreshes the cache
            CommunicationsStub.getInstance().getList(node.getObject().getClassName(), true);
        }
    }
}