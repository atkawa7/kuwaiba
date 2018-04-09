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

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Notification;
import de.steinwedel.messagebox.MessageBox;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.web.modules.osp.OutsidePlantComponent;
import org.kuwaiba.web.modules.osp.providers.google.CustomGoogleMap;
import org.kuwaiba.web.modules.osp.providers.google.GoogleMapWrapper;
import org.kuwaiba.web.modules.osp.providers.google.OutsidePlantTooledComponent;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class NewOspViewAction extends AbstractAction {
    
    public NewOspViewAction(String caption, String resourceId) {
        super(caption, new ThemeResource(resourceId));
    }

    @Override
    public void actionPerformed(Object sourceComponent, Object targetObject) {
        CustomGoogleMap map = ((GoogleMapWrapper) targetObject).getMap();
//        if (map.getUpdateView()) {
//            ((GoogleMapWrapper) targetObject).setViewClosedByNewView(true);
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
//                    ActionsFactory.createNewOspViewAction(
//                        OutsidePlantTooledComponent.ACTION_CAPTION_NEW, 
//                        OutsidePlantTooledComponent.ACTION_ICON_NEW)
//                            .actionPerformed(sourceComponent, targetObject);
//                })
//                .withCancelButton(() -> {
//                    ((GoogleMapWrapper) targetObject).setViewClosedByNewView(false);
//                });
//            
//            mbUpdateView.open();
//        } else {
//            ((OutsidePlantComponent) sourceComponent).addMainComponentToTooledComponent();
//            ((GoogleMapWrapper) targetObject).initNewMap();
//            ((OutsidePlantComponent) sourceComponent).enableTools(true);
//            
//            Notification.show("New OSP View", Notification.Type.TRAY_NOTIFICATION);
//        }
    }
}
