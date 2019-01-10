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
package com.neotropic.kuwaiba.sync.connectors.snmp.reference;

import java.util.HashMap;
import org.snmp4j.smi.OID;

/**
 * Oids that will be retrieved from the agent for 
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class SnmpifXTableResocurceDefinition extends HashMap<String, OID>{

    public SnmpifXTableResocurceDefinition() {
        put("ifName", new OID("1.3.6.1.2.1.31.1.1.1.1"));
        put("ifAlias", new OID("1.3.6.1.2.1.31.1.1.1.18"));
        put("ifHighSpeed", new OID("1.3.6.1.2.1.31.1.1.1.15"));
    }
}

