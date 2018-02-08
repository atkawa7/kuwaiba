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

package org.kuwaiba.management.services.views.endtoend;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
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
        ExplorerManager.Provider, ActionListener 
{
    private ExplorerManager em = new ExplorerManager();
    private AbstractScene scene;
    private LocalObjectLight currentService;
    private LocalObjectView currentView;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private boolean saved = true;
    
    public EndToEndViewTopComponent(final LocalObjectLight currentService, final AbstractScene scene) {
        
        this.currentService = currentService;
        List<LocalObjectViewLight> serviceViews = com.getObjectRelatedViews(this.currentService.getOid(), this.currentService.getClassName());
        
        if (serviceViews == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
            setEnabled(false);
        }
        else {
            for (LocalObjectViewLight serviceView : serviceViews) {
                if (EndToEndViewSimpleScene.VIEW_CLASS.equals(serviceView.getClassName())) {
                    currentView = com.getObjectRelatedView(currentService.getOid(), currentService.getClassName(), serviceView.getId());
                    if (currentView == null) {
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
                        setEnabled(false);
                        return;
                    }  
                    break;
                }
            }
            setLayout(new BorderLayout());
            this.scene = scene;

            JScrollPane pnlScrollMain = new JScrollPane(scene.createView());
            add(pnlScrollMain);
            add(scene.createSatelliteView(), BorderLayout.SOUTH);
            setDisplayName(String.format("End-to-end view for service %s", currentService));

            // <editor-fold defaultstate="collapsed" desc="Tool Bar Definition">
            JToolBar barMainToolBar = new JToolBar();
            JButton btnSave = new JButton(new ImageIcon(getClass().getResource("/org/kuwaiba/management/services/res/save.png")));
            btnSave.setToolTipText("Save the current view");
            btnSave.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveView();
                }
            });

            JButton btnFrame = new JButton(new ImageIcon(getClass().getResource("/org/kuwaiba/management/services/res/frame.png")));
            btnFrame.setToolTipText("Add a Frame");
            btnFrame.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ((EndToEndViewSimpleScene)scene).addFreeFrame();
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
                    componentClosed();
                    componentOpened();
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
    }
    
     @Override
    public String getDisplayName() {
        return String.format("End to End Simple View for %s", currentService.toString());
    }
    
    @Override
    public String getHtmlDisplayName() {
        if (saved)
            return getDisplayName();
        else
            return String.format(I18N.gm("modified"), getDisplayName());
    }

    private void saveView() {   
        if (currentView == null) { //The service does not have a saved view associated yet, so create a new one
            long newViewId = com.createObjectRelatedView(currentService.getOid(), currentService.getClassName(), EndToEndViewSimpleScene.VIEW_CLASS, 
                    null, EndToEndViewSimpleScene.VIEW_CLASS, scene.getAsXML(), null);
            
            if (newViewId != -1) {
                currentView = new LocalObjectView(newViewId, EndToEndViewSimpleScene.VIEW_CLASS, null, null, scene.getAsXML(), scene.getBackgroundImage());
                saved = true;
                setHtmlDisplayName(getHtmlDisplayName());
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "The view was saved successfully");
            } else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
        }
        else { //Update the existing view
            if (com.updateObjectRelatedView(currentService.getOid(), currentService.getClassName(), 
                    currentView.getId(), null, null, scene.getAsXML(), scene.getBackgroundImage())) {
                saved = true;
                setHtmlDisplayName(getHtmlDisplayName());
            } else
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
        }     
    }      
   
    @Override
    protected void componentOpened() {
        if (currentView != null)
            scene.render(currentView.getStructure()); //Render the saved view, if any
        
        //Renders the default anyway to synchronize the possible changes
        //that might have appeared since the last time the view was opened
        scene.render(currentService); 
        saved = true;
        scene.addChangeListener(this);
    }
    
    @Override
    public boolean canClose(){
        return checkForUnsavedView(true);
    }
    
    @Override
    protected void componentClosed() {
        scene.clear();
        scene.removeAllListeners();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
 
    public boolean checkForUnsavedView(boolean showCancel) {
        if (!saved){
            switch (JOptionPane.showConfirmDialog(null, "This view has not been saved, do you want to save it?", 
                I18N.gm("confirmation"), showCancel ? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION)){
                case JOptionPane.YES_OPTION:
                    saveView();
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
            case AbstractScene.SCENE_CHANGE:
                saved = false;
                setHtmlDisplayName(getHtmlDisplayName());
                break;
            case AbstractScene.SCENE_CHANGEANDSAVE:
                saveView();
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, "An external change was detected. The view has been saved automatically");
        }
    }
}
