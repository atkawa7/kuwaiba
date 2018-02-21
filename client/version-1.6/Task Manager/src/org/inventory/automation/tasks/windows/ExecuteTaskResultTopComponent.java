/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.automation.tasks.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.CompoundBorder;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import org.inventory.communications.core.LocalTaskResult;
import org.inventory.communications.core.LocalTaskResultMessage;
import org.openide.windows.TopComponent;

/**
 * Displays the results of a task that was executed on demand
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ExecuteTaskResultTopComponent extends TopComponent {
    private JTable tblResult;
    private JScrollPane pnlScrollMain;

    public ExecuteTaskResultTopComponent(LocalTaskResult taskResult) {
        setLayout(new BorderLayout());
        tblResult = new JTable(new TaskResultTableModel(taskResult));
        tblResult.getColumnModel().getColumn(0).setMaxWidth(50);
        tblResult.setRowHeight(20);
        tblResult.setDefaultRenderer(LocalTaskResultMessage.class, new TaskResultTableCellRenderer());
        pnlScrollMain = new JScrollPane(tblResult);
        add(pnlScrollMain);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    private static class TaskResultTableModel implements TableModel {
        private LocalTaskResult taskresult;
        private static String[] columns = new String[]{ "","Messages" };
        
        public TaskResultTableModel(LocalTaskResult taskresult) {
            this.taskresult = taskresult;
        }
        
        @Override
        public int getRowCount() {
            return taskresult.getMessages().size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return columns[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0)
                return int.class;
            else
                return LocalTaskResultMessage.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0)
                return rowIndex + 1;
            return taskresult.getMessages().get(rowIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            //Nothing to do here
        }

        @Override
        public void addTableModelListener(TableModelListener l) {}

        @Override
        public void removeTableModelListener(TableModelListener l) {}
    }
    
    private static class TaskResultTableCellRenderer extends JLabel implements TableCellRenderer {

        public TaskResultTableCellRenderer() {
            setOpaque(true);
            setBorder(new CompoundBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY), BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value.toString());
            LocalTaskResultMessage cellContent = (LocalTaskResultMessage)value;
            if (isSelected)
                setBackground(UIManager.getColor("Button.focus"));
            else {
                switch (cellContent.getMessageType()) {
                    case LocalTaskResultMessage.STATUS_ERROR:
                        setBackground(Color.PINK);
                        break;
                    case LocalTaskResultMessage.STATUS_WARNING:
                        setBackground(Color.YELLOW);
                        break;
                    default:
                    case LocalTaskResultMessage.STATUS_SUCCESS:
                        setBackground(Color.GREEN);
                        break;
                }
            }
            return this;
        }
    }
    
}
