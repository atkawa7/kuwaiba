/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
 * Thrown if you're trying to access to a non existing object
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ObjectNotFoundException extends InventoryException{

    public ObjectNotFoundException(String objectClass, Long oid) {
        super(java.util.ResourceBundle.getBundle("org/kuwaiba/Bundle").
                getString("LBL_NOSUCHOBJECT")+(objectClass == null ? "" : objectClass)+" "+java.util.
                ResourceBundle.getBundle("org/kuwaiba/Bundle").getString("LBL_WHICHID")+oid,Level.WARNING);
    }
}
