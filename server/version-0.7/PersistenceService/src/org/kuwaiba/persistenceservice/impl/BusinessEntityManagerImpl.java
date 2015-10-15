/*
 *  Copyright 2010 - 2013 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.persistenceservice.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.application.ActivityLogEntry;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.DatabaseException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;
import org.kuwaiba.apis.persistence.interfaces.BusinessEntityManager;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.persistenceservice.caching.CacheManager;
import org.kuwaiba.persistenceservice.util.Constants;
import org.kuwaiba.persistenceservice.util.Util;
import org.kuwaiba.psremoteinterfaces.BusinessEntityManagerRemote;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

/**
 * Business entity manager reference implementation (using Neo4J as backend)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BusinessEntityManagerImpl implements BusinessEntityManager, BusinessEntityManagerRemote {

    /**
     * Reference to the db handler
     */
    private GraphDatabaseService graphDb;
    /**
     * Class index
     */
    private Index<Node> classIndex;
    /**
     * Object index
     */
    private Index<Node> objectIndex;
    /**
     * Special nodes index
     */
    private Index<Node> specialNodesIndex;
    /**
     * Instance of application entity manager
     */
    private ApplicationEntityManagerImpl aem;
    /**
     * Reference to the CacheManager
     */
    private CacheManager cm;

    private BusinessEntityManagerImpl() {
        cm = CacheManager.getInstance();
    }

    public BusinessEntityManagerImpl(ConnectionManager cmn, ApplicationEntityManagerImpl aem) {
        this();
        this.graphDb = (EmbeddedGraphDatabase)cmn.getConnectionHandler();
        this.classIndex = graphDb.index().forNodes(Constants.INDEX_CLASS);
        this.objectIndex = graphDb.index().forNodes(Constants.INDEX_OBJECTS);
        this.specialNodesIndex = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES);
        this.aem = aem;
    }

    @Override
    public long createObject(String className, String parentClassName, long parentOid, HashMap<String,List<String>> attributes, long template, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, DatabaseException, ApplicationObjectNotFoundException, NotAuthorizedException {

        aem.validateCall("createObject", ipAddress, sessionId);
        
        ClassMetadata myClass= cm.getClass(className);
        
        Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

        if (myClass == null){
            myClass = Util.createClassMetadataFromNode(classNode);
            cm.putClass(myClass);
        }
        
        if (myClass.isInDesign())
            throw new OperationNotPermittedException("Create Object", "Can not create instances of classes marked as isDesign");
        
        if (myClass.isAbstract())
            throw new OperationNotPermittedException("Create Object", "Can not create instances of abstract classes");
        
        if (!cm.isSubClass("InventoryObject", className))
            throw new OperationNotPermittedException("Create Object", "Can not create non-inventory objects");

        //The object should be created under an instance other than the dummy root
        if (parentClassName != null){
            ClassMetadata myParentObjectClass= cm.getClass(parentClassName);
            if (myParentObjectClass == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

            if (!cm.getPossibleChildren(parentClassName).contains(className))
                throw new OperationNotPermittedException("Create Object", String.format("An instance of class %s can't be created as child of class %s", className, myParentObjectClass.getName()));
        }

        Node parentNode;
        if (parentOid != -1){
             parentNode = getInstanceOfClass(parentClassName, parentOid);
            if (parentNode == null)
                throw new ObjectNotFoundException(parentClassName, parentOid);
        }else{
            Relationship rel = graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING);
            parentNode = rel.getEndNode();
        }

        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node newObject = createObject(classNode, myClass, attributes, template);
            newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);
            
            //Creates an activity log entry
            Util.createActivityLogEntry(null, specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle(), 
                    aem.getSessions().get(sessionId).getUser().getUserName(), ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                    Calendar.getInstance().getTimeInMillis(), null, null, null, String.valueOf(newObject.getId()));
            
            tx.success();
            tx.finish();
            return newObject.getId();
        }catch(Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createObject: {0}", ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public long createObject(String className, String parentClassName, String criteria, HashMap<String,List<String>> attributes, long template, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, DatabaseException, ApplicationObjectNotFoundException, NotAuthorizedException {
        try {
                aem.validateCall("createObject", ipAddress, sessionId);
                String[] splitCriteria = criteria.split(":");
                if (splitCriteria.length < 2)
                    throw new InvalidArgumentException("The criteria is not valid. It has to have at least two components", Level.INFO);
                
                if (splitCriteria[0].equals("oid"))
                    return createObject(className, parentClassName, Long.parseLong(splitCriteria[1]), attributes, template, ipAddress, sessionId);
                
                if (splitCriteria[0].equals("name")) {
                    Node classNode = classIndex.get(Constants.PROPERTY_NAME, parentClassName).getSingle();
                    if (classNode == null)
                        throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", parentClassName));
                    long parentOid = -1;
                    Iterator<Relationship> children = classNode.getRelationships(RelTypes.INSTANCE_OF).iterator();
                    while (children.hasNext()){
                        Node possibleParentNode = children.next().getStartNode();
                        if (splitCriteria[1].equals(possibleParentNode.getProperty(Constants.PROPERTY_NAME))) {
                            parentOid = possibleParentNode.getId();
                            break;
                        }
                    }
                    if (parentOid != -1)
                        return createObject(className, parentClassName, parentOid, attributes, template, ipAddress, sessionId);
                    
                    throw new InvalidArgumentException(String.format("A parent with name %s of class %s could not be found", 
                            splitCriteria[1], parentClassName), Level.INFO);
                }
                
                throw new InvalidArgumentException("Wrong criteria identifier: " + splitCriteria[1], Level.INFO);
            }catch(Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "createObject: {0}", ex.getMessage()); //NOI18N
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public long createSpecialObject(String className, String parentClassName, long parentOid, HashMap<String,List<String>> attributes, long template, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, DatabaseException, ApplicationObjectNotFoundException, NotAuthorizedException {

        aem.validateCall("createSpecialObject", ipAddress, sessionId);
        
        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

        if (myClass.isInDesign())
            throw new OperationNotPermittedException("Create Object", "Can not create instances of classes marked as isDesign");
        
        if (myClass.isAbstract())
            throw new OperationNotPermittedException("Create Object", "Can not create objects of abstract classes");

        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));


        //The object should be created under an instance other than the dummy root
        if (parentClassName != null){
            ClassMetadata myParentObjectClass= cm.getClass(parentClassName);
            if (myParentObjectClass == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));
        }

        Node parentNode = null;
        if (parentOid != -1){
             parentNode = getInstanceOfClass(parentClassName, parentOid);
            if (parentNode == null)
                throw new ObjectNotFoundException(parentClassName, parentOid);
        }

        Transaction tx = null;
        try{

            tx = graphDb.beginTx();
            Node newObject = createObject(classNode, myClass, attributes, template);
            if (parentNode !=null)
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);

            objectIndex.putIfAbsent(newObject, Constants.PROPERTY_ID, newObject.getId());
            
            //Creates an activity log entry
            Util.createActivityLogEntry(null, specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle(), 
                    aem.getSessions().get(sessionId).getUser().getUserName(), ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                    Calendar.getInstance().getTimeInMillis(), null, null, null, String.valueOf(newObject.getId()));
            
            tx.success();
            tx.finish();
            return newObject.getId();
        }catch(Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.INFO, "createSpecialObject: {0}", ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public long[] createBulkSpecialObjects(String className, int numberOfObjects, String parentClassName, long parentId, String ipAddress, String sessionId) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, ApplicationObjectNotFoundException, NotAuthorizedException {
        
        aem.validateCall("createBulkSpecialObjects", ipAddress, sessionId);
        
        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

        if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, className))
            throw new OperationNotPermittedException("Create Object", String.format("Class %s is not an business class"));
        
        if (myClass.isInDesign())
            throw new OperationNotPermittedException("Create Object", "Can not create instances of classes marked as isDesign");
        
        if (myClass.isAbstract())
            throw new OperationNotPermittedException("Create Object", "Can't create objects from an abstract classes");

        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));


        //The object should be created under an instance other than the dummy root
        if (parentClassName != null){
            ClassMetadata myParentObjectClass= cm.getClass(parentClassName);
            if (myParentObjectClass == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));
        }

        Node parentNode;
        if (parentId != -1){
             parentNode = getInstanceOfClass(parentClassName, parentId);
            if (parentNode == null)
                throw new ObjectNotFoundException(parentClassName, parentId);
        }else{
            Relationship rel = graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING);
            parentNode = rel.getEndNode();
        }

        Transaction tx = null;
        try{

            tx = graphDb.beginTx();
            long res[] = new long[numberOfObjects];
            for (int i = 0; i < numberOfObjects; i++){
                Node newObject = createObject(classNode, myClass, null, 0);
                newObject.setProperty(Constants.PROPERTY_NAME, String.valueOf(i + 1));
                if (parentNode != null)
                    newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
                
                objectIndex.putIfAbsent(newObject, Constants.PROPERTY_ID, newObject.getId());
                //Creates an activity log entry
                Util.createActivityLogEntry(null, specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle(), 
                        aem.getSessions().get(sessionId).getUser().getUserName(), ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                        Calendar.getInstance().getTimeInMillis(), null, null, null, String.valueOf(newObject.getId()));
            }
            
            tx.success();
            tx.finish();
            return res;
        }catch(Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.INFO, "createSpecialObject: {0}", ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public RemoteBusinessObject getObject(String className, long oid, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        
        aem.validateCall("getObject", ipAddress, sessionId);
        
        ClassMetadata myClass = cm.getClass(className);
        Node instance = getInstanceOfClass(className, oid);
        RemoteBusinessObject res = Util.createRemoteObjectFromNode(instance, myClass);
        return res;
    }

    @Override
    public RemoteBusinessObjectLight getObjectLight(String className, long oid, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        
        aem.validateCall("getObjectLight", ipAddress, sessionId);
        //Perform benchmarks to see if accessing to the objects index is less expensive
        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));
        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node instance = instances.iterator().next().getStartNode();

            if (instance.getId() == oid)
                return new RemoteBusinessObjectLight(oid,
                        (String) instance.getProperty(Constants.PROPERTY_NAME),
                        className);

        }
        throw new ObjectNotFoundException(className, oid);
    }
    
    @Override
    public RemoteBusinessObject getParent(String objectClass, long oid, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        
        aem.validateCall("getParent", ipAddress, sessionId);
        
        Node objectNode = getInstanceOfClass(objectClass, oid);
        if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF)){
            Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();
            
            //If the direct parent is DummyRoot, return a dummy RemoteBusinessObject with oid = -1
            if (parentNode.hasRelationship(RelTypes.DUMMY_ROOT))
                return new RemoteBusinessObject(-1L, Constants.NODE_DUMMYROOT, Constants.NODE_DUMMYROOT);
            else    
                return Util.createRemoteObjectFromNode(parentNode, cm.getClass(Util.getClassName(parentNode)));
        }
        if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF_SPECIAL)){
            Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).getEndNode();
            return Util.createRemoteObjectFromNode(parentNode, cm.getClass(Util.getClassName(parentNode)));
        }
        return null;
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getParents (String objectClassName, long oid, String ipAddress, String sessionId)
        throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        
        aem.validateCall("getParents", ipAddress, sessionId);
        
        List<RemoteBusinessObjectLight> parents =  new ArrayList<RemoteBusinessObjectLight>();
      
        String cypherQuery = "START n=node({oid})" +
                             "MATCH n-[:" + RelTypes.CHILD_OF.toString() + "|"+RelTypes.CHILD_OF_SPECIAL.toString() + "*]->m " +
                             "RETURN m as parents";
      
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("oid", oid);
        try {
            ExecutionEngine engine = new ExecutionEngine(graphDb);
            ExecutionResult result = engine.execute(cypherQuery, params);
            Iterator<Node> column = result.columnAs("parents");
            for (Node node : IteratorUtil.asIterable(column)){
                if (node.getProperty(Constants.PROPERTY_NAME).equals(Constants.NODE_DUMMYROOT))
                    parents.add(new RemoteBusinessObjectLight((long)-1, Constants.NODE_DUMMYROOT, Constants.NODE_DUMMYROOT));
                else
                    parents.add(Util.createRemoteObjectLightFromNode(node));
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
        return parents;
    }
    
    @Override
    public RemoteBusinessObject getParentOfClass(String objectClass, long oid, String parentClass, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        
        aem.validateCall("getParentOfClass", ipAddress, sessionId);
        
        Node objectNode = getInstanceOfClass(objectClass, oid);
        
        while (true){
            //This method won't support CHILD_OF_SPECIAL relationships
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF)){
                Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();

                if (parentNode.hasRelationship(RelTypes.DUMMY_ROOT))
                    return null;
                else {
                    String thisNodeClass = Util.getClassName(parentNode);
                    if (cm.isSubClass(thisNodeClass, parentClass))
                        return Util.createRemoteObjectFromNode(parentNode, cm.getClass(thisNodeClass));
                    objectNode = parentNode;
                    continue;
                }
            }
            return null;
        }
    }

    @Override
    public void deleteObjects(HashMap<String, long[]> objects, boolean releaseRelationships, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, ApplicationObjectNotFoundException, NotAuthorizedException {

        aem.validateCall("deleteObjects", ipAddress, sessionId);
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            //TODO: Optimize so it can find all objects of a single class in one query
            for (String className : objects.keySet()){
                for (long oid : objects.get(className)){
                    if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, className))
                        throw new OperationNotPermittedException(className, String.format("Class %s is not a business-related class", className));

                    Node instance = getInstanceOfClass(className, oid);
                    Util.deleteObject(instance, releaseRelationships);
                    //Creates an activity log entry
                    Util.createActivityLogEntry(null, specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle(), 
                            aem.getSessions().get(sessionId).getUser().getUserName(), ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
                            Calendar.getInstance().getTimeInMillis(), null, null, null, String.valueOf(instance.getId()));
                }
            }
            tx.success();
            tx.finish();
        }catch(Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.INFO, "deleteObject: {0}", ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void updateObject(String className, long oid, HashMap<String,List<String>> attributes, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, 
            WrongMappingException, InvalidArgumentException, ApplicationObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {

        aem.validateCall("updateObject", ipAddress, sessionId);
        
        ClassMetadata myClass= cm.getClass(className);
        
        if (myClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
        
            Node instance = getInstanceOfClass(className, oid);

            String oldValue = null, newValue = null;

            for (String attributeName : attributes.keySet()){
                if(myClass.hasAttribute(attributeName)){
                    if (AttributeMetadata.isPrimitive(myClass.getType(attributeName))){
                        oldValue = instance.hasProperty(attributeName) ? String.valueOf(instance.getProperty(attributeName)) : null;
                        if (attributes.get(attributeName) == null)
                            instance.removeProperty(attributeName);
                        else{
                            //If the array is empty, it means the attribute should be set to null
                            if (attributes.get(attributeName).isEmpty())
                                instance.removeProperty(attributeName);
                            else{
                                newValue = attributes.get(attributeName).get(0);
                                if (attributes.get(attributeName).get(0) == null)
                                    instance.removeProperty(attributeName);
                                else
                                    instance.setProperty(attributeName,Util.getRealValue(attributes.get(attributeName).get(0),myClass.getType(attributeName)));
                            }
                        }
                    }else { //If the attribute is not a primitive type, then it's a relationship
                        if (!cm.getClass(myClass.getType(attributeName)).isListType())
                            throw new InvalidArgumentException(String.format("Class %s is not a list type", myClass.getType(attributeName)), Level.WARNING);

                        //Release all previous relationships
                        oldValue = "";
                        for (Relationship rel : instance.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO)){
                            if (rel.getProperty(Constants.PROPERTY_NAME).equals(attributeName)){
                                oldValue += rel.getEndNode().getProperty(Constants.PROPERTY_NAME);
                                rel.delete();
                            }
                        }
                        if (attributes.get(attributeName) != null){ //If the new value is different than null, then create the new relationships

                            Node listTypeNode = classIndex.get(Constants.PROPERTY_NAME, myClass.getType(attributeName)).getSingle();
                            List<Node> listTypeNodes = Util.getRealValue(attributes.get(attributeName), listTypeNode);

                            newValue = "";
                            //Create the new relationships
                            for (Node item : listTypeNodes){
                                newValue += " " + item.getProperty(Constants.PROPERTY_NAME);
                                Relationship newRelationship = instance.createRelationshipTo(item, RelTypes.RELATED_TO);
                                newRelationship.setProperty(Constants.PROPERTY_NAME, attributeName);
                            }
                        }
                    }
                } else
                    throw new InvalidArgumentException(
                            String.format("The attribute %s does not exist in class %s", attributeName, className), Level.WARNING);

                //Creates an activity log entry
                Util.createActivityLogEntry(instance, specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_OBJECT_ACTIVITY_LOG).getSingle(), 
                        aem.getSessions().get(sessionId).getUser().getUserName(), ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, 
                        Calendar.getInstance().getTimeInMillis(), attributeName, oldValue, newValue, null);
            }
            tx.success();
            tx.finish();
        }catch(Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.INFO, "updateObject: {0}", ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public boolean setBinaryAttributes(String className, long oid, List<String> attributeNames, List<byte[]> attributeValues, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, ArraySizeMismatchException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("setBinaryAttributes", ipAddress, sessionId);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String name, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("createSpecialRelationship", ipAddress, sessionId);
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node nodeA = getInstanceOfClass(aObjectClass, aObjectId);
            for (Relationship rel : nodeA.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                if (rel.getOtherNode(nodeA).getId() == bObjectId 
                        && rel.getProperty(Constants.PROPERTY_NAME).equals(name))
                    throw new OperationNotPermittedException("Relate Objects", "These elements are already related");
            }
            Node nodeB = getInstanceOfClass(bObjectClass, bObjectId);
            Relationship rel = nodeA.createRelationshipTo(nodeB, RelTypes.RELATED_TO_SPECIAL);
            rel.setProperty(Constants.PROPERTY_NAME, name);
            tx.success();
            tx.finish();
        }catch(Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.INFO, "createSpecialRelationship: {0}", ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseSpecialRelationship(String objectClass, long objectId, long otherObjectId, String name, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("releaseSpecialRelationship", ipAddress, sessionId);
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node node = getInstanceOfClass(objectClass, objectId);
            for (Relationship rel : node.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                if ((rel.getProperty(Constants.PROPERTY_NAME).equals(name) && 
                        (rel.getOtherNode(node).getId() == otherObjectId) || otherObjectId == -1))
                    rel.delete();
            }
            tx.success();
            tx.finish();
        }catch(Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.INFO, "releaseSpecialRelationship: {0}", ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public void releaseSpecialRelationship(String objectClass, long objectId, String relationshipName, long targetId, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("releaseSpecialRelationship", ipAddress, sessionId);
        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node node = getInstanceOfClass(objectClass, objectId);
            for (Relationship rel : node.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO_SPECIAL)){
                if (rel.getProperty(Constants.PROPERTY_NAME).equals(relationshipName) &&
                            rel.getEndNode().getId() == targetId)
                    rel.delete();
            }
            tx.success();
            tx.finish();
        }catch(Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.INFO, "releaseSpecialRelationship: {0}", ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void moveObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("moveObjects", ipAddress, sessionId);
        ClassMetadata newParentClass = cm.getClass(targetClassName);
        
        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", targetClassName));

        Node newParentNode = getInstanceOfClass(targetClassName, targetOid);

        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            for (String myClass : objects.keySet()){
                if (!cm.canBeChild(targetClassName, myClass))
                    throw new OperationNotPermittedException("moveObjects", String.format("An instance of class %s can not be child of an instance of class %s", myClass,targetClassName));

                Node instanceClassNode = classIndex.get(Constants.PROPERTY_NAME, myClass).getSingle();
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", myClass));
                for (long oid : objects.get(myClass)){
                    Node instance = getInstanceOfClass(instanceClassNode, oid);
                    String oldValue = null;
                    if (instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().hasNext()){
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().next();
                        oldValue = String.valueOf(rel.getEndNode().getId());
                        rel.delete();
                    }
                    //If the object was child of a pool
                    if (instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().hasNext()){
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().next();
                        oldValue = String.valueOf(rel.getEndNode().getId());
                        rel.delete();
                    }
                    
                    instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
                    
                //Creates an activity log entry
                Util.createActivityLogEntry(instance, specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_OBJECT_ACTIVITY_LOG).getSingle(), 
                        aem.getSessions().get(sessionId).getUser().getUserName(), ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
                        Calendar.getInstance().getTimeInMillis(), "parent", oldValue, String.valueOf(newParentNode.getId()), null); //NOI18N
            
                }
            }
            tx.success();
            tx.finish();
        }catch(Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.INFO, "moveObjects: {0}", ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public long[] copyObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects, boolean recursive, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("copyObjects", ipAddress, sessionId);
        ClassMetadata newParentClass = cm.getClass(targetClassName);

        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", targetClassName));

        Node newParentNode = getInstanceOfClass(targetClassName, targetOid);

        Transaction tx = null;

        try{
            tx = graphDb.beginTx();
            long[] res = new long[objects.size()];
            int i = 0;
            for (String myClass : objects.keySet()){
                if (!cm.canBeChild(targetClassName, myClass))
                    throw new OperationNotPermittedException("copyObjects", String.format("An instance of class %s can not be child of an instance of class %s", myClass,targetClassName));

                Node instanceClassNode = classIndex.get(Constants.PROPERTY_NAME, myClass).getSingle();
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", myClass));
                for (long oid : objects.get(myClass)){
                    Node templateObject = getInstanceOfClass(instanceClassNode, oid);
                    Node newInstance = copyObject(templateObject, recursive);
                    newInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
                    res[i] = newInstance.getId();
                    i++;
                    
                    //Creates an activity log entry
                    Util.createActivityLogEntry(null, specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle(), 
                            aem.getSessions().get(sessionId).getUser().getUserName(), ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
                            Calendar.getInstance().getTimeInMillis(), null, null, null, String.valueOf(newInstance.getId()));
            
                }
            }
            tx.success();
            tx.finish();
            return res;
        }catch(Exception ex){
            Logger.getLogger(getClass().getName()).log(Level.INFO, "copyObjects: {0}", ex.getMessage()); //NOI18N
            tx.failure();
            tx.finish();
            throw new RuntimeException(ex.getMessage());
        }        
    }

    @Override
    public boolean setObjectLockState(String className, long oid, Boolean value, String ipAddress, String sessionId)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("setObjectLockState", ipAddress, sessionId);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<RemoteBusinessObjectLight> getObjectChildren(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getObjectChildren", ipAddress, sessionId);
        try{
            Node parentNode;
            if(oid == -1){
                Relationship rel = graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING);
                parentNode = rel.getEndNode();
            }else
                parentNode = getInstanceOfClass(className, oid);
            
            Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            List<RemoteBusinessObjectLight> res = new ArrayList<RemoteBusinessObjectLight>();

            if (maxResults > 0){
                int counter = 0;
                while(children.iterator().hasNext() && (counter < maxResults)){
                    counter++;
                    Node child = children.iterator().next().getStartNode();
                    res.add(new RemoteBusinessObjectLight(child.getId(),(String)child.getProperty(Constants.PROPERTY_NAME), Util.getClassName(child)));
                }
            }else{
                while(children.iterator().hasNext()){
                    Node child = children.iterator().next().getStartNode();
                    res.add(new RemoteBusinessObjectLight(child.getId(),(String)child.getProperty(Constants.PROPERTY_NAME), Util.getClassName(child)));
                }
            }
            return res;
        }catch (Exception ex){
            throw new RuntimeException (ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getObjectChildren(long oid, long classId, int maxResults, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getObjectChildren", ipAddress, sessionId);
        try{
            Node parentNode;
            if(oid == -1){
                Relationship rel = graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING);
                parentNode = rel.getEndNode();
            }else
                parentNode = getInstanceOfClass(classId, oid);
            
            Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            List<RemoteBusinessObjectLight> res = new ArrayList<RemoteBusinessObjectLight>();
            if (maxResults > 0){
                int counter = 0;
                while(children.iterator().hasNext() && (counter < maxResults)){
                    counter++;
                    Node child = children.iterator().next().getStartNode();
                    res.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(Constants.PROPERTY_NAME), Util.getClassName(child)));
                }
            }else{
                while(children.iterator().hasNext()){
                    Node child = children.iterator().next().getStartNode();
                    res.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(Constants.PROPERTY_NAME), Util.getClassName(child)));
                }
            }
            return res;
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getSiblings(String className, long oid, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException{
        aem.validateCall("getSiblings", ipAddress, sessionId);
        try{
            Node node = getInstanceOfClass(className, oid);
            List<RemoteBusinessObjectLight> res = new ArrayList<RemoteBusinessObjectLight>();
            
            if (!node.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF))
                return res;
            
            Node parentNode = node.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();
            
            int resultCounter = 0;
            for (Relationship rel : parentNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF)){
                if (maxResults > 0){
                    if (resultCounter < maxResults)
                        resultCounter ++;
                    else
                        break;
                }
                
                Node child = rel.getStartNode();
                if (child.getId() == oid)
                    continue;
                
                res.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(Constants.PROPERTY_NAME), Util.getClassName(child)));
            }
                       
            return res;
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getObjectsOfClassLight(String className, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
       aem.validateCall("getObjectsOfClassLight", ipAddress, sessionId); 
        
        Node classMetadataNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
        if (classMetadataNode == null)
            throw new MetadataObjectNotFoundException(className);
        
        List<RemoteBusinessObjectLight> instances = new ArrayList<RemoteBusinessObjectLight>();
        
        TraversalDescription traversal = Traversal.description().breadthFirst().relationships(RelTypes.EXTENDS, Direction.INCOMING);
        int counter = 0;
        for(Path p : traversal.traverse(classMetadataNode)){
            for (Relationship rel : p.endNode().getRelationships(RelTypes.INSTANCE_OF)){
                if (maxResults > 0){
                    if (counter < maxResults)
                        counter ++;
                    else break;
                }
                instances.add(Util.createRemoteObjectLightFromNode(rel.getStartNode()));
            }
        }
        
        return instances;
    }

    @Override
    public List<RemoteBusinessObject> getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getChildrenOfClass", ipAddress, sessionId); 
        Node parentNode = getInstanceOfClass(parentClass, parentOid);
        Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
        List<RemoteBusinessObject> res = new ArrayList<RemoteBusinessObject>();

        int counter = 0;

        while(children.iterator().hasNext()){
            Node child = children.iterator().next().getStartNode();

            if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found",child.getId()));

            Node classNode = child.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode();

            ClassMetadata classMetadata = cm.getClass((String)classNode.getProperty(Constants.PROPERTY_NAME));
            if (cm.isSubClass(classToFilter, classMetadata.getName())){
                res.add(Util.createRemoteObjectFromNode(child, classMetadata));
                if (maxResults > 0){
                    if (++counter == maxResults)
                        break;
                }
            }
        }

        if (maxResults > 0 && counter == maxResults)
            return res;

        Iterable<Relationship> specialChildren = parentNode.getRelationships(RelTypes.CHILD_OF_SPECIAL,Direction.INCOMING);
        while(specialChildren.iterator().hasNext()){
            Node child = specialChildren.iterator().next().getStartNode();

            if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found",child.getId()));

            Node classNode = child.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode();

            ClassMetadata classMetadata = cm.getClass((String)classNode.getProperty(Constants.PROPERTY_NAME));
            if (cm.isSubClass(classToFilter, classMetadata.getName())){
                res.add(Util.createRemoteObjectFromNode(child, classMetadata));
                if (maxResults > 0 && ++counter == maxResults)
                        break;
            }
        }
        return res;
    }

    @Override
    public List<RemoteBusinessObjectLight> getChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException  {
        aem.validateCall("getChildrenOfClassLight", ipAddress, sessionId); 
        Node parentNode = getInstanceOfClass(parentClass, parentOid);
        Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING);
        List<RemoteBusinessObjectLight> res = new ArrayList<RemoteBusinessObjectLight>();

        int counter = 0;

        while(children.iterator().hasNext()){
            Node child = children.iterator().next().getStartNode();

            if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found",child.getId()));

            String className = Util.getClassName(child);
            if (cm.isSubClass(classToFilter, className)){
                res.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(Constants.PROPERTY_NAME),className));
                if (maxResults > 0){
                    if (++counter == maxResults)
                        break;
                }
            }
        }

        if (maxResults > 0 && counter == maxResults)
            return res;

        Iterable<Relationship> specialChildren = parentNode.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING);
        while(specialChildren.iterator().hasNext()){
            Node child = specialChildren.iterator().next().getStartNode();

            if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found",child.getId()));

            String className = Util.getClassName(child);

            if (cm.isSubClass(classToFilter, className)){
                res.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(Constants.PROPERTY_NAME),className));
                if (maxResults > 0){
                    if (++counter == maxResults)
                        break;
                }
            }
        }

        return res;
    }

    @Override
    public List<RemoteBusinessObjectLight> getSpecialAttribute(String objectClass, long objectId, String specialAttributeName, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException {
        aem.validateCall("getSpecialAttribute", ipAddress, sessionId); 
        Node instance = getInstanceOfClass(objectClass, objectId);
        List<RemoteBusinessObjectLight> res = new ArrayList<RemoteBusinessObjectLight>();
        for (Relationship rel : instance.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
            if(rel.hasProperty(Constants.PROPERTY_NAME)){
                if (rel.getProperty(Constants.PROPERTY_NAME).equals(specialAttributeName))
                    res.add(rel.getEndNode().getId() == objectId ? 
                        Util.createRemoteObjectLightFromNode(rel.getStartNode()) : Util.createRemoteObjectLightFromNode(rel.getEndNode()));
            }
        }
        return res;
    }
    
    @Override
    public HashMap<String,List<RemoteBusinessObjectLight>> getSpecialAttributes (String className, long objectId, String ipAddress, String sessionId) 
        throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException  {
        aem.validateCall("getSpecialAttributes", ipAddress, sessionId); 
        Node objectNode = getInstanceOfClass(className, objectId);
        HashMap<String,List<RemoteBusinessObjectLight>> res = new HashMap<String, List<RemoteBusinessObjectLight>>();
        for (Relationship rel : objectNode.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
            String relName = (String)rel.getProperty(Constants.PROPERTY_NAME);
            List<RemoteBusinessObjectLight> currentObjects = res.get(relName);
            if (currentObjects == null){
                currentObjects = new ArrayList<RemoteBusinessObjectLight>();
                res.put(relName, currentObjects);
            }
            currentObjects.add(Util.createRemoteObjectLightFromNode(rel.getOtherNode(objectNode)));
        }
        return res;
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getObjectSpecialChildren(String objectClass, long objectId, String ipAddress, String sessionId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException  {
        aem.validateCall("getObjectSpecialChildren", ipAddress, sessionId); 
        Node instance = getInstanceOfClass(objectClass, objectId);
        List<RemoteBusinessObjectLight> res = new ArrayList<RemoteBusinessObjectLight>();
        for (Relationship rel : instance.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)){
            if(rel.hasProperty(Constants.PROPERTY_NAME)){
                if(rel.getProperty(Constants.PROPERTY_NAME).equals(Constants.REL_PROPERTY_POOL))
                    return res;
            }
            res.add(Util.createRemoteObjectLightFromNode(rel.getStartNode()));
        }
        return res;
    }

    @Override
    public boolean hasRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships, String ipAddress, String sessionId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException  {
        aem.validateCall("hasRelationship", ipAddress, sessionId); 
        Node object = getInstanceOfClass(objectClass, objectId);
        int relationshipsCounter = 0;
        for (Relationship rel : object.getRelationships(RelTypes.RELATED_TO)){
            if (rel.getProperty(Constants.PROPERTY_NAME).equals(relationshipName))
                relationshipsCounter++;
            if (relationshipsCounter == numberOfRelationships)
                return true;
        }
        return false;
    }
    
    @Override
    public boolean hasSpecialRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships, String ipAddress, String sessionId) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, NotAuthorizedException  {
        aem.validateCall("hasSpecialRelationship", ipAddress, sessionId); 
        Node object = getInstanceOfClass(objectClass, objectId);
        int relationshipsCounter = 0;
        for (Relationship rel : object.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
            if (rel.getProperty(Constants.PROPERTY_NAME).equals(relationshipName))
                relationshipsCounter++;
            if (relationshipsCounter == numberOfRelationships)
                return true;
        }
        return false;
    }
    
    //TODO DELETE
    @Override
    public List<RemoteBusinessObjectLight> getPhysicalPath(String objectClass, long objectId, String ipAddress, String sessionId) throws ApplicationObjectNotFoundException, NotAuthorizedException{
        aem.validateCall("getPhysicalPath", ipAddress, sessionId); 
        Node lastNode = null;
        List<RemoteBusinessObjectLight> path = new ArrayList<RemoteBusinessObjectLight>();
        String cypherQuery = "START o=node({oid}) "+ 
                             "MATCH path = o-[r:"+RelTypes.RELATED_TO_SPECIAL.toString()+"*]-c "+
                             "WHERE all(rel in r where rel.name = 'mirror' or rel.name = 'endpointA' or rel.name = 'endpointB') "+
                             "RETURN collect(distinct c) as path";
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("oid", objectId);
        try {
            ExecutionEngine engine = new ExecutionEngine(graphDb);
            ExecutionResult result = engine.execute(cypherQuery, params);
            Iterator<List<Node>> column = result.columnAs("path");
            
            for (List<Node> list : IteratorUtil.asIterable(column)){
                if (list.isEmpty())
                    return path;
                lastNode = list.get(list.size()-1);
            }
            params.clear();
            params.put("oid", lastNode.getId());

            engine = new ExecutionEngine(graphDb);
            result = engine.execute(cypherQuery, params);
            column = result.columnAs("path");
            path.add(Util.createRemoteObjectLightFromNode(lastNode));
            for (List<Node> listOfNodes : IteratorUtil.asIterable(column)){
                for(Node node : listOfNodes)
                    path.add(Util.createRemoteObjectLightFromNode(node));
            }
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
        return path;
    }
    
    /**
     * Helpers
     */
    /**
     * Boiler-plate code. Gets a particular instance given the class name and the oid
     * @param className object class name
     * @param oid object id
     * @return a Node representing the entity
     * @throws MetadataObjectNotFoundException id the class cannot be found
     */
    private Node getInstanceOfClass(String className, long oid) throws MetadataObjectNotFoundException, ObjectNotFoundException{
        //if any of the parameters is null, return the dummy root
        if (className == null)
            return graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.BOTH).getEndNode();

        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();

        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));
        
        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node otherSide = instances.iterator().next().getStartNode();
            if (otherSide.getId() == oid)
                return otherSide;
        }
        throw new ObjectNotFoundException(className, oid);
    }
    /**
     * Boiler-plate code. Gets a particular instance given the class name and the oid
     * @param className object class name
     * @param oid object id
     * @return a Node representing the entity
     * @throws MetadataObjectNotFoundException id the class cannot be found
     */
    private Node getInstanceOfClass(long classId, long oid) throws MetadataObjectNotFoundException, ObjectNotFoundException{
        //if any of the parameters is null, return the dummy root
        if (classId == -1)
            return graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.BOTH).getEndNode();

        Node classNode = classIndex.get(Constants.PROPERTY_ID,classId).getSingle();

        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class with id %s can not be found", classId));

        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node otherSide = instances.iterator().next().getStartNode();
            if (otherSide.getId() == oid)
                return otherSide;
        }
        throw new ObjectNotFoundException((String)classNode.getProperty(Constants.PROPERTY_NAME), oid);
    }

    private Node getInstanceOfClass(Node classNode, long oid) throws ObjectNotFoundException{
        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node otherSide = instances.iterator().next().getStartNode();
            if (otherSide.getId() == oid)
                return otherSide;
        }
        throw new ObjectNotFoundException((String)classNode.getProperty(Constants.PROPERTY_NAME), oid);
    }
    
    protected Node createObject(Node classNode, ClassMetadata classToMap, HashMap<String,List<String>> attributes, long template) 
            throws InvalidArgumentException, MetadataObjectNotFoundException {
 
        if (classToMap.isAbstract())
                throw new InvalidArgumentException(String.format("Can not create objects from abstract classes (%s)", classToMap.getName()), Level.OFF);
        
        Node newObject = graphDb.createNode();
        newObject.setProperty(Constants.PROPERTY_NAME, ""); //The default value is an empty string 

        if (attributes != null){
            for (AttributeMetadata att : classToMap.getAttributes()){
                if (att.getName().equals(Constants.PROPERTY_CREATION_DATE)){
                    newObject.setProperty(att.getName(), Calendar.getInstance().getTimeInMillis());
                    continue;
                }

                if (attributes.get(att.getName()) == null)
                    continue;

                //If the array is empty, it means the attribute should be set to null, that is, ignore it
                if (!attributes.get(att.getName()).isEmpty()){
                    if (attributes.get(att.getName()).get(0) != null){
                        if (AttributeMetadata.isPrimitive(classToMap.getType(att.getName())))
                                newObject.setProperty(att.getName(), Util.getRealValue(attributes.get(att.getName()).get(0), classToMap.getType(att.getName())));
                        else{
                        //If it's not a primitive type, maybe it's a relationship

                            if (!cm.isSubClass(Constants.CLASS_GENERICOBJECTLIST, att.getType()))
                                throw new InvalidArgumentException(String.format("Type %s is not a primitive nor a list type", att.getName()), Level.WARNING);
                                                           
                            Node listTypeNode = classIndex.get(Constants.PROPERTY_NAME, att.getType()).getSingle();
                            
                            if (listTypeNode == null)
                                throw new InvalidArgumentException(String.format("Class %s could not be found as list type", att.getType()), Level.INFO);
                            
                            List<Node> listTypeNodes = Util.getRealValue(attributes.get(att.getName()), listTypeNode);
                            
                            //if (listTypeNodes.isEmpty())
                            //    throw new InvalidArgumentException(String.format("At least one of list type items could not be found. Check attribute definition for %s", att.getName()), Level.INFO);
                      
                            //Create the new relationships
                            for (Node item : listTypeNodes){
                                Relationship newRelationship = newObject.createRelationshipTo(item, RelTypes.RELATED_TO);
                                newRelationship.setProperty(Constants.PROPERTY_NAME, att.getName());
                            }
                        }
                    }
                }
            }
        }            

        objectIndex.putIfAbsent(newObject, Constants.PROPERTY_ID, newObject.getId());
        newObject.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);
        
        return newObject;       
    }
    /**
     * Copies and object and optionally its children objects. This method does not manage transactions
     * @param templateObject The object to be cloned
     * @param recursive should the children be copied recursively?
     * @return The cloned node
     */
    private Node copyObject(Node templateObject, boolean recursive) {
        Node newInstance = graphDb.createNode();
        for (String property : templateObject.getPropertyKeys())
            newInstance.setProperty(property, templateObject.getProperty(property));
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING))
            newInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, rel.getProperty(Constants.PROPERTY_NAME));

        newInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode(), RelTypes.INSTANCE_OF);

        if (recursive){
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING)){
                Node newChild = copyObject(rel.getStartNode(), true);
                newChild.createRelationshipTo(newInstance, RelTypes.CHILD_OF);
            }
        }
        return newInstance;
    }

    @Override
    public int[] executePatch() throws NotAuthorizedException {
        int executedFiles = 0;
        BufferedReader br = null;
        File patchDirectory = new File(Constants.PACTHES_PATH);
        int totalPatchFiles = patchDirectory.listFiles().length;

        for (File patchFile : patchDirectory.listFiles()) {
            if (!patchFile.getName().contains("~") && !patchFile.getName().endsWith(".ole")) {
                try {
                    br = new BufferedReader(new FileReader(patchFile));
                    String line = br.readLine();
                    while (line != null) {
                        if (line.startsWith("#")) {
                            System.out.println(line);
                        }
                        if (line.startsWith(Constants.DATABASE_SENTENCE)) {
                            String cypherQuery = br.readLine();
                            ExecutionEngine engine = new ExecutionEngine(graphDb);
                            engine.execute(cypherQuery);
                        }
                        line = br.readLine();
                    }
                    File readFile = new File(patchFile.getPath()+".ole");
                    patchFile.renameTo(readFile);
                    executedFiles++;
                } catch (IOException e) {
                    Logger.getLogger(getClass().getName()).log(Level.INFO, "executePatch: {0}", e.getMessage()); //NOI18N
                } catch (Exception ex) {
                    throw new RuntimeException(ex.getMessage());
                } finally {
                    try {
                        if (br != null) {
                            br.close();
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(getClass().getName()).log(Level.INFO, "executePatch: {0}", ex.getMessage()); //NOI18N
                    }
                }
            }
        }//end for
        return new int[]{executedFiles, totalPatchFiles};
    }
}