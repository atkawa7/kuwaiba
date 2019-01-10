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
package com.neotropic.inventory.modules.sdh;

import com.neotropic.inventory.modules.sdh.scene.SDHModuleScene;
import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.util.Lookup;

/**
 * The service associated to this module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SDHModuleService {
    /**
     * Class to identify all views made using the SDH module
     */
    public static String CLASS_VIEW = "SDHModuleView";
    /**
     * Root of all equipment that can be connected using SDH links
     */
    public static String CLASS_GENERICEQUIPMENT = "GenericCommunicationsElement";
    /**
     * Root of all SDH (and over SDH) services
     */
    public static String CLASS_GENERICSDHSERVICE = "GenericSDHService";
    /**
     * Class representing a VC12
     */
    public static final String CLASS_VC12 = "VC12";
    /**
     * Class representing a VC3
     */
    public static final String CLASS_VC3 = "VC3";
    /**
     * Class representing a VC4
     */
    public static final String CLASS_VC4 = "VC4";
    /**
     * Root of all logical connections
     */
    public static String CLASS_GENERICLOGICALCONNECTION = "GenericLogicalConnection";
    /**
     * Root of all transport links
     */
    public static String CLASS_GENERICSDHTRANSPORTLINK = "GenericSDHTransportLink";
    /**
     * Root of all container links
     */
    public static String CLASS_GENERICSDHCONTAINERLINK = "GenericSDHContainerLink";
    /**
     * Root of all high order container links
     */
    public static String CLASS_GENERICSDHHIGHORDERCONTAINERLINK = "GenericSDHHighOrderContainerLink";
    /**
     * Root of all low order container links
     */
    public static String CLASS_GENERICSDHHIGHORDERTRIBUTARYLINK = "GenericSDHHighOrderTributaryLink";
    /**
     * Reference to the currently edited view. If its id is -1 or if it is null, the view is new and unsaved
     */
    private LocalObjectView view;
    /**
     * Reference to the scene to be displayed
     */
    private SDHModuleScene scene;
    /**
     * reference to the communications module
     */
    private CommunicationsStub com = CommunicationsStub.getInstance();

    public SDHModuleService(SDHModuleScene scene) {
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
            long newViewId = com.createGeneralView(CLASS_VIEW, view.getName(), view.getDescription(), scene.getAsXML(), scene.getBackgroundImage());
            if (newViewId == -1) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            }
            else {
                view = new LocalObjectView(newViewId, CLASS_VIEW, view.getName(), view.getDescription(), scene.getAsXML(), scene.getBackgroundImage());
                SDHConfigurationObject configObject = Lookup.getDefault().lookup(SDHConfigurationObject.class);
                configObject.setProperty("saved", true);
                return true;
            }
        }
        else {
            if (com.updateGeneralView(view.getId(), view.getName(), view.getDescription(), scene.getAsXML(), scene.getBackgroundImage())) {
                SDHConfigurationObject configObject = Lookup.getDefault().lookup(SDHConfigurationObject.class);
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
    
    /**
     * Calculates a link capacity based on the class name
     * @param connectionClass The class of the link to be evaluated
     * @param linkType 1 for transport links (whose prefix must always be STM) and 2 for container links (whose prefix must always be VC4)
     * @return The maximum number of timeslots in a container or transport link
     */
    public static int calculateCapacity(String connectionClass, LinkType linkType) {
        switch (linkType) {
            case TYPE_TRANSPORTLINK:
                String positionsToBeOccupied = connectionClass.replace("STM", "");
                return Integer.valueOf(positionsToBeOccupied);
            case TYPE_CONTAINERLINK:
                positionsToBeOccupied = connectionClass.replace("VC4", "");
                if (!positionsToBeOccupied.isEmpty())
                    return Math.abs(Integer.valueOf(positionsToBeOccupied));
                return 1;
            default: //Should not happen
                throw new IllegalArgumentException("Invalid link type");
        }
    }
    
    public enum LinkType {
        TYPE_TRANSPORTLINK,
        TYPE_CONTAINERLINK
    }
}
