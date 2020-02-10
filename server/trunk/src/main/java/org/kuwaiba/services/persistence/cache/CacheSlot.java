/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.services.persistence.cache;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A slot cache allows the caching of complex objects
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class CacheSlot {
    /**
     * The content to be cached
     */
    private Object content;
    /**
     * The date where the content was cached
     */
    private long lastUpdate;
    /**
     * the time of validity of the content
     */
    private long lifeExpectancy;
    /**
     * keeps the logic necessary to refresh the content 
     * 0 if has no expiration
     */
    private String callback;

    public CacheSlot(Object content, long lastUpdate, long lifeExpectancy) {
        this.content = content;
        this.lastUpdate = lastUpdate;
        this.lifeExpectancy = lifeExpectancy;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getLifeExpectancy() {
        return lifeExpectancy;
    }

    public void setLifeExpectancy(long lifeExpectancy) {
        this.lifeExpectancy = lifeExpectancy;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }
       
    public boolean isExpired(){
        return lastUpdate > lifeExpectancy;
    }
}
