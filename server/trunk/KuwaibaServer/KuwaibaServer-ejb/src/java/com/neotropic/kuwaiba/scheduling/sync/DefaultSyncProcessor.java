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

import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import com.neotropic.kuwaiba.sync.model.AbstractSyncProvider;
import java.util.HashMap;
import javax.batch.api.chunk.ItemProcessor;
import javax.batch.runtime.context.JobContext;
import javax.inject.Inject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;

/**
 * Contains the logic that finds the differences between the polled device and the element in the inventory
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DefaultSyncProcessor implements ItemProcessor {
    @Inject
    private JobContext jobContext;
    
    @Override
    public Object processItem(Object item) throws Exception {
        //TODO: process item implementation to unmapped polls
        AbstractSyncProvider syncProvider = null;
        HashMap<RemoteBusinessObjectLight, AbstractDataEntity> mappedPollResult = null;
        
        if (jobContext.getTransientUserData() instanceof AbstractSyncProvider)
            syncProvider = ((AbstractSyncProvider) jobContext.getTransientUserData());            
        else
            throw new Exception("Synchronization provider cannot be found");
        
        if (item instanceof HashMap)
            mappedPollResult = (HashMap<RemoteBusinessObjectLight,AbstractDataEntity>) item;
        else
            throw new Exception("Mapped poll result can no be found");
        for (RemoteBusinessObjectLight object : mappedPollResult.keySet())
            syncProvider.sync(object.getClassName(), object.getId(), null); //<--NOT null change!! 
        return null; // If return null value, the job don't execute the writer 
                     // and the job end with status COMPLETED
    }

}
