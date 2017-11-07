/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.neotropic.kuwaiba.sync.connectors.snmp.model;

import java.util.List;

/**
 * This class allows to define how the raw information coming from the sync sources 
 * (SNMP, text files, etc) must be mapped into attributes in the data model
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class MappingDefinition {
    /**
     * The name of the attribute the value extracted from the sync source will be mapped to
     */
    private String attributeName;
    /**
     * The type of the value extracted from the sync source. Theoretically, it must match the type defined in the data model
     */
    private String mapToDataType;
    /**
     * The designation used to identify a particular value in the sync source. For example, an OID for SNMP or a column number in a text file
     */
    private String locationInSyncSource;
    /**
     * Operation to be performed on the extracted value before updating the value in the inventory
     */
    private Operation operation;
    /**
     * Default value to be used if it's not possible to retrieve data from the sync source. Use null to leave the value found in the inventory unchanged
     */
    private String defaultValue;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public String getMapToDataType() {
        return mapToDataType;
    }

    public void setMapToDataType(String mapToDataType) {
        this.mapToDataType = mapToDataType;
    }

    public String getLocationInSyncSource() {
        return locationInSyncSource;
    }

    public void setLocationInSyncSource(String locationInSyncSource) {
        this.locationInSyncSource = locationInSyncSource;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    
    public class Operation {
        /**
         * Math operations
         */
        public static final String OPERATION_ADDITION = "add";
        public static final String OPERATION_SUBSTRACTION = "sub";
        public static final String OPERATION_MULTIPLICATION = "mul";
        public static final String OPERATION_DIVISION = "div";
        /**
         * String operations
         */
        
        private String operationName;
        private List<String> parameters;

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }

        public List<String> getParameters() {
            return parameters;
        }

        public void setParameters(List<String> parameters) {
            this.parameters = parameters;
        }
    }
}