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
package org.inventory.communications.core;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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

    private List<PropertyChangeListener> changeListeners;

    public LocalTask(long id, String name, String description, boolean enabled, String script, HashMap<String, String> parameters, LocalTaskScheduleDescriptor schedule, LocalTaskNotificationDescriptor notificationType) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enabled = enabled;
        this.script = script;
        this.parameters = parameters;
        this.schedule = schedule;
        this.notificationType = notificationType;
        this.changeListeners = new ArrayList<>();
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        String oldName = this.name;
        this.name = name;
        firePropertyChange(Constants.PROPERTY_NAME, oldName, name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        String oldDescription = this.description;
        this.description = description;
        firePropertyChange(Constants.PROPERTY_NAME, oldDescription, name);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        boolean oldEnabled = this.isEnabled();
        this.enabled = enabled;
        firePropertyChange(Constants.PROPERTY_ENABLED, String.valueOf(oldEnabled), String.valueOf(enabled));
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameter(String parameterName, String parameterValue) {
        this.parameters.put(parameterName, parameterValue);
    }
    
    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }

    public long getStartTime() { 
        return schedule.getStartTime();
    }
    
    public void setStartTime(long startTime) { 
        long oldStartTime = this.schedule.getStartTime();
        schedule.setStartTime(startTime);
        firePropertyChange(Constants.PROPERTY_START_TIME, oldStartTime, startTime);
    }
    
    public int getExecutionType() {        
        return schedule.getExecutionType();
    }
    
    public void setExecutionType(int executionType) { 
        int oldExecutionType = this.schedule.getExecutionType();
        schedule.setExecutionType(executionType);
        firePropertyChange(Constants.PROPERTY_EXECUTION_TYPE, oldExecutionType, executionType);
    }
    
    public int getEveryXMinutes() {
        return schedule.getEveryXMinutes();
    }
    
    public void setEveryXMinutes(int everyXMinutes) { 
        int oldEveryXMinutes = this.schedule.getEveryXMinutes();
        schedule.setEveryXMinutes(everyXMinutes);
        firePropertyChange(Constants.PROPERTY_EVERY_X_MINUTES, oldEveryXMinutes, everyXMinutes);
    }
    
    public String getEmail() {
        return notificationType.getEmail();
    }
    
    public void setEmail(String email) {
        String oldEmail = this.notificationType.getEmail();
        notificationType.setEmail(email);
        firePropertyChange(Constants.PROPERTY_EMAIL, oldEmail, email);
    }
    
    public int getNotificationType() {
        return notificationType.getNotificationType();
    }
    
    public void setNotificationType(int notificationType) {
        int oldNotificationType = this.notificationType.getNotificationType();
        this.notificationType.setNotificationType(notificationType);
        firePropertyChange(Constants.PROPERTY_NOTIFICATION_TYPE, oldNotificationType, notificationType);
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        String oldScript = this.script;
        this.script = script;
        firePropertyChange(Constants.PROPERTY_SCRIPT, oldScript, script);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
    public void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        for (PropertyChangeListener changeListener : changeListeners)
            changeListener.propertyChange(new PropertyChangeEvent(this, propertyName, oldValue, newValue));
    }
    
    public void addChangeListener(PropertyChangeListener listener) {
        changeListeners.add(listener);
    }
    
    public void removeChangeListener(PropertyChangeListener listener) {
        changeListeners.remove(listener);
    }
    
    public void resetChangeListeners() {
        changeListeners.clear();
    }

    @Override
    public int compareTo(LocalTask o) {
        return name.compareTo(o.getName());
    }
    
}
