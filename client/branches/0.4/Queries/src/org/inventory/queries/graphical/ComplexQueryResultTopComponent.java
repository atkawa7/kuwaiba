/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.table.TableModel;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.queries.LocalResultRecord;
import org.inventory.core.services.factories.ObjectActionFactory;
import org.inventory.core.services.exceptions.ObjectActionException;
import org.inventory.queries.GraphicalQueryBuilderService;
import org.inventory.queries.graphical.dialogs.ExportSettingsPanel;
import org.netbeans.swing.etable.ETable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Query results for the new Graphical Query builder
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ComplexQueryResultTopComponent extends TopComponent{
    private JToolBar barMain ;
    private JButton btnNext;
    private JButton btnPrevious;
    private JButton btnAll;
    private JButton btnExport;
    private JScrollPane pnlScrollMain;
    private ETable myTable;
    private int currentPage = 1;
    private int pageSize;
    /**
     * Reference to the controller
     */
    private GraphicalQueryBuilderService qbs;
    private NotificationUtil nu;

    public ComplexQueryResultTopComponent(LocalResultRecord[] res, int pageSize,
            GraphicalQueryBuilderService qbs) {
        this.qbs = qbs;
        this.pageSize = pageSize;
        TableModel model = new QueryResultTableModel(res);
        myTable = new ETable(model);
        initComponents();
        myTable.addMouseListener(new PopupProvider());
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



        //Actions
        btnPrevious.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnPreviousActionPerformed();
            }
        });

        btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnNextActionPerformed();
            }
        });

        btnAll.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnAllActionPerformed();
            }
        });

        btnExport.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnExportActionPerformed();
            }
        });

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
    protected void componentClosed() {
        myTable.removeAll();
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
                ((QueryResultTableModel)myTable.getModel()).updateTableModel(res);
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
            ((QueryResultTableModel)myTable.getModel()).updateTableModel(res);
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
            myTable.setModel(new QueryResultTableModel(res));
            pnlScrollMain.repaint();
        }
        
    }

    public void btnExportActionPerformed(){
        ExportSettingsPanel exportPanel = new ExportSettingsPanel(qbs, this);
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options",true,exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    public NotificationUtil getNotifier(){
        if (this.nu == null)
            nu = Lookup.getDefault().lookup(NotificationUtil.class);
        return nu;
    }

    public Object[][] getCurrentResults(){
        return ((QueryResultTableModel)myTable.getModel()).getCurrentResults();
    }

    private class PopupProvider extends MouseAdapter{

        @Override
        public void mousePressed(MouseEvent e) {
          showPopup(e);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
          showPopup(e);
        }
        private void showPopup(MouseEvent e) {
          if (e.isPopupTrigger()) {
            LocalObjectLight singleRecord = (LocalObjectLight)myTable.getValueAt(myTable.rowAtPoint(new Point(e.getX(), e.getY())), 0);
            JPopupMenu  menu = new JPopupMenu();
            try{
                menu.add(ObjectActionFactory.createEditAction(singleRecord));
                if (CommunicationsStub.getInstance().getLightMetaForClass(singleRecord.getClassName(),false).isListType())
                    menu.add(ObjectActionFactory.createDeleteListTypeAction(singleRecord));
                else
                    menu.add(ObjectActionFactory.createDeleteBusinessObjectAction(singleRecord));
                menu.show(e.getComponent(), e.getX(), e.getY());
            }catch(ObjectActionException ex){
                getNotifier().showSimplePopup("Error", NotificationUtil.ERROR, ex.getMessage());
            }
          }
        }
    }
}
