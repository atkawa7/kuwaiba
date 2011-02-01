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

import com.ociweb.xml.StartTagWAX;
import com.ociweb.xml.WAX;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import org.inventory.webservice.TransientQuery;

/**
 * This class represents a local query in a machine friendly format (this is made of variables, not XML elements)
 * Pay attention that the JOIN will be treated as small queries, ie.:<br/><br/>
 * <code>SELECT * FROM building b. vendor v WHERE b.name LIKE '%my_building%' INNER JOIN vendor ON v.vendor_id=b.id and v.name ='Nokia'</code><br/>
 * There will be two queries: One (the master) having the condition "name LIKE '%my_building%'" and a "subquery"
 * with the join information.<br /> <br />
 *
 * <b>Note:</b> This query is used ONLY for execution purposes (when an user creates a query and doesn't want) 
 * to save it, only execute it. For queries to be persisted see LocalQuery
 *
 * Most of the structure of this class was borrowed from the remote (server side) implementation
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class LocalTransientQuery {
    /**
     * Logical connector OR
     */
    public static final int CONNECTOR_OR = 0;
    /**
     * Logical connector AND
     */
    public static final int CONNECTOR_AND = 1;
    /**
     * Version for the XML document created
     */
    private static final String FORMAT_VERSION = "1.0";

    private String name;
    /**
     * Instances of this class will be searched
     */
    private String className;
    /**
     * Logical connector. "And" by default
     */
    private int logicalConnector = CONNECTOR_AND;
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
    private ArrayList<LocalTransientQuery> joins;
    /**
     * Indicates if the current LocalTransientQuery object is a join or the master query. It will
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

    private LocalTransientQuery() {
        this.attributeNames = new ArrayList<String>();
        this.attributeValues = new ArrayList<String>();
        this.conditions = new ArrayList<Integer>();
        this.joins = new ArrayList<LocalTransientQuery>();
        visibleAttributeNames = new ArrayList<String>();
    }

    public LocalTransientQuery(String name, String className, int logicalConnector,
            boolean isJoin, int limit, int page) {
        this();
        this.name = name;
        this.className = className;
        this.logicalConnector = logicalConnector;
        this.isJoin = isJoin;
        this.limit = limit;
        this.page = page;
    }

    public LocalTransientQuery(byte [] queryAsXML) {
        this();
    }

    public ArrayList<String> getAttributeNames() {           
        return attributeNames;
    }

    public ArrayList<String> getAttributeValues() {           
        return attributeValues;
    }

    public String getClassName() {
        return className;
    }

    public ArrayList<Integer> getConditions() {           
        return conditions;
    }

    public boolean isIsJoin() {
        return isJoin;
    }

    public ArrayList<LocalTransientQuery> getJoins() {
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
        return visibleAttributeNames;
    }

    public TransientQuery toTransientQuery(){
        TransientQuery transientQuery = new TransientQuery();
        transientQuery.setAttributeNames(getAttributeNames());
        transientQuery.setAttributeValues(getAttributeValues());
        transientQuery.setClassName(getClassName());
        transientQuery.setConditions(getConditions());
        transientQuery.setIsJoin(false);
        transientQuery.setLimit(getLimit());
        transientQuery.setPage(page);
        transientQuery.setLogicalConnector(getLogicalConnector());
        transientQuery.setVisibleAttributeNames(getVisibleAttributeNames());

        ArrayList<TransientQuery> remoteJoins =  new ArrayList<TransientQuery>();
        if (getJoins() != null){
            for (LocalTransientQuery myJoin : getJoins()){
                if (myJoin == null)
                    remoteJoins.add(null);
                else
                    remoteJoins.add(myJoin.toTransientQuery());
            }
            transientQuery.setJoins(remoteJoins);
        }
        return transientQuery;
    }

    /**
     * Creates a valid XML document describing this object in the format exposed at the <a href="http://is.gd/kcl1a">project's wiki</a>
     * @return a byte array ready serialized somehow (file, webservice, etc)
     */
    public byte[] toXML(){
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        WAX xmlWriter = new WAX(writer);
        StartTagWAX mainTag = xmlWriter.start("query");     //NOI18N
        mainTag.attr("version", FORMAT_VERSION);      //NOI18N
        mainTag.attr("name", name);      //NOI18N
        mainTag.attr("logicalconnector", logicalConnector);      //NOI18N
        mainTag.attr("limit", limit);      //NOI18N

        buildClassNode(mainTag, this);

        mainTag.end().close();
        return writer.toByteArray();
    }

    private void buildClassNode(StartTagWAX rootTag, LocalTransientQuery currentJoin){
        StartTagWAX classTag = rootTag.start("class");     //NOI18N
        rootTag.attr("classname", currentJoin.getClassName());     //NOI18N
        StartTagWAX visibleAttributesTag = classTag.start("visibleattributes");     //NOI18N
        for (String attr : currentJoin.getVisibleAttributeNames()){
            StartTagWAX attributeTag = visibleAttributesTag.start("attribute");     //NOI18N
            attributeTag.text(attr);
            attributeTag.end();
        }
        visibleAttributesTag.end();

        StartTagWAX filtersTag = classTag.start("filters");     //NOI18N

        //Filters for simple attributes (numbers, strings, etc)
        for (int i = 0; i< currentJoin.getAttributeNames().size(); i++){
            StartTagWAX filterTag = filtersTag.start("filter");     //NOI18N
            filterTag.attr("attribute", currentJoin.getAttributeNames().get(i));     //NOI18N
            filterTag.attr("condition", currentJoin.getConditions().get(i) == null ?      //NOI18N
                                            0 : currentJoin.getConditions().get(i));
            if (currentJoin.getJoins().get(i) != null)
                buildClassNode(filterTag, currentJoin.getJoins().get(i));

            filterTag.end();
        }
        filtersTag.end();
        classTag.end();
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
