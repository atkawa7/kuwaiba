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

package com.neotropic.kuwaiba.sync.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Instances of this class are intended to inform about the results of a synchronization process. 
 * In principle a simple list of strings would suffice, however this class could be extended in
 * the future to provide mechanisms to retry a sync action
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SyncResult {
    /**
     * An unexpected error was found while execute the sync action
     */
    public static int ERROR = 0;
    /**
     * The sync action was executed successfully
     */
    public static int SUCCESS = 1;
    /**
     * The sync action was executed with warnings
     */
    public static int WARNING = 2;    
    /**
     * The type of result. Gives a feedback of the status of the executed action:
     * ERROR, SUCCESS, WARNING
     */
    private int type;
    /**
     * The description of the action that was performed
     */
    private String actionDescription;
    /**
     * The textual description of the result of that action
     */
    private String result;
   
    public SyncResult() { }

    public SyncResult(int type, String actionDescription, String result) {
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
