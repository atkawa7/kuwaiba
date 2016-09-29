/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.design.topology.scene.TopologyViewScene;
import org.openide.util.Lookup;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class TopologyViewService {
    /**
     * Class to identify all view made using the Topology View module
     */
    public static String CLASS_VIEW = "TopologyModuleView";
    private LocalObjectView view;
    private TopologyViewScene scene;
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
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
    
    public boolean saveNodesOfCurrentView() {
        for (LocalObjectLight lol : scene.getNodes()) {
            if (!lol.getName().contains(TopologyViewScene.FREE_FRAME) && !lol.getName().contains(TopologyViewScene.CLOUD_ICON)) {
                LocalObject update = new LocalObject(lol.getClassName(), lol.getOid(), new String[]{"name"}, new Object[]{lol.getName()});
                if (!CommunicationsStub.getInstance().saveObject(update)) {
                    NotificationUtil.getInstance().showSimplePopup("Error", 
                            NotificationUtil.ERROR_MESSAGE, 
                            CommunicationsStub.getInstance().getError());
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
                
                boolean savedNodes = saveNodesOfCurrentView();
                configObject.setProperty("saved", savedNodes);
                return savedNodes;
            }
        } 
        else {
            if (com.updateGeneralView(view.getId(), view.getName(), 
                    view.getDescription(), scene.getAsXML(), scene.getBackgroundImage())) {
                TopologyViewConfigurationObject configObject = Lookup.getDefault().lookup(TopologyViewConfigurationObject.class);
                
                boolean savedNodes = saveNodesOfCurrentView();
                configObject.setProperty("saved", savedNodes);
                return savedNodes;
            }
            else {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            }
        }
    }
    
    //private void update
    
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
