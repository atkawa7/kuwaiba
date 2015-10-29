/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.persistenceservice.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.persistenceservice.util.Constants;
import org.kuwaiba.persistenceservice.util.Util;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;

/**
 * Creates cypher Query
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CypherQueryBuilder {
    public static final String INSTANCE = "instance"; //NOI18N
    public static final String PARENT = "parent"; //NOI18N
    public static final String LISTTYPE = "listType_"; //NOI18N
    /**
     * nodes selected for query
     */
    public Map<String, Node> classNodes = new HashMap<String, Node>();
    /**
     * attributes selected
     */
    public Map<String, List<String>> visibleAttributes = new HashMap<String, List<String>>();
    /**
     * if has not selected attribute, name is taken as default visible attribute
     */
    private List defaultVisibleAttributes = new ArrayList<String>();
    /**
     * match statements
     */
    private String match = "";
    /**
     * where statements
     */
    private String where = "";
    /**
     * return statements
     */
    private String _return = "";
    /**
     * Cypher parser to execute the queries
     */
    private CypherParser cp;
    /**
     * result list
     */
    public List<ResultRecord> resultList = new ArrayList<ResultRecord>();

    /**
     * read the parent and his joins
     * @param listTypeName
     * @param listTypeName2
     * @param query
     */
    public void readParent(String listTypeName, String listTypeName2, ExtendedQuery query){
        Node classNode = classNodes.get(query.getClassName());

        match = match.concat(cp.createParentMatch());
        where = where.concat(cp.createParentRelation(query.getClassName()));
        _return = _return.concat(", ".concat(PARENT));

        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                    if(query.getAttributeValues().get(i) != null){
                        where = where.concat(cp.createParentWhere(query.getConditions().get(i), listTypeName,
                                                            query.getAttributeNames().get(i),
                                                            query.getAttributeValues().get(i),
                                                            Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                                            ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
                    }
                    else{
                        readJoins(query.getAttributeNames().get(i)+"_P", listTypeName, query.getJoins().get(i));
                    }
            }//end for
        }//end if
    }

    /**
     * read the sub queries (joins)
     * @param listTypeName
     * @param listTypeName2
     * @param query
     */
    public void readJoins(String listTypeName, String listTypeName2, ExtendedQuery query){
        
        Node classNode = classNodes.get(query.getClassName());
        match = match.concat(cp.createListypeMatch(listTypeName, listTypeName2));
        where = where.concat(cp.createJoinRelation(listTypeName));
        _return = _return.concat(", ").concat(LISTTYPE).concat(listTypeName);

        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                    if(query.getAttributeValues().get(i) != null){
                        where = where.concat(cp.createJoinWhere(query.getConditions().get(i), listTypeName,
                                                            query.getAttributeNames().get(i),
                                                            query.getAttributeValues().get(i),
                                                            Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                                            ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
                    }
                    else{
                        readJoins(query.getAttributeNames().get(i), listTypeName, query.getJoins().get(i));
                    }
            }//end for
        }//end if
    }

    /**
     * reads the sub sub queries
     * @param listTypeName
     * @param listTypeName2
     * @param query
     */
    public void readJoinQuery(String listTypeName, String listTypeName2, ExtendedQuery query){
        Node classNode = classNodes.get(query.getClassName());
        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getAttributeValues().get(i) != null){
                    where = where.concat(cp.createJoinWhere(query.getConditions().get(i), listTypeName,
                                            query.getAttributeNames().get(i),
                                            query.getAttributeValues().get(i),
                                            Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                            ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
                }
                else{
                   readJoins(query.getAttributeNames().get(i), listTypeName, query);
                }
            }
        }
    }

    /**
     * reads the query main recursively
     * @param query
     */
    public void readQuery(ExtendedQuery query){
        _return = cp.createReturn();
        Node classNode = classNodes.get(query.getClassName());
        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getAttributeValues().get(i) != null){
                    where = where.concat(cp.createWhere(query.getConditions().get(i),
                                                            query.getAttributeNames().get(i),
                                                            query.getAttributeValues().get(i),
                                                            Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                                            ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
                }
               else{
                    if( query.getAttributeNames().get(i).equalsIgnoreCase(PARENT)){
                        readParent(query.getAttributeNames().get(i), "", query.getJoins().get(i));
                    }
                    else{
                        readJoins(query.getAttributeNames().get(i), "", query.getJoins().get(i));
                    }
                }
            }//end for
        }//end if
    }

    /**
     * Reads the visible attributes for the main query and its joins
     * @param query
     */
    public void readVissibleAttributes(ExtendedQuery query){
        if(query.getVisibleAttributeNames() != null)
             visibleAttributes.put(INSTANCE, query.getVisibleAttributeNames());
        else
            visibleAttributes.put(INSTANCE, new ArrayList<String>());
        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getAttributeValues().get(i) == null){
                    if(query.getAttributeNames().get(i).equalsIgnoreCase(PARENT)){
                        readVissibleAttributeParent(query.getJoins().get(i));
                    }
                    else{
                        readVissibleAttributeJoins(query.getAttributeNames().get(i), query.getJoins().get(i));
                    }
                }
            }//end for
        }
    }

    /**
     * Reads the visible attributes for the parent and its joins
     * @param query
     */
    public void readVissibleAttributeParent(ExtendedQuery query){
        if(query.getVisibleAttributeNames() != null)
            visibleAttributes.put(PARENT, query.getVisibleAttributeNames());
        else
            visibleAttributes.put(PARENT, new ArrayList<String>() {{ add("name");}});
        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getJoins().get(i) != null){
                    readVissibleAttributeJoins(query.getAttributeNames().get(i)+"_P", query.getJoins().get(i));
                }
            }
        }//listTypeName, listTypeName2;
    }

    /**
     * 
     * @param listTypeName
     * @param query
     */
    public void readVissibleAttributeJoins(String listTypeName, ExtendedQuery query){
        if(query.getVisibleAttributeNames() != null)
             visibleAttributes.put(LISTTYPE.concat(listTypeName), query.getVisibleAttributeNames());
        else
            visibleAttributes.put(LISTTYPE.concat(listTypeName), new ArrayList<String>() {{ add("name");}});
        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getJoins().get(i) != null){
                    readVissibleAttributeJoins(query.getAttributeNames().get(i), query.getJoins().get(i));
                }
            }
        }
    }

    /**
     * Creates the query
     * @param query 
     */
    public void createQuery(ExtendedQuery query){

        cp = new CypherParser();
        Node classNode = classNodes.get(query.getClassName());
        boolean isAbstract = (Boolean) classNode.getProperty(Constants.PROPERTY_ABSTRACT);

        String cypherQuery = cp.createStart(query.getClassName(), isAbstract);
        cypherQuery = cypherQuery.concat(cp.createInstanceMatch(isAbstract));
        readQuery(query);
        if(!match.isEmpty())
            cypherQuery = cypherQuery.concat(match);
        if(!where.isEmpty())
            cypherQuery = cypherQuery.concat(" WHERE ".concat(where.substring(0, where.length()-4)));
        
        cypherQuery = cypherQuery.concat(" RETURN ".concat(_return));

        cypherQuery = cypherQuery.concat(" ORDER BY instance.name ASC");
        if(query.getPage()>0){
            cypherQuery = cypherQuery.concat(" skip 0 limit 10");//NOI18N
        }

        readVissibleAttributes(query);
        executeQuery(classNode, cypherQuery);
    }

    /**
     * Executes the query
     * @param classNode
     * @param cypherQuery
     */
    public void executeQuery(Node classNode, String cypherQuery){
        ExecutionEngine engine = new ExecutionEngine(classNode.getGraphDatabase());
        ExecutionResult result = engine.execute(cypherQuery, new HashMap<String, Object>());
        readResult(result.iterator());
    }

    /**
     * Read the results
     * @param columnsIterator
     */
    public void readResult(Iterator<Map<String, Object>> columnsIterator){
        List<ResultRecord> onlyResults =  new ArrayList<ResultRecord>();
        ResultRecord rr;
        List<String> vissibleAttibutesTitles = new ArrayList<String>();

        String[] split = _return.split(", ");
        for(int g = 0; g < split.length; g++){
            for(String va: (List<String>)visibleAttributes.get(split[g]))
                vissibleAttibutesTitles.add(va);
        }

        while(columnsIterator.hasNext()){//interates by row
            Map<String, Object> column = columnsIterator.next();
            List<String> extraColumns = new ArrayList<String>();
            //create the class
            Node instanceNode = (Node)column.get(split[0]);
            rr = new ResultRecord(instanceNode.getId(), Util.getAttributeFromNode(instanceNode, Constants.PROPERTY_NAME) ,Util.getClassName(instanceNode));
            //iterates by column
            for(int lu=  0; lu <split.length; lu++){
                for(String va: (List<String>)visibleAttributes.get(split[lu])){
                    Node node = (Node)column.get(split[lu]);
                    if(va.equals(Constants.PROPERTY_ID)){
                        extraColumns.add(Long.toString(node.getId()));
                    }
                    else{
                        extraColumns.add(Util.getAttributeFromNode(node, va));
                    }
                }
            }
            rr.setExtraColumns(extraColumns);
            onlyResults.add(rr);
        }
        ResultRecord resltRcrdHeader = new ResultRecord(0, null, null);
        resltRcrdHeader.setExtraColumns(vissibleAttibutesTitles);
        resultList.add(resltRcrdHeader);

        if(onlyResults.size() > 0){
            for(ResultRecord orr: onlyResults){
                resultList.add(orr);
            }
        }
    }

    public List<ResultRecord> getResultList() {
        return resultList;
    }

    public void setClassNodes(Map<String, Node> classNodes) {
        this.classNodes = classNodes;
    }
}
