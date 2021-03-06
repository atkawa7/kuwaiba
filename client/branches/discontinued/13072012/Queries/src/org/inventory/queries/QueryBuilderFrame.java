/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.queries;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;

/**
 * Shows the filtering options for the query builder
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class QueryBuilderFrame extends JFrame {

    private QueryBuilderService qbs;


    public QueryBuilderFrame() {
        initComponents();
        initCustomComponents();
        qbs = new QueryBuilderService(this);
        qbs.initComponents();
    }

    public final void initCustomComponents(){
        this.setSize(800, 500);
        pnlMainQueryBuilder.setDividerLocation(0.7);
        GroupLayout layout = new GroupLayout(pnlLeftPanel);
        pnlLeftPanel.setLayout(layout);
        this.setLocationRelativeTo(getRootPane());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMainQueryBuilder = new javax.swing.JSplitPane();
        pnlQueryBuilderRightScroll = new javax.swing.JScrollPane();
        lstClasses = new javax.swing.JList();
        pnlMainLeftPanel = new javax.swing.JPanel();
        barSearch = new javax.swing.JToolBar();
        btnSearch = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        pnlQueryBuilderLeftScroll = new javax.swing.JScrollPane();
        pnlLeftPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(org.openide.util.NbBundle.getMessage(QueryBuilderFrame.class, "QueryBuilderFrame.title")); // NOI18N

        lstClasses.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        lstClasses.setToolTipText(org.openide.util.NbBundle.getMessage(QueryBuilderFrame.class, "QueryBuilderFrame.lstClasses.toolTipText")); // NOI18N
        pnlQueryBuilderRightScroll.setViewportView(lstClasses);

        pnlMainQueryBuilder.setRightComponent(pnlQueryBuilderRightScroll);

        pnlMainLeftPanel.setLayout(new java.awt.BorderLayout());

        barSearch.setRollover(true);

        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/search.png"))); // NOI18N
        btnSearch.setText(org.openide.util.NbBundle.getMessage(QueryBuilderFrame.class, "QueryBuilderFrame.btnSearch.text")); // NOI18N
        btnSearch.setToolTipText(org.openide.util.NbBundle.getMessage(QueryBuilderFrame.class, "QueryBuilderFrame.btnSearch.toolTipText")); // NOI18N
        btnSearch.setFocusable(false);
        btnSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        barSearch.add(btnSearch);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/save.png"))); // NOI18N
        btnSave.setText(org.openide.util.NbBundle.getMessage(QueryBuilderFrame.class, "QueryBuilderFrame.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(QueryBuilderFrame.class, "QueryBuilderFrame.btnSave.toolTipText")); // NOI18N
        btnSave.setDisabledIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/save.png"))); // NOI18N
        btnSave.setEnabled(false);
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barSearch.add(btnSave);

        pnlMainLeftPanel.add(barSearch, java.awt.BorderLayout.PAGE_START);

        pnlLeftPanel.setLayout(null);
        pnlQueryBuilderLeftScroll.setViewportView(pnlLeftPanel);

        pnlMainLeftPanel.add(pnlQueryBuilderLeftScroll, java.awt.BorderLayout.CENTER);

        pnlMainQueryBuilder.setLeftComponent(pnlMainLeftPanel);

        getContentPane().add(pnlMainQueryBuilder, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        qbs.search();
}//GEN-LAST:event_btnSearchActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barSearch;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JList lstClasses;
    private javax.swing.JPanel pnlLeftPanel;
    private javax.swing.JPanel pnlMainLeftPanel;
    private javax.swing.JSplitPane pnlMainQueryBuilder;
    private javax.swing.JScrollPane pnlQueryBuilderLeftScroll;
    private javax.swing.JScrollPane pnlQueryBuilderRightScroll;
    // End of variables declaration//GEN-END:variables

    public JList getList(){
        return this.lstClasses;
    }

    public JPanel getLeftPanel(){
        return pnlLeftPanel;
    }
}
