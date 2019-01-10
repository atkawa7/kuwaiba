/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

package org.kuwaiba.web.modules.servmanager.views;

import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 * A connection widget representing a link or a container
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ObjectConnectionWidget extends ConnectionWidget {
    /**
     * Is this widget in high-contrast mode?
     */
    private boolean highContrast;
    protected LabelWidget labelWidget;
    
    public ObjectConnectionWidget(Scene scene, RemoteObjectLight object) {
        super(scene);
        labelWidget = new LabelWidget(scene, object.toString());
        labelWidget.setOpaque(true);
        labelWidget.setBorder(getScene().getLookFeel().getBorder(getState()));
        labelWidget.getActions().addAction(ActionFactory.createMoveAction());
        
        addChild(labelWidget);
        
        setConstraint(labelWidget, LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER, 0.5f);
        //It's strange, but having in the lookup just the node won't work for 
        //classes expecting the enclosed business object to also be in the lookup (unlike BeanTreeViews)
        highContrast = false;      
    }

    public ObjectConnectionWidget(Scene scene) {
        super(scene);
    }
    
    
    
    public void setHighContrast(boolean highContrast) {
        this.highContrast = highContrast;
        this.labelWidget.setOpaque(highContrast);
        notifyStateChanged(getState(), getState());
    }

    public LabelWidget getLabelWidget() {
        return labelWidget;
    }

    public void setLabelWidget(LabelWidget labelWidget) {
        this.labelWidget = labelWidget;
    }
    
    

}
