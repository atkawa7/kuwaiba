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
package org.kuwaiba.apis.web.gui.modules;

import com.google.common.eventbus.EventBus;
import com.vaadin.server.Resource;
import com.vaadin.ui.Component;

/**
 * The root class of all the pluggable modules (like Navigation Tree, Physical View, etc)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class AbstractModule {
    /**
     * Indicates the module is open-sourced and developed by the Kuwaiba project staff.
     */
    public static int MODULE_TYPE_FREE_CORE = 1;
    /**
     * Indicates the module is open-sourced and developed by a third party developer.
     */
    public static int MODULE_TYPE_FREE_THIRDPARTY = 2;
    /**
     * Indicates the module is commercial.
     */
    public static int MODULE_TYPE_COMMERCIAL = 3;
    /**
     * The number of opened modules (by the same user)
     */
    protected int instanceCount;
    /**
     * The event bus used to exchange 
     */
    protected EventBus eventBus;
    /**
     * The icon used in buttons and menus. Preferably use a 24x24 icon.
     */
    protected Resource icon;

    /**
     * Use this constructor if your module is stand-alone and does not need to send messages to other modules 
     * or publish objects.
     */
    public AbstractModule() {
        instanceCount = 0;
    }

    /**
     * Use this constructor if the module will need to exchange messages with other modules.
     * @param eventBus The eventBus used to exchange messages with other modules
     */
    public AbstractModule(EventBus eventBus) {
        instanceCount = 0;
        this.eventBus = eventBus;
    }
    
    
    
    /**
     * Gets the module's name. Must be unique, otherwise, the system will only take last one loaded at application's startup
     * @return The module's name
     */
    public abstract String getName();
    /**
     * Gets the module description
     * @return he module's description
     */
    public abstract String getDescription();
    /**
     * Gets the module's version
     * @return The module's version
     */
    public abstract String getVersion();
    /**
     * Gets the module's vendor
     * @return The module's vendor
     */
    public abstract String getVendor();
    /**
     * Returns what type of module (in terms of licensing and ownership)
     * @return An integer indicating the type. See MOUDULE_TYPE_XXX variables for possible values
     */
    public abstract int getType();
    /**
     * Gets the icon used in menus and buttons.
     * @return The icon.
     */
    public Resource getIcon() {
        return this.icon;
    }
    /**
     * Gets an instance of and/or initialize the component that displays the information of the module. The implementor
     * is responsible for checking how many open views are allowed for a particular module.
     * @return The component that will be docked into the windows system.
     */
    public abstract Component open();
    /**
     * What to do on closing
     */
    public abstract void close();
}
