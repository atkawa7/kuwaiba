/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
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

package org.neotropic.kuwaiba.core.apis.integration;

import java.util.List;

/**
 * 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ModuleAction {
    /**
     * A unique identifier for the action within the module .
     */
    private String id;
    /**
     * The label to be used in menus or buttons.
     */
    private String displayName;
    /**
     * A short description of what the action does, mainly to be used as tool text tip.
     */
    private String description;
    /**
     * Icon for buttons, menu entries, widget cards, etc. SVG images are encouraged, because they can be easily rescaled.
     */
    private byte[] icon;
    /**
     * In case this is a composed action with sub-actions.
     */
    private List<ModuleAction> childrenActions;
    /**
     * This number will be used to position the action in menus (0 is the highest priority/importance).
     */
    private int order;
}
