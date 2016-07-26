/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.inventory.modules.mpls;

import com.neotropic.inventory.modules.mpls.scene.MPLSModuleScene;
import com.neotropic.inventory.modules.mpls.wizard.MPLSConnectionWizard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component to display the MPLS module tools
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@ConvertAsProperties(
        dtd = "-//com.neotropic.inventory.modules.mpls//MPLSModule//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "MPLSModuleTopComponent",
        iconBase="com/neotropic/inventory/modules/mpls/res/icon.png", 
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.neotropic.inventory.modules.mpls.MPLSModuleTopComponent")
@ActionReference(path = "Menu/Tools/Commercial" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_MPLSModuleAction",
        preferredID = "MPLSModuleTopComponent"
)
@Messages({
    "CTL_MPLSModuleAction=MPLS Module",
    "CTL_MPLSModuleTopComponent= MPLS Module ",
    "HINT_MPLSModuleTopComponent=This is a MPLS Module"
})
public final class MPLSModuleTopComponent extends TopComponent implements ExplorerManager.Provider, Refreshable, ActionListener {

    private static final String ICON_PATH = "com/neotropic/inventory/modules/mpls/res/icon.png";
    private ExplorerManager em;
    private MPLSModuleScene scene;
    private MPLSModuleService service;
    private MPLSConfigurationObject configObject;
    
    public MPLSModuleTopComponent() {
        initComponents();
        initCustomComponents();
        setName(Bundle.CTL_MPLSModuleTopComponent());
        setToolTipText(Bundle.HINT_MPLSModuleTopComponent());
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

     public void initCustomComponents() {
        em = new ExplorerManager();
        scene = new MPLSModuleScene();
        service = new MPLSModuleService(scene);
        
        associateLookup(scene.getLookup());
        
        configObject = Lookup.getDefault().lookup(MPLSConfigurationObject.class);
        configObject.setProperty("connectionType", MPLSConnectionWizard.Connections.CONNECTION_MPLSLINK);
        configObject.setProperty("saved", true);
        
        scene.setActiveTool(MPLSModuleScene.ACTION_SELECT);
        
        add(scene.createView());
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGrpTools = new javax.swing.ButtonGroup();
        barTools = new javax.swing.JToolBar();
        btnNew = new javax.swing.JButton();
        btnOpen = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnSelect = new javax.swing.JToggleButton();
        btnConnect = new javax.swing.JToggleButton();
        jScrollPane1 = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        barTools.setRollover(true);

        btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/mpls/res/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNew, org.openide.util.NbBundle.getMessage(MPLSModuleTopComponent.class, "MPLSModuleTopComponent.btnNew.text")); // NOI18N
        btnNew.setFocusable(false);
        btnNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });
        barTools.add(btnNew);

        btnOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/mpls/res/open.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnOpen, org.openide.util.NbBundle.getMessage(MPLSModuleTopComponent.class, "MPLSModuleTopComponent.btnOpen.text")); // NOI18N
        btnOpen.setFocusable(false);
        btnOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenActionPerformed(evt);
            }
        });
        barTools.add(btnOpen);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/mpls/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(MPLSModuleTopComponent.class, "MPLSModuleTopComponent.btnSave.text")); // NOI18N
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        barTools.add(btnSave);

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/mpls/res/delete.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnDelete, org.openide.util.NbBundle.getMessage(MPLSModuleTopComponent.class, "MPLSModuleTopComponent.btnDelete.text")); // NOI18N
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        barTools.add(btnDelete);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/mpls/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(MPLSModuleTopComponent.class, "MPLSModuleTopComponent.btnExport.text")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });
        barTools.add(btnExport);

        btnGrpTools.add(btnSelect);
        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/mpls/res/select.png"))); // NOI18N
        btnSelect.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(MPLSModuleTopComponent.class, "MPLSModuleTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setFocusable(false);
        btnSelect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSelect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        barTools.add(btnSelect);

        btnGrpTools.add(btnConnect);
        btnConnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/mpls/res/connect.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnConnect, org.openide.util.NbBundle.getMessage(MPLSModuleTopComponent.class, "MPLSModuleTopComponent.btnConnect.text")); // NOI18N
        btnConnect.setFocusable(false);
        btnConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        barTools.add(btnConnect);

        add(barTools, java.awt.BorderLayout.PAGE_START);
        add(jScrollPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        if (!(boolean)configObject.getProperty("saved")) {
            switch (JOptionPane.showConfirmDialog(this, "This topology has not been saved, do you want to save it?",
                "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION)){
                case JOptionPane.YES_OPTION:
                    btnSaveActionPerformed(new ActionEvent(this, 0, "close"));
                    return;
                case JOptionPane.CANCEL_OPTION:
                    return;
                case JOptionPane.NO_OPTION:
            }
        }
        scene.clear();
        enableButtons(true);
        service.setView(null);
        configObject.setProperty("saved", true);
        setHtmlDisplayName(getDisplayName());
    }//GEN-LAST:event_btnNewActionPerformed

    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed
        if (!(boolean)configObject.getProperty("saved")) {
            switch (JOptionPane.showConfirmDialog(this, "This topology has not been saved, do you want to save it?",
                "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION)){
                case JOptionPane.YES_OPTION:
                    btnSaveActionPerformed(new ActionEvent(this, 0, "close"));
                    break;
                case JOptionPane.CANCEL_OPTION:
                    return;
            }
        }
        
        List<LocalObjectViewLight> views = service.getViews();
        JComboBox<LocalObjectViewLight> lstViews = new JComboBox<>(views.toArray(new LocalObjectViewLight[0]));
        lstViews.setName("lstViews"); //NOI18N
        JComplexDialogPanel viewsDialog = new JComplexDialogPanel(new String[] {"Available views"}, new JComponent[] { lstViews });
                
        if(JOptionPane.showConfirmDialog(null, viewsDialog, "Choose a view", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            LocalObjectViewLight selectedView = (LocalObjectViewLight)((JComboBox)viewsDialog.getComponent("lstViews")).getSelectedItem();
            if (selectedView != null) {
                LocalObjectView actualView = service.loadView(selectedView.getId());
                if (actualView != null) {
                    scene.clear();
                    service.setView(actualView);
                    scene.render(actualView.getStructure());
                    enableButtons(true);
                    btnConnect.setSelected(false);
                    configObject.setProperty("saved", true);
                    setHtmlDisplayName(getDisplayName());
                }
            }
        } 
    }//GEN-LAST:event_btnOpenActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (scene.getNodes().isEmpty())
            JOptionPane.showMessageDialog(null, "The view is empty, it won't be saved", "New View", JOptionPane.INFORMATION_MESSAGE);
        else {
            JTextField txtViewName = new JTextField();
            txtViewName.setName("txtViewName");
            txtViewName.setColumns(25);
            
            JTextField txtViewDescription = new JTextField();
            txtViewDescription.setName("txtViewDescription");
            txtViewDescription.setColumns(25);
            
            if (service.getView() != null) {
                txtViewName.setText(service.getView().getName());
                txtViewDescription.setText(service.getView().getDescription());
            }
            
            JComplexDialogPanel saveDialog = new JComplexDialogPanel(new String[] {"View name", "View Description"}, new JComponent[] { txtViewName, txtViewDescription });

            if(JOptionPane.showConfirmDialog(null, saveDialog, "View details", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                //It's a new view
                if (service.getView() == null)
                    service.setView(new LocalObjectView(-1, MPLSModuleService.CLASS_VIEW, 
                            ((JTextField)saveDialog.getComponent("txtViewName")).getText(), 
                            ((JTextField)saveDialog.getComponent("txtViewDescription")).getText(), scene.getAsXML(), null));
                else {
                    service.getView().setName(((JTextField)saveDialog.getComponent("txtViewName")).getText());
                    service.getView().setDescription(((JTextField)saveDialog.getComponent("txtViewDescription")).getText());
                }
                if (service.saveCurrentView()) {
                    NotificationUtil.getInstance().showSimplePopup("Save view", NotificationUtil.INFO_MESSAGE, "View saved successfully");
                    setHtmlDisplayName(getDisplayName());
                }
            }            
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the current topology?",
            "Delete saved view",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            scene.clear();
            if (service.getView() != null && service.getView().getId() != -1) {
                service.deleteView();
                btnSelectActionPerformed(evt);
            }
            service.setView(null);
            configObject.setProperty("saved", true);
            setHtmlDisplayName(getDisplayName());
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        ExportScenePanel exportPanel = new ExportScenePanel(new SceneExportFilter[]{ ImageFilter.getInstance() }, scene);
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options",true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        scene.setActiveTool(MPLSModuleScene.ACTION_SELECT);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        scene.setActiveTool(MPLSModuleScene.ACTION_CONNECT);
    }//GEN-LAST:event_btnConnectActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barTools;
    private javax.swing.JToggleButton btnConnect;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnExport;
    private javax.swing.ButtonGroup btnGrpTools;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnSave;
    private javax.swing.JToggleButton btnSelect;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        scene.addChangeListener(this);
    }

    @Override
    public void componentClosed() {
        scene.removeAllListeners();
        if (!(boolean)configObject.getProperty("saved")) {
            switch (JOptionPane.showConfirmDialog(this, "This topology has not been saved, do you want to save it?",
                "Confirmation", JOptionPane.YES_NO_OPTION)){
                case JOptionPane.YES_OPTION:
                    btnSaveActionPerformed(new ActionEvent(this, 0, "close"));
            }
        }
        scene.clear();
        service.setView(null);
    }

    @Override
    public String getDisplayName() {
        if (service.getView() == null || service.getView().getId() == -1)
            return "Unnamed View";
        else
            return service.getView().getName();
    }
    
    @Override
    public String getHtmlDisplayName() {
        if((boolean)configObject.getProperty("saved"))
            return getDisplayName();
        else
            return String.format("<html><b>%s*</b></html>", getDisplayName());
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

    public void enableButtons(boolean enabled) {
        btnConnect.setEnabled(enabled);
        btnSelect.setEnabled(enabled);
        btnExport.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnSelect.setSelected(enabled);
        
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public void refresh() {
        byte[] currentViewAsXML = scene.getAsXML();
        scene.clear();
        scene.render(currentViewAsXML);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        configObject.setProperty("saved", false);
        setHtmlDisplayName(String.format("<html><b>%s*</b></html>", getDisplayName()));
    }
}
