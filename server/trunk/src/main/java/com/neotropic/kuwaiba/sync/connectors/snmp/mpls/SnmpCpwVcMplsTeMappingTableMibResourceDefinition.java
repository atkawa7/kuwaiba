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
 * This Oids are used to relate tunnels with its IPs addresses
 * the instance of this table has the tunnel id and its IPs addresses
 * e.g. 1.610.0.185.35.140.22.185.35.140.9.66
 *         |    |____________________________| 
 *         |      source-destiny IP addresses
 *      Tunnel id 
 * 
 * private
 *   └─enterprises
 *     └─cisco
 *       └─ciscoExperiment 
 *         └─cpwVcMplsMIB
 *           └─cpwVcMplsObjects
 *              └─cpwVcMplsTeMappingTable
 * 
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SnmpCpwVcMplsTeMappingTableMibResourceDefinition extends HashMap<String, OID>{

    public SnmpCpwVcMplsTeMappingTableMibResourceDefinition() {
        //This index is the relationship with the instance in the cpwVcTable
        put("cpwVcMplsTeMappingVcIndex", new OID("1.3.6.1.4.1.9.10.107.1.7.1.6"));
    }
}
