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

package org.kuwaiba.apis.persistence.application;

import java.io.Serializable;

/**
 * A simplified representation of a user
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class UserProfileLight implements Serializable {

    public static final String PROPERTY_ID = "id"; //NOI18N
    public static final String PROPERTY_USERNAME = "username"; //NOI18N
    public static final String PROPERTY_DESCRIPTION = "description"; //NOI18N
    public static final String PROPERTY_PASSWORD = "password"; //NOI18N
    public static final String PROPERTY_SALT = "salt"; //NOI18N
    public static final String PROPERTY_FIRST_NAME = "firstName"; //NOI18N
    public static final String PROPERTY_LAST_NAME = "lastName"; //NOI18N
    public static final String PROPERTY_PRIVILEGES = "privileges"; //NOI18N
    public static final String PROPERTY_CREATION_DATE = "creationDate"; //NOI18N
    public static final String PROPERTY_ENABLED = "enabled"; //NOI18N
    /**
     * User's id
     */
    private long id;
    /**
     * User's username
     */
    private String userName;
    

    public UserProfileLight() {}

    public UserProfileLight(long id, String userName) {
        this.id = id;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String login) {
        this.userName = login;
    }

    public long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}