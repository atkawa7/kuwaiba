/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.util;

import java.util.Formatter;

/**
 * Misc utility methods
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class Util {
    /**
     * Formats a String. It's basically a wrapper for Formatter.format() method
     * @param stringToFormat String to be formatted
     * @param args a variable set of arguments to be used with the formatter
     * @return The resulting string of merging @stringToFormat with @args
     */
    public static String formatString(String stringToFormat,Object ... args){
        Formatter formatter = new Formatter();
        return formatter.format(stringToFormat, args).toString();
    }
}
