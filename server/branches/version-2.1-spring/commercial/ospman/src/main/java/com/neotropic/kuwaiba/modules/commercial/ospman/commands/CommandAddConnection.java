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
package com.neotropic.kuwaiba.modules.commercial.ospman.commands;

import com.neotropic.kuwaiba.modules.commercial.ospman.GeoCoordinate;
import com.vaadin.flow.server.Command;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Command that contains necessary information to create a new connection
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class CommandAddConnection implements Command {
    /**
     * Source of a new connection
     */
    private BusinessObjectLight source;
    /**
     * Target of a new connection
     */
    private BusinessObjectLight target;
    /**
     * The path of a new connection
     */
    private List<GeoCoordinate> path;
    
    public BusinessObjectLight getSource() {
        return source;
    }
    
    public void setSource(BusinessObjectLight source) {
        this.source = source;
    }
    
    public BusinessObjectLight getTarget() {
        return target;
    }
    
    public void setTarget(BusinessObjectLight target) {
        this.target = target;
    }
    
    public List<GeoCoordinate> getPath() {
        return path;
    }
    
    public void setPath(List<GeoCoordinate> path) {
        this.path = path;
    }
}
