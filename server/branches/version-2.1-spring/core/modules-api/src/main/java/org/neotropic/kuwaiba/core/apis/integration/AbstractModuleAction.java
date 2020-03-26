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
import java.util.Properties;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.Privilege;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * All actions in a module must extend 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractModuleAction {
    /**
     * A unique identifier for the action within the module .
     */
    protected String id;
    /**
     * The label to be used in menus or buttons.
     */
    protected String displayName;
    /**
     * A short description of what the action does, mainly to be used as tool text tip.
     */
    protected String description;
    /**
     * Icon for buttons, menu entries, widget cards, etc. SVG images are encouraged, because they can be easily rescaled.
     */
    protected byte[] icon;
    /**
     * Settings useful to renderer to display the action. Currently suggested and supported options: bold (boolean) and color (HTML hex RGB value).
     */
    protected Properties formatOptions;
    /**
     * In case this is a composed action with sub-actions.
     */
    protected List<AbstractModuleAction> childrenActions;
    /**
     * This number will be used to position the action in menus (0 is the highest priority/importance). The default value is 1000.
     */
    protected int order;
    /**
     * What is to be execute once the action is triggered.
     */
    protected ModuleActionCallback callback;
    /**
     * Reference to the metadata entity manager.
     */
    @Autowired
    protected MetadataEntityManager mem;
    /**
     * Reference to the application entity manager.
     */
    @Autowired
    protected ApplicationEntityManager aem;
    /**
     * Reference to the business entity manager.
     */
    @Autowired
    protected BusinessEntityManager bem;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getIcon() {
        return icon;
    }

    public void setIcon(byte[] icon) {
        this.icon = icon;
    }

    public Properties getFormatOptions() {
        return formatOptions;
    }

    public void setFormatOptions(Properties formatOptions) {
        this.formatOptions = formatOptions;
    }

    public List<AbstractModuleAction> getChildrenActions() {
        return childrenActions;
    }

    public void setChildrenActions(List<AbstractModuleAction> childrenActions) {
        this.childrenActions = childrenActions;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setCallback(ModuleActionCallback callback) {
        this.callback = callback;
    }

    public ModuleActionCallback getCallback() {
        return callback;
    }
    
    /**
     * Returns the privilege needed to execute the current action. This access level should be matched with the permissions granted 
     * to the user and the renderer must decide if it will gray-out the action or simply not show it.
     * @return The access level needed to execute this action. See {@link Privilege}.ACCESS_LEVEL_XXX for possible values. 
     */
    public abstract int getRequiredAccessLevel();
    
    /**
     * The callback code to be executed once the action is triggered.
     */
    public interface ModuleActionCallback {
        public void execute(ModuleActionParameter... parameters) throws ModuleActionException;
    }
}