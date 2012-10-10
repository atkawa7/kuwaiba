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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.LocalStuffFactory;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.api.visual.LocalEdge;
import org.inventory.core.services.api.visual.LocalNode;
import org.inventory.core.services.api.visual.LocalObjectView;
import org.inventory.core.services.api.visual.LocalObjectViewLight;
import org.inventory.views.gis.scene.GISViewScene;
import org.inventory.views.gis.scene.GeoPositionedConnectionWidget;
import org.inventory.views.gis.scene.GeoPositionedNodeWidget;
import org.inventory.views.gis.scene.MapPanel;
import org.netbeans.api.visual.anchor.AnchorFactory;
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

    public LocalObjectView getCurrentView(){
        return currentView;
    }

    void setCurrentView(Object object) {
        currentView = null;
    }

    /**
     * Updates the current view
     * @param viewId
     */
    public void loadView(long viewId) {
        this.currentView = com.getGeneralView(viewId);
        if (this.currentView == null)
            nu.showSimplePopup("Loading view", NotificationUtil.ERROR, com.getError());
         buildView();
    }

    private void buildView(){
        if (currentView == null)
            return;

        if (currentView.getCenter() != null)
            scene.setCenterPosition(currentView.getCenter()[1], currentView.getCenter()[0]);

        if (currentView.getZoom() != 0)
            scene.setZoom(currentView.getZoom());

        for (LocalNode node : currentView.getNodes()){
            GeoPositionedNodeWidget widget = (GeoPositionedNodeWidget)scene.addNode(node.getObject());
            widget.setCoordinates(node.getY(), node.getX());
        }

        for (LocalEdge edge : currentView.getEdges()){
            GeoPositionedConnectionWidget newEdge = (GeoPositionedConnectionWidget)scene.addEdge(edge.getObject());
            newEdge.setSourceAnchor(AnchorFactory.createCircularAnchor(scene.findWidget(edge.getaSide().getObject()), 3));
            newEdge.setTargetAnchor(AnchorFactory.createCircularAnchor(scene.findWidget(edge.getbSide().getObject()), 3));

            List<Point> localControlPoints = new ArrayList<Point>();
            for (double[] controlPoint : edge.getControlPoints())
                localControlPoints.add(scene.coordinateToPixel(controlPoint[1], controlPoint[0], currentView.getZoom() != 0 ? currentView.getZoom() : MapPanel.DEFAULT_ZOOM_LEVEL ));

            newEdge.setControlPoints(localControlPoints);
        }
        gvtc.toggleButtons(true);
    }

    void saveView(String nameInTxt, String descriptionInTxt) {
        if (currentView == null){
            long viewId = com.createGeneralView(LocalObjectViewLight.TYPE_GIS, nameInTxt, descriptionInTxt, scene.getAsXML(), null);
            if (viewId != -1){
                //currentView = LocalStuffFactory.createLocalObjectViewLight(viewId, nameInTxt, descriptionInTxt, LocalObjectViewLight.TYPE_GIS);
                nu.showSimplePopup("New View", NotificationUtil.INFO, "View created successfully");
            }else
                nu.showSimplePopup("New View", NotificationUtil.ERROR, com.getError());
        }
        else{
            if (com.updateGeneralView(currentView.getId(), nameInTxt, descriptionInTxt, scene.getAsXML(), null))
                nu.showSimplePopup("Save View", NotificationUtil.INFO, "View created successfully");
            else
                nu.showSimplePopup("Save View", NotificationUtil.ERROR, com.getError());
        }
    }
}
