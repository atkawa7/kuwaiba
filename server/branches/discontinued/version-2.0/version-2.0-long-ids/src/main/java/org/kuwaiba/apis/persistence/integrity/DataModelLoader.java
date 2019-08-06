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

import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.kuwaiba.apis.persistence.ConnectionManager;
import org.kuwaiba.apis.persistence.application.Privilege;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.services.persistence.util.Constants;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 * Creates the initial schema needed to load the Kuwaiba data model in a neo4j implementation
 * @author Adrian Fernando Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class DataModelLoader {

    private final MetadataEntityManager mem;
    /**
     * Reference to the db handle
     */
    private final GraphDatabaseService graphDb;
    private final Label specialNodeLabel = Label.label(Constants.LABEL_SPECIAL_NODE);
    
    public DataModelLoader(ConnectionManager cmn, MetadataEntityManager mem) {
        graphDb = (GraphDatabaseService) cmn.getConnectionHandler();
        this.mem = mem;
    }
        
    public void loadDataModel(byte[] dataModelAsByteArray) throws IOException, XMLStreamException, DatabaseException {
            XMLBackupReader reader = new XMLBackupReader(mem);
            reader.read(dataModelAsByteArray);
            createDummyRoot();
            createGroupsRootNode();
            createActivityLogRootNodes();
            reader.load();
    }
    
    public void createDummyRoot() throws DatabaseException
    {
        try (Transaction tx = graphDb.beginTx())
        {
            Node dummyRootNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            if(dummyRootNode == null) {
                Label label = Label.label(Constants.LABEL_ROOT);
                dummyRootNode = graphDb.createNode(label, specialNodeLabel);
                dummyRootNode.setProperty(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
                dummyRootNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, Constants.NODE_DUMMYROOT);
                dummyRootNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
                
                tx.success();
            }
        }catch(Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createDummyroot: {0}", ex.getMessage()); //NOI18N
        }
    }
    
    private void createGroupsRootNode() throws DatabaseException
    {
        try (Transaction tx = graphDb.beginTx())
        {
            Node groupRootNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_GROUPS);

            if(groupRootNode == null){
                Label label = Label.label(Constants.LABEL_ROOT);
                groupRootNode = graphDb.createNode(label, specialNodeLabel);
                groupRootNode.setProperty(Constants.PROPERTY_NAME, Constants.NODE_GROUPS);
                groupRootNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
                
                tx.success();
            }
        }catch(Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createGroupsRootNode: {0}", ex.getMessage()); //NOI18N
        }
    }
    
    private void createActivityLogRootNodes() throws DatabaseException
    {
        try (Transaction tx = graphDb.beginTx())
        {
            Node generalActivityRootNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG);
            Node objectActivityRootNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_OBJECT_ACTIVITY_LOG);
            
            Label label = Label.label(Constants.LABEL_ROOT);
            if (generalActivityRootNode == null){
                generalActivityRootNode = graphDb.createNode(label, specialNodeLabel);
                generalActivityRootNode.setProperty(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG);
                generalActivityRootNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            }
            
            if (objectActivityRootNode == null){
                objectActivityRootNode = graphDb.createNode(label, specialNodeLabel);
                objectActivityRootNode.setProperty(Constants.PROPERTY_NAME, Constants.NODE_OBJECT_ACTIVITY_LOG);
                objectActivityRootNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            }
                        
            tx.success();
        }catch(Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createActivityLogRootNodes: {0}", ex.getMessage()); //NOI18N
        }
    }
    
    private void createPrivilegeRootNode() throws DatabaseException
    {
        try (Transaction tx = graphDb.beginTx())
        {
            Node privilegeRootNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_PRIVILEGES);
            
            if(privilegeRootNode == null){
                Label label = Label.label(Constants.LABEL_ROOT);
                privilegeRootNode = graphDb.createNode(label, specialNodeLabel);
                privilegeRootNode.setProperty(Constants.PROPERTY_NAME, Constants.NODE_PRIVILEGES);
                privilegeRootNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
                tx.success();
            }
        }catch(Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createPriviligeRootNode: {0}", ex.getMessage()); //NOI18N
        }
    }
    
    private void createPrivileges(Privilege privilege){
//        Node privilegeRootNode = null;
//        
//        try (Transaction tx = graphDb.beginTx())
//        {
//            Node privilegeNode = graphDb.createNode();
//            privilegeNode.setProperty(Constants.PROPERTY_CODE, privilege.getCode());
//            privilegeNode.setProperty(Constants.PROPERTY_NAME, privilege.getMethodName());
//            privilegeNode.setProperty(Constants.PROPERTY_METHOD_GROUP, privilege.getMethodGroup());
//            privilegeNode.setProperty(Constants.PROPERTY_METHOD_MANAGER, privilege.getMethodManager());
//            privilegeNode.setProperty(Constants.PROPERTY_DEPENDS_OF, privilege.getDependsOf());
//            privilegeNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
//            
//            privilegeRootNode.createRelationshipTo(privilegeNode, RelTypes.PRIVILEGE);
//            graphDb.index().forNodes(Constants.INDEX_PRIVILEGE_NODES).putIfAbsent(privilegeNode, Constants.PROPERTY_CODE, privilege.getCode());
//            graphDb.index().forNodes(Constants.INDEX_PRIVILEGE_NODES).putIfAbsent(privilegeNode, Constants.PROPERTY_NAME, privilege.getMethodName());
//                    
//            tx.success();
//        }catch(Exception ex) {
//            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createPrivilegeNodes: {0}", ex.getMessage()); //NOI18N
//        }
    }
    
}
