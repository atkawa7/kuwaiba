/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
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

import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.inventory.communications.core.LocalApplicationLogEntry;
import org.netbeans.swing.etable.ETable;
import org.openide.windows.TopComponent;

/**
 * Show the activity log associated to an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectAuditTrailTopComponent extends TopComponent{
    private JToolBar barMain;
    private JButton btnNext;
    private JButton btnPrevious;
    private JButton btnAll;
    private JButton btnExport;
    private JScrollPane pnlScrollMain;
    private ETable myTable;
    private int currentPage = 1;
    private int pageSize;

    public ObjectAuditTrailTopComponent(LocalApplicationLogEntry[] logEntries) {
        
    }
}
