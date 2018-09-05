/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.kuwaiba.sync.connectors.snmp.mpls;

import java.util.HashMap;
import org.snmp4j.smi.OID;

/**
 * 
 * 
 * cpwVcMplsInboundTable
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SnmpCpwVcMplsInboundTableResourceDefinition extends HashMap<String, OID>{

    public SnmpCpwVcMplsInboundTableResourceDefinition() {
        put("cpwVcMplsInboundLsrXcIndex", new OID("1.3.6.1.4.1.9.10.107.1.5.1.2"));
        put("cpwVcMplsInboundTunnelIndex", new OID("1.3.6.1.4.1.9.10.107.1.5.1.3"));
    }
}
