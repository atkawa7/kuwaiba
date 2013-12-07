/*
 *  Copyright 2010-2013 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.objectnodes.properties;

import java.beans.PropertyEditorSupport;
import java.util.List;
import org.inventory.core.services.api.LocalObjectListItem;


/**
 * Provides a custom property editor for list-type values
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ItemListPropertyEditor extends PropertyEditorSupport{

    private List<LocalObjectListItem> list;

    public ItemListPropertyEditor(List<LocalObjectListItem> list){
        this.list = list;
    }

    @Override
    public String getAsText(){
        return getValue().toString();
    }

    @Override
    public void setAsText(String text){
        for (LocalObjectListItem loli : list)
            if (text.equals(loli.getName())){
                setValue(loli);
                break;
            }
    }

    @Override
    public String[] getTags(){
        //Remember that CommunicationsStub->getList returns the list, but adds the null value as well,
        //so it's not necessary to add it here
        String [] res = new String[list.size()];
    
        for (int i = 0; i <list.size(); i++)
            res[i] = list.get(i).getName().trim().equals("")?list.get(i).getName():list.get(i).getName();
        return res;
    }

/**
 * Implement this (returning true in supportsCustomEditor) when there's need of an advance, custom editor
 *
 * @Override
    public Component getCustomEditor(){
        return new JLabel("sfdfdgfdfgdsf");
    }
 */

    @Override
    public boolean supportsCustomEditor(){
        return false;
    }
}