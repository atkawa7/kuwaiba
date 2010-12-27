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
package org.inventory.navigation.applicationnodes.objectnodes.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.inventory.navigation.applicationnodes.objectnodes.windows.ObjectEditorTopComponent;
import org.openide.nodes.Node;

/*
 * Provides the necessary functionality to show a dedicated editor (using PropertySheetView)
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public final class Edit extends AbstractAction {
    private Node node;

    public Edit(Node _node) {
        this.node = _node;
        putValue(NAME, java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_EDIT"));
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E,InputEvent.CTRL_MASK));
        putValue(MNEMONIC_KEY,KeyEvent.VK_E);
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        ObjectEditorTopComponent component = new ObjectEditorTopComponent(new Node[]{node});
        component.open();
        component.requestActive();
    }
}