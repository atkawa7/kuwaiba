/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.routing.bgp;

import com.neotropic.inventory.modules.routing.bgp.scene.BGPModuleScene;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalLogicalConnectionDetails;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Lookup;

/**
 * The service associated to this module
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class BGPModuleService {
    /**
     * Class to identify all views made using the SDH module
     */
    public static String CLASS_VIEW = "BGPModuleView";
    /**
     * Root of all equipment that can be connected using SDH links
     */
    public static String CLASS_GENERICEQUIPMENT = "GenericCommunicationsElement";
    /**
     * Root of all low order container links
     */
    public static String CLASS_BGPLINK = "BGPLink";
    /**
     * Reference to the currently edited view. If its id is -1 or if it is null, the view is new and unsaved
     */
    private LocalObjectView view;
    /**
     * Reference to the scene to be displayed
     */
    private final BGPModuleScene scene;
    /**
     * reference to the communications module
     */
    private final CommunicationsStub com = CommunicationsStub.getInstance();

    public BGPModuleService(BGPModuleScene scene) {
        this.scene = scene;
    }
   
    public void setView(LocalObjectView view) {
        this.view = view;
    }
    
    public LocalObjectViewLight getView() {
        List<LocalObjectViewLight> views = com.getGeneralViews(CLASS_VIEW);
        if (views == null) {
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
            return null;
        }
        
        return views.get(0);
    }
    
    public void loadView(){
        List<LocalObjectViewLight> views = com.getGeneralViews(CLASS_VIEW);
        if (views == null)
            NotificationUtil.getInstance().showSimplePopup("Loading BGP view", NotificationUtil.ERROR_MESSAGE, com.getError());
        else if(views.isEmpty())
           reloadBGPView();
        else{
            view = com.getGeneralView(views.get(0).getId());
            scene.render(view.getStructure());
        }
    }
    
    public void reloadBGPView(){
        List<LocalLogicalConnectionDetails> bgpMap = com.getBGPMap(new ArrayList<>());
        scene.createBGPView(bgpMap);
    }
        
    public boolean saveCurrentView() {
        if (view == null || view.getId() == -1) {//New view
            long newViewId = com.createGeneralView(CLASS_VIEW, "BGPMap", "BGPMap Generated Automatically", scene.getAsXML(), scene.getBackgroundImage());
            if (newViewId == -1) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            }
            else {
                view = new LocalObjectView(newViewId, CLASS_VIEW, "BGPMap", "BGPMap Generated Automatically", scene.getAsXML(), scene.getBackgroundImage());
                BGPConfigurationObject configObject = Lookup.getDefault().lookup(BGPConfigurationObject.class);
                configObject.setProperty("saved", true);
                return true;
            }
        }
        else {
            if (com.updateGeneralView(view.getId(), view.getName(), view.getDescription(), scene.getAsXML(), scene.getBackgroundImage())) {
                BGPConfigurationObject configObject = Lookup.getDefault().lookup(BGPConfigurationObject.class);
                configObject.setProperty("saved", true);
                return true;
            } else {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            }
        }
    }
}
