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
import javax.swing.JComboBox;
import org.inventory.core.services.interfaces.LocalClassMetadata;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.NotificationUtil;
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
    private NextGenerationQueryBuilderService qbs;

    public QueryBuilderTopComponent() {
        initComponents();
        initCustomComponents();
        setName(NbBundle.getMessage(QueryBuilderTopComponent.class, "CTL_QueryBuilderTopComponent"));
        setToolTipText(NbBundle.getMessage(QueryBuilderTopComponent.class, "HINT_QueryBuilderTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

    private void initCustomComponents(){
        qbs = new NextGenerationQueryBuilderService(this);
        cmbClassList.addItem(null);
        queryScene = new QueryEditorScene();
        pnlMainScrollPanel.setViewportView(queryScene.createView());
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
        btnSearch = new javax.swing.JButton();
        btnButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());
        add(pnlMainScrollPanel, java.awt.BorderLayout.CENTER);

        barMain.setRollover(true);

        org.openide.awt.Mnemonics.setLocalizedText(lblSearch, org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.lblSearch.text")); // NOI18N
        barMain.add(lblSearch);

        barMain.add(cmbClassList);

        btnSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/search.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSearch, org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.btnSearch.text")); // NOI18N
        btnSearch.setFocusable(false);
        btnSearch.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSearch.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barMain.add(btnSearch);

        btnButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/queries/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnButton, org.openide.util.NbBundle.getMessage(QueryBuilderTopComponent.class, "QueryBuilderTopComponent.btnButton.text")); // NOI18N
        btnButton.setFocusable(false);
        btnButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        barMain.add(btnButton);

        add(barMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barMain;
    private javax.swing.JButton btnButton;
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox cmbClassList;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JScrollPane pnlMainScrollPanel;
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
        for (Object obj : qbs.getClassList())
            cmbClassList.addItem(obj);
    }

    @Override
    public void componentClosed() {
        cmbClassList.removeActionListener(this);
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

    public void actionPerformed(ActionEvent e) {
        LocalClassMetadataLight selectedItem = (LocalClassMetadataLight) ((JComboBox)e.getSource()).getSelectedItem();
        queryScene.removeChildren();

        if(selectedItem != null){
            LocalClassMetadata myClass = qbs.getClassDetails(selectedItem.getClassName());
            if (myClass != null)
                queryScene.addNode(myClass).setPreferredLocation(new Point(100, 100));
        }
        queryScene.validate();
        queryScene.repaint();
    }
}
