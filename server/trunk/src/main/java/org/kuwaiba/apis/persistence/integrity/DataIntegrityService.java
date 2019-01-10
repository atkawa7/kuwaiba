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
package org.kuwaiba.apis.persistence.integrity;

import org.kuwaiba.apis.persistence.ConnectionManager;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.services.persistence.util.Constants;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

/**
 * This create primary nodes like dummyRootNode, UserRootNode, GroupRootNode
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class DataIntegrityService{
    
    /**
     * Reference to the db handle
     */
    private final GraphDatabaseService graphDb;
    private final Label specialNodeLabel = Label.label(Constants.LABEL_SPECIAL_NODE);
    
    public DataIntegrityService(ConnectionManager cmn) {
        graphDb = (GraphDatabaseService) cmn.getConnectionHandler();
    }
    
    public void checkIntegrity() throws MetadataObjectNotFoundException{
        Node dummyRootNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
        Node groupRootNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_GROUPS);
        Node generalActivityRootNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG);
        Node objectActivityRootNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_OBJECT_ACTIVITY_LOG);
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
