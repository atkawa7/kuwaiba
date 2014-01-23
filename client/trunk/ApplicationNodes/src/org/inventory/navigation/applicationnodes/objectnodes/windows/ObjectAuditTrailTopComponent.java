/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.objectnodes.windows;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.inventory.communications.core.LocalApplicationLogEntry;
import org.netbeans.swing.etable.ETable;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Show the activity log associated to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectAuditTrailTopComponent extends TopComponent{
    private JToolBar barMain;
    private JButton btnExport;
    private JScrollPane pnlScrollMain;
    private ETable aTable;

    public ObjectAuditTrailTopComponent(String objectName, String objectClass, final LocalApplicationLogEntry[] logEntries) {
        setLayout(new BorderLayout());
        barMain = new JToolBar();
        add(barMain, BorderLayout.NORTH);
        btnExport = new JButton();
        btnExport.setIcon(new ImageIcon(getClass().getResource("/org/inventory/navigation/applicationnodes/res/export.png"))); //NOI18N
        barMain.add(btnExport);
        btnExport.setToolTipText("Export to CSV...");
        btnExport.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        });
        setName(String.format("Audit trail for %s [%s]", objectName, objectClass));
        aTable = new ETable(new TableModel() {
            final LocalApplicationLogEntry entries[] = logEntries;
            String columnNames[] =  new String[]{"Timestamp", "Type", "User", "Property", "Old value", "New value"};
            @Override
            public int getRowCount() {
                return entries.length;
            }

            @Override
            public int getColumnCount() {
                return 6;
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
                switch (columnIndex){
                    case 0:
                        return new Date(entries[rowIndex].getTimestamp());
                    case 1:
                        return LocalApplicationLogEntry.types[entries[rowIndex].getType() - 1];
                    case 2:
                        return entries[rowIndex].getUserName();
                    case 3:
                        return entries[rowIndex].getAffectedProperty();
                    case 4:
                        return entries[rowIndex].getOldValue();
                    case 5:
                        return entries[rowIndex].getNewValue();
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
        });
        pnlScrollMain = new JScrollPane();
        pnlScrollMain.setViewportView(aTable);
        add(pnlScrollMain, BorderLayout.CENTER);
        Mode myMode = WindowManager.getDefault().findMode("bottomSlidingSide"); //NOI18N
        myMode.dockInto(this);
    }
    
    @Override
    protected void componentClosed() {
        aTable.removeAll();
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
}