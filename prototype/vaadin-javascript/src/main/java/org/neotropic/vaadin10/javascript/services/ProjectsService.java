/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the MIT and GPLv3 Licenses, Version 1.0 (the "Licenses");
 *  you may not use this file except in compliance with the Licenses.
 *  You may obtain a copy of the License at
 *
 *       https://opensource.org/licenses/MIT
 *       https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the Licenses is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the Licenses.
 */

package org.neotropic.vaadin10.javascript.services;

import java.time.LocalDate;
import org.neotropic.vaadin10.javascript.gantt.Activity;
import org.neotropic.vaadin10.javascript.gantt.GanttChart;
import org.springframework.stereotype.Service;

/**
 * A simple service to demonstrate how to inject services in views
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class ProjectsService {
    
    public String createProject() {
        GanttChart chart = new GanttChart("gantt", "Hell-o World Project", "Hell-o World Project");
        
        //Root activity
        Activity activity1 = new Activity(1, "Hell-o World Root Activity 01");
        activity1.setDuration(15);
        activity1.setOrder(10);
        activity1.setProgress(0.5f);
        activity1.setStartDate(LocalDate.now()); //Not really necessary, as the default start day is always today.
        activity1.setCollapsed(false); //Show it expanded. The default value is "true", so in children task without children on their own, this is not necessary
        
        //First child
        Activity activity2 = new Activity(2, "Hell-o World Child Activity 01");
        activity2.setDuration(5);
        activity2.setOrder(20);
        activity2.setProgress(0.2f);
        activity2.setParentActivity(activity1);
        
        //Second child
        Activity activity3 = new Activity(3, "Hell-o World Child Activity 02");
        activity3.setDuration(10);
        activity3.setOrder(30);
        activity3.setProgress(0.8f);
        activity3.setParentActivity(activity1);
        activity3.setStartDate(LocalDate.now().plusDays(5));
        
        chart.getActivities().add(activity1);
        chart.getActivities().add(activity2);
        chart.getActivities().add(activity3);
        
        chart.getMappings().add(new Activity.ActivityMapping(1, activity2, activity3, Activity.ActivityMapping.TYPE_FINISH_TO_START));
        
        return chart.build();
    }
}
