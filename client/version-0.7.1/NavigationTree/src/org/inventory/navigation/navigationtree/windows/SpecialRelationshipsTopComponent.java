/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
import java.util.HashMap;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.LabelNode;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.RootObjectNode;
import org.inventory.navigation.applicationnodes.objectnodes.SpecialObjectNode;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
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
        em.getRootContext().getChildren().remove(em.getRootContext().getChildren().getNodes());
        lookupResult.removeLookupListener(this);
    }
    
    @Override
    public void resultChanged(LookupEvent ev) {
        if(lookupResult.allInstances().size() == 1){
            ObjectNode node = (ObjectNode)lookupResult.allInstances().iterator().next();
            
            if (node instanceof SpecialObjectNode) //Ignore its own nodes
                return;
            
            //If the current object is the same that the last selected object, do nothing
            if (node.equals(em.getRootContext()))
                return;
            
            HashMap<String, LocalObjectLight[]> relationships = CommunicationsStub.
                   getInstance().getSpecialAttributes(node.getObject().getClassName(), node.getObject().getOid());
            
            if (relationships == null){
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return;
            }
            
            SpecialRootNode rootNode = new SpecialRootNode(node.getObject(), relationships);
            rootNode.setDisplayName(String.format("%s relationships found", relationships.size()));
            em.setRootContext(rootNode);
        }
    }
    
    /**
    * Dummy class to represent a node in the special relationships tree
    */
   private class SpecialRootNode extends AbstractNode {  
       /**
        * Current Object being displayed
        */
       private LocalObjectLight currentObject;

       public SpecialRootNode() {
           super (new Children.Array());
           setIconBaseWithExtension(RootObjectNode.DEFAULT_ICON_PATH);
       }

       public SpecialRootNode (LocalObjectLight rootObject, HashMap<String, LocalObjectLight[]> children){
           this();
           currentObject = rootObject;
           for (String label : children.keySet())
               getChildren().add(new LabelNode[]{new LabelNode(label, children.get(label))});
       }

       public SpecialRootNode (LocalObjectLight rootObject){
           this();
           currentObject = rootObject;
           getChildren().add(new SpecialObjectNode[]{new SpecialObjectNode(rootObject)});
       }

       public LocalObjectLight getCurrentObject() {
           return currentObject;
       }
       
       @Override
       public boolean equals (Object anObject) {
           if (anObject instanceof ObjectNode)
               return ((ObjectNode)anObject).getObject().getOid() == currentObject.getOid();
           
           if (anObject instanceof SpecialRootNode)
               return ((SpecialRootNode)anObject).getCurrentObject().getOid() == currentObject.getOid();
           
           return false;
       }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + (this.currentObject != null ? this.currentObject.hashCode() : 0);
            return hash;
        }
   }


}
