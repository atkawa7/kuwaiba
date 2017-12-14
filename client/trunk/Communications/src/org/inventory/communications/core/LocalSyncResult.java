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
     * An unexpected error was found while execute the sync action
     */
    public static final int ERROR = 0;
    /**
     * The sync action was executed successfully
     */
    public static final int SUCCESS = 1;
    /**
     * The sync action was executed with warnings
     */
    public static final int WARNING = 2;
    /**
     * The description of the action that was performed
     */
    private String actionDescription;
    /**
     * The textual description of the result of that action
     */
    private String result;
    /**
     * The type of result. Gives a feedback of the status of the executed action:
     * ERROR, SUCCESS, WARNING
     */
    private int type;
   
    public LocalSyncResult() { }

    public LocalSyncResult(int type, String actionDescription, String result) {
        this.type = type;
        this.actionDescription = actionDescription;
        this.result = result;
    }
    
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
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
