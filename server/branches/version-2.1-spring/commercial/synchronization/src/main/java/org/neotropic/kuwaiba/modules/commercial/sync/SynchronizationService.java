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
import java.util.List;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.modules.commercial.sync.api.SyncDataSourceConfiguration;
import org.neotropic.kuwaiba.modules.commercial.sync.api.SynchronizationGroup;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.UnsupportedPropertyException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.RelTypes;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The service corresponding to the Synchronization module.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class SynchronizationService {
    @Autowired
    private TranslationService ts;
    /**
     * The Application Entity Manager instance.
     */
    @Autowired
    private ApplicationEntityManager aem;
    /**
     * The Business Entity Manager instance.
     */
    @Autowired
    private BusinessEntityManager bem;
    /**
     * The Metadata Entity Manager instance.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * The Connection Manager instance.
     */
    @Autowired
    private ConnectionManager<GraphDatabaseService> connectionManager;
    /**
     * SyncGroup label
     */
    private Label syncGroupsLabel = Label.label(Constants.LABEL_SYNCGROUPS);
    /**
     * SyncDataSourceConfig label
     */
    private Label syncDatasourceConfigLabel = Label.label(Constants.LABEL_SYNCDSCONFIG);
    /**
     * Object label
     */
    private Label inventoryObjectsLabel = Label.label(Constants.LABEL_INVENTORY_OBJECTS);
    
    /**
     * Fetches a synchronization group. From the conceptual point of view, a sync group is a set of Synchronization Data Sources.
     * @param syncGroupId The id of the sync group.
     * @return The sync group.
     * @throws ApplicationObjectNotFoundException If the sync group could not be found.
     * @throws InvalidArgumentException If the sync data group information is somehow malformed in the database.
     * @throws MetadataObjectNotFoundException If can not find the class name of the device related with the data source configuration.
     * @throws UnsupportedPropertyException If the sync group can not be mapped into a Java object.
     */
    public SynchronizationGroup getSyncGroup(long syncGroupId) throws InvalidArgumentException, ApplicationObjectNotFoundException,
            MetadataObjectNotFoundException, UnsupportedPropertyException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);
            if (syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.sync.actions.get-sync-group.messages.sync-group-not-found"), syncGroupId));
            return PersistenceUtil.createSyncGroupFromNode(syncGroupNode);
        } 
    }
    /**
     * Gets the list of available sync groups 
     * @return The list of available sync groups
     * @throws InvalidArgumentException If any of the sync groups is malformed in the database
     * @throws org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException
     * @throws org.neotropic.kuwaiba.core.apis.persistence.exceptions.UnsupportedPropertyException
     */
    public List<SynchronizationGroup> getSyncGroups() throws InvalidArgumentException, MetadataObjectNotFoundException, UnsupportedPropertyException {
        List<SynchronizationGroup> synchronizationGroups = new ArrayList<>();
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            ResourceIterator<Node> syncGroupsNodes = connectionManager.getConnectionHandler().findNodes(syncGroupsLabel);

            while (syncGroupsNodes.hasNext()) {
                Node syncGroup = syncGroupsNodes.next();
                synchronizationGroups.add(PersistenceUtil.createSyncGroupFromNode(syncGroup));            
            }            
            tx.success();
            return synchronizationGroups;
        }
    }
    /**
     * Gets a data source configuration of the object (there is only one data source configuration per object)
     * @param objectId the object id (a GenericCommunicationElement) or the SyncDataSourceConfig id
     * @return a SyncDataSourceConfiguration
     * @throws InvalidArgumentException  If any of the configurations is malformed in the database
     * @throws ApplicationObjectNotFoundException If the sync data source configuration  could not be found
     * @throws UnsupportedPropertyException if any property of the sync data source node is malformed or if there is an error with the relationship between the syncNode an it InventoryObjectNode
     */
    public SyncDataSourceConfiguration getSyncDataSourceConfiguration(String objectId) 
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node inventoryObjectNode = connectionManager.getConnectionHandler().findNode(inventoryObjectsLabel, Constants.PROPERTY_UUID, objectId);
            
            Node syncDatasourceConfiguration = null;
            if(inventoryObjectNode != null) { 
                if(!inventoryObjectNode.hasRelationship(RelTypes.HAS_CONFIGURATION))
                   throw new UnsupportedPropertyException(String.format(ts.getTranslatedString("module.sync.actions.get-sync-data-source-configuration.messages.no-ds-config"), 
                           inventoryObjectNode.getProperty(Constants.PROPERTY_NAME), objectId));
                
                syncDatasourceConfiguration = inventoryObjectNode.getSingleRelationship(RelTypes.HAS_CONFIGURATION, Direction.OUTGOING).getEndNode();
                if(syncDatasourceConfiguration == null)
                    throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.sync.actions.get-sync-data-source-configuration.messages.malformed-ds-config"), objectId));
            }

            tx.success();
            return PersistenceUtil.createSyncDataSourceConfigFromNode(syncDatasourceConfiguration);
        }
    }
    /**
     * Gets a synchronization data source configuration receiving its id as search criteria.
     * @param syncDatasourceId The sync data source configuration id.
     * @return A SyncDatasourceConfiguration instance.
     * @throws InvalidArgumentException  If any of the configurations is malformed in the database
     * @throws ApplicationObjectNotFoundException if the syncDatasource could not be found
     * @throws UnsupportedPropertyException if any property of the sync data source node is malformed or if there is an error with the relationship between the syncNode an it InventoryObjectNode
     */
    public SyncDataSourceConfiguration getSyncDataSourceConfigurationById(long syncDatasourceId) 
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
             
            Node syncDatasourceConfiguration = connectionManager.getConnectionHandler().getNodeById(syncDatasourceId);
            if(syncDatasourceConfiguration == null)
                throw new ApplicationObjectNotFoundException(String.format("The sync data source configuration with id: %s is not related with anything", syncDatasourceId));
            
            tx.success();
            return PersistenceUtil.createSyncDataSourceConfigFromNode(syncDatasourceConfiguration);
        }
    }
    /**
     * Gets the data source configurations associated to a sync group. A data source configuration is a set of parameters to access a sync data source
     * @param syncGroupId The sync group the requested configurations belong to.
     * @return A list of data source configurations.
     * @throws ApplicationObjectNotFoundException If the sync group could not be found.
     * @throws InvalidArgumentException If any of the configurations is malformed in the database.
     * @throws UnsupportedPropertyException If the sync data source can not be mapped into a Java object.
     */
    public  List<SyncDataSourceConfiguration> getSyncDataSourceConfigurations(long syncGroupId) 
            throws InvalidArgumentException, ApplicationObjectNotFoundException, UnsupportedPropertyException {
        List<SyncDataSourceConfiguration> syncDataSourcesConfigurations = new ArrayList<>();
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);
            
            if (syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format(ts.getTranslatedString("module.sync.actions.get-sync-group.messages.sync-group-not-found"), syncGroupId));
            
            for(Relationship rel : syncGroupNode.getRelationships(Direction.INCOMING, RelTypes.BELONGS_TO_GROUP))
                syncDataSourcesConfigurations.add(PersistenceUtil.createSyncDataSourceConfigFromNode(rel.getStartNode()));
            
            tx.success();
        }
        
        return syncDataSourcesConfigurations;
    }
    /**
     * Creates a synchronization group
     * @param name The name of the new group
     * @return The id of the newly created group
     * @throws InvalidArgumentException If any of the parameters is invalid
     * @throws ApplicationObjectNotFoundException If the sync provider could not be found
     */
    public long createSyncGroup(String name) throws InvalidArgumentException, ApplicationObjectNotFoundException {
        if (name == null || name.trim().isEmpty())
                throw new InvalidArgumentException("The name of the sync group can not be empty");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = connectionManager.getConnectionHandler().createNode(syncGroupsLabel);
            syncGroupNode.setProperty(Constants.PROPERTY_NAME, name);
           
            tx.success();

            return syncGroupNode.getId();
        }
    }
    /**
     * Updates the data source configurations associated to a given sync group
     * @param syncGroupId The Id of the sync group to be updated
     * @param syncGroupProperties The list of synchronization group properties
     * @throws ApplicationObjectNotFoundException If the sync group could not be found
     * @throws InvalidArgumentException If any of the provided data source configurations is invalid
     */
    public void updateSyncGroup(long syncGroupId, List<StringPair> syncGroupProperties) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        if (syncGroupProperties == null)
            throw new InvalidArgumentException(String.format("The parameters of the sync group with id %s can not be null", syncGroupId));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);
            
            if (syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Synchronization Group with id %s could not be found", syncGroupId));
            
            for (StringPair syncGroupProperty : syncGroupProperties)
                syncGroupNode.setProperty(syncGroupProperty.getKey(), syncGroupProperty.getValue());
                        
            tx.success();
        }
    }
    
    /**
     * Deletes a sync group
     * @param syncGroupId The id of the sync group
     * @throws ApplicationObjectNotFoundException If the sync group can no be found
     */
    public void deleteSynchronizationGroup(long syncGroupId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);
            
            if (syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find the Synchronization group with id %s",syncGroupId));
            
            for (Relationship relationship : syncGroupNode.getRelationships())
                relationship.delete();
     
            syncGroupNode.delete();
            tx.success();
        }
    }
    /**
     * Creates a data source configuration and associates it to a sync group
     * @param objectId  the id of the object(GenericCommunicationsElement) the data source configuration will belong to
     * @param syncGroupId The id of the sync group the data source configuration will be related to
     * @param name The name of the configuration
     * @param parameters The list of parameters that will be part of the new configuration. A sync data source configuration is a set of parameters that allow the synchronization provider to access a sync data source
     * @return The id of the newly created data source
     * @throws ApplicationObjectNotFoundException If the object has no sync data source configuration group could not be found
     * @throws InvalidArgumentException  If any of the parameters is not valid
     */
    public long createSyncDataSourceConfig(String objectId, long syncGroupId, String name, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException {
        if (name == null || name.trim().isEmpty())
                throw new InvalidArgumentException("The sync configuration name can not be empty");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);
            if(syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be found", syncGroupId));
                       
            Node objectNode = connectionManager.getConnectionHandler().findNode(inventoryObjectsLabel, Constants.PROPERTY_UUID, objectId);
            if(syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The object with id %s could not be found", objectId));
            
            if(objectNode.hasRelationship(Direction.OUTGOING, RelTypes.HAS_CONFIGURATION))
                throw new OperationNotPermittedException(String.format("The object id %s already has a sync datasource configuration", objectId));
            
            Node syncDataSourceConfigNode =  connectionManager.getConnectionHandler().createNode(syncDatasourceConfigLabel);
            syncDataSourceConfigNode.setProperty(Constants.PROPERTY_NAME, name);
            
            for (StringPair parameter : parameters) {
                if (!syncDataSourceConfigNode.hasProperty(parameter.getKey()))
                    syncDataSourceConfigNode.setProperty(parameter.getKey(), parameter.getValue() == null ? "" : parameter.getValue());
                else
                    throw new InvalidArgumentException(String.format("Parameter %s in configuration %s is duplicated", name, parameter.getKey()));
            }
            
            objectNode.createRelationshipTo(syncDataSourceConfigNode, RelTypes.HAS_CONFIGURATION);
            syncDataSourceConfigNode.createRelationshipTo(syncGroupNode, RelTypes.BELONGS_TO_GROUP);
            
            tx.success();
            return syncDataSourceConfigNode.getId();
        }           
    }
    
    /**
     * Updates a synchronization data source
     * @param syncDataSourceConfigId The id of an synchronization data source
     * @param parameters the list of parameters to update
     * @throws ApplicationObjectNotFoundException If the sync data source cannot be found
     */
    public void updateSyncDataSourceConfig(long syncDataSourceConfigId, List<StringPair> parameters) 
        throws ApplicationObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncDataSourceConfig = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, syncDataSourceConfigId);
            if (syncDataSourceConfig == null)
                throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSourceConfigId));
            
            for (StringPair parameter : parameters)
                syncDataSourceConfig.setProperty(parameter.getKey(), parameter.getValue());
                        
            tx.success();
        }
    }

    /**
     * Deletes a synchronization data source
     * @param syncDataSourceConfigId The id of an synchronization data source
     * @throws ApplicationObjectNotFoundException If the sync data source cannot be found
     */
    public void deleteSynchronizationDataSourceConfig(long syncDataSourceConfigId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node syncDataSourceConfigNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, syncDataSourceConfigId);
            if (syncDataSourceConfigNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Can not find the Synchronization Data Source Configuration with id %s",syncDataSourceConfigId));
            
            List<Relationship> relationshipsToDelete = new ArrayList();
           
            for (Relationship relationship : syncDataSourceConfigNode.getRelationships(Direction.INCOMING, RelTypes.HAS_CONFIGURATION)) 
                relationshipsToDelete.add(relationship);
            
            for (Relationship relationship : syncDataSourceConfigNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP)) 
                relationshipsToDelete.add(relationship);
            
            while (!relationshipsToDelete.isEmpty())
                relationshipsToDelete.remove(0).delete();
            
            syncDataSourceConfigNode.delete();
            tx.success();
        }
    }
    
    /**
     * Creates "copy" a relation between a set of sync data source configurations and a given sync group
     * @param syncGroupId The Sync Group Id target
     * @param syncDataSourceConfigurationIds Set of sync data source configuration ids
     * @throws ApplicationObjectNotFoundException If the sync group cannot be found, or some sync data source configuration cannot be found
     * @throws InvalidArgumentException If the sync group cannot be found, or some sync data source configuration cannot be found
     */
    public void copySyncDataSourceConfiguration(long syncGroupId, long[] syncDataSourceConfigurationIds) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node syncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, syncGroupId);
            if (syncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be find", syncGroupId));
                        
            for (long syncDataSrcId : syncDataSourceConfigurationIds) {
                Node syncDataSrcNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, syncDataSrcId);
                if (syncDataSrcNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSrcId));
                
                syncDataSrcNode.createRelationshipTo(syncGroupNode, RelTypes.BELONGS_TO_GROUP);
            }
            tx.success();
        }
    }
    
    /**
     * Release a set of sync data source configuration from a given sync group
     * @param syncGroupId The Sync Group Id target
     * @param syncDataSourceConfigurationIds Set of sync data source configuration ids
     * @throws ApplicationObjectNotFoundException If the sync group cannot be found, or some sync data source configuration cannot be found
     * @throws InvalidArgumentException If the sync group cannot be found, or some sync data source configuration cannot be found
     */
    public void releaseSyncDataSourceConfigFromSyncGroup(long syncGroupId, long[] syncDataSourceConfigurationIds) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            for (long syncDataSrcId : syncDataSourceConfigurationIds) {
                Node syncDataSrcNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, syncDataSrcId);
                if (syncDataSrcNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSrcId));

                List<Relationship> relsToDelete = new ArrayList<>();
                Iterable<Relationship> relationships = syncDataSrcNode.getRelationships(Direction.OUTGOING, RelTypes.BELONGS_TO_GROUP);
                
                int i = 0;
                for (Relationship relationship : relationships) {
                    i++;
                    if(relationship.getEndNodeId() == syncGroupId)
                        relsToDelete.add(relationship);
                }

                if(i == 1)
                    throw new ApplicationObjectNotFoundException(String.format("datasource Config, id: %s can not be release, must belong at least to one SyncGroup", syncDataSrcId));
                for (Relationship rel : relsToDelete) 
                    rel.delete();
            }
            tx.success();
        }
    }
    
    /**
     * Moves a set of sync data source configurations from a sync group to another sync group
     * @param newSyncGroupId The target sync group.
     * @param syncDataSourceConfigurationIds Set of sync data source configuration ids
     * @throws ApplicationObjectNotFoundException If the sync group cannot be found, or some sync data source configuration cannot be found
     * @throws InvalidArgumentException If the sync group is malformed, or some sync data source configuration is malformed
     */
    public void moveSyncDataSourceConfiguration(long newSyncGroupId, long[] syncDataSourceConfigurationIds) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newSyncGroupNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncGroupsLabel, newSyncGroupId);
            if (newSyncGroupNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The sync group with id %s could not be find", newSyncGroupId));
            
            for (long syncDataSrcId : syncDataSourceConfigurationIds) {
                Node syncDataSrcNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), syncDatasourceConfigLabel, syncDataSrcId);
                if (syncDataSrcNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("Synchronization Data Source Configuration with id %s could not be found", syncDataSrcId));
                
                Iterable<Relationship> relationships = syncDataSrcNode.getRelationships(RelTypes.BELONGS_TO_GROUP, Direction.OUTGOING);
                
                for (Relationship relationship : relationships)
                    relationship.delete();

                syncDataSrcNode.createRelationshipTo(newSyncGroupNode, RelTypes.BELONGS_TO_GROUP);
            }
            tx.success();
        }
    }
}
