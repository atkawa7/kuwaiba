/**
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.contacts;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;
import org.netbeans.swing.etable.ETable;

/**
 * A table that display information about contacts
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ContactsTable extends ETable {

    public ContactsTable() {
        this.setColumnSelectionAllowed(false);

        final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK, false);
        //Credits to https://pugnodifagioli.wordpress.com/2008/02/19/how-to-implement-the-copypaste-for-a-jtable/
        this.registerKeyboardAction((e) -> {
            int col = getSelectedColumn();
            int row = getSelectedRow();
            if (col != -1 && row != -1) {
                Object value = getValueAt(row, col);
                String data = value == null ? "" : value.toString();
                final StringSelection selection = new StringSelection(data);     
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
            }
        }, "Copy Cell Contents", stroke, JComponent.WHEN_FOCUSED);
    }
    
    public ContactsTable(TableModel model) {
        this();
        setModel(model);
    }
}
