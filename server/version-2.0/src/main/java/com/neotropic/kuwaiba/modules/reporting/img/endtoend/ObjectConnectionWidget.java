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
package com.neotropic.kuwaiba.modules.reporting.img.endtoend;

import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;

/**
 * A connection widget representing a link or a container
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>} modified by Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ObjectConnectionWidget extends ConnectionWidget {
     /**
     * Is this widget in high-contrast mode?
     */
    private boolean highContrast;
    /**
     * style for the line, dot line
     */
    public static final int DOT_LINE = 2;
    /**
     * line dot style
     */
    public static final int LINE = 1;
    /**
     * Label widget
     */
    protected LabelWidget labelWidget;
        
    public ObjectConnectionWidget(Scene scene, RemoteObjectLight businessObject, int lineStyle) {
        super(scene);
        if(lineStyle == DOT_LINE)
            this.setStroke(new DotLineStroke(1));
        
        setToolTipText(businessObject.toString());

        labelWidget = new LabelWidget(scene, businessObject.toString());
        labelWidget.setOpaque(false);
        labelWidget.setBorder(getScene().getLookFeel().getBorder(getState()));
        
        addChild(labelWidget);
        
        setConstraint(labelWidget, LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER, 0.5f);
        //It's strange, but having in the lookup just the node won't work for 
        //classes expecting the enclosed business object to also be in the lookup (unlike BeanTreeViews)
        setState(ObjectState.createNormal());
        
        highContrast = false;
    }
    
    public LabelWidget getLabelWidget() {
        return labelWidget;
    }
    
    public void setHighContrast(boolean highContrast) {
        this.highContrast = highContrast;
        this.labelWidget.setOpaque(highContrast);
        notifyStateChanged(getState(), getState());
    }
}
