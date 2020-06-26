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

package com.neotropic.kuwaiba.modules.commercial.mpls;

import com.neotropic.kuwaiba.modules.commercial.mpls.actions.DeleteMplsViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.mpls.actions.NewMplsViewVisualAction;
import com.neotropic.kuwaiba.modules.commercial.mpls.persistence.MplsService;
import com.neotropic.kuwaiba.modules.commercial.mpls.widgets.MplsDashboard;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the MPLS module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Orlando Paz {@literal <Orlando.Paz@kuwaiba.org>}
 */
@Route(value = "mpls", layout = MplsLayout.class)
public class MplsManagerUI extends VerticalLayout {

    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * Reference to the Business Entity Manager.
     */
    @Autowired
    private BusinessEntityManager bem;
    
    @Autowired
    private MplsService mplsService;
    
    @Autowired
    private DeleteMplsViewVisualAction deleteMPLSViewVisualAction;
    
    @Autowired
    private NewMplsViewVisualAction newMPLSViewVisualAction;
    
    @Autowired
    private ResourceFactory resourceFactory;
    
    private MplsDashboard dashboard; 
   
    public MplsManagerUI() {
        super();
        setSizeFull();
    }
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        getUI().ifPresent( ui -> ui.getPage().setTitle(ts.getTranslatedString("module.listtypeman.title")));      

        try {
            createContent();
        } catch (InvalidArgumentException | MetadataObjectNotFoundException ex) {
            
        }
        
    }  
    
    public void showActionCompledMessages(ActionCompletedListener.ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {                
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();                                          
            } catch (Exception ex) {
                Logger.getLogger(MplsManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }

    private void createContent() throws InvalidArgumentException, MetadataObjectNotFoundException {                      
        dashboard = new MplsDashboard(ts, mem, aem, bem, resourceFactory, mplsService, deleteMPLSViewVisualAction, newMPLSViewVisualAction);
        dashboard.setSizeFull();
               
        add(dashboard);
    }

}
