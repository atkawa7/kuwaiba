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
package com.neotropic.ws;

import com.neotropic.job.schedule.RunJobRemote;
import java.util.Properties;
import javax.batch.runtime.BatchStatus;
import javax.ejb.EJB;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@WebService(serviceName = "SimpleWebService")
public class SimpleWebService {
    
    @EJB
    private RunJobRemote runJob;
    /**
     * Runs a SNMP job to get a leaf value
     * @param properties The properties to execute the job
     * @return the end status of the job
     */
    @WebMethod(operationName = "runJobSnmpLeaf")
    public String runJobSnmpLeaf(@WebParam(name = "properties") Properties properties) {
        long executionId = runJob.runSnmpLeafJob(properties);                        
        String batchStatus = runJob.jobStatus(executionId);
        // TODO: Add another status in the conditions to end the execution e.g. ABANDONED or FAILED
        while (!BatchStatus.COMPLETED.name().equals(batchStatus) && 
               !BatchStatus.FAILED.name().equals(batchStatus)) // loop while the job is not ending
            batchStatus = runJob.jobStatus(executionId);
        return batchStatus;
    }
    
    /**
     * Runs a SNMP job to get a table record
     * @param properties The properties to execute the job
     * @return the end status of the job
     */
    @WebMethod(operationName = "runJobSnmpTable")
    public String runJobSnmpTable(@WebParam(name = "properties") Properties properties) {
        long executionId = runJob.runSnmpTableJob(properties);
        String batchStatus = runJob.jobStatus(executionId);
        // TODO: Add another status in the conditions to end the execution e.g. ABANDONED or FAILED
        while (!BatchStatus.COMPLETED.name().equals(batchStatus) && 
               !BatchStatus.FAILED.name().equals(batchStatus)) // loop while the job is not ending
            batchStatus = runJob.jobStatus(executionId);
        return batchStatus;            
    }
}
