/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;

/**
 * Synchronization manager 
 * @author adrian martinez molina <adrian.martinez@kuwaiba.org>
 */
public class SyncManager{
    
    private static final String PATH_DATA_LOAD_LOGS = "../kuwaiba/logs/";
    
    public String bulkUploadFromFile(byte [] uploadData, int commitSize, int dataType, 
            String IPAddress, String sessionId) {
         
        LoadDataFromFile ldf = new LoadDataFromFile(uploadData, commitSize, dataType, IPAddress, sessionId);
        try {
            return ldf.uploadFile();
        } catch (ApplicationObjectNotFoundException | NotAuthorizedException | RemoteException | MetadataObjectNotFoundException | InvalidArgumentException | ObjectNotFoundException | OperationNotPermittedException | WrongMappingException ex) {
            Logger.getLogger(SyncManager.class.getName()).log(Level.SEVERE, null, ex);
        }
         return null;
    }

    public byte [] downloadBulkLoadLog(String fileName, String ipAddress, String sessionId) throws IOException{
        File file = new File(PATH_DATA_LOAD_LOGS + fileName);
        return LoadDataFromFile.getByteArrayFromFile(file);
    }
}
