/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

/**
 * Represents a single user session
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class Session implements Serializable {
    /**
     * Identifies a session opened from a desktop client
     */
    public static final int TYPE_DESKTOP = 1;
    /**
     * Identifies a session opened from a web client
     */
    public static final int TYPE_WEB = 2;
    /**
     * Identifies a session opened from a web service client
     */
    public static final int TYPE_WS = 3;
    /**
     * Identifies a session opened from a mobile client (a mobile application)
     */
    public static final int TYPE_MOBILE = 4;
    /**
     * User associated to this session
     */
    private UserProfile user;
    /**
     * What type of session depending of the type of client. Only one session per session type is allowed. See Session.TYPE_XXX for possible values
     */
    private int sessionType;
    /**
     * Login timestamp
     */
    private Date loginTime;
    /**
     * Session token
     */
    private String token;
    /**
     * IP Address where this session was established from
     */
    protected String ipAddress;


    public Session(UserProfile user, String ipAddress, int sessionType) {
        this.user = user;
        this.ipAddress = ipAddress;
        this.loginTime = Calendar.getInstance().getTime();
        this.token = generateSessionToken();
        this.sessionType = sessionType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Date loginTime) {
        this.loginTime = loginTime;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserProfile getUser() {
        return user;
    }

    public void setUser(UserProfile user) {
        this.user = user;
    }

    public int getSessionType() {
        return sessionType;
    }

    public void setSessionType(int sessionType) {
        this.sessionType = sessionType;
    }
    
    @Override
    public String toString() {
        return getUser() +"["+getLoginTime()+"]"; //NOI18N
    }

    private String generateSessionToken(){
        try{
            MessageDigest m = MessageDigest.getInstance("MD5");
            byte[] data = loginTime.toString().getBytes();
            m.update(data,0,data.length);
            BigInteger i = new BigInteger(1,m.digest());
            return user.getId() + String.format("%1$032X", i);
        }catch(NoSuchAlgorithmException nsa){
            return null;
        }
    }
}