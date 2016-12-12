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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.neotropic.kuwaiba.web.nodes.ObjectNode;
import com.vaadin.data.Property;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.CollapseListener;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;

/**
 * Custom tree for kuwaiba
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class TreeView extends Tree implements Tree.ExpandListener, CollapseListener {

    private final EventBus eventBus;
    private ObjectNode node;
    
    public TreeView(RemoteBusinessObjectLight rootObject, String caption, final EventBus eventBus) {
        super(caption);
        addItem(new ObjectNode(rootObject, this));
        this.eventBus = eventBus;
        registerListeners();
    }    
    
    public final void registerListeners(){
        addExpandListener(this);
        addCollapseListener(this);
        
        this.addItemClickListener((ItemClickEvent event) -> {
            node = (ObjectNode)event.getItemId();
            eventBus.post(event);
        });
    }
    
    @Override
    public void nodeExpand(ExpandEvent event) {
        ((ObjectNode)event.getItemId()).expand();
    }

    @Override
    public void nodeCollapse(CollapseEvent event) {
        ((ObjectNode)event.getItemId()).collapse();
    }
    
    @Subscribe
    public void nodeChange(Property.ValueChangeEvent event) { 
        String newValue = (String) event.getProperty().getValue();
        //node.setDisplayName(newValue);
        this.setItemCaption(node, newValue);
    }
}
