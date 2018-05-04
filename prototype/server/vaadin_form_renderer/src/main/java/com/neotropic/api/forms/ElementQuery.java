/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.api.forms;

import com.neotropic.forms.KuwaibaClient;
import org.inventory.communications.wsclient.ClassInfoLight;
import org.inventory.communications.wsclient.RemoteObjectLight;
import org.inventory.communications.wsclient.RemoteQuery;
import org.inventory.communications.wsclient.RemoteQueryLight;
import org.inventory.communications.wsclient.ResultRecord;
import org.inventory.communications.wsclient.ServerSideException_Exception;
import org.inventory.communications.wsclient.TransientQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.stream.XMLStreamException;
import org.inventory.communications.core.queries.LocalQuery;
import org.inventory.communications.core.queries.LocalTransientQuery;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ElementQuery {
    private static ElementQuery instance;
    private HashMap<String, RemoteQueryLight> queries;
    private List<String> anotherQueries;
    
    private ElementQuery() {        
        anotherQueries = new ArrayList();
        anotherQueries.add("getServices");
        anotherQueries.add("getBandwidths");
        anotherQueries.add("getContracts");
        anotherQueries.add("getCurrencies");
        anotherQueries.add("getInterfaces");
        anotherQueries.add("getBillingPeriod");
        anotherQueries.add("getOrders");
        anotherQueries.add("getOrderNumber");
        anotherQueries.add("getInterfacesType");
                
        try {
            List<RemoteQueryLight> queriesLight = KuwaibaClient.getInstance().getKuwaibaService().getQueries(true, 
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
            
            queries = new HashMap();
            
            for (RemoteQueryLight queryLight : queriesLight)
                queries.put(queryLight.getName(), queryLight);
            
        } catch (ServerSideException_Exception ex) {
            Logger.getLogger(ElementQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static ElementQuery getInstance() {
        return instance == null ? instance = new ElementQuery() : instance;
    }
                
    public List executeQuery(String queryName) {
        if (anotherQueries.contains(queryName)) {
            if ("getServices".equals(queryName))                                
                return getServices();
            else if ("getBandwidths".equals(queryName))
                return getBandwidths();
            else if ("getContracts".equals(queryName))
                return getContracts();
            else if ("getCurrencies".equals(queryName))
                return getCurrencies();
            else if ("getInterfaces".equals(queryName))
                return getInterfaces();
            else if ("getBillingPeriod".equals(queryName))
                return getBillingPeriod();
            else if ("getOrders".equals(queryName))
                return getOrders();
            else if ("geOrderNumber".equals(queryName)) {
                List<String> orderNumber = new ArrayList();
                orderNumber.add("1001");
                return orderNumber;
            } else if ("getInterfacesType".equals(queryName)) {
                
                return getInterfacesType();
            }
                        
        } else if (queries.containsKey(queryName)) {
            try {
                RemoteQueryLight queryLight = queries.get(queryName);
                
                RemoteQuery query = KuwaibaClient.getInstance().getKuwaibaService().getQuery(queryLight.getOid(),
                        KuwaibaClient.getInstance().getRemoteSession().getSessionId());
                
                LocalTransientQuery localTransientQuery = new LocalTransientQuery(new LocalQuery(query));
                localTransientQuery.setPage(0);
                TransientQuery remoteQuery = LocalTransientQuery.toTransientQuery(localTransientQuery);
                
                List<ResultRecord> resultRecordList = KuwaibaClient.getInstance().getKuwaibaService().executeQuery(remoteQuery, 
                    KuwaibaClient.getInstance().getRemoteSession().getSessionId());
                
                List<RemoteObjectLight> result = new ArrayList();
                
                for (int i = 1; i < resultRecordList.size(); i += 1)
                    result.add(resultRecordList.get(i).getObject());
                
                return result;
                
            } catch (ServerSideException_Exception | XMLStreamException ex) {
                Logger.getLogger(ElementQuery.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;                
    }
    
    private List<ClassInfoLight> getServices() {
        try {
            return KuwaibaClient.getInstance().getKuwaibaService().getSubClassesLight("GenericService", false, false, 
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
        } catch (ServerSideException_Exception ex) {
            Logger.getLogger(ElementQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private List<ClassInfoLight> getInterfaces() {
        try {
            return KuwaibaClient.getInstance().getKuwaibaService().getSubClassesLight("GenericCommunicationsPort", false, false, 
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
        } catch (ServerSideException_Exception ex) {
            Logger.getLogger(ElementQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private List<RemoteObjectLight> getBandwidths() {
        try {
            return KuwaibaClient.getInstance().getKuwaibaService().getListTypeItems("BandwidthType",
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
        } catch (ServerSideException_Exception ex) {
            Logger.getLogger(ElementQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private List<RemoteObjectLight> getContracts() {
        try {
            return KuwaibaClient.getInstance().getKuwaibaService().getListTypeItems("ContractType",
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
        } catch (ServerSideException_Exception ex) {
            Logger.getLogger(ElementQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private List<RemoteObjectLight> getCurrencies() {
        try {
            return KuwaibaClient.getInstance().getKuwaibaService().getListTypeItems("CurrencyType",
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
        } catch (ServerSideException_Exception ex) {
            Logger.getLogger(ElementQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private List<RemoteObjectLight> getBillingPeriod() {
        try {
            return KuwaibaClient.getInstance().getKuwaibaService().getListTypeItems("BillingPeriodType",
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
        } catch (ServerSideException_Exception ex) {
            Logger.getLogger(ElementQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private List<RemoteObjectLight> getOrders() {
        try {
            return KuwaibaClient.getInstance().getKuwaibaService().getListTypeItems("OrderType",
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
        } catch (ServerSideException_Exception ex) {
            Logger.getLogger(ElementQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private List<RemoteObjectLight> getInterfacesType() {
        try {
            return KuwaibaClient.getInstance().getKuwaibaService().getListTypeItems("InterfazType",
                KuwaibaClient.getInstance().getRemoteSession().getSessionId());
        } catch (ServerSideException_Exception ex) {
            Logger.getLogger(ElementQuery.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}