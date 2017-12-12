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

/**
 * Helps to return a list of findings from the launch Synchronization method into the action how invokes this method
 * @author  Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public abstract class AbstractSyncRunnable implements Runnable{

    private List<LocalSyncFinding> findings;

    public void setFindings(List<LocalSyncFinding> findings) {
        this.findings = findings;
    }

    public List<LocalSyncFinding> getFindings() {
        return findings;
    }
}
