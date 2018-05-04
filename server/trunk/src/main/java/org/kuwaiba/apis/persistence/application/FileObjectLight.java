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
package org.kuwaiba.apis.persistence.application;

import java.util.List;

/**
 * This is the basic metadata about a file attached to an inventory object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class FileObjectLight {
        /**
     * File object id
     */
    protected long fileOjectId;
    /**
    * The name of the file
    */
    protected String name;
    /**
    * Tags associated to the binary file that can be used to index it or find it in searches
    */
    protected List<String> tags;
    

    public FileObjectLight(long fileOjectId, String name, List<String> tags) {
        this.fileOjectId = fileOjectId;
        this.name = name;
        this.tags = tags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getFileOjectId() {
        return fileOjectId;
    }

    public void setFileOjectId(long fileOjectId) {
        this.fileOjectId = fileOjectId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
