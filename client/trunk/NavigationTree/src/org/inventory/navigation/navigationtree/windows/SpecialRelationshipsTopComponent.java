/*
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
package org.inventory.navigation.navigationtree.windows;

import java.awt.BorderLayout;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.SpecialRelatedObjectNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Shows a tree with the special relationships of an object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@TopComponent.Description(
    preferredID = "SpecialRelationshipsTopComponent",
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "navigator", openAtStartup = false)
public class SpecialRelationshipsTopComponent extends TopComponent 
    implements ExplorerManager.Provider, LookupListener {
    private BeanTreeView tree;
    private ExplorerManager em;
    //Singleton
    private static SpecialRelationshipsTopComponent self;
    private Result<ObjectNode> lookupResult;
    
    private SpecialRelationshipsTopComponent() {
        em = new ExplorerManager();
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/navigationtree/Bundle").getString("LBL_RELATIONSHIPS"));
        tree = new BeanTreeView();
        setLayout(new BorderLayout());
        add(tree);
        em.setRootContext(Node.EMPTY);
        em.getRootContext().setDisplayName("Select a node from the Navigation Tree");
    }
    
    public static SpecialRelationshipsTopComponent getInstance() {
        if (self  == null)
            self = new SpecialRelationshipsTopComponent();
        Mode navigator = WindowManager.getDefault().findMode("navigator");//For some reason, the TopComponent.Registration annotation is being ignored
        navigator.dockInto(self);
        return self;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
    
    @Override
    public void componentOpened() {
        lookupResult = Utilities.actionsGlobalContext().lookupResult(ObjectNode.class);
        lookupResult.addLookupListener(this);
        resultChanged(null);
    }
    
    @Override
    public void componentClosed() {
        em.setRootContext(Node.EMPTY);
        lookupResult.removeLookupListener(this);
    }
    
    @Override
    public void resultChanged(LookupEvent ev) {
        if(lookupResult.allInstances().size() == 1){
            ObjectNode node = (ObjectNode)lookupResult.allInstances().iterator().next();
            
            if (node instanceof SpecialRelatedObjectNode) //Ignore its own nodes
                return;
            
            SpecialRelatedObjectNode rootNode = new SpecialRelatedObjectNode(node.getObject());
            em.setRootContext(rootNode);
        }
    }
    
    
   
   
}
 