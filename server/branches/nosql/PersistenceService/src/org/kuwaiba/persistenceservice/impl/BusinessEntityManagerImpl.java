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
import org.kuwaiba.apis.persistence.business.RemoteObject;
import org.kuwaiba.apis.persistence.business.RemoteObjectLight;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectWithRelationsException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;
import org.kuwaiba.apis.persistence.interfaces.BusinessEntityManager;
import org.kuwaiba.apis.persistence.interfaces.ConnectionManager;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.persistenceservice.caching.CacheManager;
import org.kuwaiba.persistenceservice.impl.enumerations.RelTypes;
import org.kuwaiba.persistenceservice.util.Util;
import org.kuwaiba.psremoteinterfaces.BusinessEntityManagerRemote;
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
     * To label the objects index
     */
    public static final String INDEX_OBJECTS="objects"; //NOI18N
    /**
     * Sets an object read only
     */
    public static final String PROPERTY_IS_LOCKED="isLocked"; //NOI18N
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
        this.classIndex = graphDb.index().forNodes(MetadataEntityManagerImpl.INDEX_CLASS);
        this.objectIndex = graphDb.index().forNodes(INDEX_OBJECTS);
    }

    public Long createObject(String className, Long parentOid, HashMap<String,String> attributes, String template)
            throws ClassNotFoundException, ObjectNotFoundException, NotAuthorizedException, OperationNotPermittedException {

        
        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new ClassNotFoundException("Class "+className+" can not be found");

        //Update the cache if necessary
        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null){
            myClass = Util.createMetadataFromNode(classNode);
            cm.putClass(myClass);
        }

        Node parentNode = null;
        if (parentOid != null){
             parentNode = objectIndex.get(MetadataEntityManagerImpl.PROPERTY_ID,parentOid).getSingle();
            if (parentNode == null)
                throw new ObjectNotFoundException(null, parentOid);
        }

        Transaction tx = graphDb.beginTx();
        try{

            Node newObject = graphDb.createNode();
            newObject.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);

            if (parentOid !=null)
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);

            for (AttributeMetadata att : myClass.getAttributes()){
                if (att.getName().equals(MetadataEntityManagerImpl.PROPERTY_CREATION_DATE))
                    newObject.setProperty(att.getName(), Calendar.getInstance().getTimeInMillis());
                else{
                    String value = attributes.get(att.getName());

                    if (value == null){
                        if (att.getMapping() == AttributeMetadata.MAPPING_BINARY)
                            newObject.setProperty(att.getName(), null);
                        else{
                            if (att.getMapping() != AttributeMetadata.MAPPING_MANYTOMANY &&
                                    att.getMapping() != AttributeMetadata.MAPPING_MANYTOONE){
                                Object actualValue = Util.getRealValue(value, att.getMapping(),att.getType());
                                newObject.setProperty(att.getName(), actualValue);
                            }
                        }
                    }
                }
            }
            objectIndex.putIfAbsent(newObject, MetadataEntityManagerImpl.PROPERTY_ID, newObject.getId());
            tx.success();
            return new Long(newObject.getId());
        }catch(Exception ex){
            Logger.getLogger("createObject: "+ex.getMessage()); //NOI18N
            tx.failure();
            return null;
        }
    }

    public RemoteObject getObjectInfo(String className, Long oid) throws ClassNotFoundException, ObjectNotFoundException, NotAuthorizedException, OperationNotPermittedException {
        //Perform benchmarks to see if accessing to the objects index is less expensive
        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new ClassNotFoundException(className);

        //Update the cache if necessary
        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null){
            myClass = Util.createMetadataFromNode(classNode);
            cm.putClass(myClass);
        }

        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node instance = instances.iterator().next().getEndNode();

            if (instance.getId() == oid.longValue()){
                HashMap<String, List<String>> attributes = new HashMap<String, List<String>>();

                //Iterates through attributes
                Iterable<String> attributeNames = instance.getPropertyKeys();
                while (attributeNames.iterator().hasNext()){
                    String attributeName = attributeNames.iterator().next();
                    List<String> attributeValue = null;
                    if (instance.getProperty(attributeName) != null ){
                        try {
                            if (myClass.getAttributeMapping(attributeName) != AttributeMetadata.MAPPING_BINARY) {
                                attributeValue = new ArrayList<String>();
                                attributeValue.add(instance.getProperty(attributeName).toString());
                            }
                        } catch (InvalidArgumentException ex) { //This should never happen
                            Logger.getLogger(BusinessEntityManagerImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    attributes.put(attributeName,attributeValue);
                }

                //Iterates through relationships and transform the into "plain" attributes
                Iterable<Relationship> relationships = instance.getRelationships(RelTypes.RELATED_TO);
                while(relationships.iterator().hasNext()){
                    Relationship relationship = relationships.iterator().next();
                    String attributeName = relationship.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME).toString();
                    if (attributes.get(attributeName)==null)
                        attributes.put(attributeName, new ArrayList<String>());

                    attributes.get(attributeName).add(String.valueOf(relationship.getEndNode().getId()));

                }
                return new RemoteObject(oid, className,(Boolean)instance.getProperty(PROPERTY_IS_LOCKED));
            }
        }
        throw new ObjectNotFoundException(className, oid);
    }

    public RemoteObjectLight getObjectInfoLight(String className, Long oid) throws ClassNotFoundException, ObjectNotFoundException, NotAuthorizedException, OperationNotPermittedException {
        //Perform benchmarks to see if accessing to the objects index is less expensive
        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new ClassNotFoundException(className);
        Iterable<Relationship> instances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        while (instances.iterator().hasNext()){
            Node instance = instances.iterator().next().getEndNode();

            if (instance.getId() == oid.longValue())
                return new RemoteObjectLight(oid,
                        instance.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME) == null ? null : instance.getProperty(MetadataEntityManagerImpl.PROPERTY_NAME).toString(),
                                            (Boolean)instance.getProperty(PROPERTY_IS_LOCKED));

        }
        throw new ObjectNotFoundException(className, oid);
    }

    public boolean deleteObject(Long oid) throws ObjectWithRelationsException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void updateObject(String className, Long oid, HashMap<String,String> attributes) throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, WrongMappingException, NotAuthorizedException, InvalidArgumentException {

        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME,className).getSingle();

        if (classNode == null)
            throw new ClassNotFoundException(className);

        //Update the cache if necessary
        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null){
            myClass = Util.createMetadataFromNode(classNode);
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
                            throw new InvalidArgumentException(
                                Util.formatString("The attribute %1s is binary or a relationship, so it can't be set using this method. Use setBinaryAttributes or setManyToManyAttributes instead", attributeName), Level.WARNING);
                        }
                    }
                    else{
                        tx.failure();
                        throw new InvalidArgumentException(
                                Util.formatString("The attribute %1s does not exist in class %2s", attributeName, className), Level.WARNING);
                    }
                }
                tx.success();
                return;
            }

        }
        tx.failure();
        throw new ObjectNotFoundException(className, oid);
    }

    public boolean setBinaryAttributes(String className, Long oid, List<String> attributeNames, List<byte[]> attributeValues) throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, ArraySizeMismatchException, NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean setManyToManyAttribute(String className, Long oid, String attributeTypeClassName, String attributeName, List<Long> attributeValues) throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void moveObjects(HashMap<String, List<Long>> objects, String targetClassName, Long targetOid) throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException {
        Node parentClass = classIndex.get(MetadataEntityManagerImpl.INDEX_CLASS, targetClassName).getSingle();

        if (parentClass == null)
            throw new ClassNotFoundException(targetClassName);

        Node parentNode = null;
        Iterable<Relationship> children = parentClass.getRelationships(RelTypes.INSTANCE_OF);
        while (children.iterator().hasNext()){
            Node aChild = children.iterator().next().getEndNode();
        }

   //     List<Node> objects = new ArrayList<Node>();
//        for (Long oid : )

    }

    public RemoteObjectLight[] copyObjects(List<String> objectClassNames, List<Long> templateOids, String targetClassName, Long targetOid) throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException, ArraySizeMismatchException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean setObjectLockSate(String className, Long oid, Boolean value) throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RemoteObjectLight[] getObjectChildren(String className, Long oid) throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<ResultRecord> executeQuery() throws ClassNotFoundException, NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}