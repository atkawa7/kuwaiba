/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package com.neotripic.snmp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.GenericAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

/**
 * A simple manager of agents
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SNMPManager {
    private static SNMPManager instance;
    private String community;
    private String address;
    private Snmp snmp;
    TransportMapping transportMapping;
    
    public SNMPManager() {
        this.address = null;        
        this.community = null;
    }
    
    public static SNMPManager getInstance() throws IOException {
        if (instance == null) {
            instance = new SNMPManager();
            instance.start();
        }
        return instance;
    }
    
    public String getCommunity() {
        return community;
    }
    
    public void setCommunity(String community) {
        this.community = community;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public void start() throws IOException {
        try {
            /*TransportMapping */transportMapping = new DefaultUdpTransportMapping();
            snmp = new Snmp(transportMapping);
            transportMapping.listen();
        } catch (IOException ex) {
            throw ex;
        }
    }
    
    public String getAsString(OID oid) throws IOException {
        ResponseEvent event = get(new OID[] { oid });
        if (event.getResponse() != null)
            return event.getResponse().get(0).getVariable().toString();
        return null;
    }
    
    public ResponseEvent get(OID oids[]) throws IOException {
        PDU pdu = new PDU();
        for (OID oid : oids)
            pdu.add(new VariableBinding(oid));
        pdu.setType(PDU.GET);
        
        ResponseEvent event = snmp.send(pdu, getTarget(), transportMapping);
        if (event != null)
            return event;
        throw new RuntimeException("GET timed out");
    }
    
    private Target getTarget() {
        Address targetAddress = GenericAddress.parse(address);
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(community == null ? "public" : community));
        target.setAddress(targetAddress);
        target.setRetries(2);
        target.setTimeout(1500);
        target.setVersion(SnmpConstants.version2c);
        return target;
    }
    
    public List<List<String>> getTableAsString(OID[] oids) {
        TableUtils tableUtils = new TableUtils(snmp, new DefaultPDUFactory());
        
        List<TableEvent> events = tableUtils.getTable(getTarget(), oids, null, null);
        
        List<List<String>> list = new ArrayList<>();
        for (TableEvent event : events) {
            if (event.isError())
                throw new RuntimeException(event.getErrorMessage());
            List<String> strList = new ArrayList<>();
            list.add(strList);
            for (VariableBinding vb : event.getColumns())
                strList.add(vb != null ?  vb.getVariable().toString() : "");
        }
        return list;
    }
}
