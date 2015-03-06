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

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.util.Constants;

/**
 * Provides a custom property editor for list-type values where you can choose one item
 * @author Adrian Martinez Molina <charles.bedon@kuwaiba.org>
 */
public class ListAttributeMetadataProperty extends PropertyEditorSupport{
    
    private List <String> attributeTypeslist;
    protected CommunicationsStub com;

    public ListAttributeMetadataProperty(String aValue) {
        com = CommunicationsStub.getInstance();
        LocalClassMetadataLight[] instanceableListTypes = com.getInstanceableListTypes();
        
        this.attributeTypeslist = new ArrayList<String>();
        //Primitive types
        for(String primitive : Constants.ATTRIBUTE_TYPES)
            this.attributeTypeslist.add(primitive);
        
        //List types
        for(LocalClassMetadataLight listType : instanceableListTypes)
            this.attributeTypeslist.add(listType.getClassName());
        
        setValue(aValue);
    }
    
    @Override
    public String getAsText(){
        return getValue().toString();
    }
    
    @Override
    public void setAsText(String text){
        for (String type : attributeTypeslist)
            if (text.equals(type)){
                setValue(type);
                break;
            }
    }

    @Override
    public String[] getTags(){
        //Remember that CommunicationsStub->getList returns the list, but adds the null value as well,
        //so it's not necessary to add it here
        String [] res = new String[attributeTypeslist.size()];
    
        for (int i = 0; i <attributeTypeslist.size(); i++)
            res[i] = attributeTypeslist.get(i);
        return res;
    }


    @Override
    public boolean supportsCustomEditor(){
        return false;
    }
    
}
