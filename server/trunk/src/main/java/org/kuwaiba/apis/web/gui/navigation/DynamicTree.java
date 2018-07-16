/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.navigation;

import com.vaadin.data.TreeData;
import com.vaadin.data.provider.TreeDataProvider;
import com.vaadin.event.ExpandEvent;
import com.vaadin.server.Page;
import com.vaadin.ui.Tree;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A tree that extends the features of the default one and makes use of the Nodes API
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class DynamicTree extends Tree<RemoteObjectLight> {

    /**
     *  Default constructor
     * @param root The root of the hierarchy
     * @param expandListener What to do when expanding a node. See org.kuwaiba.apis.web.gui.navigation.events.* for reference examples
     */
    public DynamicTree(RemoteObjectLight root, ChildrenProvider p) {
        TestData treeData = new TestData(p);
        //TreeData<RemoteObjectLight> treeData = new TreeData<>();
        treeData.addRootItems(root);
        setDataProvider(new TestDataProvider(treeData));
        //setData(treeData);
        setSizeFull();
    }   
    
    
    private class TestDataProvider extends TreeDataProvider<RemoteObjectLight> {

        public TestDataProvider(TreeData<RemoteObjectLight> treeData) {
            super(treeData);
        }
     
    }
    
    private class TestData extends TreeData<RemoteObjectLight> {
        private ChildrenProvider p;
        public TestData(ChildrenProvider p) {
            super();
            this.p = p;
        }

                
        @Override
        public List<RemoteObjectLight> getChildren(RemoteObjectLight expandedItem) {
//            List<AbstractNode> res = new ArrayList<>();
//                for (RemoteObjectLight child : p.getChildren((RemoteObjectLight)expandedItem.getObject()))
//                    res.add(new InventoryObjectNode(child));
//             return res;
            if (expandedItem == null)
                return super.getChildren(expandedItem);
            else {
                List<RemoteObjectLight> children = p.getChildren((RemoteObjectLight)expandedItem);
                for (RemoteObjectLight child : children) {
                    if (!contains(child))
                        addItem(expandedItem, child);
                }
                
                return children;
            }
        }
    }
    
    public interface ChildrenProvider {
        public List<RemoteObjectLight> getChildren(RemoteObjectLight c);
    }
   
    
//    /**
//     * Currently selected node
//     */
//    private AbstractNode currentlySelectedNode;
//    /**
//     * Nearest component that can provide access to the session variables
//     */
//    private final TopComponent parentComponent;
//    
//    /**
//     * To make use of the global variables (and from there, to the backend logic), 
//     * this component should be embedded into a TopComponent
//     * @param rootNode The root node of the tree.
//     * @param parentComponent The parent TopComponent component
//     */
//    public DynamicTree(AbstractNode rootNode, TopComponent parentComponent) {
//        TreeData<RemoteObjectLight> treeData = new TreeData<>();
//        treeData.addItem(rootNode);
//        registerListeners();
//        this.parentComponent = parentComponent;
//    }
//    
//    public final void registerListeners(){
//        addExpandListener(this);
//        addCollapseListener(this);
//        
//        this.addItemClickListener((event) -> {
//            currentlySelectedNode = (AbstractNode)event.getItem();
//            if (parentComponent != null)
//                parentComponent.getEventBus().post(event);
//        
//        });
//    }
//    
//    /**
//     * Registers this component in the event bus.
//     */
//    public void register() {
//        if (parentComponent != null)
//            parentComponent.getEventBus().register(this);
//    }
//    
//    /**
//     * Unregisters this component from the event bus.
//     */
//    public void unregister() {
//        if (parentComponent != null)
//            parentComponent.getEventBus().unregister(this);
//    }
//    
//    @Subscribe
//    public void nodeChange(Property.ValueChangeEvent[] event) {
//        long oid = (Long) event[0].getProperty().getValue();
//        
//        String newValue = (String) event[1].getProperty().getValue();
//        
//        AbstractNode node = new AbstractNode(new RemoteObjectLight(oid, "", "")){
//
//            @Override
//            public void expand() {}
//
//            @Override
//            public AbstractAction[] getActions() {
//                return new AbstractAction[0];
//            }
//
//            @Override
//            public void refresh(boolean recursive) {}
//        };
//        
//        for (Object item : getItemIds()) {
//            if (item instanceof AbstractNode) {
//                if (node.equals((AbstractNode) item)) {
//                    
//                    this.setItemCaption(item, newValue);
//                    break;
//                }
//            }
//        }
//    }
//
//    @Override
//    public TopComponent getTopComponent() {
//        return parentComponent;
//    }
//
//    @Override
//    public void itemExpand(ExpandEvent<AbstractNode> event) {
//        ((AbstractNode)event.getExpandedItem()).expand();
//    }
//
//    @Override
//    public void itemCollapse(CollapseEvent<AbstractNode> event) {
//        ((AbstractNode)event.getCollapsedItem()).expand();
//    }
}