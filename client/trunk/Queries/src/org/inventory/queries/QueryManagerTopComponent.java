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
package org.inventory.queries;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.queries.LocalQueryLight;
import org.inventory.communications.core.queries.LocalResultRecord;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.queries.scene.ComplexQueryResultTopComponent;
import org.inventory.queries.scene.QueryEditorScene;
import org.inventory.queries.dialogs.CreateQueryPanel;
import org.inventory.queries.dialogs.QueryListPanel;
import org.inventory.queries.scene.ClassNodeWidget;
import org.openide.windows.TopComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;

@TopComponent.Description(
        preferredID = "QueryManagerTopComponent",
        iconBase="org/inventory/queries/res/icon2.png", 
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Tools", id = "org.inventory.queries.QueryManagerTopComponent")
@ActionReferences(value = { @ActionReference(path = "Menu/Tools"),
    @ActionReference(path = "Toolbars/00_General", position = 1)})
@TopComponent.OpenActionRegistration(
        displayName = "Queries",
        preferredID = "QueryManagerTopComponent"
)
/**
 * Query manager Top component 
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public final class QueryManagerTopComponent extends TopComponent implements ActionListener{
    private QueryEditorScene queryScene;
    private QueryManagerService qbs;
    private ButtonGroup grpLogicalConnector;
    private boolean isSaved = true;

    public QueryManagerTopComponent() {
        initComponents();
        initCustomComponents();
        setName("Queries");
        setToolTipText("Create and Manage Queries");
    }

    private void initCustomComponents(){
        queryScene = new QueryEditorScene();
        qbs = new QueryManagerService(this);
        grpLogicalConnector = new ButtonGroup();
        cmbClassList.addItem(null);
        lblResultLimit.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

        pnlMainScrollPanel.setViewportView(queryScene.createView());
        grpLogicalConnector.add(chkAnd);
        grpLogicalConnector.add(chkOr);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMainScrollPanel = new javax.swing.JScrollPane();
        barMain = new javax.swing.JToolBar();
        lblSearch = new javax.swing.JLabel();
        cmbClassList = new javax.swing.JComboBox();
        sptOne = new javax.swing.JToolBar.Separator();
        btnOpen = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnClear = new javax.swing.JButton();
        btnConfigure = new javax.swing.JButton();
        btnOrganize = new javax.swing.JButton();
        btnSearch = new javax.swing.JButton();
        sptTwo = new javax.swing.JToolBar.Separator();
        lblConnector = new javax.swing.JLabel();
        chkAnd = new javax.swing.JRadioButton();
        chkOr = new javax.swing.JRadioButton();
        sptThree = new javax.swing.JToolBar.Separator();
        lblResultLimit = new javax.swing.JLabel();
        txtResultLimit = new javax.swing.JTextField();

        setLayout(new java.awt.BorderLayout());
        add(pnlMainScrollPanel, java.awt.BorderLayout.CENTER);

        barMain.setRollover(true);
        barMain.setPreferredSize(new java.awt.Dimension(326, 33));

        org.openide.awt.Mnemonics.setLocalizedText(lblSearch, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.lblSearch.text")); // NOI18N
        lblSearch.setPreferredSize(new java.awt.Dimension(70, 15));
        barMain.add(lblSearch);

        barMain.add(cmbClassList);
        barMain.add(sptOne);

        btnOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/open.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnOpen, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnOpen.text")); // NOI18N
        btnOpen.setToolTipText(org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnOpen.toolTipText")); // NOI18N
        btnOpen.setFocusable(false);
        btnOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenActionPerformed(evt);
            }
        });
        barMain.add(btnOpen);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        barMain.add(btnSave);

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/delete.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnDelete, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnDelete.text")); // NOI18N
        btnDelete.setToolTipText(org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnDelete.toolTipText")); // NOI18N
        btnDelete.setEnabled(false);
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        barMain.add(btnDelete);

        btnClear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/clear.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnClear, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnClear.text")); // NOI18N
        btnClear.setToolTipText(org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnClear.toolTipText")); // NOI18N
        btnClear.setFocusable(false);
        btnClear.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClear.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });
        barMain.add(btnClear);

        btnConfigure.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/configure-22.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnConfigure, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnConfigure.text")); // NOI18N
        btnConfigure.setToolTipText(org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnConfigure.toolTipText")); // NOI18N
        btnConfigure.setFocusable(false);
        btnConfigure.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConfigure.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConfigure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigureActionPerformed(evt);
            }
        });
        barMain.add(btnConfigure);

        btnOrganize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/organize.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnOrganize, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnOrganize.text")); // NOI18N
        btnOrganize.setToolTipText(org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnOrganize.toolTipText")); // NOI18N
        btnOrganize.setFocusable(false);
        btnOrganize.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOrganize.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOrganize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOrganizeActionPerformed(evt);
            }
        });
        barMain.add(btnOrganize);

        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/run-search.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSearch, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnSearch.text")); // NOI18N
        btnSearch.setToolTipText(org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.btnSearch.toolTipText")); // NOI18N
        btnSearch.setFocusable(false);
        btnSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });
        barMain.add(btnSearch);
        barMain.add(sptTwo);

        org.openide.awt.Mnemonics.setLocalizedText(lblConnector, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.lblConnector.text")); // NOI18N
        barMain.add(lblConnector);

        chkAnd.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkAnd, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.chkAnd.text")); // NOI18N
        chkAnd.setToolTipText(org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.chkAnd.toolTipText")); // NOI18N
        chkAnd.setFocusable(false);
        barMain.add(chkAnd);

        org.openide.awt.Mnemonics.setLocalizedText(chkOr, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.chkOr.text")); // NOI18N
        chkOr.setToolTipText(org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.chkOr.toolTipText")); // NOI18N
        chkOr.setFocusable(false);
        chkOr.setMaximumSize(new java.awt.Dimension(50, 27));
        chkOr.setMinimumSize(new java.awt.Dimension(50, 27));
        chkOr.setPreferredSize(new java.awt.Dimension(45, 18));
        barMain.add(chkOr);
        barMain.add(sptThree);

        org.openide.awt.Mnemonics.setLocalizedText(lblResultLimit, org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.lblResultLimit.text")); // NOI18N
        barMain.add(lblResultLimit);

        txtResultLimit.setColumns(4);
        txtResultLimit.setText(org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.txtResultLimit.text")); // NOI18N
        txtResultLimit.setToolTipText(org.openide.util.NbBundle.getMessage(QueryManagerTopComponent.class, "QueryManagerTopComponent.txtResultLimit.toolTipText")); // NOI18N
        barMain.add(txtResultLimit);

        add(barMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        if (!validateQuery())
            return;
        LocalResultRecord[] res = qbs.executeQuery(1);
        if (res != null){
            if (res.length == 1) //Remember: the first record is used to set the column names
                JOptionPane.showMessageDialog(null, "No results were found",
                        "Query Results",JOptionPane.INFORMATION_MESSAGE);
            else{
                TopComponent tc = new ComplexQueryResultTopComponent(res,
                        qbs.getCurrentTransientQuery().getLimit(), qbs);
                tc.open();
                tc.requestActive();
                tc.requestAttention(true);
            }
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (!validateQuery())
            return;
        if(qbs.getCurrentLocalQuery() == null){
            final CreateQueryPanel cqp = new CreateQueryPanel((String)qbs.getQueryProperties()[0],
                                        (String)qbs.getQueryProperties()[1],(Boolean)qbs.getQueryProperties()[2]);
            DialogDescriptor dd = new DialogDescriptor(cqp,
                    "Set query settings", true, new ActionListener() {
                        @Override
                                public void actionPerformed(ActionEvent e){
                                    if (e.getSource() == DialogDescriptor.OK_OPTION){
                                        qbs.setQueryProperties(cqp.getValues());
                                        qbs.saveQuery();
                                        /**
                                         * TODO: Since what createQuery returns is a LocalQueryLight, not a 
                                         * LocalQuery, when the save event creates a new query doesn't set the
                                         * "localquery" variable, thus there's no reference to the newly created object
                                         * and can't be deleted. This should be corrected somehow, so the newly created 
                                         * queries can be deleted just like the old ones
                                         */
                                        if (qbs.getCurrentLocalQuery() != null)
                                            btnDelete.setEnabled(true);
                                    }
                                }
                            });
            DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        }else{
            qbs.saveQuery();
            isSaved = true;
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed
        if (!checkForUnsavedQuery(true))
            return;
        
        boolean showAll = JOptionPane.showConfirmDialog(this,
                "Show only your saved queries?",
                "Query List",
                JOptionPane.YES_NO_OPTION) == JOptionPane.NO_OPTION;
        
        LocalQueryLight[] queries  = qbs.getQueries(showAll);
        if (queries != null){
            final QueryListPanel qlp = new QueryListPanel(queries);
            DialogDescriptor dd = new DialogDescriptor(qlp,
                    "Choose a query", true, new ActionListener() {
                        @Override
                                public void actionPerformed(ActionEvent e) {
                                    if (e.getSource() == DialogDescriptor.OK_OPTION){
                                        if (checkForUnsavedQuery(true)){
                                            if (qlp.getSelectedQuery() != null){
                                                cmbClassList.setSelectedItem(null);
                                                qbs.renderQuery(qlp.getSelectedQuery());
                                                btnDelete.setEnabled(true);
                                            }
                                            else
                                                JOptionPane.showConfirmDialog(null, "Select a query, please","Error", JOptionPane.ERROR_MESSAGE);
                                        }
                                    }
                                    qlp.releaseListeners();
                                }
                            });
            DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        }
    }//GEN-LAST:event_btnOpenActionPerformed

    private void btnConfigureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConfigureActionPerformed
        if (!validateQuery())
            return;
        final CreateQueryPanel cqp = new CreateQueryPanel((String)qbs.getQueryProperties()[0],
                                        (String)qbs.getQueryProperties()[1],(Boolean)qbs.getQueryProperties()[2]);
            DialogDescriptor dd = new DialogDescriptor(cqp,
                    "Set query settings", true, new ActionListener() {
                        @Override
                                public void actionPerformed(ActionEvent e){
                                    if (e.getSource() == DialogDescriptor.OK_OPTION)
                                        qbs.setQueryProperties(cqp.getValues());
                                }
                            });
            DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnConfigureActionPerformed

    private void btnOrganizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOrganizeActionPerformed
        if (!validateQuery())
            return;
        if (queryScene.getCurrentSearchedClass() != null){
            this.queryScene.organizeNodes(queryScene.getCurrentSearchedClass(),
                QueryEditorScene.X_OFFSET, QueryEditorScene.Y_OFFSET);
            this.queryScene.validate();
        }
    }//GEN-LAST:event_btnOrganizeActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the current query?",
                "Delete saved query",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
            qbs.deleteQuery();
            btnDelete.setEnabled(false);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the current query?", 
                    "Confirmation",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){  
            if(qbs.getCurrentLocalQuery() == null){ //It's a temporal query, not a saved one
                    queryScene.clear();
                    queryScene.setCurrentSearchedClass(null);
                    cmbClassList.setSelectedItem(null);
            }else{ //It's a saved query, so we nee to clear all nodes except the root one
                LocalClassMetadata currentSearchedClass = queryScene.getCurrentSearchedClass();
                queryScene.clear();
                ClassNodeWidget myNewNode = ((ClassNodeWidget)queryScene.addNode(currentSearchedClass));
                myNewNode.build(null);
                myNewNode.setPreferredLocation(new Point(100, 50));
                queryScene.setCurrentSearchedClass(currentSearchedClass);
                isSaved = false;
            }
            queryScene.validate();
        }
    }//GEN-LAST:event_btnClearActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barMain;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnConfigure;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnOrganize;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSearch;
    private javax.swing.JRadioButton chkAnd;
    private javax.swing.JRadioButton chkOr;
    private javax.swing.JComboBox cmbClassList;
    private javax.swing.JLabel lblConnector;
    private javax.swing.JLabel lblResultLimit;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JScrollPane pnlMainScrollPanel;
    private javax.swing.JToolBar.Separator sptOne;
    private javax.swing.JToolBar.Separator sptThree;
    private javax.swing.JToolBar.Separator sptTwo;
    private javax.swing.JTextField txtResultLimit;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        cmbClassList.addActionListener(this);
        queryScene.addActionListener(qbs);
        for (Object obj : qbs.getClassList())
            cmbClassList.addItem(obj);
    }

    @Override
    public void componentClosed() {
        cmbClassList.removeActionListener(this);
        queryScene.removeActionListener(qbs);
        queryScene.clear();
        cmbClassList.setSelectedItem(null);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0"); //NOI18N
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) { }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version"); //NOI18N
        // TODO read your settings according to their version
    }

    public NotificationUtil getNotifier(){
        return NotificationUtil.getInstance();
    }

    public QueryEditorScene getQueryScene() {
        return queryScene;
    }

    public JRadioButton getChkAnd() {
        return chkAnd;
    }

    public JComboBox getCmbClassList(){
        return cmbClassList;
    }

    public JTextField getTxtResultLimit() {
        return txtResultLimit;
    }
    
    /**
     * Called when a different item within the class list is selected
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (!checkForUnsavedQuery(true))
            return;
        LocalClassMetadataLight selectedItem = (LocalClassMetadataLight) ((JComboBox)e.getSource()).getSelectedItem();
        queryScene.clear();
        if(selectedItem != null){
            btnDelete.setEnabled(false);
            LocalClassMetadata myClass = qbs.getClassDetails(selectedItem.getClassName());
            if (myClass != null){
                ClassNodeWidget myNewNode = ((ClassNodeWidget)queryScene.addNode(myClass));
                myNewNode.build(null);
                myNewNode.setPreferredLocation(new Point(100, 50));
                queryScene.setCurrentSearchedClass(myClass);
                qbs.resetLocalQuery();
            }
        }
        queryScene.validate();
    }

    /**
     * Checks if the current query is valid (has elements, the parameters are correct)
     * @return valid or not valid
     */
    private boolean validateQuery() {
        //The limit must be an integer
        try{
            Integer.valueOf(txtResultLimit.getText());
        } catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "The result limit is not valid",
                    "Search Error",JOptionPane.ERROR_MESSAGE);
            return false;
        }
        //The query must not be empty
        if(queryScene.getNodes().isEmpty()){
            JOptionPane.showMessageDialog(this, "Nothing to do here",
                    "Search Error",JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

        public boolean checkForUnsavedQuery(boolean showCancel) {
        if (!isSaved){
            switch (JOptionPane.showConfirmDialog(null, "This query has not been saved, do you want to save it?",
                    "Confirmation",showCancel?JOptionPane.YES_NO_CANCEL_OPTION:JOptionPane.YES_NO_OPTION)){
                case JOptionPane.YES_OPTION:
                    btnSaveActionPerformed(new ActionEvent(this, 0, "close")); //NOI18N
                    break;
                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }
        isSaved = true;
        return true;
    }
}
