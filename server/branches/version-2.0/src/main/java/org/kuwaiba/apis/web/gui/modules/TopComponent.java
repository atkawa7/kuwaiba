/**
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
package org.kuwaiba.apis.web.gui.modules;

import com.google.common.eventbus.EventBus;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * Interface with the same purpose of AbstractEmbeddedComponent, can be used to by pass the multiple inheritance restriction in Java.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface TopComponent {
    /**
     * Gets the reference to the global WebserviceBean instance.
     * @return The global WebserviceBean instance.
     */
    public WebserviceBeanLocal getWsBean();
    /**
     * Gets the reference to the global EventBus instance.
     * @return The global EventBus instance.
     */
    public EventBus getEventBus();
    /**
     * Gets the reference to the current session.
     * @return The current session.
     */
    public RemoteSession getApplicationSession();
    /**
     * Registers all relevant components in the global event bus
     */
    public void registerComponents();
    /**
     * Registers all relevant components from the global event bus
     */
    public void unregisterComponents();
}
