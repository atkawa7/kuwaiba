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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * A service that provides the methods to be run in background.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class AsyncLaunchService {
    @Autowired
    private Executor taskManager;
    @Autowired
    private AsyncManagerService asyncManagerService;
    /**
     * A task that takes 20 seconds to complete and adds 10% of progress each iteration
     * @param managedJob The managed job this task is related to.
     * @return A CompletableFuture with the job description.
     */
    @Async
    public CompletableFuture<ManagedJob> launchLongLivedTask(ManagedJob managedJob) {
        asyncManagerService.getTaskList().add(managedJob);
        
        return CompletableFuture.supplyAsync(() -> {
            System.out.println("Starting job " + managedJob);
            managedJob.setState(ManagedJob.STATE_RUNNING);
            for (int i = 1; i <= 10; i++) {
                try {
                    Thread.sleep(5000); // Adds a pause to each iteration
                } catch (InterruptedException ex) { /* Should not happen */}
                managedJob.setProgress(managedJob.getProgress() + 10);
                System.out.println(managedJob.getId() + ": " + managedJob.getProgress());
            }
            managedJob.setState(ManagedJob.STATE_END_SUCCESS);
            System.out.println("Ending job " + managedJob);
            return managedJob;
        }, taskManager);
    }
}
