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

import com.vaadin.server.Page;
import java.util.List;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.util.NotificationsUtil;
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
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ProcessInstanceBean {
    private RemoteProcessInstance processInstance;
    private final WebserviceBean wsBean;
    private RemoteSession session;
                    
    public ProcessInstanceBean(RemoteProcessInstance processInstance, WebserviceBean wsBean, RemoteSession session) {
        this.processInstance = processInstance;
        this.wsBean = wsBean;
        this.session = session;
    }
    
    public RemoteProcessInstance getProcessInstance() {
        return processInstance;        
    }
    
    public RemoteProcessDefinition getProcessDefinition() {
        try {
            return wsBean.getProcessDefinition(processInstance.getProcessDefinition(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            NotificationsUtil.showError(ex.getMessage());
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
                NotificationsUtil.showError(ex.getMessage());
            }
        }
        return null;
    }
        
    public String getCurrentActivity() {
        RemoteActivityDefinition currentActivityDefinition = getCurrentActivityDefinition();
        
        return currentActivityDefinition != null ? currentActivityDefinition.getName() : null;
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
        return "Delete";
    }
    
    public String getEditButtonCaption() {
        if (getCurrentActivityDefinition() == null)
            return "Proceso sin Iniciar";
            
        if (getCurrentActivityDefinition().getNextActivity() == null)
            return "Proceso Finalizado";
        return "Continuar";
    }
    
    public String getViewButtonCaption() {
        return "View";
    }
}
