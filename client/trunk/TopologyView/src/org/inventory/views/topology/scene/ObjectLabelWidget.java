/**
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.views.topology.scene;

import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 * A connection widget representing a link or a container
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class ObjectLabelWidget extends LabelWidget{

    private String labelText;

    public ObjectLabelWidget(Scene scene, String label) {
        super(scene, label);
        this.setOpaque (true);
        this.setToolTipText ("Double-click to edit text");
        this.labelText =  label;
    }

    public String getLabelText() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText = labelText;
    }

}
