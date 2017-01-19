 /*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.osp.google;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.CustomComponent;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.web.custom.core.AbstractTooledComponent;
import org.kuwaiba.web.modules.osp.AbstractGISView;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
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
        AbstractAction connect = new AbstractAction("Connect", new ThemeResource("img/mod_icon_osp_connect.png")) {

            @Override
            public void actionPerformed(Object sourceComponent, Object targetObject) {
                getTopComponent().getEventBus().post(targetObject);
            }
        };
        
        AbstractAction drawPolygon = new AbstractAction("Draw polygon", null) {

            @Override
            public void actionPerformed(Object sourceComponent, Object targetObject) {
                getTopComponent().getEventBus().post(targetObject);
            }
        };
        actions =  new AbstractAction[]{connect, drawPolygon};
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
