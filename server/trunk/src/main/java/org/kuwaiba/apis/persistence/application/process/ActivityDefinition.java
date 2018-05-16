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

import java.util.Objects;

/**
 * An activity is an step in a process. Conditionals are a particular type of activities from the point of view of this API. This class
 * is a representation of a definition of an activity, which is basically a description of what it does (like presenting a form for the user 
 * to fill it in). The activity definition has at least one artifact definition, which contains (in our example) the actual form.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ActivityDefinition {
    /**
     * An activity that represents a step in the process. There's always an action associated to it
     */
    public static int TYPE_NORMAL = 1;
    /**
     * A start event. When a process instance is created, the "pointer" to the current activity is set there
     */
    public static int TYPE_START = 2;
    /**
     * An end event. When a process instance doesn't have any more normal actions to be executed, the "pointer" to the current activity is set there. This means that the process instance ended.
     */
    public static int TYPE_END = 3;
    /**
     * A conditional (a logical branch)
     */
    public static int TYPE_CONDITIONAL = 4;
    /**
     * Activity definition id
     */
    private long id;
    /**
     * Activity definition name
     */
    private String name;
    /**
     * Activity definition description
     */
    private String description;
    /**
     * Activity type. See TYPE_* for valid values
     */
    private int type;
    /**
     * Artifact associated to the activity definition
     */
    private ArtifactDefinition arfifact;
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
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.arfifact = arfifact;
        this.actor = actor;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArtifactDefinition getArfifact() {
        return arfifact;
    }

    public void setArfifact(ArtifactDefinition arfifact) {
        this.arfifact = arfifact;
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + this.type;
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj.getClass().isInstance(ActivityDefinition.class) ? ((ActivityDefinition)obj).getId() == this. id : false;
    }
}
