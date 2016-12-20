/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.services.persistence.impl.neo4j.telecom;

import com.neotropic.kuwaiba.modules.reporting.defaults.DefaultReports;
import com.neotropic.kuwaiba.modules.reporting.InventoryReport;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReport;
import com.neotropic.kuwaiba.modules.reporting.model.RemoteReportLight;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.kuwaiba.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.ConnectionManager;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.AnnotatedRemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLight;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObjectLightList;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;
import org.kuwaiba.services.persistence.cache.CacheManager;
import org.kuwaiba.services.persistence.impl.neo4j.RelTypes;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.services.persistence.util.Util;
import org.kuwaiba.util.ChangeDescriptor;
import org.kuwaiba.ws.todeserialize.StringPair;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.helpers.collection.IteratorUtil;

/**
 * Business entity manager reference implementation (using Neo4J as backend)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BusinessEntityManagerImpl implements BusinessEntityManager {
    /**
     * Reference to the Application Entity Manager
     */
    private ApplicationEntityManager aem;
    /**
     * Reference to the Metadata Entity Manager
     */
    private MetadataEntityManager mem;
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
     * Pools index
     */
    private Index<Node> poolsIndex;
    /**
     * Special nodes index
     */
    private Index<Node> specialNodesIndex;
    /**
     * Index for reports
     */
    private Index<Node> reportsIndex;
    /**
     * As a temporary workaround, the old hard-coded reports are wrapped instead of being completely migrated to Groovy scripts
     */
    private DefaultReports defaultReports;
    /**
     * Reference to the CacheManager
     */
    private CacheManager cm = CacheManager.getInstance();

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
        try(Transaction tx = graphDb.beginTx()) {
            this.classIndex = graphDb.index().forNodes(Constants.INDEX_CLASS);
            this.objectIndex = graphDb.index().forNodes(Constants.INDEX_OBJECTS);
            this.poolsIndex = graphDb.index().forNodes(Constants.INDEX_POOLS);
            this.reportsIndex = graphDb.index().forNodes(Constants.INDEX_REPORTS);
            this.specialNodesIndex = graphDb.index().forNodes(Constants.INDEX_SPECIAL_NODES);
        }catch(Exception ex) {
            System.out.println(String.format("[KUWAIBA] [%s] An error was found while creating the BEM instance: %s", 
                    Calendar.getInstance().getTime(), ex.getMessage()));
        }
    }

    @Override
    public long createObject(String className, String parentClassName, long parentOid, HashMap<String,List<String>> attributes, long template)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {
        
        ClassMetadata myClass= cm.getClass(className);
        
        if (!mem.getPossibleChildren(parentClassName).contains(myClass)) 
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
        
        try (Transaction tx = graphDb.beginTx()) {        
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

            if (myClass == null){
                myClass = Util.createClassMetadataFromNode(classNode);
                cm.putClass(myClass);
            }

            if (myClass.isInDesign())
                throw new OperationNotPermittedException("Can not create instances of classes marked as isDesign");

            if (myClass.isAbstract())
                throw new OperationNotPermittedException(String.format("Abstract class %s can not be instantiated", className));

            if (!cm.isSubClass("InventoryObject", className))
                throw new OperationNotPermittedException("Can not create non-inventory objects");

            //The object should be created under an instance other than the dummy root
            if (parentClassName != null && parentOid != -1) {
                ClassMetadata myParentObjectClass= cm.getClass(parentClassName);
                if (myParentObjectClass == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", parentClassName));
            }

            Node parentNode;
            if (parentOid != -1){
                 parentNode = getInstanceOfClass(parentClassName, parentOid);
                if (parentNode == null)
                    throw new ObjectNotFoundException(parentClassName, parentOid);
            }
            else
                parentNode = specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();

            Node newObject;
            if (template <= 0)
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
                
                newObject = copyTemplateElement(templateNode, true);
            }
            newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);
            tx.success();
            return newObject.getId();
        }
    }
    
    //TODO: Rewrite this!
    @Override
    public long createObject(String className, String parentClassName, String criteria, HashMap<String, List<String>> attributes, long template)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException, OperationNotPermittedException, ApplicationObjectNotFoundException {
        
        ClassMetadata objectClass = cm.getClass(className);
        if (objectClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
        
        if (objectClass.isInDesign())
            throw new OperationNotPermittedException("Can not create instances of classes marked as isDesign");

        if (objectClass.isAbstract())
            throw new OperationNotPermittedException("Can not create instances of abstract classes");

        if (!cm.isSubClass("InventoryObject", className))
            throw new OperationNotPermittedException("Can not create non-inventory objects");
        
        if (!mem.getPossibleChildren(parentClassName).contains(objectClass))
            throw new OperationNotPermittedException(String.format("An instance of class %s can't be created as child of %s", className, parentClassName == null ? Constants.NODE_DUMMYROOT : parentClassName));
        
        String[] splitCriteria = criteria.split(":");
        if (splitCriteria.length != 2)
            throw new InvalidArgumentException("The criteria is not valid, two components expected (attributeName:attributeValue)");

        if (splitCriteria[0].equals(Constants.PROPERTY_OID)) //The user is providing the id of te parent node explicitely
            return createObject(className, parentClassName, Long.parseLong(splitCriteria[1]), attributes, template);

        ClassMetadata parentClass = cm.getClass(parentClassName);
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
            
            if (Constants.NODE_DUMMYROOT.equals(parentClassName))
                parentNode = specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
            else {
                
                parentClassNode = classIndex.get(Constants.PROPERTY_NAME, parentClassName).getSingle();
                
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
                
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

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
                
                newObject = copyTemplateElement(templateNode, true);
            }
            
            newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF);
                      
            tx.success();
            return newObject.getId();
        }
    }
    
    @Override
    public long createSpecialObject(String className, String parentClassName, long parentOid, HashMap<String,List<String>> attributes, long template)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {

        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

        if (myClass.isInDesign())
            throw new OperationNotPermittedException("Can not create instances of classes marked as isDesign");
        
        if (myClass.isAbstract())
            throw new OperationNotPermittedException("Can not create objects of abstract classes");
        
        try(Transaction tx = graphDb.beginTx()) {
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
        
            Node newObject = createObject(classNode, myClass, attributes);
            if (parentNode !=null)
                newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);

            objectIndex.putIfAbsent(newObject, Constants.PROPERTY_ID, newObject.getId());
                       
            tx.success();
            return newObject.getId();
        }
    }
    
    @Override
    public long createPoolItem(long poolId, String className, String[] attributeNames, 
    String[][] attributeValues, long templateId) 
            throws ApplicationObjectNotFoundException, InvalidArgumentException, 
            ArraySizeMismatchException, MetadataObjectNotFoundException {
        
        if (attributeNames != null && attributeValues != null){
            if (attributeNames.length != attributeValues.length)
            throw new ArraySizeMismatchException("attributeNames", "attributeValues");
        }
        
        
        try(Transaction tx =graphDb.beginTx()) {
            Node pool = poolsIndex.get(Constants.PROPERTY_ID, poolId).getSingle();
            
            if (pool == null)
                throw new ApplicationObjectNotFoundException(String.format("Pool with id %s can not be found", poolId));
            
            if (!pool.hasProperty(Constants.PROPERTY_CLASS_NAME))
                throw new InvalidArgumentException("This pool has not set his class name attribute");
            
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));
            
            ClassMetadata classMetadata = cm.getClass(className);
            
            if (!cm.isSubClass((String)pool.getProperty(Constants.PROPERTY_CLASS_NAME), className))
                throw new InvalidArgumentException(String.format("Class %s is not subclass of %s", className, (String)pool.getProperty(Constants.PROPERTY_CLASS_NAME)));
            
            HashMap<String, List<String>> attributes = new HashMap<>();
            if (attributeNames != null && attributeValues != null){
                for (int i = 0; i < attributeNames.length; i++)
                    attributes.put(attributeNames[i], Arrays.asList(attributeValues[i]));
            }
            
            Node newObject = createObject(classNode, classMetadata, attributes);
            newObject.createRelationshipTo(pool, RelTypes.CHILD_OF_SPECIAL).setProperty(Constants.PROPERTY_NAME, Constants.REL_PROPERTY_POOL);
            
            tx.success();
            return newObject.getId();
        }
    }

    @Override
    public long[] createBulkSpecialObjects(String className, int numberOfObjects, String parentClassName, long parentId) 
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, InvalidArgumentException {
        
        ClassMetadata myClass= cm.getClass(className);
        if (myClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

        if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, className))
            throw new OperationNotPermittedException(String.format("Class %s is not an business class", className));
        
        if (myClass.isInDesign())
            throw new OperationNotPermittedException("Can not create instances of classes marked as isDesign");
        
        if (myClass.isAbstract())
            throw new OperationNotPermittedException("Can't create objects from an abstract classes");

        try(Transaction tx =graphDb.beginTx())
        {
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
            }
            else
                parentNode = specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();

            long res[] = new long[numberOfObjects];
            for (int i = 0; i < numberOfObjects; i++){
                Node newObject = createObject(classNode, myClass, null);
                newObject.setProperty(Constants.PROPERTY_NAME, String.valueOf(i + 1));
                if (parentNode != null)
                    newObject.createRelationshipTo(parentNode, RelTypes.CHILD_OF_SPECIAL);
                
                objectIndex.putIfAbsent(newObject, Constants.PROPERTY_ID, newObject.getId());
            }
            
            tx.success();
            return res;
        }
    }

    @Override
    public RemoteBusinessObject getObject(String className, long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = graphDb.beginTx()) {
            ClassMetadata myClass = cm.getClass(className);
            Node instance = getInstanceOfClass(className, oid);
            RemoteBusinessObject res = Util.createRemoteObjectFromNode(instance, myClass);
            return res;
        }
    }
    
    

    @Override
    public RemoteBusinessObjectLight getObjectLight(String className, long oid)
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
        
        //Perform benchmarks to see if accessing to the objects index is less expensive
        try(Transaction tx = graphDb.beginTx()) {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();
            tx.success();
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));
            Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
            Iterator<Relationship> instances = iterableInstances.iterator();
            while (instances.hasNext()){
                Node instance = instances.next().getStartNode();
                if (instance.getId() == oid)
                    return new RemoteBusinessObjectLight(oid,
                            (String) instance.getProperty(Constants.PROPERTY_NAME),
                            className);
            }
            throw new ObjectNotFoundException(className, oid);
        }
    }
    
    @Override
    public RemoteBusinessObject getParent(String objectClass, long oid) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(objectClass, oid);
            if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF)){
                Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();

                //If the direct parent is DummyRoot, return a dummy RemoteBusinessObject with oid = -1
                if (parentNode.hasProperty(Constants.PROPERTY_NAME) && Constants.NODE_DUMMYROOT.equals(parentNode.getProperty(Constants.PROPERTY_NAME)) )
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
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getParents (String objectClassName, long oid)
        throws ObjectNotFoundException, MetadataObjectNotFoundException {
        
        List<RemoteBusinessObjectLight> parents =  new ArrayList<>();
      
        String cypherQuery = "START n=node({oid})" +
                             "MATCH n-[:" + RelTypes.CHILD_OF + "|" + RelTypes.CHILD_OF_SPECIAL + "*]->m " +
                             "RETURN m as parents";
      
        Map<String, Object> params = new HashMap<>();
        params.put("oid", oid);
        try (Transaction tx = graphDb.beginTx()) {
            Result result = graphDb.execute(cypherQuery, params);
            Iterator<Node> column = result.columnAs("parents");
            for (Node node : IteratorUtil.asIterable(column)){
                if (node.getProperty(Constants.PROPERTY_NAME).equals(Constants.NODE_DUMMYROOT))
                    parents.add(new RemoteBusinessObjectLight((long)-1, Constants.NODE_DUMMYROOT, Constants.NODE_DUMMYROOT));
                else{ 
                    if(node.hasRelationship(RelTypes.INSTANCE_OF, Direction.OUTGOING))
                        parents.add(Util.createRemoteObjectLightFromNode(node));
                    else //the node has a pool as a parent
                        parents.add(Util.createRemoteObjectLightFromPoolNode(node));
                }   
            }
        }
        return parents;
    }
    
    @Override
    public RemoteBusinessObject getParentOfClass(String objectClass, long oid, String parentClass) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, InvalidArgumentException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(objectClass, oid);

            while (true){
                //This method won't support CHILD_OF_SPECIAL relationships
                if (objectNode.hasRelationship(Direction.OUTGOING, RelTypes.CHILD_OF)){
                    Node parentNode = objectNode.getSingleRelationship(RelTypes.CHILD_OF, Direction.OUTGOING).getEndNode();

                    Label label = DynamicLabel.label(Constants.LABEL_ROOT); //If the parent node is the dummy root, just return null
                    if (parentNode.hasLabel(label) && Constants.NODE_DUMMYROOT.equals(parentNode.getProperty(Constants.PROPERTY_NAME)))
                        return null;
                    else {
                        String thisNodeClass = Util.getClassName(parentNode);
                        if (cm.isSubClass(parentClass, thisNodeClass))
                            return Util.createRemoteObjectFromNode(parentNode, cm.getClass(thisNodeClass));
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
            throws ObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException {

        try(Transaction tx = graphDb.beginTx()) {
            //TODO: Optimize so it can find all objects of a single class in one query
            for (String className : objects.keySet()){
                for (long oid : objects.get(className)){
                    if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, className))
                        throw new OperationNotPermittedException(String.format("Class %s is not a business-related class", className));

                    Node instance = getInstanceOfClass(className, oid);
                    Util.deleteObject(instance, releaseRelationships);
                }
            }
            tx.success();
        }
    }

    @Override
    public void deleteObject(String className, long oid, boolean releaseRelationships) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException, OperationNotPermittedException {
        try (Transaction tx = graphDb.beginTx()) {
            if (!cm.isSubClass(Constants.CLASS_INVENTORYOBJECT, className))
                        throw new OperationNotPermittedException(String.format("Class %s is not a business-related class", className));

            Node instance = getInstanceOfClass(className, oid);
            Util.deleteObject(instance, releaseRelationships);
            tx.success();
        }
    }

    @Override
    public ChangeDescriptor updateObject(String className, long oid, HashMap<String,List<String>> attributes)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, OperationNotPermittedException, 
                InvalidArgumentException {

        ClassMetadata myClass= cm.getClass(className);
        
        if (myClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));
        
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(className, oid);

            String oldValues = "", newValues = "", affectedProperties = "";

            for (String attributeName : attributes.keySet()){
                if(myClass.hasAttribute(attributeName)) {
                    affectedProperties = attributeName + " ";
                    if (AttributeMetadata.isPrimitive(myClass.getType(attributeName))) { // We are changing a primitive type, such as String, or int
                        oldValues += (instance.hasProperty(attributeName) ? String.valueOf(instance.getProperty(attributeName)) : null) + " ";
                        //If the array is empty or null, it means the attribute should be set to null
                        if (attributes.get(attributeName) == null || attributes.get(attributeName).isEmpty())
                            instance.removeProperty(attributeName);
                        else {
                            newValues += attributes.get(attributeName).get(0) + " ";
                            if (attributes.get(attributeName).get(0) == null)
                                instance.removeProperty(attributeName);
                            else
                                instance.setProperty(attributeName,Util.getRealValue(attributes.get(attributeName).get(0), myClass.getType(attributeName)));
                        }
                    } else { //If the attribute is not a primitive type, then it's a relationship
                        if (!cm.getClass(myClass.getType(attributeName)).isListType())
                            throw new InvalidArgumentException(String.format("Class %s is not a list type", myClass.getType(attributeName)));

                        //Release all previous relationships
                        oldValues += " "; //Two empty, separation spaces
                        for (Relationship rel : instance.getRelationships(Direction.OUTGOING, RelTypes.RELATED_TO)){
                            if (rel.getProperty(Constants.PROPERTY_NAME).equals(attributeName)){
                                oldValues += rel.getEndNode().getProperty(Constants.PROPERTY_NAME) + "-";
                                rel.delete();
                            }
                        }
                        if (attributes.get(attributeName) != null){ //If the new value is different than null, then create the new relationships

                            Node listTypeNode = classIndex.get(Constants.PROPERTY_NAME, myClass.getType(attributeName)).getSingle();
                            List<Node> listTypeNodes = Util.getRealValue(attributes.get(attributeName), listTypeNode);

                            //Create the new relationships
                            for (Node item : listTypeNodes){
                                newValues += item.getProperty(Constants.PROPERTY_NAME) + "-";
                                Relationship newRelationship = instance.createRelationshipTo(item, RelTypes.RELATED_TO);
                                newRelationship.setProperty(Constants.PROPERTY_NAME, attributeName);
                            }
                            newValues += " ";
                        }
                    }
                } else
                    throw new InvalidArgumentException(
                            String.format("The attribute %s does not exist in class %s", attributeName, className));
            }
            tx.success();
            return new ChangeDescriptor(affectedProperties.trim(), oldValues.trim(), newValues.trim(), String.valueOf(oid));
        }
    }

    @Override
    public boolean setBinaryAttributes(String className, long oid, List<String> attributeNames, List<byte[]> attributeValues)
            throws ObjectNotFoundException, OperationNotPermittedException, ArraySizeMismatchException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, long bObjectId, String name, boolean unique)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        
        createSpecialRelationship(aObjectClass, aObjectId, bObjectClass, bObjectId, name, unique, new HashMap<String, Object>());
    }
    
    @Override
    public void createSpecialRelationship(String aObjectClass, long aObjectId, String bObjectClass, 
            long bObjectId, String name, boolean unique, HashMap<String, Object> properties) throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        
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
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
        
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
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
        
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
    public void moveObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        ClassMetadata newParentClass = cm.getClass(targetClassName);
        
        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", targetClassName));
        
        try(Transaction tx = graphDb.beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            for (String myClass : objects.keySet()){
                if (!cm.canBeChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be child of an instance of class %s", myClass,targetClassName));

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
                }
            }
            tx.success();
        }
    }

    @Override
    public long[] copyObjects(String targetClassName, long targetOid, HashMap<String, long[]> objects, boolean recursive)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        ClassMetadata newParentClass = cm.getClass(targetClassName);

        if (newParentClass == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", targetClassName));

        try (Transaction tx = graphDb.beginTx()) {
            Node newParentNode = getInstanceOfClass(targetClassName, targetOid);
            long[] res = new long[objects.size()];
            int i = 0;
            for (String myClass : objects.keySet()){
                if (!cm.canBeChild(targetClassName, myClass))
                    throw new OperationNotPermittedException(String.format("An instance of class %s can not be child of an instance of class %s", myClass,targetClassName));

                Node instanceClassNode = classIndex.get(Constants.PROPERTY_NAME, myClass).getSingle();
                if (instanceClassNode == null)
                    throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", myClass));
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
    public boolean setObjectLockState(String className, long oid, Boolean value)
            throws ObjectNotFoundException, OperationNotPermittedException, MetadataObjectNotFoundException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<RemoteBusinessObjectLight> getObjectChildren(String className, long oid, int maxResults)
            throws ObjectNotFoundException, MetadataObjectNotFoundException  {
        try (Transaction tx =  graphDb.beginTx()) {
            Node parentNode;
            if(oid == -1)
                parentNode = specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
            else
                parentNode = getInstanceOfClass(className, oid);
            
            Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            Iterator<Relationship> instances = children.iterator();
            List<RemoteBusinessObjectLight> res = new ArrayList<>();

            if (maxResults > 0){
                int counter = 0;
                while(children.iterator().hasNext() && (counter < maxResults)){
                    counter++;
                    Node child = children.iterator().next().getStartNode();
                    res.add(new RemoteBusinessObjectLight(child.getId(),(String)child.getProperty(Constants.PROPERTY_NAME), Util.getClassName(child)));
                }
            }else{
                while(instances.hasNext()){
                    Node child = instances.next().getStartNode();
                    res.add(new RemoteBusinessObjectLight(child.getId(),(String)child.getProperty(Constants.PROPERTY_NAME), Util.getClassName(child)));
                }
            }
            
            Collections.sort(res);
            return res;
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getObjectChildren(long classId, long oid, int maxResults)
            throws ObjectNotFoundException, MetadataObjectNotFoundException  {
        try(Transaction tx = graphDb.beginTx()) {
            Node parentNode;
            if(oid == -1)
                parentNode = specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
            else
                parentNode = getInstanceOfClass(classId, oid);
            
            Iterable<Relationship> iterableChildren = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            Iterator<Relationship> children = iterableChildren.iterator();
            List<RemoteBusinessObjectLight> res = new ArrayList<>();
            if (maxResults > 0){
                int counter = 0;
                while(children.hasNext() && (counter < maxResults)){
                    counter++;
                    Node child = children.next().getStartNode();
                    res.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(Constants.PROPERTY_NAME), Util.getClassName(child)));
                }
            }else{
                while(children.hasNext()){
                    Node child = children.next().getStartNode();
                    res.add(new RemoteBusinessObjectLight(child.getId(), (String)child.getProperty(Constants.PROPERTY_NAME), Util.getClassName(child)));
                }
            }
            return res;
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getSiblings(String className, long oid, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()){
            Node node = getInstanceOfClass(className, oid);
            List<RemoteBusinessObjectLight> res = new ArrayList<>();
            
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
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getObjectsOfClassLight(String className, int maxResults)
            throws MetadataObjectNotFoundException, InvalidArgumentException {
        try(Transaction tx = graphDb.beginTx()) {
            Node classMetadataNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            if (classMetadataNode == null)
                throw new MetadataObjectNotFoundException(className);

            List<RemoteBusinessObjectLight> instances = new ArrayList<>();

            TraversalDescription traversal = graphDb.traversalDescription().breadthFirst().relationships(RelTypes.EXTENDS, Direction.INCOMING);
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
    }

    @Override
    public List<RemoteBusinessObject> getChildrenOfClass(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException {
        
        try (Transaction tx = graphDb.beginTx()) {
        
            Node parentNode = getInstanceOfClass(parentClass, parentOid);
            Iterable<Relationship> iterableChildren = parentNode.getRelationships(RelTypes.CHILD_OF,Direction.INCOMING);
            Iterator<Relationship> children = iterableChildren.iterator();
            List<RemoteBusinessObject> res = new ArrayList<>();
            int counter = 0;


            while(children.hasNext()){
                Node child = children.next().getStartNode();

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

            Iterable<Relationship> iterableSpecialChildren = parentNode.getRelationships(RelTypes.CHILD_OF_SPECIAL,Direction.INCOMING);
            Iterator<Relationship> specialChildren = iterableSpecialChildren.iterator();

            while(specialChildren.hasNext()){
                Node child = specialChildren.next().getStartNode();

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
    }

    @Override
    public List<RemoteBusinessObjectLight> getChildrenOfClassLight(long parentOid, String parentClass, String classToFilter, int maxResults)
            throws MetadataObjectNotFoundException, ObjectNotFoundException  {
        
        try (Transaction tx = graphDb.beginTx()) {
            Node parentNode = getInstanceOfClass(parentClass, parentOid);
            Iterable<Relationship> children = parentNode.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING);
            List<RemoteBusinessObjectLight> res = new ArrayList<>();

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

            Iterable<Relationship> iterableSpecialChildren = parentNode.getRelationships(RelTypes.CHILD_OF_SPECIAL, Direction.INCOMING);
            Iterator<Relationship> specialChildren = iterableSpecialChildren.iterator();

            while(specialChildren.hasNext()){
                Node child = specialChildren.next().getStartNode();

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
    }

    @Override
    public List<RemoteBusinessObjectLight> getSpecialAttribute(String objectClass, long objectId, String specialAttributeName) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(objectClass, objectId);
            List<RemoteBusinessObjectLight> res = new ArrayList<>();
            for (Relationship rel : instance.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                if(rel.hasProperty(Constants.PROPERTY_NAME)){
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(specialAttributeName))
                        res.add(rel.getEndNode().getId() == objectId ? 
                            Util.createRemoteObjectLightFromNode(rel.getStartNode()) : Util.createRemoteObjectLightFromNode(rel.getEndNode()));
                }
            }
            return res;
        }
    }

    @Override
    public List<AnnotatedRemoteBusinessObjectLight> getAnnotatedSpecialAttribute(String objectClass, long objectId, String specialAttributeName) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(objectClass, objectId);
            List<AnnotatedRemoteBusinessObjectLight> res = new ArrayList<>();
            for (Relationship rel : instance.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                if(rel.hasProperty(Constants.PROPERTY_NAME)){
                    if (rel.getProperty(Constants.PROPERTY_NAME).equals(specialAttributeName)) {
                        RemoteBusinessObjectLight theObject = rel.getEndNode().getId() == objectId ? 
                            Util.createRemoteObjectLightFromNode(rel.getStartNode()) : Util.createRemoteObjectLightFromNode(rel.getEndNode());
                        res.add(new AnnotatedRemoteBusinessObjectLight(theObject, rel.getAllProperties()));
                    }
                }
            }
            return res;
        }
    }
    
    @Override
    public HashMap<String,List<RemoteBusinessObjectLight>> getSpecialAttributes (String className, long objectId) 
        throws MetadataObjectNotFoundException, ObjectNotFoundException  {
        
        HashMap<String,List<RemoteBusinessObjectLight>> res = new HashMap<>();
        try(Transaction tx = graphDb.beginTx()) {
            Node objectNode = getInstanceOfClass(className, objectId);
            for (Relationship rel : objectNode.getRelationships(RelTypes.RELATED_TO_SPECIAL)){
                String relName = (String)rel.getProperty(Constants.PROPERTY_NAME);
                List<RemoteBusinessObjectLight> currentObjects = res.get(relName);
                if (currentObjects == null){
                    currentObjects = new ArrayList<>();
                    res.put(relName, currentObjects);
                }
                currentObjects.add(Util.createRemoteObjectLightFromNode(rel.getOtherNode(objectNode)));
            }
            return res;
        }
    }
    
    @Override
    public List<RemoteBusinessObjectLight> getObjectSpecialChildren(String objectClass, long objectId)
            throws MetadataObjectNotFoundException, ObjectNotFoundException  {
        
        try(Transaction tx = graphDb.beginTx()) {
            Node instance = getInstanceOfClass(objectClass, objectId);
            List<RemoteBusinessObjectLight> res = new ArrayList<>();
            for (Relationship rel : instance.getRelationships(Direction.INCOMING, RelTypes.CHILD_OF_SPECIAL)){
                if(rel.hasProperty(Constants.PROPERTY_NAME)){
                    if(rel.getProperty(Constants.PROPERTY_NAME).equals(Constants.REL_PROPERTY_POOL))
                        return res;
                }
                res.add(Util.createRemoteObjectLightFromNode(rel.getStartNode()));
            }
            return res;
        }
    }

    @Override
    public boolean hasRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships)
            throws ObjectNotFoundException, MetadataObjectNotFoundException {
        
        try(Transaction tx = graphDb.beginTx()) {
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
    }
    
    @Override
    public boolean hasSpecialRelationship(String objectClass, long objectId, String relationshipName, int numberOfRelationships) 
            throws ObjectNotFoundException, MetadataObjectNotFoundException  {
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
    
    //TODO DELETE. This is a business dependant method, should not be here. Don't use it
    @Override
    public List<RemoteBusinessObjectLight> getPhysicalPath(String objectClass, long objectId) {
        List<RemoteBusinessObjectLight> path = new ArrayList<>();
        
        //The first part of the query will return many paths, the longest is the one we need. The others are
        //subsets of the longest
        String cypherQuery = "MATCH paths = (o)-[r:" + RelTypes.RELATED_TO_SPECIAL + "*]-(c) "+
                             "WHERE id(o) = " + objectId + " AND all(rel in r where rel.name = 'mirror' or rel.name = 'endpointA' or rel.name = 'endpointB') "+
                             "WITH nodes(paths) as path " +
                             "RETURN path ORDER BY length(path) DESC LIMIT 1";
        try (Transaction tx = graphDb.beginTx()){
            
            Result result = graphDb.execute(cypherQuery);
            Iterator<List<Node>> column = result.columnAs("path");
            
            for (List<Node> listOfNodes : IteratorUtil.asIterable(column)) {
                for(Node node : listOfNodes)
                    path.add(Util.createRemoteObjectLightFromNode(node));
            }
        }
        return path;

    }

    @Override
    public List<RemoteBusinessObjectLightList> findRoutesThroughSpecialRelationships(String objectAClassName, 
            long objectAId, String objectBClassName, long objectBId, String relationshipName) {
        List<RemoteBusinessObjectLightList> paths = new ArrayList<>();
        String cypherQuery = String.format("MATCH path = a-[r:%s*1..10{name:\"%s\"}]-b " +
                            "WHERE id(a) = %s AND id(b) = %s AND all(x in nodes(path) where 1 = size (filter(y in nodes(path) where x = y))) " +
                            "RETURN nodes(path) as path LIMIT 10", RelTypes.RELATED_TO_SPECIAL, relationshipName, objectAId, objectBId);
                                
        try (Transaction tx = graphDb.beginTx()){
           
            Result result = graphDb.execute(cypherQuery);
            Iterator<List<Node>> column = result.columnAs("path");
            
            for (List<Node> list : IteratorUtil.asIterable(column)){
                RemoteBusinessObjectLightList aPath = new RemoteBusinessObjectLightList();
                for (Node aNode : list)
                    aPath.add(Util.createRemoteObjectLightFromNode(aNode));
                paths.add(aPath);
            }
        }
        return paths;
    }
    
    //<editor-fold desc="Reporting API implementation" defaultstate="collapsed">
        @Override
    public long createClassLevelReport(String className, String reportName, String reportDescription, 
            String script, int outputType, boolean enabled) throws MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            Node classNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
            
            if (classNode == null)
                throw new MetadataObjectNotFoundException(String.format("Class %s could not be found", className));
            
            Node newReport = graphDb.createNode();
            newReport.setProperty(Constants.PROPERTY_NAME, reportName == null ? "" : reportName);
            newReport.setProperty(Constants.PROPERTY_DESCRIPTION, reportDescription == null ? "" : reportDescription);
            newReport.setProperty(Constants.PROPERTY_SCRIPT, script == null ? "" : script);
            newReport.setProperty(Constants.PROPERTY_TYPE, Math.abs(outputType) > 4 ? RemoteReportLight.TYPE_HTML : outputType);
            newReport.setProperty(Constants.PROPERTY_ENABLED, enabled);
            
            classNode.createRelationshipTo(newReport, RelTypes.HAS_REPORT);
            reportsIndex.putIfAbsent(newReport, Constants.PROPERTY_ID, newReport.getId());
            
            tx.success();
            return newReport.getId();
        }
    }

    @Override
    public long createInventoryLevelReport(String reportName, String reportDescription, String script, 
            int outputType, boolean enabled, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node dummyRootNode = specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
            
            if (dummyRootNode == null)
                throw new ApplicationObjectNotFoundException("Dummy Root could not be found");
            
            Node newReport = graphDb.createNode();
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
            reportsIndex.putIfAbsent(newReport, Constants.PROPERTY_ID, newReport.getId());
            
            tx.success();
            return newReport.getId();
        }
    }

    @Override
    public void deleteReport(long reportId) throws ApplicationObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()){
            
            Node reportNode = reportsIndex.get(Constants.PROPERTY_ID, reportId).getSingle();
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId));
            
            for (Relationship rel : reportNode.getRelationships())
                rel.delete();

            reportsIndex.remove(reportNode, Constants.PROPERTY_ID, reportNode.getId());
            reportNode.delete();
            
            tx.success();
        }
    }

    @Override
    public void updateReport(long reportId, String reportName, String reportDescription, Boolean enabled,
            Integer type, String script) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node reportNode = reportsIndex.get(Constants.PROPERTY_ID, reportId).getSingle();
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId));
            
            if (reportName != null)
               reportNode.setProperty(Constants.PROPERTY_NAME, reportName);
            if (reportDescription != null)
               reportNode.setProperty(Constants.PROPERTY_DESCRIPTION, reportDescription);
            if (enabled != null)
               reportNode.setProperty(Constants.PROPERTY_ENABLED, enabled);
            if (type != null)
               reportNode.setProperty(Constants.PROPERTY_TYPE, type);
            if (script != null)
               reportNode.setProperty(Constants.PROPERTY_SCRIPT, script);

            tx.success();
        }
    }
    
    @Override
    public void updateReportParameters(long reportId, List<StringPair> parameters) throws ApplicationObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node reportNode = reportsIndex.get(Constants.PROPERTY_ID, reportId).getSingle();
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("A report with id %s could not be found", reportId));

            for (StringPair parameter : parameters) {
                
                if (parameter.getKey() == null || parameter.getKey().trim().isEmpty())
                        throw new InvalidArgumentException("Parameter names can not be empty strings");
                
                String actualParameterName = "PARAM_" + parameter.getKey();
                //The parameters are stored with a prefix PARAM_
                //params set to null, must be deleted
                if (reportNode.hasProperty(actualParameterName) && parameter.getValue() == null)
                    reportNode.removeProperty(actualParameterName);
                else
                    reportNode.setProperty(actualParameterName, parameter.getValue());
            }
            
            tx.success();
        }
    }

    @Override
    public List<RemoteReportLight> getClassLevelReports(String className, boolean recursive, boolean includeDisabled) throws MetadataObjectNotFoundException {
        try (Transaction tx = graphDb.beginTx()) {
            
            Node mainClassNode = classIndex.get(Constants.PROPERTY_NAME, className).getSingle();
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
            Node dummyRootNode = specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();
            
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
            Node reportNode = reportsIndex.get(Constants.PROPERTY_ID, reportId).getSingle();
            
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
    public byte[] executeClassLevelReport(String objectClassName, long objectId, long reportId) throws MetadataObjectNotFoundException, ApplicationObjectNotFoundException, ObjectNotFoundException, InvalidArgumentException {
        try (Transaction tx = graphDb.beginTx()) {
            Node reportNode = reportsIndex.get(Constants.PROPERTY_ID, reportId).getSingle();
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId)); 
            
            Node instanceNode = getInstanceOfClass(objectClassName, objectId);
            
            String script = (String)reportNode.getProperty(Constants.PROPERTY_SCRIPT);
            
            Binding environmentParameters = new Binding();
            environmentParameters.setVariable("instanceNode", instanceNode); //NOI18N
            environmentParameters.setVariable("graphDb", graphDb); //NOI18N
            environmentParameters.setVariable("objectIndex", objectIndex); //NOI18N
            environmentParameters.setVariable("classIndex", classIndex); //NOI18N
            environmentParameters.setVariable("defaultReports", defaultReports); //NOI18N
            
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
            Node reportNode = reportsIndex.get(Constants.PROPERTY_ID, reportId).getSingle();
            
            if (reportNode == null)
                throw new ApplicationObjectNotFoundException(String.format("The report with id %s could not be found", reportId)); 
                     
            String script = (String)reportNode.getProperty(Constants.PROPERTY_SCRIPT);
            
            HashMap<String, String> scriptParameters = new HashMap<>();
            for(StringPair parameter : parameters)
                scriptParameters.put(parameter.getKey(), parameter.getValue());
            
            Binding environmentParameters = new Binding();
            environmentParameters.setVariable("parameters", scriptParameters); //NOI18N
            environmentParameters.setVariable("graphDb", graphDb); //NOI18N
            environmentParameters.setVariable("objectIndex", objectIndex); //NOI18N
            environmentParameters.setVariable("classIndex", classIndex); //NOI18N
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
    /**
     * Helpers
     */
    /**
     * Boiler-plate code. Gets a particular instance given the class name and the oid. Callers must handle associated transactions
     * @param className object class name
     * @param oid object id
     * @return a Node representing the entity
     * @throws MetadataObjectNotFoundException id the class cannot be found
     * @throws org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException if the object could not be found
     */
    public Node getInstanceOfClass(String className, long oid) throws MetadataObjectNotFoundException, ObjectNotFoundException{
        
        //if any of the parameters is null, return the dummy root
        if (className == null || className.equals(Constants.NODE_DUMMYROOT))
            return specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();

        Node classNode = classIndex.get(Constants.PROPERTY_NAME,className).getSingle();

        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class %s can not be found", className));

        Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iterableInstances.iterator();

        while (instances.hasNext()){
            Node otherSide = instances.next().getStartNode();
            if (otherSide.getId() == oid)
                return otherSide;
        }
        throw new ObjectNotFoundException(className, oid);
        
    }
    /**
     * Boiler-plate code. Gets a particular instance given the class name and the oid
     * @param classId object class id
     * @param oid object id
     * @return a Node representing the entity
     * @throws MetadataObjectNotFoundException if the class cannot be found
     * @throws org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException
     */
    public Node getInstanceOfClass(long classId, long oid) throws MetadataObjectNotFoundException, ObjectNotFoundException{
                
        //if any of the parameters is null, return the dummy root
        if (classId == -1)
            return specialNodesIndex.get(Constants.PROPERTY_NAME, Constants.NODE_DUMMYROOT).getSingle();

        Node classNode = classIndex.get(Constants.PROPERTY_ID, classId).getSingle();

        if (classNode == null)
            throw new MetadataObjectNotFoundException(String.format("Class with id %s can not be found", classId));

        Iterable<Relationship> iteratorInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iteratorInstances.iterator();
        while (instances.hasNext()){
            Node otherSide = instances.next().getStartNode();
            if (otherSide.getId() == oid)
                return otherSide;
        }
        throw new ObjectNotFoundException((String)classNode.getProperty(Constants.PROPERTY_NAME), oid);
    }

    public Node getInstanceOfClass(Node classNode, long oid) throws ObjectNotFoundException{
        Iterable<Relationship> iterableInstances = classNode.getRelationships(RelTypes.INSTANCE_OF);
        Iterator<Relationship> instances = iterableInstances.iterator();
        
        while (instances.hasNext()){
            Node otherSide = instances.next().getStartNode();
            if (otherSide.getId() == oid)
                return otherSide;
        }
        throw new ObjectNotFoundException((String)classNode.getProperty(Constants.PROPERTY_NAME), oid);
    }
    
    public Node createObject(Node classNode, ClassMetadata classToMap, HashMap<String,List<String>> attributes) 
            throws InvalidArgumentException, MetadataObjectNotFoundException {
 
        if (classToMap.isAbstract())
            throw new InvalidArgumentException(String.format("Can not create objects from abstract classes (%s)", classToMap.getName()));
        
        Node newObject = graphDb.createNode();
        newObject.setProperty(Constants.PROPERTY_NAME, ""); //The default value is an empty string 

        newObject.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis()); //The default value is right now

        if (attributes != null){
            for(String attributeName : attributes.keySet()) {
                //If the array is empty, it means the attribute should be set to null, that is, ignore it
                if (!attributes.get(attributeName).isEmpty()){
                    if (attributes.get(attributeName).get(0) != null){
                        String attributeType = classToMap.getType(attributeName);
                        if (AttributeMetadata.isPrimitive(attributeType))
                                newObject.setProperty(attributeName, Util.getRealValue(attributes.get(attributeName).get(0), classToMap.getType(attributeName)));
                        else {
                        //If it's not a primitive type, maybe it's a relationship

                            if (!cm.isSubClass(Constants.CLASS_GENERICOBJECTLIST, attributeType))
                                throw new InvalidArgumentException(String.format("Type %s is not a primitive nor a list type", attributeName));

                            Node listTypeNode = classIndex.get(Constants.PROPERTY_NAME, attributeType).getSingle();

                            if (listTypeNode == null)
                                throw new InvalidArgumentException(String.format("Class %s could not be found as list type", attributeType));

                            List<Node> listTypeNodes = Util.getRealValue(attributes.get(attributeName), listTypeNode);

                            if (listTypeNodes.isEmpty())
                                throw new InvalidArgumentException(String.format("At least one of the list type items could not be found. Check attribute definition for \"%s\"", attributeName));

                            //Create the new relationships
                            for (Node item : listTypeNodes){
                                Relationship newRelationship = newObject.createRelationshipTo(item, RelTypes.RELATED_TO);
                                newRelationship.setProperty(Constants.PROPERTY_NAME, attributeName);
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
        
        newInstance.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
        
        objectIndex.putIfAbsent(newInstance, Constants.PROPERTY_ID, newInstance.getId());
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
    private Node copyTemplateElement(Node templateObject, boolean recursive) {
        
        Node newInstance = graphDb.createNode();
        for (String property : templateObject.getPropertyKeys())
            newInstance.setProperty(property, templateObject.getProperty(property));
        
        for (Relationship rel : templateObject.getRelationships(RelTypes.RELATED_TO, Direction.OUTGOING))
            newInstance.createRelationshipTo(rel.getEndNode(), RelTypes.RELATED_TO).setProperty(Constants.PROPERTY_NAME, rel.getProperty(Constants.PROPERTY_NAME));
        
        newInstance.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());
        
        objectIndex.putIfAbsent(newInstance, Constants.PROPERTY_ID, newInstance.getId());
        newInstance.createRelationshipTo(templateObject.getRelationships(RelTypes.INSTANCE_OF_SPECIAL).iterator().next().getEndNode(), RelTypes.INSTANCE_OF);

        if (recursive){
            for (Relationship rel : templateObject.getRelationships(RelTypes.CHILD_OF, Direction.INCOMING)){
                Node newChild = copyTemplateElement(rel.getStartNode(), true);
                newChild.createRelationshipTo(newInstance, RelTypes.CHILD_OF);
            }
        }
        return newInstance;
    }
    
    /**
     * This class wraps a set of attribute definitions necessary to create objects with default values
     */
    public class AttributeDefinitionSet implements Serializable{
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
}