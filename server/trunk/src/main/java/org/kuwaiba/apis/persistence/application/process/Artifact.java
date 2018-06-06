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
 * Every process activity has at least one artifact. An artifact is the result of 
 * executing an activity. Most of the times, an artifact is simply a form filled in by a user
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Artifact {
    /**
     * Artifact id
     */
    private long id;
    /**
     * The name of the artifact
     */
    private String name;
    /**
     * What kind of artifact is it. This value helps a process renderer to know how to interpret the content
     */
    private String contentType;
    /**
     * An XML document that contains the actual artifact. It may be a form already filled in, or an XML with CDATA section 
     * containing a binary file
     */
    private byte[] content;

    public Artifact(long id, String name, String contentType, byte[] content) {
        this.name = name;
        this.contentType = contentType;
        this.content = content;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    
}