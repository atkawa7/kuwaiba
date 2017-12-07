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

import com.neotropic.kuwaiba.sync.model.AbstractSyncProvider;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import java.io.Serializable;
import java.util.Properties;
import javax.batch.api.chunk.AbstractItemReader;
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
public class DefaultSyncReader extends AbstractItemReader {
    @Inject
    private JobContext jobContext;
    private AbstractSyncProvider syncProvider;
    private SynchronizationGroup syncGroup;
    
    @Override
    public void open(Serializable checkpoint) throws Exception {
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        Properties jobParameters = jobOperator.getParameters(jobContext.getExecutionId());
        if (!jobParameters.containsKey("syncGroupId"))
            throw new InvalidArgumentException("No synchronization group was provided as parameter for the current sync job");
        Long syncGroupId = Long.valueOf((String) jobParameters.get("syncGroupId"));
        syncGroup = PersistenceService.getInstance().getApplicationEntityManager().getSyncGroup(syncGroupId);
        syncProvider = syncGroup.getProvider();
        
    }
    
    @Override
    public Object readItem() throws Exception {
        return syncProvider.mappedPoll(syncGroup);
    }
}
