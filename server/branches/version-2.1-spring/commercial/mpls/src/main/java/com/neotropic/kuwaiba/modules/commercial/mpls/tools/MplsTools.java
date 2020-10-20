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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.shared.Registration;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Component with a set of tools available to work in an Mpls view
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class MplsTools extends HorizontalLayout {
    
    private TranslationService ts;
    private MplsSearch mplsSearch;
    private Button btnNewConnection;  
    private Button btnDetectConnections;  
    private Button btnSaveView;  
    private Button btnRemoveFromDatabase;  
    private Button btnRemoveFromView;  
     
    public MplsTools(BusinessEntityManager bem, TranslationService ts, List<BusinessObjectLight> addedNodes, List<BusinessObjectLight> addedLinks) {
        
        this.ts = ts;
      
        btnNewConnection = new Button(new Icon(VaadinIcon.CONNECT),
                e -> {
              fireEvent(new NewConnectionEvent(this, false));
        }); 
        setButtonTitle(btnNewConnection, ts.getTranslatedString("module.mpls.new-connection"));
       
        btnDetectConnections = new Button(new Icon(VaadinIcon.CLUSTER),
                e -> {
              fireEvent(new DetectConnectionsEvent(this, false));
        }); 
        setButtonTitle(btnDetectConnections, ts.getTranslatedString("module.mpls.detect-connections"));
        
        mplsSearch = new MplsSearch(ts, bem, addedNodes, addedLinks);
        mplsSearch.addNewObjectListener(evt -> {
            fireEvent(new NewObjectEvent(this, false, (BusinessObjectLight) evt.getObject()));
        });
     
        btnSaveView = new Button(new Icon(VaadinIcon.DOWNLOAD), evt -> {
            fireEvent(new SaveViewEvent(this, false));
        });
        btnSaveView.getElement().setProperty("title", ts.getTranslatedString("module.mpls.save-view"));
        setButtonTitle(btnSaveView, ts.getTranslatedString("module.mpls.save-view"));        
        
        btnRemoveFromDatabase = new Button( new Icon(VaadinIcon.TRASH),
                e -> {
                    fireEvent(new DeleteObjectPermanentlyEvent(this, false));
        });
        btnRemoveFromDatabase.setEnabled(false);
        setButtonTitle(btnRemoveFromDatabase, ts.getTranslatedString("module.mpls.delete-from-database-view"));        
              
        btnRemoveFromView = new Button(new Icon(VaadinIcon.FILE_REMOVE),
                e -> {
                    fireEvent(new DeleteObjectEvent(this, false));
        }); 
        btnRemoveFromView.setEnabled(false);
        setButtonTitle(btnRemoveFromView, ts.getTranslatedString("module.mpls.delete-from-view"));        
         
        this.setAlignItems(Alignment.BASELINE);
        add(btnSaveView, mplsSearch, btnNewConnection, btnDetectConnections, btnRemoveFromDatabase, btnRemoveFromView);
        
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
    
    /**
     * Function that enables/disables main functionality buttons 
     * @param enable true to enable the buttons, false otherwise
     */
    public void setGeneralToolsEnabled(boolean enable) {     
        btnNewConnection.setEnabled(enable);
        btnSaveView.setEnabled(enable);
        btnDetectConnections.setEnabled(enable);
        mplsSearch.setEnabled(enable);
    }
    
    /**
     * Function that enables/disables the buttons that depends of object selection events 
     * @param enable true to enable the buttons, false otherwise
     */
    public void setSelectionToolsEnabled(boolean enable) {
        btnRemoveFromDatabase.setEnabled(enable);
        btnRemoveFromView.setEnabled(enable);
    }
    
    /**
     * Set the title/tool tip for the given button
     * @param button the button to be set
     * @param title the title to be added
     */
    public static void setButtonTitle(Button button, String title) {
        button.getElement().setProperty("title", title);     
    }
    
}
