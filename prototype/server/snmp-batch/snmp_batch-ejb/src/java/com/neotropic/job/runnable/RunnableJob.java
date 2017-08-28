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
package com.neotropic.job.runnable;

import java.util.Properties;
import javax.batch.runtime.BatchRuntime;

/**
 * A Runnable Job is used to execute a job or execute a job using a schedule strategy
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RunnableJob implements Runnable {
    
    private String jobXMLName;
    private Properties properties;
    
    public RunnableJob(String jobXMLName, Properties properties) {
        this.jobXMLName = jobXMLName;
        this.properties = properties;                
    }
    
    public String getJobXMLName() {
        return jobXMLName;
    }
    
    public void setobXMLName(String jobXMLName) {
        this.jobXMLName = jobXMLName;        
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public void setProperties(Properties properties) {
        this.properties = properties;        
    }

    @Override
    public void run() {
        BatchRuntime.getJobOperator().start(jobXMLName, properties);
    }
    
}
