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
package com.neotropic.job.schedule;

import java.util.Properties;
import javax.ejb.Remote;

/**
 * A remote bean to run jobs from client application e.g. a web application
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@Remote
public interface RunJobRemote {
    
    /**
     * Runs the job snmpLeaf
     * @param properties a set of properties necessary to manage the agent
     * @return job execution id
     */
    public long runSnmpLeafJob(Properties properties);    
    
    /**
     * Runs the job snmpTable
     * @param properties a set of properties necessary to manage the agent
     * @return execution id
     */
    public long runSnmpTableJob(Properties properties);
    
    /**
     * Gets the job batch status
     * @param executionId job execution id
     * @return exit status
     */
    public String jobStatus(long executionId);
}
