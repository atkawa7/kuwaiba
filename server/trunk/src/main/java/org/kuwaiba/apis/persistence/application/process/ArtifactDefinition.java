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
 * Represents an artifact associated to an activity. An artifact is the product of the execution of an activity. 
 * Most of the times, it will be a form filled in by the user.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ArtifactDefinition {
    /**
     * The artifact is a form that should be filled in and submitted by the user
     */
    public static final int TYPE_FORM = 1;
    /**
     * The type used for the TYPE_CONTIDIONAL activities, and simply asks a question expecting a yes/no answer
     */
    public static final int TYPE_CONDITIONAL = 2;
    /**
     * The activity only requires the user to submits a file
     */
    public static final int TYPE_ATTACHMENT = 3;
    /**
     * Artifact id
     */
    private long id;
    /**
     * Artifact name
     */
    private String name;
    /**
     * Artifact description
     */
    private String description;
    /**
     * The version of the artifact, expressed as a three numeric sections separated by a dot (e.g. 1.3.1)
     */
    private String version;
    /**
     * Artifact type. See TYPE_* for valid values
     */
    private int type;
    /**
     * The actual definition. It's an XML document 
     */
    private byte[] definition;

    public ArtifactDefinition(long id, String name, String description, String version, int type, byte[] definition) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.version = version;
        this.type = type;
        this.definition = definition;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
    
    public byte[] getDefinition() {
        return definition;
    }

    public void setDefinition(byte[] definition) {
        this.definition = definition;
    }
}