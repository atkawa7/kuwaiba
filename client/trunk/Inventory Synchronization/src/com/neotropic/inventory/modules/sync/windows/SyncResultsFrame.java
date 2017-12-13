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
package com.neotropic.inventory.modules.sync.windows;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.inventory.communications.core.LocalSyncResult;
import org.inventory.core.services.i18n.I18N;

/**
 * JFrame to show the list of results after synchronization
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class SyncResultsFrame extends JFrame{
    
    private JScrollPane pnlScrollMain;
    private JList<String> lstSyncResults;

    public SyncResultsFrame(List<LocalSyncResult> results) {
        setLayout(new BorderLayout());
        setTitle(I18N.gm("sync_list_of_results"));
        pnlScrollMain = new JScrollPane();
        setSize(400, 650);
        setLocationRelativeTo(null);

        JPanel pnlListOfResults = new JPanel();
        pnlListOfResults.setLayout(new GridLayout(1, 2));
        List<String> resultsInString = new ArrayList();
        for(LocalSyncResult r: results)
            resultsInString.add("Description: " + r.getActionDescription() + " Result: " + r.getResult());
        
        lstSyncResults = new JList<>(resultsInString.toArray(new String[0]));
        //lstSyncResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        pnlScrollMain.setViewportView(lstSyncResults);
        add(lstSyncResults, BorderLayout.CENTER);
    }
   
}
