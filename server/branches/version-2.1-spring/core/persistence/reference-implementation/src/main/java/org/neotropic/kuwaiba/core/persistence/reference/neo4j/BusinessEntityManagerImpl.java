/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.neotropic.kuwaiba.core.persistence.reference.neo4j;

import org.neotropic.kuwaiba.core.apis.persistence.ChangeDescriptor;
import org.neotropic.kuwaiba.core.apis.persistence.ConnectionManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.FileObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.InventoryReport;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.application.reporting.ReportMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.AnnotatedBusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLightList;
import org.neotropic.kuwaiba.core.apis.persistence.business.Contact;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.OperationNotPermittedException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.apis.persistence.util.StringPair;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.NotFoundException;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.helpers.collection.Iterators;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.core.persistence.reference.extras.caching.CacheManager;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.ObjectGraphMappingService;
import org.neotropic.kuwaiba.core.persistence.reference.neo4j.util.Util;
import org.neotropic.kuwaiba.core.persistence.reference.util.DynamicNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Business entity manager reference implementation (using Neo4J as backend).
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Service
public class BusinessEntityManagerImpl implements BusinessEntityManager {
    /**
     * Default attachment location.
     */
    private static final String DEFAULT_ATTACHMENTS_PATH = "/data/files/attachments";
    /**
     * Object Label.
     */
    private final Label inventoryObjectLabel;
    /**
     * Class Label.
     */
    private final Label classLabel;
    /**
     * Pools Label.
     */
    private final Label poolLabel;
    /**
     * Templates label 
     */
    private Label templateLabel;
    /**
     * Special nodes Label.
     */
    private final Label specialNodeLabel;
    /**
     * Label for reports.
     */
    private final Label reportsLabel;
    /**
     * Label used to tag the nodes that store contact information.
     */
    private final Label contactsLabel;
    /**
     * Global configuration variables.
     */
    private Properties configuration;
    /**
     * A classloader to place all the validator definition classes created on-the-fly.
     */
    private GroovyClassLoader validatorDefinitionsClassLoader = new GroovyClassLoader();
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ConnectionManager<GraphDatabaseService> connectionManager;
    /**
     * Reference to the Metadata Entity Manager.
     */
    @Autowired
    private MetadataEntityManager mem;
    /**
     * Reference to the Application Entity Manager.
     */
    @Autowired
    private ApplicationEntityManager aem;
    
    @Autowired
    private ObjectGraphMappingService ogmService;
    
    @Autowired
    private TranslationService ts;
    /**
     * Main constructor. It receives references to the other entity managers
     */
    @Autowired
    public BusinessEntityManagerImpl() {
        this.configuration = new Properties();
        this.inventoryObjectLabel = Label.label(Constants.LABEL_INVENTORY_OBJECTS);
        this.classLabel = Label.label(Constants.LABEL_CLASS);
        this.poolLabel = Label.label(Constants.LABEL_POOLS);
        this.templateLabel = Label.label(Constants.LABEL_TEMPLATES);
        this.specialNodeLabel = Label.label(Constants.LABEL_SPECIAL_NODE);
        this.reportsLabel = Label.label(Constants.LABEL_REPORTS);
        this.contactsLabel = Label.label(Constants.LABEL_CONTACTS);
        this.validatorDefinitionsClassLoader = new GroovyClassLoader();
    }

    @Override
    public void initCache() {
        // Nothing for now.
    }
    
    @Override
    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }

    @Override
    public String createObject(String className, String parentClassName, String parentOid, HashMap<String, String> attributes, String templateId)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (parentOid == null)
            throw new BusinessObjectNotFoundException(parentClassName, parentOid);
                
        ClassMetadata myClass= mem.getClass(className);
        
        if (!mem.canBeChild(parentClassName, className)) 
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {        
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null) 
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

            if (myClass.isInDesign())
                throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");

            if (myClass.isAbstract())
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", className));

            if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, className))
                throw new OperationNotPermittedException("Can not create non-inventory objects");

            //The object should be created under an instance other than the dummy root
            if (parentClassName != null && !parentOid.equals("-1")) {
                ClassMetadata myParentObjectClass= mem.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", parentClassName));
            }

            Node parentNode;
            if (!parentOid.equals("-1")) {
                 parentNode = getInstanceOfClass(parentClassName, parentOid);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentOid);
            }
            else
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            Node newObject;
            if (templateId == null || templateId.isEmpty())
                newObject = createObject(classNode, myClass, attributes);
            else {
                try {
                    Node templateNode = connectionManager.getConnectionHandler().findNode(templateLabel, Constants.PROPERTY_UUID, templateId);
                    if (templateNode.hasRelationship(Direction.INCOMING, RelTypes.HAS_TEMPLATE)) {
                        if (className.equals(templateNode.getSingleRelationship(RelTypes.HAS_TEMPLATE, Direction.INCOMING).
                                getStartNode().getProperty(Constants.PROPERTY_NAME)))
                            newObject = copyTemplateElement(templateNode, myClass, true);
                        else
                            throw new InvalidArgumentException(String.format("The template with id %s is not applicable to instances of class %s", templateId, className));
                    } else 
                        throw new InvalidArgumentException(String.format("The template with id %s is malformed", templateId));
                    
                } catch (NotFoundException ex) {
                    throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", templateId, className));
                }
            }
            newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);            
            tx.success();
            return (String)newObject.getProperty(Constants.PROPERTY_UUID);
        }
    }
    
    //TODO: Rewrite this!
    @Override
    public String createObject(String className, String parentClassName, HashMap<String, String> attributes, String templateId, String criteria)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException {
        
        ClassMetadata objectClass = mem.getClass(className);
        if (objectClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
        
        if (objectClass.isInDesign())
            throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");

        if (objectClass.isAbstract())
            throw new OperationNotPermittedException("Can not create instances of abstract classes");

        if (!mem.isSubclassOf("InventoryObject", className))
            throw new OperationNotPermittedException("Can not create non-inventory objects");
        
        if (!mem.canBeChild(parentClassName, className))
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
        
        String[] splitCriteria = criteria.split(":");
        if (splitCriteria.length != 2)
            throw new InvalidArgumentException("The criteria is not valid, two components expected (attributeName:attributeValue)");

        if (splitCriteria[0].equals(Constants.PROPERTY_OID)) //The user is providing the id of te parent node explicitely
            return createObject(className, parentClassName, splitCriteria[1], attributes, templateId);

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
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
           
            Node parentClassNode, parentNode = null;
            
            if (Constants.NODE_DUMMYROOT.equals(parentClassName))
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else {
                parentClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, parentClassName);
                
                Iterator<Relationship> instances = parentClassNode.getRelationships(RelTypes.INSTANCE_OF).iterator();

                while (instances.hasNext()) {
                    Node possibleParentNode = instances.next().getStartNode();                   
                    if (possibleParentNode.getProperty(splitCriteria[0]).toString().equals(splitCriteria[1])) {
                        parentNode = possibleParentNode;
                        break;
                    }
                }
            }
            
            if (parentNode == null)
                throw new InvalidArgumentException(String.format("A parent object of class %s and %s = %s could not be found", parentClassName, splitCriteria[0], splitCriteria[1]));
            
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

            Node newObject;
            if (templateId == null || templateId.isEmpty())
                newObject = createObject(classNode, objectClass, attributes);
            else {
                try {
                    Node templateNode = connectionManager.getConnectionHandler().findNode(templateLabel, Constants.PROPERTY_UUID, templateId);
                    if (templateNode.hasRelationship(Direction.INCOMING, RelTypes.HAS_TEMPLATE)) {
                        if (objectClass.equals(templateNode.getSingleRelationship(RelTypes.HAS_TEMPLATE, Direction.INCOMING).
                                getStartNode().getProperty(Constants.PROPERTY_NAME)))
                            newObject = copyTemplateElement(templateNode, objectClass, true);
                        else
                            throw new InvalidArgumentException(String.format("The template with id %s is not applicable to instances of class %s", templateId, objectClass));
                    } else 
                        throw new InvalidArgumentException(String.format("The template with id %s is malformed", templateId));
                    
                } catch (NotFoundException ex) {
                    throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", templateId, className));
                }
            }
            
            newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);
                      
            tx.success();
            return newObject.hasProperty(Constants.PROPERTY_UUID) ? newObject.getProperty(Constants.PROPERTY_UUID).toString() : null;
        }
    }

    /**
     * Creates a new inventory object for a domain specific model (where the standard containment rules don't apply)
     * @param className Name of the class which this object will be instantiated from
     * @param parentClassName Parent object class name
     * @param parentOid Parent's oid
     * @param attributes Attributes to be set by default in the new object. It's a HashMap where the keys are the attribute names and the values, the values for such attributes.
     * Note that binary type attributes can't be set here.
     * @param templateId The id of the template to be used to create this object. This id was probably retrieved by {@link ApplicationEntityManager.getTemplatesForClass(String)} before. Use a null or empty string to not use a template.
     * @return The id of the new object.
     * @throws MetadataObjectNotFoundException Thrown if the object's class can't be found
     * @throws BusinessObjectNotFoundException Thrown if the parent id is not found
     * @throws OperationNotPermittedException If the update can't be performed due to a format issue
     * @throws InvalidArgumentException If any of the attribute values has an invalid value or format.
     * @throws ApplicationObjectNotFoundException If the specified template could not be found.
     */
    @Override
    public String createSpecialObject(String className, String parentClassName, String parentOid, HashMap<String,String> attributes, String templateId)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        if (parentOid == null)
            throw new BusinessObjectNotFoundException(parentClassName, parentOid);
        
        ClassMetadata classMetadata= mem.getClass(className);
        if (classMetadata == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

        if (classMetadata.isInDesign())
            throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");
        
        if (classMetadata.isAbstract())
            throw new OperationNotPermittedException("Can not create objects of abstract classes");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

            //The object should be created under an instance other than the dummy root
            if (parentClassName != null) {
                ClassMetadata myParentObjectClass= mem.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            }

            Node parentNode = null;
            if (!parentOid.equals("-1")) {
                 parentNode = getInstanceOfClass(parentClassName, parentOid);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentOid);
            }
        
            Node newObject;
            
            if (templateId == null || templateId.isEmpty()) 
                newObject = createObject(classNode, classMetadata, attributes);
                
            else {
                try {
                    Node templateNode = connectionManager.getConnectionHandler().findNode(templateLabel, Constants.PROPERTY_UUID, templateId);
                    if (templateNode.hasRelationship(Direction.INCOMING, RelTypes.HAS_TEMPLATE)) {
                        if (className.equals(templateNode.getSingleRelationship(RelTypes.HAS_TEMPLATE, Direction.INCOMING).
                                getStartNode().getProperty(Constants.PROPERTY_NAME))) {
                            newObject = copyTemplateElement(templateNode, classMetadata, true);
                            updateObject(newObject, classMetadata, attributes); //Override the template values with those provided, if any
                        }
                        else
                            throw new InvalidArgumentException(String.format("The template with id %s is not applicable to instances of class %s", templateId, className));
                    } else 
                        throw new InvalidArgumentException(String.format("The template with id %s is malformed", templateId));
                    
                } catch (NotFoundException ex) {
                    throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", templateId, className));
                }
            }
            if (parentNode !=null)
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
            
            tx.success();
            return newObject.hasProperty(Constants.PROPERTY_UUID) ? newObject.getProperty(Constants.PROPERTY_UUID).toString() : null;
        }
    }
    
    @Override
    public void addSpecialObjectToObject(String specialObjectClass, String specialObjectId, String objectClass, String objectId) 
        throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        if (specialObjectId == null)
            throw new BusinessObjectNotFoundException(specialObjectClass, specialObjectId);
        if (objectId == null)
            throw new BusinessObjectNotFoundException(objectClass, objectId);
        if (specialObjectId.equals(objectId))
            throw new OperationNotPermittedException("An object can not be related to itself");
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node specialObjectNode = getInstanceOfClass(specialObjectClass, specialObjectId);
            for (Relationship rel : specialObjectNode.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                String otherNodeUuid = rel.getOtherNode(specialObjectNode).hasProperty(Constants.PROPERTY_UUID) ? (String) rel.getOtherNode(specialObjectNode).getProperty(Constants.PROPERTY_UUID) : null;
                
                if (otherNodeUuid != null && otherNodeUuid.equals(objectId))
                    throw new OperationNotPermittedException("These elements are already related");
            }
            Node objectNode = getInstanceOfClass(objectClass, objectId);
            specialObjectNode.createRelationshipTo(objectNode, RelTypes.CHILD_OF_SPECIAL);
            tx.success();
        }            
    }
    
    @Override
    public String createHeadlessObject(String className, HashMap<String, String> attributes, String templateId)
            throws OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        ClassMetadata myClass= mem.getClass(className);
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {        
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

            if (myClass.isInDesign())
                throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");

            if (myClass.isAbstract())
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", className));

            Node newObject;
            if (templateId == null || templateId.isEmpty())
                newObject = createObject(classNode, myClass, attributes);
            else {
                try {
                    Node templateNode = connectionManager.getConnectionHandler().findNode(templateLabel, Constants.PROPERTY_UUID, templateId);
                    if (templateNode.hasRelationship(Direction.INCOMING, RelTypes.HAS_TEMPLATE)) {
                        if (className.equals(templateNode.getSingleRelationship(RelTypes.HAS_TEMPLATE, Direction.INCOMING).
                                getStartNode().getProperty(Constants.PROPERTY_NAME)))
                            newObject = copyTemplateElement(templateNode, myClass, true);
                        else
                            throw new InvalidArgumentException(String.format("The template with id %s is not applicable to instances of class %s", templateId, className));
                    } else 
                        throw new InvalidArgumentException(String.format("The template with id %s is malformed", templateId));
                    
                } catch (NotFoundException ex) {
                    throw new ApplicationObjectNotFoundException(String.format("No template with id %s was found for class %s", templateId, className));
                }
            }
            tx.success();
            return (String)newObject.getProperty(Constants.PROPERTY_UUID);
        }
    }
    
    @Override
    public String createPoolItem(String poolId, String className, HashMap<String, String> attributes, String templateId) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        
        try (Transaction tx =connectionManager.getConnectionHandler().beginTx()) {
            Node pool = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);
            
            if (pool == null)
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
            
            if (!pool.hasProperty(Constants.PROPERTY_CLASS_NAME))
                throw new InvalidArgumentException("This pool has not set his class name attribute");
            if (className == null)
                throw new InvalidArgumentException("The class namme  can not be null");
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            ClassMetadata classMetadata = mem.getClass(className);
            
            if (!mem.isSubclassOf((String)pool.getProperty(Constants.PROPERTY_CLASS_NAME), className))
                throw new InvalidArgumentException(String.format("Class %s is not subclass of %s", className, (String)pool.getProperty(Constants.PROPERTY_CLASS_NAME)));
            
            Node newObject = createObject(classNode, classMetadata, attributes);
            newObject.createRelationshipTo(pool, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
            
            tx.success();
            return newObject.hasProperty(Constants.PROPERTY_UUID) ? newObject.getProperty(Constants.PROPERTY_UUID).toString() : null;
        }
    }
    
    @Override
    public String[] createBulkObjects(String className, String parentClassName, String parentOid, String namePattern) 
        throws MetadataObjectNotFoundException, OperationNotPermittedException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        if (parentOid == null)
            throw new InvalidArgumentException("The parent id cannot be null");
                
        ClassMetadata myClass = mem.getClass(className);
        
        if (!mem.canBeChild(parentClassName, className))
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
                       
            if (myClass.isInDesign())
                throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");
            
            if (myClass.isAbstract())
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", className));
            
            if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, className))
                throw new OperationNotPermittedException("Can not create non-inventory objects");
            //The object should be created under an instance other than the dummy root
            if (parentClassName != null && !parentOid.equals("-1")) {
                ClassMetadata myParentObjectClass = mem.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", parentClassName));
            }
            Node parentNode;
            if (!parentOid.equals("-1")) {
                parentNode = getInstanceOfClass(parentClassName, parentOid);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentOid);
                
            }
            else
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            if (parentNode == null)
                throw new BusinessObjectNotFoundException(parentClassName, parentOid);
            
            DynamicNameGenerator dynamicName = new DynamicNameGenerator(namePattern);                                   
            String res[] = new String[dynamicName.getNumberOfDynamicNames()];
            
            List<StringPair> createdMirrorPorts = new ArrayList<>();
            
            for (int i = 0; i < dynamicName.getNumberOfDynamicNames(); i++) {
                Node newObject = createObject(classNode, myClass, null);
                newObject.setProperty(Constants.PROPERTY_NAME, dynamicName.getDynamicNames().get(i));
                
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);
                
                res[i] = newObject.hasProperty(Constants.PROPERTY_UUID) ? newObject.getProperty(Constants.PROPERTY_UUID).toString() : null;
                
                if (dynamicName.isMirrorPortsSequence()) {
                    String newObjectUuid = newObject.hasProperty(Constants.PROPERTY_UUID) ? (String) newObject.getProperty(Constants.PROPERTY_UUID) : null;
                    if (newObjectUuid == null)
                        throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", newObject.getId()));
                    createdMirrorPorts.add(new StringPair(newObjectUuid, dynamicName.getDynamicNames().get(i)));
                }
            }
            
            if (dynamicName.isMirrorPortsSequence())
                dynamicName.createMirrorRelationships(createdMirrorPorts, className);
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public String[] createBulkSpecialObjects(String className, String parentClassName, String parentId, int numberOfSpecialObjects, String namePattern) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {
        
        if (parentId == null)
            throw new InvalidArgumentException("The parent id cannot be null");
                            
        ClassMetadata myClass= mem.getClass(className);
        if (myClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
        
        if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, className))
            throw new OperationNotPermittedException(String.format("Class %s is not an business class", className));
        
        if (myClass.isInDesign())
            throw new OperationNotPermittedException("Can not create instances of classes marked as inDesign");
        
        if (myClass.isAbstract())
            throw new OperationNotPermittedException("Can't create objects from an abstract classes");

        try (Transaction tx =connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME,className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));

            //The object should be created under an instance other than the dummy root
            if (parentClassName != null) {
                ClassMetadata myParentObjectClass= mem.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            }

            Node parentNode;
            if (!parentId.equals("-1")) {
                 parentNode = getInstanceOfClass(parentClassName, parentId);
                if (parentNode == null)
                    throw new BusinessObjectNotFoundException(parentClassName, parentId);
            }
            else
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
                        
            DynamicNameGenerator dynamicName = new DynamicNameGenerator(namePattern);
            if (dynamicName.getNumberOfDynamicNames() < numberOfSpecialObjects) {
                throw new InvalidArgumentException("The given pattern to generate the name has "
                        + "less possibilities that the number of object to be created");
            }
            String res[] = new String[numberOfSpecialObjects];
            for (int i = 0; i < numberOfSpecialObjects; i++) {
                Node newObject = createObject(classNode, myClass, null);
                newObject.setProperty(Constants.PROPERTY_NAME, dynamicName.getDynamicNames().get(i));
                if (parentNode != null)
                    newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
                
                res[i] = newObject.hasProperty(Constants.PROPERTY_UUID) ? newObject.getProperty(Constants.PROPERTY_UUID).toString() : null;
            }
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public BusinessObject getObject(String className, String oid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            ClassMetadata myClass = mem.getClass(className);
            Node instance = getInstanceOfClass(className, oid);
            BusinessObject res = createObjectFromNode(instance, myClass);
            tx.success();
            return res;
        }
    }

    @Override
    public BusinessObjectLight getObjectLight(String className, String oid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        if (oid == null)
            throw new InvalidArgumentException("The object id cannot be null");
        //TODO: Re-write this method and check if a simple Cypher query is faster than the programatic solution!
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
            Iterator<Relationship> instances = iterableInstances.iterator();
            while (instances.hasNext()) {
                Node instance = instances.next().getStartNode();
                if (instance.getProperty(Constants.PROPERTY_UUID).equals(oid)) {
                    tx.success();
                    return (new BusinessObjectLight(className, oid, (String)instance.getProperty(Constants.PROPERTY_NAME)));
                }
            }
            throw new BusinessObjectNotFoundException(className, oid);
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectsWithFilterLight (String className, 
            String filterName, String filterValue) throws MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            tx.success();
            return getObjectsWithFilterLight(classNode, filterName, filterValue);
        }
    }
    
    @Override
    public List<BusinessObject> getObjectsWithFilter (String className, 
            String filterName, String filterValue) throws MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);

            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            tx.success();
            return getObjectsWithFilter(classNode, filterName, filterValue);
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(String filter, int limit) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String cypherQuery = "MATCH (object:" + inventoryObjectLabel + ")-[:INSTANCE_OF]->(class)" + 
                    " WHERE TOLOWER(object.name) CONTAINS TOLOWER({searchString}) OR TOLOWER(class.name) "
                    + "CONTAINS TOLOWER({searchString}) RETURN object.name as oname, object._uuid as oid, class.name as cname ORDER BY object.name ASC" + (limit > 0 ? " LIMIT " + limit : ""); //NOI18N
            
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("searchString", filter);
            Result queryResult = connectionManager.getConnectionHandler().execute(cypherQuery, parameters);
            
            List<BusinessObjectLight> res  = new ArrayList<>();
            
            while (queryResult.hasNext()) {
                Map<String, Object> row = queryResult.next();
                res.add(new BusinessObjectLight((String)row.get("cname"), (String)row.get("oid"), 
                        (String)row.get("oname")));
            }
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSuggestedObjectsWithFilter(String filter, String superClass, int limit) {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            String cypherQuery = "MATCH (object:" + inventoryObjectLabel + ")-[:INSTANCE_OF]->(class)" +
                    "-[:EXTENDS*0..]->(superclass) WHERE (TOLOWER(object.name) CONTAINS TOLOWER({searchString})" + 
                    " OR TOLOWER(class.name) CONTAINS TOLOWER({searchString})) AND superclass.name={superclass} " + 
                    "RETURN object.name as oname, object._uuid as oid, class.name as cname ORDER BY object.name ASC" + (limit > 0 ? " LIMIT " + limit : "");

            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("searchString", filter);
            parameters.put("superclass", superClass);
            Result queryResult = connectionManager.getConnectionHandler().execute(cypherQuery, parameters);
            
            List<BusinessObjectLight> res  = new ArrayList<>();
            
            while (queryResult.hasNext()) {
                Map<String, Object> row = queryResult.next();
                res.add(new BusinessObjectLight((String)row.get("cname"), (String)row.get("oid"), 
                        (String)row.get("oname")));
            }
            
            tx.success();
            return res;
        }
    }
    
    @Override
    public String getAttributeValueAsString (String objectClass, String objectId, String attributeName) 
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
                    return theObject.getAttributes().get(attributeName).toString();
                case "Date": //NOI18N
                case "Time": //NOI18N
                case "Timestamp": //NOI18N
                    return new Date((Long)theObject.getAttributes().get(attributeName)).toString();
                default: //It's (or at least should be) a list type
                    if (theAttribute.isMultiple()) {
                        List<BusinessObjectLight> attributeValues = (List<BusinessObjectLight>)theObject.getAttributes().get(attributeName);
                        return attributeValues.stream().map(aListTypeItem -> aListTypeItem.getName())
                                .collect(Collectors.joining(";"));
                    } else
                        return ((BusinessObjectLight)theObject.getAttributes().get(attributeName)).getName();
            }
        }
    }
    
    @Override
    public HashMap<String, String> getAttributeValuesAsString (String objectClass, String objectId) 
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
                        res.put(attributeName, theObject.getAttributes().get(attributeName).toString());
                        break;
                    case "Date": //NOI18N
                    case "Time": //NOI18N
                    case "Timestamp": //NOI18N
                        res.put(attributeName, new Date((Long)theObject.getAttributes().get(attributeName)).toString());
                        break;
                    default: //It's (or at least should be) a list type
                        if (theAttribute.isMultiple()) {
                        List<BusinessObjectLight> attributeValues = (List<BusinessObjectLight>)theObject.getAttributes().get(attributeName);
                        res.put(attributeName, attributeValues.stream().map(aListTypeItem -> aListTypeItem.getName())
                                .collect(Collectors.joining(";")));
                    } else
                            res.put(attributeName, ((BusinessObjectLight)theObject.getAttributes().get(attributeName)).getName());
                }
            }
        }
        
        return res;
    }
    
    
    @Override
    public BusinessObjectLight getCommonParent(String aObjectClass, String aOid, String bObjectClass, String bOid)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        String cypherQuery = "MATCH (objectA:inventoryObjects)-[:CHILD_OF|CHILD_OF_SPECIAL*]->(parentNode)<-[:CHILD_OF|CHILD_OF_SPECIAL*]-(objectB:inventoryObjects) "
                + "WHERE objectA._uuid = {objectAId} AND objectB._uuid = {objectBId} RETURN parentNode";
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> parameters = new HashMap<>();
            parameters.put("objectAId", aOid);
            parameters.put("objectBId", bOid);
            Result queryResult = connectionManager.getConnectionHandler().execute(cypherQuery, parameters);
            
            if (!queryResult.hasNext()) //There is no common parent
                return null;
            
            Node commonParent = (Node)queryResult.next().get("parentNode");
            tx.success();
            if (Constants.DUMMY_ROOT.equals(commonParent.getProperty(Constants.PROPERTY_NAME)))
                return new BusinessObjectLight(Constants.DUMMY_ROOT, "", Constants.DUMMY_ROOT);
            else
                return new BusinessObjectLight((String)commonParent.getSingleRelationship(RelTypes.INSTANCE_OF, 
                        Direction.OUTGOING).getEndNode().getProperty(Constants.PROPERTY_NAME), (String)commonParent.getProperty(Constants.PROPERTY_UUID), 
                        (String)commonParent.getProperty(Constants.PROPERTY_NAME));
        }
    }
    
    @Override
    public BusinessObjectLight getParent(String objectClass, String oid) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(objectClass, oid);
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF)) {
                Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();

                //If the direct parent is DummyRoot, return a dummy RemoteBusinessObject with oid = -1
                if (parentNode.hasProperty(Constants.PROPERTY_NAME) && Constants.NODE_DUMMYROOT.equals(parentNode.getProperty(Constants.PROPERTY_NAME)) ) {
                    tx.success();
                    return new BusinessObject(Constants.NODE_DUMMYROOT, "-1", Constants.NODE_DUMMYROOT);
                }
                else {
                     tx.success();
                     return ogmService.createObjectLightFromNode(parentNode);
                }   
            }
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF_SPECIAL)) {
                Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                if (parentNode.hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING))
                    return ogmService.createObjectLightFromNode(parentNode);
                else
                    // Use the dummy root like parent to services, contracts, projects poolNode...
                    return new BusinessObject(Constants.NODE_DUMMYROOT, "-1", Constants.NODE_DUMMYROOT);
                
            }
            
            throw new InvalidArgumentException(String.format("The parent of %s (%s) could not be found", objectNode.getProperty(Constants.PROPERTY_NAME), oid));
        }
    }
    
    @Override
    public List<BusinessObjectLight> getParents (String objectClassName, String oid)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        List<BusinessObjectLight> parents =  new ArrayList<>();
        String cypherQuery = "MATCH (n)-[:" + RelTypes.CHILD_OF + "|" + RelTypes.CHILD_OF_SPECIAL + "*]->(m) " +
                             "WHERE n._uuid = '" + oid + "' " +
                             "RETURN m as parents";
      
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<Node> column = result.columnAs("parents");
            for (Node node : Iterators.asIterable(column)) {  
                if (node.hasProperty(Constants.PROPERTY_NAME)) {
                    if (node.getProperty(Constants.PROPERTY_NAME).equals(Constants.NODE_DUMMYROOT)) {
                        parents.add(new BusinessObjectLight(Constants.NODE_DUMMYROOT, "-1", Constants.NODE_DUMMYROOT));
                        continue;
                    }
                }
                if (node.hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING))
                    parents.add(ogmService.createObjectLightFromNode(node));
                else //the node has a poolNode as a parent
                    parents.add(Util.createRemoteObjectLightFromPoolNode(node));
            }
        }
        return parents;
    }

    
    @Override
    public List<BusinessObjectLight> getParentsUntilFirstOfClass(String objectClass, 
            String oid, String objectToMatchClassName) 
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        List<BusinessObjectLight> parents =  new ArrayList<>();
        String cypherQuery = "MATCH (n)-[:" + RelTypes.CHILD_OF + "|" + RelTypes.CHILD_OF_SPECIAL + "*]->(m) " +
                             "WHERE n._uuid = '" + oid + "' " +
                             "RETURN m as parents";
      
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<Node> column = result.columnAs("parents");
            for (Node node : Iterators.asIterable(column)) {
                
                Label label = Label.label(Constants.LABEL_ROOT); //If the parent node is the dummy root, just return null
                if (node.hasLabel(label))
                    break;
                
                if (node.hasProperty(Constants.PROPERTY_NAME)) {
                    if (node.getProperty(Constants.PROPERTY_NAME).equals(Constants.NODE_DUMMYROOT))
                        break;
                }
                
                if (node.hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING)) {                    
                    parents.add(ogmService.createObjectLightFromNode(node));
                    
                    String parentNodeClass = Util.getClassName(node);
                    if (mem.isSubclassOf(objectToMatchClassName, parentNodeClass))
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
    public BusinessObject getFirstParentOfClass(String objectClassName, String oid, String objectToMatchClassName)
        throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(objectClassName, oid);
            while (true) {
                Node parentNode = null;
                if (objectNode.hasRelationship(RelTypes.CHILD_OF, Direction.OUTGOING))
                    parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();
                
                if (objectNode.hasRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING))
                    parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).getEndNode();
                
                if (parentNode == null) //If the object is orphan
                    return null;
                
                try {
                    String parentNodeClass = Util.getClassName(parentNode);

                    if (mem.isSubclassOf(objectToMatchClassName, parentNodeClass))
                        return createObjectFromNode(parentNode);

                    objectNode = parentNode;
                } catch (MetadataObjectNotFoundException ex) { //If the parent object is either the Dummy Root or a pool
                    return null;
                }
            }
        }
    }
    
    @Override
    public boolean isParent(String parentClass, String parentId, String childClass, String childId) throws InvalidArgumentException {
        if (parentClass == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent.parent-class"));
        if (parentId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent.parent-id"));
        if (childClass == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent.child-class"));
        if (childId == null)
            throw new InvalidArgumentException(ts.getTranslatedString("apis.persistence.bem.messages.is-parent.child-id"));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            final String paramParentClass = "parentClass"; //NOI18N
            final String paramParentId = "parentId"; //NOI18N
            final String paramChildClass = "childId"; //NOI18N
            final String paramChildId = "childClass"; //NOI18N
            final String columnIsParent = "isParent"; //NOI18N
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH").append("\n"); //NOI18N
            queryBuilder.append(String.format("(parentClass:classes {name: $%s})", paramParentClass)); //NOI18N
            queryBuilder.append("<-[:INSTANCE_OF]-"); //NOI18N
            queryBuilder.append(String.format("(parent:inventoryObjects {_uuid: $%s})", paramParentId)); //NOI18N
            queryBuilder.append("<-[:CHILD_OF|:CHILD_OF_SPECIAL*]-"); //NOI18N
            queryBuilder.append(String.format("(child:inventoryObjects {_uuid: $%s})", paramChildId)); //NOI18N
            queryBuilder.append("-[:INSTANCE_OF]->"); //NOI18N
            queryBuilder.append(String.format("(childClass:classes {name: $%s})", paramChildClass)).append("\n"); //NOI18N
            queryBuilder.append("RETURN").append("\n"); //NOI18N
            queryBuilder.append(String .format("count(DISTINCT parent) = 1 AS %s", columnIsParent)); //NOI18N
            
            HashMap<String, Object> parameters = new HashMap();
            parameters.put(paramParentClass, parentClass);
            parameters.put(paramParentId, parentId);
            parameters.put(paramChildClass, childClass);
            parameters.put(paramChildId, childId);
            
            Result queryResult = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            
            return queryResult.hasNext() ? (boolean) queryResult.next().get(columnIsParent) : false;
        }
    }
    
    @Override
    public void deleteObjects(HashMap<String, List<String>> objects, boolean releaseRelationships)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            //TODO: Optimize so it can find all objects of a single class in one query
            for (String className : objects.keySet()) {
                for (String oid : objects.get(className)) {
                    ClassMetadata classMetadata = Util.createClassMetadataFromNode(connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className));
                    
                    if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, className))
                        throw new OperationNotPermittedException(String.format("Class %s is not a business-related class", className));

                    Node instance = getInstanceOfClass(className, oid);
                    
                    //Updates the unique attributes cache
                    try {
                        BusinessObject remoteObject = createObjectFromNode(instance);
                        for(AttributeMetadata attribute : classMetadata.getAttributes()) {
                            if (attribute.isUnique()) { 
                                Object attributeValue = remoteObject.getAttributes().get(attribute.getName());
                                if (attributeValue != null)
                                    CacheManager.getInstance().removeUniqueAttributeValue(className, attribute.getName(), attributeValue);
                            }
                        }
                    } catch (InvalidArgumentException ex) {
                        //Should not happen
                    }
                    deleteObject(instance, releaseRelationships);
                }
            }
            tx.success();
        }
    }

    @Override
    public void deleteObject(String className, String oid, boolean releaseRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {
        HashMap<String, List<String>> objectsToDelete = new HashMap<>();
        objectsToDelete.put(className, Arrays.asList(oid));
        deleteObjects(objectsToDelete, releaseRelationships);
    }

    @Override
    public ChangeDescriptor updateObject(String className, String oid, HashMap<String, String> attributes)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {
        ClassMetadata classMetadata = mem.getClass(className);
        if (classMetadata == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node instance = getInstanceOfClass(className, oid);

            ChangeDescriptor changes = updateObject(instance, classMetadata, attributes);
            tx.success();
            
            return changes;
        }
    }
    
    @Override
    public void createSpecialRelationship(String aObjectClass, String aObjectId, String bObjectClass, String bObjectId, String name, boolean unique)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        createSpecialRelationship(aObjectClass, aObjectId, bObjectClass, bObjectId, name, unique, new HashMap<>());
    }
    
    @Override
    public void createSpecialRelationship(String aObjectClass, String aObjectId, String bObjectClass, 
        String bObjectId, String name, boolean unique, HashMap<String, Object> properties) 
        throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (aObjectId != null && bObjectId != null && aObjectId.equals(bObjectId))
            throw new OperationNotPermittedException("An object can not be related to itself");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node nodeA = getInstanceOfClass(aObjectClass, aObjectId);
            for (Relationship rel : nodeA.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                String otherNodeUuid = rel.getOtherNode(nodeA).hasProperty(Constants.PROPERTY_UUID) ? (String) rel.getOtherNode(nodeA).getProperty(Constants.PROPERTY_UUID) : null;
                
                if (otherNodeUuid != null && otherNodeUuid.equals(bObjectId) 
                    && rel.getProperty(Constants.PROPERTY_NAME).equals(name) && unique)
                    throw new OperationNotPermittedException("These elements are already related");
            }
            
            Node nodeB = getInstanceOfClass(bObjectClass, bObjectId);
            Relationship rel = nodeA.createRelationshipTo(nodeB, RelTypes.RELATED_TO_SPECIAL);
            rel.setProperty(Constants.PROPERTY_NAME, name);
            
            //Custom properties
            properties.keySet().forEach((property) -> {
                rel.setProperty(property, properties.get(property));
            });
            
            tx.success();
        }
    }
    
    @Override
    public void releaseSpecialRelationship(String objectClass, String objectId, String otherObjectId, String name)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (otherObjectId == null)
            throw new InvalidArgumentException("The other object id cannot be null");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node node = getInstanceOfClass(objectClass, objectId);
            for (Relationship rel : node.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                String otherNodeUuid = rel.getOtherNode(node).hasProperty(Constants.PROPERTY_UUID) ? rel.getOtherNode(node).getProperty(Constants.PROPERTY_UUID).toString() : null;
                
                if ((rel.getProperty(Constants.PROPERTY_NAME).equals(name) && 
                        (otherNodeUuid != null && otherNodeUuid.equals(otherObjectId)) || otherObjectId.equals("-1")))
                    rel.delete();
            }
            tx.success();
        }
    }
    
    @Override
    public void releaseSpecialRelationshipInTargetObject(String objectClass, String objectId, String relationshipName, String targetId)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node node = getInstanceOfClass(objectClass, objectId);
            for (Relationship rel : node.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO_SPECIAL)) {
                
                String endNodeUuid = rel.getEndNode().hasProperty(Constants.PROPERTY_UUID) ? rel.getEndNode().getProperty(Constants.PROPERTY_UUID).toString() : null;
                
                if (rel.getProperty(Constants.PROPERTY_NAME).equals(relationshipName) &&
                            endNodeUuid != null && endNodeUuid.equals(targetId))
                    rel.delete();
            }
            tx.success();
        }
    }
    
    @Override
    public void moveObjectsToPool(String targetClassName, String targetOid, HashMap<String, String[]> objects)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);
        
        boolean isPool = true;
        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newParentNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, targetOid);
            
            if (newParentNode == null) {
                isPool = false;
                
                newParentNode = connectionManager.getConnectionHandler().findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, targetOid);
                
                if (newParentNode == null)
                    throw new BusinessObjectNotFoundException(targetClassName, targetOid);
            }
            
            for (String myClass : objects.keySet()) {
                Node instanceClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (String oid : objects.get(myClass)) {
                    Node instance = getInstanceOfClass(instanceClassNode, oid);
                    //If the object was specialChild of a poolNode
                    if (instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().hasNext()) {
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    if (isPool)
                        instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
                    else
                        instance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL);
                }
            }
            tx.success();
        }
    }

    @Override
    public void moveObjects(String targetClassName, String targetOid, HashMap<String, String[]> objects)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);
        
        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            for (String myClass : objects.keySet()) {
                if (!mem.canBeChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be child of an instance of class %s", myClass,targetClassName));

                Node instanceClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (String oid : objects.get(myClass)) {
                    Node instance = getInstanceOfClass(instanceClassNode, oid);
                    if (instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().hasNext()) {
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    //If the object was specialChild of a poolNode
                    if (instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().hasNext()) {
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
    public void moveSpecialObjects(String targetClassName, String targetOid, HashMap<String, String[]> objects)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);
        
        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            for (String myClass : objects.keySet()) {
                //check if can be special child only if is not a physical connection, 
                //this is to allow moving physical links in and out of the wire containers, without modifying the hierarchy containment
                if (!mem.isSubclassOf(Constants.CLASS_PHYSICALCONNECTION, myClass)) {
                    if (!mem.canBeSpecialChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be special child of an instance of class %s", myClass,targetClassName));
                }
                
                Node instanceClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (String oid : objects.get(myClass)) {
                    Node instance = getInstanceOfClass(instanceClassNode, oid);
                    
                    if (instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().hasNext()) {
                        Relationship rel = instance.getRelationships(RelTypes.CHILD_OF, Direction.OUTGOING).iterator().next();
                        rel.delete();
                    }
                    
                    if (instance.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).iterator().hasNext()) {
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
    public void movePoolItem(String poolId, String poolItemClassName, String poolItemId) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, 
        MetadataObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);
            
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
            
            if (!poolNode.hasProperty(Constants.PROPERTY_CLASS_NAME))
                throw new InvalidArgumentException("This pool has not set his class name attribute");
            
            Node poolItemClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, poolItemClassName);
            
            if (poolItemClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", poolItemClassName));
            
            if (!mem.isSubclassOf((String) poolNode.getProperty(Constants.PROPERTY_CLASS_NAME), poolItemClassName))
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
    public String[] copyObjects(String targetClassName, String targetOid, HashMap<String, String[]> objects, boolean recursive)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);

        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            String[] res = new String[objects.size()];
            int i = 0;
            for (String myClass : objects.keySet()) {
                if (!mem.canBeChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be child of an instance of class %s", myClass,targetClassName));

                Node instanceClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (String oid : objects.get(myClass)) {
                    Node templateObject = getInstanceOfClass(instanceClassNode, oid);
                    Node newInstance = copyObject(templateObject, recursive);
                    newInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF);
                    String newInstanceUuid = newInstance.hasProperty(Constants.PROPERTY_UUID) ? (String) newInstance.getProperty(Constants.PROPERTY_UUID) : null;
                    if (newInstanceUuid == null)
                        throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", newInstance.getId()));                        
                    res[i] = newInstanceUuid;
                    i++;            
                }
            }
            tx.success();
            return res;
        }        
    }
    
    @Override
    public String[] copySpecialObjects(String targetClassName, String targetOid, HashMap<String, String[]> objects, boolean recursive)
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        ClassMetadata newParentClass = mem.getClass(targetClassName);

        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", targetClassName));

        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            String[] res = new String[objects.size()];
            int i = 0;
            for (String myClass : objects.keySet()) {
                if (!mem.canBeSpecialChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be special child of an instance of class %s", myClass,targetClassName));

                Node instanceClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, myClass);
                
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", myClass));
                for (String oid : objects.get(myClass)) {
                    Node templateObject = getInstanceOfClass(instanceClassNode, oid);
                    Node newInstance = copyObject(templateObject, recursive);
                    newInstance.createRelationshipTo(newParentNode, RelTypes.CHILD_OF_SPECIAL);
                    String newInstanceUuid = newInstance.hasProperty(Constants.PROPERTY_UUID) ? (String) newInstance.getProperty(Constants.PROPERTY_UUID) : null;
                    if (newInstanceUuid == null)
                        throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", newInstance.getId()));
                    res[i] = newInstanceUuid;
                    i++;            
                }
            }
            tx.success();
            return res;
        }        
    }
    
    @Override
    public String copyPoolItem(String poolId, String poolItemClassName, String poolItemId, boolean recursive) throws 
        ApplicationObjectNotFoundException, InvalidArgumentException, BusinessObjectNotFoundException, 
        MetadataObjectNotFoundException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node poolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);
            
            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s could not be found", poolId));
            
            if (!poolNode.hasProperty(Constants.PROPERTY_CLASS_NAME))
                throw new InvalidArgumentException("This pool has not set his class name attribute");
            
            Node poolItemClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, poolItemClassName);
            
            if (poolItemClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", poolItemClassName));
            
            if (!mem.isSubclassOf((String) poolNode.getProperty(Constants.PROPERTY_CLASS_NAME), poolItemClassName))
                throw new InvalidArgumentException(String.format("Class %s is not subclass of %s", poolItemClassName, (String) poolNode.getProperty(Constants.PROPERTY_CLASS_NAME)));
                                    
            Node instance = getInstanceOfClass(poolItemClassNode, poolItemId);
            
            Node newInstance = copyObject(instance, recursive);
            String newInstanceUuid = newInstance.hasProperty(Constants.PROPERTY_UUID) ? (String) newInstance.getProperty(Constants.PROPERTY_UUID) : null;
            if (newInstanceUuid == null)
                throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", newInstance.getId()));
            newInstance.createRelationshipTo(poolNode, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);            
            tx.success();
            return newInstanceUuid;
        }
    }

    @Override
    public List<BusinessObjectLight> getObjectChildren(String className, String oid, int maxResults)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException  {
        try (Transaction tx =  connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode;
            if (oid != null && oid.equals("-1"))
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else
                parentNode = getInstanceOfClass(className, oid);
            
            Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            Iterator<Relationship> instances = children.iterator();
            List<BusinessObjectLight> res = new ArrayList<>();

            if (maxResults > 0) {
                int counter = 0;
                while(children.iterator().hasNext() && (counter < maxResults)) {
                    counter++;
                    Node child = children.iterator().next().getStartNode();
                    res.add(ogmService.createObjectLightFromNode(child));
                }
            } else {
                while(instances.hasNext()) {
                    Node child = instances.next().getStartNode();
                    res.add(ogmService.createObjectLightFromNode(child));
                }
            }
            
            Collections.sort(res);
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectChildren(long classId, String oid, int maxResults)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode;
            if (oid != null && oid.equals("-1"))
                parentNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            else
                parentNode = getInstanceOfClass(classId, oid);
            
            Iterable<Relationship> iterableChildren = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            Iterator<Relationship> children = iterableChildren.iterator();
            List<BusinessObjectLight> res = new ArrayList<>();
            if (maxResults > 0) {
                int counter = 0;
                while(children.hasNext() && (counter < maxResults)) {
                    counter++;
                    Node child = children.next().getStartNode();
                    res.add(ogmService.createObjectLightFromNode(child));
                }
            } else {
                while(children.hasNext()) {
                    Node child = children.next().getStartNode();
                    res.add(ogmService.createObjectLightFromNode(child));
                }
            }
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSiblings(String className, String oid, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node node = getInstanceOfClass(className, oid);
            List<BusinessObjectLight> res = new ArrayList<>();
            
            if (!node.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF))
                return res;
            
            Node parentNode = node.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();
            
            int resultCounter = 0;
            for (Relationship rel : parentNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF)) {
                if (maxResults > 0) {
                    if (resultCounter < maxResults)
                        resultCounter ++;
                    else
                        break;
                }
                
                Node child = rel.getStartNode();
                
                String childUuid = child.hasProperty(Constants.PROPERTY_UUID) ? (String) child.getProperty(Constants.PROPERTY_UUID) : null;
                if (childUuid == null)
                    throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", child.getId()));
                
                if (childUuid.equals(oid))
                    continue;
                
                res.add(ogmService.createObjectLightFromNode(child));
            }
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectsOfClassLight(String className, int maxResults)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classMetadataNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(className);

            List<BusinessObjectLight> instances = new ArrayList<>();
            int counter = 0;
            
            boolean isAbstract = (Boolean) classMetadataNode.getProperty(Constants.PROPERTY_ABSTRACT);
            
            String cypherQuery;
            
            if (isAbstract) 
                cypherQuery = "MATCH (class:classes)<-[:EXTENDS*]-(subclass:classes)<-[:INSTANCE_OF]-(instance:" + Constants.LABEL_INVENTORY_OBJECTS + ") "
                            + "WHERE class.name=\"" + className + "\" "
                            + "RETURN instance;";                
            else 
                cypherQuery = "MATCH (class:classes)<-[:INSTANCE_OF]-(instance:" + Constants.LABEL_INVENTORY_OBJECTS + ") "
                            + "WHERE class.name=\"" + className + "\" "
                            + "RETURN instance;";
            
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            ResourceIterator<Node> instanceColumn = result.columnAs("instance");
            List<Node> lstInstanceColumn = Iterators.asList(instanceColumn);
            
            for (Node instance : lstInstanceColumn) {
                if (maxResults > 0) {
                    if (counter < maxResults)
                        counter ++;
                    else break;
                }
                instances.add(ogmService.createObjectLightFromNode(instance));                                                                                
            }
            
            Collections.sort(instances);
            tx.success();
            return instances;
        }
    }
    
    @Override
    public List<BusinessObject> getObjectsOfClass(String className, int maxResults)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classMetadataNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(className);
                                                
            List<BusinessObject> instances = new ArrayList<>();
            int counter = 0;
            
            boolean isAbstract = (Boolean) classMetadataNode.getProperty(Constants.PROPERTY_ABSTRACT);
            
            String cypherQuery;
            
            if (isAbstract) {
                cypherQuery = "MATCH (class:classes)<-[:EXTENDS*]-(subclass:classes)<-[:INSTANCE_OF]-(instance:" + Constants.LABEL_INVENTORY_OBJECTS + ") "
                            + "WHERE class.name=\"" + className + "\" "
                            + "RETURN instance;";                
            } else {
                cypherQuery = "MATCH (class:classes)<-[:INSTANCE_OF]-(instance:" + Constants.LABEL_INVENTORY_OBJECTS + ") "
                            + "WHERE class.name=\"" + className + "\" "
                            + "RETURN instance;";
            }
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            ResourceIterator<Node> instanceColumn = result.columnAs("instance");
            List<Node> lstInstanceColumn = Iterators.asList(instanceColumn);
            
            for (Node instance : lstInstanceColumn) {
                if (maxResults > 0) {
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
    public List<BusinessObject> getChildrenOfClass(String parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode = getInstanceOfClass(parentClass, parentOid);
            Iterable<Relationship> iterableChildren = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            Iterator<Relationship> children = iterableChildren.iterator();
            List<BusinessObject> res = new ArrayList<>();
            int counter = 0;

            while(children.hasNext()) {
                Node child = children.next().getStartNode();

                if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                    throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found",child.getId()));

                Node classNode = child.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode();

                ClassMetadata classMetadata = mem.getClass((String)classNode.getProperty(Constants.PROPERTY_NAME));
                if (mem.isSubclassOf(classToFilter, classMetadata.getName())) {
                    res.add(createObjectFromNode(child, classMetadata));
                    if (maxResults > 0) {
                        if (++counter == maxResults)
                            break;
                    }
                }
            }

            if (maxResults > 0 && counter == maxResults)
                return res;

            Iterable<Relationship> iterableSpecialChildren = parentNode.getRelationships(RelTypes.CHILD_OF_SPECIAL,Direction.INCOMING);
            Iterator<Relationship> specialChildren = iterableSpecialChildren.iterator();

            while(specialChildren.hasNext()) {
                Node child = specialChildren.next().getStartNode();

                if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                    throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found",child.getId()));

                Node classNode = child.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode();

                ClassMetadata classMetadata = mem.getClass((String)classNode.getProperty(Constants.PROPERTY_NAME));
                if (mem.isSubclassOf(classToFilter, classMetadata.getName())) {
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
    public List<BusinessObjectLight> getSpecialChildrenOfClassLight(String parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode = getInstanceOfClass(parentClass, parentOid);
            
            List<BusinessObjectLight> res = new ArrayList<>();
            int counter = 0;
            
            for (Relationship specialChildRelationships : parentNode.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING)) {
                BusinessObjectLight specialChild = ogmService.createObjectLightFromNode(specialChildRelationships.getStartNode());
                
                if (mem.isSubclassOf(classToFilter, specialChild.getClassName())) {
                    res.add(specialChild);
                    if (maxResults > 0 && ++counter == maxResults)
                        break;
                }
            }
            tx.success();
            return res;
        }
    }
    
    
    @Override
    public List<BusinessObjectLight> getChildrenOfClassLightRecursive(String parentOid, String parentClass, String classToFilter, int maxResults) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        List<BusinessObjectLight> res = new ArrayList<>();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            getChildrenOfClassRecursive(parentOid, parentClass, classToFilter, maxResults, res);
            Collections.sort(res);
            tx.success();
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getSpecialChildrenOfClassLightRecursive(String parentOid, String parentClass, String classToFilter, int maxResults) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        List<BusinessObjectLight> res = new ArrayList<>();
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            getSpecialChildrenOfClassRecursive(getInstanceOfClass(parentClass, parentOid), classToFilter, maxResults, res);
            Collections.sort(res);
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getChildrenOfClassLight(String parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException  {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node parentNode = getInstanceOfClass(parentClass, parentOid);
            List<BusinessObjectLight> res = new ArrayList<>();

            int counter = 0;

            for (Relationship childOfRelationship : parentNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF, RelTypes.CHILD_OF_SPECIAL)) {
                Node child = childOfRelationship.getStartNode();

                if (!child.getRelationships(RelTypes.INSTANCE_OF).iterator().hasNext())
                    throw new MetadataObjectNotFoundException(String.format("Class for %s (%s) could not be found", 
                            child.getProperty(Constants.PROPERTY_NAME), child.getId()));

                String className = Util.getClassName(child);
                if (mem.isSubclassOf(classToFilter, className)) {
                    String childUuid = child.hasProperty(Constants.PROPERTY_UUID) ? (String) child.getProperty(Constants.PROPERTY_UUID) : null;
                    if (childUuid == null)                                        
                        throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", child.getId()));
                    res.add(new BusinessObjectLight(className, childUuid, (String)child.getProperty(Constants.PROPERTY_NAME)));
                    if (maxResults > 0) {
                        if (++counter == maxResults)
                            break;
                    }
                }
            }
            
            tx.success();
            return res;
        }
    }

    @Override
    public List<BusinessObjectLight> getSpecialAttribute(String objectClass, String objectId, String specialAttributeName) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node instance = getInstanceOfClass(objectClass, objectId);
            List<BusinessObjectLight> res = new ArrayList<>();
            for (Relationship rel : instance.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                if (rel.hasProperty(Constants.PROPERTY_NAME)) {
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(specialAttributeName)) {
                        //String uuid = instance.hasProperty(Constants.PROPERTY_UUID) ? (String) instance.getProperty(Constants.PROPERTY_UUID) : null;
                        String endNodeUuid = rel.getEndNode().hasProperty(Constants.PROPERTY_UUID) ? (String) rel.getEndNode().getProperty(Constants.PROPERTY_UUID) : null;
                        if (endNodeUuid == null)
                            throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", rel.getEndNode()));
                        res.add(endNodeUuid.equals(objectId) ? 
                            ogmService.createObjectLightFromNode(rel.getStartNode()) : ogmService.createObjectLightFromNode(rel.getEndNode()));
                    }
                }
            }
            tx.success();
            return res;
        }
    }

    @Override
    public List<AnnotatedBusinessObjectLight> getAnnotatedSpecialAttribute(String objectClass, String objectId, String specialAttributeName) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node instance = getInstanceOfClass(objectClass, objectId);
            List<AnnotatedBusinessObjectLight> res = new ArrayList<>();
            for (Relationship rel : instance.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                if (rel.hasProperty(Constants.PROPERTY_NAME)) {
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(specialAttributeName)) {
                        String endNodeUuid = rel.getEndNode().hasProperty(Constants.PROPERTY_UUID) ? (String) rel.getEndNode().getProperty(Constants.PROPERTY_UUID) : null;
                        if (endNodeUuid == null)
                            throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", rel.getEndNode().getId()));                                
                        BusinessObjectLight theObject = endNodeUuid.equals(objectId) ? 
                            ogmService.createObjectLightFromNode(rel.getStartNode()) : ogmService.createObjectLightFromNode(rel.getEndNode());
                        res.add(new AnnotatedBusinessObjectLight(theObject, rel.getAllProperties()));
                    }
                }
            }
            return res;
        }
    }

    @Override
    public HashMap<String, List<BusinessObjectLight>> getSpecialAttributes(String className, String objectId, String... attributeNames) 
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        HashMap<String,List<BusinessObjectLight>> res = new HashMap<>();
        List<String> attributeNamesAsList = Arrays.asList(attributeNames);
        boolean returnAll = attributeNames.length == 0;
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);
            for (Relationship rel : objectNode.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                String relName = (String)rel.getProperty(Constants.PROPERTY_NAME);
                if (attributeNamesAsList.contains(relName) || returnAll) {
                    List<BusinessObjectLight> currentObjects = res.get(relName);
                    if (currentObjects == null) {
                        currentObjects = new ArrayList<>();
                        res.put(relName, currentObjects);
                    }
                    currentObjects.add(ogmService.createObjectLightFromNode(rel.getOtherNode(objectNode)));
                }
            }
            return res;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectSpecialChildren(String objectClass, String objectId)
            throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException  {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node instance = getInstanceOfClass(objectClass, objectId);
            List<BusinessObjectLight> res = new ArrayList<>();
            for (Relationship rel : instance.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                if (rel.hasProperty(Constants.PROPERTY_NAME)) {
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(Constants.REL_PROPERTY_POOL))
                        return res;
                }
                res.add(ogmService.createObjectLightFromNode(rel.getStartNode()));
            }
            return res;
        }
    }

    @Override
    public boolean hasRelationship(String objectClass, String objectId, String relationshipName, int numberOfRelationships)
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node object = getInstanceOfClass(objectClass, objectId);
            int relationshipsCounter = 0;
            for (Relationship rel : object.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                if (rel.getProperty(Constants.PROPERTY_NAME).equals(relationshipName))
                    relationshipsCounter++;
                if (relationshipsCounter == numberOfRelationships)
                    return true;
            }
            return false;
        }
    }
    
    @Override
    public boolean hasSpecialAttribute(String objectClass, String objectId, String attributeName) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        if (objectId == null)
            throw new InvalidArgumentException("The object id cannot be null");
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("className", objectClass); //NOI18N
            params.put("objectId", objectId); //NOI18N
            params.put("attributeName", attributeName); //NOI18N
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (class:classes {name: $className})<-[:INSTANCE_OF]-(object:inventoryObjects {_uuid: $objectId})-[:RELATED_TO_SPECIAL {name: $attributeName}]-() "); //NOI18N
            queryBuilder.append("RETURN count(*) > 0 AS hasSpecialAtribute"); //NOI18N
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), params);
            while (result.hasNext()) {
                tx.success();
                return (Boolean) result.next().get("hasSpecialAtribute"); //NOI18N
            }
            tx.success();
            throw new BusinessObjectNotFoundException(objectClass, objectId);
        }
    }
    
    @Override
    public long countChildren(String objectClass, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        if (objectId == null)
            throw new InvalidArgumentException("The object id cannot be null");
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("className", objectClass); //NOI18N
            params.put("objectId", objectId); //NOI18N
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (class:classes {name: $className})<-[:INSTANCE_OF]-(object:inventoryObjects {_uuid: $objectId})<-[:CHILD_OF]-() "); //NOI18N
            queryBuilder.append("RETURN count(*) AS children"); //NOI18N
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), params);
            while (result.hasNext()) {
                tx.success();
                return (long) result.next().get("children"); //NOI18N
            }
            tx.success();
            throw new BusinessObjectNotFoundException(objectClass, objectId);
        }
    }
    
    @Override
    public long countSpecialChildren(String objectClass, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        if (objectId == null)
            throw new InvalidArgumentException("The object id cannot be null");
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("className", objectClass); //NOI18N
            params.put("objectId", objectId); //NOI18N
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("MATCH (class:classes {name:$className})<-[:INSTANCE_OF]-(object:inventoryObjects {_uuid:$objectId})<-[:CHILD_OF_SPECIAL]-() "); //NOI18N
            queryBuilder.append("RETURN count(*) AS specialChildren"); //NOI18N
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), params);
            while (result.hasNext()) {
                tx.success();
                return (long) result.next().get("specialChildren"); //NOI18N
            }
            tx.success();
            throw new BusinessObjectNotFoundException(objectClass, objectId);
        }
    }
    
    @Override
    public void releaseRelationships(String objectClass, String objectId, List<String> relationshipsToRelease) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node object = getInstanceOfClass(objectClass, objectId);
            
            for (Relationship rel : object.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
                if (relationshipsToRelease.contains((String)rel.getProperty(Constants.PROPERTY_NAME)))
                    rel.delete();
            }
            tx.success();
        }
    }
    
    @Override
    public boolean hasSpecialRelationship(String objectClass, String objectId, String relationshipName, int numberOfRelationships) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException  {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node object = getInstanceOfClass(objectClass, objectId);
            int relationshipsCounter = 0;
            for (Relationship rel : object.getRelationships(RelTypes.RELATED_TO_SPECIAL)) {
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
    public String createContact(String contactClass, List<StringPair> properties, String customerClassName, String customerId) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        
        if (!mem.isSubclassOf("GenericCustomer", customerClassName)) //NOI18N
            throw new InvalidArgumentException(String.format("Class %s is not a subclass of GenericCustomer", customerClassName));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node contactClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, contactClass);
            if (contactClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", contactClass));
            
            Node customerNode = getInstanceOfClass(customerClassName, customerId);
            Node newContactNode = connectionManager.getConnectionHandler().createNode(contactsLabel, inventoryObjectLabel);
            newContactNode.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());
            
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
            newContactRelationship.setProperty(Constants.PROPERTY_NAME, "contacts");
            
            newContactNode.createRelationshipTo(contactClassNode, RelTypes.INSTANCE_OF);
                        
            String newContactNodeUuid = newContactNode.hasProperty(Constants.PROPERTY_UUID) ? (String) newContactNode.getProperty(Constants.PROPERTY_UUID) : null;
            if (newContactNodeUuid == null)
                throw new InvalidArgumentException(String.format("The contact with id %s does not have uuid", newContactNode.getId()));                
            
            tx.success();
            return newContactNodeUuid;
        }
    }

    @Override
    public void updateContact(String contactClass, String contactId, List<StringPair> properties) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
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
    public void deleteContact(String contactClass, String contactId) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node contactNode = getInstanceOfClass(contactClass, contactId);
            
            for (Relationship contactRelationship : contactNode.getRelationships())
                contactRelationship.delete();
            
            contactNode.delete();
            
            tx.success();            
        }
    }

    @Override
    public Contact getContact(String contactClass, String contactId) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (!mem.isSubclassOf("GenericContact", contactClass))
            throw new InvalidArgumentException(String.format("Class %s is not an instance of GenericContact", contactClass));
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node contactNode = getInstanceOfClass(contactClass, contactId);
            
            for (Relationship customerRelationship : contactNode.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.INCOMING)) {
                if (customerRelationship.hasProperty(Constants.PROPERTY_NAME) && customerRelationship.getProperty(Constants.PROPERTY_NAME).equals("contacts"))
                    return new Contact(createObjectFromNode(contactNode), ogmService.createObjectLightFromNode(customerRelationship.getStartNode()));
            }
            
            throw new InvalidArgumentException("The contact does not have a customer associated to it, please check its relationships");
        }
    }

    @Override
    public List<Contact> getContactsForCustomer(String customerClass, String customerId) 
            throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node customerNode = getInstanceOfClass(customerClass, customerId);
            List<Contact> contacts = new ArrayList<>();
            for (Relationship contactRelationship : customerNode.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.OUTGOING)) {
                if (contactRelationship.hasProperty(Constants.PROPERTY_NAME) && contactRelationship.getProperty(Constants.PROPERTY_NAME).equals("contacts")) {
                    Node contactNode = contactRelationship.getEndNode();
                    contacts.add(new Contact(createObjectFromNode(contactNode), ogmService.createObjectLightFromNode(customerNode)));
                }
            }
            return contacts;
        }
    }

    //TODO: Optimize and improve validations!
    @Override
    public List<Contact> searchForContacts(String searchString, int maxResults) throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
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
            Result rawQueryResult = connectionManager.getConnectionHandler().execute(cypherQuery, parameters);
            ResourceIterator<Node> contactNodes = rawQueryResult.columnAs("contact");
            
            while (contactNodes.hasNext()) {
                Node contactNode = contactNodes.next();
                if (contactNode.hasRelationship(RelTypes.RELATED_TO_SPECIAL, Direction.INCOMING)) {
                    Node customerNode = contactNode.getRelationships(RelTypes.RELATED_TO_SPECIAL, Direction.INCOMING).iterator().next().getStartNode();
                    res.add(new Contact(createObjectFromNode(contactNode), ogmService.createObjectLightFromNode(customerNode)));
                }
            }
            return res;
        }
    }

    @Override
    public long attachFileToObject(String name, String tags, byte[] file, String className, String objectId) 
            throws BusinessObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        if (file.length > (float)configuration.get("maxAttachmentSize") * 1048576) //Size converted to MB
            throw new InvalidArgumentException(String.format("The file size exceeds the maximum size allowed (%s MB)", configuration.get("maxAttachmentSize")));
        
        if (name == null || name.trim().isEmpty())
            throw new InvalidArgumentException("The file name can not be an empty string");
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);
            
            Node fileObjectNode = connectionManager.getConnectionHandler().createNode(Label.label(Constants.LABEL_ATTACHMENTS));
            fileObjectNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
            fileObjectNode.setProperty(Constants.PROPERTY_NAME, name);
            fileObjectNode.setProperty(Constants.PROPERTY_TAGS, tags == null ? "" : tags);
            
            Relationship hasAttachmentRelationship = objectNode.createRelationshipTo(fileObjectNode, RelTypes.HAS_ATTACHMENT);
            hasAttachmentRelationship.setProperty(Constants.PROPERTY_NAME, "attachments");
            
            String fileName = objectNode.getProperty(Constants.PROPERTY_UUID) + "_" + fileObjectNode.getId();
            Util.saveFile((String)configuration.get("attachmentsPath"), fileName, file);
            
            tx.success();
            return fileObjectNode.getId();
        } catch(IOException ex) {
            throw new OperationNotPermittedException(ex.getMessage());
        }
    }

    @Override
    public List<FileObjectLight> getFilesForObject(String className, String objectId) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
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
    public FileObject getFile(long fileObjectId, String className, String objectId) throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);

            for (Relationship fileObjectRelationship : objectNode.getRelationships(RelTypes.HAS_ATTACHMENT, Direction.OUTGOING)) {
                if (fileObjectRelationship.getEndNode().getId() == fileObjectId) {
                    String fileName = objectNode.getProperty(Constants.PROPERTY_UUID) + "_" + fileObjectId;
                    try {
                        byte[] background = Util.readBytesFromFile(configuration.getProperty("attachmentsPath", DEFAULT_ATTACHMENTS_PATH) + "/" + fileName);
                        return new FileObject(fileObjectId, (String)fileObjectRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME), 
                                                    (String)fileObjectRelationship.getEndNode().getProperty(Constants.PROPERTY_TAGS), 
                                                    (long)fileObjectRelationship.getEndNode().getProperty(Constants.PROPERTY_CREATION_DATE), 
                                                    background);
                    }catch(IOException ex) {
                        throw new InvalidArgumentException(String.format("File with id %s could not be retrieved: %s", fileObjectId, ex.getMessage()));
                    }
                }
            }
            throw new InvalidArgumentException(String.format("The file with id %s could not be found", fileObjectId));
        }
    }

    @Override
    public void detachFileFromObject(long fileObjectId, String className, String objectId) 
            throws BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);

            for (Relationship fileObjectRelationship : objectNode.getRelationships(RelTypes.HAS_ATTACHMENT, Direction.OUTGOING)) {
                if (fileObjectRelationship.getEndNode().getId() == fileObjectId) {
                    fileObjectRelationship.delete();
                    fileObjectRelationship.getEndNode().delete();
                    
                    try {
                        String fileName = objectNode.getProperty(Constants.PROPERTY_UUID) + "_" + fileObjectId;
                        new File(configuration.getProperty("attachmentsPath", DEFAULT_ATTACHMENTS_PATH) + File.separator + fileName).delete();
                    } catch(Exception ex) {
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
    public void updateFileProperties(long fileObjectId, List<StringPair> properties, String className, String objectId) throws BusinessObjectNotFoundException, ApplicationObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
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
    
    @Override
    public List<BusinessObjectLightList> findRoutesThroughSpecialRelationships(String objectAClassName, 
            String objectAId, String objectBClassName, String objectBId, String relationshipName) throws InvalidArgumentException {
        List<BusinessObjectLightList> paths = new ArrayList<>();

        String cypherQuery = String.format("MATCH path = (a)-[:%s*1..30{name:\"%s\"}]-(b) " +
                            "WHERE a._uuid = '%s' AND b._uuid = '%s' " +
                            "RETURN nodes(path) as path LIMIT %s", RelTypes.RELATED_TO_SPECIAL, relationshipName, objectAId, objectBId, 
                                                                    aem.getConfiguration().get("maxRoutes")); //NOI18N
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
           
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            Iterator<List<Node>> column = result.columnAs("path");
            
            //Filtering the routes with repeated nodes didn't work using a cypher query, so we do it here
            //at least while we figure out a working cypher query
            for (List<Node> list : Iterators.asIterable(column)) {
                BusinessObjectLightList aPath = new BusinessObjectLightList();
                boolean discardPath = false;
                for (Node aNode : list) {
                    BusinessObjectLight aHop = ogmService.createObjectLightFromNode(aNode);
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
    public List<BusinessObjectLight> getWarehousesInObject(String objectClassName, String objectId) 
        throws MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, objectClassName);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(objectClassName);
            
            List<BusinessObjectLight> warehouses = new ArrayList();
                                    
            String cypherQuery = ""
                + "MATCH (warehouse:inventoryObjects)-[:RELATED_TO_SPECIAL{ name: 'warehouseHas' }]-(child:inventoryObjects)-[:CHILD_OF*]->(parent:inventoryObjects)-[:INSTANCE_OF]->(class:classes{name: '" + objectClassName + "'}) "
                + "WHERE parent._uuid = '" + objectId + "' RETURN warehouse;";
            
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            ResourceIterator<Node> warehouseColumn = result.columnAs("warehouse");
            List<Node> lstWarehouseColumn = Iterators.asList(warehouseColumn);
            
            for (Node warehouse : lstWarehouseColumn)
                warehouses.add(ogmService.createObjectLightFromNode(warehouse));
            
            Collections.sort(warehouses);
            
            tx.success();
            return warehouses;
        }
    }
    @Override
    public BusinessObjectLight getWarehouseToObject(String objectClassName, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, objectClassName);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(objectClassName);
            
            getObjectLight(objectClassName, objectId);
            
            List<BusinessObjectLight> warehouses = new ArrayList();
                                                
            String cypherQuery = ""
                + "MATCH (class{name:'" + objectClassName + "'})<-[:INSTANCE_OF]-(inventoryObject)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(pool)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(warehouse) "
                + "WHERE inventoryObject._uuid = '" + objectId + "' "
                + "RETURN warehouse;";
            
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            ResourceIterator<Node> warehouseColumn = result.columnAs("warehouse");
            List<Node> lstWarehouseColumn = Iterators.asList(warehouseColumn);
            
            for (Node warehouse : lstWarehouseColumn)
                warehouses.add(ogmService.createObjectLightFromNode(warehouse));
            
            Collections.sort(warehouses);
            
            tx.success();
            if (warehouses.size() > 0)
                return warehouses.get(0);
            return null;
        }
    }
    
    @Override
    public BusinessObjectLight getPhysicalNodeToObjectInWarehouse(String objectClassName, String objectId) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, objectClassName);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(objectClassName);
            
            getObjectLight(objectClassName, objectId);
            
            List<BusinessObjectLight> physicalNodes = new ArrayList();
                                    
            String cypherQuery = ""
                + "MATCH (class{name:'" + objectClassName + "'})<-[:INSTANCE_OF]-(inventoryObject)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(pool)-[:CHILD_OF_SPECIAL{name: 'pool'}]->(warehouse)-[:RELATED_TO_SPECIAL{name: 'warehouseHas'}]->(physicalNode) "
                + "WHERE inventoryObject._uuid = '" + objectId + "' "
                + "RETURN physicalNode;";
            
            Result result = connectionManager.getConnectionHandler().execute(cypherQuery);
            ResourceIterator<Node> physicalNodeColumn = result.columnAs("physicalNode");
            List<Node> lstphysicalNodeColumn = Iterators.asList(physicalNodeColumn);
            
            for (Node physicalNode : lstphysicalNodeColumn)
                physicalNodes.add(ogmService.createObjectLightFromNode(physicalNode));
            
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
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node classNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            Node newReport = connectionManager.getConnectionHandler().createNode(reportsLabel);
            newReport.setProperty(Constants.PROPERTY_NAME, reportName == null ? "" : reportName);
            newReport.setProperty(Constants.PROPERTY_DESCRIPTION, reportDescription == null ? "" : reportDescription);
            newReport.setProperty(Constants.PROPERTY_SCRIPT, script == null ? "" : script);
            newReport.setProperty(Constants.PROPERTY_TYPE, Math.abs(outputType) > 4 ? ReportMetadataLight.TYPE_HTML : outputType);
            newReport.setProperty(Constants.PROPERTY_ENABLED, enabled);
            
            classNode.createRelationshipTo(newReport, RelTypes.HAS_REPORT);
                        
            tx.success();
            return newReport.getId();
        }
    }

    @Override
    public long createInventoryLevelReport(String reportName, String reportDescription, String script, 
            int outputType, boolean enabled, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node dummyRootNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            if (dummyRootNode == null)
                throw new ApplicationObjectNotFoundException("Dummy Root could not be found");
            
            Node newReport = connectionManager.getConnectionHandler().createNode(reportsLabel);
            newReport.setProperty(Constants.PROPERTY_NAME, reportName == null ? "" : reportName);
            newReport.setProperty(Constants.PROPERTY_DESCRIPTION, reportDescription == null ? "" : reportDescription);
            newReport.setProperty(Constants.PROPERTY_SCRIPT, script == null ? "" : script);
            newReport.setProperty(Constants.PROPERTY_TYPE, Math.abs(outputType) > 4 ? ReportMetadataLight.TYPE_HTML : outputType);
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
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
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
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
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
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
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
    public List<ReportMetadataLight> getClassLevelReports(String className, boolean recursive, boolean includeDisabled) throws MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node mainClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            List<ReportMetadataLight> remoteReports = new ArrayList<>();
            
            if (mainClassNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            if (recursive) {
                Node classNode = mainClassNode;
                do {
                    for (Relationship hasReportRelationship : classNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_REPORT)) 
                        remoteReports.add(new ReportMetadataLight(hasReportRelationship.getEndNode().getId(), 
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
                    remoteReports.add(new ReportMetadataLight(hasReportRelationship.getEndNode().getId(), 
                                                        (String)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_NAME), 
                                                        (String)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_DESCRIPTION), 
                                                        (boolean)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_ENABLED),
                                                        (int)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_TYPE)));
            }
            
            return remoteReports;
        }
    }

    @Override
    public List<ReportMetadataLight> getInventoryLevelReports(boolean includeDisabled) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node dummyRootNode = connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
            
            if (dummyRootNode == null)
                throw new ApplicationObjectNotFoundException("Dummy Root could not be found");
            
            List<ReportMetadataLight> remoteReports = new ArrayList<>();
            
            for (Relationship hasReportRelationship : dummyRootNode.getRelationships(Direction.OUTGOING, RelTypes.HAS_REPORT)) {
                if (includeDisabled || (boolean)hasReportRelationship.getEndNode().getProperty(Constants.PROPERTY_ENABLED))
                    remoteReports.add(new ReportMetadataLight(hasReportRelationship.getEndNode().getId(), 
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
    public ReportMetadata getReport(long reportId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId)); 
            
            List<StringPair> parameters = new ArrayList<>();
            for (String property : reportNode.getPropertyKeys()) {
                if (property.startsWith("PARAM_"))
                    parameters.add(new StringPair(property.replace("PARAM_", ""), (String)reportNode.getProperty(property)));
            }
                
            return new ReportMetadata(reportNode.getId(), (String)reportNode.getProperty(Constants.PROPERTY_NAME), 
                                    (String)reportNode.getProperty(Constants.PROPERTY_DESCRIPTION), 
                                    (boolean)reportNode.getProperty(Constants.PROPERTY_ENABLED),
                                    (int)reportNode.getProperty(Constants.PROPERTY_TYPE),
                                    (String)reportNode.getProperty(Constants.PROPERTY_SCRIPT), 
                                    parameters);
        }
    }

    @Override
    public byte[] executeClassLevelReport(String objectClassName, String objectId, long reportId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId)); 
            
            Node instanceNode = getInstanceOfClass(objectClassName, objectId);
            
            String script = (String)reportNode.getProperty(Constants.PROPERTY_SCRIPT);
            
            Binding environmentParameters = new Binding();
            environmentParameters.setVariable("instanceNode", instanceNode); //NOI18N
            environmentParameters.setVariable("connectionHandler", connectionManager.getConnectionHandler()); //NOI18N
            environmentParameters.setVariable("inventoryObjectLabel", inventoryObjectLabel);            
            environmentParameters.setVariable("classLabel", classLabel); //NOI18N
            
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
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node reportNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), reportsLabel, reportId);
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId)); 
                     
            String script = (String)reportNode.getProperty(Constants.PROPERTY_SCRIPT);
            
            HashMap<String, String> scriptParameters = new HashMap<>();
            for(StringPair parameter : parameters)
                scriptParameters.put(parameter.getKey(), parameter.getValue());
            
            Binding environmentParameters = new Binding();
            environmentParameters.setVariable("parameters", scriptParameters); //NOI18N
            environmentParameters.setVariable("connectionHandler", connectionManager.getConnectionHandler()); //NOI18N
            environmentParameters.setVariable("inventoryObjectLabel", inventoryObjectLabel); //NOI18N
            environmentParameters.setVariable("classLabel", classLabel); //NOI18N
            
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
    public List<Pool> getRootPools(String className, int type, boolean includeSubclasses) throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<Pool> pools  = new ArrayList<>();
            ResourceIterator<Node> poolNodes = connectionManager.getConnectionHandler().findNodes(poolLabel);
            while (poolNodes.hasNext()) {
                Node poolNode = poolNodes.next();
                if (!poolNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF_SPECIAL)) { //Root pools don't have parents
                    if ((int)poolNode.getProperty(Constants.PROPERTY_TYPE) == type) {
                        // The following conditions could probably normalized, but I think this way,
                        // the code is a bit more readable
                        if (className != null) { //We will return only those matching with the specified class name or its subclasses, depending on the value of includeSubclasses
                            String poolClass = (String)poolNode.getProperty(Constants.PROPERTY_CLASS_NAME);
                            if (includeSubclasses) {
                                try {
                                    if (mem.isSubclassOf(className, poolClass))
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
            //tx.success();
            return pools;
        }
    }
    
    @Override
    public List<Pool> getPoolsInObject(String objectClassName, String objectId, String poolClass) throws 
            BusinessObjectNotFoundException, InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<Pool> pools  = new ArrayList<>();
            Node objectNode = connectionManager.getConnectionHandler().findNode(inventoryObjectLabel, Constants.PROPERTY_UUID, objectId);
            
            if (objectNode == null)
                throw new BusinessObjectNotFoundException(objectClassName, objectId);
            
            for (Relationship containmentRelationship : objectNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                if (containmentRelationship.hasProperty(Constants.PROPERTY_NAME) && 
                        Constants.REL_PROPERTY_POOL.equals(containmentRelationship.getProperty(Constants.PROPERTY_NAME))) {
                    Node poolNode = containmentRelationship.getStartNode();
                    if (poolClass != null) { // We will return only those matching with the specified class name or any of its subclasses
                        if (mem.isSubclassOf(poolClass, (String)poolNode.getProperty(Constants.PROPERTY_CLASS_NAME)))
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
    public List<Pool> getPoolsInPool(String parentPoolId, String poolClass) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            List<Pool> pools  = new ArrayList<>();
            Node parentPoolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, parentPoolId);
            
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
    public Pool getPool(String poolId) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node poolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);
            
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
    public List<BusinessObjectLight> getPoolItems(String poolId, int limit)
            throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            
            Node poolNode = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, poolId);

            if (poolNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The pool with id %s could not be found", poolId));

            List<BusinessObjectLight> poolItems  = new ArrayList<>();

            int i = 0;
            for (Relationship rel : poolNode.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)) {
                if (limit != -1) {
                    if (i >= limit)
                         break;
                    i++;
                }
                if (rel.hasProperty(Constants.PROPERTY_NAME)) {
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(Constants.REL_PROPERTY_POOL)) {
                        Node item = rel.getStartNode();
                        
                        String itemUuid = item.hasProperty(Constants.PROPERTY_UUID) ? (String) item.getProperty(Constants.PROPERTY_UUID) : null;
                        if (itemUuid == null)
                            throw new InvalidArgumentException(String.format("The pool/object with id %s does not have uuid", item.getId()));
                        
                        Node temp = connectionManager.getConnectionHandler().findNode(poolLabel, Constants.PROPERTY_UUID, itemUuid);
                        if (temp == null)  //If it's not a pool, but a normal business object
                            poolItems.add(ogmService.createObjectLightFromNode(item));
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
     * @throws BusinessObjectNotFoundException if the object could not be found
     * @throws InvalidArgumentException If the object id is null
     */
    public Node getInstanceOfClass(String className, String oid) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        if (oid == null)
            throw new InvalidArgumentException("The object id cannot be null");
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            HashMap<String, Object> params = new HashMap<>();
            params.put("className", className); //NOI18N
            params.put("objectId", oid); //NOI18N
            StringBuilder queryBuilder = new StringBuilder();
            if (className == null || className.equals(Constants.NODE_DUMMYROOT))
                queryBuilder.append("MATCH (specialNode:specialNodes {name: 'DummyRoot'})<-[:CHILD_OF]-(object:inventoryObjects {_uuid: $objectId}) RETURN object"); //NOI18N
            queryBuilder.append("MATCH (class:classes {name: $className})<-[:INSTANCE_OF]-(object:inventoryObjects{_uuid: $objectId}) RETURN object"); //NOI18N
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), params);
            while (result.hasNext()) {
                tx.success();
                return (Node) result.next().get("object"); //NOI18N
            }
            tx.success();
            throw new BusinessObjectNotFoundException(className, oid);
        }
    }
    /**
     * Boiler-plate code. Gets a particular instance given the class name and the oid
     * @param classId object class id
     * @param oid object id
     * @return a Node representing the entity
     * @throws MetadataObjectNotFoundException if the class could not be found
     * @throws BusinessObjectNotFoundException
     */
    public Node getInstanceOfClass(long classId, String oid) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException{
                
        //if any of the parameters is null, return the dummy root
        if (classId == -1)
            return connectionManager.getConnectionHandler().findNode(specialNodeLabel, Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT);
        
        Node classNode = Util.findNodeByLabelAndId(connectionManager.getConnectionHandler(), classLabel, classId);
        
        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class with id %s could not be found", classId));

        Iterable<Relationship> iteratorInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iteratorInstances.iterator();
        while (instances.hasNext()) {
            Node otherSide = instances.next().getStartNode();
            
            String otherSideUuid = otherSide.hasProperty(Constants.PROPERTY_UUID) ? (String) otherSide.getProperty(Constants.PROPERTY_UUID) : null;
            if (otherSideUuid != null && otherSideUuid.equals(oid))
                return otherSide;
        }
        throw new BusinessObjectNotFoundException((String)classNode.getProperty(Constants.PROPERTY_NAME), oid);
    }

    public Node getInstanceOfClass(Node classNode, String oid) throws BusinessObjectNotFoundException{
        Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iterableInstances.iterator();
        
        while (instances.hasNext()) {
            Node otherSide = instances.next().getStartNode();
            
            String otherSideUuid = otherSide.hasProperty(Constants.PROPERTY_UUID) ? (String) otherSide.getProperty(Constants.PROPERTY_UUID) : null;
            if (otherSideUuid != null && otherSideUuid.equals(oid))
                return otherSide;
        }
        throw new BusinessObjectNotFoundException((String)classNode.getProperty(Constants.PROPERTY_NAME), oid);
    }
    
    public Node createObject(Node classNode, ClassMetadata classToMap, HashMap<String,String> attributes) 
            throws InvalidArgumentException, MetadataObjectNotFoundException {
 
        if (classToMap.isAbstract())
            throw new InvalidArgumentException(String.format("Can not create objects from abstract classes (%s)", classToMap.getName()));
        
        Node newObject = connectionManager.getConnectionHandler().createNode(inventoryObjectLabel);
                
        String uuid = UUID.randomUUID().toString();
        newObject.setProperty(Constants.PROPERTY_UUID, uuid);
        
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

            if (AttributeMetadata.isPrimitive(attributeType)) {
                if (classToMap.isUnique(attributeName)) {
                    //if an attribute is unique and mandatory it should be checked before the object creation, here
                    if (classToMap.getType(attributeName).equals("String") || //NOI18N
                        classToMap.getType(attributeName).equals("Integer") ||  //NOI18N
                        classToMap.getType(attributeName).equals("Float") ||  //NOI18N
                        classToMap.getType(attributeName).equals("Long")) { //NOI18N
                        if (isObjectAttributeUnique(classToMap.getName(), attributeName, attributes.get(attributeName)))
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
                    
                    if (!mem.isSubclassOf(Constants.CLASS_GENERICOBJECTLIST, attributeType))
                        throw new InvalidArgumentException(String.format("Type %s is not a primitive nor a list type", attributeName));

                    List<String> listTypeItemIds = new ArrayList<>();
                    for (String listTypeItemIdAsString : attributes.get(attributeName).split(";")) //If the attribute is multiple, the ids will be separated by ";", otherwise, it will be a single long value
                        listTypeItemIds.add(listTypeItemIdAsString);

                    Node listTypeClassNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, attributeType);

                    if (listTypeClassNode == null)
                        throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", attributeType));
                    
                    List<Node> listTypeItemNodes = Util.getListTypeItemNodes(listTypeClassNode, listTypeItemIds);

                    if (!listTypeItemNodes.isEmpty()) {
                        //Create the new relationships
                        for (Node listTypeItemNode : listTypeItemNodes) {
                            Relationship newRelationship = newObject.createRelationshipTo(listTypeItemNode, RelTypes.RELATED_TO);
                            newRelationship.setProperty(Constants.PROPERTY_NAME, attributeName);
                        }

                    } else if (attributeMetadata.isMandatory())
                        throw new InvalidArgumentException(String.format("The attribute %s is mandatory, can not be set to null", attributeName));

                } catch (NumberFormatException ex) {
                    throw new InvalidArgumentException(String.format("The value %s is not a valid list type item id", attributes.get(attributeName)));
                }
            }
        }
        
        newObject.createRelationshipTo(classNode, RelTypes.INSTANCE_OF);
        
        return newObject;       
    }
    
    public ChangeDescriptor updateObject(Node instance, ClassMetadata classMetadata, HashMap<String, String> attributes) throws InvalidArgumentException, MetadataObjectNotFoundException {
        String oldValues = "", newValues = "", affectedProperties = "";

        for (String attributeName : attributes.keySet()) {
            if (classMetadata.hasAttribute(attributeName)) {
                affectedProperties = attributeName + " ";
                if (AttributeMetadata.isPrimitive(classMetadata.getType(attributeName))) { // We are changing a primitive type, such as String, or int
                    oldValues += (instance.hasProperty(attributeName) ? String.valueOf(instance.getProperty(attributeName)) : null) + " ";
                    
                    if (attributes.get(attributeName) == null) {
                        if (classMetadata.getAttribute(attributeName).isMandatory())//if attribute is mandatory can be set empty or null
                            throw new InvalidArgumentException(String.format("The attribute %s is mandatory, it can not be set null or empty", attributeName));
                        else
                            instance.removeProperty(attributeName);
                    } else {
                        newValues += attributes.get(attributeName) + " ";
                        //if attribute is mandatory string attributes can't be empty or null
                        if (classMetadata.getAttribute(attributeName).isMandatory()) {
                            if (attributes.get(attributeName).isEmpty())
                                throw new InvalidArgumentException(String.format("The attribute %s is mandatory, it can not be null or empty", attributeName));
                        }
                        if (classMetadata.getAttribute(attributeName).isUnique()) {
                            //
                            BusinessObject businessObject = createObjectFromNode(instance);
                            boolean updateUniqueAttrCache = false;
                            if (businessObject.getAttributes().containsKey(attributeName) && 
                                businessObject.getAttributes().get(attributeName) != null) {
                                updateUniqueAttrCache = true;
                            }
                            if (updateUniqueAttrCache && 
                                !businessObject.getAttributes().get(attributeName).equals(attributes.get(attributeName))) {
                                updateUniqueAttrCache = true;
                            }
                            else
                                updateUniqueAttrCache = false;
                            if (businessObject.getAttributes().get(attributeName) == null)
                                updateUniqueAttrCache = true;
                            //
                            if (updateUniqueAttrCache) {
                                if (isObjectAttributeUnique(classMetadata.getName(), attributeName, attributes.get(attributeName))) {
                                    instance.setProperty(attributeName,Util.getRealValue(attributes.get(attributeName), classMetadata.getType(attributeName)));
                                    CacheManager.getInstance().removeUniqueAttributeValue(businessObject.getClassName(), attributeName, businessObject.getAttributes().get(attributeName));
                                } else
                                    throw new InvalidArgumentException(String.format("The attribute \"%s\" is unique and the value you are trying to set is already in use", classMetadata.getAttribute(attributeName).getDisplayName() != null ? classMetadata.getAttribute(attributeName).getDisplayName() : attributeName));
                            }
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
                        if (rel.getProperty(Constants.PROPERTY_NAME).equals(attributeName)) {
                            oldValues += rel.getEndNode().getProperty(Constants.PROPERTY_NAME) + " ";
                            rel.delete();
                        }
                    }
                    
                    if (attributes.get(attributeName) == null || attributes.get(attributeName).trim().isEmpty()) {
                        if (classMetadata.getAttribute(attributeName).isMandatory())
                                throw new InvalidArgumentException(String.format("The attribute %s is mandatory, can not be set to null", attributeName));
                    } else {
                        try { //If the new value is different than null, then create the new relationships
                            List<String> listTypeItemIds = new ArrayList<>();
                            for (String listTypeItemIdAsString : attributes.get(attributeName).split(";")) //If the attribute is multiple, the ids will be separated by ";", otherwise, it will be a single long value
                                listTypeItemIds.add(listTypeItemIdAsString);
                            
                            Node listTypeNodeClass = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, classMetadata.getType(attributeName));
                            List<Node> listTypeItemNodes = Util.getListTypeItemNodes(listTypeNodeClass, listTypeItemIds);
                            
                            if (!listTypeItemNodes.isEmpty()) {
                                //Create the new relationships
                                for (Node listTypeItemNode : listTypeItemNodes) {
                                    newValues += listTypeItemNode.getProperty(Constants.PROPERTY_NAME) + " ";
                                    Relationship newRelationship = instance.createRelationshipTo(listTypeItemNode, RelTypes.RELATED_TO);
                                    newRelationship.setProperty(Constants.PROPERTY_NAME, attributeName);
                                }
                                
                            } else if (classMetadata.getAttribute(attributeName).isMandatory())
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
        Node newInstance = connectionManager.getConnectionHandler().createNode(inventoryObjectLabel);
         // Make sure the object has a name, even if it's marked as no copy. Remember that all inventory object nodes 
         //must at least have the properties name, creationDate and _uuid. The latter are also set below too.
        newInstance.setProperty(Constants.PROPERTY_NAME, "");
        
        // Let's find out what attributes should not be copied because they're either marked as unique or noCopy
        ClassMetadata classMetadata = CacheManager.getInstance().
                getClass((String)templateObject.getSingleRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING)
                        .getEndNode()
                        .getProperty(Constants.PROPERTY_NAME));
        
        // First copy normal attributes
        for (String property : templateObject.getPropertyKeys()) {
            AttributeMetadata currentAttribute = classMetadata.getAttribute(property);
            if (currentAttribute != null && !currentAttribute.isUnique() && !currentAttribute.isNoCopy())
                newInstance.setProperty(property, templateObject.getProperty(property));
        }
        
        // Then list types
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING)) {
            AttributeMetadata currentAttribute = classMetadata.getAttribute((String)rel.getProperty(Constants.PROPERTY_NAME));
            if (currentAttribute != null && !currentAttribute.isNoCopy()) // We don't check for uniqueness, because list types can not be set as unique
                newInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, rel.getProperty(Constants.PROPERTY_NAME));
        }
        
        newInstance.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
        newInstance.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());
        
        newInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF).iterator().next().getEndNode(), RelTypes.INSTANCE_OF);

        if (recursive) {
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING)) {
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
        
        Node newInstance = connectionManager.getConnectionHandler().createNode(inventoryObjectLabel);
       
        for (String property : templateObject.getPropertyKeys()) {
            if (!property.equals(Constants.PROPERTY_UUID)) {
                if (classToMap.isMandatory(property) && ((String)templateObject.getProperty(property)).isEmpty())
                    throw new InvalidArgumentException(String.format("The attribute %s is mandatory, can not be set null or empty", property));

                newInstance.setProperty(property, templateObject.getProperty(property));
            }
        }
        
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING))
            newInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, rel.getProperty(Constants.PROPERTY_NAME));
        
        newInstance.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
        newInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF_SPECIAL).iterator().next().getEndNode(), 
                RelTypes.INSTANCE_OF);

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

        newInstance.setProperty(Constants.PROPERTY_UUID, UUID.randomUUID().toString());
        return newInstance;
    }

    @Override
    public boolean canDeleteObject(String className, String oid) throws BusinessObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {
        if (!mem.isSubclassOf(Constants.CLASS_INVENTORYOBJECT, className))
            throw new OperationNotPermittedException(String.format("Class %s is not a business-related class", className));
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {   
            Node instance = getInstanceOfClass(className, oid);
            boolean isSafeToDelete = canDeleteObject(instance);
            if (!isSafeToDelete)
                return false;
            else {
                for (Relationship rel : instance.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF, RelTypes.CHILD_OF_SPECIAL))
                    isSafeToDelete = canDeleteObject(rel.getStartNode());
            }
            
            tx.success();
            return isSafeToDelete;
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
    private boolean isObjectAttributeUnique(String className, String attributeName, Object attributeValue) {
        List<Object> uniqueAttributeValues = CacheManager.getInstance().getUniqueAttributeValues(className, attributeName);
        if (uniqueAttributeValues != null) {
            for (Object uniqueAttributeValue : uniqueAttributeValues) {
                if (uniqueAttributeValue.equals(attributeValue))
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
            String filterName, String filterValue) throws InvalidArgumentException {
        
        List<BusinessObjectLight> res = new ArrayList<>();
            
        for (Relationship instanceOfRelationship : classNode.getRelationships(Direction.INCOMING, RelTypes.INSTANCE_OF)) {
            Node instance = instanceOfRelationship.getStartNode();
            if (instance.hasProperty(filterName) && instance.getProperty(filterName).equals(filterValue))
                res.add(ogmService.createObjectLightFromNode(instance));
            else {
                Iterable<Relationship> iterableRelationships = instance.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING);
                Iterator<Relationship> relationships = iterableRelationships.iterator();
                
                while(relationships.hasNext()) {
                    Relationship relationship = relationships.next();
                    
                    if (relationship.hasProperty(Constants.PROPERTY_NAME) && 
                        String.valueOf(relationship.getProperty(Constants.PROPERTY_NAME)).equals(filterName) &&
                        String.valueOf(relationship.getEndNode().getId()).equals(filterValue)) {
                        
                        res.add(ogmService.createObjectLightFromNode(instance));                        
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
                
                while(relationships.hasNext()) {
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
    
    private void getChildrenOfClassRecursive(String parentOid, String parentClass, String classToFilter, int maxResults, List<BusinessObjectLight> res) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        if (maxResults > 0 && res.size() == maxResults)
            return;
        
        Node parentNode = getInstanceOfClass(parentClass, parentOid);
        Iterable<Relationship> relationshipsChildOf = parentNode.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING);

        for (Relationship relatioshipChildOf : relationshipsChildOf) {
            Node child = relatioshipChildOf.getStartNode();
            String childClassName = Util.getClassName(child);

            if (childClassName == null)
                throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found", child.getId()));

            if (mem.isSubclassOf(classToFilter, childClassName)) {
                res.add(ogmService.createObjectLightFromNode(child));

                if (maxResults > 0 && res.size() == maxResults)
                    break;
            }
            String childUuid = child.hasProperty(Constants.PROPERTY_UUID) ? (String) child.getProperty(Constants.PROPERTY_UUID) : null;
            if (childUuid == null)
                throw new InvalidArgumentException(String.format("The object with id %s does not have uuid", child.getId()));
            getChildrenOfClassRecursive(childUuid, childClassName, classToFilter, maxResults, res);
        }
    }
    
    private void getSpecialChildrenOfClassRecursive(Node parentNode, String classToFilter, 
        int maxResults, List<BusinessObjectLight> res) 
        throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException {
        
        if (maxResults > 0 && res.size() == maxResults)
            return;
        
        Iterable<Relationship> relationshipsChildOfSpecial = parentNode.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING);

        for (Relationship relatioshipChildOfSpecial : relationshipsChildOfSpecial) {
            Node specialChild = relatioshipChildOfSpecial.getStartNode();
            
            if (!specialChild.hasLabel(this.poolLabel)) { //If the node is a pool, let's skip it and dig deeper, otherwise, let's see if it matches the filter
            
                String specialChildClassName = Util.getClassName(specialChild);

                if (specialChildClassName == null)
                    throw new MetadataObjectNotFoundException(String.format("Class for object with oid %s could not be found", specialChild.getId()));

                if (mem.isSubclassOf(classToFilter, specialChildClassName)) {
                    res.add(ogmService.createObjectLightFromNode(specialChild));

                    if (maxResults > 0 && res.size() == maxResults)
                        break;
                }
            }
            getSpecialChildrenOfClassRecursive(specialChild, classToFilter, maxResults, res);
        }
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
        
        HashMap<String, Object> attributes = new HashMap<>();
        String name = "";
        
        for (AttributeMetadata myAtt : classMetadata.getAttributes()) {
            //Only set the attributes existing in the current node. Please note that properties can't be null in
            //Neo4J, so a null value is actually a non-existing relationship/value
            if (instance.hasProperty(myAtt.getName())) {
               if (AttributeMetadata.isPrimitive(myAtt.getType())) {
                    Object value = instance.getProperty(myAtt.getName());

                    if (Constants.PROPERTY_NAME.equals(myAtt.getName()))
                        name = (String)value;

                    attributes.put(myAtt.getName(),value);
                }
            }
        }

        //Iterates through relationships and transform the into "plain" attributes
        Iterable<Relationship> iterableRelationships = instance.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING);
        Iterator<Relationship> relationships = iterableRelationships.iterator();

        while(relationships.hasNext()) {
            Relationship relationship = relationships.next();
            if (!relationship.hasProperty(Constants.PROPERTY_NAME))
                throw new InvalidArgumentException(String.format("The object with id %s is malformed", instance.getId()));

            String relationshipName = (String)relationship.getProperty(Constants.PROPERTY_NAME);              
            
            boolean hasRelationship = false;
            for (AttributeMetadata myAtt : classMetadata.getAttributes()) {
                if (myAtt.getName().equals(relationshipName)) {
                    if (attributes.containsKey(relationshipName))
                        attributes.put(relationshipName, attributes.get(relationshipName) + ";" + relationship.getEndNode().getProperty(Constants.PROPERTY_UUID)); //A multiple selection list type
                    else    
                        attributes.put(relationshipName, (String)relationship.getEndNode().getProperty(Constants.PROPERTY_UUID));
                    hasRelationship = true;
                    break;
                }                  
            }
            
            if (!hasRelationship) //This verification will help us find potential inconsistencies with list types
                                  //What this does is to verify if is there is a RELATED_TO relationship that shouldn't exist because its name is not an attribute of the class
                throw new InvalidArgumentException(String.format("The object with %s (%s) is related to list type %s (%s), but that is not consistent with the data model", 
                            instance.getProperty(Constants.PROPERTY_NAME), instance.getId(), relationship.getEndNode().getProperty(Constants.PROPERTY_NAME), relationship.getEndNode().getId()));
        }
        
        return new BusinessObject(classMetadata.getName(), (String)instance.getProperty(Constants.PROPERTY_UUID), name, attributes);
    }
    
    /**
     * Deletes recursively and object and all its children. Note that the transaction should be handled by the caller
     * @param instance The object to be deleted
     * @param unsafeDeletion True if you want the object to be deleted no matter if it has RELATED_TO, HAS_PROCESS_INSTANCE or RELATED_TO_SPECIAL relationships
     * @throws org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException If the object already has relationships
     */
    private void deleteObject(Node instance, boolean unsafeDeletion) throws OperationNotPermittedException {
        if (!unsafeDeletion && !canDeleteObject(instance)) 
            throw new OperationNotPermittedException(String.format("The object %s (%s) can not be deleted since it has relationships", 
                    instance.getProperty(Constants.PROPERTY_NAME), instance.getProperty(Constants.PROPERTY_UUID)));
        
        for (Relationship rel : instance.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF, RelTypes.CHILD_OF_SPECIAL))
            deleteObject(rel.getStartNode(), unsafeDeletion);
        
        // Searches the related views to delete the nodes in the data base       
        for (Relationship aHasViewRelationship : instance.getRelationships(RelTypes.HAS_VIEW)) {
            Node viewNode = aHasViewRelationship.getEndNode();
            aHasViewRelationship.delete();
            viewNode.delete();
        }
        
        //Now we delete the audit trail entries
        for (Relationship aHasHistoryEntryRelationship : instance.getRelationships(RelTypes.HAS_HISTORY_ENTRY)) {
            Node historyEntryNode = aHasHistoryEntryRelationship.getEndNode();
            if (historyEntryNode.hasRelationship(RelTypes.PERFORMED_BY))
                historyEntryNode.getSingleRelationship(RelTypes.PERFORMED_BY, Direction.OUTGOING).delete();
            if (historyEntryNode.hasRelationship(RelTypes.CHILD_OF_SPECIAL))
                historyEntryNode.getSingleRelationship(RelTypes.CHILD_OF_SPECIAL, Direction.OUTGOING).delete();
            aHasHistoryEntryRelationship.delete();
            historyEntryNode.delete();
        }
        
        //Now we dispose of the attachments
        for (Relationship aHasAttachmentRelationship : instance.getRelationships(RelTypes.HAS_ATTACHMENT)) {
            Node attachmentNode = aHasAttachmentRelationship.getEndNode();
            aHasAttachmentRelationship.delete();
            
            String fileName = configuration.getProperty("attachmentsPath", DEFAULT_ATTACHMENTS_PATH) +  //NOI18N
                    File.separator + instance.getId() + "_" + attachmentNode.getId(); //NOI18N
            try {
                new File(fileName).delete();
            } catch (Exception ex) {
                System.out.println(String.format("[KUWAIBA] An error occurred while deleting attachment %s for object %s (%s)", 
                        fileName, instance.getProperty(Constants.PROPERTY_NAME), instance.getId()));
            }
            attachmentNode.delete();
        }
        
        //Now the SyncsDataSourceConfiguration Related to the object
        for (Relationship rel : instance.getRelationships(RelTypes.HAS_CONFIGURATION)) {
            Node endNode = rel.getEndNode();
            for (Relationship relds : endNode.getRelationships())
                relds.delete();
            endNode.delete();
        }
        
        //Remove the remaining relationships without deleting the other end of the relationship, 
        //because we don't know what's there (a list type, another element of the model that should not be delete along, etc)
        for (Relationship rel : instance.getRelationships())
            rel.delete();

        instance.delete();
    }
    
    /**
     * Checks if it's safe to delete (not recursively) an object and all its children. Note that the transaction should be handled by the caller.
     * @param instance The object to be deleted.
     * @return true if the object is safe to be deleted, false otherwise.
     */
    private boolean canDeleteObject(Node instance) {
        return !instance.hasRelationship(RelTypes.RELATED_TO_SPECIAL, RelTypes.HAS_PROCESS_INSTANCE);        
    }
    
    //<editor-fold desc="Kuwaiba 2.1" defaultstate="collapsed">
    @Override
    public long getObjectChildrenCount(String className, String oid) throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (className == null)
                throw new InvalidArgumentException("The className cannot be null");
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            final String COUNT = "count"; //NOI18N
                                    
            if (oid == null) {
                queryBuilder.append("MATCH (dummyRoot:root:specialNodes)"); //NOI18N
                queryBuilder.append("<-[:CHILD_OF]-"); //NOI18N
                queryBuilder.append("(childNode:inventoryObjects)\n"); //NOI18N
                queryBuilder.append("WHERE dummyRoot.name = $name\n"); //NOI18N
                queryBuilder.append(String.format("RETURN count(childNode) AS %s;", COUNT)); //NOI18N
                
                parameters.put("name", Constants.DUMMY_ROOT); //NOI18N
            } else {
                queryBuilder.append("MATCH (classNode:classes)"); //NOI18N
                queryBuilder.append("<-[:INSTANCE_OF]-"); //NOI18N
                queryBuilder.append("(parentNode:inventoryObjects)"); //NOI18N
                queryBuilder.append("<-[:CHILD_OF]-"); //NOI18N
                queryBuilder.append("(childNode:inventoryObjects)\n"); //NOI18N
                queryBuilder.append("WHERE classNode.name = $className\n"); //NOI18N
                queryBuilder.append("AND parentNode._uuid = $oid\n"); //NOI18N
                queryBuilder.append(String.format("RETURN count(childNode) AS %s;", COUNT)); //NOI18N
                
                parameters.put("className", className); //NOI18N
                parameters.put("oid", oid); //NOI18N
            }
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            while (result.hasNext()) {
                tx.success();
                return (long) result.next().get(COUNT);
            }
            tx.success();
            return 0;
        }
    }
    @Override
    public List<BusinessObjectLight> getObjectChildren(String className, String oid, long skip, long limit) throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (className == null)
                throw new InvalidArgumentException("The className cannot be null");
            if (skip < 0)
                throw new InvalidArgumentException("The skip cannot be less than 0");
            if (limit < 0)
                throw new InvalidArgumentException("The limit cannot be less than 0");
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            final String CHILD_NODE = "childNode"; //NOI18N
                                    
            if (oid == null) {
                queryBuilder.append("MATCH (dummyRoot:root:specialNodes)"); //NOI18N
                queryBuilder.append("<-[:CHILD_OF]-"); //NOI18N
                queryBuilder.append("(childNode:inventoryObjects)\n"); //NOI18N
                queryBuilder.append("WHERE dummyRoot.name = $name\n"); //NOI18N
                queryBuilder.append(String.format("RETURN childNode AS %s\n", CHILD_NODE)); //NOI18N
                queryBuilder.append((skip > 0 && limit > 0)  ? "ORDER BY childNode.name ASC SKIP $skip LIMIT $limit;" : ""); //NOI18N
                
                parameters.put("name", Constants.DUMMY_ROOT); //NOI18N
            } else {
                queryBuilder.append("MATCH (classNode:classes)"); //NOI18N
                queryBuilder.append("<-[:INSTANCE_OF]-"); //NOI18N
                queryBuilder.append("(parentNode:inventoryObjects)"); //NOI18N
                queryBuilder.append("<-[:CHILD_OF]-"); //NOI18N
                queryBuilder.append("(childNode:inventoryObjects)\n"); //NOI18N
                queryBuilder.append("WHERE classNode.name = $className\n"); //NOI18N
                queryBuilder.append("AND parentNode._uuid = $oid\n"); //NOI18N
                queryBuilder.append(String.format("RETURN childNode AS %s\n", CHILD_NODE)); //NOI18N
                queryBuilder.append((skip > 0 && limit > 0)  ? "ORDER BY childNode.name ASC SKIP $skip LIMIT $limit;" : ""); //NOI18N
                
                parameters.put("className", className); //NOI18N
                parameters.put("oid", oid); //NOI18N
            }
            if(skip > 0 && limit > 0){
                parameters.put("skip", skip); //NOI18N
                parameters.put("limit", limit); //NOI18N
            }
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            List<BusinessObjectLight> objectChildren = new ArrayList();
            while (result.hasNext())
                objectChildren.add(ogmService.createObjectLightFromNode((Node) result.next().get(CHILD_NODE)));
            tx.success();
            return objectChildren;
        }
    }
    
    @Override
    public List<BusinessObjectLight> getObjectOfClassLigth(String className, HashMap <String, String> filter, long skip, long limit) throws InvalidArgumentException, MetadataObjectNotFoundException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (className == null)
                throw new InvalidArgumentException("The className cannot be null");
            if (skip < 0)
                throw new InvalidArgumentException("The skip cannot be less than 0");
            if (limit < 0)
                throw new InvalidArgumentException("The limit cannot be less than 0");
            
            Node classMetadataNode = connectionManager.getConnectionHandler().findNode(classLabel, Constants.PROPERTY_NAME, className);
            final String INSTANCE = "instance"; //NOI18N
            
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(className);

            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            StringBuilder queryFilterBuilder = new StringBuilder();
            
            boolean isAbstract = (Boolean) classMetadataNode.getProperty(Constants.PROPERTY_ABSTRACT);
            parameters.put("className", className); //NOI18N
            
            for (Map.Entry<String, String> entry : filter.entrySet()){
                String key = (String)entry.getKey();
                String val = (String)entry.getValue();
                if(val != null){
                    parameters.put(key, val);
                    queryFilterBuilder.append(String.format("AND TOLOWER(instance.%s) CONTAINS TOLOWER($%s) ", key, key)); //NOI18N
                }
            }
            
            if (isAbstract) {
                queryBuilder.append("MATCH (class:classes)<-[:EXTENDS*]-"); //NOI18N
                queryBuilder.append("(subclass:classes)<-[:INSTANCE_OF]-"); //NOI18N
                queryBuilder.append("(instance:" + Constants.LABEL_INVENTORY_OBJECTS + ") "); //NOI18N
                queryBuilder.append("WHERE class.name = $className "); //NOI18N
                queryBuilder.append(queryFilterBuilder.toString());
                queryBuilder.append(String.format("RETURN instance AS %s\n", INSTANCE)); //NOI18N
                queryBuilder.append("ORDER BY instance.name ASC SKIP $skip LIMIT $limit;"); //NOI18N
            } else {
                queryBuilder.append("MATCH (class:classes)<-[:INSTANCE_OF]-"); //NOI18N
                queryBuilder.append("(instance:" + Constants.LABEL_INVENTORY_OBJECTS + ") "); //NOI18N
                queryBuilder.append("WHERE class.name=$className "); //NOI18N
                queryBuilder.append(queryFilterBuilder.toString());
                queryBuilder.append(String.format("RETURN instance AS %s\n", INSTANCE)); //NOI18N
                queryBuilder.append("ORDER BY instance.name ASC SKIP $skip LIMIT $limit;"); //NOI18N
            }
            parameters.put("skip", skip); //NOI18N
            parameters.put("limit", limit); //NOI18N
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            List<BusinessObjectLight> objectChildren = new ArrayList();
            while (result.hasNext())
                objectChildren.add(ogmService.createObjectLightFromNode((Node) result.next().get(INSTANCE)));
            tx.success();
            return objectChildren;
        }
    }
    
    @Override
    public HashMap<String, List<BusinessObjectLight>> getSuggestedObjectsWithFilterGroupedByClassName(String filter,  long skip, long limit) throws InvalidArgumentException {
        try (Transaction tx = connectionManager.getConnectionHandler().beginTx()) {
            if (filter == null)
                throw new InvalidArgumentException("The className cannot be null");
            if (skip < 0)
                throw new InvalidArgumentException("The skip cannot be less than 0");
            if (limit < 0)
                throw new InvalidArgumentException("The limit cannot be less than 0");
                        
            HashMap<String, Object> parameters = new HashMap();
            StringBuilder queryBuilder = new StringBuilder();
            
            queryBuilder.append(String.format("MATCH (object:%s)", inventoryObjectLabel)); //NOI18N
            queryBuilder.append("-[:INSTANCE_OF]->(class) "); //NOI18N
            queryBuilder.append("WHERE TOLOWER(object.name) CONTAINS TOLOWER($searchString) "); //NOI18N
            queryBuilder.append("OR TOLOWER(class.name) CONTAINS TOLOWER($searchString) "); //NOI18N
            queryBuilder.append("RETURN class {.name, objects: collect(object {.name, ._uuid})} "); //NOI18N
            queryBuilder.append("ORDER BY class.name ASC SKIP $skip LIMIT $limit;"); //NOI18N

            parameters.put("searchString", filter);
            parameters.put("skip", skip); //NOI18N
            parameters.put("limit", limit); //NOI18N
            
            HashMap<String, List<BusinessObjectLight>> res  = new HashMap<>();
            Result result = connectionManager.getConnectionHandler().execute(queryBuilder.toString(), parameters);
            //format {name: className1, Objects[{}, {}]} {name: className2, Objects[{}, {}]} 
            while (result.hasNext()){
                Map<String, Object> row = result.next();
                
                row.entrySet().forEach(entry_ -> {
                    String key = entry_.getKey(); 
                    //format {name: className1, Objects[{}, {}]} {name: className2, Objects[{}, {}]} 
                    HashMap val = (HashMap)entry_.getValue();
                    String className = (String)val.get("name"); //name: className
                    //objects: list the objects attributes of the className in a map: [{name, _uuid:}, {name:, uuid:}, {name:, _uuid:}]
                    ArrayList objects = (ArrayList)val.get("objects");

                    if(res.get(className) == null)
                        res.put(className, new ArrayList<>());

                    objects.forEach(inventoryObjectData -> {
                        res.get(className).add(new BusinessObjectLight(className,
                                (String)((HashMap)inventoryObjectData).get("_uuid"), 
                                (String)((HashMap)inventoryObjectData).get("name")));
                    });
                });
            }
            tx.success();
            return res;
        }
    }
    //</editor-fold>
}
