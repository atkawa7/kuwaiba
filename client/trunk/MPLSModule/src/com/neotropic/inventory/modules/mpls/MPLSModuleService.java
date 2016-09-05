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
package com.neotropic.inventory.modules.mpls;

import com.neotropic.inventory.modules.mpls.scene.MPLSModuleScene;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Lookup;

/**
 * The service associated to this module
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class MPLSModuleService {
    /**
     * Class to identify all views made using the MPLS module
     */
    public static String CLASS_VIEW = "MPLSModuleView";
    /**
     * Root of all equipment that can be connected using SDH links
     */
    public static String CLASS_GENERICEQUIPMENT = "GenericCommunicationsElement";
    /**
     * Root of all SDH (and over SDH) services
     */
    public static String CLASS_GENERICMPLSSERVICE = "GenericMPLSService";
    /**
     * Root of all logical connections
     */
    public static String CLASS_GENERICLOGICALCONNECTION = "GenericLogicalConnection";
    /**
     * Reference to the currently edited view. If its id is -1 or if it is null, the view is new and unsaved
     */
    private LocalObjectView view;
    /**
     * Reference to the scene to be displayed
     */
    private MPLSModuleScene scene;
    /**
     * reference to the communications module
     */
    private CommunicationsStub com = CommunicationsStub.getInstance();
    
    public MPLSModuleService(MPLSModuleScene scene) {
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
        if (theView == null)
            NotificationUtil.getInstance().showSimplePopup("Load view", NotificationUtil.ERROR_MESSAGE, com.getError());
        return theView;
    }
    public boolean saveCurrentView() {
         if (view == null || view.getId() == -1) { //New view
            long newViewId = com.createGeneralView(CLASS_VIEW, view.getName(), view.getDescription(), scene.getAsXML(), null);
            if (newViewId == -1) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            }
            else {
                view = new LocalObjectView(newViewId, CLASS_VIEW, view.getName(), view.getDescription(), scene.getAsXML(), null);
                MPLSConfigurationObject configObject = Lookup.getDefault().lookup(MPLSConfigurationObject.class);
                configObject.setProperty("saved", true);
                return true;
            }
        }
        else {
            if (com.updateGeneralView(view.getId(), view.getName(), view.getDescription(), scene.getAsXML(), null)) {
                MPLSConfigurationObject configObject = Lookup.getDefault().lookup(MPLSConfigurationObject.class);
                configObject.setProperty("saved", true);
                return true;
            } else {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            }
        }
    }
    
    public boolean deleteView() {
        if (com.deleteGeneralViews(new long[] {view.getId()})) {
            view = null;
            return true;
        }
        NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        return false;
    }
}
