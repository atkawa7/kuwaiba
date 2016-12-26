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
import com.vaadin.event.ItemClickEvent;
import org.kuwaiba.web.custom.osp.google.overlays.NodeMarker;

/**
 * Abstract component for the property sheet of any node
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public interface AbstractNodeProperty {
    
    @Subscribe
    void nodeSelected(ItemClickEvent event);
    @Subscribe
    void markerSelected(NodeMarker event);
    void createPropertySheet(Object node);
}
