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

package org.kuwaiba.persistenceservice.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
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
public class BusinessEntityManagerImpl implements BusinessEntityManager, BusinessEntityManagerRemote{

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
    }

    public long createObject(String className, String parentClassName, long parentOid, HashMap<String,List<String>> attributes, long template)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, DatabaseException {

        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", className));

        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", className));

        if (!cm.isSubClass("InventoryObject", className))
            throw new OperationNotPermittedException("Create Object", "Can't create non-inventory objects");

        //The object should be created under an instance other than the dummy root
        if (parentClassName != null){
            ClassMetadata myParentObjectClass= cm.getClass(parentClassName);
            if (myParentObjectClass == null)
                throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", className));

            if (!cm.getPossibleChildren(parentClassName).contains(className))
                throw new OperationNotPermittedException("Create Object", String.format("An instance of class %1s can't be created as child of class %2s", className, myParentObjectClass.getName()));
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
            tx.success();
            return newObject.getId();
        }catch(Exception ex){
            Logger.getLogger("createObject: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }
    
    

    public long createSpecialObject(String className, String parentClassName, long parentOid, HashMap<String,List<String>> attributes, long template)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, DatabaseException {

        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", className));

        if (myClass.isAbstractClass())
            throw new OperationNotPermittedException("Create Object", "Can't create objects from an abstract classes");

        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", className));


        //The object should be created under an instance other than the dummy root
        if (parentClassName != null){
            ClassMetadata myParentObjectClass= cm.getClass(parentClassName);
            if (myParentObjectClass == null)
                throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", className));
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
            Node newObject = graphDb.createNode();
            newObject.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);

            if (parentNode !=null)
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);

            for (AttributeMetadata att : myClass.getAttributes()){
                if (att.getName().equals(Constants.PROPERTY_CREATION_DATE)){
                    newObject.setProperty(att.getName(), Calendar.getInstance().getTimeInMillis());
                    continue;
                }

                if (attributes.get(att.getName()) == null)
                    continue;

                switch (att.getMapping()){
                    case AttributeMetadata.MAPPING_PRIMITIVE:
                    case AttributeMetadata.MAPPING_DATE:
                    case AttributeMetadata.MAPPING_TIMESTAMP:
                        if (attributes.get(att.getName()) != null){
                            //If the array is empty, it means the attribute should be set to null
                            if (!attributes.get(att.getName()).isEmpty()){
                                if (attributes.get(att.getName()).get(0) != null)
                                    newObject.setProperty(att.getName(),
                                            Util.getRealValue(attributes.get(att.getName()).get(0),
                                            myClass.getAttributeMapping(att.getName()),
                                            myClass.getType(att.getName())));
                            }
                        }
                    break;
                    case AttributeMetadata.MAPPING_MANYTOMANY:
                    case AttributeMetadata.MAPPING_MANYTOONE:
                        List<Long> listTypeItems = new ArrayList<Long>();
                        if (!cm.isSubClass("GenericObjectList", att.getType()))
                            throw new InvalidArgumentException(String.format("Class %1s is not a list type", att.getName()), Level.WARNING);
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
                    break;
                    default:
                        tx.failure();
                        tx.finish();
                        throw new InvalidArgumentException(
                            String.format("The attribute %1s is binary so it can't be set using this method. Use setBinaryAttributes instead", att.getName()), Level.WARNING);
                }
            }

            //The object's name can't be null in N4J, it has to be set to ""
            if (attributes.get(Constants.PROPERTY_NAME) == null)
                newObject.setProperty(Constants.PROPERTY_NAME, "");

            objectIndex.putIfAbsent(newObject, Constants.PROPERTY_ID, newObject.getId());
            tx.success();
            return newObject.getId();
        }catch(Exception ex){
            Logger.getLogger("createObject: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    public RemoteBusinessObject getObjectInfo(String className, long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        ClassMetadata myClass = cm.getClass(className);
        Node instance = getInstanceOfClass(className, oid);
        RemoteBusinessObject res = Util.createRemoteObjectFromNode(instance, myClass);
        return res;
    }

    public RemoteBusinessObjectLight getObjectInfoLight(String className, long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
        //Perform benchmarks to see if accessing to the objects index is less expensive
        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", className));
        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node instance = instances.iterator().next().getStartNode();

            if (instance.getId() == oid)
                return new RemoteBusinessObjectLight(oid,
                        instance.getProperty(Constants.PROPERTY_NAME) == null ? null : instance.getProperty(Constants.PROPERTY_NAME).toString(),
                        className);

        }
        throw new ObjectNotFoundException(className, oid);
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
                        throw new OperationNotPermittedException(className, String.format("Class %1s is not a business-related class", className));

                    Node instance = getInstanceOfClass(className, oid);
                    Util.deleteObject(instance, releaseRelationships);
                }
            }
            tx.success();
        }catch(Exception ex){
            Logger.getLogger("deleteObject: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    public void updateObject(String className, long oid, HashMap<String,List<String>> attributes)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, WrongMappingException, InvalidArgumentException {

        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();

        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", className));

        ClassMetadata myClass= cm.getClass(className);

        Transaction tx = graphDb.beginTx();
        Node instance = getInstanceOfClass(className, oid);

        for (String attributeName : attributes.keySet()){
            if(myClass.hasAttribute(attributeName)){

                switch (myClass.getAttributeMapping(attributeName)){
                    case AttributeMetadata.MAPPING_PRIMITIVE:
                    case AttributeMetadata.MAPPING_DATE:
                    case AttributeMetadata.MAPPING_TIMESTAMP:
                        if (attributes.get(attributeName) == null)
                            instance.removeProperty(attributeName);
                        else{
                            //If the array is empty, it means the attribute should be set to null
                            if (attributes.get(attributeName).isEmpty())
                                instance.removeProperty(attributeName);
                            else{
                                if (attributes.get(attributeName).get(0) == null)
                                    instance.removeProperty(attributeName);
                                else
                                    instance.setProperty(attributeName,Util.getRealValue(attributes.get(attributeName).get(0), myClass.getAttributeMapping(attributeName),myClass.getType(attributeName)));
                            }
                        }
                    break;
                    case AttributeMetadata.MAPPING_MANYTOMANY:
                    case AttributeMetadata.MAPPING_MANYTOONE:
                        List<Long> listTypeItems = new ArrayList<Long>();
                        if (!cm.getClass(myClass.getType(attributeName)).isListType())
                            throw new InvalidArgumentException(String.format("Class %1s is not a list type", myClass.getType(attributeName)), Level.WARNING);
                        
                        if (attributes.get(attributeName) == null){
                            Util.releaseRelationships(instance, RelTypes.RELATED_TO, Direction.OUTGOING, Constants.PROPERTY_NAME,
                                attributeName);
                            break;
                        }

                        try{
                            for (String value : attributes.get(attributeName))
                                listTypeItems.add(Long.valueOf(value));
                        }catch(NumberFormatException ex){
                            throw new InvalidArgumentException(ex.getMessage(), Level.WARNING);
                        }
                        Node listTypeNode = classIndex.get(Constants.PROPERTY_NAME, myClass.getType(attributeName)).getSingle();
                        List<Node> listTypeNodes = Util.getRealValue(listTypeItems, listTypeNode);

                        Util.releaseRelationships(instance, RelTypes.RELATED_TO, Direction.OUTGOING, Constants.PROPERTY_NAME,
                                attributeName);

                        //Create the new relationships
                        for (Node item : listTypeNodes){
                            Relationship newRelationship = instance.createRelationshipTo(item, RelTypes.RELATED_TO);
                            newRelationship.setProperty(Constants.PROPERTY_NAME, attributeName);
                        }
                    break;
                    default:
                        tx.failure();
                        tx.finish();
                        throw new InvalidArgumentException(
                            String.format("The attribute %1s is binary so it can't be set using this method. Use setBinaryAttributes instead", attributeName), Level.WARNING);
                }
            }
            else{
                tx.failure();
                tx.finish();
                throw new InvalidArgumentException(
                        String.format("The attribute %1s does not exist in class %2s", attributeName, className), Level.WARNING);
            }
        }
        tx.success();
        tx.finish();
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
        }catch(Exception ex){
            Logger.getLogger("createSpecialRelationship: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    public void moveObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        
        ClassMetadata newParentClass = cm.getClass(targetClassName);
        
        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", targetClassName));

        Node newParentNode = getInstanceOfClass(targetClassName, targetOid);

        Transaction tx = null;
        try{
            tx = graphDb.beginTx();
            for (String myClass : objects.keySet()){
                if (!cm.canBeChild(targetClassName, myClass))
                    throw new OperationNotPermittedException("moveObjects", String.format("An instance of class %1s can not be child of an instance of class %2s", myClass,targetClassName));

                Node instanceClassNode = classIndex.get(Constants.PROPERTY_NAME, myClass).getSingle();
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", myClass));
                for (long oid : objects.get(myClass)){
                    Node instance = getInstanceOfClass(instanceClassNode, oid);
                    if (instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().hasNext())
                        instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().next().delete();
                    //If the object was child of a pool
                    if (instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().hasNext())
                        instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().next().delete();
                    
                    instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
                }
            }
            tx.success();
        }catch(Exception ex){
            Logger.getLogger("moveObjects: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    public long[] copyObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects, boolean recursive)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {

        ClassMetadata newParentClass = cm.getClass(targetClassName);

        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", targetClassName));

        Node newParentNode = getInstanceOfClass(targetClassName, targetOid);

        Transaction tx = null;

        try{
            tx = graphDb.beginTx();
            long[] res = new long[objects.size()];
            int i = 0;
            for (String myClass : objects.keySet()){
                if (!cm.canBeChild(targetClassName, myClass))
                    throw new OperationNotPermittedException("copyObjects", String.format("An instance of class %1s can not be child of an instance of class %2s", myClass,targetClassName));

                Node instanceClassNode = classIndex.get(Constants.PROPERTY_NAME, myClass).getSingle();
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", myClass));
                for (long oid : objects.get(myClass)){
                    Node templateObject = getInstanceOfClass(instanceClassNode, oid);
                    Node newInstance = copyObject(templateObject, recursive);
                    newInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
                    res[i] =newInstance.getId();
                    i++;
                }
            }
            tx.success();
            return res;
        }catch(Exception ex){
            Logger.getLogger("copyObjects: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
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
            if(classId == -1 && oid == -1){
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
    public List<RemoteBusinessObject> getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException{
        Node parentNode = getInstanceOfClass(parentClass, parentOid);
        Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
        List<RemoteBusinessObject> res = new ArrayList<RemoteBusinessObject>();

        int counter = 0;

        while(children.iterator().hasNext()){
            Node child = children.iterator().next().getStartNode();

            if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                throw new MetadataObjectNotFoundException(String.format("Class for object with oid %1s could not be found",child.getId()));

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
                throw new MetadataObjectNotFoundException(String.format("Class for object with oid %1s could not be found",child.getId()));

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
                throw new MetadataObjectNotFoundException(String.format("Class for object with oid %1s could not be found",child.getId()));

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
                throw new MetadataObjectNotFoundException(String.format("Class for object with oid %1s could not be found",child.getId()));

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

    public List<ResultRecord> executeQuery() throws MetadataObjectNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<String> getSpecialAttribute(String objectClass, long objectId, String specialAttributeName) throws ObjectNotFoundException, MetadataObjectNotFoundException {
        Node instance = getInstanceOfClass(objectClass, objectId);
        List<String> res = new ArrayList<String>();
        for (Relationship rel : instance.getRelationships(RelTypes.RELATED_TO_SPECIAL))
            if (rel.getProperty(Constants.PROPERTY_NAME).equals(specialAttributeName))
                res.add(String.valueOf(rel.getEndNode().getId() == objectId ? rel.getStartNode().getId() : rel.getEndNode().getId()));
        return res;
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
            throw new MetadataObjectNotFoundException(String.format("Class %1s can not be found", className));

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
            throw new MetadataObjectNotFoundException(String.format("Class with id %1s can not be found", classId));

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
 
        if (classToMap.isAbstractClass())
                throw new InvalidArgumentException(String.format("Can not create objects from abstract classes (%1s)", classToMap.getName()), Level.OFF);
        
        if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, classToMap.getName()))
            throw new InvalidArgumentException(String.format("Class %1s is not a subclass of %2s", classToMap.getName(), Constants.CLASS_INVENTORYOBJECT), Level.INFO);

        Node newObject = graphDb.createNode();

        for (AttributeMetadata att : classToMap.getAttributes()){
            if (att.getName().equals(Constants.PROPERTY_CREATION_DATE)){
                newObject.setProperty(att.getName(), Calendar.getInstance().getTimeInMillis());
                continue;
            }

            if (attributes.get(att.getName()) == null)
                continue;

            switch (att.getMapping()){
                case AttributeMetadata.MAPPING_PRIMITIVE:
                case AttributeMetadata.MAPPING_DATE:
                case AttributeMetadata.MAPPING_TIMESTAMP:
                    if (attributes.get(att.getName()) != null){
                        //If the array is empty, it means the attribute should be set to null
                        if (!attributes.get(att.getName()).isEmpty()){
                            if (attributes.get(att.getName()).get(0) != null)
                                newObject.setProperty(att.getName(),
                                        Util.getRealValue(attributes.get(att.getName()).get(0),
                                        classToMap.getAttributeMapping(att.getName()),
                                        classToMap.getType(att.getName())));
                        }
                    }
                break;
                case AttributeMetadata.MAPPING_MANYTOMANY:
                case AttributeMetadata.MAPPING_MANYTOONE:
                    List<Long> listTypeItems = new ArrayList<Long>();
                    if (!cm.isSubClass(Constants.CLASS_GENERICOBJECTLIST, att.getType()))
                        throw new InvalidArgumentException(String.format("Class %1s is not a list type", att.getName()), Level.WARNING);
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
                break;
                default:
                    throw new InvalidArgumentException(
                        String.format("The attribute %1s is binary so it can't be set using this method. Use setBinaryAttributes instead", att.getName()), Level.WARNING);
            }
        }

        //The object's name can't be null in N4J, it has to be set to ""
        if (attributes.get(Constants.PROPERTY_NAME) == null)
            newObject.setProperty(Constants.PROPERTY_NAME, "");

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