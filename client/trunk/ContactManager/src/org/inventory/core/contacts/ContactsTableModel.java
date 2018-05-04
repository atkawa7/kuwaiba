/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package org.inventory.core.contacts;

import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.inventory.communications.core.LocalContactLight;

/**
 * The table model associated to the contacts table
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ContactsTableModel  implements TableModel {
    private List<LocalContactLight> currentContactList;

    public ContactsTableModel() {
        this.currentContactList = new ArrayList<>();
    }
    
    
    public ContactsTableModel(List<LocalContactLight> currentContactList) {
        this.currentContactList = currentContactList;
    }

    public List<LocalContactLight> getCurrentContactList() {
        return currentContactList;
    }

    public void setCurrentContactList(List<LocalContactLight> currentContactList) {
        this.currentContactList = currentContactList;
    }
    
    @Override
    public int getRowCount() {
        return currentContactList.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Name";
            case 1:
                return "Contact Type";
            case 2:
                return "Company";
            default:
                return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case -1: //This refers to the object ifself. It's used in the table selection events to update the TC lookup
                return currentContactList.get(rowIndex);
            case 0:
                return currentContactList.get(rowIndex).getName();
            case 1:
                return currentContactList.get(rowIndex).getClassName();
            case 2:
                return currentContactList.get(rowIndex).getCustomer().getName();
            default:
                return "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) { 
        this.currentContactList.clear();
        this.currentContactList.addAll((List<LocalContactLight>)aValue);
    }

    @Override
    public void addTableModelListener(TableModelListener l) { }

    @Override
    public void removeTableModelListener(TableModelListener l) { }
    
}
