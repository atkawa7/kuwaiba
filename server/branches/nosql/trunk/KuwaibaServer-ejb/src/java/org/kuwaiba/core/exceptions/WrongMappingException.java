/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
 *
 */

package org.kuwaiba.core.exceptions;

import java.util.Formatter;
import java.util.logging.Level;

/**
 * This exception is raised when a pair attribute type - attribute value is not valid
 * (i.e. type Integer, value "aaaaa") and the latter can't be converted
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class WrongMappingException extends InventoryException{

    public WrongMappingException(String className, String attributeName,
            String attributeType, String attributeValue) {
        super (new Formatter().format("The value %1s can't mapped to a type %2s for the attribute %3s in class %4s",
                attributeValue, attributeType,attributeName,className).toString(), Level.WARNING);
    }

}
