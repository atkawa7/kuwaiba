/**
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
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

import java.util.ArrayList;
import java.util.List;
import javax.swing.ActionMap;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.behaviors.RefreshableTopComponent;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectChildren;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.RootObjectNode;
import org.inventory.navigation.applicationnodes.pools.PoolRootNode;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

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
//@ActionReference(path = "Menu/Tools" /*, position = 333 */)
@ActionReference(path = "Toolbars/Tools,Menu/Tools" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_PoolsAction",
preferredID = "PoolsTopComponent")
public final class PoolsTopComponent extends TopComponent implements ExplorerManager.Provider, RefreshableTopComponent{
    
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
        associateLookup(ExplorerUtils.createLookup(em, new ActionMap()));
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
    @Override
    public void componentOpened() {
        setRoot();
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

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

   public void setRoot(){
        LocalObjectLight[] rootChildren = ps.getRootChildren();
        if (rootChildren != null)
            em.setRootContext(new PoolRootNode(rootChildren));
        else
            em.setRootContext(Node.EMPTY);
    }

    @Override
    public void refresh() {
        if (em.getRootContext() instanceof RootObjectNode){
            List<Node> toBeDeleted = new ArrayList<Node>();
            for (Node child : em.getRootContext().getChildren().getNodes()){
                if (!((ObjectNode)child).refresh())
                    toBeDeleted.add(child);
            }
            for (Node deadNode : toBeDeleted)
                ((ObjectChildren)em.getRootContext().getChildren()).remove(new Node[]{deadNode});
        }else{
            setRoot();
            revalidate();
        }
    }
}
