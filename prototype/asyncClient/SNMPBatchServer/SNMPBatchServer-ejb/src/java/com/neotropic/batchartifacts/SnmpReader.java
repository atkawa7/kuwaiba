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
import java.io.Serializable;
import java.util.Properties;
import javax.batch.api.chunk.AbstractItemReader;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@Named("SnmpReader")
public class SnmpReader extends AbstractItemReader {
    long jobExecutionId = -1;
    @Inject
    private JobContext jobContext;
    
    @Override
    public void open(Serializable e) throws Exception {
    }

    @Override
    public Object readItem() throws Exception {
//        System.out.println(">>> SnmpReader::readItem");
        
        if (jobExecutionId == jobContext.getExecutionId())
            return null;
        
        jobExecutionId = jobContext.getExecutionId();
        
        Properties properties = BatchRuntime.getJobOperator().getParameters(jobExecutionId);
        String host = properties.getProperty("host");
        String community = properties.getProperty("community");
        String oid = properties.getProperty("oid");
                
        if (host != null && community != null && oid != null)
            return new SNMPConfig(host, community, oid);
        return null; // if are null end the job
    }
    
}
