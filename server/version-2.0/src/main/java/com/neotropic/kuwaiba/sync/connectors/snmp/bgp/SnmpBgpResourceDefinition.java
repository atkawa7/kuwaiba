/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.sync.connectors.snmp.bgp;

import java.util.HashMap;
import org.snmp4j.smi.OID;

/**
 * This class contains the oids that will be retrieved from the agent 
 * for the BGP addresses synchronization.
 * In this case, the oids correspond to columns in the table 
 * mgmt
 *   └─mib-2
 *     └─bgp
 *       └─bgpPeerTable oid(1.3.6.1.2.1.15.3)
 * 
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SnmpBgpResourceDefinition extends HashMap<String, OID>{

    public SnmpBgpResourceDefinition() {
        put("bgpPeerIdentifier", new OID("1.3.6.1.2.1.15.3.1.1")); //The BGP Identifier of this entry's BGP peer.
        put("bgpPeerRemoteAs", new OID("1.3.6.1.2.1.15.3.1.9"));   //Remote ASN (remote autonomous system number).
        put("bgpPeerRemoteAddr", new OID("1.3.6.1.2.1.15.3.1.7")); //The remote IP address of this entry's BGP peer.  
        put("bgpPeerRemotePort", new OID("1.3.6.1.2.1.15.3.1.8")); //The remote port, not really useful but it is been set as port name in external neighbors
        put("bgpPeerLocalAddr", new OID("1.3.6.1.2.1.15.3.1.5"));  //The local IP address of this entry's BGP connection.
    }
}

