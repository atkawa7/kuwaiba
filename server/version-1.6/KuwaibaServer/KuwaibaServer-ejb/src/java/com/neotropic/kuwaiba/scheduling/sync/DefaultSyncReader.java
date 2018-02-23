/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.neotropic.kuwaiba.scheduling.sync;

import com.neotropic.kuwaiba.scheduling.BackgroundJob;
import com.neotropic.kuwaiba.scheduling.JobManager;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import java.io.Serializable;
import java.util.Properties;
import javax.batch.api.chunk.ItemReader;
import javax.batch.operations.JobOperator;
import javax.batch.runtime.BatchRuntime;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;

/**
 * This reader will poll one by one the queued sync groups and retrieve the 
 * information declared in the polling definition and map it to a generic 
 * structure in memory 
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DefaultSyncReader implements ItemReader {
    @Inject
    private JobContext jobContext;
    /**
     * Reference to the sync group associated to this sync process
     */
    private SynchronizationGroup syncGroup;
    /**
     * Flag that will be used to run the process only one time
     */
    private boolean stop = false;
    
    @Override
    public void open(Serializable checkpoint) throws Exception {
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        Properties jobParameters = jobOperator.getParameters(jobContext.getExecutionId());
        if (!jobParameters.containsKey("syncGroupId"))
            throw new InvalidArgumentException("No synchronization group was provided as parameter for the current sync job");
        Long syncGroupId = Long.valueOf((String) jobParameters.get("syncGroupId"));
        syncGroup = PersistenceService.getInstance().getApplicationEntityManager().getSyncGroup(syncGroupId);
    }
    
    @Override
    public Object readItem() throws Exception {
        if (!stop) {
            stop = true;
            jobContext.setTransientUserData(syncGroup);
            
            try {
                return syncGroup.getProvider().mappedPoll(syncGroup);
            } catch(Exception ex) {
                BackgroundJob managedJob = JobManager.getInstance().getJob(jobContext.getExecutionId());
                managedJob.setStatus(BackgroundJob.JOB_STATUS.ABORTED);
                managedJob.setExceptionThrownByTheJob(ex); // Catching the exception and ending the job
                return null;
            }
        }        
        return null; //when this method returns null, no more iterations of the process are expected
        
    }

    @Override
    public void close() throws Exception {}

    @Override
    public Serializable checkpointInfo() throws Exception {
        return syncGroup; //Nothing to do 
    }
}
