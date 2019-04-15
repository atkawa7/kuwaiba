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
package org.kuwaiba.web.custom.tree;

import com.google.common.eventbus.Subscribe;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseListener;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.AbstractNode;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * A tree that extends the features of the default one and makes use of the Nodes API
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class DynamicTree extends Tree implements Tree.ExpandListener, 
        CollapseListener, EmbeddableComponent 
{
    /**
     * Currently selected node
     */
    private AbstractNode currentlySelectedNode;
    /**
     * Nearest component that can provide access to the session variables
     */
    private final TopComponent parentComponent;
    
    /**
     * To make use of the global variables (and from there, to the backend logic), 
     * this component should be embedded into a TopComponent
     * @param rootNode The root node of the tree.
     * @param parentComponent The parent TopComponent component
     */
    public DynamicTree(AbstractNode rootNode, TopComponent parentComponent) {
        addItem(rootNode);
        registerListeners();
        this.setDragMode(Tree.TreeDragMode.NODE);
        this.parentComponent = parentComponent;
    }
    
    public final void registerListeners(){
        addExpandListener(this);
        addCollapseListener(this);
        
        this.addItemClickListener((ItemClickEvent event) -> {
            currentlySelectedNode = (AbstractNode)event.getItemId();
            if (parentComponent != null)
                parentComponent.getEventBus().post(event);
        });
    }
    
    /**
     * Registers this component in the event bus.
     */
    public void register() {
        if (parentComponent != null)
            parentComponent.getEventBus().register(this);
    }
    
    /**
     * Unregisters this component from the event bus.
     */
    public void unregister() {
        if (parentComponent != null)
            parentComponent.getEventBus().unregister(this);
    }
    
    @Override
    public void nodeExpand(ExpandEvent event) {
        ((AbstractNode)event.getItemId()).expand();
    }

    @Override
    public void nodeCollapse(CollapseEvent event) {
        ((AbstractNode)event.getItemId()).collapse();
    }
    
    @Subscribe
    public void nodeChange(Property.ValueChangeEvent[] event) {
        long oid = (Long) event[0].getProperty().getValue();
        
        String newValue = (String) event[1].getProperty().getValue();
        
        AbstractNode node = new AbstractNode(new RemoteObjectLight(oid, "", "")){

            @Override
            public void expand() {}

            @Override
            public AbstractAction[] getActions() {
                return new AbstractAction[0];
            }

            @Override
            public void refresh(boolean recursive) {}
        };
        
        for (Object item : getItemIds()) {
            if (item instanceof AbstractNode) {
                if (node.equals((AbstractNode) item)) {
                    
                    this.setItemCaption(item, newValue);
                    break;
                }
            }
        }
    }

    @Override
    public TopComponent getTopComponent() {
        return parentComponent;
    }
}