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

package com.neotropic.kuwaiba.sync.connectors.snmp.ip;

import com.neotropic.kuwaiba.sync.connectors.snmp.SnmpManager;
import com.neotropic.kuwaiba.sync.connectors.snmp.reference.ReferenceSnmpEntPhysicalTableResourceDefinition;
import com.neotropic.kuwaiba.sync.connectors.snmp.reference.SnmpifXTableResocurceDefinition;
import com.neotropic.kuwaiba.sync.model.AbstractDataEntity;
import com.neotropic.kuwaiba.sync.model.AbstractSyncProvider;
import com.neotropic.kuwaiba.sync.model.PollResult;
import com.neotropic.kuwaiba.sync.model.SyncAction;
import com.neotropic.kuwaiba.sync.model.SyncDataSourceConfiguration;
import com.neotropic.kuwaiba.sync.model.SyncFinding;
import com.neotropic.kuwaiba.sync.model.SyncUtil;
import com.neotropic.kuwaiba.sync.model.SynchronizationGroup;
import com.neotropic.kuwaiba.sync.model.TableData;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.ConnectionException;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.InventoryException;
import org.kuwaiba.services.persistence.util.Constants;
import org.kuwaiba.util.i18n.I18N;
import org.snmp4j.smi.OID;

/**
 * This provider 
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class IPAddressesSyncProvider extends AbstractSyncProvider {

    @Override
    public String getName() {
        return "IP Addresses, subnets and Interfaces";
    }

    @Override
    public String getId() {
        return IPAddressesSyncProvider.class.getName();
    }

    @Override
    public boolean isAutomated() {
        return false;
    }

    @Override
    public List<AbstractDataEntity> unmappedPoll(SynchronizationGroup syncGroup) {
        throw new UnsupportedOperationException("This provider does not support unmapped polling"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PollResult mappedPoll(SynchronizationGroup syncGroup) {
        PollResult pollResult = new PollResult();
        
        for (SyncDataSourceConfiguration agent : syncGroup.getSyncDataSourceConfigurations()) {


            if (!agent.getParameters().containsKey("deviceId")) { //NOI18N
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_deviceId_no_defined"), syncGroup.getName(), syncGroup.getId())));
                continue;
            }
            if (!agent.getParameters().containsKey("deviceClass")) { //NOI18N
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_deviceClass_no_defined"), syncGroup.getName(), syncGroup.getId())));
                    continue;
            }
            if (!agent.getParameters().containsKey("ipAddress")) { //NOI18N
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_ipAddress_no_defined"), syncGroup.getName(), syncGroup.getId())));
                continue;
            }

            if (!agent.getParameters().containsKey("port")) { //NOI18N 
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_port_no_defined"), syncGroup.getName(), syncGroup.getId())));
                continue;
            }
            
            if (!agent.getParameters().containsKey(Constants.PROPERTY_SNMP_VERSION)) {
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("parameter_snmp_version_no_defined"), syncGroup.getName(), syncGroup.getId())));
                continue;
            }
            
            String snmpVersion =  agent.getParameters().get(Constants.PROPERTY_SNMP_VERSION);
            
            if (SnmpManager.VERSION_2c.equals(snmpVersion)) {
                if (!agent.getParameters().containsKey(Constants.PROPERTY_COMMUNITY)) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_community_no_defined"), syncGroup.getName(), syncGroup.getId())));
                    continue;
                }
            }
            
            if (SnmpManager.VERSION_3.equals(snmpVersion)) {
                if (!agent.getParameters().containsKey(Constants.PROPERTY_AUTH_PROTOCOL)) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_auth_protocol_no_defined"), syncGroup.getName(), syncGroup.getId())));
                    continue;
                }
                    
                if (!agent.getParameters().containsKey(Constants.PROPERTY_SECURITY_NAME)) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new InvalidArgumentException(String.format(I18N.gm("parameter_security_name_no_defined"), syncGroup.getName(), syncGroup.getId())));
                    continue;
                }
            }

            try {
                BusinessObjectLight mappedObjLight = PersistenceService.getInstance().
                        getBusinessEntityManager().getObjectLight(agent.getParameters().get("deviceClass"), Long.valueOf(agent.getParameters().get("deviceId")));
                SnmpManager snmpManager = SnmpManager.getInstance();

                snmpManager.setAddress(String.format("udp:%s/%s", agent.getParameters().get("ipAddress"), agent.getParameters().get("port"))); //NOI18N
                snmpManager.setVersion(snmpVersion);

                if (SnmpManager.VERSION_2c.equals(snmpVersion))
                    snmpManager.setCommunity(agent.getParameters().get(Constants.PROPERTY_COMMUNITY));

                if (SnmpManager.VERSION_3.equals(snmpVersion)) {
                    snmpManager.setAuthProtocol(agent.getParameters().get(Constants.PROPERTY_AUTH_PROTOCOL));
                    snmpManager.setAuthPass(agent.getParameters().get(Constants.PROPERTY_AUTH_PASS));
                    snmpManager.setSecurityLevel(agent.getParameters().get(Constants.PROPERTY_SECURITY_LEVEL));
                    snmpManager.setContextName(agent.getParameters().get(Constants.PROPERTY_CONTEXT_NAME));
                    snmpManager.setSecurityName(agent.getParameters().get(Constants.PROPERTY_SECURITY_NAME));
                    snmpManager.setPrivacyProtocol(agent.getParameters().get(Constants.PROPERTY_PRIVACY_PROTOCOL));
                    snmpManager.setPrivacyPass(agent.getParameters().get(Constants.PROPERTY_PRIVACY_PASS));
                }
                //ENTITY-MIB table
                ReferenceSnmpEntPhysicalTableResourceDefinition entPhysicalTable = new ReferenceSnmpEntPhysicalTableResourceDefinition();
                List<List<String>> tableAsString = snmpManager.getTableAsString(entPhysicalTable.values().toArray(new OID[0]));

                if (tableAsString == null) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                    return pollResult;
                }

                pollResult.getResult().put(mappedObjLight, new ArrayList<>());
                pollResult.getResult().get(mappedObjLight).add(
                        new TableData("entPhysicalTable", SyncUtil.parseMibTable("instance", entPhysicalTable, tableAsString))); //NOI18N

                //
                SnmpifXTableResocurceDefinition ifMibTable = new SnmpifXTableResocurceDefinition();
                List<List<String>> ifMibTableAsString = snmpManager.getTableAsString(ifMibTable.values().toArray(new OID[0]));

                if (ifMibTableAsString == null) {
                    pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                        new ConnectionException(String.format(I18N.gm("snmp_agent_connection_exception"), mappedObjLight.toString())));
                    return pollResult;
                }

                pollResult.getResult().get(mappedObjLight).add(
                        new TableData("ifMibTable", SyncUtil.parseMibTable("instance", ifMibTable, ifMibTableAsString))); //NOI18N
            } catch(InventoryException ex) {
                pollResult.getSyncDataSourceConfigurationExceptions(agent).add(
                    new InvalidArgumentException(String.format(I18N.gm("snmp_sync_object_not_found"), ex.getMessage())));
            }
        }
        return pollResult;
    }

    @Override
    public List<SyncFinding> sync(PollResult pollResult) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<SyncFinding> sync(List<AbstractDataEntity> originalData) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> finalize(List<SyncAction> actions) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}