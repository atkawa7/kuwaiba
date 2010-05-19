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
 *  under the License.
 */
package org.inventory.navigation.navigationtree.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.RootObjectNode;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter.Popup;


public final class Create extends AbstractAction implements Popup{
    private LocalObjectLight lol;
    private ObjectNode node;
    private RootObjectNode ron;
    private CommunicationsStub com;

    public Create(){
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_NEW"));
        com = CommunicationsStub.getInstance();
    }

    public Create(LocalObjectLight _lol, ObjectNode _node) {
        this();
        this.lol=_lol;
        this.node = _node;
        this.ron = null;
    }

    public Create (RootObjectNode _ron){
        this();
        this.ron = _ron;
        this.lol=null;
    }

    public void actionPerformed(ActionEvent ev) {
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        LocalObjectLight myLol = CommunicationsStub.getInstance().createObject(
                ((JMenuItem)ev.getSource()).getName(),
                (lol==null)?(com.getRootId()):lol.getOid(),
                null);
        if (myLol == null)
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.ERROR,
                    CommunicationsStub.getInstance().getError());
        else{
            firePropertyChange(NAME, "add", myLol);
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.INFO,
                    java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_CREATED"));
        }
    }

    public JMenuItem getPopupPresenter() {
        JMenu mnuPossibleChildren = new JMenu(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_NEW"));

        List<LocalClassMetadataLight> items;
        if (this.lol == null) //For the root node
            items = CommunicationsStub.getInstance().getRootPossibleChildren();
        else
            items = CommunicationsStub.getInstance().
                    getPossibleChildren(lol.getPackageName()+"."+lol.getClassName());

        if (items.size() == 0) mnuPossibleChildren.setEnabled(false);
            else
                for(LocalClassMetadataLight item: items){
                    JMenuItem smiChildren = new JMenuItem(item.getClassName());
                    smiChildren.setName(item.getPackageName()+"."+item.getClassName());
                    smiChildren.addActionListener(this);
                    mnuPossibleChildren.add(smiChildren);
                }
        return mnuPossibleChildren;
    }
}