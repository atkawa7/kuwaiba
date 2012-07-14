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
package org.inventory.navigation.applicationnodes.objectnodes.actions;

import org.inventory.core.services.utils.MenuScroller;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectChildren;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.RootObjectNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter.Popup;


public final class CreateBusinessObjectAction extends AbstractAction implements Popup{
    private Node node;
    private CommunicationsStub com;

    public CreateBusinessObjectAction(){
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW"));
        com = CommunicationsStub.getInstance();
    }

    public CreateBusinessObjectAction(ObjectNode _node) {
        this();
        this.node = _node;
    }

    public CreateBusinessObjectAction (RootObjectNode _ron){
        this();
        node = _ron;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        LocalObjectLight myLol = CommunicationsStub.getInstance().createObject(
                ((JMenuItem)ev.getSource()).getName(),
                (node instanceof RootObjectNode) ? null : (((ObjectNode)node)).getObject().getClassName(),
                (node instanceof RootObjectNode) ? null : (((ObjectNode)node)).getObject().getOid(), null);
        if (myLol == null)
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.ERROR,
                    CommunicationsStub.getInstance().getError());
        else{
            ((ObjectChildren)node.getChildren()).add(new ObjectNode[]{new ObjectNode(myLol)});
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.INFO,
                    java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATED"));
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW"));

        List<LocalClassMetadataLight> items;
        if (node instanceof RootObjectNode) //For the root node
            items = CommunicationsStub.getInstance().getPossibleChildren(null, false);
        else
            items = CommunicationsStub.getInstance().
                    getPossibleChildren(((ObjectNode)node).getObject().getClassName(),false);

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