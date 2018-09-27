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
package org.kuwaiba.web.procmanager;

import com.neotropic.kuwaiba.modules.reporting.img.SceneExporter;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import java.io.File;
import java.util.Properties;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * Mini Application used to show the Rack View given the object Id and Class Name
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class MiniAppRackView extends AbstractMiniApplication<Component, Component> {
    
    public MiniAppRackView(Properties inputParameters) {
        super(inputParameters);
    }

    @Override
    public String getDescription() {
        return "Mini Application used to show the Rack View given the object Id and Class Name";
    }

    @Override
    public Component launchDetached() {
        return null;
    }

    @Override
    public Component launchEmbedded() {
        Panel panel = new Panel();
        try {   
            long id = getInputParameters().getProperty("id") != null ? Long.valueOf(getInputParameters().getProperty("id")) : -1; //NOI18N
            String className = getInputParameters().getProperty("className"); //NOI18N
            
            if (id != -1 && className != null) {
                
                SceneExporter sceneExporter = SceneExporter.getInstance();
                
                String oldPath = SceneExporter.PATH;
                
                String processEnginePath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processEnginePath"));
                String newPath = processEnginePath + "/temp/"; //NOI18N

                SceneExporter.PATH = newPath;

                String img = sceneExporter.buildRackView(
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    (RemoteSession) UI.getCurrent().getSession().getAttribute("session"), //NOI18N
                    wsBean, 
                    className, 
                    id);
                                
                SceneExporter.PATH = oldPath;
                
                FileResource resource = new FileResource(new File(newPath + img + ".png"));                    

                Image image = new Image();
                image.setSource(resource);
                
                image.setWidth("100%");
                image.setHeightUndefined();
                
                panel.setSizeFull();
                panel.setContent(image);
            }
        }
        catch(NumberFormatException numberFormatException) {
            Notification.show("Unexpected Input Parameter was received in the MiniAppRackView", Notification.Type.ERROR_MESSAGE);
        }
        return panel;
    }

    @Override
    public int getType() {
        return AbstractMiniApplication.TYPE_WEB;
    }
    
}
