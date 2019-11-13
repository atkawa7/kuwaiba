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
package org.kuwaiba.apis.web.gui.dashboards.widgets;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.openide.util.Exceptions;

/**
 * Widget that allows to manage the files attached to an inventory object
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class AttachedFilesTabWidget extends VerticalLayout {
    /**
     * The reference to the business object the reports are related to
     */
    private final RemoteObjectLight remoteObject;
    /**
     * Reference to the backend bean
     */
    private final WebserviceBean webserviceBean;
    
    public AttachedFilesTabWidget(RemoteObjectLight remoteObject, WebserviceBean webserviceBean) {
        this.remoteObject = remoteObject;
        this.webserviceBean = webserviceBean;                
    }
    
    public void createContent() {
        try {
            final RemoteSession remoteSession = UI.getCurrent().getSession().getAttribute(RemoteSession.class);
            List<RemoteFileObjectLight> files = webserviceBean.getFilesForObject(remoteObject.getClassName(), remoteObject.getId(), 
                remoteSession.getIpAddress(), remoteSession.getSessionId());
            if (files.isEmpty())
                add(new Label("This object does not have files attached to it"));
            else {
                Grid<RemoteFileObjectLight> grdAttachments = new Grid();
                grdAttachments.setItems(files);
                grdAttachments.addComponentColumn(item -> {
                    String uri = "";
                    try {
                        final RemoteFileObject attachment = webserviceBean.getFile(item.getFileOjectId(), 
                            remoteObject.getClassName(), remoteObject.getId(), remoteSession.getIpAddress(), remoteSession.getSessionId());
                        attachment.getFile();
                        
                        StreamRegistration resource = UI.getCurrent().getSession().getResourceRegistry().registerResource(
                            new StreamResource(attachment.getName(), new InputStreamFactory() {
                                @Override
                                public InputStream createInputStream() {
                                    return new ByteArrayInputStream(attachment.getFile());
                                }
                        }));
                        uri = resource.getResourceUri().toString();
                    } catch (ServerSideException ex) {
                        Notification.show(ex.getMessage());
                    }
                    return new Anchor(uri, item.getName());
                });
                grdAttachments.addColumn(RemoteFileObjectLight::getTags).setHeader("Tags");
                grdAttachments.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
                add(grdAttachments);
            }
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
