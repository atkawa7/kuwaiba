/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.neotropic.vaadin14.component;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;

/**
 * Event fired when delete key is pressed on any cell
 *
 * @author Orlando Paz Duarte {@literal <orlando.paz@kuwaiba.org>}
 */
@DomEvent("delete-cell-selected")
public class MxGraphDeleteCellSelectedEvent extends ComponentEvent<MxGraph> {

    public MxGraphDeleteCellSelectedEvent(MxGraph source, boolean fromClient) {
        super(source, fromClient);
    }
}