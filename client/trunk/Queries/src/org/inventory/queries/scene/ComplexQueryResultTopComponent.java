/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.queries.scene;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyVetoException;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.table.TableModel;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.queries.LocalResultRecord;
import org.inventory.core.services.api.export.ExportTablePanel;
import org.inventory.core.services.api.export.ExportableTable;
import org.inventory.core.services.api.export.filters.CSVFilter;
import org.inventory.core.services.api.export.filters.TextExportFilter;
import org.inventory.core.services.api.export.filters.XMLFilter;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.queries.QueryManagerService;
import org.netbeans.swing.etable.ETable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 * Query results for the new Graphical Query builder
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ComplexQueryResultTopComponent extends TopComponent implements ExportableTable, ExplorerManager.Provider {
    private JToolBar barMain;
    private JButton btnNext;
    private JButton btnPrevious;
    private JButton btnAll;
    private JButton btnExport;
    private JScrollPane pnlScrollMain;
    private ETable aTable;
    private int currentPage = 1;
    private int pageSize;
    private ExplorerManager em;
    /**
     * Reference to the controller
     */
    private QueryManagerService qbs;

    public ComplexQueryResultTopComponent(LocalResultRecord[] res, int pageSize,
            QueryManagerService qbs) {       
        this.qbs = qbs;
        this.pageSize = pageSize;
        this.em = new ExplorerManager();
        TableModel model = new QueryResultTableModel(res);
        aTable = new ETable(model);
        aTable.addMouseListener(new PopupProvider());
        initComponents();
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
    }

    private void initComponents(){
        setLayout(new BorderLayout());
        barMain = new JToolBar();
        btnNext = new JButton();
        btnPrevious = new JButton();
        btnAll = new JButton();
        btnExport = new JButton();
        pnlScrollMain = new JScrollPane();
        pnlScrollMain.setViewportView(aTable);
        
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

        //Actions
        btnPrevious.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnPreviousActionPerformed();
            }
        });

        btnNext.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnNextActionPerformed();
            }
        });

        btnAll.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnAllActionPerformed();
            }
        });

        btnExport.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnExportActionPerformed();
            }
        });

        barMain.setRollover(true);
        barMain.setPreferredSize(new java.awt.Dimension(326, 33));

        add(barMain, BorderLayout.NORTH);
        add(pnlScrollMain);
        
        setName("Query Results");
    }

    @Override
    protected void componentClosed() {
        aTable.removeAll();
    }

    /**
     * Actions listeners for buttons
     */
    public void btnNextActionPerformed(){
        LocalResultRecord[] res = qbs.executeQuery(++currentPage);
        if (res != null){
            if (res.length < pageSize)
                btnNext.setEnabled(false);
            else
                btnPrevious.setEnabled(true);

            if (res.length == 1){ //One including the table header
                JOptionPane.showMessageDialog(this, "No more search results were found");
                currentPage--;
            }
            else{
                ((QueryResultTableModel)aTable.getModel()).updateTableModel(res);
                pnlScrollMain.repaint();
            }
        }else currentPage--;
    }

    public void btnPreviousActionPerformed(){
        LocalResultRecord[] res = qbs.executeQuery(--currentPage);
        if (res != null){
            if (currentPage == 1)
                btnPrevious.setEnabled(false);
            btnNext.setEnabled(true);
            ((QueryResultTableModel)aTable.getModel()).updateTableModel(res);
            pnlScrollMain.repaint();
        }else currentPage++;
    }

    public void btnAllActionPerformed(){
        LocalResultRecord[] res = qbs.executeQuery(0);
        if (res != null){
            btnNext.setEnabled(false);
            btnPrevious.setEnabled(false);
            //For some reason I can't figure out, updateTableModel *only here*
            //makes the rendering to throw a IndexOutOfBoundsException
            //((QueryResultTableModel)myTable.getModel()).updateTableModel(res);
            aTable.setModel(new QueryResultTableModel(res));
            pnlScrollMain.repaint();
        }
        
    }

    public void btnExportActionPerformed(){
        ExportTablePanel exportPanel = new ExportTablePanel(new TextExportFilter[]{CSVFilter.getInstance(), XMLFilter.getInstance()}, this);
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options",true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public Object[][] getCurrentResults(){
        Object[][] res = new Object[aTable.getModel().getRowCount() + 1][aTable.getModel().getColumnCount()];
        res[0] = ((QueryResultTableModel)aTable.getModel()).getColumnNames();
        for (int i = 1; i < aTable.getModel().getRowCount(); i++)
            for (int j = 0; j < aTable.getModel().getColumnCount(); j++)
                res[i][j] = aTable.getModel().getValueAt(i, j);
        return res;
    }
    
    @Override
    public Object[][] getResults(ExportableTable.Range range) {
        Object[][] res;
        if (range == ExportableTable.Range.CURRENT_PAGE)
            res = getCurrentResults();
        else {
            LocalResultRecord[] results = qbs.executeQuery(0);
            if (results == null)
                res = null;
            else{
                if (results.length == 0)
                    res = new Object[0][0];
                else{
                    res = new Object[results.length][results[0].getExtraColumns().size() + 1];
                    for (int i = 0; i < results.length; i++){
                        res[i][0] = results[i].getObject();
                        for (int j = 0; j < results[i].getExtraColumns().size();j++)
                            res[i][j + 1] = results[i].getExtraColumns().get(j);
                    }
                }
            }
        }
        return res;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    private class PopupProvider extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            LocalObjectLight selectedValue = (LocalObjectLight)aTable.getValueAt(aTable.rowAtPoint(new Point(e.getX(), e.getY())), 0);
            ObjectNode node = new ObjectNode(selectedValue);
            try {
                em.setRootContext(node);
                em.setSelectedNodes(new Node[] { node });
            } catch (PropertyVetoException ex) {} //Should never happen
            
            if ((e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK) {  //e.isPopupTrigger works differently depending on the platform, so we just check for the second button          
              JPopupMenu  menu = Utilities.actionsToPopup(node.getActions(true), e.getComponent());
              menu.show(e.getComponent(), e.getX(), e.getY());
            }
        }
        
        @Override
        public void mouseReleased(MouseEvent e) {}
    }
}
