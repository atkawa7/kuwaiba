/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>
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
package org.neotropic.kuwaiba.visualization.api.properties;

import com.vaadin.flow.component.ItemLabelGenerator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ConfigurationVariable;
import org.neotropic.kuwaiba.core.apis.persistence.application.Pool;
import org.neotropic.kuwaiba.core.apis.persistence.application.TemplateObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InventoryException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.properties.AbstractProperty;
import org.neotropic.util.visual.properties.BooleanProperty;
import org.neotropic.util.visual.properties.ColorProperty;
import org.neotropic.util.visual.properties.DateProperty;
import org.neotropic.util.visual.properties.DoubleProperty;
import org.neotropic.util.visual.properties.IntegerProperty;
import org.neotropic.util.visual.properties.LocalDateProperty;
import org.neotropic.util.visual.properties.LocalDateTimeProperty;
import org.neotropic.util.visual.properties.LongProperty;
import org.neotropic.util.visual.properties.ObjectMultipleProperty;
import org.neotropic.util.visual.properties.ObjectProperty;
import org.neotropic.util.visual.properties.StringProperty;

/**
 * A factory class that builds property sets given business objects.
 *
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */
public class PropertyFactory {

    /**
     * Builds a property set from a given inventory object
     *
     * @param businessObject The business object
     * @param ts Reference to the translation service.
     * @param aem ApplicationEntityManager service.
     * @param mem MetadataEntityManager service.
     * @return The set of properties ready to used in a property sheet component
     * @throws InventoryException
     */
    public static List<AbstractProperty> propertiesFromBusinessObject(BusinessObject businessObject, TranslationService ts, ApplicationEntityManager aem,
            MetadataEntityManager mem) throws InventoryException {
        ClassMetadata classMetadata = mem.getClass(businessObject.getClassName());
        classMetadata.getAttributes().sort(Comparator.comparing(item -> item.getOrder()));
                
        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
        HashMap<String, Object> attributes = businessObject.getAttributes();
        classMetadata.getAttributes().stream().forEach(anAttribute -> {
            try {
                switch (anAttribute.getType()) {
                    case Constants.DATA_TYPE_STRING:

                    objectProperties.add(new StringProperty(anAttribute.getName(),
                            anAttribute.getDisplayName(), anAttribute.getDescription(),
                            (!attributes.containsKey(anAttribute.getName()) ? null : (String) attributes.get(anAttribute.getName())) ));
                        break;
                    case Constants.DATA_TYPE_DOUBLE:
                    case Constants.DATA_TYPE_FLOAT: {
                        Double value;
                        if (attributes.containsKey(anAttribute.getName())) {
                            if (attributes.get(anAttribute.getName()) instanceof String)
                                value = Double.valueOf( (String) attributes.get(anAttribute.getName()));
                            else {
                                if (attributes.get(anAttribute.getName()) instanceof Float)
                                    value = Double.valueOf(String.valueOf( attributes.get(anAttribute.getName())));
                                else
                                    value = (Double) attributes.get(anAttribute.getName());
                            }        
                        } else
                            value = null;
                        objectProperties.add(new DoubleProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                anAttribute.getDescription(), value));
                        break;
                    }
                    case Constants.DATA_TYPE_INTEGER:{

                        Integer value;
                        if (attributes.containsKey(anAttribute.getName())) {
                            if (attributes.get(anAttribute.getName()) instanceof String)
                                value = Integer.valueOf( (String) attributes.get(anAttribute.getName()));
                            else 
                                value = (Integer) attributes.get(anAttribute.getName());
                        } else
                            value = null;
                        objectProperties.add(new IntegerProperty(anAttribute.getName(),
                            anAttribute.getDisplayName(), anAttribute.getDescription(), value));
                        break;
                    }
                    case Constants.DATA_TYPE_BOOLEAN: {
                        Boolean value;
                        if (attributes.containsKey(anAttribute.getName())) {
                            if (attributes.get(anAttribute.getName()) instanceof String)
                                value = Boolean.valueOf( (String) attributes.get(anAttribute.getName()));
                            else 
                                value = (Boolean) attributes.get(anAttribute.getName());
                        } else
                            value = false;
                        objectProperties.add(new BooleanProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                anAttribute.getDescription(), value));
                        break;
                    }
                    case Constants.DATA_TYPE_LONG: {
                        Long value;
                        if (attributes.containsKey(anAttribute.getName())) {
                            if (attributes.get(anAttribute.getName()) instanceof String)
                                value = Long.valueOf( (String) attributes.get(anAttribute.getName()));
                            else 
                                value = (Long) attributes.get(anAttribute.getName());
                        } else
                            value = null;
                        LongProperty aLongProperty = new LongProperty(anAttribute.getName(), 
                                anAttribute.getDisplayName(), anAttribute.getDescription(), value);
                        //special case for creation date attribute
                        if (Constants.PROPERTY_CREATION_DATE.equals(anAttribute.getName())) {
                            aLongProperty.setReadOnly(true);
                        }
                        objectProperties.add(aLongProperty);
                        break;
                    }
                    case Constants.DATA_TYPE_DATE: {
                        Long value;
                        if (attributes.containsKey(anAttribute.getName())) {
                            if (attributes.get(anAttribute.getName()) instanceof String)
                                value = Long.valueOf( (String) attributes.get(anAttribute.getName()));
                            else 
                                value = (Long) attributes.get(anAttribute.getName());
                        } else
                            value = 0l;
                        LocalDateProperty aDateProperty = new LocalDateProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                anAttribute.getDescription(), value);
                        aDateProperty.setReadOnly(anAttribute.getName().equals(Constants.PROPERTY_CREATION_DATE));
                        objectProperties.add(aDateProperty);
                        break;
                    }
                    case Constants.DATA_TYPE_TIME_STAMP: {
                        Long value;
                        if (attributes.containsKey(anAttribute.getName())) {
                            if (attributes.get(anAttribute.getName()) instanceof String)
                                value = Long.valueOf( (String) attributes.get(anAttribute.getName()));
                            else 
                                value = (Long) attributes.get(anAttribute.getName());
                        } else
                            value = 0l;
                        LocalDateTimeProperty aDateTimeProperty = new LocalDateTimeProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                anAttribute.getDescription(), value);
                        aDateTimeProperty.setReadOnly(anAttribute.getName().equals(Constants.PROPERTY_CREATION_DATE));
                        objectProperties.add(aDateTimeProperty);
                        break;
                    }
                    default:
                        try {
                        List<BusinessObjectLight>  items = aem.getListTypeItems(anAttribute.getType());
                        items.add(0, new BusinessObject("", "", "none"));
                        
                        List<BusinessObjectLight> selectedItems = new ArrayList<>();
                        String attributeValue = (String) attributes.get(anAttribute.getName());

                        if (attributeValue != null) {
                            String[] tokensItems = attributeValue.split(";");

                            for (String token : tokensItems) {
                                selectedItems.addAll(items.stream().filter(item -> item.getId().equals(token)).collect(Collectors.toList()));
                            }
                        }
                        if (anAttribute.isMultiple()) {
                            objectProperties.add(new ObjectMultipleProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                    anAttribute.getDescription(), 
                                    new ArrayList<>(selectedItems), new ArrayList<>(items), anAttribute.getType()));
                        } else {
                            objectProperties.add(new ObjectProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                    anAttribute.getDescription(), (selectedItems.size() > 0 ? selectedItems.get(0) : null),
                                    new ArrayList<>(items), anAttribute.getType(), (selectedItems.size() > 0 ? selectedItems.get(0).getName() : ""),
                                    (ItemLabelGenerator) (Object t) -> {
                                        if (t instanceof BusinessObjectLight) {
                                            return ((BusinessObjectLight) t).getName();
                                        }
                                        return "";
                            }));
                        }
                    } catch (InventoryException ex) {
                        Logger.getLogger(PropertyFactory.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
                    }
                    break;
                }
            } catch (Exception ex) { // Faulty values will be ignored and silently logged
                Logger.getLogger(PropertyFactory.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            }
        });

        return objectProperties;
    }

    /**
     * Builds a property set from a given template object
     *
     * @param templateObject The business object
     * @param ts Reference to the translation service.
     * @param aem ApplicationEntityManager service.
     * @param mem MetadataEntityManager service.
     * @return The set of properties ready to used in a property sheet component
     * @throws InventoryException
     */
    public static List<AbstractProperty> propertiesFromTemplateObject(TemplateObject templateObject, TranslationService ts, ApplicationEntityManager aem,
            MetadataEntityManager mem) throws InventoryException {
        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
        ClassMetadata classMetadata = mem.getClass(templateObject.getClassName());
        classMetadata.getAttributes().stream().forEach(anAttribute -> {
            try {
                if (AttributeMetadata.isPrimitive(anAttribute.getType())) {

                    switch (anAttribute.getType()) {
                        case Constants.DATA_TYPE_STRING:
                            objectProperties.add(new StringProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                    anAttribute.getDescription(), (String) templateObject.getAttributes().get(anAttribute.getName())));
                            break;
                        case Constants.DATA_TYPE_FLOAT:
                            if (templateObject.getAttributes().get(anAttribute.getName()) != null) {
                                objectProperties.add(new DoubleProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                        anAttribute.getDescription(), Double.valueOf(templateObject.getAttributes().get(anAttribute.getName()))));
                            } else
                                objectProperties.add(new DoubleProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                        anAttribute.getDescription(), null));
                            break;
                        case Constants.DATA_TYPE_INTEGER:
                            if (templateObject.getAttributes().get(anAttribute.getName()) != null) {
                                objectProperties.add(new IntegerProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                        anAttribute.getDescription(), Integer.valueOf(templateObject.getAttributes().get(anAttribute.getName()))));
                            }
                            break;
                        case Constants.DATA_TYPE_BOOLEAN:
                            if (templateObject.getAttributes().get(anAttribute.getName()) != null) {
                                objectProperties.add(new BooleanProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                        anAttribute.getDescription(), Boolean.valueOf(templateObject.getAttributes().get(anAttribute.getName()))));
                            } else
                                objectProperties.add(new DoubleProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                        anAttribute.getDescription(), null));
                            break;
                        case Constants.DATA_TYPE_LONG:
                            if (templateObject.getAttributes().get(anAttribute.getName()) != null) {
                                LongProperty aLongProperty = new LongProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                        anAttribute.getDescription(), Long.valueOf(templateObject.getAttributes().get(anAttribute.getName())));
                                
                                objectProperties.add(aLongProperty);
                            } else
                                objectProperties.add(new DoubleProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                        anAttribute.getDescription(), null));
                            
                            break;
                        case Constants.DATA_TYPE_DATE:
                        case Constants.DATA_TYPE_TIME_STAMP:
                            if (templateObject.getAttributes().get(anAttribute.getName()) != null) {
                                DateProperty aDateProperty = new DateProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                        anAttribute.getDescription(), Long.valueOf(templateObject.getAttributes().get(anAttribute.getName())));
                                aDateProperty.setReadOnly(anAttribute.getName().equals(Constants.PROPERTY_CREATION_DATE));
                                objectProperties.add(aDateProperty);
                            } else {
                                DateProperty aDateProperty = new DateProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                        anAttribute.getDescription(), null);
                                //special case for creation date attribute
                                aDateProperty.setReadOnly(anAttribute.getName().equals(Constants.PROPERTY_CREATION_DATE));                                
                                objectProperties.add(aDateProperty);                                
                            }
                            break;

                    }
                } else {
                    try {
                        List items = aem.getListTypeItems(anAttribute.getType());
                        if (anAttribute.isMultiple()) {
                            String listTypeids = templateObject.getAttributes().get(anAttribute.getName());
                            String[] split = listTypeids.split(",");
                            List<BusinessObject> objetcs = new ArrayList<>();
                            for (String listTypeId : split) {
                                BusinessObject listTypeItem = aem.getListTypeItem(anAttribute.getType(), listTypeId);
                                objetcs.add(listTypeItem);

                            }
                            String listTypeItemId = templateObject.getAttributes().get(anAttribute.getName());
                            if (listTypeItemId != null) {
                                objectProperties.add(new ObjectMultipleProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                        anAttribute.getDescription(), objetcs, items, anAttribute.getType()));
                            }
                        } else {
                            String listTypeItemId = templateObject.getAttributes().get(anAttribute.getName());
                            if (listTypeItemId != null) {
                                BusinessObject listTypeItem = aem.getListTypeItem(anAttribute.getType(), listTypeItemId);
                                objectProperties.add(new ObjectProperty(anAttribute.getName(), anAttribute.getDisplayName(),
                                        anAttribute.getDescription(), listTypeItem, items, anAttribute.getType(), (listTypeItem != null ? listTypeItem.getName() : ""),
                                 (ItemLabelGenerator) (Object t) -> {
                                            if (t instanceof BusinessObjectLight) {
                                                return ((BusinessObjectLight) t).getName();
                                            }
                                            return "";
                                }));
                            }
                        }
                    } catch (InventoryException ex) {
                        Logger.getLogger(PropertyFactory.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
                    }
                }

            } catch (Exception ex) { // Faulty values will be ignored and silently logged
                Logger.getLogger(PropertyFactory.class.getName()).log(Level.SEVERE,
                        String.format(ts.getTranslatedString("module.propertysheet.labels.wrong-data-type"), anAttribute.getName(),
                                templateObject.getId(), templateObject.getAttributes().get(anAttribute.getName())));
            }
        });

        return objectProperties;
    }

    public static List<AbstractProperty> generalPropertiesFromClass(ClassMetadata classMetadata) {

        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
        AbstractProperty property;

        property = new StringProperty(Constants.PROPERTY_NAME,
                Constants.PROPERTY_NAME, Constants.PROPERTY_NAME,
                classMetadata.getName() == null || classMetadata.getName().isEmpty()
                ? AbstractProperty.NULL_LABEL : classMetadata.getName());
        objectProperties.add(property);

        property = new StringProperty(Constants.PROPERTY_DISPLAY_NAME,
                Constants.PROPERTY_DISPLAY_NAME, Constants.PROPERTY_DISPLAY_NAME,
                classMetadata.getDisplayName() == null || classMetadata.getDisplayName().isEmpty()
                ? AbstractProperty.NULL_LABEL : classMetadata.getDisplayName());
        objectProperties.add(property);

        property = new StringProperty(Constants.PROPERTY_DESCRIPTION,
                Constants.PROPERTY_DESCRIPTION, Constants.PROPERTY_DESCRIPTION,
                classMetadata.getDescription() == null || classMetadata.getDescription().isEmpty()
                ? AbstractProperty.NULL_LABEL : classMetadata.getDescription());
        objectProperties.add(property);

        property = new BooleanProperty(Constants.PROPERTY_ABSTRACT,
                Constants.PROPERTY_ABSTRACT, Constants.PROPERTY_ABSTRACT,
                classMetadata.isAbstract());
        objectProperties.add(property);

        property = new BooleanProperty(Constants.PROPERTY_IN_DESIGN,
                Constants.PROPERTY_IN_DESIGN, Constants.PROPERTY_IN_DESIGN,
                classMetadata.isInDesign());
        objectProperties.add(property);

        property = new BooleanProperty(Constants.PROPERTY_COUNTABLE,
                Constants.PROPERTY_COUNTABLE, Constants.PROPERTY_COUNTABLE,
                classMetadata.isCountable());
        objectProperties.add(property);

        String hexColor = String.format("#%06x", (0xFFFFFF & classMetadata.getColor()));
//        String hexColor = "#"+ Integer.toHexString(classMetadata.getColor());
        property = new ColorProperty(Constants.PROPERTY_COLOR,
                Constants.PROPERTY_COLOR, Constants.PROPERTY_COLOR,
                hexColor);
        objectProperties.add(property);

        return objectProperties;
    }

    public static List<AbstractProperty> generalPropertiesFromAttribute(AttributeMetadata attributeMetadata, MetadataEntityManager mem) {

        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
        AbstractProperty property;

        boolean readOnlyAttribute;
        //special case for creation date attribute
        readOnlyAttribute = Constants.PROPERTY_CREATION_DATE.equals(attributeMetadata.getName());

        property = new StringProperty(Constants.PROPERTY_NAME,
                Constants.PROPERTY_NAME, Constants.PROPERTY_NAME,
                attributeMetadata.getName() == null || attributeMetadata.getName().isEmpty()
                ? AbstractProperty.NULL_LABEL : attributeMetadata.getName(),
                readOnlyAttribute);
        objectProperties.add(property);

        property = new StringProperty(Constants.PROPERTY_DISPLAY_NAME,
                Constants.PROPERTY_DISPLAY_NAME, Constants.PROPERTY_DISPLAY_NAME,
                attributeMetadata.getDisplayName() == null || attributeMetadata.getDisplayName().isEmpty()
                ? AbstractProperty.NULL_LABEL : attributeMetadata.getDisplayName(),
                readOnlyAttribute);
        objectProperties.add(property);

        property = new StringProperty(Constants.PROPERTY_DESCRIPTION,
                Constants.PROPERTY_DESCRIPTION, Constants.PROPERTY_DESCRIPTION,
                attributeMetadata.getDescription() == null || attributeMetadata.getDescription().isEmpty()
                ? AbstractProperty.NULL_LABEL : attributeMetadata.getDescription(),
                readOnlyAttribute);
        objectProperties.add(property);
        
        List<ClassMetadataLight> listTypes = new ArrayList<>();
        try {
            listTypes = mem.getSubClassesLight(Constants.CLASS_GENERICOBJECTLIST, false, false);
        } catch (MetadataObjectNotFoundException | InvalidArgumentException ex) {
            Logger.getLogger(PropertyFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        List<Object> lstListTypes = listTypes.stream().map(ClassMetadataLight::getName).collect(Collectors.toList());
        List<Object> lstAllTypes  = new ArrayList(Arrays.asList(Constants.DATA_TYPES));
        lstAllTypes.addAll(lstListTypes);
        
        property = new ObjectProperty(Constants.PROPERTY_TYPE,
                Constants.PROPERTY_TYPE, Constants.PROPERTY_TYPE,
                attributeMetadata.getType(), lstAllTypes, "", attributeMetadata.getType(), 
                (ItemLabelGenerator) (Object t) -> {
                    return t.toString();
                });
        objectProperties.add(property);

        property = new BooleanProperty(Constants.PROPERTY_MANDATORY,
                Constants.PROPERTY_MANDATORY, Constants.PROPERTY_MANDATORY,
                attributeMetadata.isMandatory(),
                readOnlyAttribute);
        objectProperties.add(property);

        property = new BooleanProperty(Constants.PROPERTY_UNIQUE,
                Constants.PROPERTY_UNIQUE, Constants.PROPERTY_UNIQUE,
                attributeMetadata.isUnique(),
                readOnlyAttribute);
        objectProperties.add(property);

        property = new BooleanProperty(Constants.PROPERTY_MULTIPLE,
                Constants.PROPERTY_MULTIPLE, Constants.PROPERTY_MULTIPLE,
                attributeMetadata.isMultiple(),
                readOnlyAttribute);
        objectProperties.add(property);

        property = new BooleanProperty(Constants.PROPERTY_VISIBLE,
                Constants.PROPERTY_VISIBLE, Constants.PROPERTY_VISIBLE,
                attributeMetadata.isVisible(),
                readOnlyAttribute);
        objectProperties.add(property);

        property = new BooleanProperty(Constants.PROPERTY_ADMINISTRATIVE,
                Constants.PROPERTY_ADMINISTRATIVE, Constants.PROPERTY_ADMINISTRATIVE,
                attributeMetadata.isAdministrative()
        );
        objectProperties.add(property);

        property = new BooleanProperty(Constants.PROPERTY_NO_COPY,
                Constants.PROPERTY_NO_COPY, Constants.PROPERTY_NO_COPY,
                attributeMetadata.isNoCopy(),
                readOnlyAttribute);
        objectProperties.add(property);

        property = new IntegerProperty(Constants.PROPERTY_ORDER,
                Constants.PROPERTY_ORDER, Constants.PROPERTY_ORDER,
                attributeMetadata.getOrder(),
                readOnlyAttribute);
        objectProperties.add(property);

        return objectProperties;
    }
    
    public static List<AbstractProperty> propertiesFromConfigurationVariable(ConfigurationVariable configurationVariable) {
        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
        AbstractProperty property;
        
        property = new StringProperty(Constants.PROPERTY_NAME,
                                      Constants.PROPERTY_NAME, Constants.PROPERTY_NAME, 
                                      configurationVariable.getName()==null || configurationVariable.getName().isEmpty() ?
                                            AbstractProperty.NULL_LABEL : configurationVariable.getName());
        objectProperties.add(property);
        
        property = new StringProperty(Constants.PROPERTY_DESCRIPTION,
                                      Constants.PROPERTY_DESCRIPTION, Constants.PROPERTY_DESCRIPTION, 
                                      configurationVariable.getDescription()==null || configurationVariable.getDescription().isEmpty() ?
                                            AbstractProperty.NULL_LABEL : configurationVariable.getDescription());
        objectProperties.add(property);
        
        property = new StringProperty(Constants.PROPERTY_VALUE,
                                      Constants.PROPERTY_VALUE, Constants.PROPERTY_VALUE, 
                                      configurationVariable.getValueDefinition()==null || configurationVariable.getValueDefinition().isEmpty() ?
                                            AbstractProperty.NULL_LABEL : configurationVariable.getValueDefinition(),
                                      false, configurationVariable.isMasked());
        objectProperties.add(property);
        
        property = new BooleanProperty(Constants.PROPERTY_MASKED,
                                      Constants.PROPERTY_MASKED, Constants.PROPERTY_MASKED, 
                                      configurationVariable.isMasked());
        objectProperties.add(property);
        
        return objectProperties;
    }
    
    public static List<AbstractProperty> propertiesFromPool(Pool pool) {
        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
        AbstractProperty property;
        
        property = new StringProperty(Constants.PROPERTY_CLASS_NAME,
                                      Constants.PROPERTY_CLASS_NAME, Constants.PROPERTY_CLASS_NAME,
                                      pool.getClassName()==null || pool.getClassName().isEmpty() ?
                                            AbstractProperty.NULL_LABEL : pool.getClassName(), true);
        objectProperties.add(property);
        
        property = new StringProperty(Constants.PROPERTY_NAME,
                                      Constants.PROPERTY_NAME, Constants.PROPERTY_NAME, 
                                      pool.getName()==null || pool.getName().isEmpty() ?
                                            AbstractProperty.NULL_LABEL : pool.getName());
        objectProperties.add(property);
        
        property = new StringProperty(Constants.PROPERTY_DESCRIPTION,
                                      Constants.PROPERTY_DESCRIPTION, Constants.PROPERTY_DESCRIPTION, 
                                      pool.getDescription()==null || pool.getDescription().isEmpty() ?
                                            AbstractProperty.NULL_LABEL : pool.getDescription());
        objectProperties.add(property);
        
        return objectProperties;
    }
}
