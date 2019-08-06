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

import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementMiniApplication;
import org.kuwaiba.apis.forms.elements.EventDescriptor;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.beans.WebserviceBean;
import org.openide.util.Exceptions;

/**
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentMiniApplication extends GraphicalComponent {
    private Window window;
    private final WebserviceBean webserviceBean;
    private AbstractMiniApplication ama;
    private ElementMiniApplication miniApp;

    public ComponentMiniApplication(WebserviceBean webserviceBean) {
        super(new Panel());
        this.webserviceBean = webserviceBean;
    }
    
    @Override
    public final Panel getComponent() {
        return (Panel) super.getComponent();
    }

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementMiniApplication) {
            try {
                miniApp = (ElementMiniApplication) element;
                
                String className = miniApp.getClassPackage() + "." + miniApp.getClassName();
                                                
                Class<?> aClass = Class.forName(className);
                Constructor<?> constructor = aClass.getConstructor(Properties.class);
                Object object = constructor.newInstance(new Object[] { new Properties() });
                
                if (object instanceof AbstractMiniApplication)
                    ama = (AbstractMiniApplication) object;
                                
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONCLICK.equals(event.getEventName())) {
            if (Constants.Function.OPEN.equals(event.getPropertyName())) {
                Object detached = ama.launchDetached();
                
                if (detached instanceof Window) {
                    UI.getCurrent().addWindow((Window) detached);
                }
                else if (detached instanceof Component && 
                         UI.getCurrent() != null) {
                    window = new Window();                                                            
                    window.setModal(true);
                    window.setContent((Component) detached);
                    window.center();
                    window.setSizeFull();

                    UI.getCurrent().addWindow(window);
                }
            } else if (Constants.Function.CLOSE.equals(event.getPropertyName())) {
                if (window != null)
                    window.close();
            } else if (Constants.Function.CLEAN.equals(event.getPropertyName())) {
                                                
            }
        }
        
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.INPUT_PARAMETERS.equals(event.getPropertyName())) {
                if (event.getNewValue() instanceof Properties) {
                    ama.setInputParameters((Properties) event.getNewValue());
                                        
                    ama.setWebserviceBean(webserviceBean);
                                        
                    Object content = null;                                        
////                    if (Constants.Attribute.Mode.DETACHED.equals(miniApp.getMode()))
////                        content = ama.launchDetached();
////                    else if (Constants.Attribute.Mode.EMBEDDED.equals(miniApp.getMode()))
                    if (Constants.Attribute.Mode.EMBEDDED.equals(miniApp.getMode()))
                        content = ama.launchEmbedded();
////                    if (content instanceof Window) {
////                        //UI.getCurrent().addWindow((Window) content);                        
////                    } else if (content instanceof Component) 
                    if (content instanceof Component) 
                        getComponent().setContent((Component) content);
                }
            }
        }
    }
        
}
