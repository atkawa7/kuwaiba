/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.web.gui.dashboards.widgets;

import com.vaadin.server.Page;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.List;
import org.kuwaiba.apis.web.gui.dashboards.AbstractDashboardWidget;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.apis.web.gui.resources.ResourceFactory;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteFileObjectLight;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * Widget that allows to manage the files attached to an inventory object
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AttachedFilesDashboardWidget extends AbstractDashboardWidget {
/**
     * The reference to the business object the reports are related to
     */
    private RemoteObjectLight businessObject;
    /**
     * Reference to the backend bean
     */
    private WebserviceBean wsBean;
    
    public AttachedFilesDashboardWidget(RemoteObjectLight businessObject, WebserviceBean wsBean) {
        super("Attached Files");
        this.businessObject = businessObject;
        this.wsBean = wsBean;
        this.createCover();
    }

    @Override
    public void createCover() {
        super.createCover();
        coverComponent.setStyleName("dashboard_cover_widget-darkorange");
    }

    @Override
    public void createContent() {
        try {
            RemoteSession session = (RemoteSession) UI.getCurrent().getSession().getAttribute("session");
            List<RemoteFileObjectLight> attachedFiles = wsBean.getFilesForObject(businessObject.getClassName(), businessObject.getId(), session.getIpAddress(), 
                    session.getSessionId());
            
            if (attachedFiles.isEmpty())
                this.contentComponent = new Label("This object does not have files attached to it");
            else {
                VerticalLayout lytAttachments = new VerticalLayout();
                Grid<RemoteFileObjectLight> tblAttachments = new Grid<>();
                tblAttachments.setItems(attachedFiles);
                tblAttachments.setHeaderVisible(false);
                tblAttachments.addComponentColumn((source) -> {
                    Button btnAttachment = new Button(source.getName());
                    btnAttachment.setStyleName(ValoTheme.BUTTON_LINK);
                    btnAttachment.addClickListener((event) -> {
                        try {

                            RemoteFileObject attachment = wsBean.getFile(source.getFileOjectId(), businessObject.getClassName(), 
                                    businessObject.getId(), Page.getCurrent().getWebBrowser().getAddress(),
                                    ((RemoteSession) UI.getCurrent().getSession().getAttribute("session")).getSessionId());

                            StreamResource fileStream = ResourceFactory.getFileStream(attachment.getFile(), /*attachment.getFileOjectId() + "_" + <- In case we get conflicts with files with the same name. Doubtful if the paths are generated on a per session basis*/ attachment.getName());
                            fileStream.setMIMEType("application/octet-stream"); //NOI18N
                            setResource(String.valueOf(source.getFileOjectId()), fileStream);
                            ResourceReference rr = ResourceReference.create(fileStream, this, String.valueOf(source.getFileOjectId()));
                            Page.getCurrent().open(rr.getURL(), null, false);
                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getLocalizedMessage());
                        }
                    });
                                        
                    return btnAttachment; 
                });
                tblAttachments.addColumn(RemoteFileObjectLight::getTags).setCaption("Tags");
                tblAttachments.setSizeFull();
                
                lytAttachments.addComponent(tblAttachments);
                lytAttachments.setWidth(100, Unit.PERCENTAGE);
                this.contentComponent = lytAttachments;
            }
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getLocalizedMessage());
        }
    }
}
