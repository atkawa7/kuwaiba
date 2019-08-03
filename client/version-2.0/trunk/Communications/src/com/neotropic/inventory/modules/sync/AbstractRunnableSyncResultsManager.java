/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package com.neotropic.inventory.modules.sync;

import java.util.List;
import org.netbeans.api.progress.ProgressHandle;

/**
 * Asynchronically handles the sync findings after returned by a supervised synchronization process
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractRunnableSyncResultsManager implements Runnable {
    /**
      * Used to manage the progress bar in the UI
      */
    protected ProgressHandle progressHandle;
    /**
     * The list of sync results to be displayed
     */
    protected List<LocalSyncResult> syncResults;    
    /**
     * The sync groups the results are related to
     */
    protected LocalSyncGroup localSyncGroup;

    public List<LocalSyncResult> getSyncResults() {
        return syncResults;
    }

    public void setSyncResults(List<LocalSyncResult> syncResults) {
        this.syncResults = syncResults;
    }

    public ProgressHandle getProgressHandle() {
        return progressHandle;
    }

    public void setProgressHandle(ProgressHandle progressHandle) {
        this.progressHandle = progressHandle;
    }

    public LocalSyncGroup getLocalSyncGroup() {
        return localSyncGroup;
    }

    public void setLocalSyncGroup(LocalSyncGroup localSyncGroup) {
        this.localSyncGroup = localSyncGroup;
    }

    @Override
    public final void run() {  
        // This implementation is empty because this method is called by the 
        // RequestProcessor and to avoid a null pointer exception was nescessary 
        // override it
    }
    
    public abstract void handleSyncResults();
}
