/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.apis.web.gui.modules;

import com.vaadin.navigator.View;
import com.google.common.eventbus.EventBus;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * The superclass of all components to be embedded in a Kuwaiba module. 
 * Note that you don't need to inherit from this class if you don't plan to use persistence
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class AbstractTopComponent implements TopComponent, View {
    /**
     * Reference to the global WebService bean instance, which doesn't seem to be injected using CDI.
     */
    protected WebserviceBeanLocal wsBean;
    /**
     * Reference to the global EventBus bean instance, which doesn't seem to be injected using CDI.
     */
    protected EventBus eventBus;
    /**
     * Reference to the current session
     */
    protected RemoteSession session;

    public AbstractTopComponent(WebserviceBeanLocal wsBean, EventBus eventBus, RemoteSession session) {
        this.wsBean = wsBean;
        this.eventBus = eventBus;
        this.session = session;
    }

    @Override
    public final WebserviceBeanLocal getWsBean() {
        return wsBean;
    }

    @Override
    public final EventBus getEventBus() {
        return eventBus;
    }
    
    @Override
    public final RemoteSession getApplicationSession() {
        return session;
    }   
}
