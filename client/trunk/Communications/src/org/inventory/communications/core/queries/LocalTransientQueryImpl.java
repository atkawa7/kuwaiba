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
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.inventory.core.services.api.queries.LocalQuery;
import org.inventory.core.services.api.queries.LocalTransientQuery;
import org.kuwaiba.wsclient.TransientQuery;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class represents a local query in a machine friendly format (this is made of variables, not XML elements)
 * Pay attention that the JOIN will be treated as small queries, ie.:<br/><br/>
 * <code>SELECT * FROM building b. vendor v WHERE b.name LIKE '%my_building%' INNER JOIN vendor ON v.vendor_id=b.id and v.name ='Nokia'</code><br/>
 * There will be two queries: One (the master) having the condition "name LIKE '%my_building%'" and a "subquery"
 * with the join information.<br /> <br />
 *
 * <b>Note:</b> This query is used ONLY for execution purposes (when an user creates a query and doesn't want) 
 * to save it, only execute it. For queries to be persisted see LocalQueryImpl
 *
 * Most of the structure of this class was borrowed from the remote (server side) implementation
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
@ServiceProvider(service=LocalTransientQuery.class)
public class LocalTransientQueryImpl implements LocalTransientQuery{
        /**
     * Instances of this class will be searched
     */
    private String className;
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
     * Logical connector. "And" by default
     */
    private int logicalConnector = CONNECTOR_AND;
    /**
     * As stated before, joins will be treated like simple subqueries
     */
    private ArrayList<LocalTransientQuery> joins;
    /**
     * Indicates if the current LocalTransientQueryImpl object is a join or the master query. It will
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
    /**
     * Document version applicable to this query. By default all new queries have FORMAT_VERSION version
     */
    private String version = FORMAT_VERSION;

    public LocalTransientQueryImpl() {
        this.attributeNames = new ArrayList<String>();
        this.attributeValues = new ArrayList<String>();
        this.conditions = new ArrayList<Integer>();
        this.joins = new ArrayList<LocalTransientQuery>();
        visibleAttributeNames = new ArrayList<String>();
    }

    public LocalTransientQueryImpl(String className, int logicalConnector,
            boolean isJoin, int limit, int page) {
        this();
        this.className = className;
        this.logicalConnector = logicalConnector;
        this.isJoin = isJoin;
        this.limit = limit;
        this.page = page;
    }

    public LocalTransientQueryImpl(LocalQuery localQuery) throws XMLStreamException{
        parseXML(localQuery.getStructure());
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

    public boolean isJoin() {
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

    public ArrayList<String> getVisibleAttributeNames() {            
        return visibleAttributeNames;
    }

    public static TransientQuery toTransientQuery(LocalTransientQuery localTransientQuery){
        TransientQuery transientQuery = new TransientQuery();
        transientQuery.setAttributeNames(localTransientQuery.getAttributeNames());
        transientQuery.setAttributeValues(localTransientQuery.getAttributeValues());
        transientQuery.setClassName(localTransientQuery.getClassName());
        transientQuery.setConditions(localTransientQuery.getConditions());
        transientQuery.setJoin(false);
        transientQuery.setLimit(localTransientQuery.getLimit());
        transientQuery.setPage(localTransientQuery.getPage());
        transientQuery.setLogicalConnector(localTransientQuery.getLogicalConnector());
        transientQuery.setVisibleAttributeNames(localTransientQuery.getVisibleAttributeNames());

        ArrayList<TransientQuery> remoteJoins =  new ArrayList<TransientQuery>();
        if (localTransientQuery.getJoins() != null){
            for (LocalTransientQuery myJoin : localTransientQuery.getJoins()){
                if (myJoin == null)
                    remoteJoins.add(null);
                else
                    remoteJoins.add(toTransientQuery(myJoin));
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
        mainTag.attr("version", version);      //NOI18N
        mainTag.attr("logicalconnector", logicalConnector);      //NOI18N
        mainTag.attr("limit", limit);      //NOI18N

        buildClassNode(mainTag, this);

        mainTag.end().close();
        return writer.toByteArray();
    }

    private void buildClassNode(StartTagWAX rootTag, LocalTransientQuery currentJoin){
        StartTagWAX classTag = rootTag.start("class");     //NOI18N
        rootTag.attr("name", currentJoin.getClassName());     //NOI18N
        StartTagWAX visibleAttributesTag = classTag.start("visibleattributes");     //NOI18N
        for (String attr : currentJoin.getVisibleAttributeNames()){
            StartTagWAX attributeTag = visibleAttributesTag.start("attribute");     //NOI18N
            attributeTag.attr("name", attr); //NO18N
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

    private void parseXML(byte[] structure) throws XMLStreamException {
        /*
         * Use this for debugging purposes
        try{
            FileOutputStream fos = new FileOutputStream("/home/zim/query.xml");
            fos.write(structure);
            fos.flush();
            fos.close();

        }catch(IOException e){
            e.printStackTrace();
        }
         
         */
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();

        QName qQuery = new QName("query"); //NOI18N
        QName qClass = new QName("class"); //NOI18N


        ByteArrayInputStream bais = new ByteArrayInputStream(structure);
        XMLStreamReader reader = inputFactory.createXMLStreamReader(bais);

        while (reader.hasNext()){
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT){
                if (reader.getName().equals(qQuery)){
                    this.version = reader.getAttributeValue(null, "version"); //NOI18N
                    this.logicalConnector = Integer.valueOf(reader.getAttributeValue(null, "logicalconnector")); //NOI18N
                    this.limit = Integer.valueOf(reader.getAttributeValue(null, "limit")); //NOI18N
                }else{
                    if (reader.getName().equals(qClass)){
                        LocalTransientQuery me = processClassTag(reader);
                        this.attributeNames = me.getAttributeNames();
                        this.conditions = me.getConditions();
                        this.isJoin = false;
                        this.visibleAttributeNames = me.getVisibleAttributeNames();
                        this.joins = me.getJoins();
                        this.className = me.getClassName();
                    }
                }
            }
        }
        reader.close();
    }

    private LocalTransientQuery processClassTag(XMLStreamReader reader) throws XMLStreamException {
        LocalTransientQueryImpl newJoin = new LocalTransientQueryImpl(reader.getAttributeValue(null,"name"),  //NOI18N
                                                                logicalConnector,true, limit, 0);
        
        newJoin.visibleAttributeNames = new ArrayList<String>();
        newJoin.attributeNames = new ArrayList<String>();
        newJoin.joins = new ArrayList<LocalTransientQuery>();
        
        QName qVisibleAttributes = new QName("visibleattributes"); //NOI18N
        QName qAttribute = new QName("attribute"); //NOI18N
        QName qFilters = new QName("filters"); //NOI18N
        QName qFilter = new QName("filter"); //NOI18N
        QName qClass = new QName("class"); //NOI18N

        while (true){
            int event = reader.next();
            if (event == XMLStreamConstants.START_ELEMENT){
                if (reader.getName().equals(qVisibleAttributes)){
                    while (true){
                        int localEvent = reader.next();
                        if (localEvent == XMLStreamConstants.END_ELEMENT){
                            if (reader.getName().equals(qVisibleAttributes))
                                break;
                        }else{
                            if (localEvent == XMLStreamConstants.START_ELEMENT){
                                if (reader.getName().equals(qAttribute))
                                    newJoin.visibleAttributeNames.add(reader.getAttributeValue(null, "name")); //NOI18N
                            }
                        }
                        
                    }
                }else{
                    if (reader.getName().equals(qFilters)){
                        while (true){
                            int localEvent = reader.next();
                            if (localEvent == XMLStreamConstants.END_ELEMENT){
                                if (reader.getName().equals(qFilters))
                                    break;
                            }else{
                                if (localEvent == XMLStreamConstants.START_ELEMENT){
                                    if (reader.getName().equals(qFilter)){
                                        newJoin.attributeNames.add(reader.getAttributeValue(null, "attribute"));     //NOI18N
                                        newJoin.conditions.add(Integer.valueOf(reader.getAttributeValue(null, "condition")));     //NOI18N
                                        if (reader.nextTag() != XMLStreamConstants.END_ELEMENT){ //There's a nested subquery
                                            newJoin.joins.add(processClassTag(reader));
                                        }else newJoin.joins.add(null); //padding
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                if (event == XMLStreamConstants.END_ELEMENT){
                    if (reader.getName().equals(qClass))
                        break;
                }
            }
        }
        return newJoin;
    }
}
