/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.visual.scene;

import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectNode;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Root to all widgets representing and object node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class AbstractNodeWidget extends Widget implements SelectableWidget {
    /**
     * Default widget size
     */
    public static final Dimension DEFAULT_DIMENSION = new Dimension(10, 10);
    /**
     * Widget's lookup
     */
    private Lookup lookup;
    /**
     * Object node. The wrapped object will be referenced here
     */
    private ObjectNode node;
    /**
     * The label
     */
    private LabelWidget labelWidget;
    /**
     * The generic icon (a square)
     */
    private Widget squareWidget;
    
    /**
     * Default constructor
     * @param scene Scene this widget belongs to
     * @param object object represented by this widget
     */
    public AbstractNodeWidget(Scene scene, LocalObjectLight object) {
        super(scene);
        this.node = new ObjectNode(object);
        this.squareWidget = new Widget(scene);
        this.labelWidget = new LabelWidget(scene);
        
        this.labelWidget.setLabel(object.toString());
        
        this.squareWidget.setPreferredSize(DEFAULT_DIMENSION);
        this.squareWidget.setBackground(Color.ORANGE);
        this.squareWidget.setOpaque(true);
        
        //Centers the text, and makes the widgets to stack one onto another
        setLayout(LayoutFactory.createVerticalFlowLayout (LayoutFactory.SerialAlignment.CENTER, 5));
        
        addChild(squareWidget);
        addChild(labelWidget);
        
        setToolTipText(object.toString());
        this.lookup = Lookups.singleton(object);
        createActions(AbstractScene.ACTION_SELECT);
        createActions(AbstractScene.ACTION_CONNECT);
    }

    /**
     * Convenience method to get the wrapped object
     * @return The wrapped object
     */
    public LocalObjectLight getObject() {
        return node.getObject();
    }

    /**
     * Convenience method to set the wrapped object
     * @param object the new object
     */
    public void setObject(LocalObjectLight object) {
        this.node = new ObjectNode(object);
    }
    
    public void showLabel(boolean shouldShow) {
        labelWidget.setVisible(shouldShow);
    }
    
    @Override
    public Lookup getLookup(){
        return lookup;
    }
    
    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        if (state.isSelected())
            labelWidget.setBorder(BorderFactory.createLineBorder(Color.RED));
        else
            labelWidget.setBorder(BorderFactory.createEmptyBorder());
    }

    @Override
    public ObjectNode getNode() {
        return node;
    }   
}