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
package org.kuwaiba.web.modules.servmanager;

import com.neotropic.kuwaiba.modules.reporting.img.SceneExporter;
import com.neotropic.kuwaiba.modules.reporting.img.rackview.RackViewImage;
import com.neotropic.kuwaiba.modules.reporting.img.rackview.RackViewScene;
import com.neotropic.kuwaiba.modules.reporting.img.rackview.RackViewService;
import org.kuwaiba.web.modules.servmanager.actions.AddServiceWindow;
import org.kuwaiba.web.modules.servmanager.actions.AddCustomerWindow;
import com.vaadin.navigator.View;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.UI;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.web.gui.events.OperationResultListener;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.openide.util.Exceptions;

/**
 * A module to manage customer, services and resources associated to those services
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ServiceManagerModule extends AbstractModule {
    
    private ServiceManagerComponent servManagementComponent;

    public ServiceManagerModule(WebserviceBean wsBean, RemoteSession session) {
        super(wsBean, session);
    }

    @Override
    public String getName() {
        return "Service Manager";
    }

    @Override
    public String getDescription() {
        return "Manage customer, services and resources associated to those services";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }

    @Override
    public int getType() {
        return MODULE_TYPE_FREE_CORE;
    }

    @Override
    public void attachToMenu(MenuBar menuBar) {
        MenuBar.MenuItem serviceManagerMenuItem = menuBar.addItem("Services", null);
        serviceManagerMenuItem.setDescription(getDescription());
        
        serviceManagerMenuItem.addItem(getName(), new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                UI.getCurrent().getNavigator().navigateTo(ServiceManagerComponent.VIEW_NAME);
            }
        });
        
        //Apart from the full-fledged Service Manager, we add actions to create services and customers
        serviceManagerMenuItem.addItem("Add Customer", new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                AddCustomerWindow wdwNewCustomer = new AddCustomerWindow(wsBean, new OperationResultListener() {
                    @Override
                    public void doIt() {
                        Notifications.showInfo("Customer added successfully");
                    }
                });
                UI.getCurrent().addWindow(wdwNewCustomer);
            }
        });
        
        serviceManagerMenuItem.addItem("Add Service", new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                AddServiceWindow wdwNewService = new AddServiceWindow(wsBean, new OperationResultListener() {
                    @Override
                    public void doIt() {
                        Notifications.showInfo("Service added successfully");
                    }
                });
                UI.getCurrent().addWindow(wdwNewService);
            }
        });
        
        serviceManagerMenuItem.addItem("Update Rack Views", new MenuBar.Command() {
            @Override
            public void menuSelected(MenuBar.MenuItem selectedItem) {
                try {
                    List<RemoteObjectLight> racks = wsBean.getObjectsOfClassLight("Rack", -1, session.getIpAddress(), session.getSessionId()); //NOI18N
                    String oldPath = SceneExporter.PATH;                    
                    String newPath = (String) new InitialContext().lookup("java:comp/env/attachmentsPath"); //NOI18N
                    SceneExporter.PATH = newPath;
                    for (RemoteObjectLight rack : racks) {
                        RackViewImage rackViewImage = RackViewImage.getInstance();
                        rackViewImage.setIpAddress(session.getIpAddress());
                        rackViewImage.setRemoteSession(session);
                        rackViewImage.setWebserviceBean(wsBean);

                        try {
                            RemoteObject rackObject = wsBean.getObject(rack.getClassName(), rack.getId(), session.getIpAddress(), session.getSessionId());

                            RackViewScene rackViewScene = new RackViewScene(RackViewImage.getInstance().getDevices(rackObject));
                            rackViewScene.setShowConnections(true);

                            RackViewService service = new RackViewService(rackViewScene, rackObject);

                            service.shownRack();

                            try {
                                org.netbeans.api.visual.export.SceneExporter.createImage(rackViewScene,
                                        new File(SceneExporter.PATH + "/tmpRackView_" + rack.getId() + ".png"),
                                        org.netbeans.api.visual.export.SceneExporter.ImageType.PNG,
                                        org.netbeans.api.visual.export.SceneExporter.ZoomType.ACTUAL_SIZE,
                                        false, false, 100,
                                        0,  //Not used
                                        0); //Not used
                            } catch (IOException ex) {
                                Notifications.showError(ex.getMessage());
                            }

                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getMessage());
                        }
                    }
                    SceneExporter.PATH = oldPath;
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                } catch (NamingException ex) {
                    Exceptions.printStackTrace(ex);
                }
                Notifications.showInfo("Rack Views Updated");                
            }
        });
    }

    @Override
    public View open() {
        servManagementComponent = new ServiceManagerComponent();
        //Register components in the event bus
        servManagementComponent.registerComponents();
        return servManagementComponent;
    }

    @Override
    public void close() {
        servManagementComponent.unregisterComponents();
    }
    
}
