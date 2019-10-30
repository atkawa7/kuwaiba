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
 */

package com.neotropic.kuwaiba.sync.model;

import com.vaadin.icons.VaadinIcons;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * Instances of this class are intended to inform about the results of a synchronization process. 
 * In principle a simple list of strings would suffice, however this class could be extended in
 * the future to provide mechanisms to retry a sync action
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SyncResult {
    /**
     * An unexpected error was found while execute the sync action
     */
    public static int TYPE_ERROR = 0;
    /**
     * The sync action was executed successfully
     */
    public static int TYPE_SUCCESS = 1;
    /**
     * The sync action was executed with warnings
     */
    public static int TYPE_WARNING = 2;
    /**
     * The sync action was executed and an information message was generated
     */
    public static int TYPE_INFORMATION = 3;
    /**
     * The type of result. Gives feedback of the status of the executed action. See TYPE_* for possible values
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
    /**
     * Data source configuration id
     */
    private long dataSourceId;
   
    public SyncResult() { }

    public SyncResult(long dataSourceId, int type, String actionDescription, String result) {
        this.dataSourceId = dataSourceId;
        this.type = type;
        this.actionDescription = actionDescription;
        this.result = result;
    }
    
    public String getTypeAsString() {
        switch (type) {
            case 0: // TYPE_ERROR
                return "Error";
            case 1: // TYPE_SUCCESS
                return "Success";
            case 2: // TYPE_WARNING
                return "Warning";
            case 3: // TYPE_INFORMATION
                return "Information";
            default:
                return "Error";
        }        
    }
    
    public String getTypeAsIcon() {
        VaadinIcons vaadinIcons = VaadinIcons.CLOSE_CIRCLE;
        String color = "#d40000"; //NOI18N
        switch (type) {
            case 0: // TYPE_ERROR
                vaadinIcons = VaadinIcons.CLOSE_CIRCLE;
                color = "#d40000"; //NOI18N
                break;
            case 1: // TYPE_SUCCESS
                vaadinIcons = VaadinIcons.CHECK_CIRCLE;
                color = "#aad400"; //NOI18N
                break;
            case 2: // TYPE_WARNING
                vaadinIcons = VaadinIcons.EXCLAMATION_CIRCLE;
                color = "#ffcc00"; //NOI18N
                break;
            case 3: // TYPE_INFORMATION
                vaadinIcons = VaadinIcons.INFO_CIRCLE;
                color = "#0066ff"; //NOI18N
                break;
        }
        return "<span class=\"v-icon\" style=\"font-family: " //NOI18N
            + vaadinIcons.getFontFamily() 
            + "; color:" 
            + color 
            +";\">&#x" //NOI18N
            + Integer.toHexString(vaadinIcons.getCodepoint())
            + ";</span>"; //NOI18N
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

    public long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }
}
