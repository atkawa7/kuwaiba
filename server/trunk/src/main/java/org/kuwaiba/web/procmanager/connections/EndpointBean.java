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
package org.kuwaiba.web.procmanager.connections;

import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class EndpointBean {
    private RemoteObjectLight endpointA;
    private RemoteObjectLight endpointB;
    
    public EndpointBean() {
                
    }
    
    public EndpointBean(RemoteObjectLight endpointA, RemoteObjectLight endpointB) {
        this.endpointA = endpointA;                
        this.endpointB = endpointB;
    }
    
    public RemoteObjectLight getEndpointA() {
        return endpointA;
    }
    
    public void setEndpointA(RemoteObjectLight endpointA) {
        this.endpointA = endpointA;
    }
    
    public RemoteObjectLight getEndpointB() {
        return endpointB;
    }
    
    public void setEndpointB(RemoteObjectLight endpointB) {
        this.endpointB = endpointB;
    }
}