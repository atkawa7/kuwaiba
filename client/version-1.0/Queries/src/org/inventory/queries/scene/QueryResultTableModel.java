/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.queries.scene;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.inventory.communications.core.queries.LocalResultRecord;

/**
 * This is the table model used to display the results
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class QueryResultTableModel implements TableModel{

    Object[][] currentResults;
    String[] columnNames;

    /**
     *
     * @param res An a array with the results to be displayed. Please note that the
     * first record must be <b>always</b> used to store the table headers
     */
    QueryResultTableModel(LocalResultRecord[] res) {
        columnNames = new String[res[0].getExtraColumns().size() + 1];
        columnNames[0] = "";
        for (int i = 0; i < res[0].getExtraColumns().size(); i++)
            columnNames[i + 1] = res[0].getExtraColumns().get(i);
        updateTableModel(res);
    }

    @Override
    public int getRowCount() {
        return currentResults.length;
    }
    
    public String[] getColumnNames(){
        return columnNames;
    }

    @Override
    public int getColumnCount() {
        return currentResults[0].length;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
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
        return currentResults[rowIndex][columnIndex];
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        currentResults[rowIndex][columnIndex] = aValue;
    }

    @Override
    public void addTableModelListener(TableModelListener l) {

    }

    @Override
    public void removeTableModelListener(TableModelListener l) {

    }

    public final void updateTableModel(LocalResultRecord[] res) {
        currentResults = new Object[res.length -1 ][columnNames.length]; //We ignore the first record
        for (int i = 0; i < res.length -1 ; i++){
            currentResults[i][0] = res[i+1].getObject();
            for (int j = 1; j < columnNames.length; j++)
                currentResults[i][j] = res[i+1].getExtraColumns().get(j - 1);
        }
    }

    public Object[][] getCurrentResults() {
        return currentResults;
    }
}
