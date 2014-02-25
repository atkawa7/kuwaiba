/*
 * Copyright (c) 2014 gir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    gir - initial API and implementation and/or initial documentation
 */
package org.inventory.core.history;

import java.util.Date;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.inventory.communications.core.LocalApplicationLogEntry;

/**
 *
 * @author gir
 */
public class AuditTrailTableModel implements TableModel {
    private String columnNames[] =  new String[]{"Timestamp", "Type", "User", "Property", "Old value", "New value", "Notes"};
    private LocalApplicationLogEntry[] records;

    public AuditTrailTableModel(LocalApplicationLogEntry[] records) {
        this.records = records;
    }
    
    @Override
    public int getRowCount() {
        return records.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }
    
    public String[] getColumnNames(){
        return columnNames;
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
        switch (columnIndex + 1){
            case 1:
                return new Date(records[rowIndex].getTimestamp());
            case 2:
                return LocalApplicationLogEntry.types[records[rowIndex].getType() - 1];
            case 3:
                return records[rowIndex].getUserName();
            case 4:
                return records[rowIndex].getAffectedProperty();
            case 5:
                return records[rowIndex].getOldValue();
            case 6:
                return records[rowIndex].getNewValue();
            case 7:
                return records[rowIndex].getNotes();
            default:
                return "";
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
    }

    void setRecords(LocalApplicationLogEntry[] records) {
        this.records = records;
    }    
}
