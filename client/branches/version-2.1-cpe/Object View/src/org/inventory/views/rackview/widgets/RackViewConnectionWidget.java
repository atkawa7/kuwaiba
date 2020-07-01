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

import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;

/**
 * A implementation for the rack view, when a connection is selected show the
 * name of the connection
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RackViewConnectionWidget extends ObjectConnectionWidget {
    
    public RackViewConnectionWidget(Scene scene, LocalObjectLight object) {
        super(scene, object, ObjectConnectionWidget.LINE);
    }
    
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        super.notifyStateChanged(previousState, state);
        if (state.isSelected())                       
            getLabelWidget().setVisible(true);
        else if (previousState.isSelected())
            getLabelWidget().setVisible(false);
    }
}
