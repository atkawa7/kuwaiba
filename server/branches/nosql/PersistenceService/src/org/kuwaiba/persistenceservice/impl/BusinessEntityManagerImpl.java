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

import java.rmi.server.RemoteObject;
import java.util.List;
import org.kuwaiba.apis.persistence.RemoteObjectLight;
import org.kuwaiba.apis.persistence.ResultRecord;
import org.kuwaiba.apis.persistence.exceptions.ArraySizeMismatchException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.NotAuthorizedException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectWithRelationsException;
import org.kuwaiba.apis.persistence.exceptions.OperationNotPermittedException;
import org.kuwaiba.apis.persistence.exceptions.WrongMappingException;
import org.kuwaiba.apis.persistence.interfaces.BusinessEntityManager;
import org.kuwaiba.persistenceservice.impl.enumerations.RelTypes;
import org.kuwaiba.persistenceservice.util.Util;
import org.kuwaiba.psremoteinterfaces.BusinessEntityManagerRemote;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;

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


    private BusinessEntityManagerImpl() {
    }

    public BusinessEntityManagerImpl(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
        this.classIndex = graphDb.index().forNodes(MetadataEntityManagerImpl.INDEX_CLASS);
        this.objectIndex = graphDb.index().forNodes(INDEX_OBJECTS);
    }



    public RemoteObjectLight createObject(String className, Long parentOid, List<String> attributeNames, List<String> attributeValues, String template)
            throws ClassNotFoundException, ObjectNotFoundException, ArraySizeMismatchException, NotAuthorizedException, OperationNotPermittedException {

        if (attributeNames.size() != attributeValues.size())
            throw new ArraySizeMismatchException("Attribute Names","Attribute Values"); //NOI18N
        
        Node classNode = classIndex.get(MetadataEntityManagerImpl.PROPERTY_NAME,className).getSingle();
        if (classNode == null)
            throw new ClassNotFoundException("Class "+className+" can not be found");
        
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
            String name = null;
            for (int i = 0; i<attributeValues.size();i++){
                try{
                   int type = Util.getTypeOfAttribute(classNode, attributeNames.get(i));
                   if (type == 0)
                       throw new WrongMappingException(className, className, template, template);
                   
                   Object value = Util.getRealValue(attributeValues.get(i), type);
                   newObject.setProperty(attributeNames.get(i), value);

                   if (attributeNames.get(i).equals(MetadataEntityManagerImpl.PROPERTY_NAME)) //NOI18N
                       name = attributeValues.get(i);
                }catch(InvalidArgumentException ex){
                    ex.printStackTrace();
                }
            }
            
            tx.success();
            return new RemoteObjectLight(new Long(newObject.getId()), name);
        }catch(Exception ex){
            ex.printStackTrace();
            tx.failure();
            return null;
        }
    }

    public RemoteObject getObjectInfo(String className, Long oid) throws ClassNotFoundException, ObjectNotFoundException, NotAuthorizedException, OperationNotPermittedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public RemoteObjectLight getObjectInfoLight(String className, Long oid) throws ClassNotFoundException, ObjectNotFoundException, NotAuthorizedException, OperationNotPermittedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean deleteObject(Long oid) throws ObjectWithRelationsException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean updateObject(String className, Long oid, List<String> attributeNames, List<String> attributeValues) throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, ArraySizeMismatchException, WrongMappingException, NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean setBinaryAttributes(String className, Long oid, List<String> attributeNames, List<byte[]> attributeValues) throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, ArraySizeMismatchException, NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean setManyToManyAttribute(String className, Long oid, String attributeTypeClassName, String attributeName, List<Long> attributeValues) throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean moveObjects(List<String> classNames, List<Long> oids, String targetClassName, Long targetOid) throws ClassNotFoundException, ObjectNotFoundException, OperationNotPermittedException, NotAuthorizedException, ArraySizeMismatchException {
        throw new UnsupportedOperationException("Not supported yet.");
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