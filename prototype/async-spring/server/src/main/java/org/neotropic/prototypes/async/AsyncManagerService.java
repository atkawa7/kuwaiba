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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

/**
 * A service that manages the running and finished jobs.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class AsyncManagerService {
    private Collection<ManagedJob> taskList;
    
    @PostConstruct
    public void init() {
        this.taskList = Collections.synchronizedCollection(new ArrayList<>());
    }
    
    /**
     * Return the list of jobs running and finished
     * @return A list of the current jobs executed or still running.
     */
    public Collection<ManagedJob> getTaskList() {
        return this.taskList;
    }
    
    /**
     * Fetches the job with the given id.
     * @param id The job id to try to find.
     * @return The job with the given id or null if none found.
     */
    public ManagedJob getJob(String id) {
        return this.taskList.stream().filter( aJob -> aJob.getId().equals(id)).findFirst().orElse(null);
    }
}
