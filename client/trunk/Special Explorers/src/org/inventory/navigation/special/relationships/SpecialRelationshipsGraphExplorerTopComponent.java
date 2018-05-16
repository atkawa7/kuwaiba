/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.special.relationships;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.inventory.navigation.special.relationships.scene.actions.HideSpecialRelationshipChildrenAction;
import org.inventory.navigation.special.relationships.scene.actions.ShowSpecialRelationshipChildrenAction;
import org.inventory.navigation.special.relationships.nodes.LocalObjectLightWrapper;
import org.inventory.navigation.special.relationships.scene.SpecialRelationshipsGraphExplorerScene;
import org.openide.*;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Top component which displays a graphical representation of special relationships.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@Messages({
    "CTL_GraphicalRepSpecialRelationshipsTopComponent=Graphical Representation",
    "HINT_GraphicalRepSpecialRelationshipsTopComponent=Show a Graphical Representation of Special Relationships"
})
public final class SpecialRelationshipsGraphExplorerTopComponent extends TopComponent implements 
    ExplorerManager.Provider, Refreshable, ActionListener {
    
    private ExplorerManager em;
    private GraphicalRepSpecialRelationshipService service;
    private SpecialRelationshipsGraphExplorerScene scene;

    public SpecialRelationshipsGraphExplorerTopComponent(LocalObjectLight rootObject) {
        initComponents();
        initComponentsCustom(rootObject);
        setName("Special Relationships Graphical Explorer");
        setToolTipText("Explore the relationships of an object in a simple way");
    }
    
    @Override
    protected String preferredID() {
        return "SpecialRelationshipsGraphExplorerTopComponent_" + service.getRoot().
            getLocalObjectLightWrapped().getOid(); //NOI18N
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    private void initComponentsCustom(LocalObjectLight rootObject) {
        em = new ExplorerManager();
        
        scene = new SpecialRelationshipsGraphExplorerScene();
        scene.addChangeListener(this);
        service = new GraphicalRepSpecialRelationshipService(scene, new LocalObjectLightWrapper(rootObject));
        
        associateLookup(scene.getLookup());
        pnlMainScrollPanel.setViewportView(scene.createView());
        add(scene.createSatelliteView(), BorderLayout.SOUTH);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBarMain = new javax.swing.JToolBar();
        btnCollapse = new javax.swing.JButton();
        btnExportAsImg = new javax.swing.JButton();
        btnOrganize = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        pnlMainScrollPanel = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());

        toolBarMain.setRollover(true);
        toolBarMain.setMaximumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setMinimumSize(new java.awt.Dimension(392, 38));
        toolBarMain.setPreferredSize(new java.awt.Dimension(326, 33));

        btnCollapse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/navigation/special/res/collapse.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnCollapse, org.openide.util.NbBundle.getMessage(SpecialRelationshipsGraphExplorerTopComponent.class, "SpecialRelationshipsGraphExplorerTopComponent.btnCollapse.text")); // NOI18N
        btnCollapse.setToolTipText(org.openide.util.NbBundle.getMessage(SpecialRelationshipsGraphExplorerTopComponent.class, "SpecialRelationshipsGraphExplorerTopComponent.btnCollapse.toolTipText")); // NOI18N
        btnCollapse.setFocusable(false);
        btnCollapse.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCollapse.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCollapse.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCollapseMouseClicked(evt);
            }
        });
        toolBarMain.add(btnCollapse);

        btnExportAsImg.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/navigation/special/res/exportAsImg.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExportAsImg, org.openide.util.NbBundle.getMessage(SpecialRelationshipsGraphExplorerTopComponent.class, "SpecialRelationshipsGraphExplorerTopComponent.btnExportAsImg.text")); // NOI18N
        btnExportAsImg.setToolTipText(org.openide.util.NbBundle.getMessage(SpecialRelationshipsGraphExplorerTopComponent.class, "SpecialRelationshipsGraphExplorerTopComponent.btnExportAsImg.toolTipText")); // NOI18N
        btnExportAsImg.setFocusable(false);
        btnExportAsImg.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExportAsImg.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExportAsImg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportAsImgActionPerformed(evt);
            }
        });
        toolBarMain.add(btnExportAsImg);

        btnOrganize.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/navigation/special/res/organize.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnOrganize, org.openide.util.NbBundle.getMessage(SpecialRelationshipsGraphExplorerTopComponent.class, "SpecialRelationshipsGraphExplorerTopComponent.btnOrganize.text")); // NOI18N
        btnOrganize.setToolTipText(org.openide.util.NbBundle.getMessage(SpecialRelationshipsGraphExplorerTopComponent.class, "SpecialRelationshipsGraphExplorerTopComponent.btnOrganize.toolTipText")); // NOI18N
        btnOrganize.setFocusable(false);
        btnOrganize.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOrganize.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnOrganize.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnOrganizeMouseClicked(evt);
            }
        });
        toolBarMain.add(btnOrganize);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/navigation/special/res/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRefresh, org.openide.util.NbBundle.getMessage(SpecialRelationshipsGraphExplorerTopComponent.class, "SpecialRelationshipsGraphExplorerTopComponent.btnRefresh.text")); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(SpecialRelationshipsGraphExplorerTopComponent.class, "SpecialRelationshipsGraphExplorerTopComponent.btnRefresh.toolTipText")); // NOI18N
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

    private void btnOrganizeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnOrganizeMouseClicked
        scene.reorganizeNodes();
    }//GEN-LAST:event_btnOrganizeMouseClicked

    private void btnExportAsImgActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportAsImgActionPerformed
        ExportScenePanel exportPanel = new ExportScenePanel(new SceneExportFilter[]{ImageFilter.getInstance()}, 
                scene, getDisplayName());
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export Options", true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportAsImgActionPerformed

    private void btnCollapseMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCollapseMouseClicked
        // TODO add your handling code here:
        service.hideSpecialRelationshipChildren(service.getRoot());
    }//GEN-LAST:event_btnCollapseMouseClicked

    private void btnRefreshMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnRefreshMouseClicked
        refresh();
    }//GEN-LAST:event_btnRefreshMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCollapse;
    private javax.swing.JButton btnExportAsImg;
    private javax.swing.JButton btnOrganize;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JScrollPane pnlMainScrollPanel;
    private javax.swing.JToolBar toolBarMain;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        scene.render(service.getRoot());
        service.showSpecialRelationshipChildren(service.getRoot());
    }

    @Override
    public void componentClosed() {
        scene.removeAllListeners();
        scene.clear();
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
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public void refresh() {
        service.refreshScene();
    }
    
    @Override
    public String getDisplayName() {
        
        return String.format("Relationships for %s", service.getRoot().toString());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
        if (e.getActionCommand().equals(
            HideSpecialRelationshipChildrenAction.COMMAND)) {
            
            service.hideSpecialRelationshipChildren(
                (LocalObjectLightWrapper) e.getSource());
            return;
        }
        
        if (e.getActionCommand().equals(
            ShowSpecialRelationshipChildrenAction.COMMAND)) {
            
            service.showSpecialRelationshipChildren(
                (LocalObjectLightWrapper) e.getSource());
        }
    }
}
