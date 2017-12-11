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
import java.io.Serializable;
import java.util.List;
import javax.batch.api.chunk.ItemWriter;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;

/**
 * Executes the actions after having analyzed the differences between the
 * information in the SNMP agents and the information in Kuwaiba. These actions 
 * were defined in the ItemProcessor. In practical terms, what this does is to call the finalize() method in the sync provider
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DefaultSyncWriter implements ItemWriter {
    @Inject
    private JobContext jobContext;
    
    @Override
    public void writeItems(List<Object> items) throws Exception {
        if (items.size() != 1) //The processor should return only a list of SyncFindings
            throw new InvalidArgumentException(String.format("Only one output is expected from the synchronization process, but %s found", items.size()));
        try {
            BackgroundJob managedJob = JobManager.getInstance().getJob(jobContext.getExecutionId());
            managedJob.setJobResult(items.get(0));
            managedJob.setStatus(BackgroundJob.JOB_STATUS.FINISHED);
            managedJob.setProgress(100);
        }catch (InvalidArgumentException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void open(Serializable checkpoint) throws Exception {
    }

    @Override
    public void close() throws Exception {
        //The last step is to update the job progress and set the result
    }

    @Override
    public Serializable checkpointInfo() throws Exception { return null; }
}
