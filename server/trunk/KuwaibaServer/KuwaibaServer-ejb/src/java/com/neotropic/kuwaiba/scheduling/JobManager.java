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

package com.neotropic.kuwaiba.scheduling;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.batch.operations.JobExecutionNotRunningException;
import javax.batch.operations.JobSecurityException;
import javax.batch.operations.NoSuchJobException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;

/**
 * This class handles the jobs that are being executed within the server, and provides methods to manage their life cycles
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class JobManager {
    /**
     * Max number of jobs to be managed (running + finished + aborted)
     */
    public static int MAX_QUEUE_SIZE = 10;
    /**
     * The list of managed jobs
     */
    private List<BackgroundJob> currentJobs;
    /**
     * Singleton implementation
     */
    private static JobManager instance;
    
    public static JobManager getInstance() {
        return instance == null ? instance = new JobManager() : instance;
    }

    private JobManager() { 
        currentJobs = new ArrayList<>();
    } 
    
    public void launch(BackgroundJob job) throws InvalidArgumentException, OperationNotPermittedException {
        
        if (currentJobs.size() == MAX_QUEUE_SIZE) {
            //We will see first of there's some finished jobs we can purge, if not an exception will be rised
            boolean maxExceeded = true;
            for (BackgroundJob currentJob : currentJobs) {
                if (currentJob.getStatus() == BackgroundJob.JOB_STATUS.FINISHED || currentJob.getStatus() == BackgroundJob.JOB_STATUS.ABORTED) {
                    currentJobs.remove(currentJob); //Concurrent access problem here?
                    maxExceeded = false;
                    break;
                }
            }
            
            if (maxExceeded)
                throw new OperationNotPermittedException(String.format("The number of running jobs has exceeded the maximum permitted (%s)", MAX_QUEUE_SIZE));
        }
        
        for (BackgroundJob currentJob : currentJobs)
            if (currentJob.getJobTag().equals(job.getJobTag()) && !job.allowConcurrence())
                throw new InvalidArgumentException(String.format("A job with tag id %s is already running", job.getJobTag()));
        
        currentJobs.add(job);
        job.run();
    }
    
    public void kill(long jobId) throws InvalidArgumentException {
        for (BackgroundJob currentJob : currentJobs) {
            if (currentJob.getId() == jobId) {
                try {
                    currentJob.kill();
                }catch (JobSecurityException | NoSuchJobException | JobExecutionNotRunningException ex) {
                    System.out.println(String.format("[KUWAIBA] [%s] Unexpected error: %s", Calendar.getInstance().getTime(), ex.getMessage()));
                }
            }
        }
        throw new InvalidArgumentException(String.format("A job with id %s could not be found", jobId));
    }
}
