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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import org.inventory.communications.core.LocalResultRecord;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.queries.graphical.ComplexQueryResultTopComponent;
import org.inventory.queries.graphical.QueryEditorScene;
import org.inventory.queries.graphical.elements.ClassNodeWidget;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Lookup;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.inventory.queries//QueryBuilder//EN",
autostore = false)
public final class QueryBuilderTopComponent extends TopComponent implements ActionListener{

    private static QueryBuilderTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/inventory/queries/res/icon2.png";
    private static final String PREFERRED_ID = "QueryBuilderTopComponent";
    private QueryEditorScene queryScene;
    private NotificationUtil nu;
    private GraphicalQueryBuilderService qbs;
    private ButtonGroup grpLogicalConnector;

    public QueryBuilderTopComponent() {
        initComponents();
        initCustomComponents();
        setName(NbBundle.getMessage(QueryBuilderTopComponent.class, "CTL_QueryBuilderTopComponent"));
        setToolTipText(NbBundle.getMessage(QueryBuilderTopComponent.class, "HINT_QueryBuilderTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

    private void initCustomComponents(){
        queryScene = new QueryEditorScene();
        qbs = new GraphicalQueryBuilderService(this);
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
        btnButton = new javax.swing.JButton();
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

        org.openide.awt.Mnemonics.setLocalizedText(lblSearch, org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.lblSearch.text")); // NOI18N
        lblSearch.setPreferredSize(new java.awt.Dimension(70, 15));
        barMain.add(lblSearch);

        barMain.add(cmbClassList);
        barMain.add(sptOne);

        btnOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/open.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnOpen, org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.btnOpen.text")); // NOI18N
        btnOpen.setToolTipText(org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.btnOpen.toolTipText")); // NOI18N
        btnOpen.setFocusable(false);
        btnOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barMain.add(btnOpen);

        btnButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnButton, org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.btnButton.text")); // NOI18N
        btnButton.setToolTipText(org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.btnButton.toolTipText")); // NOI18N
        btnButton.setFocusable(false);
        btnButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnButtonActionPerformed(evt);
            }
        });
        barMain.add(btnButton);

        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/run-search.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSearch, org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.btnSearch.text")); // NOI18N
        btnSearch.setToolTipText(org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.btnSearch.toolTipText")); // NOI18N
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

        org.openide.awt.Mnemonics.setLocalizedText(lblConnector, org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.lblConnector.text")); // NOI18N
        barMain.add(lblConnector);

        chkAnd.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(chkAnd, org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.chkAnd.text")); // NOI18N
        chkAnd.setToolTipText(org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.chkAnd.toolTipText")); // NOI18N
        chkAnd.setFocusable(false);
        barMain.add(chkAnd);

        org.openide.awt.Mnemonics.setLocalizedText(chkOr, org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.chkOr.text")); // NOI18N
        chkOr.setToolTipText(org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.chkOr.toolTipText")); // NOI18N
        chkOr.setFocusable(false);
        chkOr.setMaximumSize(new java.awt.Dimension(50, 27));
        chkOr.setMinimumSize(new java.awt.Dimension(50, 27));
        chkOr.setPreferredSize(new java.awt.Dimension(45, 18));
        barMain.add(chkOr);
        barMain.add(sptThree);

        org.openide.awt.Mnemonics.setLocalizedText(lblResultLimit, org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.lblResultLimit.text")); // NOI18N
        barMain.add(lblResultLimit);

        txtResultLimit.setColumns(4);
        txtResultLimit.setText(org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.txtResultLimit.text")); // NOI18N
        txtResultLimit.setToolTipText(org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.txtResultLimit.toolTipText")); // NOI18N
        barMain.add(txtResultLimit);

        add(barMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        //The limit must be an integer
        try{
            Integer.valueOf(txtResultLimit.getText());
        } catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this, "The result limit is not valid",
                    "Search Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        //The query must not be empty
        if(queryScene.getNodes().isEmpty()){
            JOptionPane.showMessageDialog(this, "There's nothing to search",
                    "Search Error",JOptionPane.ERROR_MESSAGE);
            return;
        }
        LocalResultRecord[] res = qbs.executeQuery(1);
        if (res != null){
            if (res.length == 1) //Remember: the first record is used to set the column names
                JOptionPane.showMessageDialog(null, "No results were found",
                        "Query Results",JOptionPane.INFORMATION_MESSAGE);
            else{
                TopComponent tc = new ComplexQueryResultTopComponent(res,
                        qbs.getCurrentQuery().getLimit(), qbs);
                tc.open();
                tc.requestActive();
                tc.requestAttention(true);
            }
        }
    }//GEN-LAST:event_btnSearchActionPerformed

    private void btnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnButtonActionPerformed
        JOptionPane.showMessageDialog(this, "Not working yet...");
    }//GEN-LAST:event_btnButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barMain;
    private javax.swing.JButton btnButton;
    private javax.swing.JButton btnOpen;
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
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized QueryBuilderTopComponent getDefault() {
        if (instance == null) {
            instance = new QueryBuilderTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the QueryBuilderTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized QueryBuilderTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(QueryBuilderTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof QueryBuilderTopComponent) {
            return (QueryBuilderTopComponent) win;
        }
        Logger.getLogger(QueryBuilderTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

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
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public NotificationUtil getNotifier(){
        if (nu == null)
            return Lookup.getDefault().lookup(NotificationUtil.class);
        return null;
    }

    public QueryEditorScene getQueryScene() {
        return queryScene;
    }

    public JRadioButton getChkAnd() {
        return chkAnd;
    }

    public JTextField getTxtResultLimit() {
        return txtResultLimit;
    }
    

    public void actionPerformed(ActionEvent e) {
        LocalClassMetadataLight selectedItem = (LocalClassMetadataLight) ((JComboBox)e.getSource()).getSelectedItem();
        queryScene.clear();

        if(selectedItem != null){
            LocalClassMetadata myClass = qbs.getClassDetails(selectedItem.getClassName());
            if (myClass != null){
                ClassNodeWidget myNewNode = ((ClassNodeWidget)queryScene.addNode(myClass));
                myNewNode.build(null);
                myNewNode.setPreferredLocation(new Point(100, 50));
                queryScene.setCurrentSearchedClass(myClass);
            }
        }
        queryScene.validate();
    }
}
