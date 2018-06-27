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
package com.neotropic.kuwaiba.sync.connectors.snmp.cisco;

import java.util.HashMap;
import org.snmp4j.smi.OID;

/**
 * cpwVcTable
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class SnmpCpwVcTableMibResourceDefinition extends HashMap<String, OID>{

    public SnmpCpwVcTableMibResourceDefinition() {
        put("cpwVcDescr", new OID("1.3.6.1.4.1.9.10.106.1.2.1.22"));
        put("cpwVcID", new OID("1.3.6.1.4.1.9.10.106.1.2.1.10"));
        put("cpwVcRemoteIfString", new OID("1.3.6.1.4.1.9.10.106.1.2.1.18"));
        put("cpwVcName", new OID("1.3.6.1.4.1.9.10.106.1.2.1.21"));
    }
}
