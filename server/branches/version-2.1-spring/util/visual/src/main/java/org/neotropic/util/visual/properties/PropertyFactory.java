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

import com.vaadin.flow.component.UI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObject;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.AttributeMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadata;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.ClassMetadataLight;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;



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
    public static List<AbstractProperty> propertiesFromRemoteObject(BusinessObjectLight businessObject, ApplicationEntityManager aem, BusinessEntityManager bem, MetadataEntityManager mem) throws MetadataObjectNotFoundException, BusinessObjectNotFoundException, InvalidArgumentException, ApplicationObjectNotFoundException {

        HashMap<String, String> objectAttributes = bem.getAttributeValuesAsString(businessObject.getClassName(), businessObject.getId());

        ClassMetadata classMetadata = mem.getClass(businessObject.getClassName());

        ArrayList<AbstractProperty> objectProperties = new ArrayList<>();
  
        for (AttributeMetadata am : classMetadata.getAttributes()) {
            AbstractProperty property = null;

            switch (am.getType()) {
                case Constants.DATA_TYPE_STRING:

                    property = new StringProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            objectAttributes.get(am.getName()) == null ? "<Not Set>" : objectAttributes.get(am.getName()));
                    break;
                case Constants.DATA_TYPE_INTEGER:

                    property = new IntegerProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            objectAttributes.get(am.getName()) == null ? null : Integer.parseInt(objectAttributes.get(am.getName())));
                    break;
                case Constants.DATA_TYPE_DOUBLE :
                case Constants.DATA_TYPE_FLOAT:

                    property = new DoubleProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            objectAttributes.get(am.getName()) == null ? null : Double.parseDouble(objectAttributes.get(am.getName())));
                    break;
                case Constants.DATA_TYPE_LONG:

                    property = new LongProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            objectAttributes.get(am.getName()) == null ? null : Long.parseLong(objectAttributes.get(am.getName())));
                    break;
                case Constants.DATA_TYPE_DATE:
                case Constants.DATA_TYPE_TIME_STAMP:

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
                    property = new LocalDateProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            objectAttributes.get(am.getName()) == null ? null : LocalDate.parse(objectAttributes.get(am.getName()), formatter));
                    break;
                default:   // list type
                     List<BusinessObjectLight> listTypeItems = aem.getListTypeItems(am.getType());
                     List<BusinessObjectLight> selectedItems = listTypeItems.stream().filter(item -> item.getName().equals(objectAttributes.get(am.getName()))).collect(Collectors.toList());

                     property = new ListTypeProperty(am.getName(),
                            am.getDisplayName(), am.getDescription(),
                            selectedItems.size() > 0  ? selectedItems.get(0) : null , listTypeItems);
            }
            if (property != null) {
                objectProperties.add(property);
            }
        }
        return objectProperties;
    }
}
