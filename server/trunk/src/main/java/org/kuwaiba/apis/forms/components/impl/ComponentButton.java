/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.apis.forms.components.impl;

import com.vaadin.server.Page;
import org.kuwaiba.apis.forms.elements.EventDescriptor;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import java.util.Date;
import org.kuwaiba.apis.forms.FormInstanceCreator;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentButton extends GraphicalComponent {
    
    public ComponentButton() {
        super(new Button());
    }
    
    @Override
    public Button getComponent() {
        return (Button) super.getComponent();
    }
        
    @Override
    public void initFromElement(AbstractElement element) {
        
        if (element instanceof ElementButton) {
            
            ElementButton button = (ElementButton) element;
            
            getComponent().setCaption(button.getCaption());
            
            getComponent().addClickListener(new Button.ClickListener() {
                
                @Override
                public void buttonClick(Button.ClickEvent event) {
                    fireComponentEvent(new EventDescriptor(Constants.EventAttribute.ONCLICK));
                }
            });
        }
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        
        if (Constants.EventAttribute.ONCLICK.equals(event.getEventName())) {
            
////            if (Constants.Function.OPEN_FORM.equals(event.getPropertyName())) {
////                File file = new File(Variable.FORM_RESOURCE_STRUCTURES + "/" + event.getNewValue() + ".xml");
////                FormDisplayer.getInstance().display(file, false);
////            }
            if (Constants.Function.SAVE.equals(event.getPropertyName())) {
                
                try {
                    WebserviceBean wsBean = (WebserviceBean) getComponent().getUI().getSession().getAttribute("wsBean");
                    RemoteSession session = (RemoteSession) getComponent().getUI().getSession().getAttribute("session");
                                        
                    byte [] structure = new FormInstanceCreator(((ElementButton) getComponentEventListener()).getFormStructure(), wsBean, session).getStructure();
                    
                    String address = Page.getCurrent().getWebBrowser().getAddress();

                    wsBean.createFormInstance(-1, String.valueOf(new Date().getTime()), String.valueOf(new Date().getTime()), structure, address, session.getSessionId());
                                                            
                } catch (ServerSideException ex) {
                    
                    Notification.show("Error", ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }
            }
        }
    }
    
}
