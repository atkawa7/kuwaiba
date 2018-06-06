/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.persistence.application.process;

/**
 * An activity is an step in a process. Conditionals are a particular type of activities from the point of view of this API. This class
 * is a representation of a definition of an activity, which is basically a description of what it does (like presenting a form for the user 
 * to fill it in). The activity definition has at least one artifact definition, which contains (in our example) the actual form.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ActivityDefinition extends ActivityDefinitionLight {
    /**
     * The actor responsible to execute this activity
     */
    private Actor actor;
    /**
     * The next activity according to the flow defined in the process definition
     */
    private ActivityDefinition nextActivity;

    public ActivityDefinition(long id, String name, String description, 
            int type, ArtifactDefinition arfifact, Actor actor) {
        super(id, name, description, type, arfifact);
        this.actor = actor;
    }
    public Actor getActor() {
        return actor;
    }

    public void setActor(Actor actor) {
        this.actor = actor;
    }

    public ActivityDefinition getNextActivity() {
        return nextActivity;
    }

    public void setNextActivity(ActivityDefinition nextActivity) {
        this.nextActivity = nextActivity;
    }
}