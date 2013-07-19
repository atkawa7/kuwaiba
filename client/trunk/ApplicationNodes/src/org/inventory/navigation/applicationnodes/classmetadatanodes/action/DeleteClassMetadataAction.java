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
import org.inventory.navigation.applicationnodes.classmetadatanodes.ClassMetadataNode;
import org.openide.nodes.Node;
import org.openide.util.actions.Presenter.Popup;

/**
 * Action to delete a class metadata
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class DeleteClassMetadataAction extends AbstractAction implements Popup{

    private Node node;
    private CommunicationsStub com;

    public DeleteClassMetadataAction() {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_CLASS"));
        com = CommunicationsStub.getInstance();
    }

    public DeleteClassMetadataAction(ClassMetadataNode node) {
        this();
        this.node = node;
    }
        
    @Override
    public void actionPerformed(ActionEvent ae) {
        LocalClassMetadata classMetaData = com.getMetaForClass(((JMenuItem)ae.getSource()).getName(), false);
        if(classMetaData.isCustom()){
            com.deleteClassMetadata(classMetaData.getOid());
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        JMenuItem smiChildren = new JMenuItem(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_CLASS"));
        smiChildren.setName(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_DELETE_CLASS"));
        smiChildren.addActionListener(this);
        return smiChildren;
    }
    
}
