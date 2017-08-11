/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.inventory.communications.util.Constants;

/**
 * A local representation of a task
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalTask implements Comparable<LocalTask> {
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
    private HashMap<String, String> parameters;
    /**
     * When the task should be executed
     */
    private LocalTaskScheduleDescriptor schedule;
    /**
     * How the results of the task should be notified to the subscribed users
     */
    private LocalTaskNotificationDescriptor notificationType;
    /**
     * Users subscribed to this task
     */
    private List<LocalUserObjectLight> users;
    /**
     * List of listeners of this task's properties
     */
    private List<VetoableChangeListener> changeListeners;

    public LocalTask(long id, String name, String description, boolean enabled, 
            String script, HashMap<String, String> parameters, LocalTaskScheduleDescriptor schedule, 
            LocalTaskNotificationDescriptor notificationType, List<LocalUserObjectLight> users) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.script = script;
        this.parameters = parameters;
        this.schedule = schedule;
        this.notificationType = notificationType;
        this.users = users;
        this.changeListeners = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldName = this.name;
        try {
            firePropertyChange(Constants.PROPERTY_NAME, oldName, name);
            this.name = name;
        } catch (PropertyVetoException ex) {}
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        String oldDescription = this.description;
        try {
            firePropertyChange(Constants.PROPERTY_DESCRIPTION, oldDescription, description);
            this.description = description;
        } catch (PropertyVetoException ex) {}
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        boolean oldEnabled = this.isEnabled();
        try {
            firePropertyChange(Constants.PROPERTY_ENABLED, String.valueOf(oldEnabled), String.valueOf(enabled));
            this.enabled = enabled;
        } catch (PropertyVetoException ex) {}
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameter(String parameterName, String parameterValue) {
        String oldParameterValue = this.parameters.get(parameterName);
        try {
            firePropertyChange(parameterName, oldParameterValue, parameterValue);
            this.parameters.put(parameterName, parameterValue);
        } catch (PropertyVetoException ex) {}
    }
    
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    public long getStartTime() { 
        return schedule.getStartTime();
    }
    
    public void setStartTime(long startTime) { 
        long oldStartTime = this.schedule.getStartTime();
        try {
            firePropertyChange(Constants.PROPERTY_START_TIME, oldStartTime, startTime);
            schedule.setStartTime(startTime);
        } catch (PropertyVetoException ex) {}
    }
    
    public int getExecutionType() {        
        return schedule.getExecutionType();
    }
    
    public void setExecutionType(int executionType) { 
        int oldExecutionType = this.schedule.getExecutionType();
        try {
            firePropertyChange(Constants.PROPERTY_EXECUTION_TYPE, oldExecutionType, executionType);
            schedule.setExecutionType(executionType);
        } catch (PropertyVetoException ex) {}
    }
    
    public int getEveryXMinutes() {
        return schedule.getEveryXMinutes();
    }
    
    public void setEveryXMinutes(int everyXMinutes) { 
        int oldEveryXMinutes = this.schedule.getEveryXMinutes();
        
        try {
            firePropertyChange(Constants.PROPERTY_EVERY_X_MINUTES, oldEveryXMinutes, everyXMinutes);
            schedule.setEveryXMinutes(everyXMinutes);
        } catch (PropertyVetoException ex) {}
    }
    
    public String getEmail() {
        return notificationType.getEmail();
    }
    
    public void setEmail(String email) {
        String oldEmail = this.notificationType.getEmail();
        try {
            firePropertyChange(Constants.PROPERTY_EMAIL, oldEmail, email);
            notificationType.setEmail(email);
        } catch (PropertyVetoException ex) {}
    }
    
    public int getNotificationType() {
        return notificationType.getNotificationType();
    }
    
    public void setNotificationType(int notificationType) {
        int oldNotificationType = this.notificationType.getNotificationType();
        try {
            firePropertyChange(Constants.PROPERTY_NOTIFICATION_TYPE, oldNotificationType, notificationType);
            this.notificationType.setNotificationType(notificationType);
        } catch (PropertyVetoException ex) {}
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        String oldScript = this.script;        
        try {
            firePropertyChange(Constants.PROPERTY_SCRIPT, oldScript, script);
            this.script = script;
        } catch (PropertyVetoException ex) {}
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) throws PropertyVetoException {
        for (VetoableChangeListener changeListener : changeListeners)
            changeListener.vetoableChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }
    
    public void addChangeListener(VetoableChangeListener listener) {
        changeListeners.add(listener);
    }
    
    public void removeChangeListener(VetoableChangeListener listener) {
        changeListeners.remove(listener);
    }
    
    public void resetChangeListeners() {
        changeListeners.clear();
    }

    public LocalTaskScheduleDescriptor getSchedule() {
        return schedule;
    }

    public void setSchedule(LocalTaskScheduleDescriptor schedule) {
        this.schedule = schedule;
    }

    public void setNotificationType(LocalTaskNotificationDescriptor notificationType) {
        this.notificationType = notificationType;
    }

    public List<LocalUserObjectLight> getUsers() {
        return users;
    }

    public void setUsers(List<LocalUserObjectLight> users) {
        this.users = users;
    }

    
    @Override
    public int compareTo(LocalTask o) {
        return name.compareTo(o.getName());
    }
    
}
