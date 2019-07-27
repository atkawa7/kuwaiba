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
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import java.io.File;
import java.util.Properties;
import javax.naming.InitialContext;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * Mini Application that Shows the a Rack View of a given object.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MiniAppRackView extends AbstractMiniApplication<Component, Component> {
    
    public MiniAppRackView(Properties inputParameters) {
        super(inputParameters);
    }

    @Override
    public String getDescription() {
        return "Shows the a Rack View of a given object";
    }

    @Override
    public Component launchDetached() {
        return launchEmbedded();
    }

    @Override
    public Component launchEmbedded() {
        Panel panel = new Panel();
        try {   
            String id = getInputParameters().getProperty("id") != null ? getInputParameters().getProperty("id") : "-1"; //NOI18N
            String className = getInputParameters().getProperty("className"); //NOI18N
            
            if (id != null && !id.equals("-1") && className != null) {
                
                String oldPath = SceneExporter.PATH;                    
                String newPath = (String) new InitialContext().lookup("java:comp/env/attachmentsPath"); //NOI18N
                SceneExporter.PATH = newPath;
                File file = new File(SceneExporter.PATH + "/tmpRackView_" + id + ".png");
                SceneExporter.PATH = oldPath;
                if (file.exists()) {
                    FileResource resource = new FileResource(file);                    
                    Image image = new Image();
                    image.setSource(resource);
                    image.setWidth("100%");
                    image.setHeightUndefined();
                    panel.setSizeFull();
                    panel.setContent(image);
                    return panel;
                }
                SceneExporter sceneExporter = SceneExporter.getInstance();
                
                oldPath = SceneExporter.PATH;
                
                String processEnginePath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processEnginePath"));
                newPath = processEnginePath + "/temp/"; //NOI18N

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
        } catch(Exception exception) {
            Notifications.showError("The rack view can not be displayed " + exception.getMessage());
        }
        return panel;
    }

    @Override
    public int getType() {
        return AbstractMiniApplication.TYPE_WEB;
    }
    
}
