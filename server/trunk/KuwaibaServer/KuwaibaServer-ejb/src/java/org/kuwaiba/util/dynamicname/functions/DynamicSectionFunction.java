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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kuwaiba.apis.persistence.exceptions.InvalidArgumentException;

/**
 * An Abstract class used to define the method to get the possible values of the current function
 * A dynamic section are a word between a left square bracket "[" and a right square bracket "]"
 * the content in the brackets are a function, use to define a Dynamic Name see <code>DynamicName</code>
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public abstract class DynamicSectionFunction {
    /**
     * @param functionPattern Regular expression used to verify if the entry dynamic section has  the syntax correct for the current function.
     * @param dynamicSection A dynamic section are a word between a left square bracket "[" and a right square bracket "]".
     * @throws InvalidArgumentException If the given dynamic section do not match with the given pattern
     */        
    public DynamicSectionFunction(String functionPattern, String dynamicSection) throws InvalidArgumentException {
        Pattern pattern = Pattern.compile(functionPattern);
        Matcher matcher = pattern.matcher(dynamicSection);
        
        if (!matcher.find())
            throw new InvalidArgumentException("Function in Dynamic Section \"" + dynamicSection + "\" not match with the pattern " + functionPattern);
    }
        
    /**
     * Gets possible values generated with the defined function inside the Dynamic section
     * @return A list of possible values generated using the current function
     */                   
    public abstract List<String> getPossibleValues();
}
