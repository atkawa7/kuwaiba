/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.pools;

import java.util.List;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.text.DefaultEditorKit;
import org.inventory.communications.core.LocalPool;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.pools.nodes.PoolRootNode;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Main top component for the Pools module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ConvertAsProperties(
    dtd = "-//org.inventory.navigation.pools//Pools//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "PoolsTopComponent",
    iconBase="org/inventory/navigation/pools/res/icon.png", 
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "explorer", openAtStartup = false)
@ActionID(category = "Window", id = "org.inventory.navigation.pools.PoolsTopComponent")
@ActionReference(path = "Menu/Tools" /*, position = 333 */)
//@ActionReference(path = "Toolbars/Tools" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_PoolsAction",
preferredID = "PoolsTopComponent")
public final class PoolsTopComponent extends TopComponent implements ExplorerManager.Provider, Refreshable {
    
    private static final String PREFERRED_ID = "PoolsTopComponent";
    private static PoolsTopComponent instance;
    private final ExplorerManager em = new ExplorerManager();
    private PoolsService ps;
    private BeanTreeView treeView;

    public PoolsTopComponent() {
        initComponents();
        initCustomComponents();
        setName(NbBundle.getMessage(PoolsTopComponent.class, "CTL_PoolsTopComponent"));
        setToolTipText(NbBundle.getMessage(PoolsTopComponent.class, "HINT_PoolsTopComponent"));

    }
    
    private void initCustomComponents(){
        ps = new PoolsService(this);
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(em));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(em));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(em));
        associateLookup(ExplorerUtils.createLookup(em, map));
        treeView = new BeanTreeView();
        treeView.setWheelScrollingEnabled(true);
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
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized PoolsTopComponent getDefault() {
        if (instance == null) {
            instance = new PoolsTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the NavigationTreeTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized PoolsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(PoolsTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof PoolsTopComponent) {
            return (PoolsTopComponent) win;
        }
        Logger.getLogger(PoolsTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }
    
    @Override
    public void componentOpened() {
        setRoot();
    }

    @Override
    public void componentClosed() {
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
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

   public void setRoot() {
        List<LocalPool> rootChildren = ps.getRootChildren();
        if (rootChildren != null)
            em.setRootContext(new PoolRootNode(rootChildren));
        else
            em.setRootContext(Node.EMPTY);
    }

    @Override
    public void refresh() {
        setRoot();
    }

    public NotificationUtil getNotifier() {
        return NotificationUtil.getInstance();
    }
}
