/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
 * Thrown when an object (or one of its children) have relations that have to be removed manually
 * e.g. containers and nodes, connections and endpoints
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ObjectWithRelationsException extends InventoryException{

    public ObjectWithRelationsException() {
        super ("The object could no be deleted since it (or one of its children) has relationships that have to be removed manually",Level.INFO);
    }

}
