/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.services.persistence.impl.neo4j;

import com.neotropic.kuwaiba.modules.reporting.defaults.DefaultReports;
import com.neotropic.kuwaiba.modules.reporting.InventoryReport;
import com.neotropic.kuwaiba.modules.reporting.img.SceneExporter;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReport;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.ConnectionManager;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.Contact;
import org.kuwaiba.apis.persistence.application.FileObject;
import org.kuwaiba.apis.persistence.application.FileObjectLight;
import org.kuwaiba.apis.persistence.application.Pool;
import org.kuwaiba.apis.persistence.application.Validator;
import org.kuwaiba.apis.persistence.application.ValidatorDefinition;
import org.kuwaiba.apis.persistence.business.AnnotatedBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.business.BusinessObjectLightList;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.services.persistence.cache.CacheManager;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.services.persistence.util.Util;
import org.kuwaiba.util.ChangeDescriptor;
import org.kuwaiba.util.dynamicname.DynamicName;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterators;

/**
 * Business entity manager reference implementation (using Neo4J as backend)
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class BusinessEntityManagerImpl implements BusinessEntityManager {
    /**
     * Default attachment location
     */
    private static String DEFAULT_ATTACHMENTS_PATH = "/data/files/attachments";
    /**
     * Reference to the Application Entity Manager
     */
    private final ApplicationEntityManager aem;
    /**
     * Reference to the Metadata Entity Manager
     */
    private final MetadataEntityManager mem;
    /**
     * Reference to the db handler
     */
    private final GraphDatabaseService graphDb;
    /**
     * Object Label
     */
    private final Label inventoryObjectLabel;
    /**
     * Class Label
     */
    private final Label classLabel;
    /**
     * Pools Label
     */
    private final Label poolLabel;
    /**
     * Special nodes Label
     */
    private final Label specialNodeLabel;
    /**
     * Label for reports
     */
    private final Label reportsLabel;
    /**
     * Contacts index
     */
    private Label contactsLabel;    
    /**
     * As a temporary workaround, the old hard-coded reports are wrapped instead of being completely migrated to Groovy scripts
     */
    private final DefaultReports defaultReports;
    /**
     * Global configuration variables
     */
    private Properties configuration;

    /**
     * Main constructor. It receives references to the other entity managers
     * @param cmn Reference to the ConnectionManager instance.
     * @param aem Reference to the ApplicationManager instance.
     * @param mem Reference to the MetadataManager instance. 
     */
    public BusinessEntityManagerImpl(ConnectionManager cmn, ApplicationEntityManager aem, MetadataEntityManager mem) {
        this.aem = aem;
        this.mem = mem;
        this.defaultReports = new DefaultReports(mem, this, aem);
        this.graphDb = (GraphDatabaseService)cmn.getConnectionHandler();
        
        this.configuration = new Properties();
        
        this.inventoryObjectLabel = Label.label(Constants.LABEL_INVENTORY_OBJECT);
        this.classLabel = Label.label(Constants.LABEL_CLASS);
        this.poolLabel = Label.label(Constants.LABEL_POOL);
        this.specialNodeLabel = Label.label(Constants.LABEL_SPECIAL_NODE);
        this.reportsLabel = Label.label(Constants.LABEL_REPORTS);
        this.contactsLabel = Label.label(Constants.LABEL_CONTACTS);
    }
    
    @Override
    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }

    @Override
    public long createObject(String className, String parentClassName, long parentOid, HashMap<String, String> attributes, long template)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        ClassMetadata myClass= mem.getClass(className);
        
        if (!mem.getPossibleChildren(parentClassName).contains(myClass)) 
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
        
        try (Transaction tx = graphDb.beginTx()) {        
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

            if (myClass.isInDesign())
                throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");

            if (myClass.isAbstract())
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", className));

            if (!mem.isSubClass(Constants.CLASS_INVENTORYOBJECT, className))
                throw new OperationNotPermittedException("Can not create non-inventory objects");

            //The object should be created under an instance other than the dummy root
            if (parentClassName != null && parentOid != -1) {
                ClassMetadata myParentObjectClass= mem.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", parentClassName));
            }

            Node parentNode;
            if (parentOid != -1){
                 parentNode = getInstanceOfClass(parentClassName, parentOid);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentOid);
            }
            else
                parentNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            Node newObject;
            if (template == -1)
                newObject = createObject(classNode, myClass, attributes);
            else {
                Node templateNode = null;
                for (Relationship hasTemplateRelationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_TEMPLATE)) {
                    Node endNode = hasTemplateRelationship.getEndNode();
                    if (endNode.getId() == template){
                        templateNode = endNode;
                        break;
                    }
                }
                
                if (templateNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", template, className));
                
                newObject = copyTemplateElement(templateNode, myClass, true);
            }
            newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);
            tx.success();
            return newObject.getId();
        }
    }
    
    //TODO: Rewrite this!
    @Override
    public long createObject(String className, String parentClassName, String criteria, HashMap<String, String> attributes, long template)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException {
        
        ClassMetadata objectClass = mem.getClass(className);
        if (objectClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
        
        if (objectClass.isInDesign())
            throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");

        if (objectClass.isAbstract())
            throw new OperationNotPermittedException("Can not create instances of abstract classes");

        if (!mem.isSubClass("InventoryObject", className))
            throw new OperationNotPermittedException("Can not create non-inventory objects");
        
        if (!mem.getPossibleChildren(parentClassName).contains(objectClass))
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
        
        String[] splitCriteria = criteria.split(":");
        if (splitCriteria.length != 2)
            throw new InvalidArgumentException("The criteria is not valid, two components expected (attributeName:attributeValue)");

        if (splitCriteria[0].equals(Constants.PROPERTY_OID)) //The user is providing the id of te parent node explicitely
            return createObject(className, parentClassName, Long.parseLong(splitCriteria[1]), attributes, template);

        ClassMetadata parentClass = mem.getClass(parentClassName);
        if (parentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", parentClassName));

        AttributeMetadata filterAttribute = parentClass.getAttribute(splitCriteria[0]);

        if (filterAttribute == null)
            throw new MetadataObjectNotFoundException(String.format("Attribute %s could not be found in class %s", splitCriteria[0], parentClassName));

        if (!AttributeMetadata.isPrimitive(filterAttribute.getType()))
            throw new InvalidArgumentException(String.format(
                    "The filter provided (%s) is not a primitive type. Non-primitive types are not supported as they typically don't uniquely identify an object", 
                    splitCriteria[0]));
        
        try (Transaction tx = graphDb.beginTx()) {
           
            Node parentClassNode, parentNode = null;
            
            if (Constants.NODE_DUMMYROOT.equals(parentClassName)) {
                parentNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
                
            } else {
                parentClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, parentClassName);
                
                Iterator<Relationship> instances = parentClassNode.getRelationships(RelTypes.INSTANCE_OF).iterator();

                while (instances.hasNext()){
                    Node possibleParentNode = instances.next().getStartNode();                   
                    if (possibleParentNode.getProperty(splitCriteria[0]).toString().equals(splitCriteria[1])) {
                        parentNode = possibleParentNode;
                        break;
                    }
                }
            }
            
            if(parentNode == null)
                throw new InvalidArgumentException(String.format("A parent object of class %s and %s = %s could not be found", parentClassName, splitCriteria[0], splitCriteria[1]));
            
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

            Node newObject;
            if (template <= 0)
                newObject = createObject(classNode, objectClass, attributes);
            else {
                Node templateNode = null;
                for (Relationship hasTemplateRelationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_TEMPLATE)) {
                    Node endNode = hasTemplateRelationship.getEndNode();
                    if (endNode.getId() == template){
                        templateNode = endNode;
                        break;
                    }
                }
                
                if (templateNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", template, className));
                
                newObject = copyTemplateElement(templateNode, objectClass, true);
            }
            
            newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);
                      
            tx.success();
            return newObject.getId();
        }
    }
    
    @Override
    public long createSpecialObject(String className, String parentClassName, long parentOid, HashMap<String,String> attributes, long template)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {

        ClassMetadata classMetadata= mem.getClass(className);
        if (classMetadata == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

        if (classMetadata.isInDesign())
            throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");
        
        if (classMetadata.isAbstract())
            throw new OperationNotPermittedException("Can not create objects of abstract classes");
        
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME,className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

            //The object should be created under an instance other than the dummy root
            if (parentClassName != null){
                ClassMetadata myParentObjectClass= mem.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            }

            Node parentNode = null;
            if (parentOid != -1){
                 parentNode = getInstanceOfClass(parentClassName, parentOid);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentOid);
            }
        
            Node newObject;
            
            if (template == -1) 
                newObject = createObject(classNode, classMetadata, attributes);
                
            else {
                Node templateNode = null;
                for (Relationship hasTemplateRelationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_TEMPLATE)) {
                    Node endNode = hasTemplateRelationship.getEndNode();
                    if (endNode.getId() == template){
                        templateNode = endNode;
                        break;
                    }
                }
                
                if (templateNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", template, className));
                
                newObject = copyTemplateElement(templateNode, classMetadata, true);
                updateObject(newObject, classMetadata, attributes); //Override the template values with those provided, if any
            }
            if (parentNode !=null)
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
            
            tx.success();
            return newObject.getId();
        }
    }
    
    @Override
    public long createPoolItem(long poolId, String className, String[] attributeNames, 
    String[] attributeValues, long templateId) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, 
            ArraySizeMismatchException, MetadataObjectNotFoundException {
        
        if (attributeNames != null && attributeValues != null){
            if (attributeNames.length != attributeValues.length)
            throw new ArraySizeMismatchException("attributeNames", "attributeValues");
        }
        
        try(Transaction tx =graphDb.beginTx()) {
            Node pool = Util.findNodeByLabelAndId(poolLabel, poolId);
            
            if (pool == null)
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
            
            if (!pool.hasProperty(Constants.PROPERTY_CLASS_NAME))
                throw new InvalidArgumentException("This pool has not set his class name attribute");
            
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            ClassMetadata classMetadata = mem.getClass(className);
            
            if (!mem.isSubClass((String)pool.getProperty(Constants.PROPERTY_CLASS_NAME), className))
                throw new InvalidArgumentException(String.format("Class %s is not subclass of %s", className, (String)pool.getProperty(Constants.PROPERTY_CLASS_NAME)));
            
            HashMap<String, String> attributes = new HashMap<>();
            if (attributeNames != null && attributeValues != null){
                for (int i = 0; i < attributeNames.length; i++)
                    attributes.put(attributeNames[i], attributeValues[i]);
            }
            
            Node newObject = createObject(classNode, classMetadata, attributes);
            newObject.createRelationshipTo(pool, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
            
            tx.success();
            return newObject.getId();
        }
    }
    
    @Override
    public long [] createBulkObjects(String className, String parentClassName, long parentOid, int numberOfObjects, String namePattern) 
        throws MetadataObjectNotFoundException, OperationNotPermittedException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        ClassMetadata myClass = mem.getClass(className);
        
        if (!mem.getPossibleChildren(parentClassName).contains(myClass))
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
        
        try (Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
                       
            if (myClass.isInDesign())
                throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");
            
            if (myClass.isAbstract())
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", className));
            
            if (!mem.isSubClass(Constants.CLASS_INVENTORYOBJECT, className))
                throw new OperationNotPermittedException("Can not create non-inventory objects");
            //The object should be created under an instance other than the dummy root
            if (parentClassName != null && parentOid != -1) {
                ClassMetadata myParentObjectClass = mem.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", parentClassName));
            }
            Node parentNode;
            if (parentOid != -1) {
                parentNode = getInstanceOfClass(parentClassName, parentOid);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentOid);
                
            }
            else
                parentNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            if (parentNode == null)
                throw new BusinessObjectNotFoundException(parentClassName, parentOid);
            
            DynamicName dynamicName = new DynamicName(namePattern);
            if (dynamicName.getNumberOfDynamicNames() < numberOfObjects) {
                throw new InvalidArgumentException("The given pattern to generate the name has "
                        + "less possibilities that the number of objects to be created");
            }
            long res[] = new long[numberOfObjects];
            
            List<StringPair> createdMirrorPorts = new ArrayList<>();
            
            for (int i = 0; i < numberOfObjects; i++) {
                Node newObject = createObject(classNode, myClass, null);
                newObject.setProperty(Constants.PROPERTY_NAME, dynamicName.getDynamicNames().get(i));
                
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);
                
                res[i] = newObject.getId();
                
                if(dynamicName.isMirrorPortsSequence())
                    createdMirrorPorts.add(new StringPair(Long.toString(newObject.getId()), dynamicName.getDynamicNames().get(i)));
            }
            
            if(dynamicName.isMirrorPortsSequence())
                dynamicName.createMirrorRelationships(createdMirrorPorts, className);
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public long[] createBulkSpecialObjects(String className, String parentClassName, long parentId, int numberOfSpecialObjects, String namePattern) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {
        
        ClassMetadata myClass= mem.getClass(className);
        if (myClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
        
        if (!mem.getPossibleSpecialChildren(parentClassName).contains(myClass))
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));

        if (!mem.isSubClass(Constants.CLASS_INVENTORYOBJECT, className))
            throw new OperationNotPermittedException(String.format("Class %s is not an business class", className));
        
        if (myClass.isInDesign())
            throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");
        
        if (myClass.isAbstract())
            throw new OperationNotPermittedException("Can't create objects from an abstract classes");

        try (Transaction tx =graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME,className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));


            //The object should be created under an instance other than the dummy root
            if (parentClassName != null){
                ClassMetadata myParentObjectClass= mem.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            }

            Node parentNode;
            if (parentId != -1){
                 parentNode = getInstanceOfClass(parentClassName, parentId);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentId);
            }
            else
                parentNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
                        
            DynamicName dynamicName = new DynamicName(namePattern);
            if (dynamicName.getNumberOfDynamicNames() < numberOfSpecialObjects) {
                throw new InvalidArgumentException("The given pattern to generate the name has "
                        + "less possibilities that the number of object to be created");
            }
            long res[] = new long[numberOfSpecialObjects];
            for (int i = 0; i < numberOfSpecialObjects; i++) {
                Node newObject = createObject(classNode, myClass, null);
                newObject.setProperty(Constants.PROPERTY_NAME, dynamicName.getDynamicNames().get(i));
                if (parentNode != null)
                    newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
                
                res[i] = newObject.getId();
            }
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public BusinessObject getObject(String className, long oid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = graphDb.beginTx()) {
            ClassMetadata myClass = mem.getClass(className);
            Node instance = getInstanceOfClass(className, oid);
            BusinessObject res = createObjectFromNode(instance, myClass);
            tx.success();
            return res;
        }
    }
    
    @Override
    public BusinessObject getObject(long oid) throws InvalidArgumentException, BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        String className = null;
        
        try (Transaction tx = graphDb.beginTx()) {
            Node objectNode = Util.findNodeByLabelAndId(inventoryObjectLabel, oid);
            
            if (objectNode == null)
                throw new InvalidArgumentException(String.format("The object with id %s could not be found", oid));
            
            
            if (objectNode.hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING)) {
                for (Relationship relationship : objectNode.getRelationships(RelTypes.INSTANCE_OF, Direction.OUTGOING)) {
                    Node classNode = relationship.getEndNode();
                    if (classNode.hasProperty(Constants.PROPERTY_NAME)) {
                        className = (String) classNode.getProperty(Constants.PROPERTY_NAME);
                        break;
                    }
                }
            }
        }
        
        if (className == null)
            throw new InvalidArgumentException(String.format("The class for object with id %s could not be found", oid));
        
        return getObject(className, oid);
    }

    @Override
    public BusinessObjectLight getObjectLight(String className, long oid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        
        //TODO: Re-write this method and check if a simple Cypher query is faster than the programatic solution!
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
            Iterator<Relationship> instances = iterableInstances.iterator();
            while (instances.hasNext()){
                Node instance = instances.next().getStartNode();
                if (instance.getId() == oid) {
                    tx.success();
                    return createObjectLightFromNode(instance);
                }
            }
            throw new BusinessObjectNotFoundException(className, oid);
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectsWithFilterLight (String className, 
            String filterName, String filterValue) throws MetadataObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            tx.success();
            return getObjectsWithFilterLight(classNode, filterName, filterValue);
        }
    }
    
    @Override
    public List<BusinessObject> getObjectsWithFilter (String className, 
            String filterName, String filterValue) throws MetadataObjectNotFoundException, InvalidArgumentException {
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            tx.success();
            return getObjectsWithFilter(classNode, filterName, filterValue);
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(String filter, int limit) {
        try (Transaction tx = graphDb.beginTx()) {
            String cypherQuery = "MATCH (object:" + inventoryObjectLabel + ")-[:INSTANCE_OF]->(class)" + 
                    " WHERE TOLOWER(object.name) CONTAINS TOLOWER({searchString}) OR TOLOWER(class.name) CONTAINS TOLOWER({searchString}) RETURN object.name as oname, id(object) as oid, class.name as cname ORDER BY object.name ASC" + (limit > 0 ? " LIMIT " + limit : ""); //NOI18N
            
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("searchString", filter);
            Result queryResult = graphDb.execute(cypherQuery, parameters);
            
            List<BusinessObjectLight> res  = new ArrayList<>();
            
            while (queryResult.hasNext()) {
                Map<String, Object> row = queryResult.next();
                res.add(new BusinessObjectLight((String)row.get("cname"), (long)row.get("oid"), 
                        (String)row.get("oname")));
            }
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(String filter, String superClass, int limit) {
        try (Transaction tx = graphDb.beginTx()) {
            String cypherQuery = "MATCH (object:" + inventoryObjectLabel + ")-[:INSTANCE_OF]->(class)" +
                    "-[:EXTENDS*0..]->(superclass) WHERE (TOLOWER(object.name) CONTAINS TOLOWER({searchString})" + 
                    " OR TOLOWER(class.name) CONTAINS TOLOWER({searchString})) AND superclass.name={superclass} " + 
                    "RETURN object.name as oname, id(object) as oid, class.name as cname ORDER BY object.name ASC" + (limit > 0 ? " LIMIT " + limit : "");

            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("searchString", filter);
            parameters.put("superclass", superClass);
            Result queryResult = graphDb.execute(cypherQuery, parameters);
            
            List<BusinessObjectLight> res  = new ArrayList<>();
            
            while (queryResult.hasNext()) {
                Map<String, Object> row = queryResult.next();
                res.add(new BusinessObjectLight((String)row.get("cname"), (long)row.get("oid"), 
                        (String)row.get("oname")));
            }
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public String getAttributeValueAsString (String objectClass, long objectId, String attributeName) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        ClassMetadata theClass = mem.getClass(objectClass);
        AttributeMetadata theAttribute = theClass.getAttribute(attributeName);

        BusinessObject theObject = getObject(objectClass, objectId);
        if (theObject.getAttributes().get(attributeName) == null)
            return null;
        else {

            switch (theAttribute.getType()) {
                case "String": //NOI18N
                case "Boolean": //NOI18N
                case "Integer": //NOI18N
                case "Float": //NOI18N
                case "Long": //NOI18N
                    return theObject.getAttributes().get(attributeName);
                case "Date": //NOI18N
                case "Time": //NOI18N
                case "Timestamp": //NOI18N
                    return new Date(Long.valueOf(theObject.getAttributes().get(attributeName))).toString();
                default: //It's (or at least should be) a list type
                    return aem.getListTypeItem(theAttribute.getType(), Long.valueOf(theObject.getAttributes().get(attributeName))).getName();
            }
        }
    }
    
    @Override
    public HashMap<String, String> getAttributeValuesAsString (String objectClass, long objectId) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
    
        BusinessObject theObject = getObject(objectClass, objectId);
        HashMap<String, String> res = new HashMap<>();
        
        for (String attributeName : theObject.getAttributes().keySet()) {
            AttributeMetadata theAttribute = mem.getAttribute(objectClass, attributeName);
            if (theObject.getAttributes().get(attributeName) == null)
                res.put(attributeName, null);
            else { 
                switch (theAttribute.getType()) {
                    case "String": //NOI18N
                    case "Boolean": //NOI18N
                    case "Integer": //NOI18N
                    case "Float": //NOI18N
                    case "Long": //NOI18N
                        res.put(attributeName, theObject.getAttributes().get(attributeName));
                        break;
                    case "Date": //NOI18N
                    case "Time": //NOI18N
                    case "Timestamp": //NOI18N
                        res.put(attributeName, new Date(Long.valueOf(theObject.getAttributes().get(attributeName))).toString());
                        break;
                    default: //It's (or at least should be) a list type
                        res.put(attributeName, aem.getListTypeItem(theAttribute.getType(), Long.valueOf(theObject.getAttributes().get(attributeName))).getName());
                }
            }
        }
        
        return res;
    }
    
    
    @Override
    public BusinessObjectLight getCommonParent(String aObjectClass, long aOid, String bObjectClass, long bOid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        // while we will find a better way to do the query, we use this way
        BusinessObjectLight aParent = getParent(aObjectClass, aOid);
        BusinessObjectLight bParent = getParent(bObjectClass, bOid);
        
        if (aParent.getId() == bParent.getId())
            return aParent;
        
        List<BusinessObjectLight> aParents = new ArrayList();
        List<BusinessObjectLight> bParents = new ArrayList();
        
        aParents.add(aParent);
        while (aParent.getId() != -1L) {
            aParent = getParent(aParent.getClassName(), aParent.getId());
            aParents.add(aParent);
        }
        
        bParents.add(bParent);
        while (bParent.getId() != -1L) {
            bParent = getParent(bParent.getClassName(), bParent.getId());
            bParents.add(bParent);
        }
        
        for (int i = 0; i < aParents.size(); i++) {
            for (int j = 0; j < bParents.size(); j++) {
                if (aParents.get(i).getId() == bParents.get(j).getId())
                    return aParents.get(i);                                
            }
        }
                        
        return null;
    }
    
    @Override
    public BusinessObjectLight getParent(String objectClass, long oid) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(objectClass, oid);
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF)){
                Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();

                //If the direct parent is DummyRoot, return a dummy RemoteBusinessObject with oid = -1
                if (parentNode.hasProperty(Constants.PROPERTY_NAME) && Constants.NODE_DUMMYROOT.equals(parentNode.getProperty(Constants.PROPERTY_NAME)) )
                    return new BusinessObject(Constants.NODE_DUMMYROOT, -1, Constants.NODE_DUMMYROOT);
                else    
                    return createObjectLightFromNode(parentNode);
            }
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF_SPECIAL)){
                Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                if (parentNode.hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING))
                    return createObjectLightFromNode(parentNode);
                else
                    // Use the dummy root like parent to services, contracts, projects poolNode...
                    return new BusinessObject(Constants.NODE_DUMMYROOT, -1, Constants.NODE_DUMMYROOT);
                
            }
            throw new InvalidArgumentException(String.format("The parent of object with id %s could not be found", oid));
        }
    }
    
    @Override
    public List<BusinessObjectLight> getParents (String objectClassName, long oid)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        
        List<BusinessObjectLight> parents =  new ArrayList<>();
              
        String cypherQuery = "MATCH (n)-[:" + RelTypes.CHILD_OF + "|" + RelTypes.CHILD_OF_SPECIAL + "*]->(m) " +
                             "WHERE id(n) = " + oid + " " +
                            "RETURN m as parents";
      
        try (Transaction tx = graphDb.beginTx()) {
            Result result = graphDb.execute(cypherQuery);
            Iterator<Node> column = result.columnAs("parents");
            for (Node node : Iterators.asIterable(column)){  
                if (node.hasProperty(Constants.PROPERTY_NAME)) {
                    if (node.getProperty(Constants.PROPERTY_NAME).equals(Constants.NODE_DUMMYROOT)) {
                        parents.add(new BusinessObjectLight(Constants.NODE_DUMMYROOT, -1, Constants.NODE_DUMMYROOT));
                        continue;
                    }
                }
                if(node.hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING))
                    parents.add(createObjectLightFromNode(node));
                else //the node has a poolNode as a parent
                    parents.add(Util.createRemoteObjectLightFromPoolNode(node));
            }
        }
        return parents;
    }

    
    @Override
    public List<BusinessObjectLight> getParentsUntilFirstOfClass(String objectClass, 
            long oid, String objectToMatchClassName) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
                        
        List<BusinessObjectLight> parents =  new ArrayList<>();
              
        String cypherQuery = "MATCH (n)-[:" + RelTypes.CHILD_OF + "|" + RelTypes.CHILD_OF_SPECIAL + "*]->(m) " +
                             "WHERE id(n) = " + oid + " " +
                            "RETURN m as parents";
      
        try (Transaction tx = graphDb.beginTx()) {
            Result result = graphDb.execute(cypherQuery);
            Iterator<Node> column = result.columnAs("parents");
            for (Node node : Iterators.asIterable(column)) {
                
                Label label = Label.label(Constants.LABEL_ROOT); //If the parent node is the dummy root, just return null
                if (node.hasLabel(label))
                    break;
                
                if (node.hasProperty(Constants.PROPERTY_NAME)) {
                    if (node.getProperty(Constants.PROPERTY_NAME).equals(Constants.NODE_DUMMYROOT))
                        break;
                }
                
                if(node.hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING)) {                    
                    parents.add(createObjectLightFromNode(node));
                    
                    String parentNodeClass = Util.getClassName(node);
                    if (mem.isSubClass(objectToMatchClassName, parentNodeClass))
                        break;
                }
                else //the node has a poolNode as a parent
                    parents.add(Util.createRemoteObjectLightFromPoolNode(node));
            }
            tx.success();
            return parents;
        }
    }
    
    @Override
    public BusinessObjectLight getFirstParentOfClass(String objectClassName, long oid, String objectToMatchClassName)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(objectClassName, oid);
            while (true) {
                Node parentNode = null;
                if (objectNode.hasRelationship(RelTypes.CHILD_OF, Direction.OUTGOING))
                    parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();
                
                if (objectNode.hasRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING))
                    parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                
                if (parentNode == null)
                    throw new ApplicationObjectNotFoundException(String.format("Navigation tree root not found. Contact your administrator (%s, %s)", objectClassName, oid));
                
                if (parentNode.hasLabel(Label.label(Constants.LABEL_ROOT))) //If the parent node is the dummy root, just return null
                    return null;
                
                else { 
                    String parentNodeClass = Util.getClassName(parentNode);
                    
                    if (mem.isSubClass(objectToMatchClassName, parentNodeClass))
                        return createObjectLightFromNode(parentNode);
                    
                    objectNode = parentNode;
                }
            }
        }
    }
    /**
     * This method was replaced by the method getFirstParentOfClass
     * @throws BusinessObjectNotFoundException 
     * @throws MetadataObjectNotFoundException 
     * @throws InvalidArgumentException
     */
    @Deprecated
    @Override
    public BusinessObject getParentOfClass(String objectClass, long oid, String parentClass) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(objectClass, oid);

            while (true){
                //This method won't support CHILD_OF_SPECIAL relationships
                if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF)){
                    Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();

                    Label label = Label.label(Constants.LABEL_ROOT); //If the parent node is the dummy root, just return null
                    if (parentNode.hasLabel(label) && Constants.NODE_DUMMYROOT.equals(parentNode.getProperty(Constants.PROPERTY_NAME)))
                        return null;
                    else {
                        String thisNodeClass = Util.getClassName(parentNode);
                        if (mem.isSubClass(parentClass, thisNodeClass))
                            return createObjectFromNode(parentNode, mem.getClass(thisNodeClass));
                        objectNode = parentNode;
                        continue;
                    }
                }
                return null;
            }
        }
    }

    @Override
    public void deleteObjects(HashMap<String, List<Long>> objects, boolean releaseRelationships)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {

        try(Transaction tx = graphDb.beginTx()) {
            //TODO: Optimize so it can find all objects of a single class in one query
            for (String className : objects.keySet()){
                for (long oid : objects.get(className)){
                    ClassMetadata classMetadata = Util.createClassMetadataFromNode(graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className));
                    
                    if (!mem.isSubClass(Constants.CLASS_INVENTORYOBJECT, className))
                        throw new OperationNotPermittedException(String.format("Class %s is not a business-related class", className));

                    Node instance = getInstanceOfClass(className, oid);
                    //updates the cache
                    BusinessObject remoteObject = createObjectFromNode(instance);
                    for(AttributeMetadata attribute : classMetadata.getAttributes()){
                        if(attribute.isUnique()){
                            String attributeValues = remoteObject.getAttributes().get(attribute.getName());
                            if(attributeValues != null)
                                CacheManager.getInstance().removeUniqueAttributeValue(className, attribute.getName(), attributeValues);
                        }
                    }
                    Util.deleteObject(instance, releaseRelationships);
                }
            }
            tx.success();
        }
    }

    @Override
    public void deleteObject(String className, long oid, boolean releaseRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException{
        try (Transaction tx = graphDb.beginTx()) {
            if (!mem.isSubClass(Constants.CLASS_INVENTORYOBJECT, className))
                        throw new OperationNotPermittedException(String.format("Class %s is not a business-related class", className));

            Node instance = getInstanceOfClass(className, oid);
            Util.deleteObject(instance, releaseRelationships);
            tx.success();
        }
    }

    @Override
    public ChangeDescriptor updateObject(String className, long oid, HashMap<String, String> attributes)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, 
                InvalidArgumentException {

        ClassMetadata classMetadata= mem.getClass(className);
        
        if (classMetadata == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
        
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(className, oid);

            ChangeDescriptor changes = updateObject(instance, classMetadata, attributes);
            tx.success();
            
            return changes;
        }
    }

    @Override
    public boolean setBinaryAttributes(String className, long oid, List<String> attributeNames, List<byte[]> attributeValues)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, ArraySizeMismatchException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String name, boolean unique)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        
        createSpecialRelationship(aObjectClass, aObjectId, bObjectClass, bObjectId, name, unique, new HashMap<>());
    }
    
    @Override
    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, 
            long bObjectId, String name, boolean unique, HashMap<String, Object> properties) throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        
        if (aObjectId == bObjectId)
            throw new OperationNotPermittedException("An object can not be related with itself");
        
        try(Transaction tx = graphDb.beginTx()) {
            Node nodeA = getInstanceOfClass(aObjectClass, aObjectId);
            for (Relationship rel : nodeA.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                if (rel.getOtherNode(nodeA).getId() == bObjectId 
                        && rel.getProperty(Constants.PROPERTY_NAME).equals(name) && unique)
                    throw new OperationNotPermittedException("These elements are already related");
            }
            Node nodeB = getInstanceOfClass(bObjectClass, bObjectId);
            Relationship rel = nodeA.createRelationshipTo(nodeB, RelTypes.RELATED_TO_SPECIAL);
            rel.setProperty(Constants.PROPERTY_NAME, name);
            
            //Custom properties
            for (String property : properties.keySet())
                rel.setProperty(property, properties.get(property));
            
            tx.success();
        }
    }
    
    @Override
    public void releaseSpecialRelationship(String objectClass, long objectId, long otherObjectId, String name)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node node = getInstanceOfClass(objectClass, objectId);
            for (Relationship rel : node.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                if ((rel.getProperty(Constants.PROPERTY_NAME).equals(name) && 
                        (rel.getOtherNode(node).getId() == otherObjectId) || otherObjectId == -1))
                    rel.delete();
            }
            tx.success();
        }
    }
    
    @Override
    public void releaseSpecialRelationship(String objectClass, long objectId, String relationshipName, long targetId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node node = getInstanceOfClass(objectClass, objectId);
            for (Relationship rel : node.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO_SPECIAL)){
                if (rel.getProperty(Constants.PROPERTY_NAME).equals(relationshipName) &&
                            rel.getEndNode().getId() == targetId)
                    rel.delete();
            }
            tx.success();
        }
    }
    
    @Override
    public void moveObjectsToPool(String targetClassName, long targetOid, HashMap<String, long[]> objects)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);
        
        boolean isPool = true;
        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));
        
        try(Transaction tx = graphDb.beginTx()) {
            Node newParentNode = Util.findNodeByLabelAndId(poolLabel, targetOid);
            
            if(newParentNode == null){
                isPool = false;
                
                newParentNode = Util.findNodeByLabelAndId(inventoryObjectLabel, targetOid);
                
                if(newParentNode == null)
                    throw new BusinessObjectNotFoundException(targetClassName, targetOid);
            }
            
            for (String myClass : objects.keySet()) {
                Node instanceClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (long oid : objects.get(myClass)){
                    Node instance = getInstanceOfClass(instanceClassNode, oid);
                    //If the object was specialChild of a poolNode
                    if (instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().hasNext()){
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    if(isPool)
                        instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
                    else
                        instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL);
                }
            }
            tx.success();
        }
    }

    @Override
    public void moveObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);
        
        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));
        
        try(Transaction tx = graphDb.beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            for (String myClass : objects.keySet()){
                if (!mem.canBeChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be child of an instance of class %s", myClass,targetClassName));

                Node instanceClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (long oid : objects.get(myClass)){
                    Node instance = getInstanceOfClass(instanceClassNode, oid);
                    if (instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().hasNext()){
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    //If the object was specialChild of a poolNode
                    if (instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().hasNext()){
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    
                    instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
                }
            }
            tx.success();
        }
    }
    
    @Override
    public void moveSpecialObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);
        
        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));
        
        try(Transaction tx = graphDb.beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            for (String myClass : objects.keySet()){
                //check if can be special child only if is not a physical connection, 
                //this is to allow moving physical links in and out of the wire containers, without modifying the hierarchy containment
                if(!mem.isSubClass(Constants.CLASS_PHYSICALCONNECTION, myClass)){
                    if (!mem.canBeSpecialChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be special child of an instance of class %s", myClass,targetClassName));
                }
                
                Node instanceClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (long oid : objects.get(myClass)){
                    Node instance = getInstanceOfClass(instanceClassNode, oid);
                    
                    if (instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().hasNext()){
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    
                    if (instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().hasNext()){
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL);
                }
            }
            tx.success();
        }
    }
    
    @Override
    public void movePoolItem(long poolId, String poolItemClassName, long poolItemId) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, 
        MetadataObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node poolNode = Util.findNodeByLabelAndId(poolLabel, poolId);
            
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
            
            if (!poolNode.hasProperty(Constants.PROPERTY_CLASS_NAME))
                throw new InvalidArgumentException("This pool has not set his class name attribute");
            
            Node poolItemClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, poolItemClassName);
            
            if (poolItemClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", poolItemClassName));
            
            if (!mem.isSubClass((String) poolNode.getProperty(Constants.PROPERTY_CLASS_NAME), poolItemClassName))
                throw new InvalidArgumentException(String.format("Class %s is not subclass of %s", poolItemClassName, (String) poolNode.getProperty(Constants.PROPERTY_CLASS_NAME)));
                                    
            Node instance = getInstanceOfClass(poolItemClassNode, poolItemId);
            
            for (Relationship relationship : instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING)) {
                if (relationship.hasProperty(Constants.PROPERTY_NAME)) {
                    if (Constants.REL_PROPERTY_POOL.equals((String) relationship.getProperty(Constants.PROPERTY_NAME)))
                        relationship.delete();
                }
            }
            instance.createRelationshipTo(poolNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
            tx.success();
        }
    }
    
    @Override
    public long[] copyObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects, boolean recursive)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);

        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));

        try (Transaction tx = graphDb.beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            long[] res = new long[objects.size()];
            int i = 0;
            for (String myClass : objects.keySet()){
                if (!mem.canBeChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be child of an instance of class %s", myClass,targetClassName));

                Node instanceClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (long oid : objects.get(myClass)){
                    Node templateObject = getInstanceOfClass(instanceClassNode, oid);
                    Node newInstance = copyObject(templateObject, recursive);
                    newInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
                    res[i] = newInstance.getId();
                    i++;            
                }
            }
            tx.success();
            return res;
        }        
    }
    
    @Override
    public long[] copySpecialObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects, boolean recursive)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);

        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));

        try (Transaction tx = graphDb.beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            long[] res = new long[objects.size()];
            int i = 0;
            for (String myClass : objects.keySet()){
                if (!mem.canBeSpecialChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be special child of an instance of class %s", myClass,targetClassName));

                Node instanceClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (long oid : objects.get(myClass)){
                    Node templateObject = getInstanceOfClass(instanceClassNode, oid);
                    Node newInstance = copyObject(templateObject, recursive);
                    newInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL);
                    res[i] = newInstance.getId();
                    i++;            
                }
            }
            tx.success();
            return res;
        }        
    }
    
    @Override
    public long copyPoolItem(long poolId, String poolItemClassName, long poolItemId, boolean recursive) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, 
        MetadataObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node poolNode = Util.findNodeByLabelAndId(poolLabel, poolId);
            
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
            
            if (!poolNode.hasProperty(Constants.PROPERTY_CLASS_NAME))
                throw new InvalidArgumentException("This pool has not set his class name attribute");
            
            Node poolItemClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, poolItemClassName);
            
            if (poolItemClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", poolItemClassName));
            
            if (!mem.isSubClass((String) poolNode.getProperty(Constants.PROPERTY_CLASS_NAME), poolItemClassName))
                throw new InvalidArgumentException(String.format("Class %s is not subclass of %s", poolItemClassName, (String) poolNode.getProperty(Constants.PROPERTY_CLASS_NAME)));
                                    
            Node instance = getInstanceOfClass(poolItemClassNode, poolItemId);
            
            Node newInstance = copyObject(instance, recursive);
            newInstance.createRelationshipTo(poolNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
            tx.success();
            return newInstance.getId();
        }
    }

    @Override
    public List<BusinessObjectLight> getObjectChildren(String className, long oid, int maxResults)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException  {
        try (Transaction tx =  graphDb.beginTx()) {
            Node parentNode;
            if(oid == -1)
                parentNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else
                parentNode = getInstanceOfClass(className, oid);
            
            Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            Iterator<Relationship> instances = children.iterator();
            List<BusinessObjectLight> res = new ArrayList<>();

            if (maxResults > 0){
                int counter = 0;
                while(children.iterator().hasNext() && (counter < maxResults)){
                    counter++;
                    Node child = children.iterator().next().getStartNode();
                    res.add(createObjectLightFromNode(child));
                }
            }else{
                while(instances.hasNext()){
                    Node child = instances.next().getStartNode();
                    res.add(createObjectLightFromNode(child));
                }
            }
            
            Collections.sort(res);
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectChildren(long classId, long oid, int maxResults)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException  {
        try(Transaction tx = graphDb.beginTx()) {
            Node parentNode;
            if(oid == -1)
                parentNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else
                parentNode = getInstanceOfClass(classId, oid);
            
            Iterable<Relationship> iterableChildren = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            Iterator<Relationship> children = iterableChildren.iterator();
            List<BusinessObjectLight> res = new ArrayList<>();
            if (maxResults > 0){
                int counter = 0;
                while(children.hasNext() && (counter < maxResults)){
                    counter++;
                    Node child = children.next().getStartNode();
                    res.add(createObjectLightFromNode(child));
                }
            }else{
                while(children.hasNext()){
                    Node child = children.next().getStartNode();
                    res.add(createObjectLightFromNode(child));
                }
            }
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSiblings(String className, long oid, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()){
            Node node = getInstanceOfClass(className, oid);
            List<BusinessObjectLight> res = new ArrayList<>();
            
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
                
                res.add(createObjectLightFromNode(child));
            }
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectsOfClassLight(String className, int maxResults)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        try(Transaction tx = graphDb.beginTx()) {
            Node classMetadataNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(className);

            List<BusinessObjectLight> instances = new ArrayList<>();
            int counter = 0;
            
            boolean isAbstract = (Boolean) classMetadataNode.getProperty(Constants.PROPERTY_ABSTRACT);
            
            String cypherQuery;
            
            if (isAbstract) 
                cypherQuery = "match (class:classes)<-[:EXTENDS*]-(subclass:classes)<-[:INSTANCE_OF]-(instance:inventory_objects) "
                            + "where class.name=\"" + className + "\" "
                            + "return instance;";                
            else 
                cypherQuery = "match (class:classes)<-[:INSTANCE_OF]-(instance:inventory_objects) "
                            + "where class.name=\"" + className + "\" "
                            + "return instance;";
            
            Result result = graphDb.execute(cypherQuery);
            ResourceIterator<Node> instanceColumn = result.columnAs("instance");
            List<Node> lstInstanceColumn = Iterators.asList(instanceColumn);
            
            for (Node instance : lstInstanceColumn) {
                if (maxResults > 0){
                    if (counter < maxResults)
                        counter ++;
                    else break;
                }
                instances.add(createObjectLightFromNode(instance));                                                                                
            }
            
            Collections.sort(instances);
            tx.success();
            return instances;
        }
    }
    
    @Override
    public List<BusinessObject> getObjectsOfClass(String className, int maxResults)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
                
        try(Transaction tx = graphDb.beginTx()) {
            Node classMetadataNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(className);
                                                
            List<BusinessObject> instances = new ArrayList<>();
            int counter = 0;
            
            boolean isAbstract = (Boolean) classMetadataNode.getProperty(Constants.PROPERTY_ABSTRACT);
            
            String cypherQuery;
            
            if (isAbstract) {
                cypherQuery = "MATCH (class:classes)<-[:EXTENDS*]-(subclass:classes)<-[:INSTANCE_OF]-(instance:inventory_objects) "
                            + "where class.name=\"" + className + "\" "
                            + "return instance;";                
            } else {
                cypherQuery = "MATCH (class:classes)<-[:INSTANCE_OF]-(instance:inventory_objects) "
                            + "where class.name=\"" + className + "\" "
                            + "return instance;";
            }
            Result result = graphDb.execute(cypherQuery);
            ResourceIterator<Node> instanceColumn = result.columnAs("instance");
            List<Node> lstInstanceColumn = Iterators.asList(instanceColumn);
            
            for (Node instance : lstInstanceColumn) {
                if (maxResults > 0){
                    if (counter < maxResults)
                        counter ++;
                    else break;
                }
                instances.add(createObjectFromNode(instance));                                                                                
            }
            
            Collections.sort(instances);
            tx.success();
            return instances;
        }
    }

    @Override
    public List<BusinessObject> getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = graphDb.beginTx()) {
        
            Node parentNode = getInstanceOfClass(parentClass, parentOid);
            Iterable<Relationship> iterableChildren = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            Iterator<Relationship> children = iterableChildren.iterator();
            List<BusinessObject> res = new ArrayList<>();
            int counter = 0;


            while(children.hasNext()){
                Node child = children.next().getStartNode();

                if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                    throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found",child.getId()));

                Node classNode = child.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode();

                ClassMetadata classMetadata = mem.getClass((String)classNode.getProperty(Constants.PROPERTY_NAME));
                if (mem.isSubClass(classToFilter, classMetadata.getName())){
                    res.add(createObjectFromNode(child, classMetadata));
                    if (maxResults > 0){
                        if (++counter == maxResults)
                            break;
                    }
                }
            }

            if (maxResults > 0 && counter == maxResults)
                return res;

            Iterable<Relationship> iterableSpecialChildren = parentNode.getRelationships(RelTypes.CHILD_OF_SPECIAL,Direction.INCOMING);
            Iterator<Relationship> specialChildren = iterableSpecialChildren.iterator();

            while(specialChildren.hasNext()){
                Node child = specialChildren.next().getStartNode();

                if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                    throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found",child.getId()));

                Node classNode = child.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode();

                ClassMetadata classMetadata = mem.getClass((String)classNode.getProperty(Constants.PROPERTY_NAME));
                if (mem.isSubClass(classToFilter, classMetadata.getName())){
                    res.add(createObjectFromNode(child, classMetadata));
                    if (maxResults > 0 && ++counter == maxResults)
                            break;
                }
            }
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSpecialChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode = getInstanceOfClass(parentClass, parentOid);
            
            List<BusinessObjectLight> res = new ArrayList<>();
            int counter = 0;
            
            for (Relationship specialChildRelationships : parentNode.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING)) {
                BusinessObjectLight specialChild = createObjectLightFromNode(specialChildRelationships.getStartNode());
                
                if (mem.isSubClass(classToFilter, specialChild.getClassName())) {
                    res.add(specialChild);
                    if (maxResults > 0 && ++counter == maxResults)
                        break;
                }
            }
            return res;
        }
    }
    
    
    @Override
    public List<BusinessObjectLight> getChildrenOfClassLightRecursive(long parentOid, String parentClass, String classToFilter, int maxResults) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        
        List<BusinessObjectLight> res = new ArrayList<>();
        try (Transaction tx = graphDb.beginTx()) {
            getChildrenOfClassRecursive(parentOid, parentClass, classToFilter, maxResults, res);
            Collections.sort(res);
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSpecialChildrenOfClassLightRecursive(long parentOid, String parentClass, String classToFilter, int maxResults) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        List<BusinessObjectLight> res = new ArrayList<>();
        try (Transaction tx = graphDb.beginTx()) {
            getSpecialChildrenOfClassRecursive(getInstanceOfClass(parentClass, parentOid), classToFilter, maxResults, res);
            Collections.sort(res);
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException  {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode = getInstanceOfClass(parentClass, parentOid);
            List<BusinessObjectLight> res = new ArrayList<>();

            int counter = 0;

            for (Relationship childOfRelationship : parentNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF, RelTypes.CHILD_OF_SPECIAL)) {
                Node child = childOfRelationship.getStartNode();

                if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                    throw new MetadataObjectNotFoundException(String.format("Class for object with id %s could not be found", child.getId()));

                String className = Util.getClassName(child);
                if (mem.isSubClass(classToFilter, className)) {
                    res.add(createObjectLightFromNode(child));
                    if (maxResults > 0){
                        if (++counter == maxResults)
                            break;
                    }
                }
            }

            return res;
        }
    }

    @Override
    public List<BusinessObjectLight> getSpecialAttribute(String objectClass, long objectId, String specialAttributeName) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(objectClass, objectId);
            List<BusinessObjectLight> res = new ArrayList<>();
            for (Relationship rel : instance.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                if(rel.hasProperty(Constants.PROPERTY_NAME)){
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(specialAttributeName))
                        res.add(rel.getEndNode().getId() == objectId ? 
                            createObjectLightFromNode(rel.getStartNode()) : createObjectLightFromNode(rel.getEndNode()));
                }
            }
            tx.success();
            return res;
        }
    }

    @Override
    public List<AnnotatedBusinessObjectLight> getAnnotatedSpecialAttribute(String objectClass, long objectId, String specialAttributeName) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(objectClass, objectId);
            List<AnnotatedBusinessObjectLight> res = new ArrayList<>();
            for (Relationship rel : instance.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                if(rel.hasProperty(Constants.PROPERTY_NAME)){
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(specialAttributeName)) {
                        BusinessObjectLight theObject = rel.getEndNode().getId() == objectId ? 
                            createObjectLightFromNode(rel.getStartNode()) : createObjectLightFromNode(rel.getEndNode());
                        res.add(new AnnotatedBusinessObjectLight(theObject, rel.getAllProperties()));
                    }
                }
            }
            return res;
        }
    }
    
    @Override
    public HashMap<String,List<BusinessObjectLight>> getSpecialAttributes (String className, long objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException  {
        
        HashMap<String,List<BusinessObjectLight>> res = new HashMap<>();
        try(Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);
            for (Relationship rel : objectNode.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                String relName = (String)rel.getProperty(Constants.PROPERTY_NAME);
                List<BusinessObjectLight> currentObjects = res.get(relName);
                if (currentObjects == null){
                    currentObjects = new ArrayList<>();
                    res.put(relName, currentObjects);
                }
                currentObjects.add(createObjectLightFromNode(rel.getOtherNode(objectNode)));
            }
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectSpecialChildren(String objectClass, long objectId)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException  {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(objectClass, objectId);
            List<BusinessObjectLight> res = new ArrayList<>();
            for (Relationship rel : instance.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)){
                if(rel.hasProperty(Constants.PROPERTY_NAME)){
                    if(rel.getProperty(Constants.PROPERTY_NAME).equals(Constants.REL_PROPERTY_POOL))
                        return res;
                }
                res.add(createObjectLightFromNode(rel.getStartNode()));
            }
            return res;
        }
    }

    @Override
    public boolean hasRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
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
    }
    
    @Override
    public void releaseRelationships(String objectClass, long objectId, List<String> relationshipsToRelease) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        try(Transaction tx = graphDb.beginTx()) {
            Node object = getInstanceOfClass(objectClass, objectId);
            
            for (Relationship rel : object.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                if (relationshipsToRelease.contains((String)rel.getProperty(Constants.PROPERTY_NAME)))
                    rel.delete();
            }
            tx.success();
        }
    }
    
    @Override
    public boolean hasSpecialRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException  {
        try (Transaction tx = graphDb.beginTx()) {
            Node object = getInstanceOfClass(objectClass, objectId);
            int relationshipsCounter = 0;
            for (Relationship rel : object.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                if (rel.getProperty(Constants.PROPERTY_NAME).equals(relationshipName))
                    relationshipsCounter++;
                if (relationshipsCounter == numberOfRelationships)
                    return true;
            }
        }
        return false;
            
    }
    
    //<editor-fold desc="Contact Manager" defaultstate="collapsed">
    @Override
    public long createContact(String contactClass, List<StringPair> properties, String customerClassName, long customerId) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        
        if (!mem.isSubClass("GenericCustomer", customerClassName)) //NOI18N
            throw new InvalidArgumentException(String.format("Class %s is not a subclass of GenericCustomer", customerClassName));
        
        try (Transaction tx = graphDb.beginTx()) {
            
            Node contactClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, contactClass);
            if (contactClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", contactClass));
            
            Node customerNode = getInstanceOfClass(customerClassName, customerId);
            Node newContactNode = graphDb.createNode(Label.label(Constants.LABEL_CONTACTS), Label.label(Constants.LABEL_INVENTORY_OBJECT));
            
            boolean hasName = false;
            
            ClassMetadata classMetadata = mem.getClass(contactClass);
            
            HashMap<String, String> convertedProperties = new HashMap<>();
            for (StringPair property : properties) {
                convertedProperties.put(property.getKey(), property.getValue());
                if (property.getKey().equals(Constants.PROPERTY_NAME))
                    hasName = true;
            }
            
            updateObject(newContactNode, classMetadata, convertedProperties);
            
            if (!hasName)
                throw new InvalidArgumentException("A contact must have a name");
            
            newContactNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            
            Relationship newContactRelationship = customerNode.createRelationshipTo(newContactNode, RelTypes.RELATED_TO_SPECIAL);
            newContactRelationship.setProperty("name", "contacts");
            
            newContactNode.createRelationshipTo(contactClassNode, RelTypes.INSTANCE_OF);
            
            tx.success();
            
            return newContactNode.getId();
        }
    }

    @Override
    public void updateContact(String contactClass, long contactId, List<StringPair> properties) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node contactNode = getInstanceOfClass(contactClass, contactId);
            
            for (StringPair property : properties) {
                if (property.getKey().equals(Constants.PROPERTY_CREATION_DATE))
                    throw new InvalidArgumentException("The attribute creationDate is read-only and can not be modified");
                
                contactNode.setProperty(property.getKey(), property.getValue());
            }
            
            tx.success();            
        }
    }

    @Override
    public void deleteContact(String contactClass, long contactId) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node contactNode = getInstanceOfClass(contactClass, contactId);
            
            for (Relationship contactRelationship : contactNode.getRelationships())
                contactRelationship.delete();
            
            contactNode.delete();
            
            tx.success();            
        }
    }

    @Override
    public Contact getContact(String contactClass, long contactId) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (!mem.isSubClass("GenericContact", contactClass))
            throw new InvalidArgumentException(String.format("Class %s is not an instance of GenericContact", contactClass));
        
        try (Transaction tx = graphDb.beginTx()) {
            Node contactNode = getInstanceOfClass(contactClass, contactId);
            
            for (Relationship customerRelationship : contactNode.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.INCOMING)) {
                if (customerRelationship.hasProperty(Constants.PROPERTY_NAME) && customerRelationship.getProperty(Constants.PROPERTY_NAME).equals("contacts"))
                    return new Contact(createObjectFromNode(contactNode), createObjectLightFromNode(customerRelationship.getStartNode()));
            }
            
            throw new InvalidArgumentException("The contact does not have a customer associated to it, please check its relationships");
        }
    }

    @Override
    public List<Contact> getContactsForCustomer(String customerClass, long customerId) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node customerNode = getInstanceOfClass(customerClass, customerId);
            List<Contact> contacts = new ArrayList<>();
            for (Relationship contactRelationship : customerNode.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.OUTGOING)) {
                if (contactRelationship.hasProperty(Constants.PROPERTY_NAME) && contactRelationship.getProperty(Constants.PROPERTY_NAME).equals("contacts")) {
                    Node contactNode = contactRelationship.getEndNode();
                    contacts.add(new Contact(createObjectFromNode(contactNode), createObjectLightFromNode(customerNode)));
                }
            }
            
            return contacts;
        }
    }

    //TODO: Optimize and improve validations!
    @Override
    public List<Contact> searchForContacts(String searchString, int maxResults) throws InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            String cypherQuery;
            if (searchString == null || searchString.trim().isEmpty()) //Return all contacts
                cypherQuery = "MATCH(n:contacts)  RETURN n AS contact ORDER BY n.name ASC" +
                        (maxResults > 0 ? (" LIMIT " + maxResults) : ""); //NOI18N
            else //Search the string in the contact and customer name
                cypherQuery = "MATCH (n:contacts)<-[r:" + RelTypes.RELATED_TO_SPECIAL + "]-(c) WHERE " +
                        "TOLOWER(n.name) contains TOLOWER({searchString}) OR TOLOWER(c.name) contains TOLOWER({searchString}) RETURN n AS contact ORDER BY c.name, n.name ASC" +
                        (maxResults > 0 ? (" LIMIT " + maxResults) : ""); //NOI18N
            
            List<Contact> res = new ArrayList<>();
            
            HashMap<String, Object> parameters = new HashMap();
            parameters.put("searchString", searchString);  //NOI18N
            Result rawQueryResult = graphDb.execute(cypherQuery, parameters);
            
            ResourceIterator<Node> contactNodes = rawQueryResult.columnAs("contact");
            
            while (contactNodes.hasNext()) {
                Node contactNode = contactNodes.next();
                Node customerNode = contactNode.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.INCOMING).iterator().next().getStartNode();
                
                res.add(new Contact(createObjectFromNode(contactNode), createObjectLightFromNode(customerNode)));
            }
            
            return res;
        }
    }

    @Override
    public long attachFileToObject(String name, String tags, byte[] file, String className, long objectId) 
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (file.length > (float)configuration.get("maxAttachmentSize") * 1048576) //Size converted to MB
            throw new InvalidArgumentException(String.format("The file size exceeds the maximum size allowed (%s MB)", configuration.get("maxAttachmentSize")));
        
        if (name == null || name.trim().isEmpty())
            throw new InvalidArgumentException("The file name can not be an empty string");
        
        try (Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);
            
            Node fileObjectNode = graphDb.createNode(Label.label(Constants.LABEL_ATTACHMENTS));
            fileObjectNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            fileObjectNode.setProperty(Constants.PROPERTY_NAME, name);
            fileObjectNode.setProperty(Constants.PROPERTY_TAGS, tags == null ? "" : tags);
            
            Relationship hasAttachmentRelationship = objectNode.createRelationshipTo(fileObjectNode, RelTypes.HAS_ATTACHMENT);
            hasAttachmentRelationship.setProperty(Constants.PROPERTY_NAME, "attachments");
            
            String fileName = objectNode.getId() + "_" + fileObjectNode.getId();
                    Util.saveFile((String)configuration.get("attachmentsPath"), fileName, file);
            
            tx.success();
            return fileObjectNode.getId();
        } catch(IOException ex) {
            throw new OperationNotPermittedException(ex.getMessage());
        }
    }

    @Override
    public List<FileObjectLight> getFilesForObject(String className, long objectId) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);
            List<FileObjectLight> res = new ArrayList<>();
            
            for (Relationship fileObjectRelationship : objectNode.getRelationships(RelTypes.HAS_ATTACHMENT, Direction.OUTGOING)) {
                Node fileObjectNode = fileObjectRelationship.getEndNode();
                res.add(new FileObjectLight(fileObjectNode.getId(), (String)fileObjectNode.getProperty(Constants.PROPERTY_NAME), 
                        (String)fileObjectNode.getProperty(Constants.PROPERTY_TAGS), (long)fileObjectNode.getProperty(Constants.PROPERTY_CREATION_DATE)));
            }
            
            return res;
        }
    }

    @Override
    public FileObject getFile(long fileObjectId, String className, long objectId) throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);

            for (Relationship fileObjectRelationship : objectNode.getRelationships(RelTypes.HAS_ATTACHMENT, Direction.OUTGOING)) {
                if (fileObjectRelationship.getEndNode().getId() == fileObjectId) {
                    String fileName = objectNode.getId() + "_" + fileObjectId;
                    try {
                        byte[] background = Util.readBytesFromFile(configuration.getProperty("attachmentsPath", DEFAULT_ATTACHMENTS_PATH) + "/" + fileName);
                        return new FileObject(fileObjectId, (String)fileObjectRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME), 
                                (String)fileObjectRelationship.getEndNode().getProperty(Constants.PROPERTY_TAGS), 
                                (long)fileObjectRelationship.getEndNode().getProperty(Constants.PROPERTY_CREATION_DATE), 
                                background);
                    }catch(IOException ex){
                        throw new InvalidArgumentException(String.format("File with id %s could not be retrieved: %s", fileObjectId, ex.getMessage()));
                    }
                }
            }
            
            throw new InvalidArgumentException(String.format("The file with id %s could not be found", fileObjectId));
        }
    }

    @Override
    public void detachFileFromObject(long fileObjectId, String className, long objectId) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);

            for (Relationship fileObjectRelationship : objectNode.getRelationships(RelTypes.HAS_ATTACHMENT, Direction.OUTGOING)) {
                if (fileObjectRelationship.getEndNode().getId() == fileObjectId) {
                    fileObjectRelationship.delete();
                    fileObjectRelationship.getEndNode().delete();
                    
                    try {
                        String fileName = objectNode.getId() + "_" + fileObjectId;
                        new File(configuration.getProperty("attachmentsPath", DEFAULT_ATTACHMENTS_PATH) + "/" + fileName).delete();
                    }catch(Exception ex){
                        throw new InvalidArgumentException(String.format("File with id %s could not be retrieved: %s", fileObjectId, ex.getMessage()));
                    }
                    tx.success();
                    return;
                }
            }
            
            throw new InvalidArgumentException(String.format("The file with id %s could not be found", fileObjectId));
        }
    }
    
    @Override
    public void updateFileProperties(long fileObjectId, List<StringPair> properties, String className, long objectId) throws BusinessObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);

            for (Relationship fileObjectRelationship : objectNode.getRelationships(RelTypes.HAS_ATTACHMENT, Direction.OUTGOING)) {
                if (fileObjectRelationship.getEndNode().getId() == fileObjectId) {
                    for (StringPair property : properties) {
                        switch (property.getKey()) {
                            case Constants.PROPERTY_NAME:
                                if (property.getValue().trim().isEmpty())
                                    throw new InvalidArgumentException("The file name can not be an empty string");
                                
                                fileObjectRelationship.getEndNode().setProperty(Constants.PROPERTY_NAME, property.getValue());
                                break;
                            case Constants.PROPERTY_TAGS:
                                fileObjectRelationship.getEndNode().setProperty(Constants.PROPERTY_TAGS, property.getValue());
                                break;
                            default:
                                throw new InvalidArgumentException(String.format("The property %s is not valid", property.getKey()));
                        }
                    }
                    tx.success();
                    return;
                }
            }
            
            throw new InvalidArgumentException(String.format("The file with id %s could not be found", fileObjectId));
        }
    }
    
    //TODO DELETE. This is a business dependant method, should not be here. Don't use it
    @Override
    public List<BusinessObjectLight> getPhysicalPath(String objectClass, long objectId) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, ApplicationObjectNotFoundException {
        List<BusinessObjectLight> path = new ArrayList<>();
        //If the port is a logical port (virtual port, Pseudowire or service instance, we look for the first physcal parent port)
        long logicalPortId = 0;
        if(mem.isSubClass(Constants.CLASS_GENERICLOGICALPORT, objectClass)){
            logicalPortId = objectId;
            //This should be deleted after the MPLS synchronization has been finished
            if(objectClass.equals("Pseudowire"))
                objectId = getFirstParentOfClass(objectClass, objectId, Constants.CLASS_GENERICCOMMUNICATIONSELEMENT).getId();
            else{    
                BusinessObjectLight firstPhysicalParentPort = getFirstParentOfClass(objectClass, objectId, Constants.CLASS_GENERICPHYSICALPORT);
                objectId = firstPhysicalParentPort.getId();
            }
        }
        //The first part of the query will return many paths, the longest is the one we need. The others are
        //subsets of the longest
        String cypherQuery = "MATCH paths = (o)-[r:" + RelTypes.RELATED_TO_SPECIAL + "*]-(c) "+
                             "WHERE id(o) = " + objectId + " AND all(rel in r where rel.name = 'mirror' or rel.name = 'endpointA' or rel.name = 'endpointB') "+
                             "WITH nodes(paths) as path " +
                             "RETURN path ORDER BY length(path) DESC LIMIT 1";
        try (Transaction tx = graphDb.beginTx()){
            if(logicalPortId > 0)
                path.add(createObjectLightFromNode(Util.findNodeByLabelAndId(inventoryObjectLabel, logicalPortId)));
            Result result = graphDb.execute(cypherQuery);
            Iterator<List<Node>> column = result.columnAs("path");
            
            for (List<Node> listOfNodes : Iterators.asIterable(column)) {
                for(Node node : listOfNodes)
                    path.add(createObjectLightFromNode(node));
            }
        }
        return path;

    }
    
    @Override
    public BusinessObject getLinkConnectedToPort(String portClassName, long portId) throws MetadataObjectNotFoundException, 
            BusinessObjectNotFoundException, InvalidArgumentException {
        
        if (!mem.isSubClass(Constants.CLASS_GENERICPORT, portClassName))
            throw new InvalidArgumentException(String.format("Class %s is not a subclass of GenericPort", portClassName));
        
        try (Transaction tx = graphDb.beginTx()) {
            
            Node portNode = getInstanceOfClass(portClassName, portId);
            
            for (Relationship relatedToSpecialRelationship : portNode.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                if (relatedToSpecialRelationship.getProperty(Constants.PROPERTY_NAME).equals("endpointA")  //NOI18N
                        || relatedToSpecialRelationship.getProperty(Constants.PROPERTY_NAME).equals("endpointB")) //NOI18N
                    return createObjectFromNode(relatedToSpecialRelationship.getStartNode()); //A port should have only one aEndpoint || bEndpoint relationship
            }
        }
        
        return null; //If the port does not have any connections attached to it
    }

    @Override
    public List<BusinessObjectLightList> findRoutesThroughSpecialRelationships(String objectAClassName, 
            long objectAId, String objectBClassName, long objectBId, String relationshipName) {
        List<BusinessObjectLightList> paths = new ArrayList<>();

        String cypherQuery = String.format("MATCH path = (a)-[:%s*1..30{name:\"%s\"}]-(b) " +
                            "WHERE id(a) = %s AND id(b) = %s " +
                            "RETURN nodes(path) as path LIMIT %s", RelTypes.RELATED_TO_SPECIAL, relationshipName, objectAId, objectBId, 
                                                                    aem.getConfiguration().get("maxRoutes")); //NOI18N
        try (Transaction tx = graphDb.beginTx()) {
           
            Result result = graphDb.execute(cypherQuery);
            Iterator<List<Node>> column = result.columnAs("path");
            
            //Filtering the routes with repeated nodes didn't work using a cypher query, so we do it here
            //at least while we figure out a working cypher query
            for (List<Node> list : Iterators.asIterable(column)) {
                BusinessObjectLightList aPath = new BusinessObjectLightList();
                boolean discardPath = false;
                for (Node aNode : list) {
                    BusinessObjectLight aHop = createObjectLightFromNode(aNode);
                    if (aPath.getList().contains(aHop)) {
                        discardPath = true;
                        break;
                    } else
                        aPath.add(aHop);
                }
                if (!discardPath)
                    paths.add(aPath);
            }
        }
        
        //We implement the path length sorting here since the cypher query seems to be too expensive (?) if the sort is done there
        paths.sort((o1, o2) -> {
            return Integer.compare(o1.getList().size(), o2.getList().size());
        });
        
        return paths;
        
    }
    
    //<editor-fold desc="Warehouse" defaultstate="collapsed">
    @Override
    public List<BusinessObjectLight> getWarehousesInObject(String objectClassName, long objectId) throws MetadataObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, objectClassName);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(objectClassName);
            
            List<BusinessObjectLight> warehouses = new ArrayList();
                                    
            String cypherQuery = ""
                + "match (warehouse:inventory_objects)-[:RELATED_TO_SPECIAL{ name: 'warehouseHas' }]-(child:inventory_objects)-[:CHILD_OF*]->(parent:inventory_objects)-[:INSTANCE_OF]->(class:classes{name: '" + objectClassName + "'}) "
                + "where id(parent) = " + objectId + " return warehouse;";
            
            Result result = graphDb.execute(cypherQuery);
            ResourceIterator<Node> warehouseColumn = result.columnAs("warehouse");
            List<Node> lstWarehouseColumn = Iterators.asList(warehouseColumn);
            
            for (Node warehouse : lstWarehouseColumn)
                warehouses.add(createObjectLightFromNode(warehouse));
            
            Collections.sort(warehouses);
            
            tx.success();
            return warehouses;
        }
    }
    @Override
    public BusinessObjectLight getWarehouseToObject(String objectClassName, long objectId) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, objectClassName);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(objectClassName);
            
            getObjectLight(objectClassName, objectId);
            
            List<BusinessObjectLight> warehouses = new ArrayList();
                                                
            String cypherQuery = ""
                + "MATCH (class{name:'" + objectClassName + "'})<-[:INSTANCE_OF]-(inventoryObject)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(pool)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(warehouse) "
                + "WHERE id(inventoryObject) = " + objectId + " "
                + "RETURN warehouse;";
            
            Result result = graphDb.execute(cypherQuery);
            ResourceIterator<Node> warehouseColumn = result.columnAs("warehouse");
            List<Node> lstWarehouseColumn = Iterators.asList(warehouseColumn);
            
            for (Node warehouse : lstWarehouseColumn)
                warehouses.add(createObjectLightFromNode(warehouse));
            
            Collections.sort(warehouses);
            
            tx.success();
            if (warehouses.size() > 0)
                return warehouses.get(0);
            return null;
        }
    }
    
    @Override
    public BusinessObjectLight getPhysicalNodeToObjectInWarehouse(String objectClassName, long objectId) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, objectClassName);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(objectClassName);
            
            getObjectLight(objectClassName, objectId);
            
            List<BusinessObjectLight> physicalNodes = new ArrayList();
                                    
            String cypherQuery = ""
                + "MATCH (class{name:'" + objectClassName + "'})<-[:INSTANCE_OF]-(inventoryObject)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(pool)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(warehouse)-[:RELATED_TO_SPECIAL{name: 'warehouseHas'}]->(physicalNode) "
                + "WHERE id(inventoryObject) = " + objectId + " "
                + "RETURN physicalNode;";
            
            Result result = graphDb.execute(cypherQuery);
            ResourceIterator<Node> physicalNodeColumn = result.columnAs("physicalNode");
            List<Node> lstphysicalNodeColumn = Iterators.asList(physicalNodeColumn);
            
            for (Node physicalNode : lstphysicalNodeColumn)
                physicalNodes.add(createObjectLightFromNode(physicalNode));
            
            Collections.sort(physicalNodes);
            
            tx.success();
            if (physicalNodes.size() > 0)
                return physicalNodes.get(0);
            return null;
        }
    }
    //</editor-fold>
        
    //<editor-fold desc="Reporting API implementation" defaultstate="collapsed">
        @Override
    public long createClassLevelReport(String className, String reportName, String reportDescription, 
            String script, int outputType, boolean enabled) throws MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            Node newReport = graphDb.createNode(reportsLabel);
            newReport.setProperty(Constants.PROPERTY_NAME, reportName == null ? "" : reportName);
            newReport.setProperty(Constants.PROPERTY_DESCRIPTION, reportDescription == null ? "" : reportDescription);
            newReport.setProperty(Constants.PROPERTY_SCRIPT, script == null ? "" : script);
            newReport.setProperty(Constants.PROPERTY_TYPE, Math.abs(outputType) > 4 ? RemoteReportLight.TYPE_HTML : outputType);
            newReport.setProperty(Constants.PROPERTY_ENABLED, enabled);
            
            classNode.createRelationshipTo(newReport, RelTypes.HAS_REPORT);
                        
            tx.success();
            return newReport.getId();
        }
    }

    @Override
    public long createInventoryLevelReport(String reportName, String reportDescription, String script, 
            int outputType, boolean enabled, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node dummyRootNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            if (dummyRootNode == null)
                throw new ApplicationObjectNotFoundException("Dummy Root could not be found");
            
            Node newReport = graphDb.createNode(reportsLabel);
            newReport.setProperty(Constants.PROPERTY_NAME, reportName == null ? "" : reportName);
            newReport.setProperty(Constants.PROPERTY_DESCRIPTION, reportDescription == null ? "" : reportDescription);
            newReport.setProperty(Constants.PROPERTY_SCRIPT, script == null ? "" : script);
            newReport.setProperty(Constants.PROPERTY_TYPE, Math.abs(outputType) > 4 ? RemoteReportLight.TYPE_HTML : outputType);
            newReport.setProperty(Constants.PROPERTY_ENABLED, enabled);
            
            if (parameters != null) {
                for (StringPair parameter : parameters) {
                    if (parameter.getKey() == null || parameter.getKey().trim().isEmpty())
                        throw new InvalidArgumentException("Parameter names can not be empty strings");
                    
                    newReport.setProperty("PARAM_" + parameter.getKey(), 
                            parameter.getValue() == null ? "" : parameter.getValue());
                }
            }
            
            
            dummyRootNode.createRelationshipTo(newReport, RelTypes.HAS_REPORT);
                        
            tx.success();
            return newReport.getId();
        }
    }

    @Override
    public ChangeDescriptor deleteReport(long reportId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId));
            
            for (Relationship rel : reportNode.getRelationships())
                rel.delete();
            
            String reportName = reportNode.hasProperty(Constants.PROPERTY_NAME) ? (String) reportNode.getProperty(Constants.PROPERTY_NAME) : "";
            
            reportNode.delete();
            
            tx.success();
            return new ChangeDescriptor("","","", String.format("Deleted report %s", reportName));
        }
    }

    @Override
    public ChangeDescriptor updateReport(long reportId, String reportName, String reportDescription, Boolean enabled,
            Integer type, String script) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId));
            String affectedProperties = "", oldValues = "", newValues = "";
            
            if (reportName != null) {
                affectedProperties += " " + Constants.PROPERTY_NAME;
                oldValues += " " + (reportNode.hasProperty(Constants.PROPERTY_NAME) ? reportNode.getProperty(Constants.PROPERTY_NAME) : "null");                
                newValues += " " + reportName;
                
                reportNode.setProperty(Constants.PROPERTY_NAME, reportName);
            }
            else
                reportName = reportNode.hasProperty(Constants.PROPERTY_NAME) ? (String) reportNode.getProperty(Constants.PROPERTY_NAME) : "";
            
            if (reportDescription != null) {
                affectedProperties += " " + Constants.PROPERTY_DESCRIPTION;
                oldValues += " " + (reportNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? reportNode.getProperty(Constants.PROPERTY_DESCRIPTION) : "null");
                newValues += " " + reportDescription;
                
                reportNode.setProperty(Constants.PROPERTY_DESCRIPTION, reportDescription);
            }
            if (enabled != null) {
                affectedProperties += " " + Constants.PROPERTY_ENABLED;
                oldValues += " " + (reportNode.hasProperty(Constants.PROPERTY_ENABLED) ? reportNode.getProperty(Constants.PROPERTY_ENABLED) : "null");
                newValues += " " + enabled;
                
                reportNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            }
            if (type != null) {
                affectedProperties += " " + Constants.PROPERTY_TYPE;
                oldValues += " " + (reportNode.hasProperty(Constants.PROPERTY_TYPE) ? reportNode.getProperty(Constants.PROPERTY_TYPE) : "null");
                newValues += " " + type;
                
                reportNode.setProperty(Constants.PROPERTY_TYPE, type);
            }
            if (script != null) {
                affectedProperties += " " + Constants.PROPERTY_SCRIPT;
                oldValues += " " + (reportNode.hasProperty(Constants.PROPERTY_SCRIPT) ? reportNode.getProperty(Constants.PROPERTY_SCRIPT) : "null");
                newValues += " " + script;
                
                reportNode.setProperty(Constants.PROPERTY_SCRIPT, script);
            }

            tx.success();
            return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), 
                newValues.trim(), String.format("Updated Report %s", reportName));
        }
    }
    
    @Override
    public ChangeDescriptor updateReportParameters(long reportId, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A report with id %s could not be found", reportId));
            String affectedProperties = "", oldValues = "", newValues = "";
            
            for (StringPair parameter : parameters) {
                
                if (parameter.getKey() == null || parameter.getKey().trim().isEmpty())
                        throw new InvalidArgumentException("Parameter names can not be empty strings");
                
                String actualParameterName = "PARAM_" + parameter.getKey();
                //The parameters are stored with a prefix PARAM_
                //params set to null, must be deleted
                if (reportNode.hasProperty(actualParameterName) && parameter.getValue() == null) {
                    affectedProperties += " " + parameter.getKey();
                    reportNode.removeProperty(actualParameterName);
                }
                else {
                    affectedProperties += " " + parameter.getKey();
                    oldValues += " " + (reportNode.hasProperty(actualParameterName) ? reportNode.getProperty(actualParameterName) : "null");
                    newValues += " " + parameter.getValue();
                    
                    reportNode.setProperty(actualParameterName, parameter.getValue());
                }
            }
            String reportName = reportNode.hasProperty(Constants.PROPERTY_NAME) ? (String) reportNode.getProperty(Constants.PROPERTY_NAME) : "";
            tx.success();
            return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), 
                newValues.trim(), String.format("Updated %s report parameters", reportName));
        }
    }

    @Override
    public List<RemoteReportLight> getClassLevelReports(String className, boolean recursive, boolean includeDisabled) throws MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node mainClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, className);
            List<RemoteReportLight> remoteReports = new ArrayList<>();
            
            if (mainClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            if (recursive) {
                Node classNode = mainClassNode;
                do {
                    for (Relationship hasReportRelationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_REPORT)) 
                        remoteReports.add(new RemoteReportLight(hasReportRelationship.getEndNode().getId(), 
                                                            (String)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME), 
                                                            (String)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_DESCRIPTION), 
                                                            (boolean)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_ENABLED),
                                                            (int)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_TYPE)));
                    
                    if (classNode.hasRelationship(RelTypes.EXTENDS, Direction.OUTGOING)) //This should not happen, but let's check it anyway
                        classNode = classNode.getSingleRelationship(RelTypes.EXTENDS, Direction.OUTGOING).getEndNode();
                    else
                        classNode = null;
                } while (classNode != null && !Constants.CLASS_ROOTOBJECT.equals(classNode.getProperty(Constants.PROPERTY_NAME)));
            }
            else {
                for (Relationship hasReportRelationship : mainClassNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_REPORT)) 
                    remoteReports.add(new RemoteReportLight(hasReportRelationship.getEndNode().getId(), 
                                                        (String)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME), 
                                                        (String)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_DESCRIPTION), 
                                                        (boolean)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_ENABLED),
                                                        (int)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_TYPE)));
            }
            
            return remoteReports;
        }
    }

    @Override
    public List<RemoteReportLight> getInventoryLevelReports(boolean includeDisabled) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node dummyRootNode = graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            if (dummyRootNode == null)
                throw new ApplicationObjectNotFoundException("Dummy Root could not be found");
            
            List<RemoteReportLight> remoteReports = new ArrayList<>();
            
            for (Relationship hasReportRelationship : dummyRootNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_REPORT)) {
                if (includeDisabled || (boolean)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_ENABLED))
                    remoteReports.add(new RemoteReportLight(hasReportRelationship.getEndNode().getId(), 
                                                        (String)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME), 
                                                        (String)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_DESCRIPTION), 
                                                        (boolean)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_ENABLED),
                                                        (int)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_TYPE)));
            }
            
            Collections.sort(remoteReports);
            
            return remoteReports;
        }
    }

    @Override
    public RemoteReport getReport(long reportId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId)); 
            
            List<StringPair> parameters = new ArrayList<>();
            for (String property : reportNode.getPropertyKeys()) {
                if (property.startsWith("PARAM_"))
                    parameters.add(new StringPair(property.replace("PARAM_", ""), (String)reportNode.getProperty(property)));
            }
                
            return new RemoteReport(reportNode.getId(), (String)reportNode.getProperty(Constants.PROPERTY_NAME), 
                                    (String)reportNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                                    (boolean)reportNode.getProperty(Constants.PROPERTY_ENABLED),
                                    (int)reportNode.getProperty(Constants.PROPERTY_TYPE),
                                    (String)reportNode.getProperty(Constants.PROPERTY_SCRIPT), 
                                    parameters);
        }
    }

    @Override
    public byte[] executeClassLevelReport(String objectClassName, long objectId, long reportId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId)); 
            
            Node instanceNode = getInstanceOfClass(objectClassName, objectId);
            
            String script = (String)reportNode.getProperty(Constants.PROPERTY_SCRIPT);
            
            Binding environmentParameters = new Binding();
            environmentParameters.setVariable("instanceNode", instanceNode); //NOI18N
            environmentParameters.setVariable("graphDb", graphDb); //NOI18N
            environmentParameters.setVariable("inventoryObjectLabel", inventoryObjectLabel);            
            environmentParameters.setVariable("classLabel", classLabel); //NOI18N
            environmentParameters.setVariable("defaultReports", defaultReports); //NOI18N
            
            environmentParameters.setVariable("sceneExporter", SceneExporter.getInstance(/*this, mem*/));
            
            //To keep backwards compatibility
            environmentParameters.setVariable("objectClassName", objectClassName); //NOI18N
            environmentParameters.setVariable("objectId", objectId); //NOI18N
            
            try {
                GroovyShell shell = new GroovyShell(BusinessEntityManager.class.getClassLoader(), environmentParameters);
                Object theResult = shell.evaluate(script);
                
                if (theResult == null)
                    throw new InvalidArgumentException("The script returned a null object. Please check the syntax.");
                else {
                    if (theResult instanceof InventoryReport)
                        return ((InventoryReport)theResult).asByteArray();
                    else
                        throw new InvalidArgumentException("The script does not return an InventoryReport object. Please check the return value.");
                }
            } catch(Exception ex) {
                return ("<html><head><title>Error</title></head><body><center>" + ex.getMessage() + "</center></body></html>").getBytes(StandardCharsets.UTF_8);
            }
        }
    }

    @Override
    public byte[] executeInventoryLevelReport(long reportId, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId)); 
                     
            String script = (String)reportNode.getProperty(Constants.PROPERTY_SCRIPT);
            
            HashMap<String, String> scriptParameters = new HashMap<>();
            for(StringPair parameter : parameters)
                scriptParameters.put(parameter.getKey(), parameter.getValue());
            
            Binding environmentParameters = new Binding();
            environmentParameters.setVariable("parameters", scriptParameters); //NOI18N
            environmentParameters.setVariable("graphDb", graphDb); //NOI18N
            environmentParameters.setVariable("inventoryObjectLabel", inventoryObjectLabel); //NOI18N
            environmentParameters.setVariable("classLabel", classLabel); //NOI18N
            environmentParameters.setVariable("defaultReports", defaultReports); //NOI18N
            
            try {
                GroovyShell shell = new GroovyShell(BusinessEntityManager.class.getClassLoader(), environmentParameters);
                Object theResult = shell.evaluate(script);
                
                if (theResult == null)
                    throw new InvalidArgumentException("The script returned a null object. Please check the syntax.");
                else {
                    if (theResult instanceof InventoryReport)
                        return ((InventoryReport)theResult).asByteArray();
                    else
                        throw new InvalidArgumentException("The script does not return an InventoryReport object. Please check the return value.");
                }
            } catch(Exception ex) {
                return ("<html><head><title>Error</title></head><body><center>" + ex.getMessage() + "</center></body></html>").getBytes(StandardCharsets.UTF_8);
            }
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="Pools" defaultstate="collapsed">
        @Override
    public List<Pool> getRootPools(String className, int type, boolean includeSubclasses) {
        try(Transaction tx = graphDb.beginTx()) {
            List<Pool> pools  = new ArrayList<>();
            
            ResourceIterator<Node> poolNodes = graphDb.findNodes(poolLabel);
            
            while (poolNodes.hasNext()) {
                Node poolNode = poolNodes.next();
                
                if (!poolNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF_SPECIAL)) { //Root pools don't have parents
                    if ((int)poolNode.getProperty(Constants.PROPERTY_TYPE) == type) {
                        
                        //The following conditions could probably normalized, but I think this way,
                        //the code is a bit more readable
                        if (className != null) { //We will return only those matching with the specified class name or its subclasses, depending on the value of includeSubclasses
                            String poolClass = (String)poolNode.getProperty(Constants.PROPERTY_CLASS_NAME);
                            if (includeSubclasses) {
                                try {
                                    if (mem.isSubClass(className, poolClass))
                                        pools.add(Util.createPoolFromNode(poolNode));
                                } catch (MetadataObjectNotFoundException ex) { } //Should not happen
                            } else {
                                if (className.equals(poolClass))
                                    pools.add(Util.createPoolFromNode(poolNode));
                            }
                        } else //All pools with no parent are returned
                            pools.add(Util.createPoolFromNode(poolNode));
                    }
                }
            }
            tx.success();
            return pools;
        }
    }
    
    @Override
    public List<Pool> getPoolsInObject(String objectClassName, long objectId, String poolClass) throws BusinessObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            List<Pool> pools  = new ArrayList<>();
            
            Node objectNode = Util.findNodeByLabelAndId(inventoryObjectLabel, objectId);
            
            if (objectNode == null)
                throw new BusinessObjectNotFoundException(objectClassName, objectId);
            
            for (Relationship containmentRelationship : objectNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                if (containmentRelationship.hasProperty(Constants.PROPERTY_NAME) && 
                        Constants.REL_PROPERTY_POOL.equals(containmentRelationship.getProperty(Constants.PROPERTY_NAME))){
                    Node poolNode = containmentRelationship.getStartNode();
                    if (poolClass != null) { //We will return only those matching with the specified class name
                        if (poolClass.equals((String)poolNode.getProperty(Constants.PROPERTY_CLASS_NAME)))
                            pools.add(Util.createPoolFromNode(poolNode));
                    } else
                        pools.add(Util.createPoolFromNode(poolNode));
                }
            }
            tx.success();
            return pools;
        }
    }
    
    @Override
    public List<Pool> getPoolsInPool(long parentPoolId, String poolClass) 
            throws ApplicationObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            List<Pool> pools  = new ArrayList<>();
            
            Node parentPoolNode = Util.findNodeByLabelAndId(poolLabel, parentPoolId);
            
            if (parentPoolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The pool with id %s could not be found", parentPoolId));
            
            for (Relationship containmentRelationship : parentPoolNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                Node poolNode = containmentRelationship.getStartNode();
                
                if (poolNode.hasRelationship(Direction.OUTGOING, RelTypes.INSTANCE_OF)) //The pool items and the pools themselves also have CHILD_OF_SPECIAL relationships
                    continue;
                
                if (poolClass != null) { //We will return only those matching with the specified class name
                    if (poolClass.equals((String)poolNode.getProperty(Constants.PROPERTY_CLASS_NAME)))
                        pools.add(Util.createPoolFromNode(poolNode));
                } else
                    pools.add(Util.createPoolFromNode(poolNode));
            }
            return pools;
        }
    }
           
    @Override
    public Pool getPool(long poolId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node poolNode = Util.findNodeByLabelAndId(poolLabel, poolId);
            
            if (poolNode != null) {                
                
                String name = poolNode.hasProperty(Constants.PROPERTY_NAME) ? 
                                    (String)poolNode.getProperty(Constants.PROPERTY_NAME) : null;
                
                String description = poolNode.hasProperty(Constants.PROPERTY_DESCRIPTION) ? 
                                    (String)poolNode.getProperty(Constants.PROPERTY_DESCRIPTION) : null;
                
                String className = poolNode.hasProperty(Constants.PROPERTY_CLASS_NAME) ? 
                                    (String)poolNode.getProperty(Constants.PROPERTY_CLASS_NAME) : null;
                
                int type = poolNode.hasProperty(Constants.PROPERTY_TYPE) ? 
                                    (int)poolNode.getProperty(Constants.PROPERTY_TYPE) : 0;
                
                return new Pool(poolId, name, description, className, type);
            }
            else
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
        }
    }
    
    @Override
    public List<BusinessObjectLight> getPoolItems(long poolId, int limit)
            throws ApplicationObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            
            Node poolNode = Util.findNodeByLabelAndId(poolLabel, poolId);

            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The pool with id %s could not be found", poolId));

            List<BusinessObjectLight> poolItems  = new ArrayList<>();

            int i = 0;
            for (Relationship rel : poolNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)){
                if (limit != -1){
                    if (i >= limit)
                         break;
                    i++;
                }
                if(rel.hasProperty(Constants.PROPERTY_NAME)){
                    if(rel.getProperty(Constants.PROPERTY_NAME).equals(Constants.REL_PROPERTY_POOL)){
                        Node item = rel.getStartNode();
                        Node temp = Util.findNodeByLabelAndId(poolLabel, item.getId());
                        if(temp == null)  //If it's not a pool, but a normal business object
                            poolItems.add(createObjectLightFromNode(item));
                    }
                }
            }
            tx.success();
            return poolItems;
        }
    }
    //</editor-fold>
    
    //<editor-fold desc="Helpers" defaultstate="collapsed">
    /**
     * Boiler-plate code. Gets a particular instance given the class name and the oid. Callers must handle associated transactions
     * @param className object class name
     * @param oid object id
     * @return a Node representing the entity
     * @throws MetadataObjectNotFoundException id the class could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException if the object could not be found
     */
    public Node getInstanceOfClass(String className, long oid) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException{
        
        //if any of the parameters is null, return the dummy root
        if (className == null || className.equals(Constants.NODE_DUMMYROOT))
            return graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
        
        Node classNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME,className);

        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

        Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iterableInstances.iterator();

        while (instances.hasNext()){
            Node otherSide = instances.next().getStartNode();
            if (otherSide.getId() == oid)
                return otherSide;
        }
        throw new BusinessObjectNotFoundException(className, oid);
        
    }
    /**
     * Boiler-plate code. Gets a particular instance given the class name and the oid
     * @param classId object class id
     * @param oid object id
     * @return a Node representing the entity
     * @throws MetadataObjectNotFoundException if the class could not be found
     * @throws org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException
     */
    public Node getInstanceOfClass(long classId, long oid) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException{
                
        //if any of the parameters is null, return the dummy root
        if (classId == -1)
            return graphDb.findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
        
        Node classNode = Util.findNodeByLabelAndId(classLabel, classId);
        
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class with id %s could not be found", classId));

        Iterable<Relationship> iteratorInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iteratorInstances.iterator();
        while (instances.hasNext()){
            Node otherSide = instances.next().getStartNode();
            if (otherSide.getId() == oid)
                return otherSide;
        }
        throw new BusinessObjectNotFoundException((String)classNode.getProperty(Constants.PROPERTY_NAME), oid);
    }

    public Node getInstanceOfClass(Node classNode, long oid) throws BusinessObjectNotFoundException{
        Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iterableInstances.iterator();
        
        while (instances.hasNext()){
            Node otherSide = instances.next().getStartNode();
            if (otherSide.getId() == oid)
                return otherSide;
        }
        throw new BusinessObjectNotFoundException((String)classNode.getProperty(Constants.PROPERTY_NAME), oid);
    }
    
    public Node createObject(Node classNode, ClassMetadata classToMap, HashMap<String,String> attributes) 
            throws InvalidArgumentException, MetadataObjectNotFoundException {
 
        if (classToMap.isAbstract())
            throw new InvalidArgumentException(String.format("Can not create objects from abstract classes (%s)", classToMap.getName()));
        
        Node newObject = graphDb.createNode(inventoryObjectLabel);
        newObject.setProperty(Constants.PROPERTY_NAME, ""); //The default value is an empty string 

        newObject.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis()); //The default value is right now
        
        if (attributes == null)
            attributes = new HashMap<>();
        
        for (AttributeMetadata attributeMetadata : classToMap.getAttributes()) {
            if (attributeMetadata.isMandatory() && attributes.get(attributeMetadata.getName()) == null)
                throw new InvalidArgumentException(String.format("The attribute %s is mandatory, it can not be empty", attributes.get(attributeMetadata.getName())));
            
            if (attributes.get(attributeMetadata.getName()) == null) //If the attribute is not included in the initial set of attributes to be set, we skip any further action
                continue;
            
            String attributeName = attributeMetadata.getName();
            String attributeType = classToMap.getType(attributeName);

            if (AttributeMetadata.isPrimitive(attributeType)){
                if(classToMap.isUnique(attributeName)){
                    //if an attribute is unique and mandatory it should be checked before the object creation, here
                    if(classToMap.getType(attributeName).equals("String") || //NOI18N
                        classToMap.getType(attributeName).equals("Integer") ||  //NOI18N
                        classToMap.getType(attributeName).equals("Float") ||  //NOI18N
                        classToMap.getType(attributeName).equals("Long")) { //NOI18N
                        if(isObjectAttributeUnique(classToMap.getName(), attributeName, attributes.get(attributeName)))
                            newObject.setProperty(attributeName, Util.getRealValue(attributes.get(attributeName), classToMap.getType(attributeName)));
                        else
                            throw new InvalidArgumentException(String.format("The attribute %s is unique, but the value provided is already in use", attributeName));
                    }
                }
                else
                    newObject.setProperty(attributeName, Util.getRealValue(attributes.get(attributeName), classToMap.getType(attributeName)));
            }
            else {
                //If it's not a primitive type, maybe it must be a a list type
                try {
                    
                    if (!mem.isSubClass(Constants.CLASS_GENERICOBJECTLIST, attributeType))
                        throw new InvalidArgumentException(String.format("Type %s is not a primitive nor a list type", attributeName));

                    List<Long> listTypeItemIds = new ArrayList<>();
                    for (String listTypeItemIdAsString : attributes.get(attributeName).split(";")) //If the attribute is multiple, the ids will be separated by ";", otherwise, it will be a single long value
                        listTypeItemIds.add(Long.valueOf(listTypeItemIdAsString));

                    Node listTypeClassNode = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, attributeType);

                    if (listTypeClassNode == null)
                        throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", attributeType));
                    
                    List<Node> listTypeItemNodes = Util.getListTypeItemNodes(listTypeClassNode, listTypeItemIds);

                    if(!listTypeItemNodes.isEmpty()){
                        //Create the new relationships
                        for (Node listTypeItemNode : listTypeItemNodes) {
                            Relationship newRelationship = newObject.createRelationshipTo(listTypeItemNode, RelTypes.RELATED_TO);
                            newRelationship.setProperty(Constants.PROPERTY_NAME, attributeName);
                        }

                    } else if(attributeMetadata.isMandatory())
                        throw new InvalidArgumentException(String.format("The attribute %s is mandatory, can not be set to null", attributeName));

                } catch (NumberFormatException ex) {
                    throw new InvalidArgumentException(String.format("The value %s is not a valid list type item id", attributes.get(attributeName)));
                }
            }
        }
        
        newObject.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);
        
        return newObject;       
    }
    
    private ChangeDescriptor updateObject(Node instance, ClassMetadata classMetadata, HashMap<String, String> attributes) throws InvalidArgumentException, MetadataObjectNotFoundException {
        String oldValues = "", newValues = "", affectedProperties = "";

        for (String attributeName : attributes.keySet()){
            if(classMetadata.hasAttribute(attributeName)) {
                affectedProperties = attributeName + " ";
                if (AttributeMetadata.isPrimitive(classMetadata.getType(attributeName))) { // We are changing a primitive type, such as String, or int
                    oldValues += (instance.hasProperty(attributeName) ? String.valueOf(instance.getProperty(attributeName)) : null) + " ";
                    
                    if (attributes.get(attributeName) == null) {
                        if(classMetadata.getAttribute(attributeName).isMandatory())//if attribute is mandatory can be set empty or null
                            throw new InvalidArgumentException(String.format("The attribute %s is mandatory, it can not be set null or empty", attributeName));
                        else
                            instance.removeProperty(attributeName);
                    } else {
                        newValues += attributes.get(attributeName) + " ";
                        //if attribute is mandatory string attributes can't be empty or null
                        if (classMetadata.getAttribute(attributeName).isMandatory()){
                            if (attributes.get(attributeName).isEmpty())
                                throw new InvalidArgumentException(String.format("The attribute %s is mandatory, it can not be null or empty", attributeName));
                        }
                        if (classMetadata.getAttribute(attributeName).isUnique()){
                            if(isObjectAttributeUnique(classMetadata.getName(), attributeName, attributes.get(attributeName)))
                                instance.setProperty(attributeName,Util.getRealValue(attributes.get(attributeName), classMetadata.getType(attributeName)));
                            else
                                throw new InvalidArgumentException(String.format("The attribute \"%s\" is unique and the value you are trying to set is already in use", attributeName));
                        }
                        else
                            instance.setProperty(attributeName,Util.getRealValue(attributes.get(attributeName), classMetadata.getType(attributeName)));
                    }
                } else { //If the attribute is not a primitive type, then it's a list type
                    if (!mem.getClass(classMetadata.getType(attributeName)).isListType())
                        throw new InvalidArgumentException(String.format("Class %s is not a list type", classMetadata.getType(attributeName)));

                    //Release the previous relationship
                    oldValues += " "; //Two empty, separation spaces
                    for (Relationship rel : instance.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO)) {
                        if (rel.getProperty(Constants.PROPERTY_NAME).equals(attributeName)){
                            oldValues += rel.getEndNode().getProperty(Constants.PROPERTY_NAME) + " ";
                            rel.delete();
                        }
                    }
                    
                    if (attributes.get(attributeName) == null || attributes.get(attributeName).trim().isEmpty()) {
                        if(classMetadata.getAttribute(attributeName).isMandatory())
                                throw new InvalidArgumentException(String.format("The attribute %s is mandatory, can not be set to null", attributeName));
                    } else {
                        try { //If the new value is different than null, then create the new relationships
                            List<Long> listTypeItemIds = new ArrayList<>();
                            for (String listTypeItemIdAsString : attributes.get(attributeName).split(";")) //If the attribute is multiple, the ids will be separated by ";", otherwise, it will be a single long value
                                listTypeItemIds.add(Long.valueOf(listTypeItemIdAsString));
                            
                            Node listTypeNodeClass = graphDb.findNode(classLabel, Constants.PROPERTY_NAME, classMetadata.getType(attributeName));
                            List<Node> listTypeItemNodes = Util.getListTypeItemNodes(listTypeNodeClass, listTypeItemIds);
                            
                            if(!listTypeItemNodes.isEmpty()) {
                                //Create the new relationships
                                for (Node listTypeItemNode : listTypeItemNodes) {
                                    newValues += listTypeItemNode.getProperty(Constants.PROPERTY_NAME) + " ";
                                    Relationship newRelationship = instance.createRelationshipTo(listTypeItemNode, RelTypes.RELATED_TO);
                                    newRelationship.setProperty(Constants.PROPERTY_NAME, attributeName);
                                }
                                
                            } else if(classMetadata.getAttribute(attributeName).isMandatory())
                                throw new InvalidArgumentException(String.format("The attribute %s is mandatory, can not be set to null", attributeName));

                        } catch(NumberFormatException ex) {
                            throw new InvalidArgumentException(String.format("The value %s is not a valid list type item id", attributes.get(attributeName)));
                        }
                    } 
                }
            } else
                throw new InvalidArgumentException(
                        String.format("The attribute %s does not exist in class %s", attributeName, classMetadata.getName()));
        }
        return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), newValues.trim(), String.valueOf(instance.getId()));
    }
    
    /**
     * Copies and object and optionally its children objects. This method does not manage transactions
     * @param templateObject The object to be cloned
     * @param recursive should the children be copied recursively?
     * @return The cloned node
     */
    private Node copyObject(Node templateObject, boolean recursive) {
        
        Node newInstance = graphDb.createNode(inventoryObjectLabel);
        for (String property : templateObject.getPropertyKeys())
            newInstance.setProperty(property, templateObject.getProperty(property));
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING))
            newInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, rel.getProperty(Constants.PROPERTY_NAME));
        
        newInstance.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
        
        newInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode(), RelTypes.INSTANCE_OF);

        if (recursive){
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING)){
                Node newChild = copyObject(rel.getStartNode(), true);
                newChild.createRelationshipTo(newInstance, RelTypes.CHILD_OF);
            }
        }
        return newInstance;
    }
    
    /**
     * Spawns [recursively] an inventory object from a template element
     * @param templateObject The template object used to create the inventory object
     * @param recursive Should the spawn operation be recursive?
     * @return The root copied object
     */
    private Node copyTemplateElement(Node templateObject, ClassMetadata classToMap, boolean recursive) throws InvalidArgumentException {
        
        Node newInstance = graphDb.createNode(inventoryObjectLabel);
        for (String property : templateObject.getPropertyKeys()){
            if(classToMap.isMandatory(property) && ((String)templateObject.getProperty(property)).isEmpty())
                throw new InvalidArgumentException(String.format("The attribute %s is mandatory, can not be set null or empty", property));
            
            newInstance.setProperty(property, templateObject.getProperty(property));
        }
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING))
            newInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, rel.getProperty(Constants.PROPERTY_NAME));
        
        newInstance.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
        
        newInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF_SPECIAL).iterator().next().getEndNode(), RelTypes.INSTANCE_OF);

        if (recursive) {
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING)) {
                Node classNode = rel.getStartNode().getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                Node newChild = copyTemplateElement(rel.getStartNode(), Util.createClassMetadataFromNode(classNode), true);
                newChild.createRelationshipTo(newInstance, RelTypes.CHILD_OF);
            }
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING)) {
                Node classNode = rel.getStartNode().getSingleRelationship(RelTypes.INSTANCE_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                Node newChild = copyTemplateElement(rel.getStartNode(), Util.createClassMetadataFromNode(classNode), true);
                newChild.createRelationshipTo(newInstance, RelTypes.CHILD_OF_SPECIAL);
            }
        }
        return newInstance;
    }

    @Override
    public boolean canDeleteObject(String className, long oid, boolean releaseRelationships) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException {
        if (!mem.isSubClass(Constants.CLASS_INVENTORYOBJECT, className))
            throw new OperationNotPermittedException(String.format("Class %s is not a business-related class", className));
        try (Transaction tx = graphDb.beginTx()) {   
            Node instance = getInstanceOfClass(className, oid);
            boolean safeDeletion = Util.canDeleteObject(instance, true);
            tx.success();
            return safeDeletion;
        }
    }
    
    /**
     * This class wraps a set of attribute definitions necessary to create objects with default values
     */
    public class AttributeDefinitionSet implements Serializable {
        /**
         * The key is the attribute name, the value, the attribute definition, typically one value, a string or a number
         */
        private HashMap<String, String[]> attributes;

        public HashMap<String, String[]> getAttributes() {
            return attributes;
        }

        public void setAttributes(HashMap<String, String[]> attributes) {
            this.attributes = attributes;
        }
    }
    
    /**
     * Check if the value of the given attribute name is unique across other 
     * objects in the class and its subclasses
     * @param className the class name
     * @param attributeName attribute name
     * @param attributeValue attribute value
     * @return true if the attribute value is unique
     */
    private boolean isObjectAttributeUnique(String className, String attributeName, String attributeValue){
        List<String> uniqueAttributeValues = CacheManager.getInstance().getUniqueAttributeValues(className, attributeName);
        if(uniqueAttributeValues != null){
            for (String uniqueAttributeValue : uniqueAttributeValues) {
                if(uniqueAttributeValue.equals(attributeValue))
                    return false;
            }
        }
        CacheManager.getInstance().putUniqueAttributeValueIndex(className, attributeName, attributeValue);
        return true;
    }
    //</editor-fold>
    /**
     * Transaction-less version of the overloaded method
     * @param classNode Node of the class the object we're searching for is instance of
     * @param filterName Parameter to be used as filter
     * @param filterValue Search string
     * @return The list of objects matching the search string
     */
    private List<BusinessObjectLight> getObjectsWithFilterLight (Node classNode, 
            String filterName, String filterValue) {
        
        List<BusinessObjectLight> res = new ArrayList<>();
            
        for (Relationship instanceOfRelationship : classNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF)) {
            Node instance = instanceOfRelationship.getStartNode();
            if (instance.hasProperty(filterName) && instance.getProperty(filterName).equals(filterValue))
                res.add(createObjectLightFromNode(instance));
            else {
                Iterable<Relationship> iterableRelationships = instance.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING);
                Iterator<Relationship> relationships = iterableRelationships.iterator();
                
                while(relationships.hasNext()){
                    Relationship relationship = relationships.next();
                    
                    if (relationship.hasProperty(Constants.PROPERTY_NAME) && 
                        String.valueOf(relationship.getProperty(Constants.PROPERTY_NAME)).equals(filterName) &&
                        String.valueOf(relationship.getEndNode().getId()).equals(filterValue)) {
                        
                        res.add(createObjectLightFromNode(instance));                        
                    }
                }
            }
        }
        
        for (Relationship subClassRel : classNode.getRelationships(Direction.INCOMING, RelTypes.EXTENDS))
            res.addAll(getObjectsWithFilterLight(subClassRel.getStartNode(), filterName, filterValue));
        
        return res;
    }
    
    private List<BusinessObject> getObjectsWithFilter(Node classNode, 
            String filterName, String filterValue) throws InvalidArgumentException {
        
        List<BusinessObject> res = new ArrayList<>();
            
        for (Relationship instanceOfRelationship : classNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF)) {
            Node instance = instanceOfRelationship.getStartNode();
            if (instance.hasProperty(filterName) && instance.getProperty(filterName).equals(filterValue))
                res.add(createObjectFromNode(instance));
            else {
                Iterable<Relationship> iterableRelationships = instance.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING);
                Iterator<Relationship> relationships = iterableRelationships.iterator();
                
                while(relationships.hasNext()){
                    Relationship relationship = relationships.next();
                    
                    if (relationship.hasProperty(Constants.PROPERTY_NAME) && 
                        String.valueOf(relationship.getProperty(Constants.PROPERTY_NAME)).equals(filterName) &&
                        String.valueOf(relationship.getEndNode().getId()).equals(filterValue)) {
                        
                        res.add(createObjectFromNode(instance));                        
                    }
                }
            }
        }
        
        for (Relationship subClassRel : classNode.getRelationships(Direction.INCOMING, RelTypes.EXTENDS))
            res.addAll(getObjectsWithFilter(subClassRel.getStartNode(), filterName, filterValue));
        
        return res;
    }
    
    private void getChildrenOfClassRecursive(long parentOid, String parentClass, String classToFilter, int maxResults, List<BusinessObjectLight> res) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        
        if (maxResults > 0 && res.size() == maxResults)
            return;
        
        Node parentNode = getInstanceOfClass(parentClass, parentOid);
        Iterable<Relationship> relationshipsChildOf = parentNode.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING);

        for (Relationship relatioshipChildOf : relationshipsChildOf) {
            Node child = relatioshipChildOf.getStartNode();
            String childClassName = Util.getClassName(child);

            if (childClassName == null)
                throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found", child.getId()));

            if (mem.isSubClass(classToFilter, childClassName)) {
                res.add(createObjectLightFromNode(child));

                if (maxResults > 0 && res.size() == maxResults)
                    break;
            }
            getChildrenOfClassRecursive(child.getId(), childClassName, classToFilter, maxResults, res);
        }
    }
    
    private void getSpecialChildrenOfClassRecursive(Node parentNode, String classToFilter, 
            int maxResults, List<BusinessObjectLight> res) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException {
        
        if (maxResults > 0 && res.size() == maxResults)
            return;
        
        Iterable<Relationship> relationshipsChildOfSpecial = parentNode.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING);

        for (Relationship relatioshipChildOfSpecial : relationshipsChildOfSpecial) {
            Node specialChild = relatioshipChildOfSpecial.getStartNode();
            
            if (!specialChild.hasLabel(this.poolLabel)) { //If the node is a pool, let's skip it and dig deeper, otherwise, let's see if it matches the filter
            
                String specialChildClassName = Util.getClassName(specialChild);

                if (specialChildClassName == null)
                    throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found", specialChild.getId()));

                if (mem.isSubClass(classToFilter, specialChildClassName)) {
                    res.add(createObjectLightFromNode(specialChild));

                    if (maxResults > 0 && res.size() == maxResults)
                        break;
                }
            }
            getSpecialChildrenOfClassRecursive(specialChild, classToFilter, maxResults, res);
        }
    }
    
    private BusinessObjectLight createObjectLightFromNode (Node instance) {
        String className = (String)instance.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME);
        
        //First, we create the naked business object, without validators
        BusinessObjectLight res = new BusinessObjectLight(className, instance.getId(), (String)instance.getProperty(Constants.PROPERTY_NAME));
        
        //Then, we check the cache for validator definitions
        List<ValidatorDefinition> validatorDefinitions = CacheManager.getInstance().getValidatorDefinitions(className);
        if (validatorDefinitions == null) { //Since the validator definitions are not cached, we retrieve them from the object class plus its super classes
            validatorDefinitions = new ArrayList<>();
            try {
                List<ClassMetadataLight> classHierarchy = mem.getUpstreamClassHierarchy(className, true);
                //The query return the hierarchy from the subclass to the super class, so we reverse it so the lower level validator definitions 
                //have more priority (that is, are processed the last)
                Collections.reverse(classHierarchy); 
                for (ClassMetadataLight aClass : classHierarchy) {
                    ResourceIterator<Node> validatorDefinitionNodes = graphDb.findNodes(Label.label(Constants.LABEL_VALIDATOR_DEFINITIONS), 
                            Constants.PROPERTY_CLASS_NAME, 
                            aClass.getName());
                    
                    while (validatorDefinitionNodes.hasNext()) {
                        Node aValidatorDefinitionNode = validatorDefinitionNodes.next();
                        validatorDefinitions.add(new ValidatorDefinition(
                                        aValidatorDefinitionNode.getId(), 
                                        (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_NAME), 
                                        (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                                        aClass.getName(), 
                                        (String)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_SCRIPT), 
                                        (boolean)aValidatorDefinitionNode.getProperty(Constants.PROPERTY_ENABLED)));
                    }
                }
                
                //Now we cachethe results
                CacheManager.getInstance().addValidatorDefinitions(className, validatorDefinitions);
            } catch (MetadataObjectNotFoundException ex) {
                //Should not happen
            }    
        }
        
        List<Validator> validators = new ArrayList<>();
        
        //Now we run the applicable validator definitions
        validatorDefinitions.forEach((aValidatorDefinition) -> {
            try {
                String script = aValidatorDefinition.getScript();
                if (!script.trim().isEmpty() && aValidatorDefinition.isEnabled()) {
                    Binding environmentParameters = new Binding();
                    environmentParameters.setVariable("validatorDefinitionName", aValidatorDefinition.getName());
                    environmentParameters.setVariable("objectClass", className); //Although we already have a reference to the object node, it is not
                    environmentParameters.setVariable("objectId", instance.getId()); //a good thing to encourage users to access directly to the database, 
                                                                               //since upper layers of the application must be backend-agnostic
                    GroovyShell shell = new GroovyShell(ApplicationEntityManager.class.getClassLoader(), environmentParameters);
                    Object theResult = shell.evaluate(script);

                    if (theResult instanceof Validator) //The script must return a validator, otherwise, the result will be ignored
                        validators.add((Validator)theResult);
                    else
                        System.out.println(String.format("[KUWAIBA] Validator %s is not returning a validator from its execution", 
                            aValidatorDefinition.getName()));
                }
            } catch (Exception ex) { //Errors will be logged and the validator definition skipped
                System.out.println(String.format("[KUWAIBA] An unexpected error occurred while evaluating validator %s in object %s(%s): %s", 
                        aValidatorDefinition.getName(), instance.getProperty(Constants.PROPERTY_NAME), 
                        instance.getId(), ex.getLocalizedMessage()));
            }
        });
        
        res.setValidators(validators);
        return res;
    }
       
    private BusinessObject createObjectFromNode(Node instance) throws InvalidArgumentException {
        String className = (String)instance.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME);
        try {
            return createObjectFromNode(instance, mem.getClass(className));
        } catch (MetadataObjectNotFoundException mex) {
            throw new InvalidArgumentException(mex.getLocalizedMessage());
        }
    }
    
    /**
     * Builds a RemoteBusinessObject instance from a node representing a business object
     * @param instance The object as a Node instance.
     * @param classMetadata The class metadata to map the node's properties into a RemoteBussinessObject.
     * @return The business object.
     * @throws InvalidArgumentException If an attribute value can't be mapped into value.
     */
    private BusinessObject createObjectFromNode(Node instance, ClassMetadata classMetadata) throws InvalidArgumentException {
        
        HashMap<String, String> attributes = new HashMap<>();
        String name = "";
        
        for (AttributeMetadata myAtt : classMetadata.getAttributes()){
            //Only set the attributes existing in the current node. Please note that properties can't be null in
            //Neo4J, so a null value is actually a non-existing relationship/value
            if (instance.hasProperty(myAtt.getName())){
               if (AttributeMetadata.isPrimitive(myAtt.getType())) {
                    if (!myAtt.getType().equals("Binary")) {
                        String value = String.valueOf(instance.getProperty(myAtt.getName()));
                        
                        if (Constants.PROPERTY_NAME.equals(myAtt.getName()))
                            name = value;
                        
                        attributes.put(myAtt.getName(),value);
                    } else if (myAtt.getType().equals("Binary")) {
                        byte [] byteArray = (byte []) instance.getProperty(myAtt.getName());
                        attributes.put(myAtt.getName(), new String(byteArray));
                    }
                }
            }
        }

        //Iterates through relationships and transform the into "plain" attributes
        Iterable<Relationship> iterableRelationships = instance.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING);
        Iterator<Relationship> relationships = iterableRelationships.iterator();

        while(relationships.hasNext()){
            Relationship relationship = relationships.next();
            if (!relationship.hasProperty(Constants.PROPERTY_NAME))
                throw new InvalidArgumentException(String.format("The object with id %s is malformed", instance.getId()));

            String relationshipName = (String)relationship.getProperty(Constants.PROPERTY_NAME);              
            
            boolean hasRelationship = false;
            for (AttributeMetadata myAtt : classMetadata.getAttributes()) {
                if (myAtt.getName().equals(relationshipName)) {
                    if (attributes.containsKey(relationshipName))
                        attributes.put(relationshipName, attributes.get(relationshipName) + ";" + String.valueOf(relationship.getEndNode().getId())); //A multiple selection list type
                    else    
                        attributes.put(relationshipName, String.valueOf(relationship.getEndNode().getId()));
                    hasRelationship = true;
                    break;
                }                  
            }
            
            if (!hasRelationship) //This verification will help us find potential inconsistencies with list types
                                  //What this does is to verify if is there is a RELATED_TO relationship that shouldn't exist because its name is not an attribute of the class
                throw new InvalidArgumentException(String.format("The object with %s (%s) is related to list type %s (%s), but that is not consistent with the data model", 
                            instance.getProperty(Constants.PROPERTY_NAME), instance.getId(), relationship.getEndNode().getProperty(Constants.PROPERTY_NAME), relationship.getEndNode().getId()));
        }
        BusinessObject res = new BusinessObject(classMetadata.getName(), instance.getId(), name, attributes);

        return res;
        
    }
}
