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
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import java.io.File;
import java.util.Properties;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.web.gui.miniapps.AbstractMiniApplication;
import org.kuwaiba.apis.web.gui.notifications.Notifications;

/**
 * Mini Application used to show the Physical Path View of a given port.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MiniAppPhysicalPath extends AbstractMiniApplication<Component, Component> {
    
    public MiniAppPhysicalPath(Properties inputParameters) {
        super(inputParameters);
    }

    @Override
    public String getDescription() {
        return "Mini Application used to show the Physical Path View given a port.";
    }

    @Override
    public Component launchDetached() {
        return null;
    }

    @Override
    public Component launchEmbedded() {
        Panel panel = new Panel();
        try {   
            String id = getInputParameters().getProperty("id") != null ? getInputParameters().getProperty("id") : "-1"; //NOI18N
            String className = getInputParameters().getProperty("className"); //NOI18N
            
            if (id != null && !id.equals("-1") && className != null) {
                
                SceneExporter sceneExporter = SceneExporter.getInstance();
                
                String oldPath = SceneExporter.PATH;
                
                String processEnginePath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processEnginePath"));
                String newPath = processEnginePath + "/temp/"; //NOI18N

                SceneExporter.PATH = newPath;

                String img = sceneExporter.buildPhysicalPathView(className, id);
                if(img == null){
                    Label lblPortHasNoConnections = new Label("The port has no physical connections attached to it.");
                    VerticalLayout content = new VerticalLayout(lblPortHasNoConnections);
                    content.setHeight("455px");
                    content.setComponentAlignment(lblPortHasNoConnections, Alignment.MIDDLE_CENTER);
                    panel.setContent(content);
                }
                else{
                    SceneExporter.PATH = oldPath;

                    FileResource resource = new FileResource(new File(newPath + img + ".png"));                    

                    Image image = new Image();
                    image.setSource(resource);

                    //image.setWidth("100%");
                    //image.setHeightUndefined();

                    //panel.setSizeFull();
                    panel.setContent(image);
                }
            }
        }
        catch(Exception exception) {
            Notifications.showError("Unexpected input parameter was received in the MiniAppPhysicalPath");
        }
        return panel;
    }

    @Override
    public int getType() {
        return AbstractMiniApplication.TYPE_WEB;
    }
    
}
