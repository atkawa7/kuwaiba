/*
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>
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
package org.inventory.reports.nodes.properties;

import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.core.LocalReport;
import org.inventory.communications.util.Constants;
import org.openide.nodes.PropertySupport;

/**
 * This class allows to edit primitive type properties.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class BasicProperty extends PropertySupport.ReadWrite<Object> {
    private LocalReport report;
    
    public BasicProperty(String propertyName, Class propertyType, LocalReport report) {
        super(propertyName, propertyType, propertyName, propertyName);
        this.report = report;
    }
    
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        switch (getName()) {
            case Constants.PROPERTY_NAME:
                return report.getName();
            case Constants.PROPERTY_DESCRIPTION:
                return report.getDescription();
            case Constants.PROPERTY_ENABLED:
                return report.isEnabled();
            case Constants.PROPERTY_SCRIPT:
                return report.getScript();
        }
        
       throw new IllegalAccessException(String.format("Unknown property name: %s", getName())); //Should never happen
    }

    @Override
    public void setValue(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        switch (getName()) {
            case Constants.PROPERTY_NAME:
                report.setName((String)value);
                break;
            case Constants.PROPERTY_DESCRIPTION:
                report.setDescription((String)value);
                break;
            case Constants.PROPERTY_ENABLED:
                report.setEnabled((boolean)value);
                break;
            case Constants.PROPERTY_SCRIPT:
                report.setScript((String)value);
        }
        
    }
}