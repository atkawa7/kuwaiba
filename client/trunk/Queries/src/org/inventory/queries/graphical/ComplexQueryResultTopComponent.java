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
import java.util.ArrayList;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.inventory.communications.core.LocalResultRecord;
import org.inventory.queries.GraphicalQueryBuilderService;
import org.netbeans.swing.etable.ETable;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Query results for the new Graphical Query builder
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ComplexQueryResultTopComponent extends TopComponent implements ExplorerManager.Provider{
    private JToolBar barMain ;
    private JButton btnNext;
    private JButton btnPrevious;
    private JButton btnAll;
    private JButton btnExport;
    private JScrollPane pnlScrollMain;
    private ExplorerManager em = new ExplorerManager();
    private ETable myTable;

    /**
     * Two pages can be buffered so we don't have to ask for the results every time
     * the user switch the page
     */
    private Object[][] page1;
    private Object[][] page2;

    public ComplexQueryResultTopComponent() {
    }

    public ComplexQueryResultTopComponent(LocalResultRecord[] res, List<String> columnNames,
            GraphicalQueryBuilderService aThis) {
        ArrayList<String> myColumns= new ArrayList<String>();
        myColumns.add(""); // The first column is the object itself
        myColumns.addAll(columnNames);
        page1 = new Object[res.length][columnNames.size() +1];
        for (int i = 0; i < res.length ; i++){
            page1[i][0] = res[i].getObject();
            for (int j = 1; j <= columnNames.size(); j++){
                page1[i][j] = res[i].getExtraColumns().get(j-1);
            }
        }
        myTable = new ETable(page1, myColumns.toArray());
        initComponents();

        //em.setRootContext(new RootObjectNode(new QueryResultChildren<LocalResultRecord>(res)));
    }

    private void initComponents(){
        barMain = new JToolBar();
        btnNext = new JButton();
        btnPrevious = new JButton();
        btnAll = new JButton();
        btnExport = new JButton();
        pnlScrollMain = new JScrollPane();
        setLayout(new BorderLayout());

        pnlScrollMain.setViewportView(myTable);

        btnExport.setIcon(new ImageIcon(getClass().getResource("/org/inventory/queries/res/export.png"))); //NOI18N
        btnExport.setToolTipText("Export...");
        btnNext.setIcon(new ImageIcon(getClass().getResource("/org/inventory/queries/res/next.png"))); //NOI18N
        btnNext.setToolTipText("Next page");
        btnPrevious.setIcon(new ImageIcon(getClass().getResource("/org/inventory/queries/res/previous.png"))); //NOI18N
        btnPrevious.setToolTipText("Previous page");
        btnAll.setIcon(new ImageIcon(getClass().getResource("/org/inventory/queries/res/all.png"))); //NOI18N
        btnAll.setToolTipText("Retrieve all results");

        btnPrevious.setEnabled(false);
        barMain.add(btnExport);
        barMain.add(btnPrevious);
        barMain.add(btnNext);
        barMain.add(btnAll);

        barMain.setRollover(true);
        barMain.setPreferredSize(new java.awt.Dimension(326, 33));

        add(pnlScrollMain, BorderLayout.CENTER);
        add(barMain, BorderLayout.PAGE_START);
        
        setName("Query Results");

        Mode myMode = WindowManager.getDefault().findMode("bottomSlidingSide"); //NOI18N
        myMode.dockInto(this);
        revalidate();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public ExplorerManager getExplorerManager() {
        return em;
    }
}
