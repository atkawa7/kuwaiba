/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.neotropic.kuwaiba.modules.commercial.mpls.actions;

import static com.neotropic.kuwaiba.modules.commercial.mpls.MPLSModule.CLASS_VIEW;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.AbstractAction;
import org.neotropic.kuwaiba.core.apis.integration.ActionResponse;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Creates a new View
 *  @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class NewMPLSViewAction extends AbstractAction {
    
     /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    @PostConstruct
    protected void init() {
        this.id = "mpls.new-view";
        this.displayName = ts.getTranslatedString("module.mpls.actions.new-view.name");
        this.description = ts.getTranslatedString("module.mpls.actions.new-view.name.description");
        this.order = 1000;
    
        setCallback((parameters) -> {
            
            try {
                String viewName = (String)parameters.get("viewName");                
                long newViewId = aem.createGeneralView(CLASS_VIEW, viewName, "", new byte[0], null);
                
                ActionResponse actionResponse = new ActionResponse();
                actionResponse.put("viewId", newViewId); // the id of the view created
                
                return actionResponse;
                        
            } catch (InvalidArgumentException ex) {
                Logger.getLogger(NewMPLSViewAction.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        });
    }
    
//    public boolean saveCurrentView() {
//         if (currentView == null || currentView.getId() == -1) { //New view
//            long newViewId = aem.createGeneralView(CLASS_VIEW, currentView.getName(), view.getDescription(), scene.getAsXML(), null);
//            if (newViewId == -1) {
//                return false;
//            }
//            else {
//                view = new LocalObjectView(newViewId, CLASS_VIEW, view.getName(), view.getDescription(), scene.getAsXML(), null);
//                MPLSConfigurationObject configObject = Lookup.getDefault().lookup(MPLSConfigurationObject.class);
//                configObject.setProperty("saved", true);            String className = (String)parameters.get("name");
//
//                return true;
//            }
//        }
//        else {
//            if (com.updateGeneralView(view.getId(), view.getName(), view.getDescription(), scene.getAsXML(), null)) {
//                MPLSConfigurationObject configObject = Lookup.getDefault().lookup(MPLSConfigurationObject.class);
//                configObject.setProperty("saved", true);
//                return true;
//            } else {
//                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
//                return false;
//            }
//        }
//    }

    @Override
    public int getRequiredAccessLevel() {
        return Privilege.ACCESS_LEVEL_READ_WRITE;
    }

    @Override
    public boolean requiresConfirmation() {
        return false;
    }
}