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
package org.kuwaiba.web.modules.osp.providers.google.tool.actions;

import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.Notification;
import de.steinwedel.messagebox.MessageBox;
import org.kuwaiba.apis.web.gui.actions.AbstractComposedAction;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.ViewInfo;
import org.kuwaiba.interfaces.ws.toserialize.application.ViewInfoLight;
import org.kuwaiba.web.modules.osp.OutsidePlantComponent;
import org.kuwaiba.web.modules.osp.providers.google.CustomGoogleMap;
import org.kuwaiba.web.modules.osp.providers.google.GoogleMapWrapper;
import org.kuwaiba.web.modules.osp.providers.google.OutsidePlantTooledComponent;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class OpenOspViewAction extends AbstractComposedAction {
    private final FormLayout form;
    private final ListSelect availableViews;
    //private final Property.ValueChangeListener valueChangeListener;
    private MessageBox mbChooseView;
    private ViewInfoLight viewChoosed;
    
    public OpenOspViewAction(String caption, String resourceId) {
        super(caption, new ThemeResource(resourceId));
                
        availableViews = new ListSelect("Choose a view");
                
//        valueChangeListener = new Property.ValueChangeListener() {
//
//            @Override
//            public void valueChange(Property.ValueChangeEvent event) {
//                viewChoosed = (ViewInfoLight) event.getProperty().getValue();
//            }
//        };
        //availableViews.addValueChangeListener(valueChangeListener);
        
        form = new FormLayout();
        form.addComponent(availableViews);
    }

    @Override
    public void finalActionPerformed(Object sourceComponent, Object targetObject, Object selectedOption) {
        try {
            OutsidePlantComponent parentComponent = (OutsidePlantComponent) sourceComponent;
            parentComponent.addMainComponentToTooledComponent();
            
            CustomGoogleMap map = ((GoogleMapWrapper) targetObject).getMap();
            
            if (selectedOption == null) {
                Notification.show("Choose a view", Notification.Type.ERROR_MESSAGE);
                return;
            }
            
            ViewInfoLight selectedView = (ViewInfoLight) selectedOption;
            
            ViewInfo view = parentComponent.getWsBean().getGeneralView(
                selectedView.getId(), 
                Page.getCurrent().getWebBrowser().getAddress(), 
                parentComponent.getApplicationSession().getSessionId());
            
//            map.newMap();
//            map.render(view.getStructure());
//            
//            if (map.getUpdateView()) {
//                view.setStructure(map.getAsXML());
//                
//                parentComponent.getWsBean().updateGeneralView(
//                    view.getId(), 
//                    view.getName(), 
//                    view.getDescription(), 
//                    view.getStructure(), 
//                    null, 
//                    Page.getCurrent().getWebBrowser().getAddress(), 
//                    parentComponent.getApplicationSession().getSessionId());
//                
//                Notification.show("OSP View Updated", Notification.Type.TRAY_NOTIFICATION);
//                map.physicalConnectionsSaved();
//            }
//            map.setUpdateView(false);
            ((GoogleMapWrapper) targetObject).setCurrentView(view);
            parentComponent.enableTools(true);
            Notification.show("Open OSP View", Notification.Type.TRAY_NOTIFICATION);
            
        } catch (ServerSideException ex) {
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        CustomGoogleMap map = ((GoogleMapWrapper) targetObject).getMap();
//        if (map.getUpdateView()) {
//            ((GoogleMapWrapper) targetObject).setViewClosedByOpenView(true);
//            
//            MessageBox mbUpdateView = MessageBox.createQuestion()
//                .withCaption("Confirmation")
//                .withMessage("This view has not been saved, do you want to save it")
//                .withYesButton(() -> {
//                    ActionsFactory.createSaveOspViewAction(
//                        OutsidePlantTooledComponent.ACTION_CAPTION_SAVE, 
//                        OutsidePlantTooledComponent.ACTION_ICON_SAVE)
//                            .actionPerformed(sourceComponent, targetObject);
//                })
//                .withNoButton(() -> {
//                    ((GoogleMapWrapper) targetObject).initNewMap();
//                    ((OutsidePlantComponent) sourceComponent).removeMainComponentToTooledComponent();                    
//                    
//                    ActionsFactory.createOpenOspViewAction(
//                        OutsidePlantTooledComponent.ACTION_CAPTION_OPEN, 
//                        OutsidePlantTooledComponent.ACTION_ICON_OPEN)
//                            .actionPerformed(sourceComponent, targetObject);
//                })
//                .withCancelButton(() -> {
//                    ((GoogleMapWrapper) targetObject).setViewClosedByOpenView(false);
//                });
//            
//            mbUpdateView.open();
//        } else {
//            try {
//                OutsidePlantComponent parentComponent = (OutsidePlantComponent) sourceComponent;
//                
//                ViewInfoLight [] views = parentComponent.getWsBean().getGeneralViews(
//                    GoogleMapWrapper.CLASS_VIEW, 
//                    -1, 
//                    Page.getCurrent().getWebBrowser().getAddress(), 
//                    parentComponent.getApplicationSession().getSessionId());
//                
//                if (views.length > 0) {
//                    
////                    availableViews.removeAllItems();
////                    
////                    for (ViewInfoLight view : views)
////                        availableViews.addItem(view);
//                    availableViews.setSizeFull();
//                    availableViews.setRows(5);
//                    
//                    mbChooseView = MessageBox.createQuestion()
//                        .withMessage(form)
//                        .withOkButton(() -> { finalActionPerformed(sourceComponent, targetObject, viewChoosed); })
//                        .withCancelButton();
//                    mbChooseView.open();
//                    
//                } else {
//                    Notification.show("There are not views", Notification.Type.TRAY_NOTIFICATION);
//                }
//            } catch (ServerSideException ex) {
//                Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
//            }
//        }
    }
    
}
