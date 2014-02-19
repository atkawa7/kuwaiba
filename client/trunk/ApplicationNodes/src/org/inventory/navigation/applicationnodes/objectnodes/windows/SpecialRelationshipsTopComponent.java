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
package org.inventory.navigation.applicationnodes.objectnodes.windows;

import java.awt.BorderLayout;
import java.awt.Image;
import java.util.HashMap;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
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
public class SpecialRelationshipsTopComponent extends TopComponent implements ExplorerManager.Provider {
    private BeanTreeView tree;
    private ExplorerManager em;
    private static final Image icon = ImageUtilities.loadImage("org/inventory/navigation/applicationnodes/res/relationship.png");
    
    public SpecialRelationshipsTopComponent(LocalObjectLight object, HashMap<String, LocalObjectLight[]> relationships) {
        em = new ExplorerManager();
        associateLookup(ExplorerUtils.createLookup(em, getActionMap()));
        setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_RELATIONSHIPS") + " - " + object);
        tree = new BeanTreeView();
        tree.setRootVisible(false);
        em.setRootContext(new RootNode(relationships));
        setLayout(new BorderLayout());
        add(tree);
        Mode navigator = WindowManager.getDefault().findMode("navigator");//For some reason, the TopComponent.Registration annotation is being ignored
        navigator.dockInto(this);
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }
    
    @Override
    public void componentOpened() {    }
    
    private class RootNode extends AbstractNode {
        
        public RootNode (HashMap<String, LocalObjectLight[]> relationships){
            super (new Children.Array());
            for (String relationship : relationships.keySet())
                getChildren().add(new RelationshipNode[]{new RelationshipNode(relationship, relationships.get(relationship))});
        }     
    }
    
    private class RelationshipNode extends AbstractNode {
        public RelationshipNode (String label, LocalObjectLight[] relatedObjects){
            super(new RelationshipChildren(relatedObjects));
            this.setDisplayName(label);
        }
        
        @Override
        public Image getIcon(int type) {
            return icon;
        }
        
        @Override
        public Image getOpenedIcon(int type) {
            return icon;
        }
    }
    private class RelationshipChildren extends Children.Array {
        private LocalObjectLight[] relatedObjects;
        
        public RelationshipChildren(LocalObjectLight[] relatedObjects){
            this.relatedObjects = relatedObjects;
        }

        @Override
        protected void addNotify() {
            for (LocalObjectLight item : relatedObjects){
                ObjectNode newNode = new ObjectNode(item, true);
                remove(new Node[]{newNode});
                add(new Node[]{newNode});
           }
        }
    }
}
