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
package com.neotropic.inventory.modules.sync.nodes.actions.windows;

import com.neotropic.inventory.modules.sync.LocalSyncDataSourceConfiguration;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.border.EmptyBorder;
import com.neotropic.inventory.modules.sync.LocalSyncGroup;
import com.neotropic.inventory.modules.sync.LocalSyncProvider;
import com.neotropic.inventory.modules.sync.LocalSyncResult;
import java.util.HashMap;
import javax.swing.JTabbedPane;
import org.inventory.core.services.api.export.ExportTablePanel;
import org.inventory.core.services.api.export.ExportableTable;
import org.inventory.core.services.api.export.filters.CSVFilter;
import org.inventory.core.services.api.export.filters.TextExportFilter;
import org.inventory.core.services.i18n.I18N;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.ImageUtilities;

/**
 * JFrame to show the list of results after executing a synchronization process
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SyncResultsFrame extends JFrame {
    private static final ImageIcon ICON_ERROR = ImageUtilities.loadImageIcon("com/neotropic/inventory/modules/sync/res/error.png", false);
    private static final ImageIcon ICON_WARNING = ImageUtilities.loadImageIcon("com/neotropic/inventory/modules/sync/res/warning.png", false);
    private static final ImageIcon ICON_SUCCESS = ImageUtilities.loadImageIcon("com/neotropic/inventory/modules/sync/res/success.png", false);
    private static final ImageIcon ICON_INFORMATION = ImageUtilities.loadImageIcon("com/neotropic/inventory/modules/sync/res/information.png", false);
    
    //private JScrollPane pnlScrollMain;
    private SyncResultsList<LocalSyncResult> lstSyncResults;
    //private HashMap<Long, String> mapDataSourceConfigNames;
    private JTabbedPane jTabbedPane;

    public SyncResultsFrame() {
        setLayout(new BorderLayout());
        setSize(800, 650);
        setLocationRelativeTo(null);
        JPanel pnlListOfResults = new JPanel();
        pnlListOfResults.setLayout(new GridLayout(1, 1));
        JButton btnExport = new JButton();
        btnExport.setText(I18N.gm("export")); // NOI18N
        btnExport.setToolTipText(I18N.gm( "export")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ExportTablePanel exportPanel = new ExportTablePanel(new TextExportFilter[]{CSVFilter.getInstance()}, lstSyncResults);
                DialogDescriptor dd = new DialogDescriptor(exportPanel, I18N.gm("export_options"),true, exportPanel);
                DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
            }
        });
        add(btnExport, BorderLayout.NORTH);
        
        
        jTabbedPane = new JTabbedPane();
                
        jTabbedPane.setVisible(true);
        add(jTabbedPane);
    }
   
    public void addTab(LocalSyncGroup syncGroup, LocalSyncProvider LocalSyncProvider, List<LocalSyncResult> results){
        HashMap<Long, String> mapDataSourceConfigNames = new HashMap<>();

        JScrollPane pnlScrollMain = new JScrollPane();
        
        List<LocalSyncDataSourceConfiguration> dataSourceConfigurations = syncGroup.getDataSourceConfig();
        for(LocalSyncResult result : results){
            for (LocalSyncDataSourceConfiguration dataSourceConfiguration : dataSourceConfigurations) {
                if(result.getDataSourceId() == dataSourceConfiguration.getId()){
                    mapDataSourceConfigNames.put(result.getDataSourceId(), dataSourceConfiguration.getName());
                    break;
                }
            }
        }
        
        lstSyncResults = new SyncResultsList<>(results.toArray(new LocalSyncResult[0]));
        lstSyncResults.setCellRenderer(new SyncResultsCellRenderer(mapDataSourceConfigNames));
        
        pnlScrollMain.setViewportView(lstSyncResults);
        
        jTabbedPane.addTab(syncGroup.getName() + " " +LocalSyncProvider.getDisplayName(), pnlScrollMain);
       
    }
    
    private class SyncResultsCellRenderer implements ListCellRenderer<LocalSyncResult> {
        
        private final HashMap<Long, String>  mapDataSourceConfigNames;

        public SyncResultsCellRenderer(HashMap<Long, String> mapDataSourceConfigNames) {
            this.mapDataSourceConfigNames = mapDataSourceConfigNames;
        }
                
        @Override
        public Component getListCellRendererComponent(JList<? extends LocalSyncResult> list, 
                LocalSyncResult value, int index, boolean isSelected, boolean cellHasFocus) {
            
            JLabel lblResultEntry = new JLabel("<html>"
                                            +   "<b>Data Source Configuration: </b>"+  mapDataSourceConfigNames.get(value.getDataSourceId())
                                            +   "<br/><b>Action: </b>" + value.getActionDescription() 
                                            +   "<br/><b>Result: </b>" +value.getResult()
                                            + "<html>");
            lblResultEntry.setBorder(new EmptyBorder(5, 5, 5, 0));
            lblResultEntry.setOpaque(true);
            lblResultEntry.setBackground(Color.WHITE);
            switch (value.getType()) {
                case LocalSyncResult.TYPE_ERROR:
                    lblResultEntry.setIcon(ICON_ERROR);
                    break;
                case LocalSyncResult.TYPE_WARNING:
                    lblResultEntry.setIcon(ICON_WARNING);
                    break;
                case LocalSyncResult.TYPE_SUCCESS:
                    lblResultEntry.setIcon(ICON_SUCCESS);
                    break;
                case LocalSyncResult.TYPE_INFORMATION:
                    lblResultEntry.setIcon(ICON_INFORMATION);
            }
            return lblResultEntry;
        }
    }
    
    private class SyncResultsList<E> extends JList implements ExportableTable{

        public SyncResultsList(Object[] listData) {
            super(listData);
        }

        @Override
        public Object[][] getResults(Range range) {
            Object[][] res = new Object[this.getModel().getSize()+1][2];
            res[0][0]="Description"; res[0][1]="Result";
            for (int i = 0; i < this.getModel().getSize(); i++) {
                LocalSyncResult elementAt = (LocalSyncResult)this.getModel().getElementAt(i);
                res[i+1][0]= elementAt.getActionDescription();
                res[i+1][1]= elementAt.getResult();
            }
            return res;
        }
        
    }
}
