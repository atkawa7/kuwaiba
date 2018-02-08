/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.services.api.actions;

import javax.swing.AbstractAction;
import org.inventory.communications.core.LocalPrivilege;

/**
 * This must be the root of all actions in Kuwaiba. Subclasses must be able top tell what privilege is necessary to execute them
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class GenericInventoryAction extends AbstractAction {
    /**
     * The feature tokens necessary for this action to be available. The returned values will be checked against the current user privileges.
     * @return The list of privileges necessary for this action to be invoked
     */
    public abstract LocalPrivilege getPrivilege();
}
