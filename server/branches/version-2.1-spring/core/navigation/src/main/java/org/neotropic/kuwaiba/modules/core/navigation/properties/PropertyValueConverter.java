/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.neotropic.kuwaiba.modules.core.navigation.properties;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.util.visual.properties.AbstractProperty;

/**
 * class used to transform the value of certain properties in the database format that are stored
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>} 
 */
public class PropertyValueConverter {
   
    public static String getMultipleListTypeAsStringToPersist(List<BusinessObjectLight> value) {
        if (value == null) 
            return ""; 
        
        List<BusinessObjectLight> tempList = new ArrayList<>(value);
        
        String idItems = "";
        for (int i = 0; i < tempList.size(); i++) {
            if (i > 0) 
                idItems += ";";
            
            idItems += tempList.get(i).getId();
        }
        return idItems;
    }

    public static String getListTypeAsStringToPersist(BusinessObjectLight value) {
        return value == null ? "" : value.getId() + "" ;
    }
    
    public static String getLocalDateAsStringToPersist(LocalDate value) {
        if (value != null) {
        Instant instant = value.atStartOfDay(ZoneId.systemDefault()).toInstant();	
	long timeInMillis = instant.toEpochMilli();
        return timeInMillis + "";
        } else 
            return "0";
    }
    
    public static String getAsStringToPersist(AbstractProperty property) {
         
        switch (property.getType()) {

            case Constants.DATA_TYPE_DATE:
            case Constants.DATA_TYPE_TIME_STAMP:
                return getLocalDateAsStringToPersist((LocalDate) property.getValue());
            case Constants.DATA_TYPE_OBJECT:
                return getListTypeAsStringToPersist((BusinessObjectLight) property.getValue());
            case Constants.DATA_TYPE_OBJECT_MULTIPLE:
                return getMultipleListTypeAsStringToPersist( new ArrayList<>((Set) property.getValue()));
            default:
                return property.getValue().toString();

        }
    }
    
}
