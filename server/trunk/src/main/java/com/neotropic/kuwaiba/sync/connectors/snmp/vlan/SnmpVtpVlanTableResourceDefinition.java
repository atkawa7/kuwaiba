/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.sync.connectors.snmp.vlan;

import java.util.HashMap;
import org.snmp4j.smi.OID;

/**
 * List of VLANs configured in a switch
 * ciscoMgmt/ciscoVtpMIB/vtpMIBObjects/vtpVlanTable 
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SnmpVtpVlanTableResourceDefinition extends HashMap<String, OID>{

    public SnmpVtpVlanTableResourceDefinition() {
        put("vtpVlanName", new OID("1.3.6.1.4.1.9.9.46.1.3.1.1.4"));
    }
}
