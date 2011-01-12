/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.queries.graphical;

import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.openide.windows.TopComponent;

/**
 * Query results for the new Graphical Query builder
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ComplexQueryResultTopComponent extends TopComponent{
    private JToolBar barMain = new JToolBar();
    private JButton btnNext = new JButton();
    private JButton btnPrevious = new JButton();
    private JButton btnAll = new JButton();
    private JButton btnExport = new JButton();
    private JScrollPane pnlScrollMain = new JScrollPane();

    public ComplexQueryResultTopComponent() {
    }

    private void initComponents(){
        btnNext.setToolTipText("Next page");
        btnPrevious.setToolTipText("Previous page");
        btnAll.setToolTipText("Retrieve all results");
        btnPrevious.setEnabled(false);
        barMain.add(btnExport);
        barMain.add(btnPrevious);
        barMain.add(btnNext);
        barMain.add(btnAll);
        add(barMain,BorderLayout.NORTH);
        add(pnlScrollMain);
        setLayout(new BorderLayout());
    }
}
