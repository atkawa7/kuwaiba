/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.views.objectview.scene.ChildrenViewScene;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.TopComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

/**
 * This component renders the views associated to an currentObject
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ConvertAsProperties(dtd = "-//org.inventory.views.objectview//ObjectView//EN",
autostore = false)
@TopComponent.Description(
        preferredID = "ObjectViewTopComponent",
        iconBase="org/inventory/views/objectview/res/icon.png", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "editor", openAtStartup = false)
@ActionID(category = "Tools", id = "org.inventory.views.objectview.ObjectViewTopComponent")
@ActionReferences(value = { @ActionReference(path = "Menu/Tools/Views"),
    @ActionReference(path = "Toolbars/Views", position = 200, separatorBefore = 199)})
@TopComponent.OpenActionRegistration(
        displayName = "Object View",
        preferredID = "ObjectViewTopComponent"
)
public final class ObjectViewTopComponent extends TopComponent
        implements Provider, ActionListener, Refreshable, LookupListener {

    public static final int CONNECTION_WIRECONTAINER = 1;
    public static final int CONNECTION_WIRELESSCONTAINER = 2;
    public static final int CONNECTION_ELECTRICALLINK = 3;
    public static final int CONNECTION_OPTICALLINK = 4;
    public static final int CONNECTION_WIRELESSLINK = 5;
    public static final int CONNECTION_POWERLINK = 6;
    
    private ButtonGroup buttonGroupTools;
    private ButtonGroup buttonGroupConnections;
    
    private final ExplorerManager em = new ExplorerManager();
    private ObjectViewService service;
    
    private ChildrenViewScene scene;
    private ObjectViewConfigurationObject configObject;
    private LocalObjectLight currentObject;
    private Lookup.Result<LocalObjectLight> lookupResult;
    
    public ObjectViewTopComponent() {
        initComponents();
        initCustomComponents();
    }

    public final void initCustomComponents(){
        scene = new ChildrenViewScene();
        service = new ObjectViewService(scene);
        associateLookup(scene.getLookup());
        
        pnlScrollMain.setViewportView(scene.createView());

        buttonGroupTools = new ButtonGroup();
        buttonGroupTools.add(btnSelect);
        buttonGroupTools.add(btnConnect);

        buttonGroupConnections = new ButtonGroup();
        buttonGroupConnections.add(btnContainer);
        buttonGroupConnections.add(btnLink);
        
        btnSelect.setSelected(true);
        
        configObject = Lookup.getDefault().lookup(ObjectViewConfigurationObject.class);
        configObject.setProperty("saved", true);
        configObject.setProperty("currentObject", null);
        configObject.setProperty("currentView", null);
        configObject.setProperty("connectContainer", true);
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
        btnConnect = new javax.swing.JToggleButton();
        btnExport = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnContainer = new javax.swing.JToggleButton();
        btnLink = new javax.swing.JToggleButton();
        pnlScrollMain = new javax.swing.JScrollPane();
        pnlRight = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        barMain.setRollover(true);

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

        btnConnect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/connect.png"))); // NOI18N
        btnConnect.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnConnect, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnConnect.text")); // NOI18N
        btnConnect.setToolTipText(org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnConnect.toolTipText")); // NOI18N
        btnConnect.setEnabled(false);
        btnConnect.setFocusable(false);
        btnConnect.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConnect.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        barMain.add(btnConnect);

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
        barMain.add(jSeparator1);

        btnContainer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/containers.png"))); // NOI18N
        btnContainer.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(btnContainer, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnContainer.text")); // NOI18N
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

        btnLink.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/inventory/views/objectview/res/links.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnLink, org.openide.util.NbBundle.getMessage(ObjectViewTopComponent.class, "ObjectViewTopComponent.btnLink.text")); // NOI18N
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

        add(barMain, java.awt.BorderLayout.PAGE_START);
        add(pnlScrollMain, java.awt.BorderLayout.CENTER);

        pnlRight.setLayout(new java.awt.BorderLayout());
        add(pnlRight, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void btnSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSelectActionPerformed
        scene.setActiveTool(ChildrenViewScene.ACTION_SELECT);
    }//GEN-LAST:event_btnSelectActionPerformed

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        scene.setActiveTool(ChildrenViewScene.ACTION_CONNECT);
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnAddBackgroundImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddBackgroundImageActionPerformed
        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setFileFilter(new FileNameExtensionFilter("Image files", "gif","jpg", "png"));
        if (fChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                Image myBackgroundImage = ImageIO.read(new File(fChooser.getSelectedFile().getAbsolutePath()));
                scene.setBackgroundImage(myBackgroundImage);
                scene.fireChangeEvent(new ActionEvent(this, ChildrenViewScene.SCENE_CHANGE, "Add Background"));
            } catch (IOException ex) {
                getNotifier().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
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
            new SceneExportFilter[]{ImageFilter.getInstance()}, scene);
        DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export options",true, exportPanel);
        DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnShowConnectionLabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnShowConnectionLabelsActionPerformed
        scene.toggleConnectionLabels(!btnShowConnectionLabels.isSelected());
    }//GEN-LAST:event_btnShowConnectionLabelsActionPerformed

    private void btnHighContrastActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHighContrastActionPerformed
        scene.enableHighContrastMode(btnHighContrast.isSelected());
    }//GEN-LAST:event_btnHighContrastActionPerformed

    private void btnContainerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnContainerActionPerformed
        configObject.setProperty("connectContainer", true);
    }//GEN-LAST:event_btnContainerActionPerformed

    private void btnLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLinkActionPerformed
        configObject.setProperty("connectContainer", false);
    }//GEN-LAST:event_btnLinkActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToolBar barMain;
    private javax.swing.JButton btnAddBackgroundImage;
    private javax.swing.JToggleButton btnConnect;
    private javax.swing.JToggleButton btnContainer;
    private javax.swing.JButton btnExport;
    private javax.swing.JToggleButton btnHighContrast;
    private javax.swing.JToggleButton btnLink;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRemoveBackground;
    private javax.swing.JButton btnSave;
    private javax.swing.JToggleButton btnSelect;
    private javax.swing.JToggleButton btnShowConnectionLabels;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JPanel pnlRight;
    private javax.swing.JScrollPane pnlScrollMain;
    // End of variables declaration//GEN-END:variables
    

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
    public void componentOpened() {
        scene.addChangeListener(this);
        
        lookupResult = Utilities.actionsGlobalContext().lookupResult(LocalObjectLight.class);
        lookupResult.addLookupListener(this);
        
        if (lookupResult.allInstances().size() == 1)
            resultChanged(new LookupEvent(lookupResult));
    }

    @Override
    public void componentClosed() {
        lookupResult.removeLookupListener(this);
        disableView();
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
            this.setHtmlDisplayName(String.format("<html><b>%s [Modified]</b></html>", getDisplayName()));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getID()){
            case ChildrenViewScene.SCENE_CHANGE:
                this.setSaved(false);
                break;
            case ChildrenViewScene.SCENE_CHANGEANDSAVE:
                btnSaveActionPerformed(e);
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "An external change was detected. The view has been saved automatically");
        }
    }

    public boolean checkForUnsavedView(boolean showCancel) {
        if (!((boolean) configObject.getProperty("saved"))){
            switch (JOptionPane.showConfirmDialog(null, "This view has not been saved, do you want to save it?",
                    "Confirmation", showCancel ? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION)){
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
        btnConnect.setEnabled(enabled);
        btnExport.setEnabled(enabled);
        btnRefresh.setEnabled(enabled);
        btnShowConnectionLabels.setEnabled(enabled);
        btnHighContrast.setEnabled(enabled);
        btnContainer.setEnabled(enabled);
        btnLink.setEnabled(enabled);
    }
    
    public void disableView() {
        setDisplayName(null);
        setHtmlDisplayName(null);
        scene.clear();
        toggleButtons(false);
        currentObject = null;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        Lookup.Result aLookupResult = (Lookup.Result) ev.getSource();
        
        if (aLookupResult.allInstances().size() == 1) {
            LocalObjectLight obj = (LocalObjectLight) aLookupResult.allInstances().iterator().next();
            
            if (obj.equals(currentObject))  //Only update the view if the selected object is different from the one already selected and if the selected node is an ObjectNode (not one of its subclasses). The latter to avoid that the special explorers trigger an update
                return;
                            
            checkForUnsavedView(false);
            
            currentObject = obj;
            configObject.setProperty("currentObject", currentObject);
            
            setDisplayName(null);
            btnHighContrast.setSelected(false);
            scene.clear();
            
            if (currentObject.getClassName().equals(Constants.DUMMYROOT) || !CommunicationsStub.getInstance().getMetaForClass(currentObject.getClassName(), false).isViewable()) {
                NotificationUtil.getInstance().showStatusMessage("This currentObject doesn't have any view", false);
                disableView();
                return;
            }
            
            service.renderView();
            setDisplayName(currentObject.toString());
            toggleButtons(true);
            btnConnect.setSelected(false);
            configObject.setProperty("saved", true);
            setHtmlDisplayName(getDisplayName());
        }
    }
}
