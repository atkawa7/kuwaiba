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

package org.kuwaiba.ws.toserialize.application;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import org.kuwaiba.apis.persistence.application.UserProfile;

/**
 * Wrapper for entity class UserProfile.
 * @author Adrian Fernando Martinez Molina <adrian.martinez@kuwaiba.org>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class UserInfoLight implements Serializable {
    /**
     * User's id
     */
    private long id;
   /**
    * User's login name
    */
    private String userName;

    //No-arg constructor required
    public UserInfoLight() { }
    
    public UserInfoLight(UserProfile user) {
            this.id = user.getId();
            this.userName = user.getUserName(); 
    }

    public UserInfoLight(long id, String userName) {
        this.id = id;
        this.userName = userName;
    }
    
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
