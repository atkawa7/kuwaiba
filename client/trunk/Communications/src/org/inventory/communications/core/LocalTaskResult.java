/**
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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

import java.util.ArrayList;
import java.util.List;

/**
 * The result of a task execution
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalTaskResult {
    /**
     * The list of messages showing the results of the task
     */
    private List<LocalTaskResultMessage> messages;
    

    public LocalTaskResult() {
        this.messages = new ArrayList<>();
    }

    public LocalTaskResult(List<LocalTaskResultMessage> messages) {
        this.messages = messages;
    }

    public List<LocalTaskResultMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<LocalTaskResultMessage> messages) {
        this.messages = messages;
    }    
}    
    