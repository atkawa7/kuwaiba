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

import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import java.util.HashMap;
import java.util.List;

/**
 * SNMP data entry configuration
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SnmpDataEntry extends AbstractDataEntity {
    
    public SnmpDataEntry(String name, HashMap<String, List<String>> value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public DATA_TYPE getType() {
        return DATA_TYPE.TABLE;
    }
}
