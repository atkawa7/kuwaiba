/**
 * Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.inventory.core.services.api.behaviors;

/**
 * This interface is used to verify the compatibility in a top component, for 
 * example if the database is outdated.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface CompatibleTopComponent {
    /**
     * In this method define all the preconditions that will allow that the top 
     * component work correctly
     * @return True if is compatible
     */
    public boolean isCompatible();
}
