/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.neotropic.util.visual.properties;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;



/**
 * A factory class that builds property sets given business objects.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PropertyFactory {
 
    
    /**
     * Builds a property set from a given inventory object
     * @param businessObject The business object
     * @param bem BusinessEntityManager service
     * @param mem MetadataEntityManager service
     * @return The set of properties ready to used in a property sheet component
     * @throws MetadataObjectNotFoundException
     * @throws BusinessObjectNotFoundException
     * @throws InvalidArgumentException
     * @throws ApplicationObjectNotFoundException
     */
    public static List<AbstractProperty> propertiesFromBusinessObject(BusinessObjectLight businessObject, ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {

        HashMap<String, String> objectAttributes = bem.getAttributeValuesAsString(businessObject.getClassName(), businessObject.getId());

        ClassMetadata classMetadata = mem.getClass(businessObject.getClassName());

        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
  
        for (AttributeMetadata am : classMetadata.getAttributes()) {
            AbstractProperty property = null;
            
            switch (am.getType()) {
                case Constants.DATA_TYPE_STRING:

                    property = new StringProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            (objectAttributes.get(am.getName()) == null ? "<Not Set>" : objectAttributes.get(am.getName())),
                            Constants.DATA_TYPE_STRING);
                    break;
                case Constants.DATA_TYPE_INTEGER:

                    property = new IntegerProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            (objectAttributes.get(am.getName()) == null ? null : Integer.parseInt(objectAttributes.get(am.getName()))),
                            Constants.DATA_TYPE_INTEGER);
                    break;
                case Constants.DATA_TYPE_BOOLEAN:

                    property = new BooleanProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            (objectAttributes.get(am.getName()) == null ? false : Boolean.valueOf(objectAttributes.get(am.getName()))),
                            Constants.DATA_TYPE_BOOLEAN);
                    break;
                case Constants.DATA_TYPE_DOUBLE:
                case Constants.DATA_TYPE_FLOAT:

                    property = new DoubleProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            (objectAttributes.get(am.getName()) == null ? null : Double.parseDouble(objectAttributes.get(am.getName()))),
                            am.getType().equals(Constants.DATA_TYPE_DOUBLE) ? Constants.DATA_TYPE_DOUBLE : Constants.DATA_TYPE_FLOAT);
                    break;
                case Constants.DATA_TYPE_LONG:

                    property = new LongProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            (objectAttributes.get(am.getName()) == null ? null : Long.parseLong(objectAttributes.get(am.getName()))),
                            Constants.DATA_TYPE_LONG);
                    //special case for creation date attribute
                    if (Constants.PROPERTY_CREATION_DATE.equals(am.getName())) 
                        property.setReadOnly(true);
                    
                    break;
                case Constants.DATA_TYPE_DATE:
                case Constants.DATA_TYPE_TIME_STAMP:

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                    property = new LocalDateProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            (objectAttributes.get(am.getName()) == null ? null : LocalDate.parse(objectAttributes.get(am.getName()), formatter)),
                            am.getType().equals(Constants.DATA_TYPE_DATE) ? Constants.DATA_TYPE_DATE : Constants.DATA_TYPE_TIME_STAMP);
                    break;
                default:   // list type
                    List<BusinessObjectLight> listTypeItems = aem.getListTypeItems(am.getType());
                    List<BusinessObjectLight> selectedItems = new ArrayList<>();
                    String attributeValue = objectAttributes.get(am.getName());

                    if (attributeValue != null) {
                        String[] tokensItems = attributeValue.split(";");

                        for (String token : tokensItems) {
                            selectedItems.addAll(listTypeItems.stream().filter(item -> item.getName().equals(token)).collect(Collectors.toList()));
                        }
                    }

                    if (am.isMultiple()) {
                        property = new ListTypeMultipleProperty(am.getName(),
                                am.getDisplayName(), am.getDescription(),
                                selectedItems, listTypeItems, Constants.DATA_TYPE_LIST_TYPE);
                    } else {
                        property = new ListTypeProperty(am.getName(),
                                am.getDisplayName(), am.getDescription(),
                                (selectedItems.size() > 0 ? selectedItems.get(0) : null), listTypeItems, Constants.DATA_TYPE_LIST_TYPE);
                    }
            }
            
            if (property != null) {
                objectProperties.add(property);
            }
        }
        return objectProperties;
    }
    
    public static List<AbstractProperty> generalPropertiesFromClass(ClassMetadata classMetadata) {

        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
        AbstractProperty property;
         
        property = new StringProperty(Constants.PROPERTY_NAME,
                            Constants.PROPERTY_NAME, Constants.PROPERTY_NAME,
                            classMetadata.getName() == null  || classMetadata.getName().isEmpty() ?
                                    "<Not Set>" : classMetadata.getName(),
                             Constants.DATA_TYPE_STRING);
        objectProperties.add(property);
        
        property = new StringProperty(Constants.PROPERTY_DISPLAY_NAME,
                            Constants.PROPERTY_DISPLAY_NAME, Constants.PROPERTY_DISPLAY_NAME,
                            classMetadata.getDisplayName()== null  || classMetadata.getDisplayName().isEmpty() ?
                                    "<Not Set>" : classMetadata.getDisplayName(),
                             Constants.DATA_TYPE_STRING);
        objectProperties.add(property);
        
        property = new StringProperty(Constants.PROPERTY_DESCRIPTION,
                            Constants.PROPERTY_DESCRIPTION, Constants.PROPERTY_DESCRIPTION,
                            classMetadata.getDescription()== null  || classMetadata.getDescription().isEmpty() ?
                                    "<Not Set>" : classMetadata.getDescription(),
                             Constants.DATA_TYPE_STRING);
        objectProperties.add(property);
        
        property = new BooleanProperty(Constants.PROPERTY_ABSTRACT,
                            Constants.PROPERTY_ABSTRACT, Constants.PROPERTY_ABSTRACT,
                            classMetadata.isAbstract() == null ? false : classMetadata.isAbstract(),
                             Constants.DATA_TYPE_BOOLEAN);
        objectProperties.add(property);
        
        property = new BooleanProperty(Constants.PROPERTY_IN_DESIGN,
                            Constants.PROPERTY_IN_DESIGN, Constants.PROPERTY_IN_DESIGN,
                            classMetadata.isInDesign()== null ? false : classMetadata.isInDesign(),
                             Constants.DATA_TYPE_BOOLEAN);
        objectProperties.add(property);
        
        property = new BooleanProperty(Constants.PROPERTY_COUNTABLE,
                            Constants.PROPERTY_COUNTABLE, Constants.PROPERTY_COUNTABLE,
                            classMetadata.isCountable()== null ? false : classMetadata.isCountable(),
                             Constants.DATA_TYPE_BOOLEAN);
        objectProperties.add(property);
        
        String hexColor = String.format("#%06x", (0xFFFFFF & classMetadata.getColor())); 
//        String hexColor = "#"+ Integer.toHexString(classMetadata.getColor());
        property = new ColorProperty(Constants.PROPERTY_COLOR,
                            Constants.PROPERTY_COLOR, Constants.PROPERTY_COLOR,
                            hexColor,
                            Constants.DATA_TYPE_COLOR);
        objectProperties.add(property);

        return objectProperties;
    }
    
    public static List<AbstractProperty> generalPropertiesFromAttribute(AttributeMetadata attributeMetadata) {

        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
        AbstractProperty property;
        
        boolean readOnlyAttribute = false;
        //special case for creation date attribute
        readOnlyAttribute = Constants.PROPERTY_CREATION_DATE.equals(attributeMetadata.getName());
                        
         
        property = new StringProperty(Constants.PROPERTY_NAME,
                            Constants.PROPERTY_NAME, Constants.PROPERTY_NAME,
                            attributeMetadata.getName() == null  || attributeMetadata.getName().isEmpty() ?
                                    "<Not Set>" : attributeMetadata.getName(),
                             Constants.DATA_TYPE_STRING, readOnlyAttribute);
        objectProperties.add(property);
        
        property = new StringProperty(Constants.PROPERTY_DISPLAY_NAME,
                            Constants.PROPERTY_DISPLAY_NAME, Constants.PROPERTY_DISPLAY_NAME,
                            attributeMetadata.getDisplayName()== null  || attributeMetadata.getDisplayName().isEmpty() ?
                                    "<Not Set>" : attributeMetadata.getDisplayName(),
                             Constants.DATA_TYPE_STRING, readOnlyAttribute);
        objectProperties.add(property);
        
        property = new StringProperty(Constants.PROPERTY_DESCRIPTION,
                            Constants.PROPERTY_DESCRIPTION, Constants.PROPERTY_DESCRIPTION,
                            attributeMetadata.getDescription()== null  || attributeMetadata.getDescription().isEmpty() ?
                                    "<Not Set>" : attributeMetadata.getDescription(),
                             Constants.DATA_TYPE_STRING, readOnlyAttribute);
        objectProperties.add(property);
        
        property = new BooleanProperty(Constants.PROPERTY_MANDATORY,
                            Constants.PROPERTY_MANDATORY, Constants.PROPERTY_MANDATORY,
                            attributeMetadata.isMandatory() == null ? false : attributeMetadata.isMandatory(),
                             Constants.DATA_TYPE_BOOLEAN, readOnlyAttribute);
        objectProperties.add(property);
        
        property = new BooleanProperty(Constants.PROPERTY_UNIQUE,
                            Constants.PROPERTY_UNIQUE, Constants.PROPERTY_UNIQUE,
                            attributeMetadata.isUnique() == null ? false : attributeMetadata.isUnique(),
                             Constants.DATA_TYPE_BOOLEAN, readOnlyAttribute);
        objectProperties.add(property);
        
        property = new BooleanProperty(Constants.PROPERTY_MULTIPLE,
                            Constants.PROPERTY_MULTIPLE, Constants.PROPERTY_MULTIPLE,
                            attributeMetadata.isMultiple()== null ? false : attributeMetadata.isMultiple(),
                             Constants.DATA_TYPE_BOOLEAN, readOnlyAttribute);
        objectProperties.add(property);
        
        property = new BooleanProperty(Constants.PROPERTY_VISIBLE,
                            Constants.PROPERTY_VISIBLE, Constants.PROPERTY_VISIBLE,
                            attributeMetadata.isVisible()== null ? false : attributeMetadata.isVisible(),
                             Constants.DATA_TYPE_BOOLEAN, readOnlyAttribute);
        objectProperties.add(property);
        
        property = new BooleanProperty(Constants.PROPERTY_ADMINISTRATIVE,
                            Constants.PROPERTY_ADMINISTRATIVE, Constants.PROPERTY_ADMINISTRATIVE,
                            attributeMetadata.isAdministrative()== null ? false : attributeMetadata.isAdministrative(),
                             Constants.DATA_TYPE_BOOLEAN);
        objectProperties.add(property);
        
        property = new BooleanProperty(Constants.PROPERTY_NO_COPY,
                            Constants.PROPERTY_ADMINISTRATIVE, Constants.PROPERTY_NO_COPY,
                            attributeMetadata.isNoCopy()== null ? false : attributeMetadata.isNoCopy(),
                             Constants.DATA_TYPE_BOOLEAN, readOnlyAttribute);
        objectProperties.add(property);
        
        property = new IntegerProperty(Constants.PROPERTY_ORDER,
                            Constants.PROPERTY_ORDER, Constants.PROPERTY_ORDER,
                            attributeMetadata.getOrder() == null ? null : attributeMetadata.getOrder(),
                             Constants.DATA_TYPE_INTEGER, readOnlyAttribute);
        objectProperties.add(property);

        return objectProperties;
    }
}
