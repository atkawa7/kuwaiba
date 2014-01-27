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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;

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
     * Speial nodes index
     */
    private Index<Node> specialNodesIndex;
    /**
     * Reference to the CacheManager
     */
    private CacheManager cm;

    private BusinessEntityManagerImpl() {
        cm= CacheManager.getInstance();
    }

    public BusinessEntityManagerImpl(ConnectionManager cmn) {
        this();
        this.graphDb = (EmbeddedGraphDatabase)cmn.getConnectionHandler();
        this.classIndex = graphDb.index().forNodes(Constants.INDEX_CLASS);
        this.objectIndex = graphDb.index().forNodes(Constants.INDEX_OBJECTS);
        this.specialNodesIndex = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES);
    }

    public long createObject(String className, String parentClassName, long parentOid, HashMap<String,List<String>> attributes, long template)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, DatabaseException {

        ClassMetadata myClass= cm.getClass(className);
        
        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();
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
                    "admin", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
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
    
    

    public long createSpecialObject(String className, String parentClassName, long parentOid, HashMap<String,List<String>> attributes, long template)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, DatabaseException {

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
            if (parentNode !=null)
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);

            objectIndex.putIfAbsent(newObject, Constants.PROPERTY_ID, newObject.getId());
            
            //Creates an activity log entry
            Util.createActivityLogEntry(null, specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle(), 
                    "admin", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
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

    public long[] createBulkSpecialObjects(String className, int numberOfObjects, String parentClassName, long parentId) throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException {
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
                newObject.setProperty(Constants.PROPERTY_NAME, i + 1);
                if (parentNode != null)
                    newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
                
                objectIndex.putIfAbsent(newObject, Constants.PROPERTY_ID, newObject.getId());
                //Creates an activity log entry
                Util.createActivityLogEntry(null, specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle(), 
                        "admin", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
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

    public RemoteBusinessObject getObject(String className, long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        ClassMetadata myClass = cm.getClass(className);
        Node instance = getInstanceOfClass(className, oid);
        RemoteBusinessObject res = Util.createRemoteObjectFromNode(instance, myClass);
        return res;
    }

    public RemoteBusinessObjectLight getObjectLight(String className, long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
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
    
    public RemoteBusinessObject getParent(String objectClass, long oid) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
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

    public RemoteBusinessObject getParentOfClass(String objectClass, long oid, String parentClass) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
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

    public void deleteObjects(HashMap<String, long[]> objects, boolean releaseRelationships)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException {

        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            //TODO: Optimize so it can find all objects of a single class in one query
            for (String className : objects.keySet()){
                for (long oid : objects.get(className)){
                    if (!cm.isSubClass("InventoryObject", className))
                        throw new OperationNotPermittedException(className, String.format("Class %s is not a business-related class", className));

                    Node instance = getInstanceOfClass(className, oid);
                    Util.deleteObject(instance, releaseRelationships);
                    
                    //Creates an activity log entry
                    Util.createActivityLogEntry(null, specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_GENERAL_ACTIVITY_LOG).getSingle(), 
                            "admin", ActivityLogEntry.ACTIVITY_TYPE_DELETE_INVENTORY_OBJECT, 
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

    public void updateObject(String className, long oid, HashMap<String,List<String>> attributes)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, 
            WrongMappingException, InvalidArgumentException, ApplicationObjectNotFoundException {

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
                        List<Long> listTypeItems = new ArrayList<Long>();
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
                            try{
                                for (String value : attributes.get(attributeName))
                                    listTypeItems.add(Long.valueOf(value));
                            }catch(NumberFormatException ex){
                                throw new InvalidArgumentException(ex.getMessage(), Level.WARNING);
                            }

                            Node listTypeNode = classIndex.get(Constants.PROPERTY_NAME, myClass.getType(attributeName)).getSingle();
                            List<Node> listTypeNodes = Util.getRealValue(listTypeItems, listTypeNode);

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
                        "admin", ActivityLogEntry.ACTIVITY_TYPE_UPDATE_INVENTORY_OBJECT, 
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

    public boolean setBinaryAttributes(String className, long oid, List<String> attributeNames, List<byte[]> attributeValues)
            throws ObjectNotFoundException, OperationNotPermittedException, ArraySizeMismatchException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String name)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {

        if (!cm.isSubClass("InventoryObject", aObjectClass))
            throw new OperationNotPermittedException("Create Relationship", String.format("You can't create relationships between non-inventory objects (%1s)",aObjectClass));

        if (!cm.isSubClass("InventoryObject", bObjectClass))
            throw new OperationNotPermittedException("Create Relationship", String.format("You can't create relationships between non-inventory objects (%1s)",bObjectClass));

        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node nodeA = getInstanceOfClass(aObjectClass, aObjectId);
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
    
    public void releaseSpecialRelationship(String objectClass, long objectId, String name)
            throws ObjectNotFoundException, MetadataObjectNotFoundException {

        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            Node node = getInstanceOfClass(objectClass, objectId);
            for (Relationship rel : node.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                if (rel.getProperty(Constants.PROPERTY_NAME).equals(name))
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

    public void moveObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        
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
                        "admin", ActivityLogEntry.ACTIVITY_TYPE_CHANGE_PARENT, 
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

    public long[] copyObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects, boolean recursive)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {

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
                            "admin", ActivityLogEntry.ACTIVITY_TYPE_CREATE_INVENTORY_OBJECT, 
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

    public boolean setObjectLockState(String className, long oid, Boolean value)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<RemoteBusinessObjectLight> getObjectChildren(String className, long oid, int maxResults)
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
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

    public List<RemoteBusinessObjectLight> getObjectChildren(long oid, long classId, int maxResults)
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
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
    
    public List<RemoteBusinessObjectLight> getSiblings(String className, long oid, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException{
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
    public List<RemoteBusinessObject> getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException{
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
    public List<RemoteBusinessObjectLight> getChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException {
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
    public List<String> getSpecialAttribute(String objectClass, long objectId, String specialAttributeName) throws ObjectNotFoundException, MetadataObjectNotFoundException {
        Node instance = getInstanceOfClass(objectClass, objectId);
        List<String> res = new ArrayList<String>();
        for (Relationship rel : instance.getRelationships(RelTypes.RELATED_TO_SPECIAL))
            if (rel.getProperty(Constants.PROPERTY_NAME).equals(specialAttributeName))
                res.add(String.valueOf(rel.getEndNode().getId() == objectId ? rel.getStartNode().getId() : rel.getEndNode().getId()));
        return res;
    }

    public boolean hasRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships) throws ObjectNotFoundException, MetadataObjectNotFoundException {
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
    
    public boolean hasSpecialRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships) throws ObjectNotFoundException, MetadataObjectNotFoundException {
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
            throws InvalidArgumentException, MetadataObjectNotFoundException{
 
        if (classToMap.isAbstract())
                throw new InvalidArgumentException(String.format("Can not create objects from abstract classes (%s)", classToMap.getName()), Level.OFF);
        
        if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, classToMap.getName()))
            throw new InvalidArgumentException(String.format("Class %s is not a subclass of %s", classToMap.getName(), Constants.CLASS_INVENTORYOBJECT), Level.INFO);

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
                            List<Long> listTypeItems = new ArrayList<Long>();
                            if (!cm.isSubClass(Constants.CLASS_GENERICOBJECTLIST, att.getType()))
                                throw new InvalidArgumentException(String.format("Type %s is not a primitive nor a list type", att.getName()), Level.WARNING);
                            try{
                                for (String value : attributes.get(att.getName()))
                                    listTypeItems.add(Long.valueOf(value));
                            }catch(NumberFormatException ex){
                                throw new InvalidArgumentException(ex.getMessage(), Level.WARNING);
                            }
                            Node listTypeNode = classIndex.get(Constants.PROPERTY_NAME, att.getType()).getSingle();
                            List<Node> listTypeNodes = Util.getRealValue(listTypeItems, listTypeNode);

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
}