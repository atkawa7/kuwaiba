/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.persistence.exceptions;

import java.util.logging.Level;

/**
 * Thrown when the size of two (or more arrays) does not match, but should
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ArraySizeMismatchException extends InventoryException {

    public ArraySizeMismatchException(String... arrayNames) {
        super("",Level.SEVERE); //NOI18N
        String arrays="";
        for (String arrayName: arrayNames)
            arrays+=arrayName+',';
        initCause(new Throwable("The following arrays does not have the same lenght: "+arrays.substring(0, arrays.length() - 1)));
    }

}
