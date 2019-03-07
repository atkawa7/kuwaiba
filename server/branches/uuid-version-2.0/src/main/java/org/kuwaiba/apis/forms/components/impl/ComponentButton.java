/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

import com.vaadin.icons.VaadinIcons;
import org.kuwaiba.apis.forms.elements.EventDescriptor;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementButton;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Date;
import org.kuwaiba.apis.forms.FormInstanceCreator;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
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
            getComponent().setEnabled(button.isEnabled());
            
            if (button.getStyleName() != null) {
                
                String[] styleNames = button.getStyleName().split(";");
                
                for (int i = 0; i < styleNames.length; i += 1) {
                    
                    switch(styleNames[i]) {

                        case Constants.Attribute.StyleName.BUTTON_CLOSE_ICON:
                            getComponent().setIcon(VaadinIcons.CLOSE);
                        break;
                        case Constants.Attribute.StyleName.BUTTON_PENCIL_ICON:
                            getComponent().setIcon(VaadinIcons.PENCIL);
                        break;
                        case Constants.Attribute.StyleName.BUTTON_PLUS_ICON:
                            getComponent().setIcon(VaadinIcons.PLUS);
                        break;
                        case Constants.Attribute.StyleName.BUTTON_COGS_ICON:
                            getComponent().setIcon(VaadinIcons.COGS);
                        break;
                        case Constants.Attribute.StyleName.BUTTON_PLUS_ICON_ONLY:
                            getComponent().setIcon(VaadinIcons.PLUS);
                            getComponent().addStyleName(ValoTheme.BUTTON_ICON_ONLY);
                        break;
                        case Constants.Attribute.StyleName.BUTTON_PRIMARY:
                            //getComponent().addStyleName(ValoTheme.BUTTON_PRIMARY);
                        break;
                        case Constants.Attribute.StyleName.BUTTON_DANGER:
                            //getComponent().addStyleName(ValoTheme.BUTTON_DANGER);
                        break;
                    }
                }
            }
            
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
                    
                    wsBean.createFormInstance(-1, String.valueOf(new Date().getTime()), String.valueOf(new Date().getTime()), structure, session.getIpAddress(), session.getSessionId());                                        
                } catch (ServerSideException ex) {
                    
                    Notification.show("Error", ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }
            }
        }
        else if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
                        
            if (Constants.Property.HIDDEN.equals(event.getPropertyName())) {
                getComponent().setVisible(!((ElementButton) getComponentEventListener()).isHidden());
            } 
            else if (Constants.Property.ENABLED.equals(event.getPropertyName())) {
                getComponent().setEnabled((boolean) event.getNewValue());
            }
        }
    }
    
}
