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

import com.neotrppic.wsclient.KuwaibaService;
import com.neotrppic.wsclient.KuwaibaService_Service;
import com.neotrppic.wsclient.RemoteSession;
import com.neotrppic.wsclient.ServerSideException_Exception;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

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
                remoteSession = getKuwaibaService().createSession("username", "password");
                
            } catch (ServerSideException_Exception ex) {
                Logger.getLogger(KuwaibaClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return remoteSession;
    }
}
