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

package org.kuwaiba.apis.persistence.application;

import java.io.Serializable;

/**
 * Codes assigned to the different available privileges
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class Privileges implements Serializable{
    /**
     * User/ group can login into the application. It's a dummy permission
     */
    public static final int PRIVILEGE_LOGIN = 0;
    /**
     * User/group can create objects
     */
    public static final int PRIVILEGE_CREATE_OBJECT = 1;
    /**
     * User/group can create classes
     */
    public static final int PRIVILEGE_CREATE_CLASS = 2;
}
