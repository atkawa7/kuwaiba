/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.automation.tasks.nodes;

import java.awt.Color;
import java.awt.Image;
import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import javax.swing.Action;
import org.inventory.automation.tasks.nodes.actions.TaskManagerActionFactory;
import org.inventory.communications.core.LocalTask;
import org.inventory.communications.core.LocalTaskNotificationDescriptor;
import org.inventory.communications.core.LocalTaskScheduleDescriptor;
import org.inventory.communications.core.LocalUserObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.navigation.navigationtree.nodes.properties.AbstractComboBoxProperty;
import org.inventory.navigation.navigationtree.nodes.properties.DatePickerPropertyEditorSupport;
import org.inventory.navigation.navigationtree.nodes.properties.IntegerEntry;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.lookup.Lookups;

/**
 * Node that wraps a LocalTask object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TaskNode extends AbstractNode {
    
    private static final Image defaultIcon = Utils.createRectangleIcon(Color.PINK, 10, 10);
    private static final IntegerEntry[] NOTIFICATION_TYPES 
                        = new IntegerEntry[] { new IntegerEntry(LocalTaskNotificationDescriptor.TYPE_NOTHING, "No Notification"), 
                                               new IntegerEntry(LocalTaskNotificationDescriptor.TYPE_CLIENT, "Client Managed"), 
                                               new IntegerEntry(LocalTaskNotificationDescriptor.TYPE_EMAIL, "Email") };
    
    private static final IntegerEntry[] EXECUTION_TYPES = new IntegerEntry[] { 
                        new IntegerEntry(LocalTaskScheduleDescriptor.TYPE_ON_DEMAND, "On Demand"), 
                        new IntegerEntry(LocalTaskScheduleDescriptor.TYPE_LOGIN, "User Login"), 
                        new IntegerEntry(LocalTaskScheduleDescriptor.TYPE_LOOP, "Loop"),
                        new IntegerEntry(LocalTaskScheduleDescriptor.TYPE_STARTUP, "System Startup") };
    
    public TaskNode(LocalTask task) {
        super(new TaskChildren(), Lookups.singleton(task));
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { 
            TaskManagerActionFactory.createExecuteTaskAction(),
            TaskManagerActionFactory.createAddParameterToTaskActionAction(),
            TaskManagerActionFactory.createRemoveParameterFromTaskActionAction(),
            TaskManagerActionFactory.createSubscribeUserAction(),
            null,
            TaskManagerActionFactory.createDeleteTaskAction()
        };
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return defaultIcon;
    }

    @Override
    public Image getIcon(int type) {
        return defaultIcon;
    }
    
    @Override
    public String getDisplayName() {
        LocalTask task = getLookup().lookup (LocalTask.class);
        return task.getName() == null ? "<No Name>" : task.getName();
    }
    
    public void resetPropertySheet() {
        setSheet(createSheet());
    }

    @Override
    protected Sheet createSheet() {
        LocalTask task = getLookup().lookup (LocalTask.class);
        Sheet sheet = Sheet.createDefault();
        try {
            Sheet.Set generalPropertySet = Sheet.createPropertiesSet(); //General task properties
            generalPropertySet.setName("General");
            generalPropertySet.setDisplayName("General");
            PropertySupport.Reflection<String> nameProperty = new PropertySupport.Reflection(task, String.class, Constants.PROPERTY_NAME);
            generalPropertySet.put(nameProperty);
            PropertySupport.Reflection<String> descriptionProperty = new PropertySupport.Reflection(task, String.class, Constants.PROPERTY_DESCRIPTION);
            generalPropertySet.put(descriptionProperty);
            PropertySupport.Reflection<Boolean> enabledProperty = new PropertySupport.Reflection(task, boolean.class, Constants.PROPERTY_ENABLED);
            generalPropertySet.put(enabledProperty);
            PropertySupport.Reflection<String> scriptProperty = new PropertySupport.Reflection(task, String.class, Constants.PROPERTY_SCRIPT);
            generalPropertySet.put(scriptProperty);

            Sheet.Set schedulePropertySet = Sheet.createPropertiesSet(); //Schedule task properties
            schedulePropertySet.setName("Scheduling");
            schedulePropertySet.setDisplayName("Scheduling");
            StartTimePropertySupport startTimeProperty = new StartTimePropertySupport(task, Constants.PROPERTY_START_TIME, "startTime", "" );
            schedulePropertySet.put(startTimeProperty);
            PropertySupport.Reflection<Integer> everyXminutesProperty = new PropertySupport.Reflection(task, int.class, Constants.PROPERTY_EVERY_X_MINUTES);
            schedulePropertySet.put(everyXminutesProperty);
            ExecutionTypePropertySupport executionTypeProperty = new ExecutionTypePropertySupport(task, EXECUTION_TYPES, Constants.PROPERTY_EXECUTION_TYPE, "executionType", "");
            schedulePropertySet.put(executionTypeProperty);

            Sheet.Set notificationTypePropertySet = Sheet.createPropertiesSet(); //Notification config task properties
            notificationTypePropertySet.setName("Notification Type");
            notificationTypePropertySet.setDisplayName("Notification Type");
            PropertySupport.Reflection<String> emailProperty = new PropertySupport.Reflection(task, String.class, Constants.PROPERTY_EMAIL);
            notificationTypePropertySet.put(emailProperty);
            NotificationTypePropertySupport notificationTypeProperty = new NotificationTypePropertySupport(task, NOTIFICATION_TYPES , Constants.PROPERTY_NOTIFICATION_TYPE, "notificationType", "");
            notificationTypePropertySet.put(notificationTypeProperty);

            Sheet.Set paramsPropertySet = Sheet.createPropertiesSet(); //Task parameters
            paramsPropertySet.setName("Task Parameters");
            paramsPropertySet.setDisplayName("Task Parameters");
            for (String parameterName : task.getParameters().keySet() ) {
                PropertySupport.ReadWrite<String> parameterProperty = 
                        new ParameterPropertySupport(task, parameterName, String.class, parameterName, "");
                paramsPropertySet.put(parameterProperty);
            }
            
            sheet.put(generalPropertySet);
            sheet.put(schedulePropertySet);
            sheet.put(notificationTypePropertySet);
            sheet.put(paramsPropertySet);
        }catch (NoSuchMethodException nsme) { } //Should not happen
        
        return sheet;
    }
    
    public static class TaskChildren extends Children.Keys<LocalUserObjectLight> {
        
        @Override
        public void addNotify() {
            LocalTask theTask = getNode().getLookup().lookup(LocalTask.class);
            setKeys(theTask.getUsers());
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }
        
        @Override
        protected Node[] createNodes(LocalUserObjectLight key) {
            return new Node[] { new TaskUserNode(key) };
        }
    }
    
    private static class ParameterPropertySupport extends PropertySupport.ReadWrite<String> {
        private LocalTask task;
        public ParameterPropertySupport(LocalTask task, String name, Class<String> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
            this.task = task;
        }
        
        @Override
        public String getValue() throws IllegalAccessException, InvocationTargetException {
            return task.getParameters().get(getName());
        }

        @Override
        public void setValue(String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            task.setParameter(getName(), value);
        }
    }
    
    private static class StartTimePropertySupport extends PropertySupport.ReadWrite<Date> {
        private LocalTask task;

        public StartTimePropertySupport(LocalTask task, String name, String displayName, String shortDescription) {
            super(name, Date.class, displayName, shortDescription);
            this.task = task;
        }
        
        @Override
        public Date getValue() throws IllegalAccessException, InvocationTargetException {
            return new Date(task.getStartTime());
        }

        @Override
        public void setValue(Date val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            task.setStartTime(val.getTime());
        }
        
        @Override
        public PropertyEditor getPropertyEditor(){        
            return new DatePickerPropertyEditorSupport(new Date(task.getStartTime()));
        }
    }
    
    private static class NotificationTypePropertySupport extends AbstractComboBoxProperty<IntegerEntry> {
        private LocalTask task;

        public NotificationTypePropertySupport(LocalTask task, IntegerEntry[] tags, String name, 
                                    String displayName, String shortDescription) {
            super(tags, name, IntegerEntry.class, displayName, shortDescription);
            this.task = task;
        }

        @Override
        public void setValue(IntegerEntry val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            task.setNotificationType(val.getValue());
        }

        @Override
        public IntegerEntry getValue() throws IllegalAccessException, InvocationTargetException {
            for (IntegerEntry anEntry : tags) {
                if (anEntry.getValue() == task.getNotificationType())
                    return anEntry;
            }
            return null;
        }
    }
    
    private static class ExecutionTypePropertySupport extends AbstractComboBoxProperty<IntegerEntry> {
        private LocalTask task;

        public ExecutionTypePropertySupport(LocalTask task, IntegerEntry[] tags, String name, 
                                    String displayName, String shortDescription) {
            super(tags, name, IntegerEntry.class, displayName, shortDescription);
            this.task = task;
        }

        @Override
        public void setValue(IntegerEntry val) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            task.setExecutionType(val.getValue());
        }

        @Override
        public IntegerEntry getValue() throws IllegalAccessException, InvocationTargetException {
            for (IntegerEntry anEntry : tags) {
                if (anEntry.getValue() == task.getExecutionType())
                    return anEntry;
            }
            return null;
        }
    }
}
