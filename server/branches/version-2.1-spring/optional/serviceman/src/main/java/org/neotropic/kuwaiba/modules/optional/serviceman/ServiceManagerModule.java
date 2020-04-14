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

package org.neotropic.kuwaiba.modules.optional.serviceman;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.apis.integration.AbstractModule;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Module definition.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
@Component
public class ServiceManagerModule extends AbstractModule<VerticalLayout> {
    @Autowired
    private TranslationService ts;
    
    @Override
    public String getId() {
        return "serviceman";
    }
    
    @Override
    public String getName() {
        return ts.getTranslatedString("module.serviceman.name");
    }

    @Override
    public String getDescription() {
        return ts.getTranslatedString("module.serviceman.description");
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
    public ModuleType getModuleType() {
        return ModuleType.TYPE_OPEN_SOURCE;
    }
    
    @Override
    public VerticalLayout getPowerUserWebComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public VerticalLayout getSimpleUserWebComponent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
