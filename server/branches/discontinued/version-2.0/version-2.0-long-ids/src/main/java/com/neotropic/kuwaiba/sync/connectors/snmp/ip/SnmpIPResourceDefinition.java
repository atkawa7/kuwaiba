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
package com.neotropic.kuwaiba.sync.connectors.snmp.ip;

import java.util.HashMap;
import org.snmp4j.smi.OID;

/**
 * This class contains the oids that will be retrieved from the agent for 
 * the IP addresses synchronization.
 * In this case, the oids correspond to columns in the table 
 * mgmt
 *   └─mib-2
 *     └─ip
 *       └─ipAddrTable oid(1.3.6.1.2.1.4.20)
 * 
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SnmpIPResourceDefinition extends HashMap<String, OID>{

    public SnmpIPResourceDefinition() {
        put("ipAdEntAddr", new OID("1.3.6.1.2.1.4.20.1.1"));
        put("ipAdEntIfIndex", new OID("1.3.6.1.2.1.4.20.1.2"));
        put("ipAdEntNetMask", new OID("1.3.6.1.2.1.4.20.1.3"));
    }
}

