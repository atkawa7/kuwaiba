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
package org.kuwaiba.web.modules.osp.google.actions;

import com.vaadin.server.ThemeResource;
import com.vaadin.tapio.googlemaps.GoogleMap;
import com.vaadin.tapio.googlemaps.client.overlays.GoogleMapPolygon;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.web.modules.osp.google.CustomGoogleMap;

/**
 * Action to delete a polygon overlay
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeletePolygonAction extends AbstractAction {
    
    public DeletePolygonAction() {
        super("Delete Polygon", new ThemeResource("img/warning.gif"));
    }
    
    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        ((GoogleMap) sourceComponent).removePolygonOverlay((GoogleMapPolygon) targetObject);
        ((CustomGoogleMap) sourceComponent).setUpdateView(true);
    }
}
