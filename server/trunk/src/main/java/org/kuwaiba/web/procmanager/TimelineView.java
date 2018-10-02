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

import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.renderers.HtmlRenderer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.tltv.gantt.Gantt;
import org.tltv.gantt.client.shared.Step;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class TimelineView extends HorizontalLayout {
    private final WebserviceBean webserviceBean;
    private final RemoteProcessInstance processInstance;
    private final RemoteSession session;
    
    private Gantt gantt;
    
    private ListDataProvider<TimelineStep> listDataProvider;
    
    private TimeZone defaultTimeZone;
        
    public TimelineView(RemoteProcessInstance processInstance, WebserviceBean webserviceBean, RemoteSession session) {
        this.webserviceBean = webserviceBean;
        this.processInstance = processInstance;
        this.session = session;
                
        setSizeFull();
        setMargin(false);
        setSpacing(false);
                
        UI.getCurrent().getPage().getStyles().add(".v-grid tr th, .v-grid tr td { height: 45px; }");
        
        gantt = new Gantt();
        gantt.setSizeFull();
        gantt.setResizableSteps(false);
        gantt.setMovableSteps(false);
        gantt.setTimeZone(getDefaultTimeZone());
                
        Grid<TimelineStep> grid = getGrid();
                
        addComponent(grid); 
        addComponent(gantt); 
                
        setExpandRatio(grid, 0.4f); 
        setExpandRatio(gantt, 0.6f);
                
        gantt.setVerticalScrollDelegateTarget(grid);
    }
    
    private TimeZone getDefaultTimeZone() {
        if (defaultTimeZone != null) {
            return defaultTimeZone;
        }
        TimeZone tz = TimeZone.getDefault();
        if (Gantt.getSupportedTimeZoneIDs().contains(tz.getID()))
            defaultTimeZone = tz;
        else
            defaultTimeZone = TimeZone.getTimeZone("Europe/Madrid");
        return defaultTimeZone;
    }
    
    private List<TimelineStep> getTimelineSteps() {        
        List<RemoteActivityDefinition> activityDefinitions = getActivityDefinitions();
                
        if (activityDefinitions == null)
            return null;
        
        List<TimelineStep> timelineSteps = new ArrayList();
                
        for (RemoteActivityDefinition activityDefinition : activityDefinitions) {
            TimelineStep timelineStep = new TimelineStep(activityDefinition);
            
            if (activityDefinition.getColor() != null)
                timelineStep.setBackgroundColor(activityDefinition.getColor().replace("#", "")); //NOI18N                
            else
                timelineStep.setBackgroundColor("afafaf"); //NOI18N
            
            timelineSteps.add(timelineStep);
            timelineStep.setHeight(45);
        }
        
        return timelineSteps;
    }
    
    private void initGanttSteps(List<TimelineStep> timelineSteps) {
        for (TimelineStep timelineStep : timelineSteps) {
            gantt.addStep(timelineStep);
        }
    }
    
    private Grid<TimelineStep> getGrid() {
        List<TimelineStep> timelineSteps = getTimelineSteps();
        
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        
        for (TimelineStep timelineStep : timelineSteps) {
            timelineStep.setActivityStartDate(calendar.getTime());
            timelineStep.setStartDate(calendar.getTime());
            
            int i = 0;
            
            while (i <= 0)
                i = new Random().nextInt(10);
                        
            calendar.set(Calendar.HOUR, i * 24);
            
            timelineStep.setActivityEndDate(calendar.getTime());
            timelineStep.setEndDate(calendar.getTime());
            
            long difference = timelineStep.getEndDate() - timelineStep.getStartDate();
            float days = (difference / (1000*60*60*24));            
            
            timelineStep.setActivityRealDurationActivity((int) days);
            
            int activityExpectedDuration = 0;
            
            while (activityExpectedDuration <= 0)
                activityExpectedDuration = new Random().nextInt(6);
                        
            timelineStep.setActivityExpectedDuration(activityExpectedDuration);
        }
        // Prevents a java.lang.IndexOutOfBoundsException
        if (timelineSteps.size() > 0) {
            
            long startDate = timelineSteps.get(0).getStartDate();
            long endDate = timelineSteps.get(0).getEndDate();

            for (TimelineStep timelineStep : timelineSteps) {

                if (timelineStep.getStartDate() < startDate)
                    startDate = timelineStep.getStartDate();

                if (timelineStep.getEndDate() > endDate)
                    endDate = timelineStep.getEndDate();
            }

            calendar.setTime(new Date(startDate));
            gantt.setStartDate(calendar.getTime());

            calendar.setTime(new Date(endDate));
            gantt.setEndDate(calendar.getTime());
        } else {
            Date date = new Date();
            gantt.setStartDate(date);
            gantt.setEndDate(date);
        }
                
        if (timelineSteps == null)
            return null;
        
        listDataProvider = new ListDataProvider<>(timelineSteps);
        Grid<TimelineStep> grid = new Grid<>(listDataProvider);
        grid.setWidth(100, Unit.PERCENTAGE);
        grid.setHeight(100, Unit.PERCENTAGE);
        
        Column<TimelineStep, ?> columnActivityInfo = grid.addColumn(TimelineStep::getActivityNameAndDescription, new HtmlRenderer());
        columnActivityInfo.setSortable(false);
        columnActivityInfo.setResizable(false);
        columnActivityInfo.setCaption("Activity");
        
        Column<TimelineStep, ?> columnTimelineIndicatorColor = grid.addColumn(TimelineStep::getTimelineIndicatorColor, new HtmlRenderer());
        columnTimelineIndicatorColor.setSortable(false);
        columnTimelineIndicatorColor.setResizable(false);
                
        Column<TimelineStep, ?> columnDurationDifference = grid.addColumn(TimelineStep::getRealMinusExpected);
        columnDurationDifference.setSortable(false);
        columnDurationDifference.setResizable(false);
        columnDurationDifference.setCaption("Real - Expected");
        
        Column<TimelineStep, ?> columnActor = grid.addColumn(TimelineStep::getActivityActor, new HtmlRenderer());
        columnActor.setSortable(false);
        columnActor.setResizable(false);
        columnActor.setCaption("Actor");
        
        Column<TimelineStep, ?> columnStartDate = grid.addColumn(TimelineStep::getActivityStartDateFormat);
        columnStartDate.setSortable(false);
        columnStartDate.setResizable(false);
        columnStartDate.setCaption("Start Date");
        
        Column<TimelineStep, ?> columnEndDate = grid.addColumn(TimelineStep::getActivityEndDateFormat);
        columnEndDate.setSortable(false);
        columnEndDate.setResizable(false);
        columnEndDate.setCaption("End Date");
        
        Column<TimelineStep, ?> columnExpectedDuration = grid.addColumn(TimelineStep::getActivityExpectedDuration);
        columnExpectedDuration.setSortable(false);
        columnExpectedDuration.setResizable(false);
        columnExpectedDuration.setCaption("Expected Duration");
        
        Column<TimelineStep, ?> columnRealDuration = grid.addColumn(TimelineStep::getActivityRealDuration);
        columnRealDuration.setSortable(false);
        columnRealDuration.setResizable(false);
        columnRealDuration.setCaption("Real Duration");               
        
        initGanttSteps(timelineSteps);
        return grid;
    }
    
    private List<RemoteActivityDefinition> getActivityDefinitions() {
        List<RemoteActivityDefinition> activities = null;
                
        try {
            activities = webserviceBean.getProcessInstanceActivitiesPath(
                processInstance.getId(),
                Page.getCurrent().getWebBrowser().getAddress(),
                session.getSessionId());
        } catch (ServerSideException ex) {
            return null;
        }
        List<RemoteActivityDefinition> filteredActivities = new ArrayList();
        // Ignoring the activities with type start, conditional, and end        
        for (RemoteActivityDefinition activity : activities) {
            
            if (activity.getType() == ActivityDefinition.TYPE_START || 
                activity.getType() == ActivityDefinition.TYPE_CONDITIONAL || 
                activity.getType() == ActivityDefinition.TYPE_END)
                continue;
            
            filteredActivities.add(activity);
        }
        return filteredActivities;
    }
    
    public class TimelineStep extends Step {
        private RemoteActivityDefinition activityDefinition;
        private Date activityStartDate;
        private Date activityEndDate;
        private int activityRealDuration = 0;
        private int activityExpectedDuration = 0;
        
        public TimelineStep(RemoteActivityDefinition activityDefinition) {
            this.activityDefinition = activityDefinition;
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
                
        public String getTimelineIndicatorColor() {
            String color = "#000000";
            
            if (getActivityRealDuration() > getActivityExpectedDuration()) {
                
                if (getActivityRealDuration() <= 7)
                    color = "#f7eea0"; //warning
                else
                    color = "#db9090"; //critical;
            }
            else
                color = "#bffcb3"; //normal
            
            String result = "<span class=\"v-icon\" style=\"font-family: "
                    + VaadinIcons.STOP.getFontFamily() + ";color:" + color
                    + "\">&#x"
                    + Integer.toHexString(VaadinIcons.STOP.getCodepoint())
                    + ";</span>";
            
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
    }
}
