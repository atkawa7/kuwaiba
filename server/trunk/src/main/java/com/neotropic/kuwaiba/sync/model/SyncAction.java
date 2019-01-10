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

package com.neotropic.kuwaiba.sync.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * An instance of this class define an action to be performed upon a sync finding
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SyncAction {
    /*
     * The finding should be executed
     */
    private final static int ACTION_EXECUTE = 1;
    /**
     * The finding should be skipped
     */
    private final static int ACTION_SKIP = 0;
    
    private SyncFinding finding;
    
    private int type;

    public SyncAction() { }
    
    public SyncAction(SyncFinding finding, int type) {
        this.finding = finding;
        this.type = type;
    }
   
    public SyncFinding getFinding() {
        return finding;
    }

    public void setFinding(SyncFinding finding) {
        this.finding = finding;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
