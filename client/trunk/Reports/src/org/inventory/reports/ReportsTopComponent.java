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
 *  under the License.
 */
package org.inventory.reports;

import org.inventory.core.services.api.behaviors.Refreshable;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Main top component for this module.
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.reports//Reports//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ReportsTopComponent",
        iconBase="org/inventory/reports/res/icon.png", 
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "org.inventory.reports.ReportsTopComponent")
@ActionReferences(value = {@ActionReference(path = "Menu/Tools"),
    @ActionReference(path = "Toolbars/Tools")})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ReportsAction",
        preferredID = "ReportsTopComponent"
)
@Messages({
    "CTL_ReportsAction=Reports",
    "CTL_ReportsTopComponent=Reports",
    "HINT_ReportsTopComponent=Explore and create reports"
})
public final class ReportsTopComponent extends TopComponent implements ExplorerManager.Provider, Refreshable {

    private ExplorerManager em;
    private ReportsService service;
    
    public ReportsTopComponent() {
        initComponents();
        setName(Bundle.CTL_ReportsTopComponent());
        setToolTipText(Bundle.HINT_ReportsTopComponent());
        initCustomComponents();
    }
    
    public void initCustomComponents() {
        em = new ExplorerManager();
        service = new ReportsService(this);
        
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        
        BeanTreeView treeMain = new BeanTreeView();
        treeMain.setRootVisible(false);
        
        add(treeMain);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        service.setRoot();
    }

    @Override
    public void componentClosed() {
        em.setRootContext(Node.EMPTY);
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
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public void refresh() {
        componentClosed();
        componentOpened();
    }
}
