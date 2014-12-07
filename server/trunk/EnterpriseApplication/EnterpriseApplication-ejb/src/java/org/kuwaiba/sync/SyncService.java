/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.sync;

/**
 *
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class SyncService {
    
    public String bulkUploadFromFile(byte [] uploadData, int commitSize, int dataType, 
            String IPAddress, String sessionId) {
        
         Thread t = new Thread(new LoadDataFromFile(uploadData, commitSize, dataType, IPAddress, sessionId));
         t.start();
        
        return ":)";
    }

}
