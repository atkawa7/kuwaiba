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

import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Command to add a marker
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public abstract class CommandAddMarker implements Command {
    private BusinessObjectLight businessObject;
    private double lat;
    private double lng;
    
    public BusinessObjectLight getBussinesObject() {
        return businessObject;
    }

    public void setBussinesObject(BusinessObjectLight businessObject) {
        this.businessObject = businessObject;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
