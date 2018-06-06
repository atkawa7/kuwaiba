/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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

import com.vaadin.server.Page;
import org.kuwaiba.apis.forms.elements.AbstractFormInstanceLoader;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.metadata.ClassInfo;

/**
 * 
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FormInstanceLoader extends AbstractFormInstanceLoader {
    private final WebserviceBeanLocal wsBean;
    private final RemoteSession session;
    
    public FormInstanceLoader(WebserviceBeanLocal wsBean, RemoteSession session) {
        this.wsBean = wsBean;
        this.session = session;
    }

    @Override
    public RemoteObjectLight getRemoteObjectLight(long classId, long objectId) {
        try {
            ClassInfo cli = getClassInfoLight(classId);
            return wsBean.getObjectLight(cli.getClassName(), objectId, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            NotificationsUtil.showError(ex.getMessage());
        }
        return null;
    }

    @Override
    public ClassInfo getClassInfoLight(long classId) {
        try {
            return wsBean.getClass(classId, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            NotificationsUtil.showError(ex.getMessage());
        }
        return null;
    }
        
}