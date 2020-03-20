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

/**
 * A single entry in an application menu. This class in renderer-independent to allow different types of visualizations.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class MenuEntry {
    /**
     * Display name. 
     */
    private String label;
    /**
     * 16x16 icon.
     */
    private byte[] icon;
    /**
     * RGB-coded color (must include the # symbol). 
     */
    private String color;
    /**
     * Should the label be displayed bold.
     */
    private boolean bold;
    /**
     * Where the menu option should be placed.
     */
    private MENU_CATEGORY category;
    
    /**
     * This enumeration has the possible categories (sub-sections) in the application menu. 
     */
    public enum MENU_CATEGORY {
        /* Administrative actions. */
        ADMIN, 
        /* Common core modules. */
        COMMON, 
        /* Advanced modules. */
        ADVANCED, 
        /* Additional tools. */
        TOOLS, 
        /* Session options (logout, switch, lock, etc). */
        SESSION
    }
}
