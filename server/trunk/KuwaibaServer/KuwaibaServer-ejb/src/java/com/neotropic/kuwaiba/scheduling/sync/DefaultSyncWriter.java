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

import java.io.Serializable;
import java.util.List;
import javax.batch.api.chunk.ItemWriter;

/**
 * Executes the actions after having analyzed the differences between the
 * information in the SNMP agents and the information in Kuwaiba. These actions 
 * were defined in the ItemProcessor
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DefaultSyncWriter implements ItemWriter {
        
    @Override
    public void writeItems(List<Object> items) throws Exception {
    }

    @Override
    public void open(Serializable checkpoint) throws Exception {
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public Serializable checkpointInfo() throws Exception {
        return null;
    }
}
