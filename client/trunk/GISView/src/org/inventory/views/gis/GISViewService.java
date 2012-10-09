/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.gis;

import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.visual.LocalEdge;
import org.inventory.core.services.api.visual.LocalNode;
import org.inventory.core.services.api.visual.LocalObjectView;
import org.inventory.views.gis.scene.GISViewScene;
import org.inventory.views.gis.scene.GeoPositionedNodeWidget;
import org.openide.util.Lookup;

/**
 * Logic associated to the corresponding TopComponent
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class GISViewService {

    private GISViewScene scene;
    private LocalObjectView currentView;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    private NotificationUtil nu = Lookup.getDefault().lookup(NotificationUtil.class);
    private GISViewTopComponent gvtc;

    public GISViewService(GISViewScene scene, GISViewTopComponent gvtc) {
        this.scene = scene;
        this.gvtc = gvtc;
    }

    /**
     * Updates the current view
     * @param viewId
     */
    public void updateCurrentView(long viewId) {
        this.currentView = com.getGeneralView(viewId);
        if (this.currentView == null)
            nu.showSimplePopup("Loading view", NotificationUtil.ERROR, com.getError());
         buildView();
    }

    public void buildView(){
        if (currentView == null)
            return;

        for (LocalNode node : currentView.getNodes()){
            GeoPositionedNodeWidget widget = (GeoPositionedNodeWidget)scene.addNode(node.getObject());
            widget.setCoordinates(node.getY(), node.getX());
        }

        for (LocalEdge node : currentView.getEdges()){
            GeoPositionedNodeWidget widget = (GeoPositionedNodeWidget)scene.addNode(node.getObject());
            //widget.setCoordinates(node.getY(), node.getX());
        }
        gvtc.toggleButtons(true);
    }
}
