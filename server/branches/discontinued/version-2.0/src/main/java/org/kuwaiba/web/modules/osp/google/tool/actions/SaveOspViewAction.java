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
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import de.steinwedel.messagebox.MessageBox;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.ViewInfo;
import org.kuwaiba.web.modules.osp.OutsidePlantComponent;
import org.kuwaiba.web.modules.osp.google.GoogleMapWrapper;
import org.kuwaiba.web.modules.osp.google.OutsidePlantTooledComponent;

/**
 *
 *  @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SaveOspViewAction extends AbstractAction {
    private final FormLayout form = new FormLayout();
    private final TextField tfName = new TextField("Name");
    private final TextField tfDescription = new TextField("Description");
    
    public SaveOspViewAction(String caption, String resourceId) {
        super(caption, new ThemeResource(resourceId));
                
        tfName.setRequired(true);
        form.setMargin(true);
        form.addComponent(tfName);
        form.addComponent(tfDescription);
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        OutsidePlantComponent parentComponent = (OutsidePlantComponent) sourceComponent;
        GoogleMapWrapper mapWrapper = (GoogleMapWrapper) targetObject;
        
        
        if (mapWrapper.getCurrentView() != null) {
            tfName.setValue(mapWrapper.getCurrentView().getName());
            tfDescription.setValue(mapWrapper.getCurrentView().getDescription());
        } else {
            tfName.setValue("");
            tfDescription.setValue("");
        }
        MessageBox mbSaveView = MessageBox.createQuestion()
            .withCaption("Save view")
            .withMessage(form)
            .withOkButton(() -> {
                ViewInfo view = mapWrapper.getCurrentView();
                
                try {
                    long id = -1;
                    if (view == null) {
                        id = parentComponent.getWsBean().createGeneralView(
                            GoogleMapWrapper.CLASS_VIEW, 
                            tfName.getValue(), 
                            tfDescription.getValue(), 
                            mapWrapper.getMap().getAsXML(), 
                            null, 
                            Page.getCurrent().getWebBrowser().getAddress(), 
                            parentComponent.getApplicationSession().getSessionId());
                                                
                        Notification.show("OSP View Saved", Notification.Type.TRAY_NOTIFICATION);
                        mapWrapper.getMap().physicalConnectionsSaved();
                    } else {
                        id = view.getId();
                        
                        parentComponent.getWsBean().updateGeneralView(
                            id, 
                            tfName.getValue(), 
                            tfDescription.getValue(), 
                            mapWrapper.getMap().getAsXML(), 
                            null, 
                            Page.getCurrent().getWebBrowser().getAddress(), 
                            parentComponent.getApplicationSession().getSessionId());
                                                
                        Notification.show("OSP View Updated", Notification.Type.TRAY_NOTIFICATION);
                        mapWrapper.getMap().physicalConnectionsSaved();
                    }
                    
                    if (id != -1) {
                        
                        if (mapWrapper.isViewClosedByNewView()) {
                            
                            mapWrapper.initNewMap();
                            
                            ActionsFactory.createNewOspViewAction(
                                OutsidePlantTooledComponent.ACTION_CAPTION_NEW, 
                                OutsidePlantTooledComponent.ACTION_ICON_NEW)
                                    .actionPerformed(sourceComponent, targetObject);
                            return;
                        }
                        if (mapWrapper.isViewClosedByOpenView()) {
                            
                            mapWrapper.initNewMap();
                            ((OutsidePlantComponent) sourceComponent).removeMainComponentToTooledComponent();
                            
                            ActionsFactory.createOpenOspViewAction(
                                OutsidePlantTooledComponent.ACTION_CAPTION_OPEN, 
                                OutsidePlantTooledComponent.ACTION_ICON_OPEN)
                                    .actionPerformed(sourceComponent, targetObject);
                            return;
                        }
                        mapWrapper.getMap().setUpdateView(false);
                        mapWrapper.setCurrentView(parentComponent.getWsBean().getGeneralView(
                            id, 
                            Page.getCurrent().getWebBrowser().getAddress(), 
                            parentComponent.getApplicationSession().getSessionId()));
                    }
                } catch (ServerSideException ex) {
                    Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                }
            })
            .withCancelButton();
        
        mbSaveView.open();
    }
    
}
