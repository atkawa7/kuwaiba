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
 * ciscoMgmt
 *       └─ciscoVtpMIB
 *                └─vtpMIBObjects
 *                             └─vlanTrunkPorts
 * @author Adrian Martinez {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SnmpVlanTrunkPortsTableResourceDefinition extends HashMap<String, OID>{
    //Here you find the info to sync the VLANs for ports in trunk mode
    public SnmpVlanTrunkPortsTableResourceDefinition() {
        put("vlanTrunkPortVlansEnabled", new OID("1.3.6.1.4.1.9.9.46.1.6.1.1.4")); //
        put("vlanTrunkPortVlansEnabled2k", new OID("1.3.6.1.4.1.9.9.46.1.6.1.1.17"));
        put("vlanTrunkPortVlansEnabled3k", new OID("1.3.6.1.4.1.9.9.46.1.6.1.1.18"));
        put("vlanTrunkPortVlansEnabled4k", new OID("1.3.6.1.4.1.9.9.46.1.6.1.1.19"));
    }
}
