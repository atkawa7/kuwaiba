/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.navigationtree.nodes.actions;

import java.awt.event.ActionEvent;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.JMenuItem;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.util.ImageUtilities;
import org.openide.util.actions.Presenter;

/**
 * Refreshes the node's domain object related info and its children
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class UpdateNodeAction extends GenericObjectNodeAction implements Presenter.Popup {
    private static UpdateNodeAction instance;
    private ObjectNode node;

    public UpdateNodeAction() {
        putValue(NAME, "Update");
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon("org/inventory/navigation/navigationtree/res/update_node.png", false));
    }
    
    public static UpdateNodeAction getInstance(ObjectNode node) {
        if (instance == null)
            instance = new UpdateNodeAction();
        instance.setNode(node);
        return instance;
    }
    
    public void setNode(ObjectNode node) {
        this.node = node;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        node.refresh();
    }

    @Override
    public String[] getValidators() {
        return null; //Enable this action for any object
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new JMenuItem(this);
    }

    @Override
    public String[] appliesTo() {
        return null; //Enable this action for any object
    }
    
    @Override
    public int numberOfNodes() {
        return 1;
    }
}