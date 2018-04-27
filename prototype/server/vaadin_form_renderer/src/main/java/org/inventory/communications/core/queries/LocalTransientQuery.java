/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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


package org.inventory.communications.core.queries;

import org.inventory.communications.wsclient.TransientQuery;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

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
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
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
    public static final String FORMAT_VERSION = "1.0";
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

    public LocalTransientQuery() {
        this.attributeNames = new ArrayList<>();
        this.attributeValues = new ArrayList<>();
        this.conditions = new ArrayList<>();
        this.joins = new ArrayList<>();
        visibleAttributeNames = new ArrayList<>();
    }

    public LocalTransientQuery(String className, int logicalConnector,
            boolean isJoin, int limit, int page) {
        this();
        this.className = className;
        this.logicalConnector = logicalConnector;
        this.isJoin = isJoin;
        this.limit = limit;
        this.page = page;
    }

    public LocalTransientQuery(LocalQuery localQuery) throws XMLStreamException{
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

    public void setPage(int page) {
        this.page = page;
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

        ArrayList<TransientQuery> remoteJoins =  new ArrayList<>();
        if (localTransientQuery.getJoins() != null){
            for (LocalTransientQuery myJoin : localTransientQuery.getJoins()){
                if (myJoin == null)
                    remoteJoins.add(null);
                else
                    remoteJoins.add(toTransientQuery(myJoin));
            }
//            transientQuery.setJoins(remoteJoins);
        }
        return transientQuery;
    }

    /**
     * Creates a valid XML document describing this object in the format exposed at the <a href="http://is.gd/kcl1a">project's wiki</a>
     * @return a byte array ready serialized somehow (file, webservice, etc)
     */
    public byte[] toXML() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
        
            XMLEventWriter xmlew;
            xmlew = xmlof.createXMLEventWriter(baos);
            
            XMLEventFactory xmlef = XMLEventFactory.newInstance();
            
            QName qnameQuery = new QName("query");

            xmlew.add(xmlef.createStartElement(qnameQuery, null, null));
            // query attributes
            xmlew.add(xmlef.createAttribute(new QName("version"), version));
            xmlew.add(xmlef.createAttribute(new QName("logicalconnector"), Integer.toString(logicalConnector)));
            xmlew.add(xmlef.createAttribute(new QName("limit"), Integer.toString(limit)));

            bulidClassNode(xmlew, xmlef, this);

            xmlew.add(xmlef.createEndElement(qnameQuery, null));

            xmlew.close();
            return baos.toByteArray();
        } catch (XMLStreamException ex) {
            Logger.getLogger(LocalTransientQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    private void bulidClassNode(XMLEventWriter xmlew, XMLEventFactory xmlef, LocalTransientQuery currentJoin) throws XMLStreamException {
        QName qnameClass = new QName("class");
        
        xmlew.add(xmlef.createStartElement(qnameClass, null, null));
        xmlew.add(xmlef.createAttribute(new QName("name"), currentJoin.getClassName()));
        // Visible attributes                
        QName qnameVisibleattrs = new QName("visibleattributes");
        
        xmlew.add(xmlef.createStartElement(qnameVisibleattrs, null, null));
                
        for (String visibleAttrName : currentJoin.getVisibleAttributeNames()) {
            QName qnameAttr = new QName("attribute");
            
            xmlew.add(xmlef.createStartElement(qnameAttr, null, null));
            xmlew.add(xmlef.createAttribute(new QName("name"), visibleAttrName));
            xmlew.add(xmlef.createEndElement(qnameAttr, null));
        }
        xmlew.add(xmlef.createEndElement(qnameVisibleattrs, null));
        // Filters                        
        QName qnameFilters = new QName("filters");
        
        xmlew.add(xmlef.createStartElement(qnameFilters, null, null));
                        
        for (int i = 0; i < currentJoin.getAttributeNames().size(); i += 1) {
            QName qnameFilter = new QName("filter");
            
            xmlew.add(xmlef.createStartElement(qnameFilter, null, null));
            
            xmlew.add(xmlef.createAttribute(new QName("attribute"), currentJoin.getAttributeNames().get(i)));
            xmlew.add(xmlef.createAttribute(new QName("condition"), 
                    currentJoin.getConditions().get(i) == null ? 
                            "0" : Integer.toString(currentJoin.getConditions().get(i))));
            
            if (currentJoin.getJoins().get(i) != null) 
                bulidClassNode(xmlew, xmlef, currentJoin.getJoins().get(i));
            xmlew.add(xmlef.createEndElement(qnameFilter, null));
        }
        xmlew.add(xmlef.createEndElement(qnameFilters, null));
                        
        xmlew.add(xmlef.createEndElement(qnameClass, null));
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
        LocalTransientQuery newJoin = new LocalTransientQuery(reader.getAttributeValue(null,"name"),  //NOI18N
                                                                logicalConnector,true, limit, 0);
        
        newJoin.visibleAttributeNames = new ArrayList<>();
        newJoin.attributeNames = new ArrayList<>();
        newJoin.joins = new ArrayList<>();
        
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
