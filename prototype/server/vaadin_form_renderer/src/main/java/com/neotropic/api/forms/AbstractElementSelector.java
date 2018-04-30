/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
 */
package com.neotropic.api.forms;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class AbstractElementSelector extends AbstractElementField {
    private List items;
    
    public void setItems(List items) {
        this.items = items;
    }
    
    public List getItems() {
        return items;
    }
    
    @Override
    public void fireOnload() {
        super.fireOnload();
        
        if (hasProperty(Constants.EventAttribute.ONLOAD, Constants.Property.ITEMS)) {
            
            List<String> list = getEvents().get(Constants.EventAttribute.ONLOAD).get(Constants.Property.ITEMS);

            if (list != null && !list.isEmpty()) {

                String functionName = list.get(0);

                Runner runner = getFormStructure().getElementScript().getFunctionByName(functionName);

                List parameters = new ArrayList();

                for (int i = 1; i < list.size(); i += 1) {
                    AbstractElement anElement = getFormStructure().getElementById(list.get(i));
                    parameters.add(anElement != null ? anElement : list.get(i));
                }

                Object newValue = runner.run(parameters);
                
                setItems((List) newValue);
                
                fireElementEvent(new EventDescriptor(
                    Constants.EventAttribute.ONPROPERTYCHANGE, 
                    Constants.Property.ITEMS, newValue, null));
            }
        }                        
    }
}
