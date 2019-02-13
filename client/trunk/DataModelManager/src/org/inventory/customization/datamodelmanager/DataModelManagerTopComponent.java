/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.customization.datamodelmanager;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.export.GenericExportPanel;
import org.inventory.core.services.api.export.filters.XMLExportFilter;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.customization.classhierarchy.ClassHierarchyTopComponent;
import org.inventory.customization.classhierarchy.nodes.ClassMetadataChildren;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.windows.TopComponent;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 * Data model manager Top component.
 *
 * @author Adrian Martinez Molina {@literal <charles.bedon@kuwaiba.org>}
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.customization.datamodelmanager//DataModelManager//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "DataModelManagerTopComponent",
        iconBase = "org/inventory/customization/datamodelmanager/res/icon.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Tools", id = "org.inventory.customization.datamodelmanager.DataModelManagerTopComponent")
@ActionReferences(value = {
    @ActionReference(path = "Menu/Tools/Administration")
    ,
    @ActionReference(path = "Toolbars/04_Customization", position = 1)})
@TopComponent.OpenActionRegistration(
        displayName = "#DataModelManager.module.displayname",
        preferredID = "DataModelManagerTopComponent")

public final class DataModelManagerTopComponent extends TopComponent
        implements ExplorerManager.Provider, Refreshable, ActionListener {

    private final ExplorerManager em = new ExplorerManager();
    private DataModelManagerService dmms;

    public DataModelManagerTopComponent() {
        initComponents();
        setName(I18N.gm("DataModelManager.module.name"));
        setToolTipText(I18N.gm("DataModelManager.module.tooltiptext"));
        initComponentsCustom();
    }

    public void initComponentsCustom() {
        dmms = new DataModelManagerService(this);
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        treeView = new BeanTreeView();
        treeView.setRootVisible(false);
        add(treeView);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBarMain = new javax.swing.JToolBar();
        btndefaultDataModelManaget = new javax.swing.JButton();
        btnshowClassHierarchyView = new javax.swing.JButton();
        btnExportDatabase = new javax.swing.JButton();
        lblSearch = new javax.swing.JLabel();
        cmbClassList = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout());

        toolBarMain.setRollover(true);
        toolBarMain.setMaximumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setMinimumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setPreferredSize(new java.awt.Dimension(392, 38));

        btndefaultDataModelManaget.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/customization/datamodelmanager/res/defaultDataModelManager.png"))); // NOI18N
        btndefaultDataModelManaget.setToolTipText(I18N.gm("reset_to_initial_position")); // NOI18N
        btndefaultDataModelManaget.setFocusable(false);
        btndefaultDataModelManaget.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btndefaultDataModelManaget.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btndefaultDataModelManaget.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btndefaultDataModelManagetActionPerformed(evt);
            }
        });
        toolBarMain.add(btndefaultDataModelManaget);
        btndefaultDataModelManaget.getAccessibleContext().setAccessibleDescription(I18N.gm("reset_to_initial_position")); // NOI18N

        btnshowClassHierarchyView.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/customization/datamodelmanager/res/classHierarchyView.png"))); // NOI18N
        btnshowClassHierarchyView.setToolTipText(I18N.gm("open_graphical_representation_tree")); // NOI18N
        btnshowClassHierarchyView.setFocusable(false);
        btnshowClassHierarchyView.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnshowClassHierarchyView.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnshowClassHierarchyView.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnshowClassHierarchyViewActionPerformed(evt);
            }
        });
        toolBarMain.add(btnshowClassHierarchyView);
        btnshowClassHierarchyView.getAccessibleContext().setAccessibleDescription(I18N.gm("open_graphical_representation_tree")); // NOI18N

        btnExportDatabase.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/customization/datamodelmanager/res/defaultExportDB.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExportDatabase, org.openide.util.NbBundle.getMessage(DataModelManagerTopComponent.class, "DataModelManagerTopComponent.btnExportDatabase.text")); // NOI18N
        btnExportDatabase.setToolTipText("Export Data Base");
        btnExportDatabase.setFocusable(false);
        btnExportDatabase.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExportDatabase.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExportDatabase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportDatabaseActionPerformed(evt);
            }
        });
        toolBarMain.add(btnExportDatabase);

        org.openide.awt.Mnemonics.setLocalizedText(lblSearch, I18N.gm("search")); // NOI18N
        lblSearch.setPreferredSize(new java.awt.Dimension(70, 15));
        toolBarMain.add(lblSearch);
        lblSearch.getAccessibleContext().setAccessibleName(I18N.gm("search")); // NOI18N

        cmbClassList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                cmbClassListMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                cmbClassListMouseEntered(evt);
            }
        });
        toolBarMain.add(cmbClassList);

        add(toolBarMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnshowClassHierarchyViewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnshowClassHierarchyViewActionPerformed
        ClassHierarchyTopComponent classHierarchyTC = (ClassHierarchyTopComponent) WindowManager.getDefault().findTopComponent("ClassHierarchyTopComponent");

        if (classHierarchyTC == null) {
            classHierarchyTC = new ClassHierarchyTopComponent();
            classHierarchyTC.open();
        } else {
            if (classHierarchyTC.isOpened()) {
                classHierarchyTC.requestAttention(true);
            } else { //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                //so we will reuse the instance, refreshing the vierw first
                classHierarchyTC.refresh();
                classHierarchyTC.open();
            }
        }
        classHierarchyTC.requestActive();
    }//GEN-LAST:event_btnshowClassHierarchyViewActionPerformed

    private void btndefaultDataModelManagetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btndefaultDataModelManagetActionPerformed
        LocalClassMetadataLight[] allMeta = dmms.getRootChildren();
        em.setRootContext(new AbstractNode(new ClassMetadataChildren(allMeta)));
    }//GEN-LAST:event_btndefaultDataModelManagetActionPerformed

    private void cmbClassListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmbClassListMouseClicked
        cmbClassList.removeAllItems();
        cmbClassList.addItem(null);
        for (LocalClassMetadataLight node : dmms.getRoots()) {
            cmbClassList.addItem(node);
        }
    }//GEN-LAST:event_cmbClassListMouseClicked

    private void cmbClassListMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_cmbClassListMouseEntered

    }//GEN-LAST:event_cmbClassListMouseEntered

    private void btnExportDatabaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportDatabaseActionPerformed
        GenericExportPanel exportPanel = new GenericExportPanel( new XMLExportFilter[] {XMLExportFilter.getInstance()}, "ExportDB");
        DialogDescriptor dd = new DialogDescriptor(exportPanel, I18N.gm("export_options"), true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportDatabaseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExportDatabase;
    private javax.swing.JButton btndefaultDataModelManaget;
    private javax.swing.JButton btnshowClassHierarchyView;
    private javax.swing.JComboBox cmbClassList;
    private javax.swing.JLabel lblSearch;
    private javax.swing.JToolBar toolBarMain;
    // End of variables declaration//GEN-END:variables
    private BeanTreeView treeView;

    @Override
    public void componentOpened() {
        setRoot();
        ExplorerUtils.activateActions(em, true);

        cmbClassList.addActionListener(this);

        cmbClassList.addItem(null);
        for (LocalClassMetadataLight node : dmms.getRoots()) {
            cmbClassList.addItem(node);
        }
    }

    @Override
    public void componentClosed() {
        ExplorerUtils.activateActions(em, false);
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
        //Workaround, because when you close a TC whose mode is "explorer" and open it again,
        //it docks as "explorer". This forces the TC to be always docked "explorer"
        Mode myMode = WindowManager.getDefault().findMode("explorer"); //NOI18N
        myMode.dockInto(this);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    public void setRoot() {
        LocalClassMetadataLight[] allMeta = dmms.getRootChildren();
        em.setRootContext(new AbstractNode(new ClassMetadataChildren(allMeta)));
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public void refresh() {

    }

    public NotificationUtil getNotifier() {
        return NotificationUtil.getInstance();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if ("comboBoxChanged".equals(e.getActionCommand())) {
            comboBoxChanged(e);
        }
    }

    public void comboBoxChanged(ActionEvent e) {
        LocalClassMetadataLight selectedItem = (LocalClassMetadataLight) ((JComboBox) e.getSource()).getSelectedItem();
        if (selectedItem != null) {
            em.setRootContext(new AbstractNode(new ClassMetadataChildren(new LocalClassMetadataLight[]{selectedItem})));
        }
    }
}
