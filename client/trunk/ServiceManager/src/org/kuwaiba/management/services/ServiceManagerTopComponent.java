/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.management.services;

import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/**
 * Service Manager Top component
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ConvertAsProperties(
    dtd = "-//org.kuwaiba.management.services//ServiceManager//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "ServiceManagerTopComponent",
iconBase = "org/kuwaiba/management/services/res/icon.png",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "org.kuwaiba.management.services.ServiceManagerTopComponent")
@ActionReferences(value = {@ActionReference(path = "Menu/Tools/Administrative"),
    @ActionReference(path = "Toolbars/Tools")})
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_ServiceManagerAction",
preferredID = "ServiceManagerTopComponent")
@Messages({
    "CTL_ServiceManagerAction=ServiceManager",
    "CTL_ServiceManagerTopComponent=ServiceManager Window",
    "HINT_ServiceManagerTopComponent=This is a ServiceManager window"
})
public final class ServiceManagerTopComponent extends TopComponent 
implements ExplorerManager.Provider, Refreshable{

    private ExplorerManager em = new ExplorerManager();
    private BeanTreeView tree;
    private NotificationUtil nu;
    private ServiceManagerService sms;
    
    public ServiceManagerTopComponent() {
        initComponents();
        setName(Bundle.CTL_ServiceManagerTopComponent());
        setToolTipText(Bundle.HINT_ServiceManagerTopComponent());
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        nu = Lookup.getDefault().lookup(NotificationUtil.class);
        sms = new ServiceManagerService(this);
        tree = new BeanTreeView();
        pnlSrollMain.setViewportView(tree);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlSrollMain = new javax.swing.JScrollPane();

        setLayout(new java.awt.BorderLayout());
        add(pnlSrollMain, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane pnlSrollMain;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        sms.setTreeRoot();
    }

    @Override
    public void componentClosed() {
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
        TopComponent propertiesWindow = WindowManager.getDefault().findTopComponent("properties");
        propertiesWindow.close();
        //Workaround, because when you close a TC whose mode is "explorer" and open it again,
        //it docks as "explorer". This forces the TC to be always docked "explorer"
        Mode myMode = WindowManager.getDefault().findMode("explorer"); //NOI18N
        myMode.dockInto(this);
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
    
    public NotificationUtil getNotifier(){
        return nu;
    }

    @Override
    public void refresh() {
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
        sms.setTreeRoot();
    }
}
