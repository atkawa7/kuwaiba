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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Activity {
    /**
     * Unique id within the project.
     */
    private int id;
    /**
     * Activity's title.
     */
    private String title;
    /**
     * Activity's description. Not used so far.
     */
    private String description;
    /**
     * Activity's parent activity. Leave it null if it's a root activity.
     */
    private Activity parentActivity;
    /**
     * Activity's start date.
     */
    private LocalDate startDate;
    /**
     * Duration of the activity.
     */
    private int duration;
    /**
     * How the task should be sorted.
     */
    private int order;
    /**
     * Should the task be collapsed by default? This only applies to activities with children activities.
     */
    private boolean collapsed;
    /**
     * A number between 0 and 1 representing the current progress of the activity.
     */
    private float progress;

    public Activity(int id, String title) {
        this.id = id;
        this.title = title;
        this.duration = 0;
        this.order = 0;
        this.description = "";
        this.collapsed = true;
        this.startDate = LocalDate.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Activity getParentActivity() {
        return parentActivity;
    }

    public void setParentActivity(Activity parentActivity) {
        this.parentActivity = parentActivity;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public boolean isCollapsed() {
        return collapsed;
    }

    public void setCollapsed(boolean collapsed) {
        this.collapsed = collapsed;
    }
    
    /**
     * Returns the date formatted in <code>dd-MM-yyyy</code> format.
     * @return The formatted date. 
     */
    public String getFormattedDate() {
        return startDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
    
    /**
     * Each mapping tells the dependencies (drawn as arrows in the chart.)
     */
    public static class ActivityMapping {
        /**
         * The target task can't start before the source task ends (but it may start later).
         */
        public static final int TYPE_FINISH_TO_START = 0;
        /**
         * The target task can't start until the source task starts (but it may start later).
         */
        public static final int TYPE_START_TO_START = 1;
        /**
         * The target task can't end before the source task ends (but it may end later).
         */
        public static final int TYPE_FINISH_TO_FINISH = 2;
        /**
         * The target task can't end before the source task starts (but it may end later).
         */
        public static final int TYPE_START_TO_FINISH = 3;
        /**
         * Unique mapping id.
         */
        private int id;
        /**
         * Type of dependency. It establishes the constraints for a task to start. See TYPE_XXX for possible values.
         */
        private int type;
        /**
         * The source activity.
         */
        private Activity source;
        /**
         * The target activity
         */
        private Activity target;

        public ActivityMapping(int id, Activity source, Activity target, int type) {
            this.id = id;
            this.source = source;
            this.target = target;
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public Activity getSource() {
            return source;
        }

        public Activity getTarget() {
            return target;
        }

        public int getType() {
            return type;
        }
    }
}
