/**
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.applicationnodes.metadataclassnodes.action;

import java.awt.event.ActionEvent;
import java.util.Random;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.metadataclassnodes.ClassMetadataNode;
import org.inventory.navigation.applicationnodes.metadataclassnodes.RootClassMetadataNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter.Popup;

/**
 * Action that requests a metadata class creation
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CreateClassMetadataAction extends AbstractAction implements Popup{
    
    private Node node;
    private CommunicationsStub com;

    public CreateClassMetadataAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW"));
        com = CommunicationsStub.getInstance();
    }

    public CreateClassMetadataAction(ClassMetadataNode node) {
        this();
        this.node = node;
    }

    public CreateClassMetadataAction(RootClassMetadataNode node) {
        this();
        this.node = node;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        Random random = new Random();
        NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
        boolean createClassMetadata = com.createClassMetadata(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW_CLASS")+random.nextInt(10000), 
                                                              "","", ((JMenuItem)ae.getSource()).getName(), true, false, 0, false, true);
        if (!createClassMetadata){
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.ERROR,
                    com.getError());
        }
        else{
            nu.showSimplePopup(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATION_TITLE"), NotificationUtil.INFO,
                    java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_CREATED"));
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem smiChildren = new JMenuItem(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_NEW_CLASS"));
        smiChildren.setName(((ClassMetadataNode)node).getName());
        smiChildren.addActionListener(this);
                
        return smiChildren;
    }
 }
