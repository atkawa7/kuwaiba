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
package com.neotropic.kuwaiba.modules.commercial.mpls.tools;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;

/**
 * Component with a set of tools available to work in an Mpls view
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class MplsTools extends HorizontalLayout {
    
    private Button btnNewConnection;
    private ComboBox cbxObjects;
    private Button btnSaveView;  
     
    public MplsTools(BusinessEntityManager bem, List<Object> object) {
        
        btnNewConnection = new Button("New Connection",new Icon(VaadinIcon.CONNECT), event -> {
            fireEvent(new NewConnectionEvent(this, false));
        });

        cbxObjects = new ComboBox("Add Object");
        cbxObjects.setWidth("300px");
        List<BusinessObjectLight> items = new ArrayList<>();
        try {
           items = bem.getObjectsOfClassLight("GenericCommunicationsElement", 50);
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(MplsTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        cbxObjects.setItems(items);
        cbxObjects.setAllowCustomValue(false);
        cbxObjects.addValueChangeListener( event -> {
             fireEvent(new NewObjectEvent(this, false, (BusinessObjectLight) event.getValue()));
        });
        
        btnSaveView = new Button("Save View", new Icon(VaadinIcon.LEVEL_DOWN_BOLD), evt -> {
            fireEvent(new SaveViewEvent(this, false));
        });

        add(btnNewConnection, cbxObjects, btnSaveView);
    }
    
    public Registration addNewObjectListener(ComponentEventListener<NewObjectEvent> listener) {
        return addListener(NewObjectEvent.class, listener);
    }
    
    public Registration addNewConnectionListener(ComponentEventListener<NewConnectionEvent> listener) {
        return addListener(NewConnectionEvent.class, listener);
    }
    
    public Registration addSaveViewListener(ComponentEventListener<SaveViewEvent> listener) {
        return addListener(SaveViewEvent.class, listener);
    }
    
    public class NewObjectEvent extends ComponentEvent<MplsTools> {
        private final BusinessObjectLight object;
        public NewObjectEvent(MplsTools source, boolean fromClient, BusinessObjectLight object) {
            super(source, fromClient);
            this.object = object;
        }
        public BusinessObjectLight getObject() {
            return object;
        }
    }
    
    public class NewConnectionEvent extends ComponentEvent<MplsTools> {
        public NewConnectionEvent(MplsTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public class SaveViewEvent extends ComponentEvent<MplsTools> {
        public SaveViewEvent(MplsTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public void setToolsEnabled(boolean enable) {
        btnNewConnection.setEnabled(enable);
        cbxObjects.setEnabled(enable);
        btnSaveView.setEnabled(enable);
    }
    
}
