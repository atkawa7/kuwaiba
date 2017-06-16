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
package org.kuwaiba.apis.persistence.integrity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.exceptions.ConnectionException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.ConnectionManager;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.Privilege;
import org.kuwaiba.services.persistence.util.Constants;

/**
 * 
 * @author Adrian Fernando Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class PrivilegesReader {

    private ConnectionManager cm;
    private DataIntegrityService dis;
    private int nPrivileges;
    
    public PrivilegesReader() throws ConnectionException, InvalidArgumentException {
        cm.openConnection();
        dis = new DataIntegrityService(cm);
        nPrivileges = 0;
    }
    
    public void read(File file) throws IOException{
        
//        PersistenceService ps = PersistenceService.getInstance();
//        ps.start();
//        cm = ps.getConnectionManager();
//        
//        FileReader fr = new FileReader(file);
//        BufferedReader br = new BufferedReader(fr);
//        try{
//            String line = br.readLine();
//            while(line != null){
//                String[] split = line.split(";");
//                String[] dependsOfSplit = split[4].split(",");
//                long[] dependsOf = new long[dependsOfSplit.length];
//                for (int i=0; i<dependsOfSplit.length; i++) 
//                    dependsOf[i] = Long.valueOf(dependsOfSplit[i]);
//                dis.createPrivileges(new Privilege(Long.valueOf(split[2]), split[1], split[3], split[0], dependsOf));
//                line = br.readLine();
//                nPrivileges++;
//            }
//        } finally {
//            br.close();
//        }
//        ps.stop();
    }
    
    public void setDefaultSettings() throws InvalidArgumentException, NotAuthorizedException{
//        PersistenceService ps = PersistenceService.getInstance();
//        ps.start();
//        cm = ps.getConnectionManager();
//        ApplicationEntityManager aem = ps.getApplicationEntityManager();
//        
//        long[] privileges = new long[nPrivileges];
//        for (int i = 0 ; i < nPrivileges ; i++) 
//            privileges[i] = (long)i+1;
//        try{
//            aem.createGroup(Constants.NODE_USERS, "users root group", new long[]{1,2}, null);
//            aem.createUser("user", "kuwaiba", "James", "Rodriguez", true, null, null);
//            long adminGroupId = aem.createGroup("administrators", "Administrators Group", privileges, null);
//            aem.createUser("admin", "kuwaiba", "Falcao", "garcia", true, null, new long[]{adminGroupId});
//        }catch(InvalidArgumentException ex){
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "setDefaultSettings: {0}", ex.getMessage()); //NOI18N
//        }
//        ps.stop();
    }
}
