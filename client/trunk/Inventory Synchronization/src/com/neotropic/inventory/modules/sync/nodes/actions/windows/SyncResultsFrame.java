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
package com.neotropic.inventory.modules.sync.nodes.actions.windows;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import org.inventory.communications.core.LocalSyncGroup;
import org.inventory.communications.core.LocalSyncResult;
import org.inventory.core.services.i18n.I18N;
import org.openide.util.ImageUtilities;

/**
 * JFrame to show the list of results after executing a synchronization process
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class SyncResultsFrame extends JFrame {
    private static final ImageIcon ICON_ERROR = ImageUtilities.loadImageIcon("com/neotropic/inventory/modules/sync/res/error.png", false);
    private static final ImageIcon ICON_WARNING = ImageUtilities.loadImageIcon("com/neotropic/inventory/modules/sync/res/warning.png", false);
    private static final ImageIcon ICON_SUCCESS = ImageUtilities.loadImageIcon("com/neotropic/inventory/modules/sync/res/success.png", false);
    
    private JScrollPane pnlScrollMain;
    private JList<LocalSyncResult> lstSyncResults;

    public SyncResultsFrame(LocalSyncGroup syncGroup, List<LocalSyncResult> results) {
        setLayout(new BorderLayout());
        setTitle(String.format(I18N.gm("sync_list_of_results"), syncGroup.getName()));
        pnlScrollMain = new JScrollPane();
        setSize(800, 650);
        setLocationRelativeTo(null);

        JPanel pnlListOfResults = new JPanel();
        pnlListOfResults.setLayout(new GridLayout(1, 2));
        
        lstSyncResults = new JList<>(results.toArray(new LocalSyncResult[0]));
        lstSyncResults.setCellRenderer(new SyncResultsCellRenderer());
        lstSyncResults.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        
        pnlScrollMain.setViewportView(lstSyncResults);
        add(pnlScrollMain);
    }
   
    private class SyncResultsCellRenderer implements ListCellRenderer<LocalSyncResult> {
        
        @Override
        public Component getListCellRendererComponent(JList<? extends LocalSyncResult> list, 
                LocalSyncResult value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lblResultEntry = new JLabel("<html><b>Description: </b>" + value.getActionDescription() 
                                            + "<br/><b>Result: </b>" +value.getResult()+ "<html>");
            lblResultEntry.setBorder(new EmptyBorder(5, 5, 5, 0));
            lblResultEntry.setOpaque(true);
            lblResultEntry.setBackground(Color.WHITE);
            switch (value.getType()) {
                case LocalSyncResult.ERROR:
                    lblResultEntry.setIcon(ICON_ERROR);
                    break;
                case LocalSyncResult.WARNING:
                    lblResultEntry.setIcon(ICON_WARNING);
                    break;
                default:
                    lblResultEntry.setIcon(ICON_SUCCESS);
            }
            return lblResultEntry;
        }
    }
}
