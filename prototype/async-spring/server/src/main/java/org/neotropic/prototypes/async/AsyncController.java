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

package org.neotropic.prototypes.async;

import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple controller that exposes a set REST endpoints to launch and manage existing tasks.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@RestController
public class AsyncController {
    /**
     * The service that manages the job life cycle of the existing jobs.
     */
    @Autowired
    private AsyncManagerService asyncManagerService;
    @Autowired
    private AsyncLaunchService asyncLaunchService;
    
    /**
     * Launches a long-lived job and adds it to a list of jobs that can be queried and managed afterwards.
     * @return The newly created job or null in case of error.
     */
    @GetMapping("async/launcher")
    public ManagedJob launch() {
        try {
            ManagedJob newJob = new ManagedJob();
            asyncLaunchService.launchLongLivedTask(newJob);
            return newJob;
        } catch(Exception e) {
            return null;
        }
    }
    
    /**
     * Shows the list of jobs that are running or have already finished.
     * @return The list of jobs as a JSON structure.
     */
    @GetMapping("async/manager")
    public Collection<ManagedJob> showJobs() {
        return asyncManagerService.getTaskList();
    }
    
    /**
     * Shows the state and progress of a given job.
     * @param id The id of the job to query for.
     * @return The details of the requested job as a JSON structure or null if none could be found.
     */
    @GetMapping("async/track")
    public ManagedJob getJob(@RequestParam String id) {
        return asyncManagerService.getJob(id);
    }
}