/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 */
package org.inventory.core.containment;

import org.inventory.core.services.api.behaviors.ConfigurationObject;
import org.openide.util.lookup.ServiceProvider;

/**
 * Containment Hierarchy Configuration Object
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service = ConfigurationObject.class)
public class HierarchyCustomizerConfigurationObject extends ConfigurationObject {
    /**
     * Property name which its value is used to enable the special containment hierarchy.
     */
    public static String PROPERTY_ENABLE_SPECIAL = "enableSpecialContainmentHierarchy";
}