/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.inventory.views.rackview.widgets;

import org.inventory.views.rackview.scene.RackViewScene;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.scene.SelectableNodeWidget;

/**
 * Represents device that can be selected, like a racks, routers, ports, ...
 * in a rack view scene
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class SelectableRackViewWidget extends SelectableNodeWidget {

    public SelectableRackViewWidget(RackViewScene scene, LocalObjectLight businessObject) {
        super(scene, businessObject);
    }
    
    public RackViewScene getRackViewScene() {
        return (RackViewScene) getScene();
    }
}
