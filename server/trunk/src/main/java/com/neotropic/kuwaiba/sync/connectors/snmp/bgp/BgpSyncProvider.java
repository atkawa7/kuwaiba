/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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

package com.neotropic.kuwaiba.sync.connectors.snmp.bgp;

import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import com.neotropic.kuwaiba.sync.model.AbstractSyncProvider;
import com.neotropic.kuwaiba.sync.model.PollResult;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SyncResult;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import java.util.List;

/**
 *
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BgpSyncProvider extends AbstractSyncProvider {

    @Override
    public String getDisplayName() {
        return "BGP Routing Map";
    }

    @Override
    public String getId() {
        return "com.neotropic.kuwaiba.sync.connectors.snmp.bgp.BgpSyncProvider";
    }

    @Override
    public boolean isAutomated() {
        return true;
    }

    @Override
    public List<AbstractDataEntity> unmappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PollResult mappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SyncFinding> supervisedSync(PollResult pollResult) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SyncFinding> supervisedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SyncResult> automatedSync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SyncResult> automatedSync(PollResult pollResult) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SyncResult> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
