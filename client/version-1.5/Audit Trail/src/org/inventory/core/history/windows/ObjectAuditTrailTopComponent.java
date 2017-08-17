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
package org.inventory.core.history.windows;

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
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalApplicationLogEntry;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.export.ExportTablePanel;
import org.inventory.core.services.api.export.ExportableTable;
import org.inventory.core.services.api.export.filters.CSVFilter;
import org.inventory.core.services.api.export.filters.TextExportFilter;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.netbeans.swing.etable.ETable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Show the activity log associated to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectAuditTrailTopComponent extends TopComponent implements ExportableTable, Refreshable {
    private JToolBar barMain;
    private JButton btnExport;
    private JScrollPane pnlScrollMain;
    private ETable aTable;
    private String columnNames[];
    private LocalObjectLight object;

    public ObjectAuditTrailTopComponent(LocalObjectLight object) {
        LocalApplicationLogEntry[] entries = CommunicationsStub.getInstance().getBusinessObjectAuditTrail(object.getClassName(), object.getOid());
        if (entries == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        this.object = object;
        this.columnNames =  new String[]{"Timestamp", "Type", "User", "Property", "Old value", "New value"};
        setLayout(new BorderLayout());
        barMain = new JToolBar();
        add(barMain, BorderLayout.NORTH);
        btnExport = new JButton();
        btnExport.setIcon(new ImageIcon(getClass().getResource("/org/inventory/core/history/res/export.png"))); //NOI18N
        barMain.add(btnExport);
        btnExport.setToolTipText("Export to CSV...");
        btnExport.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                btnExportButtonActionPerformed(e);
            }
        });
        setName(String.format("Audit trail for %s", object));
        
        aTable = new ETable(buildTableModel(entries));
        
        pnlScrollMain = new JScrollPane();
        pnlScrollMain.setViewportView(aTable);
        add(pnlScrollMain, BorderLayout.CENTER);
        Mode myMode = WindowManager.getDefault().findMode("bottomSlidingSide"); //NOI18N
        myMode.dockInto(this);
    }
    
    @Override
    protected String preferredID() {
        return "ObjectAuditTrailTopComponent_" + object.getOid(); //NOI18N
    }
    
    private TableModel buildTableModel(final LocalApplicationLogEntry[] logEntries) {
        
        return new TableModel() {
            final LocalApplicationLogEntry entries[] = logEntries;
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
        };
        
    }
    
    @Override
    protected void componentClosed() {
        aTable.removeAll();
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    private void btnExportButtonActionPerformed(ActionEvent e) {
        ExportTablePanel exportPanel = new ExportTablePanel(new TextExportFilter []{CSVFilter.getInstance()}, this);
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options",true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }

    @Override
    public Object[][] getResults(Range range) {
        Object[][] res = new Object[aTable.getModel().getRowCount() + 1][aTable.getModel().getColumnCount()];
        res[0] = columnNames;
        for (int i = 0; i < aTable.getModel().getRowCount(); i++)
            for (int j = 0; j < aTable.getModel().getColumnCount(); j++)
                res[i + 1][j] = aTable.getModel().getValueAt(i, j);
        return res;
    }

    @Override
    public void refresh() {
        setName(String.format("Audit trail for %s", object));
        LocalApplicationLogEntry[] entries = CommunicationsStub.getInstance().getBusinessObjectAuditTrail(object.getClassName(), object.getOid());
        if (entries == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return;
        }
        aTable.setModel(buildTableModel(entries));
    }
}