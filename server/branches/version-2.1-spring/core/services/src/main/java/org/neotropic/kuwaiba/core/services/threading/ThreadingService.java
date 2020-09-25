/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.core.services.threading;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfileLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * A service that manages long-lived jobs, such as synchronization providers execution, 
 * view rendering or management tasks execution.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class ThreadingService {
    /**
     * Reference to the translation service.
     */
    @Autowired
    private TranslationService ts;
    /**
     * The number of registered jobs after which a parallel access to the table will be attempted.
     */
    public static int PARALLEL_LIMIT = 50;
    /**
     * The list of jobs created (new, not yet running), running, or finished (with error, successfully or killed). The latter 
     * are cleaned up periodically or are removed from the table when whomever started it 
     */
    private ConcurrentHashMap<ManagedJobDescriptor, CompletableFuture> jobTable;

    public ThreadingService() {
        this.jobTable = new ConcurrentHashMap<>();
    }
    
    /**
     * Looks up a job in the job table (if existing).
     * @param jobId The id of the job.
     * @return The job, if present, or null otherwise.
     */
    public ManagedJobDescriptor getJob(String jobId) {
        return this.jobTable.searchKeys(PARALLEL_LIMIT, (aJob) -> {
            return aJob.getId().equals(jobId) ? aJob : null;
        });
    }
    
    /**
     * Kills a job.
     * @param jobId The id of the job to kill.
     * @throws IllegalArgumentException If the job could not be found, or if found, killed.
     */
    public void killJob(String jobId) throws IllegalArgumentException {
        Map.Entry<ManagedJobDescriptor, CompletableFuture> aThreadEntry = this.jobTable.searchEntries(PARALLEL_LIMIT, (anEntry) -> {
            return anEntry.getKey().getId().equals(jobId) ? anEntry : null;
        });
        if (aThreadEntry == null)
            throw new IllegalArgumentException(String.format(ts.getTranslatedString("apis.services.threading.messages.job-not-found"), jobId));
        else {
            aThreadEntry.getKey().setState(ManagedJobDescriptor.STATE_END_KILLED);
            aThreadEntry.getKey().setEndTime(Calendar.getInstance().getTimeInMillis());
            aThreadEntry.getValue().cancel(true);
        }
    }
    
    /**
     * Registers and starts a job.
     * @param theJob The job to be started.
     * @param theDescriptor The descriptor that will be used
     * @throws IllegalArgumentException If the job could not be started, most likely because of its state. Also, if <code>theDescriptor</code> 
     * is a job that already exists in the table.
     */
    public void startJob(ManagedJobDescriptor theDescriptor, CompletableFuture theJob) throws IllegalArgumentException {
        if (this.jobTable.contains(theDescriptor))
            throw new IllegalArgumentException(ts.getTranslatedString(""));
    }
}
