/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package org.kuwaiba.apis.web.gui.properties;

import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadata;

/**
 * A factory class that builds property sets given business objects
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class PropertyFactory {
    public static List<AbstractProperty> propertiesFromRemoteObject(RemoteObject businessObject, RemoteClassMetadata classMetadata) {
        ArrayList<AbstractProperty> propertySet = new ArrayList<>();
        for (int i = 0; i < classMetadata.getAttributesNames().length; i++) {
            switch (classMetadata.getAttributesTypes()[i]) {
                case "Date":
                    propertySet.add(new DateProperty(classMetadata.getAttributesNames()[i], 
                        classMetadata.getAttributesDisplayNames()[i], 
                        classMetadata.getAttributesDescriptions()[i], 
                        Long.valueOf(businessObject.getAttribute(classMetadata.getAttributesNames()[i]))));
                    break;
                default:
                    propertySet.add(new StringProperty(classMetadata.getAttributesNames()[i], 
                        classMetadata.getAttributesDisplayNames()[i], 
                        classMetadata.getAttributesDescriptions()[i], 
                        businessObject.getAttribute(classMetadata.getAttributesNames()[i])));
            }
            
            
        }
        
        return propertySet;
    }
}
