/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.communications.core;

import java.util.List;

/**
 * Represents a task result
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalTaskResult {
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
     * Task execution messages
     */
    private List<String> messages;
    /**
     * Error message, if the resultStatus is either STATUS_ERROR or STATUS_WARNING
     */
    private String errorMessage;
    /**
     * How did the execution go? See the static fields of this class for possible values
     */
    private int resultStatus;

    public LocalTaskResult(List<String> messages, String errorMessage, int resultStatus) {
        this.messages = messages;
        this.errorMessage = errorMessage;
        this.resultStatus = resultStatus;
    }
    
    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(int resultStatus) {
        this.resultStatus = resultStatus;
    }
    
    
}
