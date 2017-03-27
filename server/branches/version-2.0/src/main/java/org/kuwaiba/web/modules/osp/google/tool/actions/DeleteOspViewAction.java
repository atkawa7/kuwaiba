/*
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
package org.kuwaiba.web.modules.osp.google.tool.actions;

import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Notification;
import de.steinwedel.messagebox.MessageBox;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.ViewInfo;
import org.kuwaiba.web.modules.osp.OutsidePlantComponent;
import org.kuwaiba.web.modules.osp.google.GoogleMapWrapper;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class DeleteOspViewAction extends AbstractAction {
        
    public DeleteOspViewAction(String caption, String resourceId) {
        super(caption, new ThemeResource(resourceId));
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        OutsidePlantComponent parentComponent = (OutsidePlantComponent) sourceComponent;
        GoogleMapWrapper mapWrapper = (GoogleMapWrapper) targetObject;
        
        MessageBox msDelete = MessageBox.createQuestion()
            .withCaption("Confirmation")
            .withMessage("Are you sure you want to delete the current view?")
            .withOkButton(() -> {
                try {
                    ViewInfo view = mapWrapper.getCurrentView();
                    
                    if (view != null) {
                        parentComponent.getWsBean().deleteGeneralView(
                                new long[]{view.getId()},
                                Page.getCurrent().getWebBrowser().getAddress(),
                                parentComponent.getApplicationSession().getSessionId()
                        );
                        Notification.show("OSP View Deleted", Notification.Type.TRAY_NOTIFICATION);
                    }
                    mapWrapper.getMap().clear();
                    mapWrapper.initNewMap();
                    parentComponent.removeMainComponentToTooledComponent();
                    parentComponent.enableTools(false);

                } catch (ServerSideException ex) {
                    Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }
            })
            .withCancelButton();
        
        msDelete.open();
    }
}
