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
 */

package org.kuwaiba.management.services.views.topology;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.export.ExportScenePanel;
import org.inventory.core.visual.export.filters.ImageFilter;
import org.inventory.core.visual.export.filters.SceneExportFilter;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.windows.TopComponent;

/**
 * This TC encloses the ServiceTopologyViewScene
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TopologyViewTopComponent extends TopComponent implements 
        ExplorerManager.Provider, ActionListener {

    private ExplorerManager em = new ExplorerManager();
    private TopologyViewScene scene;
    private LocalObjectLight currentService;

    public TopologyViewTopComponent(final LocalObjectLight currentService, final TopologyViewScene scene) {
        setLayout(new BorderLayout());
        this.scene = scene;
        
        JScrollPane pnlScrollMain = new JScrollPane(scene.createView());
        add(pnlScrollMain);
        add(scene.createSatelliteView(), BorderLayout.SOUTH);
        this.currentService = currentService;

        // <editor-fold defaultstate="collapsed" desc="Tool Bar Definition">
        JToolBar barMainToolBar = new JToolBar();
        JButton btnSave = new JButton(new ImageIcon(getClass().getResource("/org/kuwaiba/management/services/res/save.png")));
        btnSave.setToolTipText("Save the current view");
        btnSave.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Nothing to do for now");
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
        // </editor-fold>  
        add(barMainToolBar, BorderLayout.NORTH);
        associateLookup(scene.getLookup());
    }
    
    @Override
    public String getDisplayName() {
        return currentService.toString();
    }
    
    @Override
    protected void componentOpened() {
        scene.render(currentService);
        scene.addChangeListener(this);
    }
    
    @Override
    public boolean canClose(){
        //return checkForUnsavedView(true);
        return true;
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

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
