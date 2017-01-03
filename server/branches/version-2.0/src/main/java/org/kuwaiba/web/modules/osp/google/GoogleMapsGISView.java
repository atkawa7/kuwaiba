/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.kuwaiba.web.modules.osp.google;

import com.vaadin.ui.CustomComponent;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.web.custom.core.AbstractTooledComponent;
import org.kuwaiba.web.modules.osp.AbstractGISView;

/**
 *
 * @author johnyortega
 */
public class GoogleMapsGISView extends CustomComponent implements AbstractGISView, EmbeddableComponent {
    
    private AbstractAction[] actions;
    private AbstractTooledComponent gisTooledComponent;
    private GoogleMapWrapper googleMapsWrapper;
    
    TopComponent parentComponent;
    
    public GoogleMapsGISView(TopComponent parentComponent) {
        this.parentComponent = parentComponent;
        
        initActions();
        
        gisTooledComponent = new AbstractTooledComponent(actions,
                AbstractTooledComponent.TOOLBAR_ORIENTATION_HORIZONTAL,
                AbstractTooledComponent.ToolBarSize.NORMAL) {};
        
        initGoogleMapWrapper();
        
        setCompositionRoot(gisTooledComponent);
        setSizeFull();
    }    
    
    private void initActions() {
        AbstractAction connect = new AbstractAction("Connect") {

            @Override
            public void actionPerformed(Object sourceComponent, Object targetObject) {
                getTopComponent().getEventBus().post(targetObject);
            }
        };
        actions =  new AbstractAction[]{connect};
    }
    
    private void initGoogleMapWrapper() {
        googleMapsWrapper = new GoogleMapWrapper(parentComponent);
        googleMapsWrapper.setSizeFull();
        gisTooledComponent.setMainComponent(googleMapsWrapper);
    }

    @Override
    public String getName() {
        return "OSP Module for Google Maps";
    }

    @Override
    public String getDescription() {
        return "OSP Module that uses Google Maps as map provider";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS";
    }
    
    public void register() {
        if (parentComponent != null)
            googleMapsWrapper.register();
    }
    
    public void unregister() {
        if (parentComponent != null)
            googleMapsWrapper.unregister();
    }

    @Override
    public TopComponent getTopComponent() {
        return parentComponent;
    }
    
}
