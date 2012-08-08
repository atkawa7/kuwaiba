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
package org.inventory.navigation.applicationnodes.objectnodes.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;

/**
 * Gets the selected object oid
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class ShowObjectIdAction extends AbstractAction{
    private ObjectNode node;

    public ShowObjectIdAction(ObjectNode _node) {
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_SHOW_OBJECT_ID_ACTION"));
        this.node = _node;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        JOptionPane.showMessageDialog(null, 
                new SelectableLabel(node.getObject().getOid() + " ["+node.getObject().getClassName()+"]"), //NOI18N
                java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_SHOW_OBJECT_ID_ACTION_TITLE"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private class SelectableLabel extends JTextField{

        public SelectableLabel(String text) {
            super(text);
            setOpaque(true);
            setEditable(false);
            setBorder(null);
        }

    }
}