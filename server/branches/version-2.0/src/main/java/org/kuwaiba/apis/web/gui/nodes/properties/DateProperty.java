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
package org.kuwaiba.apis.web.gui.nodes.properties;

import com.vaadin.shared.ui.datefield.Resolution;
import com.vaadin.ui.DateField;
import java.util.Date;

/**
 * A field to represent date values in a property sheet
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class DateProperty extends DateField {

    public DateProperty(Date date) {
        super();
        this.setResolution(Resolution.MINUTE);
        this.setValue(date);
    }
    
}
