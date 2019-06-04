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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import javax.swing.border.LineBorder;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.exceptions.InventoryException;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.visual.actions.providers.CustomSelectProvider;
import org.inventory.core.visual.scene.AbstractScene;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * Shows a custom view for SpliceBoxes
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SpliceBoxViewScene extends AbstractScene<LocalObjectLight, String> {
    /**
     * Color used enclose the ports
     */
    private static Color COLOR_LIGHT_GRAY = new Color(218, 218, 218);
    /**
     * Reference to the com stub
     */
    private CommunicationsStub com = CommunicationsStub.getInstance();
    /**
     * Dictionary with the pairs input port - Link connected to it
     */
    private TreeMap<LocalObjectLight, LocalObject> inputPorts;
    /**
     * Dictionary with the pairs output port - Link connected to it
     */
    private TreeMap<LocalObjectLight, LocalObject> outputPorts;
    /**
     * Dictionary containing the mirror ports of every input port
     */
    private HashMap<LocalObjectLight, LocalObjectLight> mirrors;
    /**
     * Container of all input port widgets
     */
    private Widget inputPortsContainer;
    /**
     * Container of all output port widgets
     */
    private Widget outputPortsContainer;
    /**
     * Custom select provider
     */
    private WidgetAction selectAction;

    public SpliceBoxViewScene() {
        this.inputPorts = new TreeMap<>();
        this.outputPorts = new TreeMap<>();
        this.mirrors = new HashMap<>();
        this.inputPortsContainer = new Widget(this);
        this.inputPortsContainer.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 2));
        this.inputPortsContainer.setPreferredLocation(new Point(100, 100));
        this.inputPortsContainer.setBorder(new LineBorder(COLOR_LIGHT_GRAY, 4));
        
        this.outputPortsContainer = new Widget(this);
        this.outputPortsContainer.setLayout(inputPortsContainer.getLayout());
        this.outputPortsContainer.setPreferredLocation(new Point(400, 100));
        this.outputPortsContainer.setBorder(inputPortsContainer.getBorder());
        
        this.edgeLayer = new LayerWidget(this);
        this.nodeLayer = new LayerWidget(this);
        
        addChild(edgeLayer);
        addChild(nodeLayer);
        nodeLayer.addChild(inputPortsContainer);
        nodeLayer.addChild(outputPortsContainer);
        
        
        selectAction = ActionFactory.createSelectAction(new CustomSelectProvider(this), true);
        
        setOpaque(true);
        setBackground(Color.WHITE);
    }
    
    @Override
    public byte[] getAsXML() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException { }

    @Override
    public void render(LocalObjectLight spliceBox) {
        List<LocalObjectLight> portsInSpliceBox = com.getChildrenOfClassLightRecursive(spliceBox.getId(), spliceBox.getClassName(), Constants.CLASS_GENERICPORT);
        if (portsInSpliceBox != null) {
            try {
                //First we retrieve all the necessary information: The ports in the splice box, the links connected to them and the mirror relationships 
                //between input and output ports
                for (LocalObjectLight port : portsInSpliceBox) {
                    if (!(port.getName().toLowerCase().startsWith("in") || port.getName().toLowerCase().startsWith("out")))
                        throw new InventoryException(I18N.gm("spliceboxview_wrong_port_naming"));

                    LocalObject linkConnectedToPort;

                    linkConnectedToPort = com.getLinkConnectedToPort(port.getClassName(), port.getId());

                    if (port.getName().toLowerCase().startsWith("in")) { //NOI18N
                        List<LocalObjectLight> mirrorPort = com.getSpecialAttribute(port.getClassName(), port.getId(), "mirror"); //NOI18N
                        this.mirrors.put(port, mirrorPort.isEmpty() ? null : mirrorPort.get(0));
                        this.inputPorts.put(port, linkConnectedToPort);
                    }
                    else
                        this.outputPorts.put(port, linkConnectedToPort);
                }
                
                //Now we do the actual render
                
                //First the inputs
                for (LocalObjectLight inputPort : inputPorts.keySet()) {
                    PortWidget portWidget = (PortWidget)addNode(inputPort);
                    portWidget.setConnectedLink(inputPorts.get(inputPort));
                }
                
                //Then the outputs
                for (LocalObjectLight outputPort : outputPorts.keySet()) {
                    PortWidget portWidget = (PortWidget)addNode(outputPort);
                    portWidget.setConnectedLink(outputPorts.get(outputPort));
                }
                
                //Now we connect the mirrors
                for (LocalObjectLight inputPort : mirrors.keySet()) {
                    if (mirrors.get(inputPort) != null) {
                        String connectionId = inputPort.getId() + " - " + mirrors.get(inputPort).getId();
                        addEdge(connectionId);
                        setEdgeSource(connectionId, inputPort);
                        setEdgeTarget(connectionId, mirrors.get(inputPort));
                    }
                }
                               
                validate();
                repaint();
            } catch (InventoryException ex) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, ex.getMessage());
            }
        } else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
    }

    @Override
    public ConnectProvider getConnectProvider() {
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

    @Override
    protected Widget attachNodeWidget(LocalObjectLight port) {
        PortWidget newNode;
        if (port.getName().toLowerCase().startsWith("in")) { //NOI18N
            newNode = new PortWidget(this, port, inputPorts.get(port), PortWidget.ALIGNMENT.LEFT);
            inputPortsContainer.addChild(newNode);
        } else {
            newNode = new PortWidget(this, port, outputPorts.get(port), PortWidget.ALIGNMENT.RIGHT);
            outputPortsContainer.addChild(newNode);
        }
        newNode.getActions().addAction(selectAction);
        validate();
        return newNode;
    }

    @Override
    protected Widget attachEdgeWidget(String e) {
        ConnectionWidget newEdge = new ConnectionWidget(this);
        newEdge.setStroke(new BasicStroke(4));
        edgeLayer.addChild(newEdge);
        validate();
        return newEdge;
    }  
}
