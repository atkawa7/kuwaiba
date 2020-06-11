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
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
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
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Component with a set of tools available to work in an Mpls view
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class MplsTools extends HorizontalLayout {
    
    TranslationService ts;
    MenuBar menuAddConnection;  
    private ComboBox cbxObjects;
    private Button btnSaveView;  
    MenuBar menuDeleteObject;  
     
    public MplsTools(BusinessEntityManager bem, TranslationService ts, List<Object> object) {
        
        this.ts = ts;
        menuAddConnection = new MenuBar();
        menuAddConnection.setVisible(false);
        MenuItem addConnectionItem = menuAddConnection.addItem(new Label(ts.getTranslatedString("module.mpsl.add-connection")));
        addConnectionItem.addComponentAsFirst(new Icon(VaadinIcon.CONNECT));
        addConnectionItem.addComponentAtIndex(2, new Icon(VaadinIcon.ANGLE_DOWN));
        
        addConnectionItem.getSubMenu().addItem(ts.getTranslatedString("module.mpsl.new-connection"), 
                e -> {
              fireEvent(new NewConnectionEvent(this, false));
        });
        addConnectionItem.getSubMenu().addItem(ts.getTranslatedString("module.mpsl.existent-connection"),
                e -> {
              fireEvent(new AddExistingConnectionEvent(this, false));
        });  
        addConnectionItem.getSubMenu().addItem(ts.getTranslatedString("module.mpsl.detect-connections"),
                e -> {
              fireEvent(new DetectConnectionsEvent(this, false));
        }); 

        cbxObjects = new ComboBox(ts.getTranslatedString("module.mpsl.add_equipment"));
        cbxObjects.setWidth("300px");
        List<BusinessObjectLight> items = new ArrayList<>();
        try {
           items = bem.getObjectsOfClassLight(Constants.CLASS_GENERICCOMMUNICATIONSELEMENT, 50);
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(MplsTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        cbxObjects.setItems(items);
        cbxObjects.setAllowCustomValue(false);
        cbxObjects.addValueChangeListener( event -> {
             fireEvent(new NewObjectEvent(this, false, (BusinessObjectLight) event.getValue()));
        });
        
        btnSaveView = new Button(ts.getTranslatedString("module.mpsl.save-view"), new Icon(VaadinIcon.LEVEL_DOWN_BOLD), evt -> {
            fireEvent(new SaveViewEvent(this, false));
        });
        
        menuDeleteObject = new MenuBar();
        menuDeleteObject.setVisible(false);
        MenuItem deleteItem = menuDeleteObject.addItem(new Label(ts.getTranslatedString("module.mpsl.delete-object")));
        deleteItem.addComponentAsFirst(new Icon(VaadinIcon.TRASH));
        deleteItem.addComponentAtIndex(2, new Icon(VaadinIcon.ANGLE_DOWN));
        
        deleteItem.getSubMenu().addItem(ts.getTranslatedString("module.mpsl.delete-from-database-view"), 
                e -> {
                    fireEvent(new DeleteObjectPermanentlyEvent(this, false));
        });
        deleteItem.getSubMenu().addItem(ts.getTranslatedString("module.mpsl.delete-from-view"),
                e -> {
                    fireEvent(new DeleteObjectEvent(this, false));
        });      

        this.setAlignItems(Alignment.BASELINE);
        add(cbxObjects, menuAddConnection, menuDeleteObject, btnSaveView);
        
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
    
    public Registration addDeleteObjectListener(ComponentEventListener<DeleteObjectEvent> listener) {
        return addListener(DeleteObjectEvent.class, listener);
    }
    
    public Registration addDeleteObjectPermanentlyObjectListener(ComponentEventListener<DeleteObjectPermanentlyEvent> listener) {
        return addListener(DeleteObjectPermanentlyEvent.class, listener);
    }
    
    public Registration AddExistingConnectionListener(ComponentEventListener<AddExistingConnectionEvent> listener) {
        return addListener(AddExistingConnectionEvent.class, listener);
    }
    
    public Registration AddDetectConnectionsListener(ComponentEventListener<DetectConnectionsEvent> listener) {
        return addListener(DetectConnectionsEvent.class, listener);
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
    
    public class AddExistingConnectionEvent extends ComponentEvent<MplsTools> {
        public AddExistingConnectionEvent(MplsTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public class SaveViewEvent extends ComponentEvent<MplsTools> {
        public SaveViewEvent(MplsTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public class DeleteObjectEvent extends ComponentEvent<MplsTools> {
        public DeleteObjectEvent(MplsTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public class DeleteObjectPermanentlyEvent extends ComponentEvent<MplsTools> {
        public DeleteObjectPermanentlyEvent(MplsTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public class DetectConnectionsEvent extends ComponentEvent<MplsTools> {
        public DetectConnectionsEvent(MplsTools source, boolean fromClient) {
            super(source, fromClient);
        }
    }
    
    public void setToolsEnabled(boolean enable) {
        
        cbxObjects.setEnabled(enable);
        menuAddConnection.setVisible(enable);
        btnSaveView.setEnabled(enable);
        menuDeleteObject.setVisible(enable);
    }
    
}
