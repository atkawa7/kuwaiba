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

import java.io.File;
import java.io.IOException;

/**
 * Syncronnization manager 
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class SyncManager implements ThreadCompleteListener{
    
    private static final String PATH_DATA_LOAD_LOGS = "../kuwaiba/logs/";
    private long startTime = System.currentTimeMillis();
    private long endTime = 0;
    
    public String bulkUploadFromFile(byte [] uploadData, int commitSize, int dataType, 
            String IPAddress, String sessionId) {
         
        LoadDataFromFile t = new LoadDataFromFile(uploadData, commitSize, dataType, IPAddress, sessionId);
        t.addListener(this);
        t.start();
//        while(t.isAlive()){
//            boolean x = true;
//        }
        //if(notifyOfThreadComplete(t) && t.){
            //System.out.println("That took " + (endTime - startTime) + " milliseconds");
       return t.getUploadFile().getName()+";"+(endTime - startTime);
    }

    @Override
    public boolean notifyOfThreadComplete(Thread thread) {
        long endTime = System.currentTimeMillis();
        return true;
    }
    
    public byte [] downloadBulkLoadLog(String fileName, String ipAddress, String sessionId) throws IOException{
        File file = new File(PATH_DATA_LOAD_LOGS + fileName);
        return LoadDataFromFile.getByteArrayFromFile(file);
    }

}
