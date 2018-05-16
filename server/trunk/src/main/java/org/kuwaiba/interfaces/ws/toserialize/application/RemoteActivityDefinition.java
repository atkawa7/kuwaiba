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
package org.kuwaiba.interfaces.ws.toserialize.application;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * wrapper of ActivityDefinition. An activity is an step in a process. Conditionals are a particular type of activities from the point of view of this API. This class
 * is a representation of a definition of an activity, which is basically a description of what it does (like presenting a form for the user 
 * to fill it in). The activity definition has at least one artifact definition, which contains (in our example) the actual form.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteActivityDefinition implements Serializable {
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
    private RemoteArtifactDefinition arfifact;
    /**
     * The actor responsible to execute this activity
     */
    private RemoteActor actor;
    /**
     * The next activity according to the flow defined in the process definition
     */
    private RemoteActivityDefinition nextActivity;

    public RemoteActivityDefinition(long id, String name, String description, 
            int type, RemoteArtifactDefinition arfifact, RemoteActor actor) {
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

    public RemoteArtifactDefinition getArfifact() {
        return arfifact;
    }

    public void setArfifact(RemoteArtifactDefinition arfifact) {
        this.arfifact = arfifact;
    }

    public RemoteActor getActor() {
        return actor;
    }

    public void setActor(RemoteActor actor) {
        this.actor = actor;
    }

    public RemoteActivityDefinition getNextActivity() {
        return nextActivity;
    }

    public void setNextActivity(RemoteActivityDefinition nextActivity) {
        this.nextActivity = nextActivity;
    }
}
