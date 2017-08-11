/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.management.services.nodes.actions.endtoend;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.visual.configuration.ObjectViewConfigurationObject;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.inventory.core.visual.scene.AbstractScene;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.TopComponent;

/**
 * Top Component that displays the end-to-end view of service
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class EndToEndViewTopComponent extends TopComponent implements 
        ExplorerManager.Provider, ActionListener, Refreshable
{
    private ExplorerManager em = new ExplorerManager();
    private AbstractScene scene;
    private JScrollPane pnlScrollMain;
    private JToolBar barMainToolBar; 
    private ObjectViewConfigurationObject configObject;
    private EndToEndViewService service;
    
    public EndToEndViewTopComponent(final LocalObjectLight currentService, final AbstractScene scene) {
        setLayout(new BorderLayout());
        this.configObject = new ObjectViewConfigurationObject();
        configObject.setProperty("saved", true);
        configObject.setProperty("currentObject", currentService);
        configObject.setProperty("currentView", null);
        configObject.setProperty("connectContainer", true);
        this.scene = scene;
        this.scene.setConfigObject(configObject);
        this.service = new EndToEndViewService(scene);
        
        pnlScrollMain = new JScrollPane(scene.createView());
        add(pnlScrollMain);
        add(scene.createSatelliteView(), BorderLayout.SOUTH);
        setDisplayName(String.format("End-to-end view for service %s", currentService));

        // <editor-fold defaultstate="collapsed" desc="Tool Bar Definition">
        barMainToolBar = new JToolBar();
        JButton btnSave = new JButton(new ImageIcon(getClass().getResource("/org/kuwaiba/management/services/res/save.png")));
        btnSave.setToolTipText("Save the current view");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                btnSaveActionPerformed(e);
            }
        });
        
        JButton btnFrame = new JButton(new ImageIcon(getClass().getResource("/org/kuwaiba/management/services/res/frame.png")));
        btnFrame.setToolTipText("Add a Frame");
        btnFrame.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                btnAddFrameActionPerformed(e);
            }
        });
        
        JButton btnExport = new JButton(new ImageIcon(getClass().getResource("/org/kuwaiba/management/services/res/export.png")));
        btnExport.setToolTipText("Export to popular image formats");
        btnExport.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ExportScenePanel exportPanel = new ExportScenePanel(
                        new SceneExportFilter[]{ ImageFilter.getInstance() }, scene, currentService.toString());
            DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export Options",true, exportPanel);
            DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
            }
        });
        JButton btnRefresh = new JButton(new ImageIcon(getClass().getResource("/org/kuwaiba/management/services/res/refresh.png")));
        btnRefresh.setToolTipText("Refresh the current view");
        btnRefresh.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                scene.clear();
                scene.render(currentService);
            }
        });
        barMainToolBar.add(btnSave);
        barMainToolBar.add(btnExport);
        barMainToolBar.add(btnRefresh);
        barMainToolBar.add(btnFrame);
        // </editor-fold>  
        add(barMainToolBar, BorderLayout.NORTH);
        associateLookup(scene.getLookup());
    }

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {                                        
        service.saveView();
        setHtmlDisplayName(getDisplayName());
        configObject.setProperty("saved", true);
    }      
    
    private void btnAddFrameActionPerformed(java.awt.event.ActionEvent evt) {
        ((EndToEndViewSimpleScene)scene).addFreeFrame();
    }
    
    private void btnAddImageActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fChooser = new JFileChooser();
        fChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fChooser.setFileFilter(new FileNameExtensionFilter("Image files", "gif","jpg", "png"));
        if (fChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
            try {
                Image myBackgroundImage = ImageIO.read(new File(fChooser.getSelectedFile().getAbsolutePath()));
                //((EndToEndViewSimpleScene)scene).addImage(myBackgroundImage);
            } catch (IOException ex) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, ex.getMessage());
            }
        }
    }
    
    @Override
    protected void componentOpened() {
        service.renderView();
        configObject.setProperty("saved", true);
        scene.addChangeListener(this);
    }
    
    @Override
    public boolean canClose(){
        return checkForUnsavedView(true);
    }
    
    @Override
    protected void componentClosed() {
        scene.clear();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    public void setSaved(boolean value) {
        configObject.setProperty("saved", value);
        
        if (value)
            this.setHtmlDisplayName(this.getDisplayName());
        else
            this.setHtmlDisplayName(String.format("<html><b>%s [Modified]</b></html>", getDisplayName()));
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
    public void actionPerformed(ActionEvent e) {
        switch (e.getID()){
            case EndToEndViewSimpleScene.SCENE_CHANGE:
                this.setSaved(false);
                break;
            case EndToEndViewSimpleScene.SCENE_CHANGEANDSAVE:
                btnSaveActionPerformed(e);
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "An external change was detected. The view has been saved automatically");
        }
    }
    
    @Override
    public void refresh() {
        if (checkForUnsavedView(true)) {
            scene.clear();
            service.renderView();
        }
    }
}
