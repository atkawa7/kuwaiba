/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.vaadin.lienzo.client;

import com.ait.lienzo.client.core.event.NodeMouseClickEvent;
import com.ait.lienzo.client.core.event.NodeMouseClickHandler;
import com.ait.lienzo.client.core.event.NodeMouseDoubleClickEvent;
import com.ait.lienzo.client.core.event.NodeMouseDoubleClickHandler;
import com.ait.lienzo.client.core.event.NodeMouseEnterEvent;
import com.ait.lienzo.client.core.event.NodeMouseEnterHandler;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.MultiPath;
import com.ait.lienzo.client.core.shape.Picture;
import com.ait.lienzo.client.core.shape.Shape;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.shape.wires.WiresManager;
import com.ait.lienzo.client.core.shape.wires.WiresShape;
import com.ait.lienzo.client.core.shape.wires.event.WiresDragEndEvent;
import com.ait.lienzo.client.core.shape.wires.event.WiresDragEndHandler;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.neotropic.vaadin.lienzo.client.core.shape.LienzoNode;
import com.neotropic.vaadin.lienzo.client.events.LienzoMouseOverListener;
import com.neotropic.vaadin.lienzo.client.events.LienzoNodeClickListener;
import com.neotropic.vaadin.lienzo.client.events.LienzoNodeDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.LienzoNodeRightClickListener;
import java.util.HashMap;
import java.util.Map;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;

/**
 * The class representing LienzoPanel Client-side widget
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
public class LienzoComponentWidget extends LienzoPanel {
    private double labelFontSize = 11;
        
    private Layer backgroundLayer;
    /**
     * Layer for node, icons, edges and frames.
     */
    private Layer wiresLayer;
    private WiresManager wiresManager;
    // label Layers
    private Layer nodeLabelLayer;
    private Layer edgeLabelLayer;
    private Layer frameLabelLayer;
    
    private final Map<Long, WiresShape> nodeWiresShape = new HashMap<>();
    private final Map<Long, Picture> nodePicture = new HashMap<>();
    private final Map<Picture, Long> pictureIds = new HashMap<>();
    private final Map<Long, Text> nodeLabel = new HashMap<>();
    
    public LienzoMouseOverListener lienzoMouseOverListener = null;
    public LienzoNodeClickListener lienzoNodeClickListener = null;
    public LienzoNodeRightClickListener lienzoNodeRightClickListener = null;
    public LienzoNodeDblClickListener lienzoNodeDblClickListener = null;
        
    // The Shape that throws the NodeMouseEnterEvent.
    private Shape nodeMouseEnter = null;
    /**
     * Implementation to support the right click event for nodes, edges and frames.
     * A little trick: first we handle the NodeMouseEnterEvent of Shapes. 
     * After, we handle the ContextMenuEvent of our LienzoPanel and in this way 
     * we handle the right click event.
     */
    private ContextMenuHandler contextMenuHandler = new ContextMenuHandler() {

        @Override
        public void onContextMenu(ContextMenuEvent event) {
            event.preventDefault();
            event.stopPropagation();
            
            if (event.getNativeEvent().getButton() == NativeEvent.BUTTON_RIGHT) {
                if (nodeMouseEnter != null) {
                    if (nodeMouseEnter instanceof Picture) {
                        if (pictureIds.containsKey(nodeMouseEnter)) {
                            if (lienzoNodeRightClickListener != null) {
                                lienzoNodeRightClickListener
                                    .lienzoNodeRightClicked(pictureIds.get(nodeMouseEnter));
                            }
                        }
                    }
                    nodeMouseEnter = null;
                }
            }
        }
    };
    
    public LienzoComponentWidget() {
        sinkEvents(Event.ONCONTEXTMENU);
        addHandler(contextMenuHandler, ContextMenuEvent.getType());
                
        addMouseOverHandler(new MouseOverHandler() {

            @Override
            public void onMouseOver(MouseOverEvent event) {
                if (lienzoMouseOverListener != null)
                    lienzoMouseOverListener.lienzoMouseOver(event.getX(), event.getY());
            }
        });
    }
    
    public void drawLayers() {
        backgroundLayer = new Layer();
        wiresLayer = new Layer();
        nodeLabelLayer = new Layer();
        edgeLabelLayer = new Layer();
        frameLabelLayer = new Layer();
        
        wiresManager = WiresManager.get(wiresLayer);
                
        add(wiresLayer);
        wiresLayer.draw();
                
        add(nodeLabelLayer);
        nodeLabelLayer.draw();
        
        add(edgeLabelLayer);
        edgeLabelLayer.draw();
        
        add(frameLabelLayer);
        frameLabelLayer.draw();
    }
    
    public void setLienzoMouseOverListener(LienzoMouseOverListener listener) {
        this.lienzoMouseOverListener = listener;
    }
    
    public void setLienzoNodeClickListener(LienzoNodeClickListener listener) {
        this.lienzoNodeClickListener = listener;
    }
    
    public void setLienzoNodeRightClickListener(LienzoNodeRightClickListener listener) {
        this.lienzoNodeRightClickListener = listener;
    }
    
    public void setLienzoNodeDblClickListener(LienzoNodeDblClickListener listener) {
        this.lienzoNodeDblClickListener = listener;        
    }
    
    protected void addLienzoNode(final LienzoNode node) {
        if (wiresManager == null) {
            drawLayers();
        }
        long id = node.getId();
        
        final Picture aNodePicture = new Picture(node.getUrlIcon());
        
        aNodePicture.addNodeMouseClickHandler(new NodeMouseClickHandler() {

            @Override
            public void onNodeMouseClick(NodeMouseClickEvent event) {
                if (lienzoNodeClickListener != null)
                    lienzoNodeClickListener.lienzoNodeClicked(node.getId());
            }
        });
        aNodePicture.addNodeMouseDoubleClickHandler(new NodeMouseDoubleClickHandler() {
            
            @Override
            public void onNodeMouseDoubleClick(NodeMouseDoubleClickEvent event) {
                if (lienzoNodeDblClickListener != null)
                    lienzoNodeDblClickListener.lienzoNodeDoubleClicked(node.getId());
            }
        });
        // A little trick: 
        aNodePicture.addNodeMouseEnterHandler(new NodeMouseEnterHandler() {

            @Override
            public void onNodeMouseEnter(NodeMouseEnterEvent event) {
                nodeMouseEnter = aNodePicture;
            }
        });
        
        final Text aNodeLabel = new Text(node.getCaption());
        aNodeLabel.setFontSize(labelFontSize);
        aNodeLabel.setX(node.getX());
        aNodeLabel.setY(node.getY() + node.getHeight() + labelFontSize);
                
        MultiPath multiPath = new MultiPath();
        multiPath.rect(0, 0, node.getWidth(), node.getWidth());
        multiPath.setStrokeColor("black");
        
        final WiresShape aNodeWiresShape = new WiresShape(multiPath);        
        aNodeWiresShape.setX(node.getX());
        aNodeWiresShape.setY(node.getY());
        aNodeWiresShape.setDraggable(node.isDraggable());
        aNodeWiresShape.addChild(aNodePicture);
        
        aNodeWiresShape.addWiresDragEndHandler(new WiresDragEndHandler() {

            @Override
            public void onShapeDragEnd(WiresDragEndEvent event) {
                aNodeLabel.setX(aNodeWiresShape.getX());
                aNodeLabel.setY(aNodeWiresShape.getY() + node.getHeight() + labelFontSize);
                nodeLabelLayer.batch();
            }
        });
        nodePicture.put(id, aNodePicture);
        pictureIds.put(aNodePicture, id);
        
        wiresManager.register(aNodeWiresShape);
        wiresManager.getMagnetManager().createMagnets(aNodeWiresShape);
        
        nodeWiresShape.put(id, aNodeWiresShape);
        
        nodeLabelLayer.add(aNodeLabel);
        nodeLabelLayer.batch();
        
        nodeLabel.put(id, aNodeLabel);
        
        //TODO: add linenzo nodes children recursive
    }
    
    public final native void browserLog(Object obj) /*-{
        $wnd.console.log(obj);
    }-*/;
}