/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.communications.runnable;

import java.util.List;
import org.inventory.communications.core.LocalSyncFinding;
import org.netbeans.api.progress.ProgressHandle;

/**
 * Helps to return a list of findings from the launch Synchronization method into the action how invokes this method
 * @author  Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public abstract class AbstractSyncRunnable implements Runnable {
    /**
      * Used to manage the progress bar in the UI
      */
    private ProgressHandle progressHandle;

    private List<LocalSyncFinding> findings;    

    public void setFindings(List<LocalSyncFinding> findings) {
        this.findings = findings;
    }

    public List<LocalSyncFinding> getFindings() {
        return findings;
    }

    public ProgressHandle getProgressHandle() {
        return progressHandle;
    }

    public void setProgressHandle(ProgressHandle progressHandle) {
        this.progressHandle = progressHandle;
    }

    @Override
    public final void run() {  
        // This implementation is empty becouse this method is called by the 
        // RequestProcessor and to avoid a null pointer exception was nescessary 
        // override it
    }
    
    public abstract void runSync();
}
