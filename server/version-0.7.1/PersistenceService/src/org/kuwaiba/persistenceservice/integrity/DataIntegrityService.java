/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.persistenceservice.integrity;

import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.application.Privilege;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.persistenceservice.impl.RelTypes;
import org.kuwaiba.persistenceservice.util.Constants;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 * 
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class DataIntegrityService{

    /**
     * Reference to the db handle
     */
    private EmbeddedGraphDatabase graphDb;
    
    public DataIntegrityService(ConnectionManager cmn) {
        graphDb = (EmbeddedGraphDatabase) cmn.getConnectionHandler();
    }
    
    public void createDummyroot() throws DatabaseException{
        Node referenceNode = graphDb.getReferenceNode();
        Relationship rel = referenceNode.getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING);
        if (rel == null){
            Transaction tx = null;
            try{
                tx = graphDb.beginTx();
                Node dummyRootNode = graphDb.createNode();
                dummyRootNode.setProperty(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
                dummyRootNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, Constants.NODE_DUMMYROOT);
                dummyRootNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
                
                graphDb.getReferenceNode().createRelationshipTo(dummyRootNode, RelTypes.DUMMY_ROOT);
                graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).putIfAbsent(dummyRootNode, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
                tx.success();
                tx.finish();
            }catch(Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createDummyroot: {0}", ex.getMessage()); //NOI18N
                if (tx != null){
                    tx.failure();
                    tx.finish();
                }
            }
        }
    }
    
    public void createGroupsRootNode(){
        Node referenceNode = graphDb.getReferenceNode();
        
        //Don't create it if it already exists
        for (Relationship rel : referenceNode.getRelationships(RelTypes.ROOT)){
            if (Constants.NODE_GROUPS.equals(rel.getEndNode().getProperty(Constants.PROPERTY_NAME)))
                return;
        }
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node groupRootNode = graphDb.createNode();
            groupRootNode.setProperty(Constants.PROPERTY_NAME, Constants.NODE_GROUPS);
            groupRootNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());

            referenceNode.createRelationshipTo(groupRootNode, RelTypes.ROOT);
            graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).putIfAbsent(groupRootNode, Constants.PROPERTY_NAME, Constants.NODE_GROUPS);
            tx.success();
            tx.finish();
        }catch(Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createGroupsRootNode: {0}", ex.getMessage()); //NOI18N
            if (tx != null){
                tx.failure();
                tx.finish();
            }
        }
        
    }
    
    public void createActivityLogRootNodes() {
        boolean generalActivityRootNodeExists = false, objectActivityRootNodeExists = false;
        
        //Don't create it if it already exists
        for (Relationship rel : graphDb.getReferenceNode().getRelationships(RelTypes.ROOT)){
            if (Constants.NODE_GENERAL_ACTIVITY_LOG.equals(rel.getEndNode().getProperty(Constants.PROPERTY_NAME)))
                generalActivityRootNodeExists = true;
            else
                if (Constants.NODE_OBJECT_ACTIVITY_LOG.equals(rel.getEndNode().getProperty(Constants.PROPERTY_NAME)))
                objectActivityRootNodeExists = true;
        }
        
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            
            if (!generalActivityRootNodeExists){
                Node generalActivityRootNode = graphDb.createNode();
                generalActivityRootNode.setProperty(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG);
                generalActivityRootNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
                graphDb.getReferenceNode().createRelationshipTo(generalActivityRootNode, RelTypes.ROOT);
                graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).putIfAbsent(generalActivityRootNode, Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG);
            }
            
            if (!objectActivityRootNodeExists){
                Node objectActivityRootNode = graphDb.createNode();
                objectActivityRootNode.setProperty(Constants.PROPERTY_NAME, Constants.NODE_OBJECT_ACTIVITY_LOG);
                objectActivityRootNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
                graphDb.getReferenceNode().createRelationshipTo(objectActivityRootNode, RelTypes.ROOT);
                graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).putIfAbsent(objectActivityRootNode, Constants.PROPERTY_NAME, Constants.NODE_OBJECT_ACTIVITY_LOG);
            }
                        
            tx.success();
            tx.finish();
        }catch(Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createActivityLogRootNodes: {0}", ex.getMessage()); //NOI18N
            if (tx != null){
                tx.failure();
                tx.finish();
            }
        }
        
    }
    
    public void createPrivilegeRootNode(){
        for (Relationship rel : graphDb.getReferenceNode().getRelationships(RelTypes.ROOT)){
            if (Constants.NODE_PRIVILEGES.equals(rel.getEndNode().getProperty(Constants.PROPERTY_NAME)))
                return;
        }
        Transaction tx =null;
        try{
            tx = graphDb.beginTx();
            
            Node privilegeRootNode = graphDb.createNode();
            privilegeRootNode.setProperty(Constants.PROPERTY_NAME, Constants.NODE_PRIVILEGES);
            privilegeRootNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            graphDb.getReferenceNode().createRelationshipTo(privilegeRootNode, RelTypes.ROOT);
            graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES).putIfAbsent(privilegeRootNode, Constants.PROPERTY_NAME, Constants.NODE_PRIVILEGES);
            tx.success();
            tx.finish();
        }catch(Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createPriviligeRootNode: {0}", ex.getMessage()); //NOI18N
            if (tx != null){
                tx.failure();
                tx.finish();
            }
        }
    }
    
    public void createPrivileges(Privilege privilege){
        Node privilegeRootNode = null;
        for (Relationship rel : graphDb.getReferenceNode().getRelationships(RelTypes.ROOT)){
            if (Constants.NODE_PRIVILEGES.equals(rel.getEndNode().getProperty(Constants.PROPERTY_NAME))){
                privilegeRootNode = rel.getEndNode();
                break;
            }
                
        }
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node privilegeNode = graphDb.createNode();
            privilegeNode.setProperty(Constants.PROPERTY_CODE, privilege.getCode());
            privilegeNode.setProperty(Constants.PROPERTY_NAME, privilege.getMethodName());
            privilegeNode.setProperty(Constants.PROPERTY_METHOD_GROUP, privilege.getMethodGroup());
            privilegeNode.setProperty(Constants.PROPERTY_METHOD_MANAGER, privilege.getMethodManager());
            privilegeNode.setProperty(Constants.PROPERTY_DEPENDS_OF, privilege.getDependsOf());
            privilegeNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            
            privilegeRootNode.createRelationshipTo(privilegeNode, RelTypes.PRIVILEGE);
            graphDb.index().forNodes(Constants.INDEX_PRIVILEGE_NODES).putIfAbsent(privilegeNode, Constants.PROPERTY_CODE, privilege.getCode());
            graphDb.index().forNodes(Constants.INDEX_PRIVILEGE_NODES).putIfAbsent(privilegeNode, Constants.PROPERTY_NAME, privilege.getMethodName());
                    
            tx.success();
            tx.finish();
        }catch(Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createPrivilegeNodes: {0}", ex.getMessage()); //NOI18N
            if (tx != null){
                tx.failure();
                tx.finish();
            }
        }
    }
        
    public void checkIntegrity() {
        //check every attributes inheritance.
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
