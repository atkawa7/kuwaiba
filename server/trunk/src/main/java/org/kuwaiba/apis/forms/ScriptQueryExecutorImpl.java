/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

import com.neotropic.kuwaiba.modules.reporting.img.SceneExporter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.FileInformation;
import org.kuwaiba.apis.forms.elements.ScriptQueryExecutor;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.apis.web.gui.notifications.Notifications;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.openide.util.Exceptions;

/**
 * An Implementation of Script Query Executor to the Web Client of Kuwaiba
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ScriptQueryExecutorImpl implements ScriptQueryExecutor {
    private final WebserviceBean wsBean;
    private final RemoteSession session;
    private final RemoteProcessInstance processInstance;
    private boolean debug = false;
        
    public ScriptQueryExecutorImpl(WebserviceBean wsBean, RemoteSession session, RemoteProcessInstance processInstance) {
        this.wsBean = wsBean;
        this.session = session;
        this.processInstance = processInstance;
    }
    
    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
    
    @Override
    public Object execute(String scriptQueryName, List<String> parameterNames, List<String> parameterValues) {
        if ("activityConditionalValue".equals(scriptQueryName) && parameterValues != null && parameterValues.size() == 1) {
            String paramValue0 = parameterValues.get(0);
            try {
                RemoteArtifact remoteArtifact = wsBean.getArtifactForActivity(processInstance.getId(), Long.valueOf(paramValue0), session.getIpAddress(), session.getSessionId());
                String content = new String(remoteArtifact.getContent());
                return content.contains("true");
            } catch (ServerSideException | NumberFormatException ex) {
                return false;
            }
        }
        if ("activityHasArtifact".equals(scriptQueryName) && parameterValues != null && parameterValues.size() == 1) { //NOI18N
            try {
                String paramValue0 = parameterValues.get(0);
                RemoteArtifact artifact = wsBean.getArtifactForActivity(processInstance.getId(), Long.valueOf(paramValue0), session.getIpAddress(), session.getSessionId());
                return artifact != null;
            } catch (ServerSideException ex) {
                return false;
            }
        }
        // The Keyword "shared" is used as Function Name to get to the execution 
        // of a Script Query the Artifacts shared values
        if ("shared".equals(scriptQueryName) && parameterValues != null && parameterValues.size() >= 1) { //NOI18N
            
            String paramValue0 = parameterValues.get(0);
            
            if (paramValue0.equals("__processInstanceId__")) //NOI18N
                return String.valueOf(processInstance.getId());                        
            if (paramValue0.equals("__userName__") && session != null) //NOI18N
                return session.getUsername();
            
            if (parameterValues.size() == 2 || parameterValues.size() == 3) {
                long activityId = Long.valueOf(paramValue0);
                String sharedId = parameterValues.get(1);
                long processInstanceId = processInstance.getId();
                
                if (parameterValues.size() == 3)
                    processInstanceId = Long.valueOf(parameterValues.get(2));
                                                
                RemoteArtifact remoteArtifact = null;
                
                try {
                    
                    List<RemoteActivityDefinition> path = wsBean.getProcessInstanceActivitiesPath(
                        processInstanceId, 
                        session.getIpAddress(), 
                        session.getSessionId());

                    for (RemoteActivityDefinition activity : path) {

                        if (activity.getId() == activityId) {

                            remoteArtifact = wsBean.getArtifactForActivity(
                                processInstanceId, 
                                activity.getId(), 
                                session.getIpAddress(), 
                                session.getSessionId());
                            break;
                        }
                    }
                } catch (ServerSideException ex) {
                    if (debug)
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
                                    String objId = sharedInfo.getProperty(sharedId + Constants.Attribute.OBJECT_ID);
                                    
                                    try {                                    
                                        return wsBean.getObjectLight(objClassName, objId, session.getIpAddress(), session.getSessionId());
                                        
                                    } catch (ServerSideException ex) {
                                        if (debug)
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
        else if ("notifications".equals(scriptQueryName) && //NOI18N
            parameterNames != null && parameterNames.size() >= 1 &&
            parameterValues != null && parameterValues.size() >= 1 &&
            parameterNames.size() == parameterValues.size()) {
            
            for (int i = 0; i < parameterNames.size(); i++) {
                String parameterName = parameterNames.get(i);
                
                if (null != parameterName) {
                    switch (parameterName) {
                        case "error": //NOI18N
                            Notifications.showError(parameterValues.get(i));
                            break;
                        case "info": //NOI18N
                            Notifications.showInfo(parameterValues.get(i));
                            break;
                        case "warning": //NOI18N
                            Notifications.showWarning(parameterValues.get(i));
                            break;
                        default:
                            break;
                    }
                }
            }
        } else if ("endToEndViewAsByteArray".equals(scriptQueryName)&& //NOI18N
            parameterNames != null && parameterNames.size() == 2 &&
            parameterValues != null && parameterValues.size() == 2 &&
            parameterNames.size() == parameterValues.size()) {
            /*parameterNames ["id", "className"];
            parameterValues ["service.getClassName()", "service.getId()"];*/
            
            String processEnginePath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processEnginePath")); //NOI18N
            String newPath = processEnginePath + "/temp/";
            
            String oldPath = SceneExporter.PATH;
            SceneExporter.PATH = newPath;
            String pathEndToEndView = SceneExporter.getInstance().buildEndToEndView(
                session, 
                wsBean, 
                parameterValues.get(0), 
                parameterValues.get(1));
            SceneExporter.PATH = oldPath;
            try {
                if (pathEndToEndView != null)
                    return Files.readAllBytes(new File(newPath + pathEndToEndView).toPath());
                
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }        
        else {
            Notifications.showError("[PROCESS ENGINE] Function " + scriptQueryName + " does not exist");            
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
                            String objectId = sharedInfo.getProperty(sharedId + Constants.Attribute.OBJECT_ID);
                            String className = sharedInfo.getProperty(sharedId + Constants.Attribute.CLASS_NAME);
                            
                            try {
                                
                                RemoteObjectLight rol = wsBean.getObjectLight(
                                    className, 
                                    objectId,
                                    session.getIpAddress(), 
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
            // Expected NumberFormatException or another parse error of an data type no supported in grids                        
        }
                                
        return null;                
    }    
        
}
