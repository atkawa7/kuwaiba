/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.persistence.application;

import java.io.Serializable;
import java.util.List;

/**
 * The result of a task execution
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */

public class TaskResult implements Serializable {
    /**
     * The task resulted in error. The consumer should check the errorMessage for details
     */
    public static final int STATUS_ERROR = 1;
    /**
     * The execution was successful
     */
    public static final int STATUS_SUCCESS = 2;
    /**
     * The execution had non-blocking errors. There will be messages, but also an error message
     */
    public static final int STATUS_WARNING = 3;
    
    /**
     * The list of messages showing the results of the task
     */
    private List<String> messages;
    /**
     * The status of the result. For possible values see the static members of this class
     */
    private int resultStatus;
    /**
     * Error message, if applicable
     */
    private String errorMessage;

    public TaskResult(List<String> messages, int resultStatus, String errorMessage) {
        this.messages = messages;
        this.resultStatus = resultStatus;
        this.errorMessage = errorMessage;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public int getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(int resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
}    
    