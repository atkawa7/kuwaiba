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
import java.util.Iterator;
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
    public static int MAX_MANAGED_JOBS_SIZE = 10;
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
    
    public List<BackgroundJob> getCurrentJobs() {
        return currentJobs;                        
    }
    
    /**
     * Launches a job, previously configure with a set of parameters
     * @param job The job to be executed
     * @throws InvalidArgumentException If the requested job is already running and does not support concurrence
     * @throws OperationNotPermittedException If the max number of running jobs has been already reached
     */
    public void launch(BackgroundJob job) throws InvalidArgumentException, OperationNotPermittedException {
         
        if (currentJobs.size() >= MAX_MANAGED_JOBS_SIZE) {
            //We will see first of there's some finished jobs we can purge, if not an exception will be rised
            boolean maxExceeded = true;
            Iterator<BackgroundJob> bgJobIterator = currentJobs.iterator();
            while (bgJobIterator.hasNext()) {
                BackgroundJob currentJob = bgJobIterator.next();
                if (currentJob.getStatus() == BackgroundJob.JOB_STATUS.FINISHED || currentJob.getStatus() == BackgroundJob.JOB_STATUS.ABORTED) {
                    currentJobs.remove(currentJob);
                    maxExceeded = false;
                    break;
                }
            }
            
            if (maxExceeded)
                throw new OperationNotPermittedException(String.format("The number of running jobs has exceeded the maximum permitted (%s)", MAX_MANAGED_JOBS_SIZE));
        }
        
        for (BackgroundJob currentJob : currentJobs)
            if (currentJob.getJobTag().equals(job.getJobTag()) && !job.allowConcurrence() && currentJob.getStatus() == BackgroundJob.JOB_STATUS.RUNNNING)
                throw new InvalidArgumentException(String.format("A job of type %s is already running", job.getJobTag()));
        
        currentJobs.add(job);
        job.run();
    }
    
    /**
     * Gets a managed job
     * @param jobId The id of the job
     * @return The job object
     * @throws InvalidArgumentException If the job can not be found
     */
    public BackgroundJob getJob(long jobId) throws InvalidArgumentException {
        for (BackgroundJob managedJob : currentJobs) {
            if (managedJob.getId() == jobId)
                return managedJob;
        }
        throw new InvalidArgumentException(String.format("The job with id %s does not exist or was removed from the job pool", jobId));
    }
    
    /**
     * Kills a job
     * @param jobId The id of the job
     * @throws InvalidArgumentException If there was a low level problem that avoided to kill the job 
     * (the job could not be found, there is a security restriction) 
     */
    public void kill(long jobId) throws InvalidArgumentException {
        Iterator<BackgroundJob> bgJobIterator = currentJobs.iterator();
        while (bgJobIterator.hasNext()) {
            BackgroundJob currentJob = bgJobIterator.next();
            if (currentJob.getId() == jobId) {
                try {
                    currentJob.kill();
                    return;
                }catch (JobSecurityException | NoSuchJobException | JobExecutionNotRunningException ex) {
                    System.out.println(String.format("[KUWAIBA] [%s] Unexpected error: %s", Calendar.getInstance().getTime(), ex.getMessage()));
                }
                currentJobs.remove(currentJob);
                return;
            }
        }
        throw new InvalidArgumentException(String.format("A job with id %s could not be found", jobId));
    }
}
