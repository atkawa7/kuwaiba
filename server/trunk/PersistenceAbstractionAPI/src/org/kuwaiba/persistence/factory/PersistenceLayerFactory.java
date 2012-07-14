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

package org.kuwaiba.persistence.factory;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.kuwaiba.apis.persistence.interfaces.BusinessEntityManager;
import org.kuwaiba.apis.persistence.interfaces.MetadataEntityManager;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;


/**
 *
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class PersistenceLayerFactory{

    public BusinessEntityManager createBusinessEntityManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Creates a Connection manager
     * @return ConnectionManager
     * @throws InstantiationException
     * @throws IllegalAccessException
     */

    public ConnectionManager createConnectionManager() throws InstantiationException, IllegalAccessException {
        try
        {
             ConnectionManager cmn = (ConnectionManager)Class.forName(
                     "org.kuwaiba.persistenceservice.impl.ConnectionManagerImpl").newInstance();
             
            return cmn;
        }
        catch (ClassNotFoundException cnfe) // driver not found
        {
                System.err.println ("Unable to load database driver");
                System.err.println ("Details : " + cnfe);
                System.exit(0);
        }
        return null;
    }

    /*public DataMiningPersistenceManager createDataMiningPersistenceManager() {
        throw new UnsupportedOperationException("Not supported yet.");
    }*/

    /**
     * Attempt to load database driver
     * @param connectionManager
     * @return MetadataEntityManager
     * @throws InstantiationException
     * @throws IllegalAccessException
     */

    public MetadataEntityManager createMetadataEntityManager(ConnectionManager connectionManager) throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        try
        {
            Class myClass = (Class) Class.forName(
                    "org.kuwaiba.persistenceservice.impl.MetadataEntityManagerImpl");
            
            Constructor cmMem = myClass.getConstructor(ConnectionManager.class);

            MetadataEntityManager mem = (MetadataEntityManager)cmMem.newInstance(connectionManager);
            
            return mem;
        }
        catch (ClassNotFoundException cnfe) // driver not found
        {
                System.err.println ("Unable to load database driver");
                System.err.println ("Details : " + cnfe);
                System.exit(0);
        }
        return null;
    }
    
}
