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
package org.inventory.navigation.navigationtree;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.behaviors.Refreshable;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectChildren;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.RootObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.actions.DeleteBusinessObjectAction;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Navigation Tree Top Component
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@ConvertAsProperties(dtd = "-//org.inventory.navigation.navigationtree//NavigationTree//EN",
autostore = false)
public final class NavigationTreeTopComponent extends TopComponent
            implements ExplorerManager.Provider, Refreshable{

    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/inventory/navigation/navigationtree/res/icon.png";
    private static NavigationTreeTopComponent instance;
    private static final String PREFERRED_ID = "NavigationTreeTopComponent";
    private final ExplorerManager em = new ExplorerManager();
    private NavigationTreeService nts;
    private BeanTreeView treeView;

    public NavigationTreeTopComponent() {
        initComponents();
        initComponentsCustom();
        setName(NbBundle.getMessage(NavigationTreeTopComponent.class, "CTL_NavigationTreeTopComponent"));
        setToolTipText(NbBundle.getMessage(NavigationTreeTopComponent.class, "HINT_NavigationTreeTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }

        /*
     * Adds and setup all the components created without the help of the GUI Editor
     */
    public void initComponentsCustom(){
        //Associates a lookup to this component
        //use InstanceContent dynamic lookups (?), and ProxyLookup to expose many lookups
        //within the same (?)
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(em));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(em));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(em));
        map.put(DeleteBusinessObjectAction.ACTION_MAP_KEY, SystemAction.get(DeleteBusinessObjectAction.class));

        //Now the keystrokes
        InputMap keys = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.copyAction);
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.cutAction);
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), DefaultEditorKit.pasteAction);
        keys.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), DeleteBusinessObjectAction.ACTION_MAP_KEY);
        
        nts = new NavigationTreeService(this);
        associateLookup(ExplorerUtils.createLookup(em, map));
        treeView = new BeanTreeView();
        treeView.setWheelScrollingEnabled(true);
        add(treeView);
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
    public static synchronized NavigationTreeTopComponent getDefault() {
        if (instance == null) {
            instance = new NavigationTreeTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the NavigationTreeTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized NavigationTreeTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(NavigationTreeTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof NavigationTreeTopComponent) {
            return (NavigationTreeTopComponent) win;
        }
        Logger.getLogger(NavigationTreeTopComponent.class.getName()).warning(
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
        setRoot();
        ExplorerUtils.activateActions(em, true);
    }

    @Override
    public void componentClosed() {
        ExplorerUtils.activateActions(em, false);
        em.setRootContext(Node.EMPTY);
    }

    void writeProperties(java.util.Properties p) {
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    public void setRoot(){
        LocalObjectLight[] rootChildren = nts.getRootChildren();
        if (rootChildren != null){
            RootObjectNode root = new RootObjectNode(new ObjectChildren(rootChildren));
            em.setRootContext(root);
        }
    }

    @Override
    public void refresh() {
        if (em.getRootContext() instanceof RootObjectNode){
            List<Node> toBeDeleted = new ArrayList<>();
            for (Node child : em.getRootContext().getChildren().getNodes()){
                if (!((ObjectNode)child).refresh())
                    toBeDeleted.add(child);
            }
            for (Node deadNode : toBeDeleted)
                ((ObjectChildren)em.getRootContext().getChildren()).remove(new Node[]{deadNode});
        }else {
            setRoot();
            revalidate();
        }
    }
}
