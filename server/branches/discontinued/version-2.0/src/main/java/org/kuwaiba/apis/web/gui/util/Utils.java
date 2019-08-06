/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.util;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.persistence.metadata.AttributeMetadata;
import org.kuwaiba.apis.web.gui.wrappers.LocalObjectLight;
import org.kuwaiba.apis.web.gui.wrappers.LocalObjectListItem;

/**
 *
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class Utils {
        public static Class getRealType(String typeAsString){
        if (typeAsString.equals("String"))
            return String.class;
        if (typeAsString.equals("Integer"))
            return Integer.class;
        if (typeAsString.equals("Float"))
            return Float.class;
        if (typeAsString.equals("Long"))
            return Long.class;
        if (typeAsString.equals("Date"))
            return Date.class;
        if (typeAsString.equals("Time"))
            return Time.class;
        if (typeAsString.equals("Timestamp"))
            return Timestamp.class;
        if (typeAsString.equals("Boolean"))
            return Boolean.class;
        else
            return LocalObjectLight.class;
    }
        
    /**
     * Converts a string value to the real type given the mapping and the type
     * @param type
     * @param mapping
     * @param valueAsString
     * @return
     * @throws IllegalArgumentException 
     */
    public static Object getRealValue (String type, Integer mapping, List<String> valueAsString) throws IllegalArgumentException{
        if (valueAsString == null)
            return null;
        if (valueAsString.isEmpty())
            return null;
        try{
            switch (mapping){
                case AttributeMetadata.MAPPING_PRIMITIVE:
                case AttributeMetadata.MAPPING_DATE:
                case AttributeMetadata.MAPPING_TIMESTAMP:
                    if (type.equals("Boolean"))
                        return Boolean.valueOf(valueAsString.get(0));

                    if (type.equals("String"))
                        return valueAsString.get(0);

                    if (type.equals("Integer"))
                        return Integer.valueOf(valueAsString.get(0));

                    if (type.equals("Float"))
                        return Float.valueOf(valueAsString.get(0));

                    if (type.equals("Long"))
                        return Long.valueOf(valueAsString.get(0));

                    if (type.equals("Date"))
                        return new Date(Long.valueOf(valueAsString.get(0)));
                    if (type.equals("Timestamp"))
                        return Timestamp.valueOf(valueAsString.get(0));

                    //In any other case we rise an IllegalArgumentException
                    throw new IllegalArgumentException(String.format("The type %s has a wrong mapping and will be ignored", type));
                case AttributeMetadata.MAPPING_MANYTOMANY:
                    List<Long> res = new ArrayList<>();
                    for (String value : valueAsString)
                        res.add(Long.valueOf(value));
                    return res;
                case AttributeMetadata.MAPPING_MANYTOONE:
                    if (valueAsString.isEmpty())
                        return null;
                    //return Long.valueOf(valueAsString.get(0));
                    return Utils.getListTypeItem(type, Long.valueOf(valueAsString.get(0)));
                default:
                    throw new Exception();
            }
        }catch(Exception e){
            throw new IllegalArgumentException();
        }
    }
    
    public static LocalObjectListItem getListTypeItem(String listTypeClass, long listTypeItemId) throws IllegalAccessException {
//        List<LocalObjectListItem> list = CommunicationsStub.getInstance().getList(listTypeClass, true, false);
//        //getListTypeItems;
//        if (list == null)
//            throw new IllegalAccessException(CommunicationsStub.getInstance().getError());
//        
//        for (LocalObjectListItem listItem : list) {
//            if (listItem.getId() == listTypeItemId)
//                return listItem;
//        }
//        
//        throw new IllegalArgumentException(String.format("List type %s with id %s could not be found", listTypeClass, listTypeItemId));
        return null;
    }
}
