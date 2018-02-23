/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package com.neotropic.kuwaiba.sync.model;

import java.io.Serializable;
import java.util.HashMap;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;

/**
 * Basically a hash-map that stores a set of configuration parameters that will be used by the sync provider
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SyncDataSourceConfiguration implements Serializable {
    /**
     * Configuration id
     */
    private long id;
    /**
     * Configuration name
     */
    private String name;
    /**
     * The parameters stored in this configuration entry
     */
    private HashMap<String, String> parameters;

    public SyncDataSourceConfiguration(long id, String name, HashMap<String, String> parameters) throws InvalidArgumentException {
        this.id = id;
        this.name = name;
        this.parameters = parameters;
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

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }
}
