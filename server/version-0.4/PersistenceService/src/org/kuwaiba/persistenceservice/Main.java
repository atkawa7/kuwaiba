/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.persistenceservice;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.persistence.factory.PersistenceLayerFactory;
import org.kuwaiba.persistenceservice.impl.ApplicationEntityManagerImpl;
import org.kuwaiba.persistenceservice.impl.BusinessEntityManagerImpl;
import org.kuwaiba.persistenceservice.impl.MetadataEntityManagerImpl;
import org.kuwaiba.psremoteinterfaces.ApplicationEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.BusinessEntityManagerRemote;
import org.kuwaiba.psremoteinterfaces.MetadataEntityManagerRemote;

/**
 * Application's entry point
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (System.getSecurityManager() == null)
            System.setSecurityManager(new SecurityManager());
        try{

            System.out.println("Current working directory: " + System.getProperty("user.dir"));
            PersistenceLayerFactory plf = new PersistenceLayerFactory();
            final ConnectionManager cm = plf.createConnectionManager();
            System.out.println("Establishing connection to the database...");
            cm.openConnection();
            System.out.println("Connection established");
            cm.printConnectionDetails();
            MetadataEntityManagerRemote meri = new MetadataEntityManagerImpl(cm);
            MetadataEntityManagerRemote memStub = (MetadataEntityManagerRemote)UnicastRemoteObject.exportObject(meri,0);

            BusinessEntityManagerRemote bemri = new BusinessEntityManagerImpl(cm);
            BusinessEntityManagerRemote bemStub = (BusinessEntityManagerRemote)UnicastRemoteObject.exportObject(bemri,0);

            ApplicationEntityManagerRemote aemri = new ApplicationEntityManagerImpl(cm);
            ApplicationEntityManagerRemote aemStub = (ApplicationEntityManagerRemote)UnicastRemoteObject.exportObject(aemri,0);

            

            Registry registry = LocateRegistry.getRegistry();
            System.out.println("Registry obtained...");

            registry.rebind(MetadataEntityManagerRemote.REFERENCE_MEM, memStub);
            registry.rebind(BusinessEntityManagerRemote.REFERENCE_BEM, bemStub);
            registry.rebind(ApplicationEntityManagerRemote.REFERENCE_AEM, aemStub);
            System.out.println("Remote Interface bound");

            Runtime.getRuntime().addShutdownHook(new Thread() {

                    @Override
                    public void run() {
                       System.out.println("Closing connection... ");
                       cm.closeConnection();
                       System.out.println("Connection closed");
                    }
            });

        }catch(Exception e){
            e.printStackTrace();
            System.out.println("Abnormal program termination. See log file for details");
            System.exit(1);
        }
    }

}
