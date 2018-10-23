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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.FileInformation;
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
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 * An Implementation of Script Query Executor to the Web Client of Kuwaiba
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ScriptQueryExecutorImpl implements ScriptQueryExecutor {
    private final WebserviceBean wsBean;
    private final RemoteSession session;
    private final RemoteProcessInstance processInstance;
    private final List<RemoteScriptQuery> scriptQueries;
        
    public ScriptQueryExecutorImpl(WebserviceBean wsBean, RemoteSession session, RemoteProcessInstance processInstance) {
        this.wsBean = wsBean;
        this.session = session;
        
        this.processInstance = processInstance;
        
        List<RemoteScriptQuery> remoteScriptQueries = null;
        try {
            remoteScriptQueries = wsBean.getScriptQueries(Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
        } catch (ServerSideException ex) {
            Notifications.showError(ex.getMessage());
        }
        scriptQueries = remoteScriptQueries != null ? remoteScriptQueries : new ArrayList();
    }
    
    @Override
    public Object execute(String scriptQueryName, List<String> parameterNames, List<String> parameterValues) {
        // The Keyword "shared" is used as Function Name to get to the execution 
        // of a Script Query the Artifacts shared values
        if ("shared".equals(scriptQueryName) && parameterValues != null && parameterValues.size() >= 1) {
            
            String paramValue0 = parameterValues.get(0);
            
            if (paramValue0.equals("__processInstanceId__"))
                return String.valueOf(processInstance.getId());
            
            if (parameterValues.size() == 2) {
                                                
                long activityId = Long.valueOf(paramValue0);
                String sharedId = parameterValues.get(1);
                                
                RemoteArtifact remoteArtifact = null;
                
                try {
                    
                    List<RemoteActivityDefinition> path = wsBean.getProcessInstanceActivitiesPath(
                        processInstance.getId(), 
                        Page.getCurrent().getWebBrowser().getAddress(), 
                        session.getSessionId());

                    for (RemoteActivityDefinition activity : path) {

                        if (activity.getId() == activityId) {

                            remoteArtifact = wsBean.getArtifactForActivity(
                                processInstance.getId(), 
                                activity.getId(), 
                                Page.getCurrent().getWebBrowser().getAddress(), 
                                session.getSessionId());
                            break;
                        }
                    }
                } catch (ServerSideException ex) {
                    Notifications.showError(ex.getMessage());
                    return null;
                }
                if (remoteArtifact != null) {
                    
                    List<StringPair> sharedInformation = remoteArtifact.getSharedInformation();

                    if (sharedInformation != null) {
                        Properties sharedInfo = new Properties();

                        for (StringPair pair : sharedInformation)
                            sharedInfo.setProperty(pair.getKey(), pair.getValue());
                        
                        if (isGrid(sharedId, sharedInfo)) {
                            return getRows(sharedId, sharedInfo);

                        } else if (sharedInfo.containsKey(sharedId)) {
                            return sharedInfo.getProperty(sharedId);

                        } else if (sharedInfo.containsKey(sharedId + Constants.Attribute.DATA_TYPE)) {
                            if (Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH.equals(sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE))) {
                                                                                                
                                if (sharedInfo.containsKey(sharedId + Constants.Attribute.CLASS_NAME) && 
                                    sharedInfo.containsKey(sharedId + Constants.Attribute.OBJECT_ID) && 
                                    sharedInfo.containsKey(sharedId + Constants.Attribute.OBJECT_NAME)) {
                                    
                                    String objClassName = sharedInfo.getProperty(sharedId + Constants.Attribute.CLASS_NAME);
                                    long objId = Long.valueOf(sharedInfo.getProperty(sharedId + Constants.Attribute.OBJECT_ID));
                                    
                                    try {                                    
                                        return wsBean.getObjectLight(objClassName, objId, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                                        
                                    } catch (ServerSideException ex) {
                                        
                                        Notifications.showError(ex.getMessage());
                                        return sharedInfo.get(sharedId + Constants.Attribute.OBJECT_NAME);
                                    }
                                }
                            }
                            if (Constants.Attribute.DataType.ATTACHMENT.equals(sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE))) {
                                if (sharedInfo.containsKey(sharedId + Constants.Attribute.NAME) && 
                                    sharedInfo.containsKey(sharedId + Constants.Attribute.PATH)) {
                                    
                                    return new FileInformation(
                                        sharedInfo.getProperty(sharedId + Constants.Attribute.NAME), 
                                        sharedInfo.getProperty(sharedId + Constants.Attribute.PATH)
                                    );
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
        
        if (scriptQueries != null) {
            
            for (RemoteScriptQuery scriptQuery : scriptQueries) {
                // Finds the Script Query to execute
                if (scriptQuery.getName().equals(scriptQueryName)) {
                    // Checks if the arrays to parameters match in size
                    if (parameterNames != null && parameterValues != null && parameterNames.size() == parameterValues.size()) {
                        try {
                            List<StringPair> newParameters = new ArrayList();

                            for (int i = 0; i < parameterNames.size(); i += 1)
                                newParameters.add(new StringPair(parameterNames.get(i), parameterValues.get(i)));
                            // Updating the parameters
                            wsBean.updateScriptQueryParameters(scriptQuery.getId(), newParameters, Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                            
                            if ("false".equals(scriptQuery.getCountable())) {
                                // Excecuting the Script Query to No Countable result
                                RemoteScriptQueryResult scriptQueryResult = wsBean.executeScriptQuery(
                                    scriptQuery.getId(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                                
                                return scriptQueryResult.getResult();
                            }
                            if ("true".equals(scriptQuery.getCountable())) {
                                // Excecuting the Script Query to Countable results
                                RemoteScriptQueryResultCollection scriptQueryResultCollection = wsBean.executeScriptQueryCollection(
                                    scriptQuery.getId(), Page.getCurrent().getWebBrowser().getAddress(), session.getSessionId());
                                
                                return scriptQueryResultCollection.getResults();
                            }

                        } catch (ServerSideException ex) {
                            Notifications.showError(ex.getMessage());
                        }
                    }
                }
            }
        }
        return null;
    }
    
    private boolean isGrid(String gridId, Properties sharedInfo) {
        final String ROWS_COUNT = "rowscount"; //NOI18N
        final String COLUMNS_COUNT = "columnscount"; //NOI18N
        
        return gridId != null && sharedInfo != null && 
               sharedInfo.containsKey(gridId + ROWS_COUNT) && 
               sharedInfo.containsKey(gridId + COLUMNS_COUNT);
    }
    
    private List<List<Object>> getRows(String gridId, Properties sharedInfo) {
        final String ROWS_COUNT = "rowscount"; //NOI18N
        final String COLUMNS_COUNT = "columnscount"; //NOI18N
        try {
            int rowCount = Integer.valueOf(sharedInfo.getProperty(gridId + ROWS_COUNT));
            int columnCount = Integer.valueOf(sharedInfo.getProperty(gridId + COLUMNS_COUNT));
            
            if (rowCount > 0 && columnCount > 0) {
                List<List<Object>> rows = new ArrayList();
                
                for (int i = 0; i < rowCount; i += 1) {
                    List<Object> row = new ArrayList();
                    
                    for (int j = 0; j < columnCount; j += 1) {
                        String sharedId = gridId + i + j;
                        
                        if (Constants.Attribute.DataType.REMOTE_OBJECT_LIGTH.equals(
                            sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE))) {
                                                        
                            String objectName = sharedInfo.getProperty(sharedId + Constants.Attribute.OBJECT_NAME);
                            long objectId = Long.valueOf(sharedInfo.getProperty(sharedId + Constants.Attribute.OBJECT_ID));
                            String className = sharedInfo.getProperty(sharedId + Constants.Attribute.CLASS_NAME);
                            
                            try {
                                
                                RemoteObjectLight rol = wsBean.getObjectLight(
                                    className, 
                                    objectId,
                                    Page.getCurrent().getWebBrowser().getAddress(), 
                                    session.getSessionId());
                                
                                row.add(rol != null ? rol : (objectName != null ? objectName : ""));
                            } catch (Exception exception) {
                                row.add(sharedInfo.get(sharedId) != null ? sharedInfo.get(sharedId) : "");
                            }
                        } else if (Constants.Attribute.DataType.STRING.equals(
                            sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE))) {
                            
                            row.add(sharedInfo.get(sharedId) != null ? sharedInfo.get(sharedId) : "");
                        } else if (Constants.Attribute.DataType.ATTACHMENT.equals(
                            sharedInfo.getProperty(sharedId + Constants.Attribute.DATA_TYPE))) {
                            
                            String name = sharedInfo.getProperty(sharedId + Constants.Attribute.NAME);
                            String path = sharedInfo.getProperty(sharedId + Constants.Attribute.PATH);
                            
                            if (name != null && path != null) {
                                FileInformation fileInformation = new FileInformation(name, path);
                                row.add(fileInformation);
                            }
                            else {
                                row.add(sharedInfo.get(sharedId) != null ? sharedInfo.get(sharedId) : "");
                            }
                        }
                    }
                    rows.add(row);
                }
                return rows;
            }            
        } catch(Exception ex) {
                        
        }
                                
        return null;                
    }    
        
}
