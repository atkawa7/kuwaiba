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

import org.inventory.communications.wsclient.KuwaibaService;
import org.inventory.communications.wsclient.KuwaibaService_Service;
import org.inventory.communications.wsclient.RemoteObjectLight;
import org.inventory.communications.wsclient.RemoteSession;
import org.inventory.communications.wsclient.ServerSideException_Exception;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.inventory.communications.wsclient.ClassInfo;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class KuwaibaClient {
    private static KuwaibaClient instance;
    private KuwaibaService kuwaibaService;
    private RemoteSession remoteSession;
    
    private KuwaibaClient() {
        
    }
    
    public static KuwaibaClient getInstance() {
        return instance == null ? instance = new KuwaibaClient() : instance;
    }
    
    public KuwaibaService getKuwaibaService() {
        if (kuwaibaService == null) {
            try {
                kuwaibaService = new KuwaibaService_Service(new URL("http", "localhost", 8080,"/kuwaiba/KuwaibaService?WSDL")).getKuwaibaServicePort();
                
            } catch (MalformedURLException ex) {
                Logger.getLogger(KuwaibaClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return kuwaibaService;
    }
    
    public RemoteSession getRemoteSession() {
        if (remoteSession == null) {
            try {
                remoteSession = getKuwaibaService().createSession(Variable.USER, Variable.PASS);
                
            } catch (ServerSideException_Exception ex) {
                Logger.getLogger(KuwaibaClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return remoteSession;
    }
    
    public List<RemoteObjectLight> getObjectChildren(String objectClassName, long oid) {
        try {
            return getKuwaibaService().getObjectChildren(objectClassName, oid, 0, getRemoteSession().getSessionId());
        } catch (ServerSideException_Exception ex) {
            return null;
        }
    }    
    
    public ClassInfo getClass(String className) {
        try {
            return getKuwaibaService().getClass(className, getRemoteSession().getSessionId());
        } catch (ServerSideException_Exception ex) {
            return null;
        }
    }
    
    public ClassInfo getClass(long classId) {
        try {
            return getKuwaibaService().getClassWithId(classId, getRemoteSession().getSessionId());
            
        } catch (ServerSideException_Exception ex) {
            
            return null;
        }
    }
    
    public RemoteObjectLight getObjectLight(String objectClass, long objectId) {
        try {
            return getKuwaibaService().getObjectLight(objectClass, objectId, getRemoteSession().getSessionId());
            
        } catch (ServerSideException_Exception ex) {
                        
            return null;
        }
    }
}
