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
    public static String VIEW_CLASS = "SDHModuleView";
    private LocalObjectView view;
    private SDHModuleScene scene;
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
        List<LocalObjectViewLight> views = com.getGeneralViews(VIEW_CLASS);
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
            long newViewId = com.createGeneralView(VIEW_CLASS, view.getName(), view.getDescription(), scene.getAsXML(), null);
            if (newViewId == -1) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
                return false;
            }
            else {
                view = new LocalObjectView(newViewId, VIEW_CLASS, view.getName(), view.getDescription(), scene.getAsXML(), null);
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
