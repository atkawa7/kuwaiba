/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.views.objectview;

import org.inventory.core.services.event.CurrentKeyEventDispatcher;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.configuration.ObjectViewConfigurationObject;
import org.inventory.views.objectview.scene.ChildrenViewScene;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.TopComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.windows.WindowManager;

/**
 * This component renders the views associated to an currentObject
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */

public final class ObjectViewTopComponent extends TopComponent
        implements ExplorerManager.Provider, ActionListener, Refreshable {
    
    private ButtonGroup buttonGroupTools;
    
    private final ExplorerManager em = new ExplorerManager();
    private ObjectViewService service;
    
    private ChildrenViewScene scene;
    private ObjectViewConfigurationObject configObject;
    private final LocalObjectLight currentObject;
    
    KeyEventDispatcher keyEventDispatcher;
    
    /**
     * Default constructor
     * @param aBusinessObject The business object whose view will be rendered 
     */
    public ObjectViewTopComponent(LocalObjectLight aBusinessObject) {
        this.currentObject = aBusinessObject;
        initComponents();
        initCustomComponents();
    }

    @Override
    protected String preferredID() {
        return "ObjectViewTopComponent_" + currentObject.getId().split("-")[0] ; //NOI18N
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    public final void initCustomComponents(){
        
        
        configObject = new ObjectViewConfigurationObject();
        configObject.setProperty("saved", true);
        configObject.setProperty("currentObject", currentObject);
        configObject.setProperty("currentView", null);
        configObject.setProperty("connectContainer", true);

        scene = new ChildrenViewScene(configObject);
        service = new ObjectViewService(scene);
        associateLookup(scene.getLookup());
        
        pnlScrollMain.setViewportView(scene.createView());

        buttonGroupTools = new ButtonGroup();
        buttonGroupTools.add(btnSelect);
        buttonGroupTools.add(btnContainer);
        buttonGroupTools.add(btnLink);
        
        keyEventDispatcher = new KeyEventDispatcher() {

            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F5 && e.getModifiers() == 0) {
                    btnRefreshActionPerformed(new ActionEvent(e.getSource(), e.getID(), ""));
                    return true;
                }
                return false;
            }
        };
        CurrentKeyEventDispatcher.getInstance().addKeyEventDispatcher(this, keyEventDispatcher);

        addComponentListener(new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
                CurrentKeyEventDispatcher.getInstance().updateKeyEventDispatcher(ObjectViewTopComponent.this);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        barMain = new javax.swing.JToolBar();
        btnSave = new javax.swing.JButton();
        btnAddBackgroundImage = new javax.swing.JButton();
        btnRemoveBackground = new javax.swing.JButton();
        btnShowConnectionLabels = new javax.swing.JToggleButton();
        btnHighContrast = new javax.swing.JToggleButton();
        btnSelect = new javax.swing.JToggleButton();
        btnContainer = new javax.swing.JToggleButton();
        btnLink = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnExport = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        pnlScrollMain = new javax.swing.JScrollPane();
        pnlRight = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        barMain.setRollover(true);
        barMain.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.barMain.toolTipText")); // NOI18N

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/save.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnSave, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnSave.text")); // NOI18N
        btnSave.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnSave.toolTipText")); // NOI18N
        btnSave.setEnabled(false);
        btnSave.setFocusable(false);
        btnSave.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSave.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        barMain.add(btnSave);

        btnAddBackgroundImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/add-background.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnAddBackgroundImage, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnAddBackgroundImage.text")); // NOI18N
        btnAddBackgroundImage.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnAddBackgroundImage.toolTipText")); // NOI18N
        btnAddBackgroundImage.setEnabled(false);
        btnAddBackgroundImage.setFocusable(false);
        btnAddBackgroundImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAddBackgroundImage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAddBackgroundImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddBackgroundImageActionPerformed(evt);
            }
        });
        barMain.add(btnAddBackgroundImage);

        btnRemoveBackground.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/remove-background.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRemoveBackground, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnRemoveBackground.text")); // NOI18N
        btnRemoveBackground.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnRemoveBackground.toolTipText")); // NOI18N
        btnRemoveBackground.setEnabled(false);
        btnRemoveBackground.setFocusable(false);
        btnRemoveBackground.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnRemoveBackground.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnRemoveBackground.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveBackgroundActionPerformed(evt);
            }
        });
        barMain.add(btnRemoveBackground);

        btnShowConnectionLabels.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/hide_conn_labels.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnShowConnectionLabels, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnShowConnectionLabels.text")); // NOI18N
        btnShowConnectionLabels.setEnabled(false);
        btnShowConnectionLabels.setFocusable(false);
        btnShowConnectionLabels.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnShowConnectionLabels.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnShowConnectionLabels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnShowConnectionLabelsActionPerformed(evt);
            }
        });
        barMain.add(btnShowConnectionLabels);

        btnHighContrast.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/high_contrast.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnHighContrast, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnHighContrast.text")); // NOI18N
        btnHighContrast.setEnabled(false);
        btnHighContrast.setFocusCycleRoot(true);
        btnHighContrast.setFocusable(false);
        btnHighContrast.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnHighContrast.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnHighContrast.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHighContrastActionPerformed(evt);
            }
        });
        barMain.add(btnHighContrast);

        btnSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/select.png"))); // NOI18N
        btnSelect.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnSelect, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnSelect.text")); // NOI18N
        btnSelect.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnSelect.toolTipText")); // NOI18N
        btnSelect.setEnabled(false);
        btnSelect.setFocusable(false);
        btnSelect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSelect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSelect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSelectActionPerformed(evt);
            }
        });
        barMain.add(btnSelect);

        btnContainer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/container_connections.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnContainer, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnContainer.text")); // NOI18N
        btnContainer.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnContainer.toolTipText")); // NOI18N
        btnContainer.setEnabled(false);
        btnContainer.setFocusable(false);
        btnContainer.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnContainer.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnContainer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnContainerActionPerformed(evt);
            }
        });
        barMain.add(btnContainer);

        btnLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/link_connections.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnLink, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnLink.text")); // NOI18N
        btnLink.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnLink.toolTipText")); // NOI18N
        btnLink.setEnabled(false);
        btnLink.setFocusable(false);
        btnLink.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLink.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLinkActionPerformed(evt);
            }
        });
        barMain.add(btnLink);
        barMain.add(jSeparator2);

        btnExport.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/export.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnExport, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnExport.text")); // NOI18N
        btnExport.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnExport.toolTipText")); // NOI18N
        btnExport.setEnabled(false);
        btnExport.setFocusable(false);
        btnExport.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExport.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });
        barMain.add(btnExport);

        btnRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/refresh.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnRefresh, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnRefresh.text")); // NOI18N
        btnRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnRefresh.toolTipText")); // NOI18N
        btnRefresh.setEnabled(false);
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
        add(pnlScrollMain, java.awt.BorderLayout.CENTER);

        pnlRight.setLayout(new java.awt.BorderLayout());
        add(pnlRight, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        scene.setActiveTool(ChildrenViewScene.ACTION_SELECT);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnAddBackgroundImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBackgroundImageActionPerformed
        JFileChooser fChooser = Utils.getGlobalFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setFileFilter(new FileNameExtensionFilter("Image files", "gif","jpg", "png"));
        if (fChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                Image myBackgroundImage = ImageIO.read(new File(fChooser.getSelectedFile().getAbsolutePath()));
                scene.setBackgroundImage(myBackgroundImage);
                scene.fireChangeEvent(new ActionEvent(this, ChildrenViewScene.SCENE_CHANGE, "Add Background"));
            } catch (IOException ex) {
                getNotifier().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
            }
        }
    }//GEN-LAST:event_btnAddBackgroundImageActionPerformed

    private void btnRemoveBackgroundActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveBackgroundActionPerformed
          scene.setBackgroundImage(null);
          scene.fireChangeEvent(new ActionEvent(this, AbstractScene.SCENE_CHANGE, "Remove Background"));
    }//GEN-LAST:event_btnRemoveBackgroundActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        service.saveView();
        setHtmlDisplayName(getDisplayName());
        configObject.setProperty("saved", true);
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refresh();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        ExportScenePanel exportPanel = new ExportScenePanel(
            new SceneExportFilter[]{ImageFilter.getInstance()}, scene, currentObject.toString());
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export Options",true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnShowConnectionLabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowConnectionLabelsActionPerformed
        scene.toggleConnectionLabels(!btnShowConnectionLabels.isSelected());
    }//GEN-LAST:event_btnShowConnectionLabelsActionPerformed

    private void btnHighContrastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHighContrastActionPerformed
        scene.enableHighContrastMode(btnHighContrast.isSelected());
    }//GEN-LAST:event_btnHighContrastActionPerformed

    private void btnContainerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContainerActionPerformed
        scene.setActiveTool(ChildrenViewScene.ACTION_CONNECT);
        configObject.setProperty("connectContainer", true);
    }//GEN-LAST:event_btnContainerActionPerformed

    private void btnLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLinkActionPerformed
        scene.setActiveTool(ChildrenViewScene.ACTION_CONNECT);
        configObject.setProperty("connectContainer", false);
    }//GEN-LAST:event_btnLinkActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barMain;
    private javax.swing.JButton btnAddBackgroundImage;
    private javax.swing.JToggleButton btnContainer;
    private javax.swing.JButton btnExport;
    private javax.swing.JToggleButton btnHighContrast;
    private javax.swing.JToggleButton btnLink;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRemoveBackground;
    private javax.swing.JButton btnSave;
    private javax.swing.JToggleButton btnSelect;
    private javax.swing.JToggleButton btnShowConnectionLabels;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JPanel pnlRight;
    private javax.swing.JScrollPane pnlScrollMain;
    // End of variables declaration//GEN-END:variables
    
    @Override
    public void componentOpened() {
        if (currentObject.getClassName().equals(Constants.DUMMYROOT) || 
                !CommunicationsStub.getInstance().getMetaForClass(currentObject.getClassName(), false).isViewable()) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "This object does not have a view");
            disableView();
            return;
        }
        scene.addChangeListener(this);
        service.renderView();
        setDisplayName(currentObject.toString());
        toggleButtons(true);
        configObject.setProperty("saved", true);
        setHtmlDisplayName(getDisplayName());
    }

    @Override
    public void componentClosed() {
        scene.removeAllListeners();
        scene.clear();
    }
    
    @Override
    public boolean canClose(){
        return checkForUnsavedView(true);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    public NotificationUtil getNotifier(){
        return NotificationUtil.getInstance();
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
            this.setHtmlDisplayName(String.format(I18N.gm("modified"), getDisplayName()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getID()){
            case ChildrenViewScene.SCENE_CHANGE:
                this.setSaved(false);
                break;
            case ChildrenViewScene.SCENE_CHANGEANDSAVE:
                btnSaveActionPerformed(e);
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "An external change was detected. The view has been saved automatically");
        }
    }

    public boolean checkForUnsavedView(boolean showCancel) {
        if (!((boolean) configObject.getProperty("saved"))){
            switch (JOptionPane.showConfirmDialog(null, "This view has not been saved, do you want to save it?", 
                    I18N.gm("confirmation"), showCancel ? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION)){
                case JOptionPane.YES_OPTION:
                    
                    btnSaveActionPerformed(null);
                    configObject.setProperty("saved", true);
                    return true;
                case JOptionPane.CANCEL_OPTION:
                    return false;
            }
        }
        return true;
    }

    @Override
    public void refresh() {
        if (checkForUnsavedView(true)) {
            scene.clear();
            service.renderView();
        }
    }

    public void toggleButtons(boolean enabled) {
        btnAddBackgroundImage.setEnabled(enabled);
        btnRemoveBackground.setEnabled(enabled);
        btnSave.setEnabled(enabled);
        btnSelect.setEnabled(enabled);
        btnExport.setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
        btnShowConnectionLabels.setEnabled(enabled);
        btnHighContrast.setEnabled(enabled);
        btnContainer.setEnabled(enabled);
        btnLink.setEnabled(enabled);
    }
    
    public void disableView() {
        scene.clear();
        setEnabled(false);
    }
}
