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
package org.inventory.views.rackinsideviewz;

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
 * Represents a device and the devices inside, to be shown in the rack inside view.
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class NestedDeviceWidget extends SelectableNodeWidget {
    private static Border emptyBorder = BorderFactory.createEmptyBorder(5, 5 ,5 , 5);
    private Color backgorundColor;
    private LabelWidget labelWidgetObjectName;
    private LabelWidget labelWidgetObjectClassName;
    private Widget childrenWidget;
    
    public NestedDeviceWidget(AbstractScene scene, LocalObjectLight object, boolean isPort) {
        super(scene, object);
        setOpaque(true);
        setLayout(LayoutFactory.createVerticalFlowLayout());
        this.labelWidgetObjectName = new LabelWidget(scene);
        this.labelWidgetObjectClassName = new LabelWidget(scene);
        this.childrenWidget = new Widget(scene);
        
        labelWidgetObjectName.setForeground(Color.WHITE);
        labelWidgetObjectClassName.setForeground(Color.WHITE);
        
        addChild(labelWidgetObjectName);
        
        if(isPort){
            //if the object is an instance of a port the label should be display veticaly
            this.labelWidgetObjectName.setOrientation(LabelWidget.Orientation.ROTATE_90);
            this.labelWidgetObjectName.setBorder(BorderFactory.createEmptyBorder(2, 0 ,2 , 0));
            this.labelWidgetObjectName.setLabel("["+object.getClassName().substring(0,1)+"]");
            this.childrenWidget.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        }
        else{
            this.labelWidgetObjectName.setBorder(BorderFactory.createEmptyBorder(0, 5 ,0 , 5));
            this.labelWidgetObjectClassName.setBorder(BorderFactory.createEmptyBorder(0, 5 ,0 , 5));
            this.labelWidgetObjectName.setLabel(object.getName());
            this.labelWidgetObjectClassName.setLabel("["+object.getClassName()+"]");
            this.childrenWidget.setBorder(emptyBorder);
            addChild(labelWidgetObjectClassName);
        }
        this.childrenWidget.setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 2));
        
        addChild(childrenWidget);
        
    }
    
    public NestedDeviceWidget(AbstractScene scene, LocalObjectLight object, Color originalColor, boolean  port) {
        this (scene, object, port);
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
            labelWidgetObjectName.setForeground(Color.ORANGE);
            labelWidgetObjectClassName.setForeground(Color.ORANGE);
        }
        if (previousState.isSelected()) {
            setBackground(backgorundColor == null ? Color.WHITE : backgorundColor);
            labelWidgetObjectName.setForeground(Color.WHITE);
            labelWidgetObjectClassName.setForeground(Color.WHITE);
        }
    }

    public void setBackgroundColor(Color originalColor) {
        this.backgorundColor = originalColor;
        setBackground(originalColor);
    }
}
