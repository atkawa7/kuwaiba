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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import org.inventory.communications.core.LocalObjectLight;
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
public class EndToEndViewTopComponent extends TopComponent implements ExplorerManager.Provider {
    private ExplorerManager em = new ExplorerManager();
    private AbstractScene scene;
    private JScrollPane pnlScrollMain;
    private LocalObjectLight service;
    private JToolBar barMainToolBar; 

    public EndToEndViewTopComponent(final LocalObjectLight service, final AbstractScene scene) {
        setLayout(new BorderLayout());
        this.service = service;
        this.scene = scene;
        pnlScrollMain = new JScrollPane(scene.createView());
        add(pnlScrollMain);
        add(scene.createSatelliteView(), BorderLayout.SOUTH);
        setDisplayName(String.format("End-to-end view for service %s", service));
        barMainToolBar = new JToolBar();
        JButton btnExport = new JButton(new ImageIcon(getClass().getResource("/org/kuwaiba/management/services/res/export.png")));
        btnExport.setToolTipText("Export to popular image formats");
        btnExport.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ExportScenePanel exportPanel = new ExportScenePanel(
                        new SceneExportFilter[]{ ImageFilter.getInstance() }, scene, service.toString());
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
                scene.render(service);
            }
        });
        barMainToolBar.add(btnExport);
        barMainToolBar.add(btnRefresh);
        add(barMainToolBar, BorderLayout.NORTH);
        associateLookup(scene.getLookup());
    }

    @Override
    protected void componentClosed() {
        scene.clear();
    }

    @Override
    protected void componentOpened() {
        scene.render(service);
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
