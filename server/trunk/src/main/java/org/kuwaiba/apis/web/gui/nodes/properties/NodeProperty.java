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
package org.kuwaiba.apis.web.gui.nodes.properties;

import com.google.common.eventbus.Subscribe;
import com.vaadin.ui.CustomComponent;
import org.kuwaiba.apis.web.gui.nodes.AbstractNode;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.web.modules.osp.google.overlays.MarkerNode;

/**
 * This class contains the method that listens when a node is selected in the 
 * tree or a marker is selected in the map and creates a property sheet 
 * for the selected object. 
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class NodeProperty extends CustomComponent  {

  
    
//    protected Sheet sheet;
//    private final TopComponent parentComponent;
//    
//    public NodeProperty(TopComponent parentComponent) {
//        this.parentComponent = parentComponent;
//    }
//      
//    @Subscribe
//    public void nodeSelected(ItemClickEvent event) {
//        if(event.getItemId() instanceof InventoryObjectNode)
//            setCompositionRoot(((InventoryObjectNode)event.getItemId()).createPropertySheet());
//        else if(event.getItemId() instanceof ListTypeChildNode)  
//            setCompositionRoot(((AbstractNode)event.getItemId()).createPropertySheet());
//        else{
//            Sheet p = new Sheet(null, null);
//            setCompositionRoot(null);
//        }
//    }
//    
//    /** 
//     * A marker is selected in GIS View
//     * @param marker The selected marker
//     */
//    @Subscribe
//    public void markerSelected(MarkerNode marker) {
////        createPropertySheet(marker.getRemoteBusinessObject());
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
//        if (parentComponent!= null)
//            parentComponent.getEventBus().unregister(this);
//    }
//    
//    @Override
//    public TopComponent getTopComponent() {
//        return parentComponent;
//    }
    
    
}
