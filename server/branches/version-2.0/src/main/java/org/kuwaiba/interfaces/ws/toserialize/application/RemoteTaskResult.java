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
package org.kuwaiba.interfaces.ws.toserialize.application;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * The result of a task execution
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteTaskResult implements Serializable {
    /**
     * The list of messages showing the results of the task
     */
    private List<RemoteResultMessage> messages;
    

    public RemoteTaskResult() {
        this.messages = new ArrayList<>();
    }

    public RemoteTaskResult(List<RemoteResultMessage> messages) {
        this.messages = messages;
    }

    public List<RemoteResultMessage> getMessages() {
        return messages;
    }

    public void setMessages(List<RemoteResultMessage> messages) {
        this.messages = messages;
    }

    public static RemoteTaskResult createErrorResult(String message) {
        ArrayList<RemoteResultMessage> errorMessage = new ArrayList<>();
        errorMessage.add(new RemoteResultMessage(RemoteResultMessage.STATUS_ERROR, message));
                
        return new RemoteTaskResult(errorMessage);
    }
    
}    
    