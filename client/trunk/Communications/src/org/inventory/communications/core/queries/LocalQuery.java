/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.communications.core.queries;

import java.util.ArrayList;
import org.inventory.webservice.RemoteQuery;

/**
 * This class represents a local query in a machine friendly format (this is made of variables, not XML elements)
 * Pay attention that the JOIN will be treated as small queries, ie.:<br/><br/>
 * <code>SELECT * FROM building b. vendor v WHERE b.name LIKE '%my_building%' INNER JOIN vendor ON v.vendor_id=b.id and v.name ='Nokia'</code><br/>
 * There will be two queries: One (the master) having the condition "name LIKE '%my_building%'" and a "subquery"
 * with the join information.<br /> <br />
 *
 * Most of the structure of this class was borrowed from the remote (server side) implementation
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalQuery {
    public static final int CONNECTOR_OR = 0;
    public static final int CONNECTOR_AND = 1;

    private String name;
    /**
     * Instances of this class will be searched
     */
    private String className;
    private int logicalConnector;
    /**
     * Attributes that will be used to build the criteria
     */
    private ArrayList<String> attributeNames;
    /**
     * Attributes to be shown in the final result (read this as "SELECT visibleAttributesNames" FROM...).
     * If this is the master query(see @isJoin) and the it's empty or null, all attributes will be shown; if
     * this is a join, none will be shown
     */
    private ArrayList<String> visibleAttributeNames;

    private ArrayList<String> attributeValues;
    /**
     * Equal to, less than, like, etc
     */
    private ArrayList<Integer> conditions;
    /**
     * As stated before, joins will be treated like simple subqueries
     */
    private ArrayList<LocalQuery> joins;
    /**
     * Indicates if the current LocalQuery object is a join or the master query. It will
     * be used later to determine if 
     */
    private boolean isJoin = false;

    /**
     * Results limit. Not used if @isJoin is true. Default value is 10
     */
    private int limit = 10;
    /**
     * Current result page. If its value is less than 1, means that no pagination should be used
     */
    private int page = 1;

    public LocalQuery(String name, String className, int logicalConnector, 
            boolean isJoin, int limit, int page) {
        this.name = name;
        this.className = className;
        this.logicalConnector = logicalConnector;
        this.isJoin = isJoin;
        this.limit = limit;
        this.page = page;
    }

    public ArrayList<String> getAttributeNames() {
        if (attributeNames == null)
            attributeNames = new ArrayList<String>();
        return attributeNames;
    }

    public ArrayList<String> getAttributeValues() {
        if (attributeValues == null)
            attributeValues = new ArrayList<String>();
        return attributeValues;
    }

    public String getClassName() {
        return className;
    }

    public ArrayList<Integer> getConditions() {
        if (conditions == null)
            conditions = new ArrayList<Integer>();
        return conditions;
    }

    public boolean isIsJoin() {
        return isJoin;
    }

    public ArrayList<LocalQuery> getJoins() {
        if (joins == null)
            joins = new ArrayList<LocalQuery>();
        return joins;
    }

    public int getLimit() {
        return limit;
    }

    public int getPage() {
        return page;
    }

    public int getLogicalConnector() {
        return logicalConnector;
    }

    public String getName() {
        return name;
    }

    public ArrayList<String> getVisibleAttributeNames() {
        if (visibleAttributeNames == null)
            visibleAttributeNames = new ArrayList<String>();
        return visibleAttributeNames;
    }

    public RemoteQuery toRemoteQuery(){
        RemoteQuery remoteQuery = new RemoteQuery();
        remoteQuery.setAttributeNames(getAttributeNames());
        remoteQuery.setAttributeValues(getAttributeValues());
        remoteQuery.setClassName(getClassName());
        remoteQuery.setConditions(getConditions());
        remoteQuery.setIsJoin(false);
        remoteQuery.setLimit(getLimit());
        remoteQuery.setPage(page);
        remoteQuery.setLogicalConnector(getLogicalConnector());
        remoteQuery.setVisibleAttributeNames(getVisibleAttributeNames());

        ArrayList<RemoteQuery> remoteJoins =  new ArrayList<RemoteQuery>();
        if (getJoins() != null){
            for (LocalQuery myJoin : getJoins()){
                if (myJoin == null)
                    remoteJoins.add(null);
                else
                    remoteJoins.add(myJoin.toRemoteQuery());
            }
            remoteQuery.setJoins(remoteJoins);
        }
        return remoteQuery;
    }

    public enum Criteria{
        EQUAL("Equal to",0),
        LESS_THAN("Less than",1),
        EQUAL_OR_LESS_THAN("Equals or less than",2),
        GREATER_THAN("Greater than",3),
        EQUAL_OR_GREATER_THAN("Equal or greater than",4),
        BETWEEN("Between",5),
        LIKE("Like",6);
        private final String label;
        private final int id;

        Criteria(String label, int id){
            this.label = label;
            this.id = id;
        }

        public String label(){return label;}
        public int id(){return id;}

        @Override
        public String toString(){return label;}
    }
}
