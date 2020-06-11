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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import com.vaadin.flow.component.Component;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;

/**
 * Outside plant info window container
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface OspInfoWindowContainer {
    /**
     * Gets the info window.
     * @return Container info window
     */
    Component getInfoWindow();
    /**
     * @param content Info Window content
     */
    void setInfoWindowContent(Component content);
    /**
     * @param position Position of the Info Window
     */
    void setInfoWindowPosition(GeoCoordinate position);
    /**
     * Opens the Info Window
     */
    void openInfoWindow();
    /**
     * Opens the Info window to the given a business object
     * @param businessObject The business object of an OSP Node
     */
    void openInfoWindow(BusinessObjectLight businessObject);
    /**
     * Closes the Info Window
     */
    void closeInfoWindow();
}
