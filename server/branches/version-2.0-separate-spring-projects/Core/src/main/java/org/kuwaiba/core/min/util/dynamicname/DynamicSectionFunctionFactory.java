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
package org.kuwaiba.core.min.util.dynamicname;

import org.kuwaiba.core.min.util.dynamicname.functions.FunctionValue;
import org.kuwaiba.core.min.util.dynamicname.functions.NumericSequence;
import org.kuwaiba.core.min.util.dynamicname.functions.AlphabeticUppercaseSequence;
import org.kuwaiba.core.min.util.dynamicname.functions.AlphabeticLowercaseSequence;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.kuwaiba.core.min.apis.persistence.exceptions.InvalidArgumentException;
import org.kuwaiba.core.min.util.dynamicname.functions.MirrorPortsFunction;

/**
 * Factory to dynamic name functions
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DynamicSectionFunctionFactory {
    
    private static boolean isDynamicSectionFunction(String functionPattern, String dynamicSection) {
        Pattern pattern = Pattern.compile(functionPattern);
        Matcher matcher = pattern.matcher(dynamicSection);
        
        return matcher.find();
    }
        
    public static AlphabeticUppercaseSequence getAlphabeticUppercaseSequence(String dynamicSection) 
        throws InvalidArgumentException {
        
        if (isDynamicSectionFunction(AlphabeticUppercaseSequence.FUNCTION_PATTERN, dynamicSection))
            return new AlphabeticUppercaseSequence(dynamicSection);
        return null;
    }
    
    public static AlphabeticLowercaseSequence getAlphabeticLowercaseSequence(String dynamicSection) 
        throws InvalidArgumentException {
        
        if (isDynamicSectionFunction(AlphabeticLowercaseSequence.FUNCTION_PATTERN, dynamicSection))
            return new AlphabeticLowercaseSequence(dynamicSection);
        return null;
    }
    
    public static NumericSequence getNumericSequence(String dynamicSection) 
        throws InvalidArgumentException {
        
        if (isDynamicSectionFunction(NumericSequence.FUNCTION_PATTERN, dynamicSection))
            return new NumericSequence(dynamicSection);
        return null;
    }
    
    public static FunctionValue getFunctionValue(String dynamicSection) 
        throws InvalidArgumentException {
        
        if (isDynamicSectionFunction(FunctionValue.FUNCTION_PATTERN, dynamicSection))
            return new FunctionValue(dynamicSection);
        return null;
    }
    
    public static MirrorPortsFunction getMirrorPortsPairing(String dynamicSection)
            throws InvalidArgumentException {
        
        if (isDynamicSectionFunction(MirrorPortsFunction.FUNCTION_PATTERN, dynamicSection))
            return new MirrorPortsFunction(dynamicSection);
        return null;
    }
}
