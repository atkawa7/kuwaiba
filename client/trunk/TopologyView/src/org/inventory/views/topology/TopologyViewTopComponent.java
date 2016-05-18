/*
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

package org.inventory.views.topology;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.inventory.views.graphical.dialogs.CreateTopologyPanel;
import org.inventory.views.graphical.dialogs.TopologyListPanel;
import org.inventory.views.topology.scene.TopologyViewScene;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd="-//org.inventory.views.topology//TopologyView//EN",
    autostore=false
)
public final class TopologyViewTopComponent extends TopComponent implements ActionListener, Provider, Refreshable{
    private static TopologyViewTopComponent instance;
    /** 
     * path to the icon used by the component and its open action
     */
    static final String ICON_PATH = "org/inventory/views/topology/res/icon.png";
    
    private static final String PREFERRED_ID = "TopologyViewTopComponent";
    /**
     * Main scene
     */
    private TopologyViewScene scene;
    /**
     * TC Explorer Manager
     */
    private ExplorerManager em = new ExplorerManager();
    /**
     * Topology view service
     */
    private TopologyViewService tvsrv;
  
    private boolean isSaved = false;

    public TopologyViewTopComponent() {
        initComponents();
        initiCustomComponents();
        setName(NbBundle.getMessage(TopologyViewTopComponent.class, "CTL_TopologyViewTopComponent"));
        setToolTipText(NbBundle.getMessage(TopologyViewTopComponent.class, "HINT_TopologyViewTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }
    
    public final void initiCustomComponents(){
        scene = new TopologyViewScene(getNotifier());
        tvsrv = new TopologyViewService(scene, this);
        pnlMainScrollPanel.setViewportView(scene.createView());
        associateLookup(scene.getLookup());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMainScrollPanel = new javax.swing.JScrollPane();
        jToolBar1 = new javax.swing.JToolBar();
        btnNewTopology = new javax.swing.JButton();
        btnOpen = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnSelect = new javax.swing.JToggleButton();
        btnShowNodesLabels = new javax.swing.JToggleButton();
        btnConnect = new javax.swing.JToggleButton();
        btnCloud = new javax.swing.JButton();
        btnFrame = new javax.swing.JButton();
        btnAddFreeLabel = new javax.swing.JButton();

        jToolBar1.setRollover(true);

        btnNewTopology.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNewTopology, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnNewTopology.text")); // NOI18N
        btnNewTopology.setFocusable(false);
        btnNewTopology.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNewTopology.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNewTopology.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewTopologyActionPerformed(evt);
            }
        });
        jToolBar1.add(btnNewTopology);

        btnOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/open.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnOpen, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnOpen.text")); // NOI18N
        btnOpen.setFocusable(false);
        btnOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOpenActionPerformed(evt);
            }
        });
        jToolBar1.add(btnOpen);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnSave.text")); // NOI18N
        btnSave.setEnabled(false);
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        jToolBar1.add(btnSave);

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/delete.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnDelete, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnDelete.text")); // NOI18N
        btnDelete.setEnabled(false);
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        jToolBar1.add(btnDelete);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnExport.text")); // NOI18N
        btnExport.setEnabled(false);
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });
        jToolBar1.add(btnExport);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/select.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setEnabled(false);
        btnSelect.setFocusable(false);
        btnSelect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSelect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        jToolBar1.add(btnSelect);

        btnShowNodesLabels.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/hide_node_labels.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnShowNodesLabels, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnShowNodesLabels.text")); // NOI18N
        btnShowNodesLabels.setEnabled(false);
        btnShowNodesLabels.setFocusable(false);
        btnShowNodesLabels.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowNodesLabels.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowNodesLabels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowNodesLabelsActionPerformed(evt);
            }
        });
        jToolBar1.add(btnShowNodesLabels);

        btnConnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/connect.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnConnect, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnConnect.text")); // NOI18N
        btnConnect.setEnabled(false);
        btnConnect.setFocusable(false);
        btnConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        jToolBar1.add(btnConnect);

        btnCloud.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/cloud.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnCloud, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnCloud.text")); // NOI18N
        btnCloud.setFocusable(false);
        btnCloud.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCloud.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCloud.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloudActionPerformed(evt);
            }
        });
        jToolBar1.add(btnCloud);

        btnFrame.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/frame.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnFrame, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnFrame.text")); // NOI18N
        btnFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFrameActionPerformed(evt);
            }
        });
        jToolBar1.add(btnFrame);

        btnAddFreeLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/label.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAddFreeLabel, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnAddFreeLabel.text")); // NOI18N
        btnAddFreeLabel.setFocusable(false);
        btnAddFreeLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddFreeLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddFreeLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddFreeLabelActionPerformed(evt);
            }
        });
        jToolBar1.add(btnAddFreeLabel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlMainScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMainScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        btnConnect.setSelected(false);
        scene.setActiveTool(TopologyViewScene.ACTION_SELECT);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnShowNodesLabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowNodesLabelsActionPerformed
        scene.toggleLabels(!btnShowNodesLabels.isSelected());
    }//GEN-LAST:event_btnShowNodesLabelsActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        btnSelect.setSelected(false);
        scene.setActiveTool(TopologyViewScene.ACTION_CONNECT);
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnCloudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloudActionPerformed
        scene.addFreeCloud();
    }//GEN-LAST:event_btnCloudActionPerformed

    private void btnFrameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFrameActionPerformed
        scene.addFreeFrame();
    }//GEN-LAST:event_btnFrameActionPerformed

    private void btnAddFreeLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddFreeLabelActionPerformed
        scene.addFreeLabel();
    }//GEN-LAST:event_btnAddFreeLabelActionPerformed

    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed
        if (!checkForUnsavedView(true))
            return;
         LocalObjectViewLight[] topologyViews = tvsrv.getTopologyViews();

         final TopologyListPanel tlp = new TopologyListPanel(topologyViews);
         DialogDescriptor dd = new DialogDescriptor(tlp, "Choose a view", true, new ActionListener(){
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (e.getSource() == DialogDescriptor.OK_OPTION){
                                    if (tlp.getSelectedView() != null){
                                        tvsrv.loadTopologyView(tlp.getSelectedView());
                                        toggleButtons(true);
                                        if(btnConnect.isSelected())
                                            btnConnect.setSelected(false);
                                    }
                                    else
                                        JOptionPane.showConfirmDialog(null, "Select a view, please","Error", JOptionPane.ERROR_MESSAGE);
                                }
                                tlp.releaseListeners();
                            }
                        });
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnOpenActionPerformed

    private void btnNewTopologyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewTopologyActionPerformed
        if (scene.getNodes().isEmpty()){
            toggleButtons(true);
        }
        else {
            switch (JOptionPane.showConfirmDialog(this, "This topology has not been saved, do you want to save it?",
                    "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION)){
                case JOptionPane.NO_OPTION:
                    scene.clear();
                    isSaved=false;
                    tvsrv.setTvId(-1);
                    break;
                case JOptionPane.YES_OPTION:
                    btnSaveActionPerformed(new ActionEvent(this, 0, "close"));
                    break;
                case JOptionPane.CANCEL_OPTION:
                    break;
            }  
        
        }
    }//GEN-LAST:event_btnNewTopologyActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (!validateTopology())
            return;
        if(!scene.getNodes().isEmpty()){
            final CreateTopologyPanel cqp = new CreateTopologyPanel((String)tvsrv.getViewProperties()[0],
                                        (String)tvsrv.getViewProperties()[1]);
            DialogDescriptor dd = new DialogDescriptor(cqp,
                    "Set view settings", true, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e){
                                    if (e.getSource() == DialogDescriptor.OK_OPTION){
                                        tvsrv.setViewProperties(cqp.getValues());
                                        tvsrv.saveView();
                                        isSaved = true;
                                    }
                                }
                            });
            DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        }else{
            tvsrv.saveView();
            isSaved = true;
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the current topology?",
                "Delete saved view",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
            tvsrv.deleteView();
            scene.clear();
            tvsrv.setTvId(0);
            toggleButtons(false);
            btnSelectActionPerformed(evt);
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        ExportScenePanel exportPanel = new ExportScenePanel(new SceneExportFilter[]{ImageFilter.getInstance()}, scene);
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options",true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAddFreeLabel;
    private javax.swing.JButton btnCloud;
    private javax.swing.JToggleButton btnConnect;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnFrame;
    private javax.swing.JButton btnNewTopology;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnSave;
    private javax.swing.JToggleButton btnSelect;
    private javax.swing.JToggleButton btnShowNodesLabels;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JScrollPane pnlMainScrollPanel;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized TopologyViewTopComponent getDefault() {
        if (instance == null) {
            instance = new TopologyViewTopComponent();
        }
        return instance;
    }

     private boolean validateTopology() {
        //The view must not be empty
        if(scene.getNodes().isEmpty()){
            JOptionPane.showMessageDialog(this, "Theres nothing to save",
                    "Search Error",JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    /**
     * Obtain the TopologyViewTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized TopologyViewTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(TopologyViewTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof TopologyViewTopComponent) {
            return (TopologyViewTopComponent) win;
        }
        Logger.getLogger(TopologyViewTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public boolean canClose(){
        if(!scene.getNodes().isEmpty() && !isSaved)
            return checkForUnsavedView(true);
        return true;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        toggleButtons(false);
        scene.addActionListener(this);
    }

    @Override
    public void componentClosed() {
        scene.clear();
        tvsrv.setViewProperties(new Object[2]);
        tvsrv.setTvId(0);
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
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    public TopologyViewScene getScene() {
        return scene;
    }

    public boolean checkForUnsavedView(boolean showCancel) {
        if(!scene.getNodes().isEmpty() && !isSaved){
            switch (JOptionPane.showConfirmDialog(null, "This topology view has not been saved, do you want to save it?",
                    "Confirmation",showCancel?JOptionPane.YES_NO_CANCEL_OPTION:JOptionPane.YES_NO_OPTION)){
                case JOptionPane.YES_OPTION:
                    btnSaveActionPerformed(new ActionEvent(this, 0, "close")); //NOI18N
                    isSaved = true;
                    return true;
                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }
        return true;
    }

    public NotificationUtil getNotifier(){
        return NotificationUtil.getInstance();
    }

    public void toggleButtons(boolean enabled) {
        btnConnect.setEnabled(enabled);
        btnSelect.setEnabled(enabled);
        btnExport.setEnabled(enabled);
        btnShowNodesLabels.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnSelect.setSelected(enabled);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch(e.getID()){
            case TopologyViewScene.SCENE_OBJECTADDED:
                toggleButtons(true);
                if(btnConnect.isSelected()){
                    btnConnect.setSelected(true);
                    btnSelect.setSelected(false);
    }
                break;
        }
    }

    @Override
    public void refresh() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
