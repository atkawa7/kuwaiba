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

package org.kuwaiba.web.modules.navtree.views;

import org.kuwaiba.apis.web.gui.navigation.views.AbstractScene;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * The scene in the ObjectView component
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class ObjectViewScene extends AbstractScene {
    /**
     * Reference to the related inventory object
     */
    private RemoteObjectLight businessObject;
    
    public ObjectViewScene(RemoteObjectLight businessObject, WebserviceBean wsBean, RemoteSession session) {
        super(wsBean, session);
        this.businessObject = businessObject;
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void render() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
