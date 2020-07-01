/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package com.neotropic.inventory.modules.mpls.views;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.behaviors.Refreshable;
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
 * Top Component for MplsLinks endpoints
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class MplsLinkEndPointsViewTopComponent extends TopComponent implements 
        ExplorerManager.Provider, Refreshable
{
    private ExplorerManager em = new ExplorerManager();
    private AbstractScene scene;
    private LocalObjectLight mplsLink;
    private CommunicationsStub com = CommunicationsStub.getInstance();

    public MplsLinkEndPointsViewTopComponent(final LocalObjectLight mplsLink, final AbstractScene scene) {
        this.mplsLink = mplsLink;
        List<LocalObjectViewLight> serviceViews = com.getObjectRelatedViews(this.mplsLink.getId(), this.mplsLink.getClassName());
        
        if (serviceViews == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
            setEnabled(false);
        }
        else {
            setLayout(new BorderLayout());
            this.scene = scene;

            JScrollPane pnlScrollMain = new JScrollPane(scene.createView());
            add(pnlScrollMain);
            add(scene.createSatelliteView(), BorderLayout.SOUTH);
            setDisplayName(String.format("End-to-end view for service %s", mplsLink));

            // <editor-fold defaultstate="collapsed" desc="Tool Bar Definition">
            JToolBar barMainToolBar = new JToolBar();
//            JButton btnSave = new JButton(new ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/mpls/res/save.png")));
//            btnSave.setToolTipText("Save the current view");
//            btnSave.addActionListener(new ActionListener() {
//                @Override
//                public void actionPerformed(ActionEvent e) {
//                    saveView();
//                }
//            });

            JButton btnExport = new JButton(new ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/mpls/res/export.png")));
            btnExport.setToolTipText("Export to popular image formats");
            btnExport.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    ExportScenePanel exportPanel = new ExportScenePanel(
                            new SceneExportFilter[]{ ImageFilter.getInstance() }, scene, mplsLink.toString());
                DialogDescriptor dd = new DialogDescriptor(exportPanel, "Export Options",true, exportPanel);
                DialogDisplayer.getDefault().createDialog(dd).setVisible(true);
                }
            });
            JButton btnRefresh = new JButton(new ImageIcon(getClass().getResource("/com/neotropic/inventory/modules/mpls/res/refresh.png")));
            btnRefresh.setToolTipText("Refresh the current view");
            btnRefresh.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    componentClosed();
                    componentOpened();
                }
            });
            
            barMainToolBar.add(btnExport);
            barMainToolBar.add(btnRefresh);
            // </editor-fold>  
            add(barMainToolBar, BorderLayout.NORTH);
            associateLookup(scene.getLookup());
        }
    }
    
    @Override
    public String getDisplayName() {
        return String.format("MPLSLink endpoints %s", mplsLink.toString());
    }
    
    @Override
    protected void componentOpened() {
        //This view is autogenerated every time
        scene.render(mplsLink); 
        //scene.addChangeListener(this);
    }
    
    @Override
    public boolean canClose(){
        return true;
    }
    
    @Override
    public void refresh() {
        scene.clear();
        scene.render(mplsLink);
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
 
}
