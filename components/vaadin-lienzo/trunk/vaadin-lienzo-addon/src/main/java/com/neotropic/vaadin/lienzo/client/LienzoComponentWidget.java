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

import com.ait.lienzo.client.core.event.NodeDragEndEvent;
import com.ait.lienzo.client.core.event.NodeDragEndHandler;
import com.ait.lienzo.client.core.event.NodeDragMoveEvent;
import com.ait.lienzo.client.core.event.NodeDragMoveHandler;
import com.ait.lienzo.client.core.event.NodeMouseClickEvent;
import com.ait.lienzo.client.core.event.NodeMouseClickHandler;
import com.ait.lienzo.client.core.event.NodeMouseDoubleClickEvent;
import com.ait.lienzo.client.core.event.NodeMouseDoubleClickHandler;
import com.ait.lienzo.client.core.event.NodeMouseEnterEvent;
import com.ait.lienzo.client.core.event.NodeMouseEnterHandler;
import com.ait.lienzo.client.core.event.NodeMouseExitEvent;
import com.ait.lienzo.client.core.event.NodeMouseExitHandler;
import com.ait.lienzo.client.core.mediator.EventFilter;
import com.ait.lienzo.client.core.mediator.MousePanMediator;
import com.ait.lienzo.client.core.mediator.MouseWheelZoomMediator;
import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Line;
import com.ait.lienzo.client.core.shape.Picture;
import com.ait.lienzo.client.core.shape.PolyLine;
import com.ait.lienzo.client.core.shape.Shape;
import com.ait.lienzo.client.core.shape.Text;
import com.ait.lienzo.client.core.types.Point2DArray;
import com.ait.lienzo.client.widget.LienzoPanel;
import com.ait.tooling.common.api.java.util.UUID;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.neotropic.vaadin.lienzo.client.events.LienzoMouseOverListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetClickListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetRightClickListener;
import java.util.HashMap;
import java.util.Map;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.user.client.Event;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.neotropic.vaadin.lienzo.client.core.shape.EdgeWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.FrameWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.FrameWidget.ClntFrameWidgetUpdateListener;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvFrameWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvNodeWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.NodeWidget;
import com.neotropic.vaadin.lienzo.client.core.shape.SrvEdgeWidget;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetClickListener;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetRightClickListener;
import com.neotropic.vaadin.lienzo.client.events.FrameWidgetUpdateListener;
import java.util.ArrayList;
import java.util.List;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.neotropic.vaadin.lienzo.client.core.shape.EdgeWidget.ClntEdgeWidgetUpdateListener;
import com.neotropic.vaadin.lienzo.client.core.shape.Point;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetAddListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetClickListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetDblClickListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetRightClickListener;
import com.neotropic.vaadin.lienzo.client.events.EdgeWidgetUpdateListener;
import com.neotropic.vaadin.lienzo.client.events.NodeWidgetUpdateListener;

/**
 * The class representing LienzoPanel Client-side widget
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
public class LienzoComponentWidget extends LienzoPanel implements ClntFrameWidgetUpdateListener, ClntEdgeWidgetUpdateListener {
    /**
     * Inner class to manage the drawing of edges
     */        
    private class Edge {
        private NodeWidget source;
        private NodeWidget target;
        private Line line;
        // Inner class used to cancel the drawing of edges in the LienzoPanel
        private final ClickHandler clickHandler = new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                if (enableConnectionTool && source != null)
                    cancelEdgeWidget();
            }
        };
        // Inner class used to catch the source and target node widget of edge
        private final MouseMoveHandler mouseMoveHandler = new MouseMoveHandler() {
            
            @Override
            public void onMouseMove(MouseMoveEvent event) {
                if (enableConnectionTool && source != null) {
                    double x1 = source.getX() + source.getWidth() / 2;
                    double y1 = source.getY() + source.getHeight() / 2;
                    double x2 = event.getX();
                    double y2 = event.getY();
                    
                    if (line == null) {
                        line = new Line(x1, y1, x2, y2);
                        line.setStrokeColor("black");
                        line.setStrokeWidth(3);
                        
                        edgeLayer.add(line);
                    }
                    else {
                        line.setPoints(Point2DArray.fromArrayOfDouble(new double [] {x1, y1, x2, y2}));
                        line.batch();
                    }
		}
            }
        };
        
        public Edge() {
            addMouseMoveHandler(mouseMoveHandler);
            addClickHandler(clickHandler);
            source = null;
            target = null;
            line = null;
	}
        
        public Picture getSource() {
            return source;
        }
        
        public void setSource(NodeWidget source) {
            this.source = source;
        }
        
        public void setTarget(NodeWidget target) {
            this.target = target;
            addEdgeWidget();
        }
        
        public Picture getTarget() {
            return target;
        }
		
	public void addEdgeWidget() {
            edgeLayer.remove(line);
                        
            SrvEdgeWidget clntNewEdge = new SrvEdgeWidget();
            clntNewEdge.setId(UUID.uuid());
                        
            List<Point> controlPoints = new ArrayList<>();
            controlPoints.add(new Point(source.getX(), source.getY()));
            controlPoints.add(new Point(target.getX(), target.getY()));            
            
            clntNewEdge.setSource(clntNodeWidgets.get(source));
            clntNewEdge.setTarget(clntNodeWidgets.get(target));
            clntNewEdge.setControlPoints(controlPoints);
            
            source = null;
            target = null;
            line = null;
            // inform to server side that a new edge was drawn
            edgeWidgetAddListener.edgeWidgetAdded(clntNewEdge);
	}
        
        public void cancelEdgeWidget() {
            edgeLayer.remove(line);
            edgeLayer.batch();
            source = null;
            target = null;
            line = null;
	}
    }
    /* Properties to update from state component */
    private double labelsPaddingTop;
    private double labelsPaddingLeft;
    private double labelsFontSize;
    private boolean enableConnectionTool = false;    
    /* Layer */    
    private Layer backgroundLayer;
    private Layer frameLayer;
    private Layer frameLabelLayer;
    private Layer nodeLayer;
    private Layer nodeLabelLayer;
    private Layer edgeLayer;
    private Layer edgeLabelLayer;
    
    private Picture background;
    
    private Map<SrvFrameWidget, FrameWidget> srvFrameWidgets = new HashMap<>();
    private Map<FrameWidget, SrvFrameWidget> clntFrameWidgets = new HashMap<>();
    private Map<FrameWidget, Text> frameWidgetLabels = new HashMap<>();
    
    private Map<SrvNodeWidget, NodeWidget> srvNodeWidgets = new HashMap<>();
    private Map<NodeWidget, SrvNodeWidget> clntNodeWidgets = new HashMap<>();
    private Map<NodeWidget, Text> nodeWidgetLabels = new HashMap<>();
    
    private Map<SrvEdgeWidget, EdgeWidget> srvEdgeWidgets = new HashMap<>();
    private Map<EdgeWidget, SrvEdgeWidget> clntEdgeWidgets = new HashMap<>();
    
    private Map<EdgeWidget, Text> edgeWidgetLabels = new HashMap<>();
    
    private Map<EdgeWidget, NodeWidget> edgeSourceNodes = new HashMap<>();
    private Map<EdgeWidget, NodeWidget> edgeTargetNodes = new HashMap<>();
    
    private Map<NodeWidget, List<EdgeWidget>> nodeInputEdges = new HashMap<>();
    private Map<NodeWidget, List<EdgeWidget>> nodeOutputEdges = new HashMap<>();
    // Listeners
    private LienzoMouseOverListener lienzoMouseOverListener = null;
    
    private NodeWidgetClickListener nodeWidgetClickListener = null;
    private NodeWidgetRightClickListener nodeWidgetRightClickListener = null;
    private NodeWidgetDblClickListener nodeWidgetDblClickListener = null;
    private NodeWidgetUpdateListener nodeWidgetUpdateListener = null;
    
    private FrameWidgetClickListener frameWidgetClickListener = null;
    private FrameWidgetDblClickListener frameWidgetDblClickListener = null;
    private FrameWidgetRightClickListener frameWidgetRightClickListener = null;
    private FrameWidgetUpdateListener frameWidgetUpdateListener = null;
    
    private EdgeWidgetAddListener edgeWidgetAddListener = null;
    private EdgeWidgetClickListener edgeWidgetClickListener = null;
    private EdgeWidgetRightClickListener edgeWidgetRightClickListener = null;
    private EdgeWidgetDblClickListener edgeWidgetDblClickListener = null;
    private EdgeWidgetUpdateListener edgeWidgetUpdateListener = null;        
    // The Shape that throws the NodeMouseEnterEvent.
    private Shape shapeMouseEnter = null;    
    // The Edge Widget that throws the NodeMouseEnterEvent.
    private EdgeWidget edgeWidgetMouseEnter = null;
    
    private final Edge tempEdge;
    /**
     * Implementation to support the right click event for nodes, edges and frameWidgets.
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
                if (shapeMouseEnter != null) {
                    
                    if (shapeMouseEnter instanceof NodeWidget) {
                        
                        if (nodeWidgetRightClickListener != null) {
                            String id = clntNodeWidgets.get((NodeWidget) shapeMouseEnter).getId();
                            nodeWidgetRightClickListener.nodeWidgetRightClicked(id);
                        }
                        return;
                    }
                    
                    if (shapeMouseEnter instanceof FrameWidget) {
                        
                        if (frameWidgetRightClickListener != null) {
                            long id = clntFrameWidgets.get((FrameWidget) shapeMouseEnter).getId();
                            frameWidgetRightClickListener.frameWidgetRightClicked(id);
                        }
                        return;
                    }
                    
                    if (shapeMouseEnter instanceof PolyLine) {
                        
                        if (edgeWidgetRightClickListener != null) {
                            String id = clntEdgeWidgets.get(edgeWidgetMouseEnter).getId();
                            edgeWidgetRightClickListener.edgeWidgetRightClicked(id);
                        }
                    }
                }
            }
        }
    };
    
    public LienzoComponentWidget() {        
        getViewport().pushMediator(new MousePanMediator(EventFilter.CONTROL, EventFilter.BUTTON_LEFT));
        getViewport().pushMediator(new MouseWheelZoomMediator(EventFilter.SHIFT, EventFilter.CONTROL));
        
        this.tempEdge = new Edge();
        backgroundLayer = new Layer();
        add(backgroundLayer);
        backgroundLayer.draw();
        
        frameLayer = new Layer();
        add(frameLayer);
        frameLayer.draw();
        
        frameLabelLayer = new Layer();
        add(frameLabelLayer);
        frameLabelLayer.draw();
        
        edgeLayer = new Layer();
        add(edgeLayer);
        edgeLayer.draw();
        
        edgeLabelLayer = new Layer();
        add(edgeLabelLayer);
        edgeLabelLayer.draw();
                
        nodeLayer = new Layer();
        add(nodeLayer);
        nodeLayer.draw();
        
        nodeLabelLayer = new Layer();
        add(nodeLabelLayer);
        nodeLabelLayer.draw();
        
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
    
    public boolean isEnableConnectionTool() {
        return enableConnectionTool;
    }
    
    public void setEnableConnectionTool(boolean enableConnectionTool) {
        this.enableConnectionTool = enableConnectionTool;
    }
    
    public double getLabelsFontSize() {
        return labelsFontSize;
    }
    
    public void setLabelsFontSize(double labelsFontSize) {
        this.labelsFontSize = labelsFontSize;        
    }
    
    public double getLabelsPaddingTop() {
        return labelsPaddingTop;
    }
    
    public void setLabelsPaddingTop(double labelsPaddingTop) {
        this.labelsPaddingTop = labelsPaddingTop;
    }
    
    public double getLabelsPaddingLeft() {
        return labelsPaddingLeft;        
    }
    
    public void setLabelsPaddingLeft(double labelsPaddingLeft) {
        this.labelsPaddingLeft = labelsPaddingLeft;
    }
    
    /**
     * @param url url or null to remove background
     * @param x background x position
     * @param y background y position
     */
    public void setBackground(String url, double x, double y) {
        if (url != null) {
            if (background == null) {
                addBackground(url, x, y);
            }
            else {
                if (url.equals(background.getURL())) {
                    if (background.getX() != x)
                        background.setX(x);
                    
                    if (background.getY() != y)
                        background.setY(y);
                    
                    backgroundLayer.batch();
                }
                else {
                    removeBackground();
                    addBackground(url, x, y);
                }
            }            
        }
        else {
            removeBackground();
        }
    }
    
    private void addBackground(String url, double x, double y) {
        background = new Picture(url);
        background.setX(x);
        background.setY(y);
        
        backgroundLayer.add(background);
        backgroundLayer.batch();
    }
    
    private void removeBackground() {
        if (background != null) {
            backgroundLayer.remove(background);
            backgroundLayer.batch();
            background = null;
        }
    }
    
    public void addFrameFromServer(SrvFrameWidget srvFrame) {        
        String caption = srvFrame.getCaption();
        double x = srvFrame.getX();
        double y = srvFrame.getY();
        double width = srvFrame.getWidth();
        double height = srvFrame.getHeight();
        
        FrameWidget frameWidget = new FrameWidget(frameLayer, caption, x, y, width, height);
        frameWidget.addFrameWidgetUpdateListener(this);
        
        frameWidget.addNodeMouseClickHandler(new NodeMouseClickHandler() {

            @Override
            public void onNodeMouseClick(NodeMouseClickEvent event) {
                FrameWidget clntFrame = (FrameWidget) event.getSource();
                clntFrame.setEditable(!clntFrame.isEditable());
                
                if (frameWidgetClickListener != null) {
                    long id = clntFrameWidgets.get(clntFrame).getId();
                    frameWidgetClickListener.frameWidgetClicked(id);
                }
            }
        });        
        frameWidget.addNodeMouseDoubleClickHandler(new NodeMouseDoubleClickHandler() {

            @Override
            public void onNodeMouseDoubleClick(NodeMouseDoubleClickEvent event) {
                if (frameWidgetDblClickListener != null) {
                    FrameWidget clntFrame = (FrameWidget) event.getSource();
                    
                    long id = clntFrameWidgets.get(clntFrame).getId();
                    frameWidgetDblClickListener.frameWidgetDblClicked(id);
                }
            }
        });                
        frameWidget.addNodeMouseEnterHandler(new NodeMouseEnterHandler() {

            @Override
            public void onNodeMouseEnter(NodeMouseEnterEvent event) {
                shapeMouseEnter = (FrameWidget) event.getSource();
            }
        });
        frameWidget.addNodeMouseExitHandler(new NodeMouseExitHandler() {

            @Override
            public void onNodeMouseExit(NodeMouseExitEvent event) {
                shapeMouseEnter = null;
            }
        });
        

        Text frameLabel = new Text(caption);
        frameLabel.setFontSize(labelsFontSize);
        frameLabel.setX(x + labelsPaddingLeft);
        frameLabel.setX(y + labelsPaddingTop + labelsFontSize);
        
        frameLayer.add(frameWidget);
        frameWidget.batch();
        
        frameLabelLayer.add(frameLabel);
        frameLabel.batch();
        
        srvFrameWidgets.put(srvFrame, frameWidget);
        clntFrameWidgets.put(frameWidget, srvFrame);
        frameWidgetLabels.put(frameWidget, frameLabel);
    }
    
    public void updateFrameFromServer(SrvFrameWidget srvFrame) {
        if (srvFrameWidgets.containsKey(srvFrame)) {
            FrameWidget frameWidget = srvFrameWidgets.get(srvFrame);
            
            frameWidget.setCaption(srvFrame.getCaption());
            frameWidget.setX(srvFrame.getX());
            frameWidget.setY(srvFrame.getY());
            frameWidget.setWidth(srvFrame.getWidth());
            frameWidget.setHeight(srvFrame.getHeight());
            
            SrvFrameWidget clntFrame = clntFrameWidgets.get(frameWidget);
            
            clntFrame.setCaption(srvFrame.getCaption());
            clntFrame.setX(srvFrame.getX());
            clntFrame.setY(srvFrame.getY());
            clntFrame.setWidth(srvFrame.getWidth());
            clntFrame.setHeight(srvFrame.getHeight());
            
            updateFrameLabel(frameWidget);
        }
    }
    
    public void removeFrameFromServer(SrvFrameWidget srvFrame) {
        FrameWidget frameWidget = srvFrameWidgets.remove(srvFrame);
        clntFrameWidgets.remove(frameWidget);
        Text frameWidgetLabel = frameWidgetLabels.remove(frameWidget);
                
        frameWidget.removeControlPoints();
        frameWidget.removeFrameWidgetUpdateListener(this);
        frameLayer.remove(frameWidget);
        frameLayer.batch();
        
        frameLabelLayer.remove(frameWidgetLabel);
        frameLabelLayer.batch();
    }
    
    public void addNodeFromServer(SrvNodeWidget srvNode) {
        
        NodeWidget nodeWidget = new NodeWidget(srvNode.getUrlIcon());
        nodeWidget.setDraggable(true);
        nodeWidget.setCaption(srvNode.getCaption());
        nodeWidget.setX(srvNode.getX());
        nodeWidget.setY(srvNode.getY());
        nodeWidget.setWidth(srvNode.getWidth());
        nodeWidget.setHeight(srvNode.getHeight());
        
        nodeWidget.addNodeMouseClickHandler(new NodeMouseClickHandler() {

            @Override
            public void onNodeMouseClick(NodeMouseClickEvent event) {
                NodeWidget nodeWidget = (NodeWidget) event.getSource();
                SrvNodeWidget clntNode = clntNodeWidgets.get(nodeWidget);
                
                if (enableConnectionTool && tempEdge.getSource() == null) {
                    tempEdge.setSource(nodeWidget);
                }
                else if (enableConnectionTool && tempEdge.getTarget() == null) {
                    tempEdge.setTarget(nodeWidget);
                }
                nodeWidgetClickListener.nodeWidgetClicked(clntNode.getId());
            }
        });
        nodeWidget.addNodeMouseDoubleClickHandler(new NodeMouseDoubleClickHandler() {

            @Override
            public void onNodeMouseDoubleClick(NodeMouseDoubleClickEvent event) {
                NodeWidget nodeWidget = (NodeWidget) event.getSource();
                SrvNodeWidget clntNode = clntNodeWidgets.get(nodeWidget);
                nodeWidgetDblClickListener.nodeWidgetDoubleClicked(clntNode.getId());
            }
        });
        nodeWidget.addNodeMouseEnterHandler(new NodeMouseEnterHandler() {

            @Override
            public void onNodeMouseEnter(NodeMouseEnterEvent event) {
                shapeMouseEnter = (NodeWidget) event.getSource();
            }
        });
        nodeWidget.addNodeMouseExitHandler(new NodeMouseExitHandler() {

            @Override
            public void onNodeMouseExit(NodeMouseExitEvent event) {
                shapeMouseEnter = null;
            }
        });
        nodeWidget.addNodeDragMoveHandler(new NodeDragMoveHandler() {

            @Override
            public void onNodeDragMove(NodeDragMoveEvent event) {
                NodeWidget nodeWidget = (NodeWidget) event.getSource();
                Text nodeWidgetLabel = nodeWidgetLabels.get(nodeWidget);
                nodeWidgetLabel.setX(nodeWidget.getX());
                nodeWidgetLabel.setY(nodeWidget.getY() + nodeWidget.getHeight() + labelsFontSize);
                nodeWidgetLabel.batch();
                
                if (nodeOutputEdges.containsKey(nodeWidget)) {
                    for (EdgeWidget edgeWidget : nodeOutputEdges.get(nodeWidget)) {
                        // Update first point
                        edgeWidget.updateHeadConnection(
                            nodeWidget.getX() + nodeWidget.getWidth() / 2, 
                            nodeWidget.getY() + nodeWidget.getHeight() / 2);
                    }
                }
                if (nodeInputEdges.containsKey(nodeWidget)) {
                    for (EdgeWidget edgeWidget : nodeInputEdges.get(nodeWidget)) {
                        // Update last point
                        edgeWidget.updateTailConnection(
                            nodeWidget.getX() + nodeWidget.getWidth() / 2, 
                            nodeWidget.getY() + nodeWidget.getHeight() / 2);
                    }
                }
            }
        });
        nodeWidget.addNodeDragEndHandler(new NodeDragEndHandler() {

            @Override
            public void onNodeDragEnd(NodeDragEndEvent event) {
                NodeWidget clntNode = (NodeWidget) event.getSource();
                
                SrvNodeWidget srvNode = clntNodeWidgets.get(clntNode);
                srvNode.setX(clntNode.getX());
                srvNode.setY(clntNode.getY());
                
                nodeWidgetUpdateListener.nodeWidgetUpdated(srvNode);
            }
        });
        Text nodeWidgetLabel = new Text(srvNode.getCaption());
        nodeWidgetLabel.setFontSize(labelsFontSize);
        nodeWidgetLabel.setX(srvNode.getX());
        nodeWidgetLabel.setY(srvNode.getY() + srvNode.getHeight() + labelsFontSize);
        
        srvNodeWidgets.put(srvNode, nodeWidget);
        clntNodeWidgets.put(nodeWidget, srvNode);
        nodeWidgetLabels.put(nodeWidget, nodeWidgetLabel);
        
        nodeLayer.add(nodeWidget);
        nodeWidget.batch();
        
        nodeLabelLayer.add(nodeWidgetLabel);
        nodeWidgetLabel.batch();
    }
    
    public void updateNodeFromServer(SrvNodeWidget srvNode) {
        if (srvNodeWidgets.containsKey(srvNode)) {
            NodeWidget nodeWidget = srvNodeWidgets.get(srvNode);
            SrvNodeWidget clntNode = clntNodeWidgets.get(nodeWidget);
            
            clntNode.setCaption(srvNode.getCaption());
            clntNode.setUrlIcon(srvNode.getUrlIcon());            
            
            nodeWidgetLabels.get(nodeWidget).setText(srvNode.getCaption());            
            nodeWidgetLabels.get(nodeWidget).batch();
        }
    }
    
    public void removeNodeFromServer(SrvNodeWidget srvNode) {
        NodeWidget nodeWidget = srvNodeWidgets.remove(srvNode);
        clntNodeWidgets.remove(nodeWidget);
        Text nodeWidgetLabel = nodeWidgetLabels.remove(nodeWidget);        
        
        if (nodeOutputEdges.containsKey(nodeWidget)) {
            for (EdgeWidget edgeWidget : nodeOutputEdges.remove(nodeWidget)) {
                srvEdgeWidgets.remove(clntEdgeWidgets.remove(edgeWidget));
                
                edgeSourceNodes.remove(edgeWidget);
                NodeWidget target = edgeTargetNodes.remove(edgeWidget);
                
                nodeInputEdges.get(target).remove(edgeWidget);                
                
                edgeWidget.removeEdgetWidgetUpdateListener(this);
                edgeWidget.removePolyLine();
                                
                Text edgeWidgetLabel = edgeWidgetLabels.remove(edgeWidget);                
                edgeLabelLayer.remove(edgeWidgetLabel);
            }
        }
        
        if (nodeInputEdges.containsKey(nodeWidget)) {
            for (EdgeWidget edgeWidget : nodeInputEdges.remove(nodeWidget)) {
                srvEdgeWidgets.remove(clntEdgeWidgets.remove(edgeWidget));
                                
                NodeWidget source = edgeSourceNodes.remove(edgeWidget);
                edgeTargetNodes.remove(edgeWidget);
                
                nodeOutputEdges.get(source).remove(edgeWidget);                
                
                edgeWidget.removeEdgetWidgetUpdateListener(this);
                edgeWidget.removePolyLine();
                
                Text edgeWidgetLabel = edgeWidgetLabels.remove(edgeWidget);                
                edgeLabelLayer.remove(edgeWidgetLabel);
            }
        }
        edgeLabelLayer.batch();
        
        nodeLayer.remove(nodeWidget);
        nodeLayer.batch();
        
        nodeLabelLayer.remove(nodeWidgetLabel);
        nodeLabelLayer.batch();
    }
    
    public void addEdgeFromServer(SrvEdgeWidget srvEdge) {
        NodeWidget source = srvNodeWidgets.get(srvEdge.getSource());
        NodeWidget target = srvNodeWidgets.get(srvEdge.getTarget());
        
        List<Point> coordinates;
        
        coordinates = new ArrayList();

        coordinates.add(new Point(
            source.getX() + source.getWidth() / 2, 
            source.getY() + source.getHeight() / 2));

        coordinates.add(new Point(
            target.getX() + target.getWidth() / 2, 
            target.getY() + target.getHeight() / 2));
        
        if (srvEdge.getControlPoints() == null || (srvEdge.getControlPoints() != null && srvEdge.getControlPoints().size() <= 1)) {    
            
        }
        else {
            srvEdge.getControlPoints().set(0, coordinates.get(0));
            srvEdge.getControlPoints().set(srvEdge.getControlPoints().size() - 1, coordinates.get(1));
            
            coordinates = srvEdge.getControlPoints();
        }
        
        final EdgeWidget edgeWidget = new EdgeWidget(edgeLayer, srvEdge.getColor(), coordinates);
        edgeWidget.addEdgeWidgetUpdateListener(this);
        edgeWidget.setCaption(srvEdge.getCaption());
        
        edgeWidget.getPolyLine().addNodeMouseClickHandler(new NodeMouseClickHandler() {
            
            @Override
            public void onNodeMouseClick(NodeMouseClickEvent event) {
                edgeWidget.setEditable(!edgeWidget.isEditable());
                
                if (edgeWidgetClickListener != null) {
                    SrvEdgeWidget clntEdge = clntEdgeWidgets.get(edgeWidget);
                    edgeWidgetClickListener.edgeWidgetClicked(clntEdge.getId());
                }
            }
        });
        edgeWidget.getPolyLine().addNodeMouseDoubleClickHandler(new NodeMouseDoubleClickHandler() {

            @Override
            public void onNodeMouseDoubleClick(NodeMouseDoubleClickEvent event) {
                if (edgeWidgetDblClickListener != null) {
                    SrvEdgeWidget clntEdge = clntEdgeWidgets.get(edgeWidget);
                    edgeWidgetDblClickListener.edgeWidgetDblClicked(clntEdge.getId());
                }
            }
        });
        edgeWidget.getPolyLine().addNodeMouseEnterHandler(new NodeMouseEnterHandler() {

            @Override
            public void onNodeMouseEnter(NodeMouseEnterEvent event) {
                shapeMouseEnter = edgeWidget.getPolyLine(); 
                edgeWidgetMouseEnter = edgeWidget;
            }
        });
        edgeWidget.getPolyLine().addNodeMouseExitHandler(new NodeMouseExitHandler() {

            @Override
            public void onNodeMouseExit(NodeMouseExitEvent event) {
                shapeMouseEnter = null;
                edgeWidgetMouseEnter = null;
            }
        });
        srvEdgeWidgets.put(srvEdge, edgeWidget);
        clntEdgeWidgets.put(edgeWidget, srvEdge);
        
	edgeSourceNodes.put(edgeWidget, source);
	edgeTargetNodes.put(edgeWidget, target);
        
        if (!nodeOutputEdges.containsKey(source))
            nodeOutputEdges.put(source, new ArrayList());
        nodeOutputEdges.get(source).add(edgeWidget);
        
        if (!nodeInputEdges.containsKey(target))
            nodeInputEdges.put(target, new ArrayList());
        nodeInputEdges.get(target).add(edgeWidget);
                
        Text edgeLabel = new Text(edgeWidget.getCaption());
        Point midCp = edgeWidget.connectionMidControlPoint();
        
        edgeLabel.setFontSize(labelsFontSize);
        edgeLabel.setX(midCp.getX());
        edgeLabel.setY(midCp.getY() + labelsPaddingTop + labelsFontSize);
        
        edgeWidgetLabels.put(edgeWidget, edgeLabel);
        
        edgeLabelLayer.add(edgeLabel);
        edgeLabel.batch();
    }
    
    public void updateEdgeFromServer(SrvEdgeWidget srvEdge) {
        if (srvEdgeWidgets.containsKey(srvEdge)) {
            EdgeWidget edgeWidget = srvEdgeWidgets.get(srvEdge);
                        
            SrvEdgeWidget clntEdge = clntEdgeWidgets.get(edgeWidget);
            
            if (!clntEdge.getCaption().equals(srvEdge.getCaption())) {
                
                edgeWidget.setCaption(srvEdge.getCaption());
                
                clntEdge.setCaption(srvEdge.getCaption());
                
                if (edgeWidgetLabels.containsKey(edgeWidget)) {
                    Text edgeWidgetLabel = edgeWidgetLabels.get(edgeWidget);
                    edgeWidgetLabel.setText(srvEdge.getCaption());

                    edgeWidgetLabel.batch();
                }
            }
            
            if (!clntEdge.getColor().equals(srvEdge.getColor())) {
                
                edgeWidget.setStrokeColor(srvEdge.getColor());
                
                clntEdge.setColor(srvEdge.getColor());
            }
        }
    }
    
    public void removeEdgeFromServer(SrvEdgeWidget srvEdge) {
        EdgeWidget edgeWidget = srvEdgeWidgets.remove(srvEdge);
        clntEdgeWidgets.remove(edgeWidget);
        Text edgeWidgetLabel = edgeWidgetLabels.remove(edgeWidget);
        
        NodeWidget source = edgeSourceNodes.remove(edgeWidget);
        NodeWidget target = edgeTargetNodes.remove(edgeWidget);
        
        nodeOutputEdges.get(source).remove(edgeWidget);
        nodeInputEdges.get(target).remove(edgeWidget);
        
        edgeWidget.removeEdgetWidgetUpdateListener(this);
        edgeWidget.removePolyLine();
        
        edgeLabelLayer.remove(edgeWidgetLabel);
        edgeLabelLayer.batch();
    }
    
    public void setLienzoMouseOverListener(LienzoMouseOverListener listener) {
        this.lienzoMouseOverListener = listener;
    }
    
    public void setNodeWidgetClickListener(NodeWidgetClickListener listener) {
        this.nodeWidgetClickListener = listener;
    }
    
    public void setNodeWidgetRightClickListener(NodeWidgetRightClickListener listener) {
        this.nodeWidgetRightClickListener = listener;
    }
    
    public void setNodeWidgetDblClickListener(NodeWidgetDblClickListener listener) {
        this.nodeWidgetDblClickListener = listener;        
    }
    
    public void setNodeWidgetUpdateListener(NodeWidgetUpdateListener listener) {
        this.nodeWidgetUpdateListener = listener;
    }
    
    public void setFrameWidgetClickListener(FrameWidgetClickListener listener) {
        this.frameWidgetClickListener = listener;
    }
    
    public void setFrameWidgetDblClickListener(FrameWidgetDblClickListener listener) {
        this.frameWidgetDblClickListener = listener;        
    }
    
    public void setFrameWidgetRightClickListener(FrameWidgetRightClickListener listener) {
        this.frameWidgetRightClickListener = listener;
    }
    
    public void setFrameWidgetUpdateListener(FrameWidgetUpdateListener listener) {
        this.frameWidgetUpdateListener = listener;        
    }
    
    public void setEdgeWidgetAddListener(EdgeWidgetAddListener listener) {
        this.edgeWidgetAddListener = listener;
    }
    
    public void setEdgeWidgetClickListener(EdgeWidgetClickListener listener) {
        this.edgeWidgetClickListener = listener;
    }
    
    public void setEdgeWidgetDblClickListener(EdgeWidgetDblClickListener listener) {
        this.edgeWidgetDblClickListener = listener;
    }
    
    public void setEdgeWidgetRightClickListener(EdgeWidgetRightClickListener listener) {
        this.edgeWidgetRightClickListener = listener;
    }
    
    public void setEdgeWidgetUpdateListener(EdgeWidgetUpdateListener listener) {
        this.edgeWidgetUpdateListener = listener;
    }
    
    private void updateFrameLabel(FrameWidget frameWidget) {
        Text clntFrameText = frameWidgetLabels.get(frameWidget);
        
        clntFrameText.setText(frameWidget.getCaption());
        clntFrameText.setX(frameWidget.getX() + labelsPaddingLeft);
        clntFrameText.setY(frameWidget.getY() + labelsPaddingTop + labelsFontSize);
        clntFrameText.batch();
    }
    
    @Override
    public void frameWidgetUpdated(FrameWidget frameWidgetUpdated) {
        if (clntFrameWidgets.containsKey(frameWidgetUpdated)) {
            
            if (frameWidgetLabels.containsKey(frameWidgetUpdated))
                updateFrameLabel(frameWidgetUpdated);
                        
            if (frameWidgetUpdateListener != null) {
                SrvFrameWidget svrFrame = clntFrameWidgets.get(frameWidgetUpdated);
                
                svrFrame.setCaption(frameWidgetUpdated.getCaption());
                svrFrame.setX(frameWidgetUpdated.getX());
                svrFrame.setY(frameWidgetUpdated.getY());
                svrFrame.setWidth(frameWidgetUpdated.getWidth());
                svrFrame.setHeight(frameWidgetUpdated.getHeight());
                
                frameWidgetUpdateListener.frameWidgetUpdated(svrFrame);
            }
	}
    }
    
    @Override
    public void connectionWidgetUpdated(EdgeWidget edgeWidgetUpdated) {
        if (clntEdgeWidgets.containsKey(edgeWidgetUpdated)) {
            if (edgeWidgetLabels.containsKey(edgeWidgetUpdated)) {
                Text edgeLabel = edgeWidgetLabels.get(edgeWidgetUpdated);
                
                Point midCp = edgeWidgetUpdated.connectionMidControlPoint();
                edgeLabel.setX(midCp.getX());
                edgeLabel.setY(midCp.getY() + labelsPaddingTop + labelsFontSize);
                
                edgeLabel.batch();
            }
            
            if (edgeWidgetUpdateListener != null) {
                SrvEdgeWidget clntEdge = clntEdgeWidgets.get(edgeWidgetUpdated);
                
                List<Point> points = new ArrayList();
                for (Circle controlPoint : edgeWidgetUpdated.getControlPoints())
                    points.add(new Point(controlPoint.getX(), controlPoint.getY()));
                
                clntEdge.setControlPoints(points);
                
                edgeWidgetUpdateListener.edgeWidgetUpdated(clntEdge);
            }
        }
    }
        
    public final native void browserLog(Object obj) /*-{
        $wnd.console.log(obj);
    }-*/;
}