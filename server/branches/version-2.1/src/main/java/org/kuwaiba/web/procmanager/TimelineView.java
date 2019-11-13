//package org.kuwaiba.web.procmanager;
//public class TimelineView {}
/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.web.procmanager;

//import com.neotropic.vaadin14.component.addon.gantt.GanttAddon;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteKpi;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteKpiResult;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
//import com.neotropic.vaadin14.component.addon.gantt.GanttAddon;
//import com.neotropic.vaadin14.component.addon.gantt.model.Link;
//import com.neotropic.vaadin14.component.addon.gantt.model.Task;
import org.openide.util.Exceptions;
//import org.tltv.gantt.Gantt;
//import org.tltv.gantt.client.shared.Step;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
//@Route("TimelineView")
public class TimelineView extends HorizontalLayout {
    private WebserviceBean webserviceBean;
    private RemoteProcessInstance processInstance;
    private RemoteSession session;
    
//    private Gantt gantt;
//    
//    private ListDataProvider<TimelineStep> listDataProvider;
    
    private TimeZone defaultTimeZone;
    
    public TimelineView(){
//        String url = UI.getCurrent().getRouter().getUrl(TimelineView.class);
        getUI().ifPresent(ui -> { 
            RemoteSession logedSession = null;
            logedSession = ui.getSession().getAttribute(RemoteSession.class);
            if(logedSession == null){
                // Session not found
                ui.navigate("login");
            }
            else{
                // Session found
                this.session = logedSession;
                this.webserviceBean = ui.getSession().getAttribute(WebserviceBean.class);
                try {
                    this.processInstance = this.webserviceBean.getProcessInstance(0, session.getIpAddress(), this.session.getSessionId());
                } catch (ServerSideException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
         });
        initGantt();
    }
    
    public TimelineView(RemoteProcessInstance processInstance, WebserviceBean webserviceBean, RemoteSession session) {
        this.webserviceBean = webserviceBean;
        this.processInstance = processInstance;
        this.session = session;
                
        setSizeFull();
        setMargin(false);
        setSpacing(false);
                
//        UI.getCurrent().getPage().getStyles().add(".v-grid tr th, .v-grid tr td { height: 45px; }");
        
//        gantt = new Gantt();
//        gantt.setSizeFull();
//        gantt.setResizableSteps(false);
//        gantt.setMovableSteps(false);
//        gantt.setTimeZone(getDefaultTimeZone());
                
//        Grid<TimelineStep> grid = getGrid();
                
//        addComponent(grid); 
        initGantt();
    }
    
    /**
     * 
     */
    public void initGantt(){
//        List<Task> tasks = new ArrayList<>();
//        tasks.add(getProjectTask());
//        tasks.addAll(getActivitiesTasks());
//        
//        GanttAddon gantt = createGantt(tasks, new ArrayList<>());
//        add(gantt);
    }
    
//    private TimeZone getDefaultTimeZone() {
//        if (defaultTimeZone != null) {
//            return defaultTimeZone;
//        }
//        TimeZone tz = TimeZone.getDefault();
//        if (Gantt.getSupportedTimeZoneIDs().contains(tz.getID()))
//            defaultTimeZone = tz;
//        else
//            defaultTimeZone = TimeZone.getTimeZone("Europe/Madrid");
//        return defaultTimeZone;
//    }
    
    private List<TimelineStep> getTimelineSteps() {        
        List<RemoteActivityDefinition> activityDefinitions = getActivityDefinitions();
        // Current URL
//        String url = UI.getCurrent().getRouter().getUrl(TimelineView.class);
        if (activityDefinitions == null)
            return null;
        
        List<TimelineStep> timelineSteps = new ArrayList();
                        
        for (int i = 0; i < activityDefinitions.size(); i += 1) {
            
            RemoteActivityDefinition activityDefinition = activityDefinitions.get(i);
            
            if (activityDefinition.getType() == ActivityDefinition.TYPE_START || 
                activityDefinition.getType() == ActivityDefinition.TYPE_CONDITIONAL || 
                activityDefinition.getType() == ActivityDefinition.TYPE_END)
                continue;
            
            RemoteArtifact artifact = null;
            try {
                artifact = webserviceBean.getArtifactForActivity(
                        processInstance.getId(),
                        activityDefinition.getId(),
                        session.getIpAddress(),
                        session.getSessionId());
            } catch (ServerSideException ex) {
                // Activity does not have artifact
            }
            RemoteArtifact previousArtifact = null;
            try {
                if (i - 1 >= 0) {
                    previousArtifact = webserviceBean.getArtifactForActivity(
                            processInstance.getId(),
                            activityDefinitions.get(i - 1).getId(),
                            session.getIpAddress(),
                            session.getSessionId());
                }
            } catch (ServerSideException ex) {
                // Activity does not have artifact
            }
            TimelineStep timelineStep = new TimelineStep(
                webserviceBean, 
                processInstance.getProcessDefinition(), 
                activityDefinition, 
                artifact,
                previousArtifact);
            
//            if (activityDefinition.getColor() != null)
//                timelineStep.setBackgroundColor(activityDefinition.getColor().replace("#", "")); //NOI18N                
//            else
//                timelineStep.setBackgroundColor("afafaf"); //NOI18N
//            
//            timelineSteps.add(timelineStep);
//            timelineStep.setHeight(45);
        }
        
        return timelineSteps;
    }
    
//    private void initGanttSteps(List<TimelineStep> timelineSteps) {
//        for (TimelineStep timelineStep : timelineSteps) {
//            gantt.addStep(timelineStep);
//        }
//    }
    
//    private Grid<TimelineStep> getGrid() {
//        List<TimelineStep> timelineSteps = getTimelineSteps();
//        
//        if (timelineSteps == null)
//            return null;
//        
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        
//        for (TimelineStep timelineStep : timelineSteps) {
//            if (timelineStep.getPreviousArtifact() != null &&
//                timelineStep.getPreviousArtifact().getCreationDate() > 0 &&
//                timelineStep.getPreviousArtifact().getCommitDate() > 0 &&
//                timelineStep.getArtifact() != null && 
//                timelineStep.getArtifact().getCreationDate() > 0 &&
//                timelineStep.getArtifact().getCommitDate() > 0) {
//                
//                calendar.setTime(new Date(timelineStep.getPreviousArtifact().getCommitDate()));
//                
//                timelineStep.setActivityStartDate(calendar.getTime());
//                timelineStep.setStartDate(calendar.getTime());
//                
//                if (timelineStep.getArtifact().getCreationDate() == timelineStep.getArtifact().getCommitDate())
//                    calendar.setTime(new Date());
//                else
//                    calendar.setTime(new Date(timelineStep.getArtifact().getCommitDate()));
//                                
//                timelineStep.setActivityEndDate(calendar.getTime());
//                timelineStep.setEndDate(calendar.getTime());
//                
//                long difference = timelineStep.getEndDate() - timelineStep.getStartDate();
//                float days = (difference / (1000*60*60*24));            
//
//                timelineStep.setActivityRealDurationActivity((int) days);
//                
//                int activityExpectedDuration = -1;
//                
//                if (timelineStep.getKpi() != null && 
//                    timelineStep.getKpi().getThresholds() != null && 
//                    timelineStep.getKpi().getThresholds().getProperty("normal") != null) { //NOI18N
//                    
//                    activityExpectedDuration = Integer.valueOf(timelineStep.getKpi().getThresholds().getProperty("normal")); //NOI18N
//                }
//                timelineStep.setActivityExpectedDuration(activityExpectedDuration);
//            }
//            else {
//                Date date = new Date();
//                timelineStep.setStartDate(date);
//                timelineStep.setEndDate(date);
//            }
//        }
//        // Prevents a java.lang.IndexOutOfBoundsException
//        if (timelineSteps.size() > 0) {
//            
//            long startDate = timelineSteps.get(0).getStartDate();
//            long endDate = timelineSteps.get(0).getEndDate();
//
//            for (TimelineStep timelineStep : timelineSteps) {
//
//                if (timelineStep.getStartDate() > 0 && timelineStep.getStartDate() < startDate)
//                    startDate = timelineStep.getStartDate();
//
//                if (timelineStep.getEndDate() > endDate)
//                    endDate = timelineStep.getEndDate();
//            }
//            gantt.setStartDate(new Date(startDate));
//            //Fix the end date of gantt to next day
//            calendar.setTime(new Date(endDate));
//            calendar.set(Calendar.HOUR, 24);            
//            gantt.setEndDate(calendar.getTime());
//        } else {
//            Date date = new Date();
//            gantt.setStartDate(date);
//            
//            calendar.setTime(date);
//            calendar.set(Calendar.HOUR, 24);  
//            
//            gantt.setEndDate(calendar.getTime());
//        }
//                
//        listDataProvider = new ListDataProvider<>(timelineSteps);
//        Grid<TimelineStep> grid = new Grid<>(listDataProvider);
//        grid.setWidth(100, Unit.PERCENTAGE);
//        grid.setHeight(100, Unit.PERCENTAGE);
//        
//        Column<TimelineStep, ?> columnActivityInfo = grid.addColumn(TimelineStep::getActivityNameAndDescription, new HtmlRenderer());
//        columnActivityInfo.setSortable(false);
//        columnActivityInfo.setResizable(false);
//        columnActivityInfo.setCaption("Activity");
//        
//        Column<TimelineStep, ?> columnTimelineIndicatorColor = grid.addColumn(TimelineStep::getTimelineIndicatorColor, new HtmlRenderer());
//        columnTimelineIndicatorColor.setSortable(false);
//        columnTimelineIndicatorColor.setResizable(false);
//                
//        Column<TimelineStep, ?> columnDurationDifference = grid.addColumn(TimelineStep::getRealMinusExpected);
//        columnDurationDifference.setSortable(false);
//        columnDurationDifference.setResizable(false);
//        columnDurationDifference.setCaption("Real - Expected");
//        
//        Column<TimelineStep, ?> columnActor = grid.addColumn(TimelineStep::getActivityActor, new HtmlRenderer());
//        columnActor.setSortable(false);
//        columnActor.setResizable(false);
//        columnActor.setCaption("Actor");
//        
//        Column<TimelineStep, ?> columnStartDate = grid.addColumn(TimelineStep::getActivityStartDateFormat);
//        columnStartDate.setSortable(false);
//        columnStartDate.setResizable(false);
//        columnStartDate.setCaption("Start Date");
//        
//        Column<TimelineStep, ?> columnEndDate = grid.addColumn(TimelineStep::getActivityEndDateFormat);
//        columnEndDate.setSortable(false);
//        columnEndDate.setResizable(false);
//        columnEndDate.setCaption("End Date");
//        
//        Column<TimelineStep, ?> columnExpectedDuration = grid.addColumn(TimelineStep::getActivityExpectedDuration);
//        columnExpectedDuration.setSortable(false);
//        columnExpectedDuration.setResizable(false);
//        columnExpectedDuration.setCaption("Expected Duration");
//        
//        Column<TimelineStep, ?> columnRealDuration = grid.addColumn(TimelineStep::getActivityRealDuration);
//        columnRealDuration.setSortable(false);
//        columnRealDuration.setResizable(false);
//        columnRealDuration.setCaption("Real Duration");               
//        
//        initGanttSteps(timelineSteps);
//        return grid;
//    }
    
    private List<RemoteActivityDefinition> getActivityDefinitions() {
        List<RemoteActivityDefinition> activities = null;
//        String url = UI.getCurrent().getRouter().getUrl(TimelineView.class);
        try {
            
            activities = webserviceBean.getProcessInstanceActivitiesPath(
                processInstance.getId(),
                session.getIpAddress(),
                session.getSessionId());
            
        } catch (ServerSideException ex) {
            return null;// UI.getCurrent().getRouter().getUrl(LoginViewFlow.class)
        }
        List<RemoteActivityDefinition> filteredActivities = new ArrayList();
        // Ignoring the activities with type start, conditional, and end        
        for (RemoteActivityDefinition activity : activities)
            filteredActivities.add(activity);
        
        return filteredActivities;
        
    }
    
    /**
     * Creates a task that will be the main container and it's the Process Name
     * @return a project task 
     */
//    public Task getProjectTask(){
//        Task project = new Task(1, "New Service", null, -1, 0, 0, true, null);
//        return project;
//    }
    
    /**
     * Creates a list of tasks based on process activities
     * @return the tasks list
     */
//    public List<Task> getActivitiesTasks(){
//        List<Task> activities = new ArrayList<>();
//        Task task1 = new Task(2, "Activity #1", "2019-08-01 00:00", 5, 1, 1, false, null);
//        Task task2 = new Task(3, "Activity #2", "2019-08-06 00:00", 2, 1, (float) 0.5, false, "red");
//        
//        // Tasks that need other subtask to be completed wont have a Starting Date or duration
//        Task task3 = new Task(4, "Activity #3", null, -1, 1, (float) 0.8, true, null);
//        
//        // The subtasks need to set the full task as the parent
//        Task task4 = new Task(5, "Activity #3.1", "2019-08-09 00:00", 2, 4, (float) 0.2, false, "yellow");
//        Task task5 = new Task(6, "Activity #3.2", "2019-08-11 00:00", 1, 4, 0, false, null);
//        Task task6 = new Task(7, "Activity #3.3", "2019-08-11 00:00", 1, 4, 0, false, "orange");
//        //tasksList.add(task0);
//        activities.add(task1);
//        activities.add(task2);
//        activities.add(task3);
//        activities.add(task4);
//        activities.add(task5);
//        activities.add(task6);
//        return activities;
//    }
    
    /**
     * This method will create the connections between the activities
     * @return the links list
     */
//    public List<Link> getActivitiesLinks(){
//        List<Link> activitiesLinksList = new ArrayList();
//        
//        // The Link Id's are independant from Tasks Id's
//        Link link1 = new Link(1, 2, 3, "0");
//        Link link2 = new Link(2, 3, 4, "0");
//        Link link3 = new Link(3, 5, 6, "0");
//        activitiesLinksList.add(link1);
//        activitiesLinksList.add(link2);
//        activitiesLinksList.add(link3);
//        
//        return activitiesLinksList;
//    }
    
    /**
     * Creates the Gantt to be added on a layout
     * @param tasks: the list of tasks
     * @param links: the links holding thee connections between tasks
     * @return 
     */
//    public GanttAddon createGantt(List<Task> tasks, List<Link> links){
//        GanttAddon gantt = new GanttAddon(tasks, links);
//        return gantt;
//    }
    
//    public class TimelineStep extends Step {
    public class TimelineStep {
        private final WebserviceBean webserviceBean;
        private final long processDefinitionId;
        private final RemoteActivityDefinition activityDefinition;
        private final RemoteArtifact artifact;
        private final RemoteArtifact previousArtifact;
        private final RemoteKpi kpi;
        private Date activityStartDate;
        private Date activityEndDate;
        private int activityRealDuration = 0;
        private int activityExpectedDuration = 0;
        
        public TimelineStep(WebserviceBean webserviceBean, long processDefinitionId, RemoteActivityDefinition activityDefinition, RemoteArtifact artifact, RemoteArtifact previousArtifact) {
            this.webserviceBean = webserviceBean;
            this.processDefinitionId = processDefinitionId;
            this.activityDefinition = activityDefinition;
            this.artifact = artifact;
            this.previousArtifact = previousArtifact;
            kpi = findKpi(activityDefinition);
        }
        
        public RemoteArtifact getArtifact() {
            return artifact;
        }
        
        public RemoteArtifact getPreviousArtifact() {
            return previousArtifact;
        }
        
        public RemoteKpi getKpi() {
            return kpi;
        }
        
        public String getActivityNameAndDescription() {
            String activityName = activityDefinition != null ? activityDefinition.getName() : null;
            String activityDescription = activityDefinition != null ? activityDefinition.getDescription() : null;
            
            String result = "<div style=\"margin: 0px; padding: 0px;\"><b>" + activityName + "</b><br>(" + activityDescription + ")</div>";
            return result;
        }
        
        public int getActivityRealDuration() {
            return activityRealDuration;
        }
        
        public void setActivityRealDurationActivity(int activityRealDuration) {
            this.activityRealDuration = activityRealDuration;
        }
        
        public int getActivityExpectedDuration() {
            return activityExpectedDuration;
        }
        
        public void setActivityExpectedDuration(int activityExpectedDuration) {
            this.activityExpectedDuration = activityExpectedDuration;
        }
        
        /**
         * Returns a span holding an indicator as a square with different color
         * depending on time kpi
         * @return the indicator
         * @throws ServerSideException 
         */
        public String getTimelineIndicatorColor() throws ServerSideException {
            String color = "#000000";
//            String url = UI.getCurrent().getRouter().getUrl(TimelineView.class);
            
            RemoteKpiResult kpiResult = webserviceBean.executeActivityKpiAction(
                    "time",
                    artifact,
                    processDefinitionId,
                    activityDefinition.getId(),
                    session.getIpAddress(),
                    session.getSessionId());
            if (kpiResult != null) {
                if (kpiResult.getComplianceLevel() == 10)
                    color = "#bffcb3"; //warning
                if (kpiResult.getComplianceLevel() == 5)
                    color = "#f7eea0"; //normal
                if (kpiResult.getComplianceLevel() == 0)
                    color = "#db9090"; //critical
            }
//            String result = "<span class=\"v-icon\" style=\"font-family: "
//                    + VaadinIcon.STOP.getFontFamily() + ";color:" + color
//                    + "\">&#x"
//                    + Integer.toHexString(VaadinIcons.STOP.getCodepoint())
//                    + ";</span>";
            String result = "";
            return result;
        }
        
        public int getRealMinusExpected() {
            return getActivityRealDuration() - getActivityExpectedDuration();            
        }
        
        public String getActivityActor() {
            return activityDefinition.getActor() != null ? "<b>" + activityDefinition.getActor().getName() + "</b>" : null;
        }
                
        public Date getActivityStartDate() {
            return activityStartDate;
        }
        
        public void setActivityStartDate(Date activityStartDate) {
            this.activityStartDate = activityStartDate;
        }
        
        public Date getActivityEndDate() {
            return activityEndDate;
        }
        
        public void setActivityEndDate(Date activityEndDate) {
            this.activityEndDate = activityEndDate;
        }
        
        private String getActivityStartDateFormat() {
            if (getActivityStartDate() == null)
                return null;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return simpleDateFormat.format(getActivityStartDate());
        }
        
        private String getActivityEndDateFormat() {
            if (getActivityEndDate() == null)
                return null;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            return simpleDateFormat.format(getActivityEndDate());
        }        
        
        private RemoteKpi findKpi(RemoteActivityDefinition activityDefinition) {
            if (activityDefinition != null && activityDefinition.getKpis() != null) {
                for (RemoteKpi kpi :activityDefinition.getKpis()) {
                    if (kpi.getName() != null && kpi.getName().equals("time"))
                        return kpi;
                }
            }
            return null;
        } 
    }
}
