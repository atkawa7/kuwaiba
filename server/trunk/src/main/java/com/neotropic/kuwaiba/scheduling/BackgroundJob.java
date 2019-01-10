/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import javax.batch.operations.JobSecurityException;
import javax.batch.operations.JobStartException;
import javax.batch.runtime.BatchRuntime;

/**
 * This class represents an actual job to be run in background by the JobManager. 
 * It is a wrapper that abstracts the threading provider (Java batch jobs, Spring threads, etc)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class BackgroundJob implements Runnable {
    /**
     * The execution id used to identify the current instance within the local job manager
     */
    private long id;
    /**
     * The unique string that allows the system to create an instance of the job (see META-INF.batch-jobs)
     */
    private String jobTag;
    /**
     * The percentage of progress
     */
    private int progress;
    /**
     * Allow or not multiple instances of this job
     */
    private boolean allowConcurrence;
    /**
     * The initial parameters to execute this job
     */
    private Properties parameters;
    /**
     * What's the current status of the job
     */
    private JOB_STATUS status;
    /**
     * The job result. This field will be set once the job is finished
     */
    private Object jobResult;
    /**
     * Used to catch the exception thrown during the batch job execution
     */
    private Exception exceptionThrownByTheJob;

    public BackgroundJob(String jobTag, boolean allowConcurrence, Properties parameters) {
        this.jobTag = jobTag;
        this.progress = 0;
        this.allowConcurrence = allowConcurrence;
        this.parameters = parameters;
        this.status = JOB_STATUS.NOT_STARTED;
    }

    public long getId() {
        return id;
    }

    public String getJobTag() {
        return jobTag;
    }

    public void setJobTag(String jobTag) {
        this.jobTag = jobTag;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public boolean allowConcurrence() {
        return allowConcurrence;
    }

    public void setAllowConcurrence(boolean allowConcurrence) {
        this.allowConcurrence = allowConcurrence;
    }

    public Properties getParameters() {
        return parameters;
    }

    public void setParameters(Properties parameters) {
        this.parameters = parameters;
    }

    public JOB_STATUS getStatus() {
        return status;
    }

    public void setStatus(JOB_STATUS status) {
        this.status = status;
    }
    
    public Date getStartTime(){
        return BatchRuntime.getJobOperator().getJobExecution(id).getStartTime();
    }
    
    public Date getEndTime(){
        return BatchRuntime.getJobOperator().getJobExecution(id).getEndTime();
    }

    public Exception getExceptionThrownByTheJob() {
        return exceptionThrownByTheJob;
    }

    public void setExceptionThrownByTheJob(Exception exceptionThrownByTheJob) {
        this.exceptionThrownByTheJob = exceptionThrownByTheJob;
    }
    
    @Override
    public void run() {
        try {
            this.status = JOB_STATUS.RUNNNING;
            id = BatchRuntime.getJobOperator().start(jobTag, parameters);
        }catch (JobStartException | JobSecurityException ex) {
            System.out.println(String.format("[KUWAIBA] [%s] %s", ex.getMessage(), Calendar.getInstance().getTime()));
            this.status = JOB_STATUS.ABORTED;
        }
    }
    
    public void kill() {
        BatchRuntime.getJobOperator().stop(id);
        this.status = JOB_STATUS.ABORTED;
    }
    
    public void  setJobResult(Object jobResult) {
        this.jobResult = jobResult;
    }
    
    public Object getJobResult() {
        return this.jobResult;
    }
    
    @Override
    public boolean equals (Object obj) {
        return obj instanceof BackgroundJob && ((BackgroundJob)obj).getId() == this.id;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 97 * hash + Objects.hashCode(this.jobTag);
        return hash;
    }
    
    public enum JOB_STATUS {
        NOT_STARTED,
        RUNNNING,
        PAUSED,
        FINISHED,
        ABORTED
    }     
} 
