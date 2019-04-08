/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.util.dynamicname.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.apis.persistence.application.ApplicationEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.BusinessObject;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.BusinessObjectNotFoundException;
import org.kuwaiba.apis.persistence.metadata.ClassMetadata;
import org.kuwaiba.apis.persistence.metadata.MetadataEntityManager;

/**
 * A dynamic section function used to get an attribute value from an object
 * given the object id.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class FunctionValue extends DynamicSectionFunction {
    public static final String FUNCTION_PATTERN = "value\\([0-9]+,[a-zA-Z]+\\)";
    private String uuid;
    private String attribute;
    /**
     * Remote business object representation of the object with the given id
     */
    private BusinessObject remoteBusinessObject;
    /**
     * Class metadata to the object with the given id
     */
    private ClassMetadata classMetadata;
    
    public FunctionValue(String dynamicSectionFunction) throws InvalidArgumentException {
        super(FUNCTION_PATTERN, dynamicSectionFunction);
        
        Pattern pattern = Pattern.compile("[0-9]+,[a-zA-Z]+");
        Matcher matcher = pattern.matcher(dynamicSectionFunction);
        if (matcher.find()) {
            uuid = matcher.group().split(",")[0];
            attribute = matcher.group().split(",")[1];
        }
        
        try {
            BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
            remoteBusinessObject = bem.getObject("XXXXXXXXXXXX", uuid);
            
            MetadataEntityManager mem = PersistenceService.getInstance().getMetadataEntityManager();
            classMetadata = mem.getClass(remoteBusinessObject.getClassName());
            
            if (classMetadata.getAttribute(attribute) == null)
                throw new InvalidArgumentException(String.format("The attribute \"%s\" can not be found in object with id %s", attribute, uuid));
            
        } catch (BusinessObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new InvalidArgumentException(String.format("The object with id %s could not be found", uuid));
        }
    }
    
    @Override
    public List<String> getPossibleValues() {
        List<String> dynamicSections = new ArrayList();
        try {
            ApplicationEntityManager aem = PersistenceService.getInstance().getApplicationEntityManager();
            MetadataEntityManager mem = PersistenceService.getInstance().getMetadataEntityManager();
            
            String attributeValue = remoteBusinessObject.getAttributes().get(attribute);            
            String attributeType = classMetadata.getAttribute(attribute).getType();
            
            if (attributeValue != null && mem.isSubclassOf("GenericObjectList", attributeType)) {
                BusinessObjectLight item = aem.getListTypeItem(attributeType, attributeValue);
                if (item != null)
                    attributeValue = item.getName();
            }
            
            if (attributeValue == null)
                attributeValue = "__"; // If the attribute value is null
                            
            dynamicSections.add(attributeValue);
            
        } catch (Exception ex) {
            dynamicSections.add("__"); // If the attribute value is null
        }
        return dynamicSections;
    }
}
