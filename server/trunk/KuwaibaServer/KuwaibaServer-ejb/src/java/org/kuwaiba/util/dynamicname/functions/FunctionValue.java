/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
import org.kuwaiba.apis.persistence.business.BusinessEntityManager;
import org.kuwaiba.apis.persistence.business.RemoteBusinessObject;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.kuwaiba.apis.persistence.exceptions.ObjectNotFoundException;

/**
 * A dynamic section function used to get an attribute value from an object
 * given the object id.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FunctionValue extends DynamicSectionFunction {
    public static final String FUNCTION_PATTERN = "value\\([0-9]+,[a-zA-z]+\\)";
    private long id;
    private String attribute;

    public FunctionValue(String dynamicSectionFunction) throws InvalidArgumentException {
        super(FUNCTION_PATTERN, dynamicSectionFunction);
        
        Pattern pattern = Pattern.compile("[0-9]+,[a-zA-z]+");
        Matcher matcher = pattern.matcher(dynamicSectionFunction);
        if (matcher.find()) {
            id = Long.parseLong(matcher.group().split(",")[0]);
            attribute = matcher.group().split(",")[1];
        }
        
        try {
            BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
            
            RemoteBusinessObject rmo = bem.getObject(id);
            
            if (!rmo.getAttributes().containsKey(attribute))
                throw new InvalidArgumentException(String.format("The attribute \"%s\" can not be found for the object with id %s", attribute, id));
            
        } catch (ObjectNotFoundException | MetadataObjectNotFoundException ex) {
            throw new InvalidArgumentException(String.format("The object with id %s can not be found", id));
        }
    }
    
    @Override
    public List<String> getPossibleValues() {
        try {
            BusinessEntityManager bem = PersistenceService.getInstance().getBusinessEntityManager();
            
            String attributeValue;
            RemoteBusinessObject rmo = bem.getObject(id);
            if (rmo.getAttributes().containsKey(attribute)) {
                attributeValue = (String) rmo.getAttributes().get(attribute).get(0);
            } else {
                return null;
            }
            List<String> dynamicSections = new ArrayList();
            dynamicSections.add(attributeValue);
            return dynamicSections;
            
        } catch (Exception ex) {
            return null;
        }
    }
}
