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
package com.neotropic.kuwaiba.modules.reporting.img.rackview;

import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 * A implementation for the rack view, when a connection is selected show the
 * name of the connection
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class RackViewConnectionWidget extends ConnectionWidget {
    protected LabelWidget labelWidget;
    
    public RackViewConnectionWidget(Scene scene, RemoteObjectLight object) {
        super(scene);
        
        labelWidget = new LabelWidget(scene, object.toString());
        labelWidget.setOpaque(true);
        labelWidget.setBorder(getScene().getLookFeel().getBorder(getState()));
                
        addChild(labelWidget);
        
        setConstraint(labelWidget, LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER, 0.5f);
                
        setState(ObjectState.createNormal());
    }
    
    public LabelWidget getLabelWidget() {
        return labelWidget;
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

