/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package org.kuwaiba.apis.forms;

import org.kuwaiba.apis.forms.elements.AbstractFormInstanceLoader;
import org.kuwaiba.apis.forms.elements.FileInformation;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;
import org.kuwaiba.beans.WebserviceBean;

/**
 * 
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FormInstanceLoader extends AbstractFormInstanceLoader {
    private final WebserviceBean wsBean;
    private final RemoteSession session;
    
    public FormInstanceLoader(WebserviceBean wsBean, RemoteSession session) {
        this.wsBean = wsBean;
        this.session = session;
    }

    @Override
    public RemoteObjectLight getRemoteObjectLight(long classId, String objectId) {
        try {
            RemoteClassMetadata cli = getClassInfoLight(classId);
            return wsBean.getObjectLight(cli.getClassName(), objectId, session.getIpAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        return null;
    }

    @Override
    public RemoteClassMetadata getClassInfoLight(long classId) {
        try {
            return wsBean.getClass(classId, session.getIpAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        return null;
    }
    
    @Override
    public Object getAttachment(String name, String path) {
        return new FileInformation(name, path);
    }
        
}
