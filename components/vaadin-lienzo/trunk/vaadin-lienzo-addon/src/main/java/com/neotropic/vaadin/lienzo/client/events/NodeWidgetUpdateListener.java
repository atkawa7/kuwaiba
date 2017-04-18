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
package com.neotropic.vaadin.lienzo.client.events;

import com.neotropic.vaadin.lienzo.client.core.shape.SrvNodeWidget;
import java.io.Serializable;

/**
 * Interface for listening Node Widget Update events initiated by the user
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
public interface NodeWidgetUpdateListener extends Serializable {
    /**
     * Handle a Node Widget Update event
     * 
     * @param clntNode Node Widget Updated
     */
    void nodeWidgetUpdated(SrvNodeWidget clntNode);
}
