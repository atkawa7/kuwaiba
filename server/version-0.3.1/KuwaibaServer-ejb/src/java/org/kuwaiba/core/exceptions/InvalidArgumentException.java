/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
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

package org.kuwaiba.core.exceptions;

import java.util.logging.Level;

/**
 * Yeah, there's already an IllegalArgumentException, but this wrapper let me set the severity
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class InvalidArgumentException extends InventoryException{

    public InvalidArgumentException(String msg, Level level) {
        super(msg,level);
    }

}
