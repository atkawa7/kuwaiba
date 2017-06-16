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
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either expregss or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.core.usermanager;

import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.usermanager.nodes.UserManagerRootNode;
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
 * Top component for the User Manager Module.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.core.usermanager//UserManager//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "UserManagerTopComponent",
        iconBase="org/inventory/core/usermanager/res/icon.png", 
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Tools", id = "org.inventory.core.usermanager.UserManagerTopComponent")
@ActionReferences(value = {@ActionReference(path = "Menu/Tools"),
    @ActionReference(path = "Toolbars/Tools")} /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_UserManagerAction",
        preferredID = "UserManagerTopComponent"
)
@Messages({
    "CTL_UserManagerAction=User Manager",
    "CTL_UserManagerTopComponent=User Manager",
    "HINT_UserManagerTopComponent=Manage users and groups"
})
public final class UserManagerTopComponent extends TopComponent 
        implements ExplorerManager.Provider, Refreshable {

    private ExplorerManager em = new ExplorerManager();
    private BeanTreeView treeMain;
    
    public UserManagerTopComponent() {
        initComponents();
        initCustomComponents();
        setName(Bundle.CTL_UserManagerTopComponent());
        setToolTipText(Bundle.HINT_UserManagerTopComponent());

    }
    
    private void initCustomComponents() {
        treeMain = new BeanTreeView();
        add(treeMain);
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
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
        em.setRootContext(new UserManagerRootNode());
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
