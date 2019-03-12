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
package org.kuwaiba.core.config.variables;

import org.inventory.core.services.api.behaviors.Refreshable;
import org.kuwaiba.core.config.variables.nodes.ConfigurationVariablesRootNode;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Main TopComponent for the Configuration Variables module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@ConvertAsProperties(
        dtd = "-//org.kuwaiba.core.config.variables//ConfigurationVariables//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "ConfigurationVariablesTopComponent",
        iconBase="org/kuwaiba/core/config/res/variables.png", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "org.kuwaiba.core.config.variables.ConfigurationVariablesTopComponent")
@ActionReference(path = "Menu/Tools/Configuration")
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_ConfigurationVariablesAction",
        preferredID = "ConfigurationVariablesTopComponent"
)
@Messages({
    "CTL_ConfigurationVariablesAction=Configuration Variables",
    "CTL_ConfigurationVariablesTopComponent=Configuration Variables",
    "HINT_ConfigurationVariablesTopComponent=Create and Edit Configuration Variables"
})
public final class ConfigurationVariablesTopComponent extends TopComponent 
        implements ExplorerManager.Provider, Refreshable {

    private ExplorerManager em = new ExplorerManager();
    
    public ConfigurationVariablesTopComponent() {
        initComponents();
        initCustomComponents();
        setName(Bundle.CTL_ConfigurationVariablesTopComponent());
        setToolTipText(Bundle.HINT_ConfigurationVariablesTopComponent());

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
        em.setRootContext(new ConfigurationVariablesRootNode());
    }

    @Override
    public void componentClosed() {
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
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
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        add(new BeanTreeView());
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
