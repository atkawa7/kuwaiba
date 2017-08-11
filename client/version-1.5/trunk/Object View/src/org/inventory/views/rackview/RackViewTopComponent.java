/*
 * Copyright (c) 2017 johnyortega.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    johnyortega - initial API and implementation and/or initial documentation
 */
package org.inventory.views.rackview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.inventory.views.rackview.scene.RackViewScene;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component for Rack view
 * Top component which displays something.
 */
@Messages({
    "CTL_RackViewTopComponent=Rack View",
    "HINT_RackViewTopComponent=Rack View"
})
public final class RackViewTopComponent extends TopComponent implements ExplorerManager.Provider, ActionListener, Refreshable {
    private ExplorerManager em;
    private RackViewService service;
    private RackViewScene scene;
    private LocalObjectLight currentRack;
    
    public RackViewTopComponent(LocalObjectLight rack) {
        this.currentRack = rack;
        initComponents();
        initCustomComponents();
        setName(Bundle.CTL_RackViewTopComponent());
        setToolTipText(Bundle.HINT_RackViewTopComponent());
    }
    
    @Override
    protected String preferredID() {
        return "RackViewTopComponent_" + service.getRack().getOid(); //NOI18N
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    private void initCustomComponents() {
        em = new ExplorerManager();
        
        scene = new RackViewScene();
        scene.addChangeListener(this);
                
        associateLookup(scene.getLookup());
        pnlMainScrollPanel.setViewportView(scene.createView());
        
        service = new RackViewService(scene, currentRack);                
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBarMain = new javax.swing.JToolBar();
        btnExport = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        pnlMainScrollPanel = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        toolBarMain.setRollover(true);
        toolBarMain.setAlignmentY(0.5F);
        toolBarMain.setMaximumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setMinimumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setPreferredSize(new java.awt.Dimension(392, 38));

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/rackview/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnExportMouseClicked(evt);
            }
        });
        toolBarMain.add(btnExport);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/rackview/res/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRefresh, org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnRefresh.text")); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(RackViewTopComponent.class, "RackViewTopComponent.btnRefresh.toolTipText")); // NOI18N
        btnRefresh.setFocusable(false);
        btnRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRefresh.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnRefreshMouseClicked(evt);
            }
        });
        toolBarMain.add(btnRefresh);

        add(toolBarMain, java.awt.BorderLayout.PAGE_START);
        add(pnlMainScrollPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRefreshMouseClicked
        try {
            scene.clear();
            service.buildRackView();
        } catch (Exception ex) {
            scene.clear();
            NotificationUtil.getInstance().showSimplePopup("Error", 
                NotificationUtil.ERROR_MESSAGE, ex.getMessage());
        }
    }//GEN-LAST:event_btnRefreshMouseClicked

    private void btnExportMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnExportMouseClicked
        ExportScenePanel exportPanel = new ExportScenePanel(
            new SceneExportFilter[]{ImageFilter.getInstance()}, 
            scene, currentRack.toString());
        
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options",true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JScrollPane pnlMainScrollPanel;
    private javax.swing.JToolBar toolBarMain;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        try {
            service.buildRackView();
        } catch (Exception ex) {
            scene.clear();
            NotificationUtil.getInstance().showSimplePopup("Error", 
                NotificationUtil.ERROR_MESSAGE, ex.getMessage());
        }
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }
    
    @Override
    public String getDisplayName() {
        return String.format("Rack View for %s", service.getRack().getName());
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

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public void refresh() {
    }
}
