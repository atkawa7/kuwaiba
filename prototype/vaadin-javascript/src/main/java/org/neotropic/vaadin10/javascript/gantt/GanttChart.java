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

package org.neotropic.vaadin10.javascript.gantt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A simple Gantt chart 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class GanttChart {
    /**
     * The id used by the div that will contain the chart.
     */
    private String divId;
    /**
     * Chart title.
     */
    private String title;
    /**
     * Chart description. Not used so far.
     */
    private String description;
    /**
     * List of activities in no a particular order
     */
    private List<Activity> activities;
    /**
     * The dependency structure.
     */
    private List<Activity.ActivityMapping> mappings;
    
    public GanttChart(String divId, String title, String description) {
        this.divId =divId;
        this.title = title;
        this.description = description;
        this.activities = new ArrayList<>();
        this.mappings = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public List<Activity.ActivityMapping> getMappings() {
        return mappings;
    }
    
    /**
     * The Javascript block necessary to render the chart in the page.
     * @return 
     */
    public String build() {
        String activitiesBlock =  activities.stream().map((anActivity) -> {
            return "{\n" +
"                       id: " + anActivity.getId() + ", text: \"" + anActivity.getTitle() + 
                        "\", start_date: \"" + anActivity.getFormattedDate() + "\", duration: " + anActivity.getDuration() + ", order: " + anActivity.getOrder() + ",\n" +
"				progress: " + anActivity.getProgress() + 
                                (anActivity.getParentActivity() == null ? ", " : ", parent:" + anActivity.getParentActivity().getId() + ", ")  + 
                                "open: " + !anActivity.isCollapsed() + "\n" +
"	            }\n";
        }).collect(Collectors.joining(","));
        
        String linksBlock = mappings.stream().map((aMapping) -> {
            return "{" +
"                       id: " + aMapping.getId() + ", source: " + aMapping.getSource().getId() + ", target: " + aMapping.getTarget().getId() + ", type: \"" + aMapping.getType() + "\"" +
"                   }\n";
        }).collect(Collectors.joining(","));
        
        return "var tasks = {\n" +
"		data: [\n" +
                    activitiesBlock +
"               ],\n" +
"		links: [\n" +
                    linksBlock +
"		]\n" +
"	};\n" +
"\n" +
"	gantt.init(\"" + divId + "\");\n" +
"\n\n" +
"	gantt.parse(tasks);";
        
    }
}
