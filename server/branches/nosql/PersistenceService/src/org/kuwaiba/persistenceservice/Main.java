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
import org.kuwaiba.persistenceservice.impl.MetadataEntityManagerImpl;
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


//            MetadataEntityManagerRemote meri = new MetadataEntityManagerImpl();

//            MetadataEntityManagerRemote class1Stub = (MetadataEntityManagerRemote)UnicastRemoteObject.exportObject(meri,0);

            System.out.println("Iniciando...");

            Registry registry = LocateRegistry.getRegistry();
            System.out.println("Registro obtenido...");

  //          registry.rebind("mem", class1Stub);

            System.out.println("Remote Interface bound");

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
