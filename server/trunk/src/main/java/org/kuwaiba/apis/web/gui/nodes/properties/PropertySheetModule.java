/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.apis.web.gui.nodes.properties;

import com.google.common.eventbus.EventBus;
import com.vaadin.navigator.View;
import com.vaadin.server.ThemeResource;
import org.kuwaiba.apis.web.gui.modules.AbstractModule;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 * The definition of the property sheet module.
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class PropertySheetModule extends AbstractModule {

    public PropertySheetModule(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getVersion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getVendor() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMenuEntry() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public View open() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    private PropertySheetComponent propertySheet;
//    private final WebserviceBeanLocal wsBean;
//    private final RemoteSession session;
//    
//     public PropertySheetModule(EventBus eventBus, WebserviceBeanLocal wsBean, RemoteSession session) {
//        super(eventBus);
//        this.wsBean = wsBean;
//        this.session = session;
//        icon = new ThemeResource("img/mod_icon_navtree.png");
//    }
//    
//    @Override
//    public String getName() {
//        return "Property Sheet";
//    }
//
//    @Override
//    public String getDescription() {
//        return "Get the attributes of a selected object";
//    }
//
//    @Override
//    public String getVersion() {
//        return "1.0";
//    }
//
//    @Override
//    public String getVendor() {
//        return "Neotropic SAS";
//    }
//
//    @Override
//    public int getType() {
//        return MODULE_TYPE_FREE_CORE;
//    }
//
//    @Override
//    public String getLocation() {
//        return "Tools/Navigation";
//    }
//
//    @Override
//    public int getMode() {
//        return AbstractModule.COMPONENT_MODE_EXPLORER;
//    }
//
//    @Override
//    public Component open() {
//        if (instanceCount == 0) {
//            propertySheet = new PropertySheetComponent(eventBus, wsBean, session);
//            instanceCount ++;
//        }
//        //Register components in the event bus
//        propertySheet.registerComponents();
//        return propertySheet;
//    }
//    
//    public void show(){
//        propertySheet.showSheet("new");
//    }
//
//    @Override
//    public void close() {
//        //Unregister components from the event bus
//        propertySheet.unregisterComponents();
//    }
    
}
