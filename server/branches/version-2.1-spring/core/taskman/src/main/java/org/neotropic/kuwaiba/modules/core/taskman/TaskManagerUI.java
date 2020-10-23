/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.core.taskman;

import com.neotropic.flow.component.aceeditor.AceEditor;
import com.neotropic.flow.component.aceeditor.AceMode;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.StringUtils;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameter;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleActionParameterSet;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionCompletedListener;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ResultMessage;
import org.neotropic.kuwaiba.core.apis.persistence.application.Task;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskNotificationDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskResult;
import org.neotropic.kuwaiba.core.apis.persistence.application.TaskScheduleDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.application.UserProfileLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.taskman.actions.DeleteTaskParameterVisualAction;
import org.neotropic.kuwaiba.modules.core.taskman.actions.DeleteTaskUserVisualAction;
import org.neotropic.kuwaiba.modules.core.taskman.actions.DeleteTaskVisualAction;
import org.neotropic.kuwaiba.modules.core.taskman.actions.NewTaskParameterVisualAction;
import org.neotropic.kuwaiba.modules.core.taskman.actions.NewTaskUserVisualAction;
import org.neotropic.kuwaiba.modules.core.taskman.actions.NewTaskVisualAction;
import org.neotropic.kuwaiba.modules.core.taskman.tools.TaskManagerRenderingTools;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Main for the Task Manager module. This class manages how the pages corresponding 
 * to different functionalities are presented in a single place.
 * @author Mauricio Ruiz {@literal <mauricio.ruiz@kuwaiba.org>}
 */
@Route(value = "taskman", layout = TaskManagerLayout.class)
public class TaskManagerUI extends VerticalLayout implements ActionCompletedListener, HasDynamicTitle {
    /**
     * Reference to the Translation Service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * The visual action to create a task
     */
    @Autowired
    private NewTaskVisualAction newTaskVisualAction;
    /**
     * The visual action to delete a task
     */
    @Autowired
    private DeleteTaskVisualAction deleteTaskVisualAction;
    /**
     * The visual action to create a task parameter
     */
    @Autowired
    private NewTaskParameterVisualAction newTaskParameterVisualAction;
    /**
     * The visual action to delete a task parameter
     */
    @Autowired
    private DeleteTaskParameterVisualAction deleteTaskParameterVisualAction;
    /**
     * The visual action to subscribe a task user
     */
    @Autowired
    private NewTaskUserVisualAction newTaskUserVisualAction;
    /**
     * The visual action to unsubscribe a task user
     */
    @Autowired
    private DeleteTaskUserVisualAction deleteTaskUserVisualAction;
    /**
     * The grid with the list task
     */
    private final Grid<Task> tblTask;
    /**
     * Object to save the selected task
     */
    private Task currentTask;
    /**
     * The grid with the list task parameters
     */
    private final Grid<StringPair> tblParameters;
    /**
     * Object to save the select task parameter
     */
    private StringPair currentParameter;
    /**
     * The grid with the list task users
     */
    private final Grid<UserProfileLight> tblUsers;
    /**
     * Object to save the selected task user
     */
    private UserProfileLight currentTaskUser;
    /**
     * The grid with the list result messages
     */
    private final Grid<ResultMessage> tblResult;
    /**
     * Split the content
     */
    SplitLayout splitLayout;
    /**
     * Button used to delete task preselected
     */
    Button btnDeleteTask;
    /**
     * Button used to delete task user preselected
     */
    Button btnDeleteTaskUser;
    /**
     * Button used to create new task parameter
     */
    Button btnAddTaskParameter;
    /**
     * Button used to delete task parameter preselected
     */
    Button btnDeleteTaskParameter;
    /**
     * Button used to subscribe a user in a task
     */
    Button btnAddTaskUser;
    
    public TaskManagerUI() {
        super();
        setSizeFull();
        tblTask = new Grid<>();
        tblUsers = new Grid<>();
        tblParameters = new Grid<>();
        tblResult = new Grid<>();
    }

    @Override
    public void actionCompleted(ActionCompletedEvent ev) {
        if (ev.getStatus() == ActionCompletedListener.ActionCompletedEvent.STATUS_SUCCESS) {
            try {
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ev.getMessage()).open();

                if (currentTask != null) {
                    loadTasks();
                    loadTaskUsers(currentTask);
                    loadTaskParameters(currentTask);    
                } else
                    loadTasks();
                
            } catch (UnsupportedOperationException ex) {
                Logger.getLogger(TaskManagerUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else
            new SimpleNotification(ts.getTranslatedString("module.general.messages.error"), ev.getMessage()).open();
    }
    
    @Override
    public void onAttach(AttachEvent ev) {
        setSizeFull();
        setMargin(false);
        setSpacing(false);
        setPadding(false);

        try {
            createContent();
        } catch (InvalidArgumentException ex) {
            Logger.getLogger(TaskManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void onDetach(DetachEvent ev) {
       this.newTaskVisualAction.unregisterListener(this);
       this.deleteTaskVisualAction.unregisterListener(this);
       this.newTaskParameterVisualAction.unregisterListener(this);
       this.deleteTaskParameterVisualAction.unregisterListener(this);
       this.newTaskUserVisualAction.unregisterListener(this);
       this.deleteTaskUserVisualAction.unregisterListener(this);
    }

    private void createContent() throws InvalidArgumentException {
        this.newTaskVisualAction.registerActionCompletedLister(this);
        this.deleteTaskVisualAction.registerActionCompletedLister(this);
        this.newTaskParameterVisualAction.registerActionCompletedLister(this);
        this.deleteTaskParameterVisualAction.registerActionCompletedLister(this);
        this.newTaskUserVisualAction.registerActionCompletedLister(this);
        this.deleteTaskUserVisualAction.registerActionCompletedLister(this);
        
        splitLayout = new SplitLayout();
        splitLayout.setSizeFull();
        splitLayout.setSplitterPosition(25);
        buildTaskGrid();
        
        Button btnAddTask = new Button(this.newTaskVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                    this.newTaskVisualAction.getVisualComponent(new ModuleActionParameterSet()).open();
                });
        
        btnDeleteTask = new Button(this.deleteTaskVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
                (event) -> {
                    this.deleteTaskVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("task", currentTask))).open();
                });
        btnDeleteTask.setEnabled(false);
        
        btnAddTaskParameter = new Button(this.newTaskParameterVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                    this.newTaskParameterVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("task", currentTask))).open();
                });
        
        btnAddTaskUser = new Button(this.newTaskUserVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.PLUS),
                (event) -> {
                    this.newTaskUserVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("task", currentTask))).open();
                });
        
        btnDeleteTaskUser = new Button(this.deleteTaskUserVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
                (event) -> {
                    this.deleteTaskUserVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("user", currentTaskUser),
                            new ModuleActionParameter("task", currentTask)
                    )).open();
                });
        
        btnDeleteTaskParameter = new Button(this.deleteTaskParameterVisualAction.getModuleAction().getDisplayName(), new Icon(VaadinIcon.TRASH),
                (event) -> {
                    this.deleteTaskParameterVisualAction.getVisualComponent(new ModuleActionParameterSet(
                            new ModuleActionParameter("parameter", currentParameter),
                            new ModuleActionParameter("task", currentTask)
                    )).open();
                });
        
        VerticalLayout lytListTask = new VerticalLayout(tblTask, btnAddTask, btnDeleteTask);
        lytListTask.setPadding(false);
        lytListTask.setHeightFull();
        
        splitLayout.addToPrimary(lytListTask);
        add(splitLayout);
    }
    
    private void loadTasks() {
        List<Task> listTask = aem.getTasks();
        tblTask.setItems(listTask);
        tblTask.getDataProvider().refreshAll();
    }

    private void buildTaskGrid() {
        List<Task> listTask = aem.getTasks();
        ListDataProvider<Task> dataProvider = new ListDataProvider<>(listTask);
        tblTask.setDataProvider(dataProvider);
        tblTask.setHeightFull();
        tblTask.addColumn(Task::getName)
                .setKey(ts.getTranslatedString("module.general.labels.name"));
        
        tblTask.addItemClickListener(event -> {
                btnDeleteTask.setEnabled(true);
                currentTask = event.getItem();
                TaskForm taskForm = new TaskForm(currentTask);
                loadTaskNotification(event.getItem());
                splitLayout.addToSecondary(taskForm);  
        });
        
        // Filter task by name
        HeaderRow filterRow = tblTask.appendHeaderRow();
        TextField txtTaskName = createTxtTaskName(dataProvider);
        filterRow.getCell(tblTask.getColumnByKey(ts.getTranslatedString("module.general.labels.name"))).setComponent(txtTaskName);
    }
    
    /**
     * Create a new input field to filter tasks in the header row.
     * @param dataProvider Data provider to filter.
     * @return The new input field filter.
     */
    private TextField createTxtTaskName(ListDataProvider<Task> dataProvider) {
        Icon icon = VaadinIcon.SEARCH.create();
        icon.getElement().setProperty("title", ts.getTranslatedString("module.taskman.task.label.filter-task"));
        icon.setSize("16px");
        
        TextField txtTaskName = new TextField(ts.getTranslatedString("module.general.labels.filter"), ts.getTranslatedString("module.general.labels.filterplaceholder"));
        txtTaskName.setValueChangeMode(ValueChangeMode.EAGER);
        txtTaskName.setWidthFull();
        txtTaskName.setSuffixComponent(icon);
        
        txtTaskName.addValueChangeListener(event -> dataProvider.addFilter(
                project -> StringUtils.containsIgnoreCase(project.getName(), 
                        txtTaskName.getValue())));
        return txtTaskName;
    }

    private void loadTaskUsers(Task task) {
        try {
            List<UserProfileLight> taskUsers = aem.getSubscribersForTask(task.getId());
            tblUsers.setItems(taskUsers);
            tblUsers.getDataProvider().refreshAll();
        } catch (ApplicationObjectNotFoundException ex) {
            Logger.getLogger(TaskManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void buildTaskUsersGrid() {
        tblUsers.addColumn(UserProfileLight::getUserName)
                .setHeader(ts.getTranslatedString("module.taskman.task.actions.new-task-user-name"));
        tblUsers.addItemClickListener(event -> {
            currentTaskUser = event.getItem();
            btnDeleteTaskUser.setEnabled(true);
        });            
    }
    
    private void loadTaskParameters(Task task) {
        try {
            List<StringPair> taskParameters = aem.getTask(task.getId()).getParameters();
            tblParameters.setItems(taskParameters);
            tblParameters.getDataProvider().refreshAll(); 
        } catch (ApplicationObjectNotFoundException ex) {
            Logger.getLogger(TaskManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void buildTaskParametersGrid() {
        tblParameters.addColumn(StringPair::getKey)
                .setHeader(ts.getTranslatedString("module.taskman.task.parameters.name"));
        tblParameters.addColumn(StringPair::getValue)
                .setHeader(String.format("%s (%s)", ts.getTranslatedString("module.taskman.task.parameters.value"), ts.getTranslatedString("module.taskman.task.parameters.header.info")));
        
        tblParameters.addItemClickListener(event -> {
            currentParameter = event.getItem();
            btnDeleteTaskParameter.setEnabled(true);
        });
        tblParameters.addItemDoubleClickListener(event -> {
            currentParameter = event.getItem();
            UpdateParameterDialog dialogUpdate = new UpdateParameterDialog(currentParameter);
            add(dialogUpdate);     
        });
    }
    
    private void loadTaskNotification(Task task) {
        try {
            TaskResult taskResult = aem.executeTask(task.getId());
            tblResult.setItems(taskResult.getMessages());
            tblResult.getDataProvider().refreshAll();
        } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(TaskManagerUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void buildTaskNotificationGrid() {
        tblResult.addComponentColumn(result -> getResultMessage(result))
                .setHeader(ts.getTranslatedString("module.taskman.task.actions.execute-task-header"));
    }
    
    /**
     * Create result message template
     * @param result
     * @return htmlStatus
     */
    private static Div getResultMessage(ResultMessage result) {
        Div htmlStatus = new Div();
        Span html;

        HorizontalLayout lytResult = new HorizontalLayout();
        lytResult.setPadding(true);
        lytResult.setSpacing(false);

        switch (result.getMessageType()) {
            case ResultMessage.STATUS_SUCCESS:
                html = new Span(result.getMessage());   
                htmlStatus.addClassNames("success", "task-result");
                htmlStatus.add(html);
                break;
            case ResultMessage.STATUS_WARNING:
                html = new Span(result.getMessage());  
                htmlStatus.addClassNames("warning", "task-result");
                htmlStatus.add(html);
                break;
            case ResultMessage.STATUS_ERROR:
                html = new Span(result.getMessage());  
                htmlStatus.addClassNames("error", "task-result");
                htmlStatus.add(html);
                break;
            default:
                return null;
        }
        return htmlStatus;
    }

    @Override
    public String getPageTitle() {
        return ts.getTranslatedString("module.taskman.title");
    }
             
    /**
     * This class manages how different functionalities of a selected task are
     * presented in one place.
     */
    private class TaskForm extends VerticalLayout {
        
        public TaskForm(Task task) {            
            Label lblScript = new Label(ts.getTranslatedString("module.taskman.task.properties-general.script"));
            AceEditor editorScript = new AceEditor();
            editorScript.setMode(AceMode.groovy);
            editorScript.setValue(task.getScript());
            editorScript.addAceEditorValueChangedListener(event -> { 
                task.setScript(editorScript.getValue());
            });
            
            // Header Task Properties
            H4 headerMain = new H4(String.format("%s %s", task.getName(), ts.getTranslatedString("module.taskman.task.header-main")));
            
            Checkbox checkCommit = new Checkbox();
            checkCommit.setLabel(ts.getTranslatedString("module.taskman.task.properties-general.commit-on-execute"));
            checkCommit.setValue(task.commitOnExecute());
            checkCommit.addValueChangeListener(event -> {
               task.setCommitOnExecute(checkCommit.getValue());
                try {
                    aem.updateTaskProperties(task.getId(), Constants.PROPERTY_COMMIT_ON_EXECUTE, checkCommit.getValue().toString());
                } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(TaskManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                }
               
            });
            
            Button btnEditProperties = new Button(ts.getTranslatedString("module.taskman.task.actions.edit-properties.name"), new Icon(VaadinIcon.EDIT));
            btnEditProperties.addClickListener(event -> {
                UpdatePropertiesDialog propertiesDialog = new UpdatePropertiesDialog(task);
            });
                        
            Button btnExecuteTask = new Button(ts.getTranslatedString("module.taskman.task.actions.execute-task.name"), new Icon(VaadinIcon.PLAY));
            btnExecuteTask.addClickListener(event -> {
                try {
                    aem.updateTaskProperties(task.getId(), Constants.PROPERTY_SCRIPT, editorScript.getValue());
                } catch (ApplicationObjectNotFoundException | InvalidArgumentException ex) {
                    Logger.getLogger(TaskManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                }
                tblResult.removeAllColumns();
                loadTaskNotification(task);
                TaskNotificationDialog notificationDialog = new TaskNotificationDialog(task);
            });
                        
            HorizontalLayout lytHeaderMain = new HorizontalLayout(headerMain);
            lytHeaderMain.setWidth("45%");
            lytHeaderMain.setPadding(false);
            lytHeaderMain.setMargin(false);
            HorizontalLayout lytHeaderButton = new HorizontalLayout(checkCommit, btnEditProperties, btnExecuteTask);
            lytHeaderButton.setAlignItems(Alignment.END);
            HorizontalLayout lytHeader = new HorizontalLayout(lytHeaderMain, lytHeaderButton);
            lytHeader.setWidthFull();
            lytHeader.setPadding(false);
            lytHeader.setMargin(false);
            VerticalLayout lytScript = new VerticalLayout(lblScript, editorScript);
            lytScript.setHeightFull();
            lytScript.setMargin(false);
            
            add(lytHeader, lytScript);    
        }
    }
    
    /**
     * This class manages updating of the task properties.
     */
    private class UpdatePropertiesDialog extends Dialog {
        
        private UpdatePropertiesDialog(Task task) {
            
            String notificationType;
            String executeType;
            
            // Header Task Properties
            H4 headerMain = new H4(String.format("%s %s", task.getName(), ts.getTranslatedString("module.taskman.task.properties.header-main")));
                   
            Button btnUsers = new Button(ts.getTranslatedString("module.taskman.task.actions.edit-users.name"), new Icon(VaadinIcon.USERS));
            btnUsers.addClickListener(event -> {
               tblUsers.removeAllColumns();
               loadTaskUsers(task);
               updateUsersDialog usersDialog = new updateUsersDialog(task); 
            });
            
            Button btnParameters = new Button(ts.getTranslatedString("module.taskman.task.actions.edit-parameters.name"), new Icon(VaadinIcon.EDIT));
            btnParameters.addClickListener(event -> {
               tblParameters.removeAllColumns();
               loadTaskParameters(task);
               updateParametersDialog parametersDialog = new updateParametersDialog(task);
            });
            
            // Init general properties
            Label headerGeneral = new Label(ts.getTranslatedString("module.taskman.task.properties-general.header"));

            TextField txtName = new TextField(ts.getTranslatedString("module.taskman.task.properties-general.name"));
            txtName.setValue(task.getName());
            txtName.setWidth("33%");
            txtName.addValueChangeListener(event -> {
               task.setName(txtName.getValue());
            });

            TextField txtDescription = new TextField(ts.getTranslatedString("module.taskman.task.properties-general.description"));
            txtDescription.setValue(task.getDescription());
            txtDescription.setWidth("33%");
            txtDescription.addValueChangeListener(event -> {
               task.setDescription(txtDescription.getValue());
            });

            Checkbox checkEnable = new Checkbox();
            checkEnable.setLabel(ts.getTranslatedString("module.taskman.task.properties-general.enable"));
            checkEnable.setValue(task.isEnabled());
            checkEnable.setWidth("17%");
            checkEnable.addValueChangeListener(event -> {
               task.setEnabled(checkEnable.getValue());
            });

            Checkbox checkCommit = new Checkbox();
            checkCommit.setLabel(ts.getTranslatedString("module.taskman.task.properties-general.commit-on-execute"));
            checkCommit.setValue(task.commitOnExecute());
            checkCommit.setWidth("16%");
            checkCommit.addValueChangeListener(event -> {
               task.setCommitOnExecute(checkCommit.getValue());
            });
            // End general properties

            // Init scheduling properties
            Label headerScheduling = new Label(ts.getTranslatedString("module.taskman.task.properties-scheduling.header"));
            
            DateTimePicker dateStart = new DateTimePicker(ts.getTranslatedString("module.taskman.task.properties-scheduling.start-time"));
            dateStart.setValue(TaskManagerRenderingTools.convertLongToLocalDateTime(task.getSchedule().getStartTime()));
            dateStart.setMin(LocalDateTime.now());
            dateStart.setWidth("33%");
            dateStart.isRequiredIndicatorVisible();
            dateStart.addValueChangeListener(event -> {
                task.getSchedule().setStartTime(TaskManagerRenderingTools.convertLocalDateTimeToLong(dateStart.getValue()));
            });
               
            IntegerField intEveryxMinutes = new IntegerField(ts.getTranslatedString("module.taskman.task.properties-scheduling.every-x-minutes"));
            intEveryxMinutes.setValue(task.getSchedule().getEveryXMinutes());
            intEveryxMinutes.setHasControls(true);
            intEveryxMinutes.setMin(1);
            intEveryxMinutes.setWidth("33%");
            intEveryxMinutes.addValueChangeListener(event -> {
               task.getSchedule().setEveryXMinutes(intEveryxMinutes.getValue());
            });

            switch (task.getSchedule().getExecutionType()) {
                case 1:
                    executeType = ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.system-startup");
                    break;
                case 2:
                    executeType = ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.user-login");
                    break;
                case 3:
                    executeType = ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.loop");
                    break;
                default:
                    executeType = ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.on-demand");
                    break;
            }

            ComboBox<ExecutionType> cmbExecutionType = new ComboBox<>(ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type"));
            cmbExecutionType.setItems(
                    new ExecutionType(ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.on-demand"), 0),
                    new ExecutionType(ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.system-startup"), 1),
                    new ExecutionType(ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.user-login"), 2),
                    new ExecutionType(ts.getTranslatedString("module.taskman.task.properties-scheduling.execution-type.loop"), 3)
            );
            cmbExecutionType.setValue(new ExecutionType(executeType, task.getSchedule().getExecutionType()));
            cmbExecutionType.setAllowCustomValue(false);
            cmbExecutionType.setSizeFull();
            cmbExecutionType.setWidth("33%");
            cmbExecutionType.addValueChangeListener(event -> {
               task.getSchedule().setExecutionType(cmbExecutionType.getValue().getType());
            });
            // End scheduling properties

            // Init notification type properties
            Label headerNotificationType = new Label(ts.getTranslatedString("module.taskman.task.properties-notification.header"));

            TextField txtEmail = new TextField(ts.getTranslatedString("module.taskman.task.properties-notification.email"));
            txtEmail.setValue(task.getNotificationType().getEmail());
            txtEmail.setWidth("33%");
            txtEmail.addValueChangeListener(event -> {
               task.getNotificationType().setEmail(txtEmail.getValue());
            });

            switch (task.getNotificationType().getNotificationType()) {
                case 1:
                    notificationType = ts.getTranslatedString("module.taskman.task.properties-notification.type.client-managed");
                    break;
                case 2:
                    notificationType = ts.getTranslatedString("module.taskman.task.properties-notification.type.email");
                    break;
                default:
                    notificationType = ts.getTranslatedString("module.taskman.task.properties-notification.type.no-notification");
                    break;
            }

            ComboBox<NotificationType> cmbnotificationType = new ComboBox<>(ts.getTranslatedString("module.taskman.task.properties-notification.type"));
            cmbnotificationType.setItems(
                    new NotificationType(ts.getTranslatedString("module.taskman.task.properties-notification.type.no-notification"), 0),
                    new NotificationType(ts.getTranslatedString("module.taskman.task.properties-notification.type.client-managed"), 1),
                    new NotificationType(ts.getTranslatedString("module.taskman.task.properties-notification.type.email"), 2)
            );
            cmbnotificationType.setValue(new NotificationType(notificationType, task.getNotificationType().getNotificationType()));
            cmbnotificationType.setAllowCustomValue(false);
            cmbnotificationType.setSizeFull();
            cmbnotificationType.setWidth("33%");
            cmbnotificationType.addValueChangeListener(event -> {
               task.getNotificationType().setNotificationType(cmbnotificationType.getValue().getType());
            });
            // End notification type properties
            
            // Windows to update task properties
            Dialog wdwUpdateProperties = new Dialog();
            
            Button btnSave = new Button(ts.getTranslatedString("module.taskman.task.properties-button.save"), new Icon(VaadinIcon.DOWNLOAD));
            btnSave.addClickListener(event -> {
                try {
                    try {
                        aem.updateTaskProperties(task.getId(), Constants.PROPERTY_NAME, txtName.getValue());
                        aem.updateTaskProperties(task.getId(), Constants.PROPERTY_DESCRIPTION, txtDescription.getValue());
                        aem.updateTaskProperties(task.getId(), Constants.PROPERTY_ENABLED, checkEnable.getValue().toString());
                        aem.updateTaskProperties(task.getId(), Constants.PROPERTY_COMMIT_ON_EXECUTE, checkCommit.getValue().toString());
                    } catch (InvalidArgumentException ex) {
                        Logger.getLogger(TaskManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    // Update Task Notification Type
                    TaskNotificationDescriptor notification =  new TaskNotificationDescriptor(txtEmail.getValue(), cmbnotificationType.getValue().getType());
                    aem.updateTaskNotificationType(task.getId(), notification);
                    // Update Task Schedule
                    TaskScheduleDescriptor schedule = new TaskScheduleDescriptor(task.getSchedule().getStartTime(), intEveryxMinutes.getValue(),  cmbExecutionType.getValue().getType());
                    aem.updateTaskSchedule(task.getId(), schedule);
                    
                    new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.taskman.task.properties-button.notification-saved")).open();
                    wdwUpdateProperties.close();
                    loadTasks();
                } catch (ApplicationObjectNotFoundException ex) {
                    Logger.getLogger(TaskManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                }      
            });
            
            Button btnCancel = new Button(ts.getTranslatedString("module.taskman.task.properties-general.script.button.cancel"), new Icon(VaadinIcon.CLOSE_SMALL));
            btnCancel.addClickListener(event -> {
                wdwUpdateProperties.close();
            });
            
            HorizontalLayout lytHeaderMain = new HorizontalLayout(headerMain);
            lytHeaderMain.setHeight("20%");
            lytHeaderMain.setWidth("70%");
            lytHeaderMain.setPadding(false);
            lytHeaderMain.setMargin(false);
            HorizontalLayout lytHeaderButton = new HorizontalLayout(btnUsers, btnParameters, btnSave, btnCancel);
            lytHeaderButton.setHeight("80%");
            HorizontalLayout lytHeader = new HorizontalLayout(lytHeaderMain, lytHeaderButton);
            lytHeader.setWidthFull();
            lytHeader.setPadding(false);
            lytHeader.setMargin(false);
                        
            HorizontalLayout lytGeneralProperties = new HorizontalLayout(txtName, txtDescription, checkEnable, checkCommit);
            lytGeneralProperties.setWidthFull();
            lytGeneralProperties.setMargin(false);
            lytGeneralProperties.setPadding(false);
            
            HorizontalLayout lytSchedulingProperties = new HorizontalLayout(dateStart, intEveryxMinutes, cmbExecutionType);
            lytSchedulingProperties.setWidthFull();
            lytSchedulingProperties.setMargin(false);
            lytSchedulingProperties.setPadding(false);
            
            HorizontalLayout lytNotificationProperties = new HorizontalLayout(txtEmail, cmbnotificationType);
            lytNotificationProperties.setWidthFull();
            lytNotificationProperties.setMargin(false);
            lytNotificationProperties.setPadding(false);
            
            VerticalLayout lytMain = new VerticalLayout(lytHeader, headerGeneral, lytGeneralProperties, 
                    headerScheduling, lytSchedulingProperties, headerNotificationType, lytNotificationProperties);
            lytMain.setMargin(false);
            lytMain.setPadding(false);
            lytMain.setSizeFull();
            lytMain.setHeightFull();
            
            wdwUpdateProperties.add(lytMain);
            wdwUpdateProperties.setWidthFull();
            wdwUpdateProperties.setHeightFull();
            
            wdwUpdateProperties.open();
                        
        }    
    }
    
    /**
     * This class manages updating of the task users.
     */
    private class updateUsersDialog extends Dialog { 
        
        private updateUsersDialog(Task task) {
            buildTaskUsersGrid();
            Label headerTaskUser = new Label(ts.getTranslatedString("module.taskman.task.users.header"));
            btnDeleteTaskUser.setEnabled(false);
            VerticalLayout lytTaskUsers = new VerticalLayout(headerTaskUser, tblUsers);
            
            // Windows to update task users
            Dialog wdwUpdateUsers = new Dialog();
            
            Button btnCancel = new Button(ts.getTranslatedString("module.taskman.task.properties-general.script.button.cancel"), new Icon(VaadinIcon.CLOSE_SMALL));
            btnCancel.addClickListener(event -> {
                wdwUpdateUsers.close();
            });
            
            HorizontalLayout lytUserButtons = new HorizontalLayout(btnAddTaskUser, btnDeleteTaskUser, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytTaskUsers, lytUserButtons);
            lytMain.setSizeFull();
            lytMain.setHeightFull();
            
            wdwUpdateUsers.add(lytMain);
            wdwUpdateUsers.setWidth("50%");
            wdwUpdateUsers.setHeightFull();
            
            wdwUpdateUsers.open();
        }
    }
    
    /**
     * This class manages updating of the task parameters.
     */
    private class updateParametersDialog extends Dialog { 
        
        private updateParametersDialog(Task task) {
            buildTaskParametersGrid();
            Label headerTaskParameters = new Label(ts.getTranslatedString("module.taskman.task.parameters.header"));
            btnDeleteTaskParameter.setEnabled(false);
            VerticalLayout lytTaskParameters = new VerticalLayout(headerTaskParameters, tblParameters);
            
            // Windows to update task parameters
            Dialog wdwUpdateParameters = new Dialog();
            
            Button btnCancel = new Button(ts.getTranslatedString("module.taskman.task.properties-general.script.button.cancel"), new Icon(VaadinIcon.CLOSE_SMALL));
            btnCancel.addClickListener(event -> {
                wdwUpdateParameters.close();
            });
            
            HorizontalLayout lyParameterButtons = new HorizontalLayout(btnAddTaskParameter, btnDeleteTaskParameter, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytTaskParameters, lyParameterButtons);
            lytMain.setSizeFull();
            lytMain.setHeightFull();
            
            wdwUpdateParameters.add(lytMain);
            wdwUpdateParameters.setWidth("50%");
            wdwUpdateParameters.setHeightFull();
            
            wdwUpdateParameters.open();
        }
    }
    
    /**
     * Dummy class to be used in the scheduling properties for execution type combo box
     */
    private class ExecutionType {

        private final String displayName;
        private final int type;

        public ExecutionType(String displayName, int type) {
            this.displayName = displayName;
            this.type = type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getType() {
            return type;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
    
    /**
     * Dummy class to be used in the notification type properties for notification type combo box
     */
    private class NotificationType {

        private final String displayName;
        private final int type;

        public NotificationType(String displayName, int type) {
            this.displayName = displayName;
            this.type = type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getType() {
            return type;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }
         
    /**
     * This class manages updating of the task parameters.
     */
    private class UpdateParameterDialog extends Dialog {

        private UpdateParameterDialog(StringPair parameter) {
            TextField txtName = new TextField(ts.getTranslatedString("module.taskman.task.parameters.name"));
            txtName.setValue(parameter.getKey());
            txtName.setEnabled(false);
            txtName.setWidthFull();
            
            TextField txtValue = new TextField(ts.getTranslatedString("module.taskman.task.parameters.value"));
            txtValue.setValue(parameter.getValue());
            txtValue.setWidthFull();
            
            // Windows to update task parameter
            Dialog wdwUpdateParameter = new Dialog();
            
            Button btnSave = new Button(ts.getTranslatedString("module.taskman.task.parameters.button.save"));
            btnSave.addClickListener(event -> {
                StringPair updateParameter = new StringPair(txtName.getValue(), txtValue.getValue());
                List<StringPair> listParameter = new ArrayList();
                listParameter.add(updateParameter);
                try {
                    aem.updateTaskParameters(currentTask.getId(), listParameter);
                } catch (ApplicationObjectNotFoundException ex) {
                    Logger.getLogger(TaskManagerUI.class.getName()).log(Level.SEVERE, null, ex);
                } 
                new SimpleNotification(ts.getTranslatedString("module.general.messages.success"), ts.getTranslatedString("module.taskman.task.parameters.button.notification-saved")).open();  
                wdwUpdateParameter.close();
                loadTaskParameters(currentTask);
            });
            
            Button btnCancel = new Button((ts.getTranslatedString("module.taskman.task.parameters.button.cancel")),
                    (event) -> {
                        wdwUpdateParameter.close();
            });
            
            VerticalLayout lytUpdateParameter = new VerticalLayout(txtName, txtValue);
            HorizontalLayout lytMoreButtons = new HorizontalLayout(btnSave, btnCancel);
            VerticalLayout lytMain = new VerticalLayout(lytUpdateParameter, lytMoreButtons);
            lytMain.setSizeFull();
            lytMain.setHeightFull();
            
            wdwUpdateParameter.add(lytMain);
            wdwUpdateParameter.setWidth("30%");
            wdwUpdateParameter.setHeight("50%");
            
            wdwUpdateParameter.open();
        }
    }
    
    /**
     * This class shows the task result messages
     */
    private class TaskNotificationDialog extends Dialog {
        private final Task task;
       
        private TaskNotificationDialog(Task task) {
            this.task = task;
            
            Label headerTask = new Label(String.format("%s %s", ts.getTranslatedString("module.taskman.task.actions.execute-task-result"), task.getName()));
            Dialog wdwTaskNotification = new Dialog();
            Button btnOk = new Button(ts.getTranslatedString("module.taskman.task.actions.execute-task-result.close"),
                    (event) -> {
                        wdwTaskNotification.close();
                    });
            btnOk.setAutofocus(true);
            
            buildTaskNotificationGrid();
            VerticalLayout lytTaskResult = new VerticalLayout(headerTask, tblResult);
            VerticalLayout lytBtn = new VerticalLayout(btnOk);
            lytBtn.setDefaultHorizontalComponentAlignment(Alignment.END);
            VerticalLayout lytMain = new VerticalLayout(lytTaskResult, lytBtn);
            lytMain.setSizeFull();
            lytMain.setHeightFull();
            
            wdwTaskNotification.add(lytMain);
            wdwTaskNotification.setWidth("80%");
            wdwTaskNotification.setHeightFull();
            
            wdwTaskNotification.open();
        }
    }
    
}
