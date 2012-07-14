/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.inventory.customization.listmanager;

import java.util.logging.Logger;
import javax.swing.ActionMap;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.listmanagernodes.ListTypeChildren;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerManager.Provider;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;

/**
 * Administration frontend for list-type attributes
 */
@ConvertAsProperties(dtd = "-//org.inventory.customization.listmanager//ListManager//EN",
autostore = false)
public final class ListManagerTopComponent extends TopComponent
        implements Provider{

    private static ListManagerTopComponent instance;
    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/inventory/customization/listmanager/res/icon.png";
    static final String ROOT_ICON_PATH = "org/inventory/customization/listmanager/res/root.png";
    private static final String PREFERRED_ID = "ListManagerTopComponent";

    private final ExplorerManager em = new ExplorerManager();
    private BeanTreeView bt;
    private ListManagerService lms;
    private NotificationUtil nu = null;

    public ListManagerTopComponent() {
        initComponents();
        initCustomComponents();
        setName(NbBundle.getMessage(ListManagerTopComponent.class, "CTL_ListManagerTopComponent"));
        setToolTipText(NbBundle.getMessage(ListManagerTopComponent.class, "HINT_ListManagerTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        associateLookup(ExplorerUtils.createLookup(em, new ActionMap()));
        
    }

    public void initCustomComponents(){
        bt = new BeanTreeView();
        lms = new ListManagerService(this);
        add(bt);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
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
    public static synchronized ListManagerTopComponent getDefault() {
        if (instance == null) {
            instance = new ListManagerTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ListManagerTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ListManagerTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ListManagerTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ListManagerTopComponent) {
            return (ListManagerTopComponent) win;
        }
        Logger.getLogger(ListManagerTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    @Override
    public void componentOpened() {
        AbstractNode root = new AbstractNode(new ListTypeChildren(lms.getInstanceableListTypes()));
        root.setIconBaseWithExtension(ROOT_ICON_PATH);
        em.setRootContext(root);
        em.getRootContext().setDisplayName("Available List Types");
    }

    @Override
    public void componentClosed() {
        lms.refreshLists();
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public ExplorerManager getExplorerManager() {
        return em;
    }

    public NotificationUtil getNotifier(){
        if (nu == null)
            nu = Lookup.getDefault().lookup(NotificationUtil.class);
        return nu;
    }
}
