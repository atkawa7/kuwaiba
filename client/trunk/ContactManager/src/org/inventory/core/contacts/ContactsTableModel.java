/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalContact;

/**
 * The table model associated to the contacts table. Although a LocalContact object will contain ALL the 
 * attributes corresponding to the contact class, this table will only show all that are common across the contact classes,
 * that is, the attributes of their superclass, GenericContact
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ContactsTableModel  implements TableModel {
    private List<LocalContact> currentContactList;
    private LocalClassMetadata genericContactMetadata;

    public ContactsTableModel() throws ConnectException {
        this.currentContactList = new ArrayList<>();
        this.genericContactMetadata = CommunicationsStub.getInstance().getMetaForClass("GenericContact", false); //NOI18N
        
        if (this.genericContactMetadata == null)
            throw new ConnectException(CommunicationsStub.getInstance().getError());
    }
    
    
    public ContactsTableModel(List<LocalContact> currentContactList) throws ConnectException {
        this.currentContactList = currentContactList;
        this.genericContactMetadata = CommunicationsStub.getInstance().getMetaForClass("GenericContact", false); //NOI18N
        
        if (this.genericContactMetadata == null)
            throw new ConnectException(CommunicationsStub.getInstance().getError());
    }

    public List<LocalContact> getCurrentContactList() {
        return currentContactList;
    }

    public void setCurrentContactList(List<LocalContact> currentContactList) {
        this.currentContactList = currentContactList;
    }
    
    @Override
    public int getRowCount() {
        return currentContactList.size();
    }

    @Override
    public int getColumnCount() {
        return genericContactMetadata.getAttributes().size() + 2; // +1 = company, +1 = type
     }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "company";
            case 1:
                return "type";
            default:
                return genericContactMetadata.getAttributes().get(columnIndex - 2).getDisplayName();
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
            case -1: //This refers to the object itself. It's used in the table selection events to update the TC lookup
                return currentContactList.get(rowIndex);
            case 0:
                return currentContactList.get(rowIndex).getCustomer().getName();
            case 1:
                return currentContactList.get(rowIndex).getClassName();
            default:
                return currentContactList.get(rowIndex).getAttribute(getColumnName(columnIndex)) != null ?
                        currentContactList.get(rowIndex).getAttribute(getColumnName(columnIndex)).toString() : "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) { 
        this.currentContactList.clear();
        this.currentContactList.addAll((List<LocalContact>)aValue);
    }
    
    public String[] getColumnNames() {
        String[] columnNames = new String[getColumnCount()];
        
        for (int i = 0; i < getColumnCount(); i++)
            columnNames[i] = getColumnName(i);
        
        return columnNames;
    }
    
    /**
     * Returns the unique (non-repeated) values in a table column
     * @param columnIndex The index of the column
     * @return The list of non-repeated values
     */
    public String[] collectColumnValuesForColumn(int columnIndex) {
        List<String> valuesAsList = new ArrayList<>();
        
        for (int i = 0; i < getRowCount(); i++) {
            String valueAtCell = (String)getValueAt(i, columnIndex);
            if (!valuesAsList.contains(valueAtCell))
                valuesAsList.add(valueAtCell);
        }
        
        return valuesAsList.toArray(new String[0]);
    }

    @Override
    public void addTableModelListener(TableModelListener l) { }

    @Override
    public void removeTableModelListener(TableModelListener l) { }
    
}
