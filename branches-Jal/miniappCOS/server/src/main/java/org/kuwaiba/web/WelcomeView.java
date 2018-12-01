/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.Page;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import com.vaadin.tapio.googlemaps.GoogleMapsComponent;
import com.vaadin.tapio.googlemaps.client.LatLon;
import com.vaadin.ui.UI;
import java.util.List;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * The welcome screen
 * @author Charles Edward Bedon Cortazar{@literal <charles.bedon@kuwaiba.org>}
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@CDIView("welcome")
class WelcomeView extends VerticalLayout implements View {
    /**
     * View identifier
     */
    public static String VIEW_NAME = "welcome";
    
    @Inject
    private WebserviceBean wsBean;
    
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        
        final RemoteSession session = (RemoteSession)getSession().getAttribute("session");
        
        if (session == null) 
             getUI().getNavigator().navigateTo(LoginView.VIEW_NAME);
        else {
            Page.getCurrent().setTitle(String.format("Kuwaiba Open Network Inventory - [%s]", session.getUsername()));
            
            VerticalLayout lytContent = new VerticalLayout();
            try {
                Context context = new InitialContext();
                String apiKey = (String)context.lookup("java:comp/env/googleMapsApiKey");
                String language = (String)context.lookup("java:comp/env/mapLanguage");
                
                
                GoogleMapsComponent mapMain = new GoogleMapsComponent(apiKey.isEmpty() ? null : apiKey, null, language);
                
                mapMain.setSizeFull();
                mapMain.showMarkerLabels(true);
                
                try {
                    List<RemoteObjectLight> allPhysicalLocations = wsBean.getObjectsOfClassLight(Constants.CLASS_GENERICLOCATION, -1, Page.getCurrent().getWebBrowser().getAddress(), 
                            ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                    
                    allPhysicalLocations.stream().forEach(aPhysicalLocation -> {
                            try {
                                String longitude = wsBean.getAttributeValueAsString(aPhysicalLocation.getClassName(), 
                                        aPhysicalLocation.getId(), "longitude", Page.getCurrent().getWebBrowser().getAddress(), 
                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                
                                if (longitude != null) {
                                    String latitude = wsBean.getAttributeValueAsString(aPhysicalLocation.getClassName(), 
                                        aPhysicalLocation.getId(), "latitude", Page.getCurrent().getWebBrowser().getAddress(), 
                                        ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());
                                    
                                    if (latitude != null)
                                        mapMain.addMarker(aPhysicalLocation.toString(), new LatLon(
                                            Float.valueOf(latitude), Float.valueOf(longitude)), false, "/icons/" + aPhysicalLocation.getClassName() + ".png");
                                }
                                
                            } catch (ServerSideException ex) {
                                Notifications.showError(ex.getLocalizedMessage());
                            }
                    });
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getLocalizedMessage());
                }
                
                mapMain.setCenter(new LatLon(12.8260721, 11.8399727));
                mapMain.setZoom(3);

                lytContent.addComponent(mapMain);

 //                 OLMap map = new OLMap(new OLMapOptions().setShowOl3Logo(true).setInputProjection(Projections.EPSG4326));
                // add layer to map
//                OLOSMSourceOptions options = new OLOSMSourceOptions();
//                OLOSMSource mapSource = new OLOSMSource();
//
//                OLBingSourceOptions options = new OLBingSourceOptions();
//                options.setImageryType("Road");
//                options.setCulture("en-us");
//                options.setKey("AqYNJVHws6td2MxrvBpDNLt-NIMgWmI93hESK0E6nSU2oHIc2G8f-wFpgCwVttJq");
//                OLBingSource mapSource = new OLBingSource(options);
//
//                OLTileLayer layer = new OLTileLayer(mapSource);
//                map.addLayer(layer);
//                
//                map.setView(createView());
//                
//                map.setSizeFull();
//                lytContent.addComponent(map);

            } catch (NamingException ex) {
                Notifications.showError(String.format("Map configuration could not be retrieved: %s", ex.getLocalizedMessage()));
            }
            lytContent.setSizeFull();
            
            MenuBar mnuMain = ((IndexUI)getUI()).getMainMenu();
            
            this.addComponents(mnuMain, lytContent);
            this.setExpandRatio(mnuMain, 0.3f);
            this.setExpandRatio(lytContent, 9.7f);
            this.setSizeFull();
        }
    }
    
//    protected OLView createView(){
//        OLViewOptions opts=new OLViewOptions();
//        opts.setInputProjection(Projections.EPSG4326);
//        OLView view=new OLView(opts);
//        view.setZoom(1);
//        view.setCenter(0,0);
//        return view;
//    }
}
