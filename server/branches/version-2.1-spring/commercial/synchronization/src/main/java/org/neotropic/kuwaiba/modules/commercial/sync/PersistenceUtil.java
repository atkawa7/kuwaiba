/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.modules.commercial.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.UnsupportedPropertyException;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.neotropic.kuwaiba.modules.commercial.sync.api.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.api.SynchronizationGroup;

/**
 *
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PersistenceUtil {
    /**
     * Converts a node representing a Node into a SynchronizationGroup object
     * @param syncGroupNode The source node
     * @return A SynchronizationGroup object built from the source node information
     * @throws InvalidArgumentException if some element of the list of 
     * syncDataSourceConfiguration has more paramNames than paramValues
     * @throws MetadataObjectNotFoundException
     * @throws UnsupportedPropertyException
     */
    public static SynchronizationGroup createSyncGroupFromNode(Node syncGroupNode)  
            throws InvalidArgumentException, MetadataObjectNotFoundException, UnsupportedPropertyException {    
        
        if (!syncGroupNode.hasProperty(Constants.PROPERTY_NAME))
            throw new InvalidArgumentException(String.format("The sync group with id %s is malformed. Check its properties", syncGroupNode.getId()));

        List<SyncDataSourceConfiguration> syncDataSourceConfiguration = new ArrayList<>();

        for(Relationship rel : syncGroupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP))
            syncDataSourceConfiguration.add(createSyncDataSourceConfigFromNode(rel.getStartNode()));

        return  new SynchronizationGroup(syncGroupNode.getId(),
                (String)syncGroupNode.getProperty(Constants.PROPERTY_NAME),
                syncDataSourceConfiguration);
    }

    /**
     * Converts a node to a SyncDataSourceConfiguration object
     * @param syncDataSourceConfigNode The source node
     * @return A SyncDataSourceConfiguration object built from the source node information
     * @throws InvalidArgumentException if the size of the list of paramNames and paramValues are not the same 
     * @throws UnsupportedPropertyException if any property of the sync data source node is malformed or if there is an error with the relationship between the syncNode an it InventoryObjectNode
     */
    public static SyncDataSourceConfiguration createSyncDataSourceConfigFromNode(Node syncDataSourceConfigNode) throws UnsupportedPropertyException, InvalidArgumentException{   
        
        if (!syncDataSourceConfigNode.hasProperty(Constants.PROPERTY_NAME))
            throw new UnsupportedPropertyException(String.format("The sync configuration with id %s is malformed. Its name is empty", syncDataSourceConfigNode.getId()));
        
        if(!syncDataSourceConfigNode.hasRelationship(RelTypes.HAS_CONFIGURATION))
            throw new UnsupportedPropertyException(String.format("The sync configuration with id %s is malformed. It is not related to any inventory object", syncDataSourceConfigNode.getId()));
        
        Node inventoryObjectNode = syncDataSourceConfigNode.getSingleRelationship(RelTypes.HAS_CONFIGURATION, Direction.OUTGOING).getStartNode();

        HashMap<String, String> parameters = new HashMap<>();
        String configName = "";
      
        for (String property : syncDataSourceConfigNode.getPropertyKeys()) {
            if (property.equals(Constants.PROPERTY_NAME))
                configName = (String)syncDataSourceConfigNode.getProperty(property);
            if(property.equals("deviceId") && !((String)syncDataSourceConfigNode.getProperty(property)).equals(inventoryObjectNode.getProperty(Constants.PROPERTY_UUID)))
                throw new UnsupportedPropertyException(String.format("The sync configuration with id %s is malformed. It seems to be incorrectly related to a network device", inventoryObjectNode.getId()));   
            else
                parameters.put(property, (String)syncDataSourceConfigNode.getProperty(property));
        }
            
        return  new SyncDataSourceConfiguration(syncDataSourceConfigNode.getId(), configName, parameters);
    }
}
