/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.kuwaiba.tools.kadmin;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.kuwaiba.tools.kadmin.migration.BackupProviderImpl021;

/**
 * This program is intended to be used to perform the main administrative tasks
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        EntityManager em = Persistence.createEntityManagerFactory("KuwaibaToolsPersistenceUnit").createEntityManager();
        if (em != null){
            System.out.println("Starting export...");
            ByteArrayOutputStream bas = new ByteArrayOutputStream();
            BackupProvider bp = new BackupProviderImpl021();
            bp.startTextBackup(em, bas, "legacy", BackupProvider.TYPE_ALL);
            try{
                FileOutputStream fos = new FileOutputStream("/home/zim/backup.xml");
                fos.write(bas.toByteArray());
                fos.flush();
                fos.close();
                bas.close();
            }catch(IOException e){
                e.printStackTrace();
            }
            System.out.println("Export finished");
        }
        else
            System.out.println("The EntityManager couldn't be created. Please check you database connection setings");
    }

}
