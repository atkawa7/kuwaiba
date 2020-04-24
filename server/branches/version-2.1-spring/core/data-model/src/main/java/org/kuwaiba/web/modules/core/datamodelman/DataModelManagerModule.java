/**
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.core.datamodelman;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.neotropic.kuwaiba.core.apis.integration.AbstractModule;
import org.springframework.stereotype.Component;


/**
 * The definition of the Data Model Manager module
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>}
 */
@Component
public class DataModelManagerModule  extends AbstractModule<VerticalLayout> {

    @Override
    public String getName() {
        return "Data Model Manager";
    }

    @Override
    public String getDescription() {
        return "This module allows to manage the data model structure hierarchy of all objects and list types";
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
        return "ltmanager";
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
