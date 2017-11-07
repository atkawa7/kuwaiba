/**
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
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
 *
 */
package org.inventory.design.modelsLayouts;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.inventory.design.modelsLayouts.lookup.SharedContent;
import java.util.Collections;
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.design.modelsLayouts.scene.ModelLayoutScene;
import org.inventory.predefinedshapes.PredefinedShapesTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays a model type layout view.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.design.drawobject//DrawObject//EN",
        autostore = false
)
public final class ModelLayoutTopComponent extends TopComponent implements ActionListener, Refreshable {
    public static String ID = "LayoutTopComponent_";
    private ModelLayoutService service;
    
    private ModelLayoutConfigurationObject configObject;
    
    public ModelLayoutTopComponent(LocalObjectListItem listItem) {
        this();
        configObject = new ModelLayoutConfigurationObject();
        configObject.setProperty("saved", true);
        
        service = new ModelLayoutService(listItem);
                
        setDisplayName(listItem.toString() + " Layout");
        
        associateLookup(SharedContent.getInstance().getAbstractLookup());
        
        SharedContent.getInstance().getInstanceContent()
            .set(Collections.singleton(service.getPalette()), null);
        
        pnlScrollPane.setViewportView(service.getScene().createView());
    }
    
    public ModelLayoutTopComponent() {
        initComponents();
    }
    
    @Override
    protected String preferredID() {
        return ID + service.getListItem().getId(); //NOI18N
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlScrollPane = new javax.swing.JScrollPane();
        barMain = new javax.swing.JToolBar();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnManagePredefineShapes = new javax.swing.JButton();
        btnShowPalette = new javax.swing.JButton();
        btnShapeHierarchy = new javax.swing.JButton();
        btnClean = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());
        add(pnlScrollPane, java.awt.BorderLayout.CENTER);

        barMain.setRollover(true);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/design/modelsLayouts/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        barMain.add(btnSave);

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/design/modelsLayouts/res/delete.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnDelete, org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnDelete.text")); // NOI18N
        btnDelete.setToolTipText(org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnDelete.toolTipText")); // NOI18N
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        barMain.add(btnDelete);

        btnManagePredefineShapes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/design/modelsLayouts/res/manage_predefined_shapes.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnManagePredefineShapes, org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnManagePredefineShapes.text")); // NOI18N
        btnManagePredefineShapes.setToolTipText(org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnManagePredefineShapes.toolTipText")); // NOI18N
        btnManagePredefineShapes.setFocusable(false);
        btnManagePredefineShapes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnManagePredefineShapes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnManagePredefineShapes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnManagePredefineShapesActionPerformed(evt);
            }
        });
        barMain.add(btnManagePredefineShapes);

        btnShowPalette.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/design/modelsLayouts/res/show_palette.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnShowPalette, org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnShowPalette.text")); // NOI18N
        btnShowPalette.setToolTipText(org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnShowPalette.toolTipText")); // NOI18N
        btnShowPalette.setFocusable(false);
        btnShowPalette.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowPalette.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowPalette.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowPaletteActionPerformed(evt);
            }
        });
        barMain.add(btnShowPalette);

        btnShapeHierarchy.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/design/modelsLayouts/res/shape_hierarchy.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnShapeHierarchy, org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnShapeHierarchy.text")); // NOI18N
        btnShapeHierarchy.setToolTipText(org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnShapeHierarchy.toolTipText")); // NOI18N
        btnShapeHierarchy.setFocusable(false);
        btnShapeHierarchy.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShapeHierarchy.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShapeHierarchy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShapeHierarchyActionPerformed(evt);
            }
        });
        barMain.add(btnShapeHierarchy);

        btnClean.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/design/modelsLayouts/res/clean.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnClean, org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnClean.text")); // NOI18N
        btnClean.setToolTipText(org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnClean.toolTipText")); // NOI18N
        btnClean.setFocusable(false);
        btnClean.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClean.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnClean.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCleanActionPerformed(evt);
            }
        });
        barMain.add(btnClean);

        add(barMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents
    
    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the current device layout?", 
                "Delete", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            if (service.deleteView()) {
                service.getScene().clear();
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "The current view was deleted");
            } else
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "The current view can not be deleted");            
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (service.saveView()) {
            btnDelete.setEnabled(true);
            setSaved(true);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnCleanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCleanActionPerformed
        if (JOptionPane.showConfirmDialog(null, "Are you sure you want to clear the canvas?", 
                "Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            service.getScene().clear();
            setSaved(false);
        }
    }//GEN-LAST:event_btnCleanActionPerformed
    
    private void btnShowPaletteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowPaletteActionPerformed
        for (TopComponent topComponent : WindowManager.getDefault().findMode("commonpalette").getTopComponents()) {
            if (!topComponent.isOpened()) // open the palette is was closed
                topComponent.open();
        }
    }//GEN-LAST:event_btnShowPaletteActionPerformed

    private void btnManagePredefineShapesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnManagePredefineShapesActionPerformed
            PredefinedShapesTopComponent topComponent = (PredefinedShapesTopComponent) WindowManager.getDefault().findTopComponent("PredefinedShapesTopComponent"); //NOI18N
            if (topComponent == null) {
                topComponent = new PredefinedShapesTopComponent();
                topComponent.open();
            } else {
                if (topComponent.isOpened())
                    topComponent.requestAttention(true);
                else { //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                       //so we will reuse the instance, refreshing the vierw first
                    //topComponent.refresh();
                    topComponent.open();
                }
            }
            topComponent.requestActive();
    }//GEN-LAST:event_btnManagePredefineShapesActionPerformed

    private void btnShapeHierarchyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShapeHierarchyActionPerformed
        
            ShapeHierarchyTopComponent topComponent = (ShapeHierarchyTopComponent) WindowManager.getDefault().findTopComponent("ShapeHierarchyTopComponent"); //NOI18N
            if (topComponent == null) {
                topComponent = new ShapeHierarchyTopComponent(service.getScene());
                topComponent.open();
            } else {
                if (topComponent.isOpened())
                    topComponent.requestAttention(true);
                else { //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                       //so we will reuse the instance, refreshing the vierw first
                    //topComponent.refresh();
                    topComponent.open();
                }
            }
            topComponent.requestActive();
    }//GEN-LAST:event_btnShapeHierarchyActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barMain;
    private javax.swing.JButton btnClean;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnManagePredefineShapes;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnShapeHierarchy;
    private javax.swing.JButton btnShowPalette;
    private javax.swing.JScrollPane pnlScrollPane;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {    
        btnShowPaletteActionPerformed(null);

        service.renderView();      
        if (service.getCurrentView() == null)
            btnDelete.setEnabled(false);
        service.getScene().addChangeListener(this);
    }

    @Override
    public void componentClosed() {
        service.getScene().removeAllListeners();
        service.getScene().clear();
        
        ShapeHierarchyTopComponent topComponent = (ShapeHierarchyTopComponent) WindowManager.getDefault().findTopComponent("ShapeHierarchyTopComponent"); //NOI18N
        if (topComponent != null) {
            if (topComponent.isOpened())
                topComponent.close();
        }
    }
    
    @Override
    public boolean canClose(){
        return checkForUnsavedView();
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
    public void refresh() {
    }
    
    @Override
    public String getDisplayName(){
        if (super.getDisplayName() == null)
            return "<No View>";
        return super.getDisplayName().trim().isEmpty() ? "<No view>" : super.getDisplayName();
    }
    
    public void setSaved(boolean value) {
        configObject.setProperty("saved", value);
        
        if (value)
            this.setHtmlDisplayName(this.getDisplayName());
        else
            this.setHtmlDisplayName(String.format("<html><b>%s [Modified]</b></html>", getDisplayName()));
    }
    
    public boolean checkForUnsavedView() {
        if (!(boolean) configObject.getProperty("saved")) {
            int option = JOptionPane.showConfirmDialog(null, "This view has not been saved, do you want to save it?", 
                "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                
                btnSaveActionPerformed(null);
                configObject.setProperty("saved", true);
                return true;
            }
            if (option == JOptionPane.NO_OPTION)
                return true;                
        } else
            return true;
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getID()){
            case ModelLayoutScene.SCENE_CHANGE:
                setSaved(false);
                break;
            case ModelLayoutScene.SCENE_CHANGEANDSAVE:
                break;
        }
    }
    
}
