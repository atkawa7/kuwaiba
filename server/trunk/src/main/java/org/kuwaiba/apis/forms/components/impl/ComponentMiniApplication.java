/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.web.procmanager.AbstractComponentMiniApplication;
import org.openide.util.Exceptions;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentMiniApplication extends GraphicalComponent {
    private Window window;
    private final WebserviceBean webserviceBean;
    private AbstractComponentMiniApplication acma;
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
                
                if (object instanceof AbstractComponentMiniApplication) {
                    
                    acma = (AbstractComponentMiniApplication) object;
                    /*
                    acma.setWebserviceBean(webserviceBean);
                                        
                    Object content = null;
                    
//                    miniApp.fireOnLoad();
                    
                    if (Constants.Attribute.Mode.DETACHED.equals(miniApp.getMode()))
                        content = acma.launchDetached();
                    else if (Constants.Attribute.Mode.EMBEDDED.equals(miniApp.getMode()))
                        content = acma.launchEmbedded();
                    
                    if (content instanceof Component) 
                        getComponent().setContent((Component) content);
                    */
                }
                
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONCLICK.equals(event.getEventName())) {
            if (Constants.Function.OPEN.equals(event.getPropertyName())) {
                if (UI.getCurrent() != null) {
                    if (window == null)
                        window = new Window();
                                        
                    window.setModal(true);
                    window.setContent(getComponent());
                    window.center();

                    UI.getCurrent().addWindow(window);
                }
            } else if (Constants.Function.CLOSE.equals(event.getPropertyName())) {
                if (window != null)
                    window.close();
            } else if (Constants.Function.CLEAN.equals(event.getPropertyName())) {
                                                
            }
        }
        /*
        public void onElementEvent(EventDescriptor event) {
            if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {

                if (Constants.Property.ROWS.equals(event.getPropertyName())) {
                    ElementGrid grid = (ElementGrid) getComponentEventListener();
                    updateRows(grid);
                }
            }
        }
        */
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.INPUT_PARAMETERS.equals(event.getPropertyName())) {
                if (event.getNewValue() instanceof Properties) {
                    acma.setInputParameters((Properties) event.getNewValue());
                                        
                    acma.setWebserviceBean(webserviceBean);
                                        
                    Object content = null;
                                        
                    if (Constants.Attribute.Mode.DETACHED.equals(miniApp.getMode()))
                        content = acma.launchDetached();
                    else if (Constants.Attribute.Mode.EMBEDDED.equals(miniApp.getMode()))
                        content = acma.launchEmbedded();
                    
                    if (content instanceof Component) 
                        getComponent().setContent((Component) content);
                }
            }
        }
    }
        
}
