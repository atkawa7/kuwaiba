/*
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
package org.inventory.views.objectview.scene;

import java.awt.Color;
import java.awt.Dimension;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.PhysicalConnectionProvider;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.layout.Layout;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Scene class used in this module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RackViewScene extends AbstractScene<LocalObject, LocalObject> {
    public static final int STANDARD_RACK_WIDTH = 300;
    public static final int RACK_UNIT_IN_PX = 20;
    private Widget rackWidget;
    private Widget infoWidget;
    private Layout verticalLayout;
    private Border elementBorder;

    public RackViewScene() {
        this.verticalLayout = LayoutFactory.createVerticalFlowLayout();
        this.elementBorder = BorderFactory.createEmptyBorder(5, 10, 0, 10);
        this.rackWidget = new Widget(this);
        this.rackWidget.setOpaque(true);
        this.rackWidget.setLayout(LayoutFactory.createAbsoluteLayout());
        this.rackWidget.setBackground(new Color(77, 77, 77));
        
        this.infoWidget = new Widget(this);
        this.infoWidget.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 10));
        
        setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.LEFT_TOP, 100));
        addChild(rackWidget);
        addChild(infoWidget);
    }   
    
    @Override
    public Widget attachNodeWidget(LocalObject node) {
        Widget widget = new Widget(this);
        widget.setPreferredSize(new Dimension(STANDARD_RACK_WIDTH, 50));
        widget.setOpaque(true);
        widget.setBorder(elementBorder);
        widget.setBackground(new Color(136, 170, 0));
        widget.setLayout(verticalLayout);
        LabelWidget txtName = new LabelWidget(this, node.toString());
        LabelWidget txtInfo  = new LabelWidget(this, "Position: "+ 
                node.getAttribute(Constants.PROPERTY_POSITION) + "U - " + 
                "Size: " + node.getAttribute(Constants.PROPERTY_RACKUNITS) + "U");
        txtName.setForeground(Color.WHITE);
        txtInfo.setForeground(Color.WHITE);
        widget.addChild(txtName);
        widget.addChild(txtInfo);
        rackWidget.addChild(widget);
        validate();
        return widget;      
    }

    @Override
    protected Widget attachEdgeWidget(LocalObject edge) {return null;}

    @Override
    protected void attachEdgeSourceAnchor(LocalObject edge, LocalObject oldSourceNode, LocalObject sourceNode) {}

    @Override
    protected void attachEdgeTargetAnchor(LocalObject edge, LocalObject oldTargetNode, LocalObject targetNode) {}
    
    @Override
    public void clear(){
        while (!getNodes().isEmpty())
            removeNode(getNodes().iterator().next());

        while (!getEdges().isEmpty())
            removeNode(getEdges().iterator().next());
        
        infoWidget.removeChildren();
        rackWidget.setPreferredSize(new Dimension(0,0));
        validate();
    }
    
    public void addInfoLabel(String infoLabel, boolean emphasis){
        LabelWidget newInfoLine = new LabelWidget(this, infoLabel);
        if (emphasis)
            newInfoLine.setForeground(Color.RED);
        infoWidget.addChild(newInfoLine);
        validate();
    }
    
    public Widget getRackWidget(){
        return rackWidget;
    }

    //Not needed
    @Override
    public byte[] getAsXML() {return null;}
    
    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
        //TODO: Render the view here instead of in the service
    }

    @Override
    public PhysicalConnectionProvider getConnectProvider() {
        return null;
    }
    
    @Override
    public Color getConnectionColor(LocalObjectLight theConnection) {
        return null;
    }

    @Override
    public boolean supportsConnections() {
        return false;
    }

    @Override
    public boolean supportsBackgrounds() {
        return false;
    }
}
