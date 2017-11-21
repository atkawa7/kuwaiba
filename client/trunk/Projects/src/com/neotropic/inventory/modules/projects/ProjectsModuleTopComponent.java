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
package com.neotropic.inventory.modules.projects;

import com.neotropic.inventory.modules.projects.nodes.ProjectRootNode;
import javax.swing.JOptionPane;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.i18n.I18N;
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
 * Top component for the Projects Module
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ConvertAsProperties(
        dtd = "-//com.neotropic.inventory.modules.projects//ProjectsModule//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ProjectsModuleTopComponent",
        iconBase="com/neotropic/inventory/modules/projects/res/icon.png", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "com.neotropic.inventory.modules.projects.ProjectsModuleTopComponent")
@ActionReferences(value = {@ActionReference(path = "Menu/Tools/Advanced"),
    @ActionReference(path = "Toolbars/10_Advanced", position = 6)})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ProjectsModuleAction",
        preferredID = "ProjectsModuleTopComponent"
)
@Messages({
    "CTL_ProjectsModuleAction=Projects",
    "CTL_ProjectsModuleTopComponent=Projects",
    "HINT_ProjectsModuleTopComponent=Projects"
})
public final class ProjectsModuleTopComponent extends TopComponent implements ExplorerManager.Provider, Refreshable {
    private final ExplorerManager em = new ExplorerManager();
    private BeanTreeView treeView;
    private ProjectsModuleService service;

    public ProjectsModuleTopComponent() {
        initComponents();
        setName(Bundle.CTL_ProjectsModuleTopComponent());
        setToolTipText(Bundle.HINT_ProjectsModuleTopComponent());
        initCustomComponents();
    }
        
    public void initCustomComponents() {
        service = new ProjectsModuleService();
        treeView = new BeanTreeView();
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        add(treeView);
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
        if (CommunicationsStub.getInstance().getSession() == null) {
            close();
            return;
        }
        
        if (service.isDataBaseUpdated()) {
            close();
            JOptionPane.showMessageDialog(null, "This database seems outdated. Contact your administrator to apply the necessary patches to run the Projects module", I18N.gm("error"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        em.setRootContext(new ProjectRootNode());
        ExplorerUtils.activateActions(em, true);
    }

    @Override
    public void componentClosed() {
        ExplorerUtils.activateActions(em, false);
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
