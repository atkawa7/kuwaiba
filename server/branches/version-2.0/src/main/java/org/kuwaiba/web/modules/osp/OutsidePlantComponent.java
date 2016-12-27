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
package org.kuwaiba.web.modules.osp;

import com.google.common.eventbus.EventBus;
import org.kuwaiba.apis.web.gui.modules.AbstractTopComponent;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.web.custom.osp.GoogleMapsGISView;

/**
 * The main component of the OSP module.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
class OutsidePlantComponent extends AbstractTopComponent {
        
        public OutsidePlantComponent(EventBus eventBus, WebserviceBeanLocal wsBean, RemoteSession session) {
            super(wsBean, eventBus, session);
            setCompositionRoot(new GoogleMapsGISView(eventBus));
            this.setSizeFull();
        }
        
        @Override
        public void registerComponents() {
            //TODO
        }
        
        @Override
        public void unregisterComponents() {
            //TODO
        }
    }