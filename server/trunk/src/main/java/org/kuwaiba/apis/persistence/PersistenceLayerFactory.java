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

package org.kuwaiba.apis.persistence;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;


/**
 * Factory used to create entity managers independent from the underlying implementation
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class PersistenceLayerFactory{

    /**
     * Dynamically creates a connection manager
     * @return The manager
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public ConnectionManager createConnectionManager() throws InstantiationException, IllegalAccessException {
        try {
             return (ConnectionManager)Class.forName(
                     "org.kuwaiba.services.persistence.impl.neo4j.ConnectionManagerImpl").newInstance();
        }
        catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException ("ConnectionManager implementation not found: " + cnfe.getMessage());
        }
    }
    
    /**
     * Dynamically creates a metadata entity manager
     * @param connectionManager
     * @return An instance of the current Metadata Application Manager
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    public MetadataEntityManager createMetadataEntityManager(ConnectionManager connectionManager) 
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        try {
            Class myClass = (Class) Class.forName(
                    "org.kuwaiba.services.persistence.impl.neo4j.MetadataEntityManagerImpl");
            
            Constructor cmMem = myClass.getConstructor(ConnectionManager.class);
            return (MetadataEntityManager)cmMem.newInstance(connectionManager);
        }
        catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException ("MetadataEntityManager implementation not found: " + cnfe.getMessage());
        }
    }
    
    /**
     * Dynamically creates a application entity manager
     * @param connectionManager
     * @param mem A reference to the current Metadata Entity Manager
     * @return The manager
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException 
     */
    public ApplicationEntityManager createApplicationEntityManager(ConnectionManager connectionManager, MetadataEntityManager mem) 
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        try {
            Class myClass = (Class) Class.forName(
                    "org.kuwaiba.services.persistence.impl.neo4j.ApplicationEntityManagerImpl");
            Constructor cmMem = myClass.getConstructor(ConnectionManager.class, MetadataEntityManager.class);
            return (ApplicationEntityManager)cmMem.newInstance(connectionManager, mem);
        }
        catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException ("ApplicationEntityManager implementation not found: " + cnfe.getMessage());
        }
    }
    
    /**
     * Dynamically creates a business entity manager instance
     * @param connectionManager The Connection Manager to be used inside the BEM instance
     * @param aem A reference to the Application Entity Manager instance  to be used inside the BEM
     * @param mem A reference to the Metadata Entity Manager instance  to be used inside the BEM
     * @return A Business Entity Manager Instance
     * @throws InstantiationException If the manager could not be instantiated due to class path issues)
     * @throws IllegalAccessException If the manager could not be instantiated due to problems accessing the class
     * @throws NoSuchMethodException If the default constructor could not be found
     * @throws IllegalArgumentException If the constructor is called with wrong arguments
     * @throws InvocationTargetException If while instantiating the manager, a run time issue appears
     */
    public BusinessEntityManager createBusinessEntityManager(ConnectionManager connectionManager, ApplicationEntityManager aem, MetadataEntityManager mem) 
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        try {
            Class myClass = (Class) Class.forName(
                    "org.kuwaiba.services.persistence.impl.neo4j.BusinessEntityManagerImpl"); //NOI18N
            
            Constructor bemConstructor = myClass.getConstructor(ConnectionManager.class, ApplicationEntityManager.class, MetadataEntityManager.class);
            return (BusinessEntityManager)bemConstructor.newInstance(connectionManager, aem, mem);
        }
        catch (ClassNotFoundException cnfe) {
            throw new IllegalArgumentException ("BusinessEntityManager implementation not found: " + cnfe.getMessage());
        }
    }   
}
