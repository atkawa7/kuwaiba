/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.communications;

import org.inventory.communications.wsclient.RemoteSession;

/**
 * Local representation of a session containing the basic information about it.
 * Attributes are read only
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LocalSession {
    /**
     * When creating a session, a session type is required. This is the value if 
     * the session is opened from a desktop client.
     */
    public static final int TYPE_DESKTOP = 1;
    /**
     * When creating a session, a session type is required. This is the value if 
     * the session is opened from the web client.
     */
    public static final int TYPE_WEB = 2;
    /**
     * When creating a session, a session type is required. This is the value if 
     * the session is opened from an automated web service client.
     */
    public static final int TYPE_WEBSERVICE = 3;
    /**
     * The id of the session.
     */
    private String sessionId;
    /**
     * The current user name
     */
    private String username;
    /**
     * The id of the user
     */
    private Long userId;

    public LocalSession(RemoteSession session) {
        this. sessionId = session.getSessionId();
        this.username = session.getUsername();
        this.userId = session.getUserId();
    }

    public String getSessionId() {
        return sessionId;
    }

    private void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return username;
    }

    private void setUsername(String username) {
        this.username = username;
    }

    public Long getUserId() {
        return userId;
    }

    private void setUserId(Long userId) {
        this.userId = userId;
    }

}
