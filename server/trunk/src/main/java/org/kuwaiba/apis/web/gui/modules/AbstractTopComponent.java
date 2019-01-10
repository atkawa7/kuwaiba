/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

import com.vaadin.navigator.View;
import com.vaadin.ui.VerticalLayout;

/**
 * The superclass of all components to be embedded in a Kuwaiba module. 
 * Note that you don't need to inherit from this class if you don't plan to use persistence
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractTopComponent extends VerticalLayout implements View {
    /**
     * Registers all relevant components in the global event bus
     */
    public abstract void registerComponents();
    /**
     * Registers all relevant components from the global event bus
     */
    public abstract void unregisterComponents();
       
}
