/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.neotropic.kuwaiba.modules.core.navigation;

import javax.annotation.PostConstruct;
import org.neotropic.kuwaiba.core.apis.integration.modules.AbstractModule;
import org.neotropic.kuwaiba.core.apis.integration.modules.ModuleRegistry;
import org.neotropic.kuwaiba.core.apis.integration.modules.actions.ActionRegistry;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.modules.core.navigation.actions.DeleteBusinessObjectVisualAction;
import org.neotropic.kuwaiba.modules.core.navigation.actions.NewBusinessObjectVisualActionToo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * The definition of the Navigation module
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
@Component
public class NavigationModule extends AbstractModule {
    /**
     * The module id.
     */
    public static final String MODULE_ID = "navman"; 
    /**
     * translation service
     */
    @Autowired
    private TranslationService ts;
    /**
     * Reference to the action that copy a Business Object.
     */
    //@Autowired
    //private CopyVisualAction;
    /**
     * Reference to the action that cut a Business Object.
     */
    //@Autowired
    //private CutVisualAction;
    /**
     * Reference to the action that paste a Business Object.
     */
    //@Autowired
    //private PasteVisualAction;
    /**
     * Reference to the action that opens a tree form the selected Business Object.
     */
    //@Autowired
    //private OpenExplorerVisualAction openExplorerVisualAction;
    /**
     * Reference to the action that creates a new Business Object.
     */
    @Autowired
    private NewBusinessObjectVisualActionToo actNewObj;
    /**
     * Reference to the action that deletes a Business Object.
     */
    @Autowired
    private DeleteBusinessObjectVisualAction actDeleteObj;
    /**
     * Reference to the action registry.
     */
    @Autowired
    private ActionRegistry actionRegistry;
    /**
     * Reference to the module registry.
     */
    @Autowired
    private ModuleRegistry moduleRegistry;
    
    @PostConstruct
    public void init() {
        // Register all actions provided by this module
        this.actionRegistry.registerAction(MODULE_ID, actNewObj);
        this.actionRegistry.registerAction(MODULE_ID, actDeleteObj);
        // Now the module itself
        this.moduleRegistry.registerModule(this);
    }
   
    @Override
    public String getName() {
        return ts.getTranslatedString("module.navigation.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.navigation.description");
    }

    @Override
    public String getVersion() {
        return "2.1";
    }

    @Override
    public String getVendor() {
        return "Neotropic SAS <contact@neotropic.co>";
    }
    
     @Override
    public String getId() {
        return MODULE_ID;
    }

    @Override
    public ModuleType getModuleType() {
        return ModuleType.TYPE_OPEN_SOURCE;        
    }

    @Override
    public int getCategory() {
        return CATEGORY_NAVIGATION;
    }
}
