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
package org.inventory.views.connections;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.SelectableNodeWidget;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Represents a node in the Graphical Physical Path view
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class NestedDeviceWidget extends SelectableNodeWidget {
    private static Border emptyBorder = BorderFactory.createEmptyBorder(5, 5 ,5 , 5);
    private Color backgorundColor;
    private LabelWidget labelWidget;
    private Widget childrenWidget;
    
    public NestedDeviceWidget(AbstractScene scene, LocalObjectLight object) {
        super(scene, object);
        setOpaque(true);
        setLayout(LayoutFactory.createVerticalFlowLayout());
        this.labelWidget = new LabelWidget(scene, object.toString());
        this.labelWidget.setBorder(emptyBorder);
        addChild(labelWidget);
        this.childrenWidget = new Widget(scene);
        this.childrenWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 1));
        this.childrenWidget.setBorder(emptyBorder);
        addChild(childrenWidget);
    }
    
    public NestedDeviceWidget(AbstractScene scene, LocalObjectLight object, Color originalColor) {
        this (scene, object);
        this.backgorundColor = originalColor;
        setBackground(originalColor);
    }
    
    public void addBox(Widget child){
        childrenWidget.addChild(child);
    }
    
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        if (state.isSelected()) {
            setBackground(new Color(255, 255, 255, 230));
            labelWidget.setForeground(Color.BLACK);
        }
        if (previousState.isSelected()) {
            setBackground(backgorundColor == null ? Color.WHITE : backgorundColor);
            labelWidget.setForeground(Color.WHITE);
        }
    }

    public void setBackgroundColor(Color originalColor) {
        this.backgorundColor = originalColor;
        setBackground(originalColor);
    }
}
