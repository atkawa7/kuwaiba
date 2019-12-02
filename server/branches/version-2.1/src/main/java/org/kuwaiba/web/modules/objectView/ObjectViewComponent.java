/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.objectView;

import com.vaadin.flow.component.button.Button;
import org.kuwaiba.web.modules.osp.*;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.apis.web.gui.views.AbstractView;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.KuwaibaConst;
import org.kuwaiba.web.MainLayout;
import org.openide.util.Exceptions;

/**
 * Main window of the Outside Plant module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Route(value = KuwaibaConst.PAGE_OBJECT_VIEW, layout = MainLayout.class)
@PageTitle(KuwaibaConst.TITLE_WELCOME)
public class ObjectViewComponent extends AbstractTopComponent {
    /**
     * The name of the view
     */
    public static String ROUTE_VALUE = "objectView";
    /**
     * Reference to the backend bean
     */
    @Inject
    private WebserviceBean wsBean;
    
    public ObjectViewComponent() {
        init();
    }
    
    public void init() {
        Button btnAddObjectView = new Button("Add ObjectView Example Database"); // (3)

            btnAddObjectView.addClickListener(click -> {
                addExample();
                    //
//                    RemoteObjectLight objgf = wsBean.getObjectLight("room", "f581203e-6beb-46ef-997a-0bb552ca6576","",idSession);
//            add(objectViewInstance.getAsComponent());

          }
  );
    add(btnAddObjectView);    
    }
    
    public void addExample(){
        AbstractView objectViewInstance;
        try {
            String idSession = "";
            
            idSession = VaadinSession.getCurrent().getAttribute(RemoteSession.class).getSessionId();
            PersistenceService.getInstance().getViewFactory().setWsBean(wsBean);
            objectViewInstance = PersistenceService.getInstance().getViewFactory().
                    createViewInstance("org.kuwaiba.web.modules.navtree.views.ObjectView");
          //ob 26d613a4-f51c-4a6d-92cd-46f94ffcbf98
            
            RemoteObjectLight objgf = wsBean.getObjectLight("Room", "f581203e-6beb-46ef-997a-0bb552ca6576","",idSession);
            objectViewInstance.buildWithBusinessObject(objgf);
            this.add(objectViewInstance.getAsComponent());
                  
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
   
    }
    
    @PostConstruct
    public void post(){
        add(new Label(">>> objectView post"));
    }
    
    @Override
    public void registerComponents() {
    }

    @Override
    public void unregisterComponents() {
    }

}
