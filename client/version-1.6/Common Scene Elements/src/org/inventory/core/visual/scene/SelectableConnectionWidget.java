/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.visual.scene;

import java.awt.BasicStroke;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * A connection widget that can be selected and its wrapped object exposed via the property sheet
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class SelectableConnectionWidget extends ConnectionWidget {
    public static final int STROKE_WIDTH = 3;
    public static final int SELECTED_STROKE_WIDTH = 4;
    
    private Lookup lookup;
    protected LabelWidget labelWidget;
    
    public SelectableConnectionWidget(Scene scene, LocalObjectLight businessObject) {
        super(scene);
        setToolTipText(businessObject.toString());

        labelWidget = new LabelWidget(scene, businessObject.toString());
        labelWidget.setOpaque(true);
        labelWidget.setBorder(getScene().getLookFeel().getBorder(getState()));
        labelWidget.getActions().addAction(ActionFactory.createMoveAction());
        
        addChild(labelWidget);
        
        setConstraint(labelWidget, LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER, 0.5f);
        //It's strange, but having in the lookup just the node won't work for 
        //classes expecting the enclosed business object to also be in the lookup (unlike BeanTreeViews)
        lookup = Lookups.fixed(new ObjectNode(businessObject), businessObject);
        
        setState(ObjectState.createNormal());
        setStroke(new BasicStroke(STROKE_WIDTH));
    }
    
    public LabelWidget getLabelWidget() {
        return labelWidget;
    }
    
    @Override
    public Lookup getLookup() {
        return lookup;
    }   
}
