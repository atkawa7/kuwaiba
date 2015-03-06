/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.navigationtree.windows;

import java.awt.BorderLayout;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.SpecialObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.SpecialRootNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Used to explore a link or a container 
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@TopComponent.Description(
    preferredID = "SpecialObjectExplorerTopComponent",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "navigator", openAtStartup = false)
public class SpecialChildrenTopComponent extends TopComponent 
        implements ExplorerManager.Provider, LookupListener {
    
    private ExplorerManager em;
    private BeanTreeView tree;
    //Singleton
    private static SpecialChildrenTopComponent self;
    private Lookup.Result<ObjectNode> lookupResult;
    
    private SpecialChildrenTopComponent() {
        em = new ExplorerManager();
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_SPECIALCHILDREN"));
        tree = new BeanTreeView();
        setLayout(new BorderLayout());
        add(tree);
        em.setRootContext(new SpecialRootNode());
    }
    
    public static SpecialChildrenTopComponent getInstance() {
        if (self  == null)
            self = new SpecialChildrenTopComponent();
        Mode navigator = WindowManager.getDefault().findMode("navigator");//For some reason, the TopComponent.Registration annotation is being ignored
        navigator.dockInto(self);
        return self;
    }

    @Override
    public void componentOpened() {
        lookupResult = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class);
        lookupResult.addLookupListener(this);
        resultChanged(null);
    }
    
    @Override
    public void componentClosed() {
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
        lookupResult.removeLookupListener(this);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        if(lookupResult.allInstances().size() == 1){
            //Don't update if the same object is selected
            ObjectNode node = (ObjectNode)lookupResult.allInstances().iterator().next();
            
            if (node instanceof SpecialObjectNode) //Ignore its own nodes
                return;
            
            //If the current object is the same that the last selected object, do nothing
            if (node.equals(em.getRootContext()))
                return;
            
            SpecialObjectNode newRootNode = new SpecialObjectNode(node.getObject());
            em.setRootContext(newRootNode);
            tree.expandNode(newRootNode);
        }
    }
}