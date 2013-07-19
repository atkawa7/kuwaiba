/*
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
package org.inventory.navigation.applicationnodes.attributemetadatanodes.action;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.inventory.communications.CommunicationsStub;
import org.inventory.navigation.applicationnodes.classmetadatanodes.ClassMetadataNode;
import org.openide.nodes.Node;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class DeleteAttributeMetadataAction extends AbstractAction implements Presenter.Popup{

    private Node node;
    private CommunicationsStub com;

    public DeleteAttributeMetadataAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_ATTRIBUTE"));
        com = CommunicationsStub.getInstance();
    }

    public DeleteAttributeMetadataAction(Node node) {
        this();
        this.node = node;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae) {
        //TODO
    }

    @Override
    public JMenuItem getPopupPresenter() {
         JMenuItem smiChildren = new JMenuItem(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_ATTRIBUTE"));
        smiChildren.setName(((ClassMetadataNode)node).getName());
        smiChildren.addActionListener(this);
                
        return smiChildren;
    }
    
}
