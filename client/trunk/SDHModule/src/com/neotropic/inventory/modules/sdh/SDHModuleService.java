/*
 * Copyright (c) 2016 gir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    gir - initial API and implementation and/or initial documentation
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
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
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
     * Root of all transport links
     */
    public static String CLASS_GENERICSDHTRANSPORTLINK = "GenericSDHTransportLink";
    /**
     * Root of all transport links
     */
    public static String CLASS_GENERICSDHCONTAINERLINK = "GenericSDHContainerLink";
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
            long newViewId = com.createGeneralView(CLASS_VIEW, view.getName(), view.getDescription(), scene.getAsXML(), null);
            if (newViewId == -1) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            }
            else {
                view = new LocalObjectView(newViewId, CLASS_VIEW, view.getName(), view.getDescription(), scene.getAsXML(), null);
                SDHConfigurationObject configObject = Lookup.getDefault().lookup(SDHConfigurationObject.class);
                configObject.setProperty("saved", true);
                return true;
            }
        }
        else {
            if (com.updateGeneralView(view.getId(), view.getName(), view.getDescription(), scene.getAsXML(), null)) {
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
}
