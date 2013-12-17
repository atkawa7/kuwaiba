/**
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>.
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
                dummyRootNode.setProperty(Constants.PROPERTY_NAME, Constants.DUMMYROOT);
                dummyRootNode.setProperty(Constants.PROPERTY_DISPLAY_NAME, Constants.DUMMYROOT);
                dummyRootNode.setProperty(Constants.PROPERTY_CREATION_DATE, Calendar.getInstance().getTimeInMillis());

                if (graphDb.getReferenceNode() == null) {
                    throw new DatabaseException("Reference node does not exists. The database seems to be corrupted");
                }
                graphDb.getReferenceNode().createRelationshipTo(dummyRootNode, RelTypes.DUMMY_ROOT);
                tx.success();
            }finally {
                tx.finish();
            }
        }
    }
        
    public void checkIntegrity() {
        //check every attributes inheritance.
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
