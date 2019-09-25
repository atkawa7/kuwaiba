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
package com.neotropic.kuwaiba.sync.connectors.snmp.test;

import java.util.HashMap;
import org.snmp4j.smi.OID;

/**
 * Contains the OIDs that will be retrieved from the agent for this reference implementation. 
 * In this case, the OIDs correspond to columns in the tables  entPhysicalTable (branch 1.3.6.1.2.1.47.1.1.1.)
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ReferenceSnmpTesterTableResourceDefinition extends HashMap<String, OID>{

    public ReferenceSnmpTesterTableResourceDefinition() {
        put("entPhysicalClass", new OID("1.3.6.1.2.1.47.1.1.1.1.5")); //NOI18N
        put("entPhysicalContainedIn", new OID("1.3.6.1.2.1.47.1.1.1.1.4")); //NOI18N
    }
}

