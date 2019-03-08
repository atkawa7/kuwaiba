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
package com.neotropic.kuwaiba.sync.connectors.ssh.mpls;

import java.util.HashMap;
import org.snmp4j.smi.OID;

/**
 * This Oids are used for MPLS synchronization 
 * private
 *  └─enterprises
 *    └─cisco
 *      └─ciscoExperiment 
 *        └─cpwVcMIB
 *          └─cpwVcObjects 
 *            └─cpwVcTable
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Deprecated
public class SnmpCpwVcTableMibResourceDefinition extends HashMap<String, OID>{

    public SnmpCpwVcTableMibResourceDefinition() {
        //name of the service
        put("cpwVcDescr", new OID("1.3.6.1.4.1.9.10.106.1.2.1.22"));
        //VC name a MPLSlink that connects two routers in MPLSViews
        put("cpwVcID", new OID("1.3.6.1.4.1.9.10.106.1.2.1.10"));
        //name of the destiny port
        put("cpwVcRemoteIfString", new OID("1.3.6.1.4.1.9.10.106.1.2.1.18"));
        //name of the source port
        put("cpwVcName", new OID("1.3.6.1.4.1.9.10.106.1.2.1.21"));
    }
}
