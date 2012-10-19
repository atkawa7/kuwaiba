/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.visual.LocalObjectViewLight;
import org.inventory.core.visual.actions.ExportSceneAction;
import org.inventory.views.graphical.dialogs.CreateTopologyPanel;
import org.inventory.views.graphical.dialogs.TopologyListPanel;
import org.inventory.views.topology.scene.ObjectNodeWidget;
import org.inventory.views.topology.scene.TopologyViewScene;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.api.visual.widget.Widget;
import org.openide.explorer.ExplorerManager;
import org.openide.util.ImageUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
    dtd="-//org.inventory.views.topology//TopologyView//EN",
    autostore=false
)
public final class TopologyViewTopComponent extends TopComponent implements ExplorerManager.Provider{
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

    private NotificationUtil nu;
    
    private boolean isSaved = false;

    public TopologyViewTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(TopologyViewTopComponent.class, "CTL_TopologyViewTopComponent"));
        setToolTipText(NbBundle.getMessage(TopologyViewTopComponent.class, "HINT_TopologyViewTopComponent"));
        scene = new TopologyViewScene();
        tvsrv = new TopologyViewService(scene, this);
        pnlMainScrollPanel.setViewportView(scene.createView());
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        associateLookup(scene.getLookup());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barToolMain = new javax.swing.JToolBar();
        btnNew = new javax.swing.JButton();
        btnOpen = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnExport = new javax.swing.JButton();
        btnConnect = new javax.swing.JToggleButton();
        btnShowNodeLabels = new javax.swing.JToggleButton();
        btnSelect = new javax.swing.JToggleButton();
        btnCloud = new javax.swing.JToggleButton();
        btnFrame = new javax.swing.JToggleButton();
        btnLabel = new javax.swing.JToggleButton();
        pnlMainScrollPanel = new javax.swing.JScrollPane();

        barToolMain.setRollover(true);

        btnNew.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/add.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnNew, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnNew.text")); // NOI18N
        btnNew.setFocusable(false);
        btnNew.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNew.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewActionPerformed(evt);
            }
        });
        barToolMain.add(btnNew);
        btnNew.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnNew.AccessibleContext.accessibleDescription_2")); // NOI18N

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
        barToolMain.add(btnOpen);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        barToolMain.add(btnSave);

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/delete.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnDelete, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnDelete.text")); // NOI18N
        btnDelete.setToolTipText(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnDelete.toolTipText")); // NOI18N
        btnDelete.setEnabled(false);
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        barToolMain.add(btnDelete);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });
        barToolMain.add(btnExport);
        btnExport.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.jButton1.AccessibleContext.accessibleDescription")); // NOI18N

        btnConnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/select.png"))); // NOI18N
        btnConnect.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnConnect, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnConnect.text")); // NOI18N
        btnConnect.setToolTipText(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnConnect.toolTipText")); // NOI18N
        btnConnect.setFocusable(false);
        btnConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        barToolMain.add(btnConnect);

        btnShowNodeLabels.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/hide_node_labels.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnShowNodeLabels, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnShowNodeLabels.text")); // NOI18N
        btnShowNodeLabels.setToolTipText(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnShowNodeLabels.toolTipText")); // NOI18N
        btnShowNodeLabels.setFocusable(false);
        btnShowNodeLabels.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowNodeLabels.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowNodeLabels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowNodeLabelsActionPerformed(evt);
            }
        });
        barToolMain.add(btnShowNodeLabels);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/connect.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnSelect.toolTipText")); // NOI18N
        btnSelect.setFocusable(false);
        btnSelect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSelect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        barToolMain.add(btnSelect);
        btnSelect.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.jToggleButton2.AccessibleContext.accessibleDescription")); // NOI18N

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
        barToolMain.add(btnCloud);
        btnCloud.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.jToggleButton2.AccessibleContext.accessibleName")); // NOI18N

        btnFrame.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/frame.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnFrame, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnFrame.text")); // NOI18N
        btnFrame.setFocusable(false);
        btnFrame.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFrame.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnFrame.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnFrameActionPerformed(evt);
            }
        });
        barToolMain.add(btnFrame);
        btnFrame.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.jToggleButton5.AccessibleContext.accessibleName")); // NOI18N

        btnLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/topology/res/label.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnLabel, org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.btnLabel.text")); // NOI18N
        btnLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLabelActionPerformed(evt);
            }
        });
        barToolMain.add(btnLabel);
        btnLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(TopologyViewTopComponent.class, "TopologyViewTopComponent.jToggleButton6.AccessibleContext.accessibleName")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(barToolMain, javax.swing.GroupLayout.PREFERRED_SIZE, 400, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(211, Short.MAX_VALUE))
            .addComponent(pnlMainScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 611, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(barToolMain, javax.swing.GroupLayout.PREFERRED_SIZE, 31, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlMainScrollPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 252, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        scene.setActiveTool(ObjectNodeWidget.ACTION_SELECT);
        btnSelect.setSelected(false);
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        scene.setActiveTool(ObjectNodeWidget.ACTION_CONNECT);
        btnConnect.setSelected(false);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLabelActionPerformed
        scene.addFreeLabel();
        btnLabel.setSelected(false);
    }//GEN-LAST:event_btnLabelActionPerformed

    private void btnFrameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnFrameActionPerformed
        scene.addFreeFrame();
        btnFrame.setSelected(false);
    }//GEN-LAST:event_btnFrameActionPerformed

    private void btnCloudActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloudActionPerformed
        scene.addFreeCloud();
        btnCloud.setSelected(false);
    }//GEN-LAST:event_btnCloudActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the current view?",
                "Delete saved view",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
            tvsrv.deleteView();
            scene.clear();
            btnDelete.setEnabled(false);
        }
}//GEN-LAST:event_btnDeleteActionPerformed

    private void btnNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewActionPerformed
        if (scene.getNodes().isEmpty()){
            toggleButtons(true);
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the current view?",
                    "Confirmation",JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
            if(tvsrv.getTvId() == -1){ //It's a temporal view, not a saved one
                    scene.clear();
            }else{ //It's a saved view, so we need to clear all
                scene.clear();
                toggleButtons(true);
                isSaved = false;
            }
            scene.validate();
        }
    }//GEN-LAST:event_btnNewActionPerformed

    private void btnOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOpenActionPerformed
        if (!checkForUnsavedView(true))
            return;
         LocalObjectViewLight[] topologyViews = tvsrv.getTopologyViews();

         final TopologyListPanel tlp = new TopologyListPanel(topologyViews);
         DialogDescriptor dd = new DialogDescriptor(tlp, "Choose a view", true, new ActionListener(){
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                if (e.getSource() == DialogDescriptor.OK_OPTION){
                                    if (checkForUnsavedView(true)){
                                        if (tlp.getSelectedView() != null){
                                            tvsrv.loadTopologyView(tlp.getSelectedView());
                                            btnDelete.setEnabled(true);
                                        }
                                        else
                                            JOptionPane.showConfirmDialog(null, "Select a view, please","Error", JOptionPane.ERROR_MESSAGE);
                                    }
                                }
                                tlp.releaseListeners();
                            }
                        });
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        btnOpen.setSelected(false);
    }//GEN-LAST:event_btnOpenActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        new ExportSceneAction(scene).actionPerformed(evt);
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnShowNodeLabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowNodeLabelsActionPerformed
        for (Widget node : scene.getNodesLayer().getChildren())
            ((ObjectNodeWidget)node).getLabelWidget().setVisible(!btnShowNodeLabels.isSelected());
        scene.validate();
    }//GEN-LAST:event_btnShowNodeLabelsActionPerformed

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
                                        if (tvsrv.getTvId() != -1)
                                            btnDelete.setEnabled(true);
                                    }
                                }
                            });
            DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
        }else{
            tvsrv.saveView();
            isSaved = true;
        }
         btnSave.setSelected(false);
    }//GEN-LAST:event_btnSaveActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barToolMain;
    private javax.swing.JToggleButton btnCloud;
    private javax.swing.JToggleButton btnConnect;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnExport;
    private javax.swing.JToggleButton btnFrame;
    private javax.swing.JToggleButton btnLabel;
    private javax.swing.JButton btnNew;
    private javax.swing.JButton btnOpen;
    private javax.swing.JButton btnSave;
    private javax.swing.JToggleButton btnSelect;
    private javax.swing.JToggleButton btnShowNodeLabels;
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
            JOptionPane.showMessageDialog(this, "Nothing to do here",
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
        if(!scene.getNodes().isEmpty())
            return checkForUnsavedView(true);
        return true;
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        toggleButtons(false);
    }

    @Override
    public void componentClosed() {
        scene.clear();
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
        if (!isSaved){
            switch (JOptionPane.showConfirmDialog(null, "This topology view has not been saved, do you want to save it?",
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

    public NotificationUtil getNotifier(){
        if (nu == null)
            return Lookup.getDefault().lookup(NotificationUtil.class);
        return null;
    }

    public void toggleButtons(boolean enabled) {
        btnCloud.setEnabled(enabled);
        btnConnect.setEnabled(enabled);
        btnSelect.setEnabled(enabled);
        btnExport.setEnabled(enabled);
        btnFrame.setEnabled(enabled);
        btnLabel.setEnabled(enabled);
        btnShowNodeLabels.setEnabled(enabled);
        btnSave.setEnabled(enabled);
    }

}
