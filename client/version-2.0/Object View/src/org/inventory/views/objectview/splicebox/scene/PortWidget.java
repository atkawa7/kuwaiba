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
package org.inventory.views.objectview.splicebox.scene;

import java.awt.Color;
import java.awt.Dimension;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.scene.SelectableNodeWidget;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * A widget representing a port in a SpliceBoxView. It displays the name of the port, plus the name of the connected link
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class PortWidget extends SelectableNodeWidget {
    /**
     * The default height of the box containing the labels
     */
    public static int DEFAULT_WIDGET_HEIGHT = 40;
    /**
     * The default height of the box containing the port names
     */
    public static int DEFAULT_WIDGET_WIDTH_1 = 20;
    /**
     * The default height of the box containing the link names
     */
    public static int DEFAULT_WIDGET_WIDTH_2 = 200;
    
    /**
     * The link connected to the port
     */
    private LocalObject connectedLink;
    /**
     * Label that displays the port name to the left side of the widget
     */
    private Widget linkColorWidget;
    /**
     * Label that displays the port name to the left side of the widget
     */
    private LabelWidget portNameWidget;
    
    /**
     * Default constructor
     * @param scene Scene the widget belongs to
     * @param port The business object representing the port
     * @param connection The business object representing the link connected to the port. A null value means that the port has nothing connected to it
     */
    public PortWidget(Scene scene, LocalObjectLight port, LocalObject connectedLink, ALIGNMENT alignment) {
        super(scene, port);
        this.connectedLink = connectedLink;
        this.linkColorWidget = new Widget(scene);
        this.portNameWidget = new LabelWidget(scene, String.format("%s [%s]", 
                port.getName(), connectedLink == null ? I18N.gm("not_connected") :  //NOI18N
                        (connectedLink.getName().length() > 15 ? connectedLink.getName().substring(0, 15) + "...": connectedLink.getName())));  //NOI18N
        
        this.portNameWidget.setOpaque(true);
        this.linkColorWidget.setOpaque(true);
        
        this.portNameWidget.setPreferredSize(new Dimension(DEFAULT_WIDGET_WIDTH_2, DEFAULT_WIDGET_HEIGHT));
        this.linkColorWidget.setPreferredSize(new Dimension(DEFAULT_WIDGET_WIDTH_1, DEFAULT_WIDGET_HEIGHT));
        
        if (alignment.equals(ALIGNMENT.RIGHT)) {
            this.addChild(this.linkColorWidget);
            this.addChild(this.portNameWidget);
        } else {
            this.addChild(this.portNameWidget);
            this.addChild(this.linkColorWidget);
        }
        
        setLayout(LayoutFactory.createHorizontalFlowLayout());
        
    }

    public LocalObject getConnectedLink() {
        return connectedLink;
    }

    public void setConnectedLink(LocalObject connectedLink) {
        this.connectedLink = connectedLink;
        if (connectedLink == null) //Nothing connected
            linkColorWidget.setBackground(Color.DARK_GRAY);
        else {
            Integer rawColor = (Integer)connectedLink.getAttribute(Constants.PROPERTY_COLOR);
            if (rawColor != null)
                linkColorWidget.setBackground(new Color(rawColor));
            else
                linkColorWidget.setBackground(Color.BLACK);
        }
    }

    public Widget getPortNameWidget() {
        return linkColorWidget;
    }

    public void setPortNameWidget(Widget linkColorWidget) {
        this.linkColorWidget = linkColorWidget;
    }

    public LabelWidget getLinkNameWidget() {
        return portNameWidget;
    }

    public void setLinkNameWidget(LabelWidget portNameWidget) {
        this.portNameWidget = portNameWidget;
    }
    
    /**
     * Implements the widget-state specific look of the widget.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    public void notifyStateChanged (ObjectState previousState, ObjectState state) {
        setBorder(getScene().getLookFeel().getBorder (state));
    }   
    
    /**
     * The port and link name should be at the right side or the left side of the widget
     */
    public enum ALIGNMENT {
        RIGHT,
        LEFT
    }
}
