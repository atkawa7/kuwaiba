/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

import org.kuwaiba.apis.persistence.ConnectionManager;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.services.persistence.util.Constants;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;

/**
 * This create primary nodes like dummyRootNode, UserRootNode, GroupRootNode
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class DataIntegrityService{
    
    /**
     * Reference to the db handle
     */
    private final GraphDatabaseService graphDb;
    private final Index<Node> specialNodes;
    
    public DataIntegrityService(ConnectionManager cmn) {
        graphDb = (GraphDatabaseService) cmn.getConnectionHandler();
        specialNodes = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES);
    }
    
    public void checkIntegrity() throws MetadataObjectNotFoundException{
        Node dummyRootNode = specialNodes.get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
        Node groupRootNode = specialNodes.get(Constants.PROPERTY_NAME, Constants.NODE_GROUPS).getSingle();
        Node generalActivityRootNode = specialNodes.get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle();
        Node objectActivityRootNode = specialNodes.get(Constants.PROPERTY_NAME, Constants.NODE_OBJECT_ACTIVITY_LOG).getSingle();
        //TODO privileges
        if(dummyRootNode == null)
            throw new MetadataObjectNotFoundException(String.format(
                        "Can not find the class %s", Constants.NODE_DUMMYROOT));
        if(groupRootNode == null)
            throw new MetadataObjectNotFoundException(String.format(
                        "Can not find the class %s", Constants.NODE_GROUPS));
        if(generalActivityRootNode == null)
            throw new MetadataObjectNotFoundException(String.format(
                        "Can not find the class %s", Constants.NODE_GENERAL_ACTIVITY_LOG));
        if(objectActivityRootNode == null)
            throw new MetadataObjectNotFoundException(String.format(
                        "Can not find the class %s", Constants.NODE_OBJECT_ACTIVITY_LOG));
    }
    
    //TODO look for the object
    //TODO at least one user
}
