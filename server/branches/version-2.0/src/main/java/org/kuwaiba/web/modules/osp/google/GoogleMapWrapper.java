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

import com.google.common.eventbus.Subscribe;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.ui.Button;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.Window;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.apis.web.gui.modules.EmbeddableComponent;
import org.kuwaiba.apis.web.gui.modules.TopComponent;
import org.kuwaiba.apis.web.gui.nodes.InventoryObjectNode;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.ViewInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.ViewInfoLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.web.modules.osp.windows.CleanViewWindow;
import org.kuwaiba.apis.web.gui.windows.ConfirmDialogWindow;
import org.kuwaiba.web.modules.osp.windows.DeleteWindow;
import org.kuwaiba.web.modules.osp.windows.OpenViewWindow;
import org.kuwaiba.web.modules.osp.windows.SaveTopologyWindow;
import org.kuwaiba.web.modules.osp.windows.SaveViewDialog;

/**
 * This wrapper contain the GoogleMap. Give the power of drag and drop
 * elements to the map
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class GoogleMapWrapper extends DragAndDropWrapper implements EmbeddableComponent, Window.CloseListener {
    public static String CLASS_VIEW = "OutsidePlantModuleView";
    private ViewInfo view;
    private CustomGoogleMap map;
    private TopComponent parentComponent;
    private boolean windowCallSaveView = false;
    
    public GoogleMapWrapper(TopComponent parentComponent) {
        this.parentComponent = parentComponent;
        
        try {
            Context context = new InitialContext();
            String apiKey = (String)context.lookup("java:comp/env/googleMapsApiKey"); //NOI18N
            String mapLanguage = (String)context.lookup("java:comp/env/mapLanguage"); //NOI18N
            double longitude = (double)context.lookup("java:comp/env/defaultCenterLongitude"); //NOI18N
            double latitude = (double)context.lookup("java:comp/env/defaultCenterLatitude"); //NOI18N
            int zoom = (int)context.lookup("java:comp/env/defaultZoom"); //NOI18N
            
            map = new CustomGoogleMap(parentComponent, apiKey, null, mapLanguage);
            map.setCenter(new LatLon(latitude, longitude));
            map.setZoom(zoom);
            map.setSizeFull();
            
            setCompositionRoot(map);
            setDropHandler(new DropHandlerImpl());
        } catch (NamingException ex){
            Notification.show("An error occurred while loading the map default settings. Please contact your administrator", 
                    Notification.Type.ERROR_MESSAGE);
        }
    }
    
    public void register() {
        if (parentComponent != null) {
            parentComponent.getEventBus().register(this);
            map.register();
        }
    }
    
    public void unregister() {
        if (parentComponent != null) {
            parentComponent.getEventBus().unregister(this);
            map.unregister();
        }
    }
    
    @Override
    public TopComponent getTopComponent() {
        return parentComponent;
    }
    
    @Subscribe
    public void enableTool(Button.ClickEvent event) {
        if ("Connect".equals(event.getButton().getDescription())) {
            if (map.getMarkers().size() >= 2)
                map.enableConnectionTool(true);
            else
                Notification.show("There are not nodes to connect", Type.WARNING_MESSAGE);
        }
        if ("Draw polygon".equals(event.getButton().getDescription())) {
            map.enablePolygonTool(true);
        }
        if ("Clean".equals(event.getButton().getDescription())) {
            if (view != null) {
                CleanViewWindow window = new CleanViewWindow(this);
                getUI().addWindow(window);
            }
            else {
                map.clear();
                Notification.show("The view was cleaned", Type.TRAY_NOTIFICATION);
            }
        }
        if ("Open".equals(event.getButton().getDescription())) {
            try {
                WebserviceBeanLocal wsBean = getTopComponent().getWsBean();
                String ipAddress = getUI().getPage().getWebBrowser().getAddress();
                String sessioId = getTopComponent().getApplicationSession().getSessionId();
                
                ViewInfoLight [] views = wsBean.getGeneralViews(CLASS_VIEW, -1, ipAddress, sessioId);
                if (views.length > 0) {
                    if (view != null || !map.isEmpty()) {
                        SaveViewDialog window = new SaveViewDialog(this);                
                        getUI().addWindow(window);
                    }
                    else {
                        OpenViewWindow window = new OpenViewWindow(views);
                        window.addCloseListener(this);
                        getUI().addWindow(window);
                    }
                }
                else
                    Notification.show("There are not views", Type.WARNING_MESSAGE);                                
            } catch (ServerSideException ex) {
                Notification.show(ex.getMessage(), Type.ERROR_MESSAGE);
            }
            return;
        }
        if ("Save".equals(event.getButton().getDescription())) {
            if (map.isEmpty())
                Notification.show("The view is empty, it won't be saved", 
                        Type.WARNING_MESSAGE);
            else {
                saveView();
            }
        }
        if ("New".equals(event.getButton().getDescription())) {
            if (view != null || !map.isEmpty()) {
                SaveViewDialog window = new SaveViewDialog(this);                
                getUI().addWindow(window);
            }
        }
        if ("Delete".equals(event.getButton().getDescription())) {
            if (view != null) {
                DeleteWindow window = new DeleteWindow(this);
                getUI().addWindow(window);
            }
            else
                Notification.show("There are not view to delete", Type.WARNING_MESSAGE);
        }
    }

    @Override
    public void windowClose(Window.CloseEvent e) {
        try {
            WebserviceBeanLocal wsBean = getTopComponent().getWsBean();
            String ipAddress = getUI().getPage().getWebBrowser().getAddress();
            String sessioId = getTopComponent().getApplicationSession().getSessionId();
            
            if (e.getWindow() instanceof OpenViewWindow) {
                OpenViewWindow window = (OpenViewWindow) e.getWindow();

                if (window.isOk()) {
                    if (window.getView() != null) {
                        ViewInfoLight selectedView = window.getView();
                        view = wsBean.getGeneralView(selectedView.getId(), ipAddress, sessioId);
                        map.render(view.getStructure());
                    }
                }
                window.removeCloseListener(this);
                getUI().removeWindow(window);
                return;
            }
            if (e.getWindow() instanceof SaveTopologyWindow) {
                SaveTopologyWindow window = (SaveTopologyWindow) e.getWindow();
                getUI().addWindow(window);
                if (window.isOk()) {
                    String viewName = window.getViewName();
                    String viewDescription = window.getViewDescription();
                    
                    if (view == null || view.getId() == -1) {
                        long id = wsBean.createGeneralView(CLASS_VIEW, viewName, 
                        viewDescription, map.getAsXML(), null, 
                        ipAddress, sessioId);
                        view = wsBean.getGeneralView(id, ipAddress, sessioId);
                        
                        map.connectionsSaved();
                        Notification.show("OSP View Saved", Type.TRAY_NOTIFICATION);
                    }
                    else {
                        wsBean.updateGeneralView(view.getId(), viewName, 
                                viewDescription, map.getAsXML(), null, 
                                ipAddress, sessioId);
                        map.connectionsSaved();
                        Notification.show("OSP View Updated", Type.TRAY_NOTIFICATION);
                    }
                    if (windowCallSaveView) {
                        map.clear();
                        view = null;
                        windowCallSaveView = false;
                    }
                    
                } // end if
                window.removeCloseListener(this);
                getUI().removeWindow(window);
                return;
            }
            if (e.getWindow() instanceof DeleteWindow) {
                DeleteWindow window = (DeleteWindow) e.getWindow();
                if (window.getOption() == ConfirmDialogWindow.OK_OPTION) {
                    if (view != null) {
                        wsBean.deleteGeneralView(new long[]{view.getId()}, 
                                ipAddress, sessioId);
                        map.removeAllPhysicalConnection();
                        map.clear();
                        view = null;
                        Notification.show("OSP View Deleted", Type.TRAY_NOTIFICATION);
                    }
                }                
                window.removeCloseListener(this);
                getUI().removeWindow(window);
                return;
            }
            if (e.getWindow() instanceof CleanViewWindow) {
                CleanViewWindow window = (CleanViewWindow) e.getWindow();
                if (window.getOption() == ConfirmDialogWindow.OK_OPTION) {
                    if (view != null) {
                        map.removeAllPhysicalConnection();
                        map.clear();
                        Notification.show("The view was cleaned", Type.TRAY_NOTIFICATION);
                    }
                }                
                window.removeCloseListener(this);
                getUI().removeWindow(window);
                return;
            }
            if (e.getWindow() instanceof SaveViewDialog) {
                SaveViewDialog window = (SaveViewDialog) e.getWindow();
                if (window.getOption() == ConfirmDialogWindow.YES_OPTION) {
                    saveView();
                    windowCallSaveView = true;
                }
                               
                if (window.getOption() == ConfirmDialogWindow.NO_OPTION) {
                    map.removeConnectionsUnsave();
                    map.clear();
                    view = null;
                }
                window.removeCloseListener(this);
                getUI().removeWindow(window);
            }
        }
        catch(ServerSideException ex) {
            Notification.show(ex.getMessage(), Type.ERROR_MESSAGE);
        }
    }
    
    private void saveView() {        
        String viewName = "";
        String viewDescription = "";
        if (view != null) {
            viewName = view.getName();
            viewDescription = view.getDescription();
        }
        SaveTopologyWindow window = new SaveTopologyWindow(viewName, 
                viewDescription);
        window.addCloseListener(this);
        getUI().addWindow(window);
    }
    
    private class DropHandlerImpl implements DropHandler {
        
        public DropHandlerImpl() {
        }

        @Override
        public void drop(DragAndDropEvent event) {
            Object object = event.getTransferable().getData("itemId");
            if (object instanceof InventoryObjectNode) {
                InventoryObjectNode objectNode = (InventoryObjectNode) object;
                
                map.addNodeMarker((RemoteObjectLight) objectNode.getObject());
            }
            else
                NotificationsUtil.showError("Only inventory objects are allowed to be dropped here");
        }
        
        @Override
        public AcceptCriterion getAcceptCriterion() {
            return AcceptAll.get();
        }
    }
}
