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
package org.inventory.core.visual.scene;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.inventory.communications.core.LocalObjectLight;
import org.netbeans.api.visual.laf.LookFeel;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;

/**
 * An empty widget that do not need to be selected and it represents the 
 * unconnected side of a link
 * @author Adrian Martinez Molina {@literal <adrian.martinez@kuwaiba.org>}
 */
public class EmptyNodeWidget extends Widget{
    /**
     * random id
     */
    private LocalObjectLight emptyObj;
    /**
     * Default widget size
     */
    public static final Dimension DEFAULT_DIMENSION = new Dimension(20, 20);
    /**
     * The label
     */
    private LabelWidget labelWidget;
    /**
     * An icon or a colored square
     */
    private Widget nodeWidget;
    /**
     * Widgets in high-contrast mode are opaque, with a dark background and light-colored letter
     */
    private boolean highContrast;
    /**
     * Extra info
     */
    private String[] extraInfo;
    
    private final Image icon = ImageUtilities.loadImage("org/inventory/core/visual/res/empty.png");
    
   public EmptyNodeWidget(Scene scene, LocalObjectLight emptyObj, String[] extraInfo) {
        super(scene);
        LookFeel lookFeel = scene.getLookFeel();
        
        this.extraInfo = extraInfo;
        this.emptyObj = emptyObj;
        this.nodeWidget = new Widget(scene);
        this.nodeWidget.setPreferredSize(DEFAULT_DIMENSION);
        //this.nodeWidget.setBackground(new Color(193, 193, 215));
        this.nodeWidget = new ImageWidget(scene, icon);
        this.nodeWidget.setOpaque(true);
        
        this.labelWidget = new LabelWidget(scene, emptyObj.getName());
        this.labelWidget.setBorder(getScene().getLookFeel().getBorder(getState()));
        this.labelWidget.setOpaque(true);
        
        //Centers the text, and makes the widgets to stack one onto another
        setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 1 - lookFeel.getMargin()));
        addChild(nodeWidget);
        addChild(labelWidget);
                
        createActions(AbstractScene.ACTION_SELECT);
                
        setState (ObjectState.createNormal());
    }

    public LocalObjectLight getEmptyObj() {
        return emptyObj;
    }
    
    public LabelWidget getLabelWidget() {
        return labelWidget;
    }
    
    public Widget getNodeWidget() {
        return nodeWidget;
    }

    public boolean isHighContrast() {
        return highContrast;
    }

    public void setHighContrast(boolean highContrast) {
        this.highContrast = highContrast;
        notifyStateChanged(getState(), getState());
    }
    
    public String[] getExtraInfo() {
        return extraInfo;
    }
        
    public void showExtraInfo(String frameTittle, String instructions){
        final JFrame frame = new JFrame(frameTittle);
        frame.setLayout(new BorderLayout());
        frame.setSize(400, 350);
        frame.setLocationRelativeTo(null);
        JLabel lblInstructions = new JLabel(instructions);
        lblInstructions.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        frame.add(lblInstructions, BorderLayout.NORTH);
        frame.add(new JScrollPane(new JList<>(extraInfo)), BorderLayout.CENTER);
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener((ActionEvent e) -> {
            frame.dispose();
        });
        JPanel pnlButtons = new JPanel();
        pnlButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
        pnlButtons.add(btnClose);
        frame.add(pnlButtons, BorderLayout.SOUTH);
        frame.setVisible(true);
    }
    
    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        if (!highContrast) {
            labelWidget.setForeground (getScene().getLookFeel().getForeground (state));
            labelWidget.setBackground(getScene().getLookFeel().getBackground(state));
            labelWidget.setBorder(getScene().getLookFeel().getBorder (state));
        } else {
            labelWidget.setForeground (HighContrastLookAndFeel.getInstance().getForeground (state));
            labelWidget.setBackground(HighContrastLookAndFeel.getInstance().getBackground(state));
            labelWidget.setBorder(HighContrastLookAndFeel.getInstance().getBorder (state));
        }
    }   
}
