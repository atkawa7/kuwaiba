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

package org.kuwaiba.persistenceservice.queries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.apis.persistence.application.ResultRecord;
import org.kuwaiba.persistenceservice.impl.MetadataEntityManagerImpl;
import org.kuwaiba.persistenceservice.util.Util;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.Node;

/**
 * Creates cypher simple Query
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CypherSimpleQuery {

    /**
     *
     */
    public Map<String, Node> classNodes = new HashMap<String, Node>();
    /**
     *
     */
    public Map<String, List<String>> vissibleAttributes = new HashMap<String, List<String>>();
    /**
     *
     */
    List defaultVisibleAttributes = new ArrayList<String>() {{add("name"); }};
    /**
     *
     */
    private CypherParser parser = new CypherParser();
    /**
     *
     */
    private String match = "";
    /**
     *
     */
    private String where = "";
    /**
     *
     */
    private String _return = "";
    /**
     *
     */
    CypherParser cp = new CypherParser();
    /**
     *
     */
    public List<ResultRecord> resultList = new ArrayList<ResultRecord>();

    /**
     *
     * @param listTypeName
     * @param listTypeName2
     * @param query
     */
    public void readParent(String listTypeName, String listTypeName2, ExtendedQuery query){
        Node classNode = classNodes.get(query.getClassName());

        match = match.concat(cp.createParentMatch());
        where = where.concat(cp.createParentRelation(query.getClassName()));
        _return = ", parent";

        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                    if(query.getAttributeValues().get(i) != null){
                        where = where.concat(parser.createParentWhere(query.getConditions().get(i), listTypeName,
                                                            query.getAttributeNames().get(i),
                                                            query.getAttributeValues().get(i),
                                                            Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                                            ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
                    }
                    else
                        readJoins(query.getAttributeNames().get(i)+"_P", listTypeName, query.getJoins().get(i));
            }//end for
        }//end if
    }

    /**
     *
     * @param listTypeName
     * @param listTypeName2
     * @param query
     */
    public void readJoins(String listTypeName, String listTypeName2, ExtendedQuery query){
        
        Node classNode = classNodes.get(query.getClassName());
        match = match.concat(cp.createListypeMatch(listTypeName, listTypeName2));
        where = where.concat(cp.createJoinRelation(listTypeName));
        _return = _return.concat(", listType_"+listTypeName);

        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                    if(query.getAttributeValues().get(i) != null){
                        where = where.concat(parser.createJoinWhere(query.getConditions().get(i), listTypeName,
                                                            query.getAttributeNames().get(i),
                                                            query.getAttributeValues().get(i),
                                                            Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                                            ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
                    }
                    else
                        readJoins(query.getAttributeNames().get(i), listTypeName, query.getJoins().get(i));
            }//end for
        }//end if
    }

    /**
     *
     * @param listTypeName
     * @param listTypeName2
     * @param query
     */
    public void readJoinQuery(String listTypeName, String listTypeName2, ExtendedQuery query){

        Node classNode = classNodes.get(query.getClassName());

        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getAttributeValues().get(i) != null){
                    where = where.concat(parser.createJoinWhere(query.getConditions().get(i), listTypeName,
                                                        query.getAttributeNames().get(i),
                                                        query.getAttributeValues().get(i),
                                                        Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                                        ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
                }
                else
                   readJoins(query.getAttributeNames().get(i), listTypeName, query);
            }
            
        }
    }

    /**
     *
     * @param query
     */
    public void readQuery(ExtendedQuery query){
        Node classNode = classNodes.get(query.getClassName());
        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getAttributeValues().get(i) != null)
                    where = where.concat(parser.createWhere(query.getConditions().get(i),
                                                            query.getAttributeNames().get(i),
                                                            query.getAttributeValues().get(i),
                                                            Util.getTypeOfAttribute(classNode, query.getAttributeNames().get(i))
                                                            ).concat(query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR "));
               else{
                    if( query.getAttributeNames().get(i).equalsIgnoreCase("parent"))
                        readParent(query.getAttributeNames().get(i), "", query.getJoins().get(i));
                    else
                        readJoins(query.getAttributeNames().get(i), "", query.getJoins().get(i));
                }
            }//end for
        }//end if
    }

    /**
     *
     * @param query
     */
    public void readVissibleAttributes(ExtendedQuery query){
        if(query.getVisibleAttributeNames() != null)
             vissibleAttributes.put("instance", query.getVisibleAttributeNames());
        else
            vissibleAttributes.put("instance", defaultVisibleAttributes);

        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getAttributeValues().get(i) == null)
                    if( query.getAttributeNames().get(i).equalsIgnoreCase("parent"))
                        readVissibleAttributeParent(query.getJoins().get(i));
                    else
                        readVissibleAttributeJoins(query.getAttributeNames().get(i), query.getJoins().get(i));
            }
        }
    }

    /**
     * 
     * @param query
     */
    public void readVissibleAttributeParent(ExtendedQuery query){
        if(query.getVisibleAttributeNames() != null)
            vissibleAttributes.put("parent", query.getVisibleAttributeNames());
        else
            vissibleAttributes.put("parent", defaultVisibleAttributes);


        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getJoins().get(i) != null)
                    readVissibleAttributeJoins(query.getAttributeNames().get(i), query.getJoins().get(i));
            }
        }
        //listTypeName, listTypeName2;
    }

    /**
     * 
     * @param listTypeName
     * @param query
     */
    public void readVissibleAttributeJoins(String listTypeName, ExtendedQuery query){
         if(query.getVisibleAttributeNames() != null)
             vissibleAttributes.put("listType_"+listTypeName, query.getVisibleAttributeNames());
        else{
            vissibleAttributes.put("listType_"+listTypeName, defaultVisibleAttributes);
        }

        if(query.getAttributeNames() != null){
            for(int i=0; i<query.getAttributeNames().size(); i++){
                if(query.getJoins().get(i) != null)
                    readVissibleAttributeJoins(query.getAttributeNames().get(i), query.getJoins().get(i));
            }
        }
    }

    /**
     * 
     * @param query
     */
    public void createQuery(ExtendedQuery query){

        Node classNode = classNodes.get(query.getClassName());
        boolean isAbstract = (Boolean) classNode.getProperty(MetadataEntityManagerImpl.PROPERTY_ABSTRACT);

        String cypherQuery = cp.createStart(query.getClassName(), isAbstract);
        cypherQuery = cypherQuery.concat(cp.createInstanceMatch(isAbstract));
        readQuery(query);
        if(!match.isEmpty())
            cypherQuery = cypherQuery.concat(match);
        if(!where.isEmpty())
            cypherQuery = cypherQuery.concat(" WHERE ".concat(where.substring(0, where.length()-4)));

        cypherQuery = cypherQuery.concat(" RETURN ".concat(cp.createReturn()).concat(_return));

        cypherQuery = cypherQuery.concat(" ORDER BY instance.name ASC");
        if(query.getPage()>0){
            cypherQuery = cypherQuery.concat(" skip 0 limit 10");//NOI18N
        }

        readVissibleAttributes(query);
        executeQuery(classNode, cypherQuery);
    }

    /**
     * 
     * @param classNode
     * @param cypherQuery
     */
    public void executeQuery(Node classNode, String cypherQuery){
        
        ExecutionEngine engine = new ExecutionEngine(classNode.getGraphDatabase());
        ExecutionResult result = engine.execute(cypherQuery, new HashMap<String, Object>());
        readResult(result.iterator());
    }

    /**
     * 
     * @param columnsIterator
     */
    public void readResult(Iterator<Map<String, Object>> columnsIterator){
        List<ResultRecord> onlyResults =  new ArrayList<ResultRecord>();
        ResultRecord rr= null;
        List<String> vissibleAttibutesTitles = new ArrayList<String>();

        //Iterator it = vissibleAttributes.entrySet().iterator();
        //while (it.hasNext()) {
            //Map.Entry e = (Map.Entry)it.next();

            for(String va: (List<String>)vissibleAttributes.get("instance"))
                vissibleAttibutesTitles.add(va);

            while(columnsIterator.hasNext()){
                Map<String, Object> column = columnsIterator.next();
                List<String> extraColumns = new ArrayList<String>();
                Node instanceNode = (Node)column.get("instance");//(String)e.getKey()

                for(String va: (List<String>)vissibleAttibutesTitles){
                    if(va.equals("id"))
                        extraColumns.add(Long.toString(instanceNode.getId()));
                    else
                        extraColumns.add(Util.getAttributeFromNode(instanceNode, va));

                }
                rr = new ResultRecord(instanceNode.getId(), Util.getAttributeFromNode(instanceNode,"name") ,Util.getClassName(instanceNode));

                rr.setExtraColumns(extraColumns);
                onlyResults.add(rr);
            }
        //}

        ResultRecord resltRcrdHeader = new ResultRecord(0, null, null);
        resltRcrdHeader.setExtraColumns(vissibleAttibutesTitles);
        resultList.add(resltRcrdHeader);

        if(onlyResults.size()>0){
            for(ResultRecord orr: onlyResults)
                resultList.add(orr);
        }
    }

    public List<ResultRecord> getResultList() {
        return resultList;
    }

    public void setClassNodes(Map<String, Node> classNodes) {
        this.classNodes = classNodes;
    }
}
