/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.persistence.application;

/**
 * The store-friendly version of an ExtendedQuery. Its structure is not meant to be executed, but
 * to be transported and stored
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class CompactQuery {
    /**
     * Query id
     */
    protected Long id;
    /**
     * Query description
     */
    protected String name;
    /**
     * Query description
     */
    protected String description;
    /**
     * Query owner
     */
    protected UserProfile owner;
    /**
     * Query body
     */
    protected byte[] content;

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] body) {
        this.content = body;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UserProfile getOwner() {
        return owner;
    }

    public void setOwner(UserProfile owner) {
        this.owner = owner;
    }
}
