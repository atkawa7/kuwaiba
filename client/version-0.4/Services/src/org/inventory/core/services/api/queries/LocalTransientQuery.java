/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.inventory.core.services.api.queries;

import java.util.ArrayList;

/**
 * This interface represents a local query in a machine friendly format (this is made of variables, not XML elements)
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
public interface LocalTransientQuery {
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
    public static final String FORMAT_VERSION = "1.0";

    public ArrayList<String> getAttributeNames();

    public ArrayList<String> getAttributeValues();

    public String getClassName();

    public ArrayList<Integer> getConditions();

    public boolean isJoin();

    public ArrayList<LocalTransientQuery> getJoins();

    public int getLimit();

    public int getPage();

    public int getLogicalConnector();

    public ArrayList<String> getVisibleAttributeNames();

    /**
     * Creates a valid XML document describing this object in the format exposed at the <a href="http://is.gd/kcl1a">project's wiki</a>
     * @return a byte array ready serialized somehow (file, webservice, etc)
     */
    public byte[] toXML();


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

        public static Criteria fromId(int i){
            switch (i){
                default:
                case 0:
                    return EQUAL;
                case 1:
                    return LESS_THAN;
                case 2:
                    return EQUAL_OR_LESS_THAN;
                case 3:
                    return GREATER_THAN;
                case 4:
                    return EQUAL_OR_GREATER_THAN;
                case 5:
                    return BETWEEN;
                case 6:
                    return LIKE;
            }
        }
    }
}
