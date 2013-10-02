/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.navigation.applicationnodes.attributemetadatanodes.properties;

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import org.inventory.core.services.api.metadata.LocalAttributeMetadata;
import org.inventory.core.services.utils.Constants;
import org.inventory.navigation.applicationnodes.attributemetadatanodes.customeditor.AttributeEditorSupport;
import org.openide.nodes.PropertySupport;

/**
 * Provides a property editor
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ClassAttributeMetadataProperty extends PropertySupport.ReadWrite {

    private Object value;
    private LocalAttributeMetadata attributeMetadata;
    private long classId;
    
    public ClassAttributeMetadataProperty(String name, Class valueType, Object value,
            String displayName,String toolTextTip, LocalAttributeMetadata attributeMetadata, long classId) {
        super(name,valueType,displayName,toolTextTip);
        setName(name);
        this.value = value;
        this.attributeMetadata = attributeMetadata;
        this.classId = classId;
    }
    
    @Override
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return this.value;
    }
    
    @Override
    public void setValue(Object t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //We don't do anything here because the properties of this attribute will be set in the custom editor
    }
    
    @Override
    public PropertyEditor getPropertyEditor(){
        return new AttributeEditorSupport(attributeMetadata, classId);
    }

    @Override
    public boolean canWrite(){
        //The atribute "name" can't be modified in any way to avoid future problems
        if (getName().equals(Constants.PROPERTY_NAME))
            return false;
        else
            return true;
    }
}
