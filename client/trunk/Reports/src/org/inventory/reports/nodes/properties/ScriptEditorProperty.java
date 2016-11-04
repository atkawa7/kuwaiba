/*
 *  Copyright 2010-2016, Neotropic SAS <contact@neotropic.co>
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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.inventory.communications.core.LocalReport;
import org.openide.nodes.PropertySupport;

/**
 * This class allows to edit a groovy script
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ScriptEditorProperty extends PropertySupport.ReadWrite<Object> {
    private LocalReport report;
    
    public ScriptEditorProperty(String propertyName, Class propertyType, LocalReport report) {
        super(propertyName, propertyType, propertyName, propertyName);
        this.report = report;
    }
    
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return report.getScript();
    }

    @Override
    public void setValue(Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        report.setScript((String)value);
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        return null;
    }
    
    //public class
}