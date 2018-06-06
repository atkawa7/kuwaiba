/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.sdh;

import com.neotropic.inventory.modules.sdh.scene.SDHModuleScene;
import com.neotropic.inventory.modules.sdh.wizard.SDHConnectionWizard;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.JComplexDialogPanel;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.api.visual.widget.Widget;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.explorer.ExplorerManager;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component to display the SDH module tools
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ConvertAsProperties(
        dtd = "-//com.neotropic.inventory.modules.sdh//SDHModule//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "SDHModuleTopComponent",
        iconBase = "com/neotropic/inventory/modules/sdh/res/icon.png",
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Window", id = "com.neotropic.inventory.modules.sdh.SDHModuleTopComponent")
@ActionReferences(value = {@ActionReference(path = "Menu/Tools/Advanced"),
    @ActionReference(path = "Toolbars/10_Advanced", position = 4)})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_SDHModuleAction",
        preferredID = "SDHModuleTopComponent"
)
@Messages({
    "CTL_SDHModuleAction=SDH Networks",
    "CTL_SDHModuleTopComponent=SDH Networks",
    "HINT_SDHModuleTopComponent=SDHModule"
})
public final class SDHModuleTopComponent extends TopComponent implements ExplorerManager.Provider, Refreshable, ActionListener {
    
    private static final String ICON_PATH = "com/neotropic/inventory/modules/sdh/res/icon.png";
    private ExplorerManager em;
    private SDHModuleScene scene;
    private SDHModuleService service;
    private SDHConfigurationObject configObject;

    public SDHModuleTopComponent() {
        initComponents();
        initCustomComponents();
        setName(Bundle.CTL_SDHModuleTopComponent());
        setToolTipText(Bundle.HINT_SDHModuleTopComponent());
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }
    
    public void initCustomComponents() {
        em = new ExplorerManager();
        scene = new SDHModuleScene();
        service = new SDHModuleService(scene);
        
        associateLookup(scene.getLookup());
        
        configObject = Lookup.getDefault().lookup(SDHConfigurationObject.class);
        configObject.setProperty("connectionType", SDHConnectionWizard.Connections.CONNECTION_TRANSPORTLINK);
        configObject.setProperty("saved", true);
        
        scene.setActiveTool(SDHModuleScene.ACTION_SELECT);
        
        pnlScrollMain.setViewportView(scene.createView());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGrpConnections = new javax.swing.ButtonGroup();
        btnGrpTools = new javax.swing.ButtonGroup();
        barTools = new javax.swing.JToolBar();
        btnNew = new javax.swing.JButton();
        btnOpen = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        sep1 = new javax.swing.JToolBar.Separator();
        btnSelect = new javax.swing.JToggleButton();
        btnConnect = new javax.swing.JToggleButton();
        btnShowConnectionLabels = new javax.swing.JToggleButton();
        btnAddBackground = new javax.swing.JButton();
        btnRemoveBackground = new javax.swing.JButton();
        sep2 = new javax.swing.JToolBar.Separator();
        btnTransportLink = new javax.swing.JToggleButton();
        btnContainerLink = new javax.swing.JToggleButton();
        btnTributaryLink = new javax.swing.JToggleButton();
        pnlScrollMain = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        barTools.setRollover(true);

        btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNew, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnNew.text")); // NOI18N
        btnNew.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnNew.toolTipText")); // NOI18N
        btnNew.setFocusable(false);
        btnNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });
        barTools.add(btnNew);

        btnOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/open.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnOpen, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnOpen.text")); // NOI18N
        btnOpen.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnOpen.toolTipText")); // NOI18N
        btnOpen.setFocusable(false);
        btnOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenActionPerformed(evt);
            }
        });
        barTools.add(btnOpen);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        barTools.add(btnSave);

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/delete.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnDelete, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnDelete.text")); // NOI18N
        btnDelete.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnDelete.toolTipText")); // NOI18N
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        barTools.add(btnDelete);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });
        barTools.add(btnExport);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRefresh, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnRefresh.text")); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnRefresh.toolTipText")); // NOI18N
        btnRefresh.setFocusable(false);
        btnRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        barTools.add(btnRefresh);
        barTools.add(sep1);

        btnGrpTools.add(btnSelect);
        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/select.png"))); // NOI18N
        btnSelect.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnSelect.toolTipText")); // NOI18N
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
        btnConnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/connect.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnConnect, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnConnect.text")); // NOI18N
        btnConnect.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnConnect.toolTipText")); // NOI18N
        btnConnect.setFocusable(false);
        btnConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        barTools.add(btnConnect);

        btnShowConnectionLabels.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/hide_conn_labels.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnShowConnectionLabels, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnShowConnectionLabels.text")); // NOI18N
        btnShowConnectionLabels.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnShowConnectionLabels.toolTipText")); // NOI18N
        btnShowConnectionLabels.setFocusable(false);
        btnShowConnectionLabels.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowConnectionLabels.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowConnectionLabels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowConnectionLabelsActionPerformed(evt);
            }
        });
        barTools.add(btnShowConnectionLabels);

        btnAddBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/add-background.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAddBackground, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnAddBackground.text")); // NOI18N
        btnAddBackground.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnAddBackground.toolTipText")); // NOI18N
        btnAddBackground.setFocusable(false);
        btnAddBackground.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddBackground.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddBackgroundActionPerformed(evt);
            }
        });
        barTools.add(btnAddBackground);

        btnRemoveBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/remove-background.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveBackground, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnRemoveBackground.text")); // NOI18N
        btnRemoveBackground.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnRemoveBackground.toolTipText")); // NOI18N
        btnRemoveBackground.setFocusable(false);
        btnRemoveBackground.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRemoveBackground.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRemoveBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveBackgroundActionPerformed(evt);
            }
        });
        barTools.add(btnRemoveBackground);
        barTools.add(sep2);

        btnGrpConnections.add(btnTransportLink);
        btnTransportLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/btnTransportLink.png"))); // NOI18N
        btnTransportLink.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnTransportLink, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnTransportLink.text")); // NOI18N
        btnTransportLink.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnTransportLink.toolTipText")); // NOI18N
        btnTransportLink.setFocusable(false);
        btnTransportLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnTransportLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnTransportLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTransportLinkActionPerformed(evt);
            }
        });
        barTools.add(btnTransportLink);

        btnGrpConnections.add(btnContainerLink);
        btnContainerLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/btnContainerLink.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnContainerLink, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnContainerLink.text")); // NOI18N
        btnContainerLink.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnContainerLink.toolTipText")); // NOI18N
        btnContainerLink.setFocusable(false);
        btnContainerLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnContainerLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnContainerLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContainerLinkActionPerformed(evt);
            }
        });
        barTools.add(btnContainerLink);

        btnGrpConnections.add(btnTributaryLink);
        btnTributaryLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/sdh/res/btnTributaryLink.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnTributaryLink, org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnTributaryLink.text")); // NOI18N
        btnTributaryLink.setToolTipText(org.openide.util.NbBundle.getMessage(SDHModuleTopComponent.class, "SDHModuleTopComponent.btnTributaryLink.toolTipText")); // NOI18N
        btnTributaryLink.setFocusable(false);
        btnTributaryLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnTributaryLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnTributaryLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTributaryLinkActionPerformed(evt);
            }
        });
        barTools.add(btnTributaryLink);

        add(barTools, java.awt.BorderLayout.PAGE_START);
        add(pnlScrollMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        if (!(boolean)configObject.getProperty("saved")) {
            switch (JOptionPane.showConfirmDialog(this, "This topology has not been saved, do you want to save it?",
                I18N.gm("confirmation"), JOptionPane.YES_NO_CANCEL_OPTION)){
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
                I18N.gm("confirmation"), JOptionPane.YES_NO_CANCEL_OPTION)){
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
                    setHtmlDisplayName(getDisplayName());
                    configObject.setProperty("saved", true);
                    scene.render(actualView.getStructure());
                    scene.setBackgroundImage(actualView.getBackground());
                    enableButtons(true);
                    btnConnect.setSelected(false);
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
            
            JComplexDialogPanel saveDialog = new JComplexDialogPanel(new String[] {"View Name", "View Description"}, new JComponent[] { txtViewName, txtViewDescription });

            if(JOptionPane.showConfirmDialog(null, saveDialog, "Save View", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                //It's a new view
                if (service.getView() == null)
                    service.setView(new LocalObjectView(-1, SDHModuleService.CLASS_VIEW, 
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
        ExportScenePanel exportPanel = new ExportScenePanel(new SceneExportFilter[]{ ImageFilter.getInstance() }, 
                scene, getDisplayName());
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export Options",true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        scene.setActiveTool(SDHModuleScene.ACTION_SELECT);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        scene.setActiveTool(SDHModuleScene.ACTION_CONNECT);
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnTransportLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTransportLinkActionPerformed
        configObject.setProperty("connectionType", SDHConnectionWizard.Connections.CONNECTION_TRANSPORTLINK);
    }//GEN-LAST:event_btnTransportLinkActionPerformed

    private void btnContainerLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContainerLinkActionPerformed
        configObject.setProperty("connectionType", SDHConnectionWizard.Connections.CONNECTION_CONTAINERLINK);
    }//GEN-LAST:event_btnContainerLinkActionPerformed

    private void btnTributaryLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTributaryLinkActionPerformed
        configObject.setProperty("connectionType", SDHConnectionWizard.Connections.CONNECTION_TRIBUTARYLINK);
    }//GEN-LAST:event_btnTributaryLinkActionPerformed

    private void btnShowConnectionLabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowConnectionLabelsActionPerformed
        scene.toggleConnectionLabels(!btnShowConnectionLabels.isSelected());
    }//GEN-LAST:event_btnShowConnectionLabelsActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        if (!(boolean)configObject.getProperty("saved")) {
            switch (JOptionPane.showConfirmDialog(this, "This topology has not been saved, do you want to save it?",
                I18N.gm("confirmation"), JOptionPane.YES_NO_CANCEL_OPTION)){
                case JOptionPane.YES_OPTION:
                    btnSaveActionPerformed(new ActionEvent(this, 0, "close"));
                    break;
                case JOptionPane.CANCEL_OPTION:
                    return;
            }
        }
        
        for (LocalObjectLight node : scene.getNodes()) {
            Widget widget = scene.findWidget(node);
            
            if (widget instanceof ObjectNodeWidget) {
                ((ObjectNodeWidget) widget).getLabelWidget().setLabel(node.toString());
                
                widget.revalidate();
                widget.repaint();
            }            
        }
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnAddBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBackgroundActionPerformed
        JFileChooser fChooser = Utils.getGlobalFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setFileFilter(new FileNameExtensionFilter("Image files", "gif","jpg", "png"));
        if (fChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                Image myBackgroundImage = ImageIO.read(new File(fChooser.getSelectedFile().getAbsolutePath()));
                scene.setBackgroundImage(myBackgroundImage);
                scene.fireChangeEvent(new ActionEvent(this, AbstractScene.SCENE_CHANGE, "Add Background"));
            } catch (IOException ex) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
            }
        }
    }//GEN-LAST:event_btnAddBackgroundActionPerformed

    private void btnRemoveBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveBackgroundActionPerformed
        scene.setBackgroundImage(null);
        scene.fireChangeEvent(new ActionEvent(this, AbstractScene.SCENE_CHANGE, "Remove Background"));
    }//GEN-LAST:event_btnRemoveBackgroundActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barTools;
    private javax.swing.JButton btnAddBackground;
    private javax.swing.JToggleButton btnConnect;
    private javax.swing.JToggleButton btnContainerLink;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnExport;
    private javax.swing.ButtonGroup btnGrpConnections;
    private javax.swing.ButtonGroup btnGrpTools;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRemoveBackground;
    private javax.swing.JButton btnSave;
    private javax.swing.JToggleButton btnSelect;
    private javax.swing.JToggleButton btnShowConnectionLabels;
    private javax.swing.JToggleButton btnTransportLink;
    private javax.swing.JToggleButton btnTributaryLink;
    private javax.swing.JScrollPane pnlScrollMain;
    private javax.swing.JToolBar.Separator sep1;
    private javax.swing.JToolBar.Separator sep2;
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
                I18N.gm("confirmation"), JOptionPane.YES_NO_OPTION)){
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
        btnTransportLink.setEnabled(enabled);
        btnContainerLink.setEnabled(enabled);
        btnTributaryLink.setEnabled(enabled);
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
