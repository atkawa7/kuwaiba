/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.management.software;

import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Shows information about software licenses
 */
@ConvertAsProperties(
    dtd = "-//org.kuwaiba.management.software//SoftwareManager//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "SoftwareManagerTopComponent",
iconBase = "org/kuwaiba/management/services/res/icon.png", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
@ActionID(category = "Window", id = "org.kuwaiba.management.software.SoftwareManagerTopComponent")
//@ActionReferences(value = {@ActionReference(path = "Menu/Tools"),
//    @ActionReference(path = "Toolbars/Tools")})
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_SoftwareManagerAction",
preferredID = "SoftwareManagerTopComponent")
@Messages({
    "CTL_SoftwareManagerAction=Software Manager",
    "CTL_SoftwareManagerTopComponent=Software Manager",
    "HINT_SoftwareManagerTopComponent=Software licenses infomartion"
})
public final class SoftwareManagerTopComponent extends TopComponent 
    implements ExplorerManager.Provider, Refreshable {
    private ExplorerManager em = new ExplorerManager();
    private BeanTreeView tree;
    private NotificationUtil nu;
    private SoftwareManagerService sms;
    
    public SoftwareManagerTopComponent() {
        initComponents();
        setName(Bundle.CTL_SoftwareManagerTopComponent());
        setToolTipText(Bundle.HINT_SoftwareManagerTopComponent());
        em = new ExplorerManager();
        nu = Lookup.getDefault().lookup(NotificationUtil.class);
        sms = new SoftwareManagerService(this);
        tree = new BeanTreeView();
        pnlScrollMain.setViewportView(tree);
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
        sms.setTreeRoot();
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
    
    public NotificationUtil getNotifier(){
        return nu;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public void refresh() {
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
        sms.setTreeRoot();
    }
}
