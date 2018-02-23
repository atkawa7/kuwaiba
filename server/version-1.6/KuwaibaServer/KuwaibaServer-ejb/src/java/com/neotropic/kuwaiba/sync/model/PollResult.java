/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.sync.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class PollResult {
    private final HashMap<RemoteBusinessObjectLight, AbstractDataEntity> result;
    private final HashMap<SyncDataSourceConfiguration, List<Exception>> exceptions;
    
    public PollResult() {
        result = new HashMap();
        exceptions = new HashMap();
    }
    
    public HashMap<RemoteBusinessObjectLight, AbstractDataEntity> getResult() {
        return result;
    }
    
    public HashMap<SyncDataSourceConfiguration, List<Exception>> getExceptions() {
        return exceptions;
    }
    
    public List<Exception> getSyncDataSourceConfigurationExceptions(SyncDataSourceConfiguration object) {
        if (!exceptions.containsKey(object))
            exceptions.put(object, new ArrayList());
        return exceptions.get(object);
    }
}
