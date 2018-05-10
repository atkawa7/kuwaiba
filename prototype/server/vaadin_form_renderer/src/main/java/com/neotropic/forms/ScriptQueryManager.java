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
package com.neotropic.forms;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.inventory.communications.wsclient.ClassInfoLight;
import org.inventory.communications.wsclient.RemoteObjectLight;
import org.inventory.communications.wsclient.RemoteScriptQuery;
import org.inventory.communications.wsclient.RemoteScriptQueryResult;
import org.inventory.communications.wsclient.RemoteScriptQueryResultCollection;
import org.inventory.communications.wsclient.ServerSideException_Exception;
import org.inventory.communications.wsclient.StringPair;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ScriptQueryManager {
    HashMap<String, RemoteScriptQuery> scriptQueryMap = new HashMap();
    private static ScriptQueryManager instance;
    
    private ScriptQueryManager() {
    }    
    
    public static ScriptQueryManager getInstance() {
        return instance == null ? instance = new ScriptQueryManager() : instance;                
    }
    
    public boolean hasScriptQuery(String scriptQueryName) {
        return scriptQueryMap != null ? scriptQueryMap.containsKey(scriptQueryName) : false;
    }
    
    public RemoteScriptQuery getScriptQuery(String scriptQueryName) {
        return scriptQueryMap != null ? scriptQueryMap.get(scriptQueryName) : null;
    }
    
    public void loadScriptQueryFiles() {
        List<RemoteScriptQuery> scriptQueries = getScriptQueries();
        
        if (scriptQueries == null)
            return;
        
        scriptQueryMap = new HashMap();
        
        for (RemoteScriptQuery scriptQuery : scriptQueries) {
            scriptQueryMap.put(scriptQuery.getName(), scriptQuery);
        }
                
        File folder = new File(Variable.FORM_RESOURCE_SCRIPT_QUERIES);
        
        File[] files = folder.listFiles();
        
        for (File file : files) {
                        
            String fileName = file.getName();
                        
            if (scriptQueryMap.containsKey(fileName))
                continue;
                        
            try {
                Scanner in;
                
                in = new Scanner(file);
                
                String script = "";

                while (in.hasNext())
                    script += "\n" + in.nextLine();
                
                String[] params = fileName.split("\\.");
                
                List<StringPair> parameters = new ArrayList();
                
                for (int i = 1; i < params.length; i += 1) {
                    
                    StringPair parameter = new StringPair();
                    parameter.setKey(params[i]);
                    parameter.setValue(params[i]);
                    parameters.add(parameter);
                }
                
                createScriptQuery(fileName, "", script, parameters);
                
            } catch (FileNotFoundException ex) {
                
            }
        }
    }
    
    public boolean createScriptQuery(String name, String description, String script, List<StringPair> parameters) {
        try {
            
            KuwaibaClient.getInstance().getKuwaibaService().createScriptQuery(name, description, script, parameters, 
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
            return true;
            
        } catch (ServerSideException_Exception ex) {
            
            return false;
        }
    }
    
    public boolean deleteScriptQuery(long scriptQueryId) {
        try {
            
            KuwaibaClient.getInstance().getKuwaibaService().deleteScriptQuery(scriptQueryId, 
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
            return true;
            
        } catch (ServerSideException_Exception ex) {
            
            return false;
        }
    }
    
    public List<RemoteScriptQuery> getScriptQueries() {
        try {
            
            return KuwaibaClient.getInstance().getKuwaibaService().getScriptQueries(
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
                        
        } catch (ServerSideException_Exception ex) {
            
            return null;
        }
    }
    
    public RemoteScriptQuery getScriptQuery(long scriptQueryId) {
        try {
            
            return KuwaibaClient.getInstance().getKuwaibaService().getScriptQuery(scriptQueryId, 
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
            
        } catch (ServerSideException_Exception ex) {
            
            return null;
        }
    }
        
    public RemoteScriptQueryResult executeScriptQuery(long scriptQueryId) {
        try {
            
            return KuwaibaClient.getInstance().getKuwaibaService().executeScriptQuery(scriptQueryId, 
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
            
        } catch (ServerSideException_Exception ex) {
            
            return null;
        }
    }
    
    public RemoteScriptQueryResultCollection executeScriptQueryCollection(long scriptQueryId) {
        try {
            return KuwaibaClient.getInstance().getKuwaibaService().executeScriptQueryCollection(scriptQueryId, 
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
        
        }   catch (ServerSideException_Exception ex) {
            return null;
        }
    }
        
    public boolean updateScriptQueryParameters(long scriptQueryId, List<StringPair> parameters) {
        try {
            
            KuwaibaClient.getInstance().getKuwaibaService().updateScriptQueryParameters(scriptQueryId, parameters, 
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
            return true;
            
        } catch (ServerSideException_Exception ex) {
            
            return false;
        }
    }
    
    public List<RemoteObjectLight> getListTypeItems(String className) {
        try {
            
            return KuwaibaClient.getInstance().getKuwaibaService().getListTypeItems(className, 
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
            
        } catch (ServerSideException_Exception ex) {
            
            return null;
        }
    }
    
    public List<ClassInfoLight> getSubClassesLight(String className, boolean includeAbstractClasses, boolean includeSelf) {
        try {
            
            return KuwaibaClient.getInstance().getKuwaibaService().getSubClassesLight(className, includeAbstractClasses, includeSelf, 
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
            
        } catch (ServerSideException_Exception ex) {
            
            return null;
        }
    }
    
    public List<RemoteObjectLight> getSpecialAttribute(String objectClassName, long objectId, String attributeName) {
        try {
            return KuwaibaClient.getInstance().getKuwaibaService().getSpecialAttribute(objectClassName, objectId, attributeName,
                    KuwaibaClient.getInstance().getRemoteSession().getSessionId());
        } catch (ServerSideException_Exception ex) {
            
            return null;
        }
    }
        
    public Object excecuteScriptQuery(String scriptQueryName, HashMap<String, String> parameters) {
        if (!scriptQueryMap.containsKey(scriptQueryName))
            return String.format("The Script Query with name %s not exist", scriptQueryName);
        
        RemoteScriptQuery remoteScriptQuery = scriptQueryMap.get(scriptQueryName);
        
        List<StringPair> params = remoteScriptQuery.getParameters();
        
        if (params != null && parameters != null) { 
            
            if (params.size() != parameters.size())
                return String.format("The number of parameters no match", scriptQueryName);
            
            for (StringPair param : params) {
                if (parameters.containsKey(param.getKey()))
                    param.setValue(parameters.get(param.getKey()));
            }
            
            updateScriptQueryParameters(remoteScriptQuery.getId(), params);
        }
                
        RemoteScriptQueryResult rsqr = executeScriptQuery(remoteScriptQuery.getId());

        if (rsqr != null)
            return rsqr.getResult();
        
        return null;
    }
    
    public List executeScriptQueryCollection(String scriptQueryName, HashMap<String, String> parameters) {
        if (!scriptQueryMap.containsKey(scriptQueryName)) {
            List<String> result = new ArrayList();            
            result.add(String.format("The Script Query with name %s not exist", scriptQueryName));
            return result;
        }
        RemoteScriptQuery remoteScriptQuery = scriptQueryMap.get(scriptQueryName);
        
        List<StringPair> params = remoteScriptQuery.getParameters();
        
        if (params != null && parameters != null) { 
            
            if (params.size() != parameters.size()) {
                List<String> result = new ArrayList();            
                result.add(String.format("The number of parameters no match", scriptQueryName));
                return result;
            }
            for (StringPair param : params) {
                if (parameters.containsKey(param.getKey()))
                    param.setValue(parameters.get(param.getKey()));
            }
            
            updateScriptQueryParameters(remoteScriptQuery.getId(), params);
        }
                
        RemoteScriptQueryResultCollection rsqr = executeScriptQueryCollection(remoteScriptQuery.getId());

        if (rsqr != null)
            return rsqr.getResult();
        
        return null;
    }
}
