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

package core.todeserialize;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

/**
 * This is a representation in a code friendly fashion of a complex query.
 * Pay attention that the JOIN will be treated as small queries, ie.:<br/><br/>
 * <code>SELECT * FROM building b. vendor v WHERE b.name LIKE '%my_building%' INNER JOIN vendor ON v.vendor_id=b.id and v.name ='Nokia'</code><br/>
 * There will be two queries: One (the master) having the condition "name LIKE '%my_building%'" and a "subquery"
 * with the join information.<br /> <br />
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoteQuery {
    
    /**
     * OR logical connector
     */
    public static final int CONNECTOR_OR = 0;
    /**
     * AND logical connector
     */
    public static final int CONNECTOR_AND = 1;
    /**
     * Equal comparison
     */
    public static final int EQUAL = 0;
    /**
     * Less than comparison
     */
    public static final int LESS_THAN = 1;
    /**
     * Less than or equal to comparison
     */
    public static final int EQUAL_OR_LESS_THAN = 2;
    /**
     * Greater than comparison
     */
    public static final int GREATER_THAN = 3;
    /**
     * Less than or equal to comparison
     */
    public static final int EQUAL_OR_GREATER_THAN = 4;
    /**
     * Between comparison (used for numbers and dates)
     */
    public static final int BETWEEN = 5;
    /**
     * Like comparison (used for strings)
     */
    public static final int LIKE = 6;

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
    private ArrayList<RemoteQuery> joins;
    /**
     * Indicates if the current LocalQuery object is a join or the master query. It will
     * be used later to determine if
     */
    private boolean isJoin = false;

    /**
     * Results limit. Not used if @isJoin is true. Default value is 10
     */
    private int limit = 10;

    public RemoteQuery() {
    }

    public ArrayList<String> getAttributeNames() {
        return attributeNames;
    }

    public ArrayList<String> getAttributeValues() {
        return attributeValues;
    }

    public ArrayList<Integer> getConditions() {
        return conditions;
    }

    public boolean isIsJoin() {
        return isJoin;
    }

    public ArrayList<RemoteQuery> getJoins() {
        return joins;
    }

    public int getLogicalConnector() {
        return logicalConnector;
    }

    public ArrayList<String> getVisibleAttributeNames() {
        return visibleAttributeNames;
    }

    public String getClassName() {
        return className;
    }

    public int getLimit() {
        return limit;
    }
}
