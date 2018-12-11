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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import java.util.List;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActor;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ProcessInstanceBean {
    private final RemoteProcessInstance processInstance;
    private final WebserviceBean wsBean;
    private final RemoteSession session;
                    
    public ProcessInstanceBean(RemoteProcessInstance processInstance, WebserviceBean wsBean, RemoteSession session) {
        this.processInstance = processInstance;
        this.wsBean = wsBean;
        this.session = session;
    }
    
    public RemoteProcessInstance getProcessInstance() {
        return processInstance;        
    }
    
    public long getProcessId() {
        return processInstance.getId();
    }
    
    public RemoteProcessDefinition getProcessDefinition() {
        try {
            return wsBean.getProcessDefinition(processInstance.getProcessDefinition(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        return null;
    }
    
    private RemoteActivityDefinition getCurrentActivityDefinition() {
        
        RemoteProcessDefinition processDefinition = getProcessDefinition();
        
        if (processDefinition != null) {
            try {
                
                List<RemoteActivityDefinition> path = wsBean.getProcessInstanceActivitiesPath(
                    processInstance.getId(), 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    session.getSessionId());
                
                if (path != null && !path.isEmpty())
                    return path.get(path.size() - 1);
                
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getMessage());
            }
        }
        return null;
    }
    
    private boolean deleteEnable() {
        
        RemoteProcessDefinition processDefinition = getProcessDefinition();
        
        if (processDefinition != null) {
            try {
                
                List<RemoteActivityDefinition> path = wsBean.getProcessInstanceActivitiesPath(
                    processInstance.getId(), 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    session.getSessionId());
                
                if (path != null) {
                    for (RemoteActivityDefinition activityDef : path) {
                                                                        
                        if (processDefinition.getId() == 1 && activityDef.getId() == 3)
                            return false;
                    }
                }
                
            } catch (ServerSideException ex) {                
                Notifications.showError(ex.getMessage());
            }
        }
        return true;
    }
        
    public String getCurrentActivity() {
        RemoteActivityDefinition currentActivityDefinition = getCurrentActivityDefinition();
        
        String result = "";
        
        if (currentActivityDefinition != null) {
            
            result = "<span class=\"v-icon\" style=\"font-family: "
                    + VaadinIcons.STOP.getFontFamily() + ";color:" + (currentActivityDefinition.getColor() == null ? "black" : currentActivityDefinition.getColor())
                    + "\">&#x"
                    + Integer.toHexString(VaadinIcons.STOP.getCodepoint())
                    + ";</span>";
            
            result += " " + currentActivityDefinition.getName();
        }
        return currentActivityDefinition != null ? result : null;
        
    }
    
    public String getCurrentActivityActor() {
        
        RemoteActivityDefinition activityDefinition = getCurrentActivityDefinition();
        
        if (activityDefinition != null) {
            
            RemoteActor actor = activityDefinition.getActor();
            
            if (actor != null)
                return actor.getName();
        }
        return null;
    }
    
    public String getServiceCode() {
        try {
            List<RemoteActivityDefinition> path = wsBean.getProcessInstanceActivitiesPath(
                    processInstance.getId(),
                    Page.getCurrent().getWebBrowser().getAddress(),
                    session.getSessionId());
            
            if (path != null && path.size() >= 2) {
                RemoteActivityDefinition orderServiceActivity = path.get(1);
                
                if (orderServiceActivity != null) {

                    RemoteArtifact artifact = wsBean.getArtifactForActivity(processInstance.getId(),
                            orderServiceActivity.getId(),
                            Page.getCurrent().getWebBrowser().getAddress(),
                            session.getSessionId());

                    if (artifact.getSharedInformation() != null) {

                        for (StringPair pair : artifact.getSharedInformation()) {

                            if (pair.getKey().equals("txtServiceCode"))
                                return pair.getValue();
                        }
                    }
                }
            }
        } catch (ServerSideException ex) {
        }
        return null;
    }
    
    public String getOrderNumber() {
        try {
            List<RemoteActivityDefinition> path = wsBean.getProcessInstanceActivitiesPath(
                    processInstance.getId(),
                    Page.getCurrent().getWebBrowser().getAddress(),
                    session.getSessionId());
            
            if (path != null && path.size() >= 2) {
                RemoteActivityDefinition orderServiceActivity = path.get(1);
                
                if (orderServiceActivity != null) {

                    RemoteArtifact artifact = wsBean.getArtifactForActivity(processInstance.getId(),
                            orderServiceActivity.getId(),
                            Page.getCurrent().getWebBrowser().getAddress(),
                            session.getSessionId());

                    if (artifact.getSharedInformation() != null) {

                        for (StringPair pair : artifact.getSharedInformation()) {

                            if (pair.getKey().equals("txtOrderNumber"))
                                return pair.getValue();
                        }
                    }
                }
            }
        } catch (ServerSideException ex) {
        }
        return null;
    }
    
    public String getDeleteButtonCaption() {
        //return deleteEnable() ? "Delete" : "";
        //return "Delete";
        return "<span class=\"v-icon\" style=\"font-family: "
            + VaadinIcons.TRASH.getFontFamily()
            + "\">&#x"
            + Integer.toHexString(VaadinIcons.TRASH.getCodepoint())
            + ";</span>";
    }
    
    public String getEditButtonCaption() {
        return "<span class=\"v-icon\" style=\"font-family: "
            + VaadinIcons.TASKS.getFontFamily()
            + "\">&#x"
            + Integer.toHexString(VaadinIcons.TASKS.getCodepoint())
            + ";</span>";
    }
    
    public String getViewButtonCaption() {
        return "<span class=\"v-icon\" style=\"font-family: "
            + VaadinIcons.SITEMAP.getFontFamily()
            + "\">&#x"
            + Integer.toHexString(VaadinIcons.SITEMAP.getCodepoint())
            + ";</span>";
    }
    
    public String getTimelineButtonCaption() {
        return "<span class=\"v-icon\" style=\"font-family: "
            + VaadinIcons.CALENDAR_CLOCK.getFontFamily()
            + "\">&#x"
            + Integer.toHexString(VaadinIcons.CALENDAR_CLOCK.getCodepoint())
            + ";</span>";
    }    
}
