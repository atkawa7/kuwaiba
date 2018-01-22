/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.templates.layouts2;

import java.awt.BorderLayout;
import java.util.Collections;
import javax.swing.JOptionPane;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.EquipmentLayoutConfigurationObject;
import org.inventory.core.templates.layouts.customshapes.CustomShapesTopComponent;
import org.inventory.core.templates.layouts.lookup.SharedContent;
import org.inventory.core.templates.layouts.shapehierarchy.ShapeHierarchyTopComponent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
////@ConvertAsProperties(
////        dtd = "-//org.inventory.core.templates.layouts2//Equipment//EN",
////        autostore = false
////)
////@TopComponent.Description(
////        preferredID = "EquipmentLayoutTopComponent",    
////        //iconBase="SET/PATH/TO/ICON/HERE", 
////        persistenceType = TopComponent.PERSISTENCE_ALWAYS
////)
////@TopComponent.Registration(mode = "editor", openAtStartup = false)
////@ActionID(category = "Window", id = "org.inventory.core.templates.layouts2.EquipmentLayoutTopComponent")
////@ActionReference(path = "Menu/Window" /*, position = 333 */)
////@TopComponent.OpenActionRegistration(
////        displayName = "#CTL_EquipmentAction",
////        preferredID = "EquipmentLayoutTopComponent"
////)
////@Messages({
////    "CTL_EquipmentAction=Equipment",
////    "CTL_EquipmentTopComponent=Equipment Window",
////    "HINT_EquipmentTopComponent=This is a Equipment window"
////})
/**
 *  
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public final class EquipmentLayoutTopComponent extends TopComponent implements Refreshable {
    public static String ID = "EquipmentTopComponent_";
    private EquipmentLayoutService service;
    
    private EquipmentLayoutConfigurationObject configObject;

    public EquipmentLayoutTopComponent() {
        initComponents();
////        setName(Bundle.CTL_EquipmentTopComponent());
////        setToolTipText(Bundle.HINT_EquipmentTopComponent());

    }
    
    public EquipmentLayoutTopComponent(LocalObjectListItem model) {
        this();
        configObject = new EquipmentLayoutConfigurationObject();
        configObject.setProperty("saved", true); //NOI18N
        
        setDisplayName(model.toString() + " "  + I18N.gm("layout"));
        service = new EquipmentLayoutService(model);
        
        associateLookup(SharedContent.getInstance().getAbstractLookup());
        
        SharedContent.getInstance().getInstanceContent()
            .set(Collections.singleton(EquipmentLayoutPalette.getInstance().getPalette()), null);
        
        pnlScroll.setViewportView(service.getScene().createView());
        add(service.getScene().createSatelliteView(), BorderLayout.SOUTH);
    }
    
    @Override
    protected String preferredID() {
        return ID + service.getModel().getId();
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

        pnlScroll = new javax.swing.JScrollPane();
        barMain = new javax.swing.JToolBar();
        btnSave = new javax.swing.JButton();
        btnDelete = new javax.swing.JButton();
        btnCustomShapes = new javax.swing.JButton();
        btnShowPalette = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());
        add(pnlScroll, java.awt.BorderLayout.CENTER);

        barMain.setRollover(true);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/templates/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(EquipmentLayoutTopComponent.class, "EquipmentLayoutTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(EquipmentLayoutTopComponent.class, "EquipmentLayoutTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        barMain.add(btnSave);

        btnDelete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/templates/res/delete.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnDelete, org.openide.util.NbBundle.getMessage(EquipmentLayoutTopComponent.class, "EquipmentLayoutTopComponent.btnDelete.text")); // NOI18N
        btnDelete.setToolTipText(org.openide.util.NbBundle.getMessage(EquipmentLayoutTopComponent.class, "EquipmentLayoutTopComponent.btnDelete.toolTipText")); // NOI18N
        btnDelete.setFocusable(false);
        btnDelete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDelete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDelete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteActionPerformed(evt);
            }
        });
        barMain.add(btnDelete);

        btnCustomShapes.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/templates/res/custom_shapes.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnCustomShapes, org.openide.util.NbBundle.getMessage(EquipmentLayoutTopComponent.class, "EquipmentLayoutTopComponent.btnCustomShapes.text")); // NOI18N
        btnCustomShapes.setToolTipText(org.openide.util.NbBundle.getMessage(EquipmentLayoutTopComponent.class, "EquipmentLayoutTopComponent.btnCustomShapes.toolTipText")); // NOI18N
        btnCustomShapes.setFocusable(false);
        btnCustomShapes.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCustomShapes.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCustomShapes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCustomShapesActionPerformed(evt);
            }
        });
        barMain.add(btnCustomShapes);

        btnShowPalette.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/templates/res/show_palette.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnShowPalette, org.openide.util.NbBundle.getMessage(EquipmentLayoutTopComponent.class, "EquipmentLayoutTopComponent.btnShowPalette.text")); // NOI18N
        btnShowPalette.setToolTipText(org.openide.util.NbBundle.getMessage(EquipmentLayoutTopComponent.class, "EquipmentLayoutTopComponent.btnShowPalette.toolTipText")); // NOI18N
        btnShowPalette.setFocusable(false);
        btnShowPalette.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowPalette.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowPalette.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowPaletteActionPerformed(evt);
            }
        });
        barMain.add(btnShowPalette);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/core/templates/res/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRefresh, org.openide.util.NbBundle.getMessage(EquipmentLayoutTopComponent.class, "EquipmentLayoutTopComponent.btnRefresh.text")); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(EquipmentLayoutTopComponent.class, "EquipmentLayoutTopComponent.btnRefresh.toolTipText")); // NOI18N
        btnRefresh.setFocusable(false);
        btnRefresh.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRefresh.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        barMain.add(btnRefresh);

        add(barMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCustomShapesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCustomShapesActionPerformed
            CustomShapesTopComponent topComponent = (CustomShapesTopComponent) WindowManager.getDefault().findTopComponent("CustomShapesTopComponent"); //NOI18N
            if (topComponent == null) {
                topComponent = new CustomShapesTopComponent();
                topComponent.open();
            } else {
                if (topComponent.isOpened())
                    topComponent.requestAttention(true);
                else { //Even after closed, the TCs (even the no-singletons) continue to exist in the NBP's PersistenceManager registry, 
                       //so we will reuse the instance, refreshing the vierw first
////                    topComponent.refresh();
                    topComponent.open();
                }
            }
            topComponent.requestActive();
    }//GEN-LAST:event_btnCustomShapesActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        if (service.getScene().getNodes().isEmpty()) {
            JOptionPane.showConfirmDialog(null, "The canvas is empty, the device layout cannot be saved", 
                I18N.gm("confirmation"), JOptionPane.OK_OPTION);
            setSaved(true);
            return;                                
        }
        
        if (service.saveLayout()) {
            btnDelete.setEnabled(true);
            setSaved(true);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnShowPaletteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowPaletteActionPerformed
        for (Mode mode : WindowManager.getDefault().getModes()) {
            for (TopComponent topComponent : WindowManager.getDefault().findMode(mode.getName()).getTopComponents()) {
                if (!topComponent.isOpened()) { // open the palette is was closed
                    if (topComponent.getClass().getName().equals("org.netbeans.spi.palette.PaletteTopComponent")) // NOI18N
                        topComponent.open();
                }
            }
        }
    }//GEN-LAST:event_btnShowPaletteActionPerformed

    private void btnDeleteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteActionPerformed
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the current device layout?", 
                I18N.gm("delete"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            
            if (service.deleteLayout()) {
                service.getScene().clear();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "The current view was deleted");
            } else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "The current view can not be deleted");            
        }
    }//GEN-LAST:event_btnDeleteActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        btnSaveActionPerformed(null);
        service.getScene().clear();
        service.renderLayout();
    }//GEN-LAST:event_btnRefreshActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barMain;
    private javax.swing.JButton btnCustomShapes;
    private javax.swing.JButton btnDelete;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnShowPalette;
    private javax.swing.JScrollPane pnlScroll;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        btnShowPaletteActionPerformed(null);

        service.renderLayout();      
        if (service.getLayoutView()== null)
            btnDelete.setEnabled(false);
////        service.getScene().addChangeListener(this); 
    }

    @Override
    public void componentClosed() {
        service.getScene().removeAllListeners();
        service.getScene().clear();
        
        ShapeHierarchyTopComponent topComponent = (ShapeHierarchyTopComponent) WindowManager.getDefault().findTopComponent("ShapeHierarchyTopComponent_" + service.getModel().getId()); //NOI18N
        if (topComponent != null) {
            if (topComponent.isOpened())
                topComponent.close();
        }
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
    
    public void setSaved(boolean value) {
        configObject.setProperty("saved", value); //NOI18N
        
        if (value)
            this.setHtmlDisplayName(this.getDisplayName());
        else
            this.setHtmlDisplayName(String.format(I18N.gm("modified"), getDisplayName()));
    }

    @Override
    public void refresh() {
        EquipmentLayoutPalette.getInstance().createPalette();
                
        SharedContent.getInstance().getInstanceContent()
            .set(Collections.singleton(EquipmentLayoutPalette.getInstance().getPalette()), null);
    }
}
