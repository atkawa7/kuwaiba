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
package org.kuwaiba.apis.forms;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.forms.elements.ScriptQueryExecutor;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteScriptQuery;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteScriptQueryResult;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteScriptQueryResultCollection;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.openide.util.Exceptions;
import org.kuwaiba.beans.WebserviceBean;

/**
 * An Implementation of Script Query Executor to the Web Client of Kuwaiba
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ScriptQueryExecutorImpl implements ScriptQueryExecutor {
    private final WebserviceBean wsBean;
    private final RemoteSession session;
    
    private List<RemoteScriptQuery> scriptQueries;
    
    private RemoteProcessInstance processInstance;
////    private final List<RemoteArtifact> remoteArtifacts;
    
    public ScriptQueryExecutorImpl(WebserviceBean wsBean, RemoteSession session, RemoteProcessInstance processInstance/*, List<RemoteArtifact> remoteArtifacts*/) {
        this.wsBean = wsBean;
        this.session = session;
////        this.remoteArtifacts = remoteArtifacts;
        this.processInstance = processInstance;
        try {
            scriptQueries = wsBean.getScriptQueries(Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
            
        } catch (ServerSideException ex) {
                        
            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
    
    @Override
    public Object execute(String scriptQueryName, List<String> parameterNames, List<String> parameterValues) {
        if ("shared".equals(scriptQueryName)) {
            try {
                List<RemoteActivityDefinition> path = wsBean.getProcessInstanceActivitiesPath(
                    processInstance.getId(), 
                    Page.getCurrent().getWebBrowser().getAddress(), 
                    session.getSessionId());
              
                List<RemoteArtifact> remoteArtifacts = new ArrayList();
                
                for (RemoteActivityDefinition activity : path) {
                    try {
                        RemoteArtifact remoteArtifact = wsBean.getArtifactForActivity(
                            processInstance.getId(), 
                            activity.getId(), 
                            Page.getCurrent().getWebBrowser().getAddress(), 
                            session.getSessionId());
                        
                        remoteArtifacts.add(remoteArtifact);
                        
                    } catch (ServerSideException ex) {
                    }
                }
                
                if (!remoteArtifacts.isEmpty()) {
                    
                    List<StringPair> sharedInformation = remoteArtifacts.get(Integer.valueOf(parameterValues.get(0))).getSharedInformation();
                    
                    if (sharedInformation != null) {
                        for (StringPair pair : sharedInformation) {
                            if (parameterValues.get(1).equals(pair.getKey())) {
                                return pair.getValue();
                            }
                        }
                    }
                }
                return null;
                
            } catch (ServerSideException ex) {
                Notifications.showError(ex.getMessage());
                return null;
            }
        }
        
        if (scriptQueries != null) {
            
            for (RemoteScriptQuery scriptQuery : scriptQueries) {
                                
                if (parameterNames != null && parameterValues != null && parameterNames.size() == parameterValues.size()) {
                    try {
                        List<StringPair> newParameters = new ArrayList();
                        
                        for (int i = 0; i < parameterNames.size(); i += 1)
                            newParameters.add(new StringPair(parameterNames.get(i), parameterValues.get(i)));
                        
                        wsBean.updateScriptQueryParameters(scriptQuery.getId(), newParameters, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                        
                    } catch (ServerSideException ex) {
                        
                        Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                    }
                }
                
                if (scriptQuery.getName().equals(scriptQueryName)) {
                    
                    if ("false".equals(scriptQuery.getCountable())) {
                        try {
                            RemoteScriptQueryResult scriptQueryResult = wsBean.executeScriptQuery(scriptQuery.getId(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                            return scriptQueryResult.getResult();
                            
                        } catch (ServerSideException ex) {
                            
                            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                        }
                    }
                    
                    if ("true".equals(scriptQuery.getCountable())) {
                        try {
                            RemoteScriptQueryResultCollection scriptQueryResultCollection = wsBean.executeScriptQueryCollection(scriptQuery.getId(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                            return scriptQueryResultCollection.getResults();
                            
                        } catch (ServerSideException ex) {
                            
                            Notification.show(ex.getMessage(), Notification.Type.ERROR_MESSAGE);
                        }
                    }
                }
            }
        }
        return null;
    }
    
}
