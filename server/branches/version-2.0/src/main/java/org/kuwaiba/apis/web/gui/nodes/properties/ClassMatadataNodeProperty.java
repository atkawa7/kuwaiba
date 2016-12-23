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

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.kuwaiba.apis.web.gui.nodes.ClassMetadataNode;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.ui.CustomComponent;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.web.custom.googlemap.overlays.NodeMarker;
import org.kuwaiba.web.properties.AbstractNodePorperty;
import org.kuwaiba.web.properties.PropertySheet;

/**
 * This class contains the method that listens when a node is selected in the 
 * class metadata tree 
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class ClassMatadataNodeProperty extends CustomComponent implements AbstractNodePorperty{
    private ClassMetadata classMetadata;
    private final EventBus eventBus;
    private PropertySheet sheet;
    
    public ClassMatadataNodeProperty(final EventBus eventBus) {
        this.eventBus = eventBus;
    }
      
    @Subscribe
    @Override
    public void nodeSelected(ItemClickEvent event) {
        createPropertySheet((ClassMetadataNode) event.getItemId());
    }

    @Override
    public void createPropertySheet(Object node) {
        //sheet = new PropertySheet(classMetadata, eventBus);
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void markerSelected(NodeMarker marker) {
        //createPropertySheet((ClassMetadataNode) marker.get);
    }
    
   
}
