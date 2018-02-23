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

import com.neotropic.kuwaiba.sync.model.PollResult;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import javax.batch.api.chunk.ItemProcessor;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;

/**
 * Contains the logic that finds the differences between the polled device and the element in the inventory
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DefaultSyncProcessor implements ItemProcessor {
    @Inject
    private JobContext jobContext;
    
    @Override
    public Object processItem(Object item) throws Exception {
        if (jobContext.getTransientUserData() == null)
            throw new InvalidArgumentException("Syncronization group not found. Impossible to perform second stage");
        
        SynchronizationGroup syncGroup = (SynchronizationGroup)jobContext.getTransientUserData();
        
        if (item == null)
            throw new InvalidArgumentException("The result from the first stage of the synchronization process failed. Check the logs for details");
        
        return syncGroup.getProvider().sync((PollResult) item);
    }

}
