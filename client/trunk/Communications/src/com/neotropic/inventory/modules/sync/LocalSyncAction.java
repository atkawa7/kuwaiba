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
package com.neotropic.inventory.modules.sync;



/**
 * An instance of this class define an action to be performed upon a sync finding
 * @author Adrian Martinez <adrian.martinez@kuwiba.org>
 */
public class LocalSyncAction {
    /** 
        The finding should be executed
     */
    public final static int ACTION_EXECUTE = 1;
    /**
     * The finding should be skipped
     */
    public final static int ACTION_SKIP = 0;
    /**
     * the finding that has been processed
     */
    private final LocalSyncFinding finding;
    /**
     * the actions that will be executed
     */
    private final int type;
    
    public LocalSyncAction(LocalSyncFinding finding, int type) {
        this.finding = finding;
        this.type = type;
    }

    public LocalSyncFinding getFinding() {
        return finding;
    }

    public int getType() {
        return type;
    }
}
