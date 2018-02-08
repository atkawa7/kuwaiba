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
package org.inventory.views.rackview;

import java.util.List;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.export.ExportTablePanel;
import org.inventory.core.services.api.export.ExportableTable;
import org.inventory.core.services.api.export.filters.CSVFilter;
import org.inventory.core.services.api.export.filters.TextExportFilter;
import org.netbeans.swing.etable.ETable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.TopComponent;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public final class RackTableViewTopComponent extends TopComponent implements ExportableTable, Refreshable {
    private final String [] columns = new String [] {"Source Equipment", "Source Port", "Target Equipment", "Target Port"};
    private LocalObjectLight rack;
    private RackViewService service;
    private ETable aTable;
    
    public RackTableViewTopComponent(LocalObjectLight rack, RackViewService service) {
        this();
        this.rack = rack;
        this.service = service;
        
        aTable = new ETable(buildTableModel(service.getRackTable()));        
        pnlMainScrollPanel.setViewportView(aTable);
        
        setName("Rack Table for " + rack.toString());
        Mode myMode = WindowManager.getDefault().findMode("bottomSlidingSide"); //NOI18N
        myMode.dockInto(this);
    }
    
    private RackTableViewTopComponent() {
        initComponents();
    }
    
    @Override
    protected String preferredID() {
        return "RackTableViewTopComponent_" + rack.getOid(); //NOI18N
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    private TableModel buildTableModel(final List<List<LocalObjectLight>> data) {
        
        return new TableModel() {
            List<List<LocalObjectLight>> rows = data;
            @Override
            public int getRowCount() {
                return rows.size();
            }

            @Override
            public int getColumnCount() {
                return columns.length;
            }

            @Override
            public String getColumnName(int columnIndex) {
                return columns[columnIndex];
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
                return rows.get(rowIndex).get(columnIndex);
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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMainScrollPanel = new javax.swing.JScrollPane();
        toolBarMain = new javax.swing.JToolBar();
        btnExport = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());
        add(pnlMainScrollPanel, java.awt.BorderLayout.CENTER);

        toolBarMain.setRollover(true);
        toolBarMain.setMaximumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setMinimumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setPreferredSize(new java.awt.Dimension(392, 38));

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/rackview/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(RackTableViewTopComponent.class, "RackTableViewTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(RackTableViewTopComponent.class, "RackTableViewTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnExportMouseClicked(evt);
            }
        });
        toolBarMain.add(btnExport);

        add(toolBarMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExportMouseClicked
        ExportTablePanel exportPanel = new ExportTablePanel(new TextExportFilter []{CSVFilter.getInstance()}, this);
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options",true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExport;
    private javax.swing.JScrollPane pnlMainScrollPanel;
    private javax.swing.JToolBar toolBarMain;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
        aTable.removeAll();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    public void refresh() {
        aTable.setModel(buildTableModel(service.getRackTable()));
    }

    @Override
    public Object[][] getResults(Range range) {
        Object[][] res = new Object[aTable.getModel().getRowCount() + 1][aTable.getModel().getColumnCount()];
        res[0] = columns;
        for (int i = 0; i < aTable.getModel().getRowCount(); i++)
            for (int j = 0; j < aTable.getModel().getColumnCount(); j++)
                res[i + 1][j] = aTable.getModel().getValueAt(i, j);
        return res;
    }
}
