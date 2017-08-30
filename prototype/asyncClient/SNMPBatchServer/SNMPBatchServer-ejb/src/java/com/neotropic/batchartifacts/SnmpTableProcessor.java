/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.batchartifacts;

import com.neotripic.snmp.SNMPConfig;
import com.neotripic.snmp.SNMPManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.batch.api.chunk.ItemProcessor;
import javax.inject.Named;
import org.snmp4j.smi.OID;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@Named
public class SnmpTableProcessor  implements ItemProcessor {
    
    @Override
    public Object processItem(Object item) throws Exception {
//        System.out.println(">>> SnmpTableProcessor::processItem ");
        TimeUnit.SECONDS.sleep(5);
        if (item != null) {
            SNMPConfig config = (SNMPConfig) item;
            
            SNMPManager client = SNMPManager.getInstance();
            
            client.setAddress(config.getHost());
            client.setCommunity(config.getCommunity());
            
            String oid = config.getOid();
            String [] strOids = oid.split(",");
            List<OID> oids = new ArrayList();
            for (String strOid : strOids)
                oids.add(new OID(strOid));
            
            return client.getTableAsString(oids.toArray(new OID [0]));
        }
        return null;
    }    
}
