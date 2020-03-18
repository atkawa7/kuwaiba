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
package org.neotropic.kuwaiba.core.persistence.reference.util;

import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to get an ascending alphabetic lowercase sequence, given the start and end of the sequence
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class AlphabeticLowercaseSequence extends DynamicSectionFunction {
    public static final String FUNCTION_PATTERN = "sequence\\([a-z],[a-z]\\)";
    protected char parameter1;
    protected char parameter2;
    
    protected AlphabeticLowercaseSequence(String dynamicSectionPattern, String dynamicSectionFunction) throws InvalidArgumentException {
        super(dynamicSectionPattern, dynamicSectionFunction);
    }
        
    public AlphabeticLowercaseSequence(String dynamicSectionFunction) throws InvalidArgumentException {
        this(FUNCTION_PATTERN, dynamicSectionFunction);
        
        Pattern pattern = Pattern.compile("[a-z],[a-z]");
        Matcher matcher = pattern.matcher(dynamicSectionFunction);
        if (matcher.find()) {
            parameter1 = matcher.group().split(",")[0].charAt(0);
            parameter2 = matcher.group().split(",")[1].charAt(0);
                
            if (parameter1 >= parameter2)
                throw new InvalidArgumentException("Function definition malformed: In \"" + dynamicSectionFunction + "\", the parameter " + parameter1 + " is greater than or equal to " + parameter2);
        }
    }
        
    @Override
    public List<String> getPossibleValues() {
        List<String> dynamicSections = new ArrayList();
        for (char c = parameter1; c <= parameter2; c++)
            dynamicSections.add("" + c);
        return dynamicSections;
    }
}
