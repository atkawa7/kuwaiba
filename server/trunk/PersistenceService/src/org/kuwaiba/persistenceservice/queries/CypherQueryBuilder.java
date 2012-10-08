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
import org.kuwaiba.persistenceservice.impl.RelTypes;
import org.kuwaiba.persistenceservice.util.Util;
import org.neo4j.graphdb.Node;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;

/**
 * Application Entity Manager reference implementation
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CypherQueryBuilder {

    //Cypher query paramas
    public Map<String, Object> params = new HashMap<String, Object>();
    public List<ResultRecord> resultList = new ArrayList<ResultRecord>();
    private String cypherMatch = "";
    private String cypherWhere = "";
    private String cypherReturn = "";

    public void readQuery(Map<String, Node> classNodes, ExtendedQuery query, String attributeJoinName, Boolean isJoin, Boolean isParent, Integer j, String connector){
        List<String> attributeNames = null;
        List<String> attributeValues = null;
        List<Integer> conditions = null;
        if(query != null){
            attributeNames = query.getAttributeNames();
            attributeValues = query.getAttributeValues();
            conditions = query.getConditions();
        }
        else{
            cypherMatch = cypherMatch.concat(listTypesMatch(isJoin, false, j));
            cypherWhere = cypherWhere.concat(createWhere(classNodes, attributeJoinName, j, null, null, null, isJoin, isParent)).concat(connector);
        }
        //nothing was selected
        if(attributeNames != null){
            for (int i = 0; i < attributeNames.size(); i++) {
                if (attributeValues.get(i) != null) {
                    if(attributeValues.get(i).equals("parent")){
                        readQuery(classNodes, query.getParent(), attributeNames.get(i), isJoin, true, j, connector);
                    }
                    cypherMatch = cypherMatch.concat(listTypesMatch(isJoin, false, j));
                    cypherWhere = cypherWhere.concat(createWhere(classNodes, attributeJoinName, j,conditions.get(i), attributeValues.get(i), attributeNames.get(i), isJoin, isParent)).concat(connector);
                }
                else{
                    cypherReturn = cypherReturn.concat(listTypeReturn(i));
                    if(query.getJoins() != null)
                        readQuery(classNodes, query.getJoins().get(i), attributeNames.get(i), true, isParent, i, connector);
                }
            }//end for
        }
    }

    public void createQuery(Map<String, Node> classNodes, Boolean isAbstract, ExtendedQuery query, Boolean hasParent, Boolean isParentAbstract){

        String cypherQuery;
        String returnNodes = " RETURN instance";//NOI18N
        params.put("className", query.getClassName());//NOI18N
        //Start
        cypherQuery = createStart(isAbstract);
        //Match
        if(query.getAttributeNames() == null)
            cypherMatch = instanceMatch(isAbstract, false, false);
        else{
            cypherQuery =  cypherQuery.concat(instanceMatch(isAbstract, hasParent, isParentAbstract));
            readQuery(classNodes, query, null, false, false, 0, query.getLogicalConnector() == ExtendedQuery.CONNECTOR_AND ? " AND " : "  OR ");
            cypherWhere = cypherWhere.substring(0, cypherWhere.length()-4);
        }
        cypherQuery = cypherQuery.concat(cypherMatch);
        //where
        if(cypherWhere.length() > 0)
            cypherQuery = cypherQuery.concat(" WHERE ");
        cypherQuery = cypherQuery.concat(cypherWhere);
        //Return
        if(query.getParent() != null)
            returnNodes = " RETURN instance, parent";
        cypherQuery = cypherQuery.concat(returnNodes).concat(cypherReturn);
        //limits
        if(query.getPage()>0)
            params.put("s", (query.getLimit() * (query.getPage() - 1)));//NOI18N
            params.put("l", query.getLimit());//NOI18N
        //Order By
        cypherQuery = cypherQuery.concat(" ORDER BY instance.name ASC");
        //0 = limit
        if(query.getPage()>0){
            params.put("s", (query.getLimit() * (query.getPage() - 1)));//NOI18N
            params.put("l", query.getLimit());//NOI18N
            cypherQuery = cypherQuery.concat(" skip {s} limit {l}");
        }

        executeQuery(classNodes.get("className"), cypherQuery, query);
    }

    public static String createStart(Boolean isAbstract){
        if(isAbstract)
            return "START abstractClassmetadata = node:classes(name = {className}) ";//NOI18N
        else
            return "START classmetadata = node:classes(name = {className}) ";//NOI18N
    }

    public static String instanceMatch(Boolean isAbstract, Boolean isParent, Boolean isParentAbstract){
        String match = "";
        if(isParent){
            if(isParentAbstract && isAbstract)
                match = "MATCH abstractClassmetadata<-[:" + RelTypes.EXTENDS + "*]-classmetadata<-[:" + RelTypes.INSTANCE_OF + "]-instance,"
                    + " instance-[:" + RelTypes.CHILD_OF + "*]->parent-[:" + RelTypes.INSTANCE_OF + "]-parentclassmetadata-[:"+ RelTypes.EXTENDS +"*]->abstractParentClassmetadata";//NOI18N
            else if(isAbstract)
                match = "MATCH abstractClassmetadata<-[:" + RelTypes.EXTENDS + "*]-classmetadata<-[:" + RelTypes.INSTANCE_OF + "]-instance,"
                    + "instance-[:" + RelTypes.CHILD_OF + "*]->parent-[:" + RelTypes.INSTANCE_OF + "]-parentclassmetadata";//NOI18N
            else
                match = "MATCH classmetadata<-[:" + RelTypes.INSTANCE_OF + "]-instance,"
                    + "instance-[:" + RelTypes.CHILD_OF + "*]->parent-[:" + RelTypes.INSTANCE_OF + "]-parentclassmetadata";//NOI18N
        }
        else{
            if(isAbstract)
                match = "MATCH abstractClassmetadata<-[:" + RelTypes.EXTENDS + "*]-classmetadata<-[:" + RelTypes.INSTANCE_OF + "]-instance";
            else
                match = "MATCH classmetadata<-[:" + RelTypes.INSTANCE_OF + "]-instance";
        }
        return match;
    }

    public static String listTypesMatch(Boolean isJoin, Boolean isParent, Integer i){
        String match = "";
        if(isJoin)
            match = match.concat(", instance-[r"+i+"?:"+RelTypes.RELATED_TO+"]->listType"+i);
        if(isParent && isJoin)
            match = match.concat(", parent-[r"+i+"?:"+RelTypes.RELATED_TO+"]->listType"+i);
        return match;
    }

    public String createWhere(Map<String, Node> classNodes, String attributePrincipalName, Integer i,
            Integer condition, String value, String attributeSecondaryName, Boolean isJoin, Boolean isParent)
    {
        String where = "";
        if(value != null){
            Node classNode = null;
            if(!isParent && !isJoin)
                classNode = classNodes.get("className");
            else if(isParent)
                classNode = classNodes.get("parent");
            else if(isJoin)
                classNode = classNodes.get("join"+i);
            else if(isParent && isJoin)
                classNode = classNodes.get("parentJoin"+i);

            String operator = "";
            if(value.equals("none")){
                where = "instance.".concat(attributeSecondaryName).concat(" is null ");
            }
            else if(value != null)
            {
                switch (condition) {
                    case ExtendedQuery.EQUAL:
                        operator = "! =~";//NOI18N
                        value = "(?i)".concat(value);//NOI18N
                        break;
                    case ExtendedQuery.EQUAL_OR_GREATER_THAN:
                        operator = "! >=";//NOI18N
                        break;
                    case ExtendedQuery.EQUAL_OR_LESS_THAN:
                        operator = "! <=";//NOI18N
                        break;
                    case ExtendedQuery.GREATER_THAN:
                        operator = "! >";//NOI18N
                        break;
                    case ExtendedQuery.LESS_THAN:
                        operator = "! <";//NOI18N
                        break;
                    case ExtendedQuery.LIKE:
                        operator = "! =~";//NOI18N
                        value = "(?i).*".concat(value).concat(".*");//NOI18N
                        break;
                }
                if(!isJoin && !isParent){
                    //No join
                    Object newParam = Util.evalAttributeType(Util.getTypeOfAttribute(classNode, attributeSecondaryName),attributeSecondaryName, value);
                    params.put(attributeSecondaryName, newParam);
                    if (Long.class.isInstance(newParam) || Boolean.class.isInstance(newParam) || Float.class.isInstance(newParam) || Integer.class.isInstance(newParam)){
                        if(condition == ExtendedQuery.EQUAL)
                            operator = operator.substring(0, operator.length() - 1);
                    }
                    where = "instance.".concat(attributeSecondaryName).concat(operator).concat(" {".concat(attributeSecondaryName).concat("}"));
                }
                if(isParent){
                    //parentclassmetadata.name = "City"  AND parent.name="bogota"
                    Object newParam = Util.evalAttributeType(Util.getTypeOfAttribute(classNode, attributePrincipalName),attributePrincipalName, value);
                    params.put(attributePrincipalName, newParam);
                    if (Long.class.isInstance(newParam) || Boolean.class.isInstance(newParam) || Float.class.isInstance(newParam) || Integer.class.isInstance(newParam)){
                        if(condition == ExtendedQuery.EQUAL)
                            operator = operator.substring(0, operator.length() - 1);
                    }
                    where = "parent.".concat(attributePrincipalName).concat(operator).concat("{").concat(attributePrincipalName).concat("}");
                }
                if (isJoin){
                    if (attributeSecondaryName.equals("id")) {//is small view
                        params.put("join".concat(i.toString()).concat(attributeSecondaryName), Long.valueOf(value.substring(4)));//take off the (?i)
                        params.put("rel".concat(i.toString()), attributePrincipalName);//the name of the relationship is the principal attibuteName selected
                        where = "r".concat(Integer.toString(i)).concat(".name={rel").concat(
                                Integer.toString(i)).concat("} AND ID(listType".concat(
                                Integer.toString(i)).concat(")").concat("=").concat(
                                " {join".concat(i.toString()).concat(attributeSecondaryName).concat("}")));//NOI18N
                    }
                    else{//Detail view
                        Object newJoinParam = Util.evalAttributeType(Util.getTypeOfAttribute(classNode, attributeSecondaryName),attributeSecondaryName, value);
                        params.put("join".concat(i.toString()).concat(attributeSecondaryName), newJoinParam);
                        params.put("rel".concat(i.toString()), attributePrincipalName);

                        if (Long.class.isInstance(newJoinParam) || Boolean.class.isInstance(newJoinParam) || Float.class.isInstance(newJoinParam) || Integer.class.isInstance(newJoinParam))
                            operator = operator.substring(0, operator.length() - 1);
                        //by every join it necesary to compare the relationship.name and the join attibutes in the listtype.property
                        where = "r".concat(i.toString()).concat(".name={rel").concat(i.toString()).concat("} AND listType".concat(i.toString()).concat(".").concat(attributeSecondaryName).concat(operator).concat(" {").concat("join".concat(i.toString()).concat(attributeSecondaryName)).concat("}"));
                    }
                }
            }//end if value is null this means something is checked as visible this matter if is ina join
        }
        else{//if nothing is selected in a join (isJoin && attributeSecondaryName == null)
            params.put("rel".concat(i.toString()), attributePrincipalName);
            where = "r".concat(i.toString()).concat(".name? ={rel").concat(i.toString()).concat("} AND listType").concat(i.toString()).concat(" ").concat("is null").concat("");
        }
        return where;
    }

    public static String listTypeReturn(Integer i){
        return ", listType".concat(i.toString());
    }

    public void executeQuery(Node classNode, String cypherQuery, ExtendedQuery query){
        //execute the cypher query
        ExecutionEngine engine = new ExecutionEngine(classNode.getGraphDatabase());
        ExecutionResult result = engine.execute(cypherQuery, params);

        Iterator<Map<String, Object>> columnsIterator = result.iterator();
        List<String> visibleAttributeNames = query.getVisibleAttributeNames();
        List<List<String>> joinVisibleAttributeNames = new ArrayList<List<String>>();
        List<List<String>> joinHeaderVisibleAttributeNames = new ArrayList<List<String>>();
        //Query Parent vissible Attribute names
        List<String> parentVisibleAttributeNames = null;
        if(query.getParent() != null)
            parentVisibleAttributeNames = query.getParent().getVisibleAttributeNames();

        List<List<String>> parentJoinVisibleAttributeNames = new ArrayList<List<String>>();
        List<List<String>> parentJoinHeaderVisibleAttributeNames = new ArrayList<List<String>>();
        
        Boolean isJoin = false;
        if(query.getJoins() != null ){
            //format the join Visible attribute
            for(ExtendedQuery join : query.getJoins()){
                if(join != null){
                    if(join.getVisibleAttributeNames() != null)
                    {
                        List<String> formatVisibleattributes = new ArrayList<String>();
                        for(String joinHeader: join.getVisibleAttributeNames()){
                            formatVisibleattributes.add(join.getClassName().concat(".").concat(joinHeader));
                        }
                        joinHeaderVisibleAttributeNames.add(formatVisibleattributes);
                        joinVisibleAttributeNames.add(join.getVisibleAttributeNames());
                    }
                    else{
                        List<String> formatVisibleattributes = new ArrayList<String>();
                        formatVisibleattributes.add(join.getClassName().concat(".").concat("name"));
                        joinHeaderVisibleAttributeNames.add(formatVisibleattributes);
                        List emptyJoinVisibleAttributes = new ArrayList<String>();
                        emptyJoinVisibleAttributes.add("name");
                        joinVisibleAttributeNames.add(emptyJoinVisibleAttributes);
                    }
                }//end if the joins is not null
                else{
//                    List<String> formatVisibleattributes = new ArrayList<String>();
//                    formatVisibleattributes.add("no ".concat(query.getAttributeNames().get(h)));
//                    joinHeaderVisibleAttributeNames.add(formatVisibleattributes);
                    List emptyJoinVisibleAttributes = new ArrayList<String>();
                    emptyJoinVisibleAttributes.add("name");
                    joinVisibleAttributeNames.add(emptyJoinVisibleAttributes);
                }
                isJoin = true;
            }//end for joins
        }//if joins is not null
        if(query.getParent() != null){
            for(ExtendedQuery join : query.getParent().getJoins()){
                if(join != null){
                    if(join.getVisibleAttributeNames() != null)
                    {
                        List<String> formatVisibleattributes = new ArrayList<String>();
                        for(String joinHeader: join.getVisibleAttributeNames()){
                            formatVisibleattributes.add(join.getClassName().concat(".").concat(joinHeader));
                        }
                        parentJoinHeaderVisibleAttributeNames.add(formatVisibleattributes);
                        parentJoinVisibleAttributeNames.add(join.getVisibleAttributeNames());
                    }
                    else{
                        List<String> formatVisibleattributes = new ArrayList<String>();
                        formatVisibleattributes.add(join.getClassName().concat(".").concat("name"));
                        parentJoinHeaderVisibleAttributeNames.add(formatVisibleattributes);
                        List emptyJoinVisibleAttributes = new ArrayList<String>();
                        emptyJoinVisibleAttributes.add("name");
                        parentJoinVisibleAttributeNames.add(emptyJoinVisibleAttributes);
                    }
                }
            }//end for
        }
        //headers of result
        if (visibleAttributeNames == null) {
            visibleAttributeNames = new ArrayList<String>();
            visibleAttributeNames.add("name");
        }
        //Query Results
        List<ResultRecord> onlyResults =  new ArrayList<ResultRecord>();
        ResultRecord rr= null;
        
        while(columnsIterator.hasNext()){
            Map<String, Object> column = columnsIterator.next();
            List<String> extraColumns = new ArrayList<String>();
            Node instanceNode = (Node)column.get("instance");

            String objectName = "";
            for(String van: visibleAttributeNames){
                extraColumns.add(Util.getAttributeFromNode(instanceNode, van));
                if (van.equals("name"))
                    objectName = extraColumns.get(extraColumns.size()-1);
            }
            rr = new ResultRecord(instanceNode.getId(), objectName ,Util.getClassName(instanceNode));
            if(joinVisibleAttributeNames!=null && isJoin && joinVisibleAttributeNames.size()>0){
                int t = 0;
                String [] joinReturns = cypherReturn.split(",");
                for(int k=1; k<joinReturns.length; k++){
                    Node joinNode = (Node)column.get(joinReturns[k].trim());//get the column
                    for( String jvan : joinVisibleAttributeNames.get(t)){
                        if(joinNode != null)
                            extraColumns.add(Util.getAttributeFromNode((Node)joinNode, jvan));
                    }
                    t++;
                }
            }
            rr.setExtraColumns(extraColumns);
            onlyResults.add(rr);
        }
        //headers of result
        if(isJoin){
            for(List<String> visibleAttributes: joinHeaderVisibleAttributeNames){
                for(String joinVisibleAttribute: visibleAttributes)
                    visibleAttributeNames.add(joinVisibleAttribute);
                }
        }
        ResultRecord resltRcrdHeader = new ResultRecord(0, null, null);
        resltRcrdHeader.setExtraColumns(visibleAttributeNames);
        resultList.add(resltRcrdHeader);

        if(onlyResults.size()>0){
            for(ResultRecord orr: onlyResults)
                resultList.add(orr);
        }
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public List<ResultRecord> getResultList() {
        return resultList;
    }


}
