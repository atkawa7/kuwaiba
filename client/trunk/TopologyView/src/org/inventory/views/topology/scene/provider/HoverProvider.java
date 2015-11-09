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

package org.inventory.views.topology.scene.provider;

import org.inventory.views.topology.scene.TopologyViewScene;
import org.netbeans.api.visual.action.TwoStateHoverProvider;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Widget;

/**
 * A hover provider to edit label widgets
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class HoverProvider implements TwoStateHoverProvider {

    private TopologyViewScene scene;

    public HoverProvider(TopologyViewScene scene) {
        this.scene = scene;
    }

    @Override
    public void unsetHovering(Widget widget) {
        if (widget != null) {
            widget.setBackground(scene.getLookFeel().getBackground(ObjectState.createNormal()));
            widget.setForeground(scene.getLookFeel().getForeground(ObjectState.createNormal()));
        }
    }

    @Override
    public void setHovering(Widget widget) {
        if (widget != null) {
            ObjectState state = ObjectState.createNormal().deriveSelected(true);
            widget.setBackground(scene.getLookFeel().getBackground(state));
            widget.setForeground(scene.getLookFeel().getForeground(state));
        }
    }
}


