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
package org.kuwaiba.ws.toserialize.application;

import java.io.Serializable;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.ws.todeserialize.StringPair;

/**
 * Remote representation of a task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */

@XmlAccessorType(XmlAccessType.FIELD)
public final class RemoteTask implements Serializable {
    /**
     * Task id
     */
    private long id;
    /**
     * Task name
     */
    private String name;
    /**
     * Task description
     */
    private String description;
    /**
     * Id this task enabled?
     */
    private boolean enabled;
    /**
     * Task script
     */
    private String script;
    /**
     * List of parameters as a set of parameter name/value pairs
     */
    private List<StringPair> parameters;
    /**
     * When the task should be executed
     */
    private TaskScheduleDescriptor schedule;
    /**
     * How the results of the task should be notified to the subscribed users
     */
    private TaskNotificationDescriptor notificationType;
    /**
     * Users subscribed to this task
     */
    private List<UserInfoLight> users;

    //No-arg constructor required
    public RemoteTask() {   }

    public RemoteTask(long id, String name, String description, boolean enabled, 
            String script, List<StringPair> parameters, TaskScheduleDescriptor schedule, TaskNotificationDescriptor notificationType, List<UserInfoLight> users) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.script = script;
        this.parameters = parameters;
        this.schedule = schedule;
        this.notificationType = notificationType;
        this.users = users;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<StringPair> getParameters() {
        return parameters;
    }

    public void setParameters(List<StringPair> parameters) {
        this.parameters = parameters;
    }

    public TaskScheduleDescriptor getSchedule() {
        return schedule;
    }

    public void setSchedule(TaskScheduleDescriptor schedule) {
        this.schedule = schedule;
    }

    public TaskNotificationDescriptor getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(TaskNotificationDescriptor notificationType) {
        this.notificationType = notificationType;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public List<UserInfoLight> getUsers() {
        return users;
    }

    public void setUsers(List<UserInfoLight> users) {
        this.users = users;
    }
}    
    