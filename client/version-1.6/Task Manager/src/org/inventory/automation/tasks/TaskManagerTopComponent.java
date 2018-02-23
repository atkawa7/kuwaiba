/**
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
package org.inventory.automation.tasks;

import org.inventory.automation.tasks.nodes.TaskManagerRootNode;
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
 * Top component which displays something.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.automation.tasks//Task//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "TaskManagerTopComponent",
        iconBase="org/inventory/automation/tasks/res/icon.png", 
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "org.inventory.automation.tasks.TaskManagerTopComponent")
@ActionReferences( value = {@ActionReference(path = "Menu/Tools" ), @ActionReference(path = "Toolbars/05_Tools", position = 1 )} )
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_TaskAction",
        preferredID = "TaskManagerTopComponent"
)
@Messages({
    "CTL_TaskAction=Task Manager",
    "CTL_TaskManagerTopComponent=Task Manager",
    "HINT_TaskManagerTopComponent=Schedule and Manage Tasks"
})
public final class TaskManagerTopComponent extends TopComponent implements ExplorerManager.Provider {
    private ExplorerManager em;
    public TaskManagerTopComponent() {
        initComponents();
        setName(Bundle.CTL_TaskManagerTopComponent());
        setToolTipText(Bundle.HINT_TaskManagerTopComponent());
        initCustomComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlScrollMain = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());
        add(pnlScrollMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane pnlScrollMain;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        em.setRootContext(new TaskManagerRootNode());
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

    private void initCustomComponents() {
        em = new ExplorerManager();
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        BeanTreeView tree = new BeanTreeView();
        pnlScrollMain.setViewportView(tree);
        
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
}
