/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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

package org.kuwaiba.apis.persistence.exceptions;

import java.util.logging.Level;

/**
 * Thrown when an inconsistency in the database structure is detected
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class DatabaseException extends InventoryException{

    public DatabaseException(String msg) {
        super(msg, Level.SEVERE);
    }

}
