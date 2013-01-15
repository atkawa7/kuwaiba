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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import org.kuwaiba.apis.persistence.application.ExtendedQuery;
import org.kuwaiba.persistenceservice.impl.ApplicationEntityManagerImpl;
import org.kuwaiba.persistenceservice.impl.RelTypes;

/**
 * Cypher parser
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class CypherParser {

        
    /**
     * creates the start for the cypher query
     * @param isAbstract if the class is an abstract classMetadata
     * @return a query part
     */
    public String createStart(String className, boolean isAbstract){
         if(isAbstract)
            return "START abstractClassmetadata = node:classes(name = \""+ className +"\") ";//NOI18N
        else
            return "START classmetadata = node:classes(name = \""+ className + "\") ";//NOI18N
    }

    /**
     * creates the match when is only joins
     * @param isAbstract
     * @return
     */
    public String createInstanceMatch(boolean isAbstract){
        if(isAbstract)
            return "MATCH abstractClassmetadata<-[:" + RelTypes.EXTENDS + "*]-classmetadata<-[:" + RelTypes.INSTANCE_OF + "]-instance";
        else
            return "MATCH classmetadata<-[:" + RelTypes.INSTANCE_OF + "]-instance";
    }

     /**
     * Adds the match when the query has parent
     * @param isAbstract
     * @return
     */
    public String createParentMatch(){
        return ", instance-[:" + RelTypes.CHILD_OF + "*]->parent-[:" + RelTypes.INSTANCE_OF + "]-parentclassmetadata";//NOI18N
    }
    /**
     * add every listype into de the match a listType for every join
     * @param listTypeName
     * @param listTypeName2
     * @return
     */
    public String createListypeMatch(String listTypeName, String listTypeName2){
        if(listTypeName2.isEmpty())
            return ", instance-[r_"+listTypeName+"?:"+RelTypes.RELATED_TO+"]->listType_"+listTypeName;
        if(listTypeName2.equalsIgnoreCase("parent"))
            return ", "+listTypeName2+"-[r_"+listTypeName+"?:"+RelTypes.RELATED_TO+"]->listType_"+listTypeName;
        else
            return ", listType_"+listTypeName2+"-[r_"+listTypeName+"?:"+RelTypes.RELATED_TO+"]->listType_"+listTypeName;
    }

    /**
     * Add this to the match for the parent and the parent joins
     * @param listTypeName
     * @param listTypeName2
     * @return
     */
    public String createListypeParentMatch(String listTypeName, String listTypeName2){
        if(listTypeName2.isEmpty())
            return ", parent-[r_"+listTypeName+"?:"+RelTypes.RELATED_TO+"]->listType_"+listTypeName;
        else
            return ", listType_"+listTypeName2+"-[r_"+listTypeName+"?:"+RelTypes.RELATED_TO+"]->listType_"+listTypeName;
    }

    /**
     * Simple where without joins or parent in the query
     * @param condition
     * @param attributeName
     * @param attributeValue
     * @param attibuteType
     * @return
     */
    public String createWhere(int condition, String attributeName, String attributeValue, String attibuteType){
        String operator = getOperator(condition);

        if(attributeName.equals("id"))
            return "ID (instance)".concat("=").concat(attributeValue);
        else{
            if (!attibuteType.equals("String") && condition == ExtendedQuery.EQUAL)
                operator = operator.substring(0, operator.length() - 1);
            if (attibuteType.equals("Date")){
                SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationEntityManagerImpl.DATE_FORMAT);//NOI18N
                try {
                    attributeValue = Long.toString(dateFormat.parse(attributeValue).getTime());
                } catch (ParseException ex) {
                    System.out.println("wrong date format should be " + ApplicationEntityManagerImpl.DATE_FORMAT);//NOI18N
                }
            }
            if(attibuteType.equals("String")){
                if(condition == ExtendedQuery.LIKE)
                        attributeValue = "\"(?i).*".concat(attributeValue).concat(".*\"");
                else
                    attributeValue = "\"(?i)".concat(attributeValue).concat("\"");
            }
                
            return "instance.".concat(attributeName).concat(operator).concat(attributeValue);
        }
    }

    /**
     * every time a list type for a join or a parent o r a parent join is added
     * there must be a relation to identify every list type.
     * @param joinName
     * @return
     */
    public String createJoinRelation(String joinName){
        if(joinName.contains("_P"))
            return "r_"+joinName+".name=\""+joinName.substring(0,joinName.length()-2)+"\" AND ";
        else
            return "r_"+joinName+".name=\""+joinName+"\" AND ";
    }

    public String createJoinWhere(int condition, String joinName, String attributeName, String attributeValue, String attibuteType){
        String operator = getOperator(condition);

        if (attributeName.equals("id")) {//is small view
            return "ID (listType_".concat(joinName.concat(")=")).concat(attributeValue);
        }
        else{
            if (!attibuteType.equals("String") && condition == ExtendedQuery.EQUAL)
                operator = operator.substring(0, operator.length() - 1);
            if (attibuteType.equals("Date")){
                SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationEntityManagerImpl.DATE_FORMAT);//NOI18N
                try {
                    attributeValue = Long.toString(dateFormat.parse(attributeValue).getTime());
                } catch (ParseException ex) {
                    System.out.println("wrong date format should be " + ApplicationEntityManagerImpl.DATE_FORMAT);//NOI18N
                }
            }
            if(attibuteType.equals("String")){
                if(condition == ExtendedQuery.LIKE)
                        attributeValue = "\"(?i).*".concat(attributeValue).concat(".*\"");
                else
                    attributeValue = "\"(?i)".concat(attributeValue).concat("\"");
            }
        }
        return "listType_"+joinName+"."+attributeName+operator+attributeValue;
    }

    /**
     * every time a list type for a join or a parent o r a parent join is added
     * there must be a relation to identify every list type.
     * @param joinName
     * @return
     */
    public String createParentRelation(String joinName){
        return "parentclassmetadata.name=\""+joinName+"\" AND ";
    }

    public String createParentWhere(int condition, String joinName, String attributeName, String attributeValue, String attibuteType){
        String operator = getOperator(condition);

        if (attributeName.equals("id")) {//is small view
            return "ID (".concat(joinName.concat(")=")).concat(attributeValue);
        }
        else{
            if (!attibuteType.equals("String") && condition == ExtendedQuery.EQUAL)
                operator = operator.substring(0, operator.length() - 1);
            if (attibuteType.equals("Date")){
                SimpleDateFormat dateFormat = new SimpleDateFormat(ApplicationEntityManagerImpl.DATE_FORMAT);//NOI18N
                try {
                    attributeValue = Long.toString(dateFormat.parse(attributeValue).getTime());
                } catch (ParseException ex) {
                    System.out.println("wrong date format should be " + ApplicationEntityManagerImpl.DATE_FORMAT);//NOI18N
                }
            }
            if(attibuteType.equals("String")){
                if(condition == ExtendedQuery.LIKE)
                        attributeValue = "\"(?i).*".concat(attributeValue).concat(".*\"");
                else
                    attributeValue = "\"(?i)".concat(attributeValue).concat("\"");
            }
        }
        return joinName+"."+attributeName+operator+attributeValue;
    }

    public String getOperator(int condition){
        switch (condition) {
            case ExtendedQuery.EQUAL:
                return "! =~";//NOI18N
            case ExtendedQuery.EQUAL_OR_GREATER_THAN:
                return "! >=";//NOI18N
            case ExtendedQuery.EQUAL_OR_LESS_THAN:
                return "! <=";//NOI18N
            case ExtendedQuery.GREATER_THAN:
                return "! >";//NOI18N
            case ExtendedQuery.LESS_THAN:
                return "! <";//NOI18N
            case ExtendedQuery.LIKE:
                return "! =~";//NOI18N
            default:
                return "";
        }
    }

    public String createReturn(){
        return "instance";
    }
 }
