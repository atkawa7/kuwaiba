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
import org.kuwaiba.apis.persistence.application.process.ActivityDefinition;
import org.kuwaiba.apis.persistence.application.process.ConditionalActivityDefinition;

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
     * Define if an Activity can be mark as idle activity
     */    
    private boolean idling;
    /**
     * Define if is necessary Confirm before commit the Activity
     */
    private boolean confirm;
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
            int type, RemoteArtifactDefinition arfifact, RemoteActor actor, boolean idling, boolean confirm) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.arfifact = arfifact;
        this.actor = actor;
        this.idling = idling;
        this.confirm = confirm;
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
    
    public boolean isIdling() {
        return idling;
    }
        
    public void setIdling(boolean idling) {
        this.idling = idling;                
    }
    
    public boolean confirm() {
        return confirm;
    }
        
    public void setConfirm(boolean confirm) {
        this.confirm = confirm;        
    }
    
    public static RemoteActivityDefinition asRemoteActivityDefinition(ActivityDefinition activityDefinition) {
        RemoteActivityDefinition res = null;
        
        if (activityDefinition instanceof ConditionalActivityDefinition) {
            res = new RemoteConditionalActivityDefinition(activityDefinition.getId(), activityDefinition.getName(), 
                activityDefinition.getDescription(), activityDefinition.getType(), 
                new RemoteArtifactDefinition(activityDefinition.getArfifact().getId(), activityDefinition.getArfifact().getName(), activityDefinition.getArfifact().getDescription(), activityDefinition.getArfifact().getVersion(), activityDefinition.getArfifact().getType(), activityDefinition.getArfifact().getDefinition(), activityDefinition.getArfifact().getPreconditionsScript(), activityDefinition.getArfifact().getPostconditionsScript(), activityDefinition.getArfifact().isPrintable(), activityDefinition.getArfifact().getPrintableTemplate()), 
                new RemoteActor(activityDefinition.getActor().getId(), activityDefinition.getActor().getName(), activityDefinition.getActor().getType()),
                activityDefinition.confirm());
            
            if (((ConditionalActivityDefinition) activityDefinition).getNextActivityIfTrue() != null) 
                ((RemoteConditionalActivityDefinition) res).setNextActivityIfTrue(RemoteActivityDefinition.asRemoteActivityDefinition(((ConditionalActivityDefinition) activityDefinition).getNextActivityIfTrue()));
            
            if (((ConditionalActivityDefinition) activityDefinition).getNextActivityIfFalse() != null) 
                ((RemoteConditionalActivityDefinition) res).setNextActivityIfFalse(RemoteActivityDefinition.asRemoteActivityDefinition(((ConditionalActivityDefinition) activityDefinition).getNextActivityIfFalse()));
            
        } else {
            res = new RemoteActivityDefinition(activityDefinition.getId(), activityDefinition.getName(), 
                activityDefinition.getDescription(), activityDefinition.getType(), 
                new RemoteArtifactDefinition(activityDefinition.getArfifact().getId(), activityDefinition.getArfifact().getName(), activityDefinition.getArfifact().getDescription(), activityDefinition.getArfifact().getVersion(), activityDefinition.getArfifact().getType(), activityDefinition.getArfifact().getDefinition(), activityDefinition.getArfifact().getPreconditionsScript(), activityDefinition.getArfifact().getPostconditionsScript(), activityDefinition.getArfifact().isPrintable(), activityDefinition.getArfifact().getPrintableTemplate()), 
                new RemoteActor(activityDefinition.getActor().getId(), activityDefinition.getActor().getName(), activityDefinition.getActor().getType()),
                activityDefinition.isIdling(),
                activityDefinition.confirm());
            
            if (activityDefinition.getNextActivity() != null) 
                res.setNextActivity(RemoteActivityDefinition.asRemoteActivityDefinition(activityDefinition.getNextActivity()));
        }
        return res;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RemoteActivityDefinition other = (RemoteActivityDefinition) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name != null ? name : "";
    }
    
}
