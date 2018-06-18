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
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 *
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class PropertySheetComponent extends AbstractTopComponent{

    public PropertySheetComponent(EventBus eventBus, WebserviceBean wsBean, RemoteSession session) {
        super(wsBean, eventBus, session);
    }

    @Override
    public void registerComponents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void unregisterComponents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * The property sheet
     */
//    private NodeProperty propertySheet;
//    
//    public PropertySheetComponent(EventBus eventBus, WebserviceBeanLocal wsBean, RemoteSession session) {
//        super(wsBean, eventBus, session);
//        propertySheet = new NodeProperty(this);
//        setCompositionRoot(propertySheet);
//    }
//
//    @Override
//    public void registerComponents() {
//        propertySheet.register();
//    }
//
//    @Override
//    public void unregisterComponents() {
//        propertySheet.unregister();
//    }
//    
//    public void showSheet(String product) {
//        if (product != null) {
//            addStyleName("visible");
//            setEnabled(true);
//        } else {
//            removeStyleName("visible");
//            setEnabled(false);
//        }
//    }
}
