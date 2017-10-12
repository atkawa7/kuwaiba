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
import org.inventory.design.modelsLayouts.scene.ModelLayoutScene;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;

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
        btnClean = new javax.swing.JButton();

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
        });
        setLayout(new java.awt.BorderLayout());

        pnlScrollPane.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                pnlScrollPaneMouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                pnlScrollPaneMouseEntered(evt);
            }
        });
        add(pnlScrollPane, java.awt.BorderLayout.CENTER);

        barMain.setRollover(true);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/design/modelsLayouts/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnSaveMouseClicked(evt);
            }
        });
        barMain.add(btnSave);

        btnClean.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/design/modelsLayouts/res/clean.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnClean, org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnClean.text")); // NOI18N
        btnClean.setToolTipText(org.openide.util.NbBundle.getMessage(ModelLayoutTopComponent.class, "ModelLayoutTopComponent.btnClean.toolTipText")); // NOI18N
        btnClean.setFocusable(false);
        btnClean.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnClean.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnClean.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                btnCleanMouseClicked(evt);
            }
        });
        barMain.add(btnClean);

        add(barMain, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    private void pnlScrollPaneMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlScrollPaneMouseEntered
        
    }//GEN-LAST:event_pnlScrollPaneMouseEntered

    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
        
    }//GEN-LAST:event_formMouseEntered

    private void pnlScrollPaneMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_pnlScrollPaneMouseExited
        
    }//GEN-LAST:event_pnlScrollPaneMouseExited

    private void btnSaveMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnSaveMouseClicked
        service.saveView();
        setSaved(true);
    }//GEN-LAST:event_btnSaveMouseClicked

    private void btnCleanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCleanMouseClicked
        service.getScene().clear();
        setSaved(false);
    }//GEN-LAST:event_btnCleanMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barMain;
    private javax.swing.JButton btnClean;
    private javax.swing.JButton btnSave;
    private javax.swing.JScrollPane pnlScrollPane;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {        
        service.renderView();      
        service.getScene().addChangeListener(this);
    }

    @Override
    public void componentClosed() {
        service.getScene().removeAllListeners();
        service.getScene().clear();
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
            if (JOptionPane.showConfirmDialog(null, "This view has not been saved, do you want to save it?", 
                "Confirmation", JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
                
                btnSaveMouseClicked(null);
                configObject.setProperty("saved", true);
                return true;
            }
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
