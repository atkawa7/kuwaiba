/**
 * Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.navigation.applicationnodes.pools.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.MenuScroller;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.pools.PoolNode;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * Creates a new element in a pool
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class NewPoolItemAction extends AbstractAction implements Presenter.Popup{
    private PoolNode poolNode;
    private CommunicationsStub com;

    public NewPoolItemAction(PoolNode node) {
        this.poolNode = node;
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW"));
        com = CommunicationsStub.getInstance();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        LocalObjectLight newObject = com.createPoolItem(poolNode.getPool().getOid(), ((JMenuItem)e.getSource()).getName());
        if (newObject == null)
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATION_ERROR"), NotificationUtil.ERROR, com.getError());
        else{
            poolNode.getChildren().add(new ObjectNode[]{new ObjectNode(newObject)});
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.INFO, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATED"));
        }
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW"));

        List<LocalClassMetadataLight> items;
        items = com.getLightSubclasses(poolNode.getPool().getClassName(), false, true);

            if (items.isEmpty())
                mnuPossibleChildren.setEnabled(false);
            else
                for(LocalClassMetadataLight item: items){
                        JMenuItem smiChildren = new JMenuItem(item.getClassName());
                        smiChildren.setName(item.getClassName());
                        smiChildren.addActionListener(this);
                        mnuPossibleChildren.add(smiChildren);
                }

        MenuScroller.setScrollerFor(mnuPossibleChildren, 20, 100);
		
        return mnuPossibleChildren;
    }
}
