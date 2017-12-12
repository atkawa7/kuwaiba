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
 */
package org.inventory.communications.core;

/**
 * Represents a inform about the results of a synchronization process
 * @author adrian
 */
public class LocalSyncResult {
    /**
     * The description of the action that was performed
     */
    private String actionDescription;
    /**
     * The textual description of the result of that action
     */
    private String result;
   
    public LocalSyncResult() { }

    public LocalSyncResult(String actionDescription, String result) {
        this.actionDescription = actionDescription;
        this.result = result;
    }

    public String getActionDescription() {
        return actionDescription;
    }

    public void setActionDescription(String actionDescription) {
        this.actionDescription = actionDescription;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
