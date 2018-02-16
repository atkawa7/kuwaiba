/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.inventory.modules.sync.nodes.properties;

import java.beans.PropertyEditorSupport;
import org.inventory.communications.util.Constants;

/**
 * Data source configuration property editor
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SyncDataSourceConfigPropertyEditor extends PropertyEditorSupport {
    /**
     * SNMP version
     */
    private final String[] snmpVersions = new String[] {"2c", "3"};
    /**
     * Authentication protocols
     */
    private final String[] authProtocols = new String [] {"None", "MD5", /*"SHA" Not supported yet*/};
    /**
     * Security level
     */                
    private final String[] securityLevels = new String[] {"noAuthNoPriv", "authNoPriv", "authPriv"};
    /**
     * Privacy protocol
     */
    private final String[] privacyProtocols = new String[] {"None", "DES", /*"AES" Not supported yet*/};

    private final String propertyName;

    public SyncDataSourceConfigPropertyEditor(String propertyName) {
        this.propertyName = propertyName;                        
    }

    @Override
    public String[] getTags(){

        switch (propertyName) {
            case Constants.PROPERTY_VERSION:
                return snmpVersions;
            case Constants.PROPERTY_AUTH_PROTOCOL:
                return authProtocols;
            case Constants.PROPERTY_SECURITY_LEVEL:
                return securityLevels;
            case Constants.PROPERTY_PRIVACY_PROTOCOL:
                return privacyProtocols;
            default:
                return new String[0];
        }
    }    
}
