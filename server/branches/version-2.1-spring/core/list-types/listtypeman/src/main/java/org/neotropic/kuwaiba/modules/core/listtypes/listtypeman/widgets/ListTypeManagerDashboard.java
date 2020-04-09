/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.neotropic.kuwaiba.modules.core.listtypes.listtypeman.widgets;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.apis.integration.AbstractModuleDashboard;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * The dashboard used in the List Type Manager
 * @author Orlando Paz {@literal <charles.bedon@kuwaiba.org>}
 */
public class ListTypeManagerDashboard extends VerticalLayout implements AbstractModuleDashboard {

    /**
     * Reference to the translation service.
     */
    private TranslationService ts;
    /**
     * the main layout
     */
    private VerticalLayout lytContent;
    
    public ListTypeManagerDashboard(TranslationService ts) {
        this.ts = ts;
    }
    
     @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        this.lytContent = new VerticalLayout();
        
        
//        Button btnAddListTypeItem = new Button(this.newListTypeItemVisualAction.getModuleAction().getDisplayName(), (event) -> {
//            this.newListTypeItemVisualAction.getVisualComponent().open();
//        });
        
//        add(btnAddListTypeItem);
    }
    
    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
 
    
}
