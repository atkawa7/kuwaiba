/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.core.templates.layouts;

import org.inventory.core.templates.layouts.nodes.DeviceLayoutsRootNode;
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
 * Top component which displays the existing device layouts
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@ConvertAsProperties(
        dtd = "-//org.inventory.core.templates.layouts//DeviceLayouts//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "DeviceLayoutsTopComponent",
        iconBase="org/inventory/core/templates/res/layoutIcon.png", 
        persistenceType = TopComponent.PERSISTENCE_NEVER
)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "org.inventory.core.templates.layouts.DeviceLayoutsTopComponent")
@ActionReferences(value = {@ActionReference(path = "Menu/Tools"),
    @ActionReference(path = "Toolbars/04_Customization", position = 5)})
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_DeviceLayoutsAction",
        preferredID = "DeviceLayoutsTopComponent"
)
@Messages({
    "CTL_DeviceLayoutsAction=Device Layouts",
    "CTL_DeviceLayoutsTopComponent=Device Layouts",
    "HINT_DeviceLayoutsTopComponent=Device Layouts"
})
public final class DeviceLayoutsTopComponent extends TopComponent implements ExplorerManager.Provider, Refreshable {
    private final ExplorerManager em = new ExplorerManager();
    private final BeanTreeView beanTreeView = new BeanTreeView();
    
    public DeviceLayoutsTopComponent() {
        initComponents();
        initCustomComponents();
        setName(Bundle.CTL_DeviceLayoutsTopComponent());
        setToolTipText(Bundle.HINT_DeviceLayoutsTopComponent());

    }
    
    private void initCustomComponents() {
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        add(beanTreeView);
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
        beanTreeView.setRootVisible(false);
        
        em.setRootContext(new DeviceLayoutsRootNode());
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
