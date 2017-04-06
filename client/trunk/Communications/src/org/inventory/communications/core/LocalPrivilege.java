/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.communications.core;

/**
 * A local, simplified representation of a user/group privilege
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalPrivilege {
    /**
     * Read-only privilege
     */
    public static final int ACCESS_LEVEL_READ = 1;
    /**
     * Read-write privilege
     */
    public static final int ACCESS_LEVEL_READ_WRITE = 2;
    /**
     * Unique identifier of a particular feature
     */
   private String featureToken;
   /**
    * Access level. See ACCESS_LEVEL* for possible values
    */
   private int accessLevel;

    public LocalPrivilege(String featureToken, int accessLevel) {
        this.featureToken = featureToken;
        this.accessLevel = accessLevel;
    }

    public String getFeatureToken() {
        return featureToken;
    }

    public void setFeatureToken(String featureToken) {
        this.featureToken = featureToken;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }
   
   
}
