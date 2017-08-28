/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package com.neotripic.snmp;

/**
 * SNMP Configuration class used to set the information necessary to manage an
 * agent and get the OIDs values.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SNMPConfig {
    /**
     * The address e.g. udp:127.0.0.1/161
     */
    String host;
    String community;
    String oid;
    
    public SNMPConfig(String host, String community, String oid) {
        this.host = host;
        this.community = community;
        this.oid = oid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
    
}
