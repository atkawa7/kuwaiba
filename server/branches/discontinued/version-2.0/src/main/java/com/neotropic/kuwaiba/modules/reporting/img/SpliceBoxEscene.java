/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.neotropic.kuwaiba.modules.reporting.img;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import javax.swing.border.LineBorder;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.exceptions.ServerSideException;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObject;
import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.netbeans.api.visual.anchor.AnchorFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.Exceptions;

/**
 *
 * @author adrian
 */
public class SpliceBoxEscene extends GraphScene<RemoteObjectLight, String>{

    public static final String PATH = "../docroot/imgs/";
    
   /**
     * Color used enclose the ports
     */
    private static Color COLOR_LIGHT_GRAY = new Color(218, 218, 218);
   
    /**
     * Dictionary with the pairs input port - Link connected to it
     */
    private TreeMap<RemoteObjectLight, RemoteObject> inputPorts;
    /**
     * Dictionary with the pairs output port - Link connected to it
     */
    private TreeMap<RemoteObjectLight, RemoteObject> outputPorts;
    /**
     * Dictionary containing the mirror ports of every input port
     */
    private HashMap<RemoteObjectLight, RemoteObjectLight> mirrors;
    /**
     * Container of all input port widgets
     */
    private Widget inputPortsContainer;
    /**
     * Container of all output port widgets
     */
    private Widget outputPortsContainer;
    /**
     * Used to hold the nodes
     */
    protected LayerWidget nodeLayer;
    /**
     * Used to hold the connections
     */
    protected LayerWidget edgeLayer;
    private WebserviceBeanLocal wsBean;
    private String sessionId;
    private String address;
    private String image;

    public SpliceBoxEscene() {
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
        
        setOpaque(true);
        setBackground(Color.WHITE);
    }

    public SpliceBoxEscene(WebserviceBeanLocal wsBean, String sessionId, String address) {
        this();
        this.wsBean = wsBean;
        this.sessionId = sessionId;
        this.address = address;
    }
    
    public void render(RemoteObjectLight spliceBox){
        try {
            List<RemoteObjectLight> portsInSpliceBox = wsBean.getChildrenOfClassLightRecursive(spliceBox.getOid(), spliceBox.getClassName(), "GenericPort", -1,  address, sessionId);
            if (portsInSpliceBox != null) {
                try {
                    //First we retrieve all the necessary information: The ports in the splice box, the links connected to them and the mirror relationships
                    //between input and output ports
                    for (RemoteObjectLight port : portsInSpliceBox) {
                        if (!(port.getName().toLowerCase().startsWith("in") || port.getName().toLowerCase().startsWith("out")))
                            throw new Exception("spliceboxview_wrong_port_naming");
                        
                        RemoteObject linkConnectedToPort;
                        
                        linkConnectedToPort = wsBean.getLinkConnectedToPort(port.getClassName(), port.getOid(), address, sessionId);
                        
                        if (port.getName().toLowerCase().startsWith("in")) { //NOI18N
                            List<RemoteObjectLight> mirrorPort = wsBean.getSpecialAttribute(port.getClassName(), port.getOid(), "mirror", address, sessionId); //NOI18N
                            this.mirrors.put(port, mirrorPort.isEmpty() ? null : mirrorPort.get(0));
                            this.inputPorts.put(port, linkConnectedToPort);
                        }
                        else
                            this.outputPorts.put(port, linkConnectedToPort);
                    }
                    //Now we do the actual render
                    //First the inputs
                    for (RemoteObjectLight inputPort : inputPorts.keySet()) {
                        PortWidget portWidget = (PortWidget)addNode(inputPort);
                        portWidget.setConnectedLink(inputPorts.get(inputPort));
                    }
                    
                    //Then the outputs
                    for (RemoteObjectLight outputPort : outputPorts.keySet()) {
                        PortWidget portWidget = (PortWidget)addNode(outputPort);
                        portWidget.setConnectedLink(outputPorts.get(outputPort));
                    }
                    
                    //Now we connect the mirrors
//                    for (RemoteObjectLight inputPort : mirrors.keySet()) {
//                        if (mirrors.get(inputPort) != null) {
//                            String connectionId = inputPort.getOid() + " - " + mirrors.get(inputPort).getOid();
//                            addEdge(connectionId);
//                            this.setEdgeSource(connectionId, inputPort);
//                            this.setEdgeTarget(connectionId, mirrors.get(inputPort));
//                        }
//                    }
                    
                    validate();
                    repaint();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                try {
                    org.netbeans.api.visual.export.SceneExporter.createImage(this,
                            new File(PATH + spliceBox.getClassName() + "_" + spliceBox.getOid() +".png"),
                            org.netbeans.api.visual.export.SceneExporter.ImageType.PNG,
                            org.netbeans.api.visual.export.SceneExporter.ZoomType.ACTUAL_SIZE,
                            false, false, 100,
                            0,  //Not used
                            0); //Not used
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else{
                System.out.println("error");
            }
            
        } catch (ServerSideException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    protected Widget attachNodeWidget(RemoteObjectLight port) {
        PortWidget newNode;
        if (port.getName().toLowerCase().startsWith("in")) { //NOI18N
            newNode = new PortWidget(this, port, inputPorts.get(port), PortWidget.ALIGNMENT.LEFT);
            inputPortsContainer.addChild(newNode);
        } else {
            newNode = new PortWidget(this, port, outputPorts.get(port), PortWidget.ALIGNMENT.RIGHT);
            outputPortsContainer.addChild(newNode);
        }
        
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

   @Override
    protected void attachEdgeSourceAnchor(String edge, RemoteObjectLight oldSourceNode, RemoteObjectLight newSourceNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget sourceWidget = findWidget(newSourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createCircularAnchor(sourceWidget, 3) : null);
    }

    @Override
    protected void attachEdgeTargetAnchor(String edge, RemoteObjectLight oldTargetNode, RemoteObjectLight newTargetNode) {
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget targetWidget = findWidget(newTargetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createCircularAnchor(targetWidget, 3) : null);
    }
  
}
