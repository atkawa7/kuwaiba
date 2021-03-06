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
package org.kuwaiba.web.modules.osp;

import com.google.common.eventbus.EventBus;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.web.modules.osp.google.GoogleMapWrapper;
import org.kuwaiba.web.modules.osp.google.OutsidePlantTooledComponent;

/**
 * The main component of the OSP module.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class OutsidePlantComponent extends AbstractTopComponent {
    private final GoogleMapWrapper googleMapWrapper;
    private final OutsidePlantTooledComponent tooledComponent;
            
    public OutsidePlantComponent(EventBus eventBus, WebserviceBeanLocal wsBean, RemoteSession session) {
        super(wsBean, eventBus, session);
        
        googleMapWrapper = new GoogleMapWrapper(this);
        googleMapWrapper.setSizeFull();
        
        tooledComponent = new OutsidePlantTooledComponent(this);
        
        setCompositionRoot(tooledComponent);
        enableTools(false);
        this.setSizeFull();
    }
    
    public GoogleMapWrapper getGoogleMapWrapper() {
        return googleMapWrapper;
    }
    
    public void addMainComponentToTooledComponent() {
        if (tooledComponent.getMainComponent() == null)
            tooledComponent.setMainComponent(googleMapWrapper);
    }
            
    public void removeMainComponentToTooledComponent() {
        enableTools(false);
        tooledComponent.setMainComponent(null);
    }
    
    public void enableTools(boolean enable) {
        tooledComponent.enableTools(enable);
    }
            
    @Override
    public void registerComponents() {
        googleMapWrapper.register();
        tooledComponent.register();
    }
    
    @Override
    public void unregisterComponents() {
        googleMapWrapper.unregister();
        tooledComponent.unregister();
    }
}