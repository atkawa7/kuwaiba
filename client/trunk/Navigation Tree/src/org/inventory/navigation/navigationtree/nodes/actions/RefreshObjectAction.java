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
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;

/**
 * Refreshes the node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class RefreshObjectAction extends GenericObjectNodeAction {
    private static RefreshObjectAction instance;
    private ObjectNode node;

    public RefreshObjectAction() {
        putValue(NAME, "Update");
    }
    
    public static RefreshObjectAction getInstance(ObjectNode node) {
        if (instance == null)
            instance = new RefreshObjectAction();
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
    public String getValidator() {
        return null;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_NAVIGATION_TREE, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}