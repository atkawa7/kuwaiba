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
package org.inventory.navigation.navigationtree;

import java.awt.BorderLayout;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectChildren;
import org.inventory.navigation.navigationtree.nodes.RootObjectNode;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.windows.Mode;
import org.openide.windows.WindowManager;

/*
 * This component is not a singleton. The user would want to create more than one instance of it
 */
@ConvertAsProperties(dtd = "-//org.inventory.navigation.navigationtree//NavigationTree//EN",
autostore = false)
public final class NavigationTreeTopComponent extends TopComponent
        implements ExplorerManager.Provider{

    /** path to the icon used by the component and its open action */
    static final String ICON_PATH = "org/inventory/navigation/navigationtree/res/icon.png";
    static final String ROOT_ICON_PATH = "org/inventory/navigation/navigationtree/res/root.png";

    private final ExplorerManager em = new ExplorerManager();
    private NavigationTreeService nts;

    public NavigationTreeTopComponent() {
        initComponents();
        initComponentsCustom();
        setName(NbBundle.getMessage(NavigationTreeTopComponent.class, "CTL_NavigationTreeTopComponent"));
        setToolTipText(NbBundle.getMessage(NavigationTreeTopComponent.class, "HINT_NavigationTreeTopComponent"));
        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
    }



    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 290, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 466, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
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
        

        //Now the keystrokes (doesn't seem to be working)
        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        keys.put(KeyStroke.getKeyStroke("control C"), DefaultEditorKit.copyAction);
        keys.put(KeyStroke.getKeyStroke("control X"), DefaultEditorKit.cutAction);
        keys.put(KeyStroke.getKeyStroke("control V"), DefaultEditorKit.pasteAction);
        
        nts = new NavigationTreeService(this);
        associateLookup(ExplorerUtils.createLookup(em, map));
        setLayout(new BorderLayout());
        BeanTreeView treeView = new BeanTreeView();
        treeView.setWheelScrollingEnabled(true);

        
        LocalObjectLight[] rootChildren = nts.getRootChildren();
        if (rootChildren != null){
            RootObjectNode root = new RootObjectNode(new ObjectChildren(rootChildren));
            root.setIconBaseWithExtension(ROOT_ICON_PATH);
            em.setRootContext(root);
            em.getRootContext().setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_ROOT"));
            add(treeView,BorderLayout.CENTER);
        }
        
        /* This makes programatically the Window Manager to use an "explorer" (window docked at left side)
           this should be done because since this component is not singleton anymore, all XML configuration files
           and
         */

        Mode myMode = WindowManager.getDefault().findMode("explorer");
        myMode.dockInto(this);
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    /*public static synchronized NavigationTreeTopComponent getDefault() {
    if (instance == null) {
    instance = new NavigationTreeTopComponent();
    }
    return instance;
    }*/

    public static synchronized NavigationTreeTopComponent showComponent(){
       return new NavigationTreeTopComponent();
    }

    /**
     * Obtain the NavigationTreeTopComponent instance. Never call {@link #getDefault} directly!
     */
    /*public static synchronized NavigationTreeTopComponent findInstance() {
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
    }*/

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }

    
    @Override
    public void componentOpened() {
        //Set the tree title
        setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_TITLE"));
        ExplorerUtils.activateActions(em, true);

    }

    @Override
    public void componentClosed() {
        ExplorerUtils.activateActions(em, false);
    }

    /*
     * These to methods are interesting, since they're useful to freeze
     * the state of a given component by means of writing and reading values from
     * a properties file as if it was a hash
     */
    void writeProperties(java.util.Properties p) {
        p.setProperty("version", "1.0");
    }

    Object readProperties(java.util.Properties p) {
        return this;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    public ExplorerManager getExplorerManager() {
        return em;
    }
}
