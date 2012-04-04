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
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectWithRelationsException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;
import org.kuwaiba.apis.persistence.interfaces.BusinessEntityManager;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.persistenceservice.caching.CacheManager;
import org.kuwaiba.persistenceservice.impl.enumerations.RelTypes;
import org.kuwaiba.persistenceservice.util.Util;
import org.kuwaiba.psremoteinterfaces.BusinessEntityManagerRemote;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.EmbeddedGraphDatabase;

/**
 * Business entity manager reference implementation (using Neo4J as backend)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BusinessEntityManagerImpl implements BusinessEntityManager, BusinessEntityManagerRemote{

    /**
     * To label the objects index
     */
    public static final String INDEX_OBJECTS ="objects"; //NOI18N
    /**
     * Name of the index for list type items
     */
    public static final String INDEX_LIST_TYPE_ITEMS = "listTypeItems"; //NOI18N
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
     * Index for list type items (of all classes)
     */
    private Index<Node> listTypeItemsIndex;
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
        this.classIndex = graphDb.index().forNodes(MetadataEntityManagerImpl.INDEX_CLASS);
        this.objectIndex = graphDb.index().forNodes(INDEX_OBJECTS);
        this.listTypeItemsIndex = graphDb.index().forNodes(INDEX_LIST_TYPE_ITEMS);
    }

    public Long createObject(String className, String parentClassName, Long parentOid, HashMap<String,String> attributes, Long template)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {


        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class %1s can not be found", className));

        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class %1s can not be found", className));

        if (!Util.isSubClass("InventoryObject", classNode))
            throw new OperationNotPermittedException("Create Object", "You can't create non-inventory objects using this method");

        //The object should be created under an instance other than the dummy root
        if (parentClassName != null){
            ClassMetadata myParentObjectClass= cm.getClass(parentClassName);
            if (myParentObjectClass == null)
                throw new MetadataObjectNotFoundException(Util.formatString("Class %1s can not be found", className));

            if (myParentObjectClass.getPossibleChildren().contains(className))
                throw new OperationNotPermittedException("Create Object", Util.formatString("An instance of class %1s can't be created as child of object with id %2s", className, parentOid));
        }

        Node parentNode = null;
        if (parentOid != null){
             parentNode = getInstanceOfClass(parentClassName, parentOid);
            if (parentNode == null)
                throw new ObjectNotFoundException(parentClassName, parentOid);
        }else
            parentNode = graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.OUTGOING).getEndNode();

        Transaction tx = null;
        try{

            tx = graphDb.beginTx();
            Node newObject = graphDb.createNode();
            newObject.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);

            if (parentNode !=null)
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);

            for (AttributeMetadata att : myClass.getAttributes()){
                if (att.getName().equals(MetadataEntityManagerImpl.PROPERTY_CREATION_DATE))
                    newObject.setProperty(att.getName(), Calendar.getInstance().getTimeInMillis());
                else{
                    String value = attributes.get(att.getName());

                    if (value != null){
                        if (att.getMapping() != AttributeMetadata.MAPPING_BINARY){

                            if (att.getMapping() != AttributeMetadata.MAPPING_MANYTOMANY &&
                                    att.getMapping() != AttributeMetadata.MAPPING_MANYTOONE){
                                Object actualValue = Util.getRealValue(value, att.getMapping(),att.getType());
                                newObject.setProperty(att.getName(), actualValue);
                            }
                        }
                    }
                }
            }

            //The object's name can't be null in N4J, it has to be set to ""
            if (attributes.get(MetadataEntityManagerImpl.PROPERTY_NAME) == null)
                newObject.setProperty(MetadataEntityManagerImpl.PROPERTY_NAME, "");
            
            objectIndex.putIfAbsent(newObject, MetadataEntityManagerImpl.PROPERTY_ID, newObject.getId());
            tx.success();
            return new Long(newObject.getId());
        }catch(Exception ex){
            Logger.getLogger("createObject: "+ex.getMessage()); //NOI18N
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    public RemoteBusinessObject getObjectInfo(String className, Long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        //Perform benchmarks to see if accessing to the objects index is less expensive
        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class %1s can not be found", className));

        //Update the cache if necessary
        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null){
            myClass = Util.createClassMetadataFromNode(classNode);
            cm.putClass(myClass);
        }

        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node instance = instances.iterator().next().getEndNode();

            if (instance.getId() == oid.longValue()){
                return Util.createRemoteObjectFromNode(instance, myClass);
            }
        }
        throw new ObjectNotFoundException(className, oid);
    }

    public RemoteBusinessObjectLight getObjectInfoLight(String className, Long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
        //Perform benchmarks to see if accessing to the objects index is less expensive
        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class %1s can not be found", className));
        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node instance = instances.iterator().next().getEndNode();

            if (instance.getId() == oid.longValue())
                return new RemoteBusinessObjectLight(oid,
                        instance.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME) == null ? null : instance.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME).toString(),
                        className);

        }
        throw new ObjectNotFoundException(className, oid);
    }

    public boolean deleteObject(Long oid)
            throws ObjectWithRelationsException, ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateObject(String className, Long oid, HashMap<String,String> attributes) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, WrongMappingException, InvalidArgumentException {

        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME,className).getSingle();

        if (classNode == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class %1s can not be found", className));

        //Update the cache if necessary
        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null){
            myClass = Util.createClassMetadataFromNode(classNode);
            cm.putClass(myClass);
        }

        Transaction tx = graphDb.beginTx();
        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node instance = instances.iterator().next().getEndNode();

            if (instance.getId() == oid.longValue()){
                for (String attributeName : attributes.keySet()){
                    if(instance.hasProperty(attributeName)){
                        if (myClass.getAttributeMapping(attributeName) != AttributeMetadata.MAPPING_BINARY
                                &&myClass.getAttributeMapping(attributeName) != AttributeMetadata.MAPPING_MANYTOMANY
                                &&myClass.getAttributeMapping(attributeName) != AttributeMetadata.MAPPING_MANYTOONE)
                            instance.setProperty(attributeName,Util.getRealValue(attributeName, myClass.getAttributeMapping(attributeName),myClass.getType(attributeName)));
                        else{
                            tx.failure();
                            tx.finish();
                            throw new InvalidArgumentException(
                                Util.formatString("The attribute %1s is binary or a relationship, so it can't be set using this method. Use setBinaryAttributes or setManyToManyAttributes instead", attributeName), Level.WARNING);
                        }
                    }
                    else{
                        tx.failure();
                        tx.finish();
                        throw new InvalidArgumentException(
                                Util.formatString("The attribute %1s does not exist in class %2s", attributeName, className), Level.WARNING);
                    }
                }
                tx.success();
                tx.finish();
                return;
            }

        }
        tx.failure();
        tx.finish();
        throw new ObjectNotFoundException(className, oid);
    }

    public boolean setBinaryAttributes(String className, Long oid, List<String> attributeNames, List<byte[]> attributeValues)
            throws ObjectNotFoundException, OperationNotPermittedException, ArraySizeMismatchException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean setManyToManyAttribute(String className, Long oid, String attributeTypeClassName, String attributeName, List<Long> attributeValues)
            throws ObjectNotFoundException, OperationNotPermittedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void moveObjects(HashMap<String, List<Long>> objects, String targetClassName, Long targetOid)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        Node parentClass = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME, targetClassName).getSingle();

        if (parentClass == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class %1s can not be found", targetClassName));

        Node parentNode = null;
        Iterable<Relationship> children = parentClass.getRelationships(RelTypes.INSTANCE_OF);
        while (children.iterator().hasNext()){
            Node aChild = children.iterator().next().getEndNode();
        }

   //     List<Node> objects = new ArrayList<Node>();
//        for (Long oid : )

    }

    public RemoteBusinessObjectLight[] copyObjects(HashMap<String, Long> objects, String targetClassName, Long targetOid)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException{
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean setObjectLockState(String className, Long oid, Boolean value)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<RemoteBusinessObjectLight> getObjectChildren(String className, Long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
        try{
            Node parentNode = getInstanceOfClass(className, oid);
            Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF);
            List<RemoteBusinessObjectLight> res = new ArrayList<RemoteBusinessObjectLight>();
            while(children.iterator().hasNext()){
                Node child = children.iterator().next().getStartNode();
                res.add(new RemoteBusinessObjectLight(child.getId(),(String)child.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME), getClassName(child)));
            }
            return res;
        }catch (Exception ex){
            throw new RuntimeException (ex.getMessage());
        }
    }

    public List<RemoteBusinessObjectLight> getObjectChildren(Long oid, Long classId)
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
        try{
            Node parentNode = getInstanceOfClass(oid, classId);
            Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF);
            List<RemoteBusinessObjectLight> res = new ArrayList<RemoteBusinessObjectLight>();
            while(children.iterator().hasNext()){
                Node child = children.iterator().next().getStartNode();
                res.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME),getClassName(child)));
            }
            return res;
        }catch(Exception ex){
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public List<RemoteBusinessObject> getChildrenOfClass(Long parentOid, String parentClass, String classToFilter)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException{
        Node parentNode = getInstanceOfClass(parentClass, parentOid);
        Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF);
        List<RemoteBusinessObject> res = new ArrayList<RemoteBusinessObject>();
        while(children.iterator().hasNext()){
            Node child = children.iterator().next().getStartNode();

            if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                throw new MetadataObjectNotFoundException(Util.formatString("Class for object with oid %1s could not be found",child.getId()));

            Node classNode = child.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode();

            ClassMetadata classMetadata = cm.getClass((String)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME));
            if (classToFilter.equals(classMetadata.getName()))
                res.add(Util.createRemoteObjectFromNode(child, classMetadata));
        }
        return res;
    }

    @Override
    public List<RemoteBusinessObjectLight> getChildrenOfClassLight(Long parentOid, String parentClass, String classToFilter)
            throws MetadataObjectNotFoundException, ObjectNotFoundException {
        Node parentNode = getInstanceOfClass(parentClass, parentOid);
        Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF);
        List<RemoteBusinessObjectLight> res = new ArrayList<RemoteBusinessObjectLight>();
        while(children.iterator().hasNext()){
            Node child = children.iterator().next().getStartNode();

            if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                throw new MetadataObjectNotFoundException(Util.formatString("Class for object with oid %1s could not be found",child.getId()));

            Node classNode = child.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode();

            if (classToFilter.equals((String)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME)))
                res.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME),getClassName(child)));
        }
        return res;
    }

    public Long createListTypeItem(String className, String name, String displayName)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME, className).getSingle();
        if (classNode ==  null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class for object with oid %1s could not be found",className));
        if (!Util.isSubClass("GenericObjectList", classNode))
            throw new InvalidArgumentException(Util.formatString("Class %1s is not a list type", className), Level.SEVERE);

        Transaction tx = null;
        try{
             tx = graphDb.beginTx();
             Node newItem = graphDb.createNode();
             newItem.setProperty(MetadataEntityManagerImpl.PROPERTY_NAME, name);
             newItem.setProperty(MetadataEntityManagerImpl.PROPERTY_DISPLAY_NAME, displayName);
             newItem.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);
             listTypeItemsIndex.putIfAbsent(newItem, MetadataEntityManagerImpl.PROPERTY_ID, newItem.getId());
             tx.success();
             return newItem.getId();
        }catch(Exception ex){
            tx.failure();
            throw new RuntimeException(ex.getMessage());
        }finally{
            if (tx != null)
                tx.finish();
        }
    }

    public List<RemoteBusinessObjectLight> getListTypeItems(String className) throws MetadataObjectNotFoundException, InvalidArgumentException{
        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME, className).getSingle();
        if (classNode ==  null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class for object with oid %1s could not be found",className));

        if (!Util.isSubClass("GenericObjectList", classNode))
            throw new InvalidArgumentException(Util.formatString("Class %1s is not a list type", className), Level.SEVERE);

        Iterable<Relationship> childrenAsRelationships = classNode.getRelationships(RelTypes.INSTANCE_OF);
        List<RemoteBusinessObjectLight> children = new ArrayList<RemoteBusinessObjectLight>();

        while(childrenAsRelationships.iterator().hasNext()){
            Node child = childrenAsRelationships.iterator().next().getEndNode();
            children.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME), getClassName(child)));
        }
        return children;
    }

    public List<ClassMetadataLight> getInstanceableListTypes() throws MetadataObjectNotFoundException {
        Node genericObjectListNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME, "GenericObjectList").getSingle();
        if (genericObjectListNode == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class %1s is not a list type", "GenericObjectList"));
        Traverser traverserMetadata = Util.traverserMetadata(genericObjectListNode);
        List<ClassMetadataLight> res = new ArrayList<ClassMetadataLight>();
        for (Node child : traverserMetadata){
            if (!(Boolean)child.getProperty(MetadataEntityManagerImpl.PROPERTY_ABSTRACT))
                res.add(new ClassMetadataLight(child.getId(),(String)child.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME),(String)child.getProperty(MetadataEntityManagerImpl.PROPERTY_DISPLAY_NAME)));
        }

        return res;
    }

    public List<ResultRecord> executeQuery() throws MetadataObjectNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
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
    private Node getInstanceOfClass(String className, Long oid) throws MetadataObjectNotFoundException, ObjectNotFoundException{

        //if any of the parameters is null, return the dummy root
        if (className == null || oid == null)
            return graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.BOTH).getEndNode();


        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME,className).getSingle();

        if (classNode == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class %1s can not be found", className));

        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node otherSide = instances.iterator().next().getStartNode();
            if (otherSide.getId() == oid.longValue())
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
    private Node getInstanceOfClass(Long oid, Long classId) throws MetadataObjectNotFoundException, ObjectNotFoundException{

        //if any of the parameters is null, return the dummy root
        if (classId == null || oid == null)
            return graphDb.getReferenceNode().getSingleRelationship(RelTypes.DUMMY_ROOT, Direction.BOTH).getEndNode();

        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_ID,classId).getSingle();

        if (classNode == null)
            throw new MetadataObjectNotFoundException(Util.formatString("Class with id %1s can not be found", classId));

        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node otherSide = instances.iterator().next().getStartNode();
            if (otherSide.getId() == oid.longValue())
                return otherSide;
        }
        throw new ObjectNotFoundException((String)classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME), oid);
    }

    /**
     * Gets the class name of a given object given its respective node
     * @param instance the node to be tested
     * @return The object class name. Null if none
     */
    private String getClassName(Node instance){
        Iterable<Relationship> aClass = instance.getRelationships(RelTypes.INSTANCE_OF, Direction.OUTGOING);
        if (!aClass.iterator().hasNext())
            return null;
        return (String)aClass.iterator().next().getEndNode().getProperty(MetadataEntityManagerImpl.PROPERTY_NAME);
    }
}