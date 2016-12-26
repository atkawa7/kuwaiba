/**
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
package org.kuwaiba.apis.web.gui.modules;


/**
 * Implementors are embedded into a TopComponent at some point. The idea is that you can use the global variables defined at TopComponent level (usually beans and session variables that can't be injected) at a lower level.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface EmbeddableComponent {
    /**
     * Gets the reference to the nearest parent TopComponent.
     * @return The nearest top component in the component containment hierarchy.
     */
    public TopComponent getTopComponent();
}
