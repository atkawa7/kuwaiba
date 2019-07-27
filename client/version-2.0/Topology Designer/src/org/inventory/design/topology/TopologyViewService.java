/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.design.topology;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.design.topology.scene.TopologyViewScene;
import org.openide.util.Lookup;

/**
 * Service class for the Topology Designer module
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class TopologyViewService {
    /**
     * Class to identify all view made using the Topology View module
     */
    public static String CLASS_VIEW = "TopologyModuleView";
    private LocalObjectView view;
    private final TopologyViewScene scene;
    private final CommunicationsStub com = CommunicationsStub.getInstance();
    
    public TopologyViewService(TopologyViewScene scene) {
        this.scene = scene;
    }

    public LocalObjectView getView() {
        return view;
    }

    public void setView(LocalObjectView view) {
        this.view = view;
    }
    
    public List<LocalObjectViewLight> getViews() {
        List<LocalObjectViewLight> views = com.getGeneralViews(CLASS_VIEW);
        if (views == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return Collections.EMPTY_LIST;
        }
        return views;
    }
    
    public LocalObjectView loadView(long viewId) {
        LocalObjectView theView = com.getGeneralView(viewId);
        if(theView == null)
            NotificationUtil.getInstance().showSimplePopup("Load view", NotificationUtil.ERROR_MESSAGE, com.getError());
        return theView;
    }
    
    public boolean saveNodesInCurrentView() {
        for (LocalObjectLight lol : scene.getNodes()) {
            if (!lol.getName().contains(TopologyViewScene.FREE_FRAME) && !lol.getName().contains(TopologyViewScene.CLOUD_ICON)) {
                HashMap<String, Object> attributesToUpdate = new HashMap<>();
                attributesToUpdate.put(Constants.PROPERTY_NAME, lol.getName());

                if(!CommunicationsStub.getInstance().updateObject(lol.getClassName(), lol.getId(), attributesToUpdate)) {
                    NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                    return false;
                }
            }
        }
        return true;
    }
    
    public boolean saveCurrentView() {
        if (view == null || view.getId() == -1) { // a new view
            long newViewId = com.createGeneralView(CLASS_VIEW, view.getName(), 
                    view.getDescription(), scene.getAsXML(), scene.getBackgroundImage());
            if (newViewId == -1) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            } 
            else {
                view = new LocalObjectView(newViewId, CLASS_VIEW, view.getName(), 
                        view.getDescription(), scene.getAsXML(), scene.getBackgroundImage());
                
                TopologyViewConfigurationObject configObject = Lookup.getDefault().lookup(TopologyViewConfigurationObject.class);
                
                boolean savedNodes = saveNodesInCurrentView();
                configObject.setProperty("saved", savedNodes);
                return savedNodes;
            }
        } 
        else {
            LocalObjectView theView = com.getGeneralView(view.getId());
            if (theView == null) {
                NotificationUtil.getInstance().showSimplePopup("Load view", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            }
            String name = view.getName() != null ? view.getName().equals(theView.getName()) ? null : view.getName() : null;
            String description = view.getDescription() != null ? view.getDescription().equals(theView.getDescription()) ? null : view.getDescription() : null;
                        
            if (com.updateGeneralView(
                view.getId(), 
                name, 
                description, 
                scene.getAsXML(), 
                scene.getBackgroundImage())) {
                TopologyViewConfigurationObject configObject = Lookup.getDefault().lookup(TopologyViewConfigurationObject.class);
                
                boolean savedNodes = saveNodesInCurrentView();
                configObject.setProperty("saved", savedNodes);
                return savedNodes;
            }
            else {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            }
        }
    }
       
    public boolean deleteView() {
        if (view != null) {
            if (com.deleteGeneralViews(new long [] {view.getId()})) {
                view = null;
                return true;
            }
            NotificationUtil.getInstance().showSimplePopup("Delete view", NotificationUtil.ERROR_MESSAGE, com.getError());
        }
        return false;
    }
}
