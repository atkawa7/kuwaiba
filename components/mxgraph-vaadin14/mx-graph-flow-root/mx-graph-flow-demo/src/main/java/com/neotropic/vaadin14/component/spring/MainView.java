package com.neotropic.vaadin14.component.spring;

import com.neotropic.flow.component.mxgraph.MxCellStyle;
import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.neotropic.flow.component.mxgraph.MxGraphCellPositionChanged;
import com.neotropic.flow.component.mxgraph.MxGraphCellUnselectedEvent;
import com.neotropic.flow.component.mxgraph.MxGraphClickCellEvent;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphLayer;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.PWA;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import java.util.UUID;

@Route
@PWA(name = "Project Base for Vaadin Flow with Spring", shortName = "Project Base")
public class MainView extends VerticalLayout {

    boolean tooglePosition = false;
    public MainView(@Autowired MessageBean bean) {
        
        MxGraph mxGraph = new MxGraph();
        mxGraph.setTooltips(true);
  
        mxGraph.setWidth("700px");
        mxGraph.setHeight("400px");
        mxGraph.setRotationEnabled(true);
//        mxGraph.setHasOutline(true);
        mxGraph.setOverflow("scroll");
        mxGraph.setBeginUpdateOnInit(true);
//        mxGraph.setIsCellMovable(false);
        Button addButton = new Button("Add Cell"); // (3)

        
        addButton.addClickListener(click -> {
     // (1)
            MxGraphCell mxGraphCell = new MxGraphCell();
            mxGraph.addCell(mxGraphCell);
        });
        
        mxGraph.addClickGraphListener((t) -> {
              Notification.show("Graph Clicked on X: " + t.getX()+ " Y: " + t.getY());
        });
        
        mxGraph.addRightClickGraphListener((t) -> {
              Notification.show("Right Click Graph on X: " + t.getX()+ " Y: " + t.getY());
        });
        
        mxGraph.addMouseMoveGraphListener((t) -> {
              //Notification.show("Right Click Graph on X: " + t.getX()+ " Y: " + t.getY());
              System.out.println("MOuse Move at X :" + t.getX()+ " Y: " + t.getY());
        });
        
        MxGraphNode nodeA = new MxGraphNode();          
        MxGraphNode nodeB = new MxGraphNode();
        MxGraphNode nodeContainer = new MxGraphNode();
        MxGraphNode nodeC = new MxGraphNode();
        MxGraphNode nodeD = new MxGraphNode();
        MxGraphNode nodeE = new MxGraphNode();
        MxGraphEdge edge = new MxGraphEdge();
        MxGraphLayer layerEdge = new MxGraphLayer();
        MxGraphLayer layerNodes = new MxGraphLayer();
          
        MxCellStyle customStyle = new MxCellStyle("customStyle");
        customStyle.addProperty(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        customStyle.addProperty(MxConstants.STYLE_STROKECOLOR, "red");
        customStyle.addProperty(MxConstants.STYLE_FILLCOLOR, "blue");
        MxCellStyle customStyle2 = new MxCellStyle("customStyle2");
        customStyle2.addProperty(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_HEXAGON);
        customStyle2.addProperty(MxConstants.STYLE_STROKECOLOR, "green");
        customStyle2.addProperty(MxConstants.STYLE_FILLCOLOR, "orange");
        
        nodeA.addRightClickCellListener(t-> {
             Notification.show("Right Click Graph on X:Cell :" + nodeA.getLabel());
        });
                              
        nodeA.addCellPositionChangedListener(new ComponentEventListener<MxGraphCellPositionChanged>() {
            @Override
            public void onComponentEvent(MxGraphCellPositionChanged t) {
                Notification.show("Node Press moved");
            }
        });
        
        nodeB.addCellPositionChangedListener(new ComponentEventListener<MxGraphCellPositionChanged>() {
            @Override
            public void onComponentEvent(MxGraphCellPositionChanged t) {
                Notification.show("Node Print moved");
            }
        });
        
        edge.addClickCellListener(new ComponentEventListener<MxGraphClickCellEvent>() {
            @Override
            public void onComponentEvent(MxGraphClickCellEvent t) {
                Notification.show("mxgraph click edge");
            }
        });
        
        mxGraph.addCellUnselectedListener(new ComponentEventListener<MxGraphCellUnselectedEvent>() {
            @Override
            public void onComponentEvent(MxGraphCellUnselectedEvent t) {
               Notification.show("Cell Unselected Id:" + t.getCellId() + " is Vertex:" + t.isVertex());
            }
        });

         // only set an id to the layers
          layerNodes.setUuid("layerNodes");
          
          nodeA.setUuid("nodeA");
          nodeA.setShape(MxConstants.SHAPE_IMAGE);
          nodeA.setImage("images/press32.png");
          nodeA.setLabel("Press");
          nodeA.setGeometry(50, 100, 80, 20);
          nodeA.setCellLayer(layerNodes.getUuid());       
          nodeB.setUuid("nodeB");
          nodeB.setImage("images/print32.png");
          nodeB.setShape(MxConstants.SHAPE_IMAGE);
          nodeB.setLabel("print");
          nodeB.setGeometry(200, 100, 80, 20);
          nodeB.setCellLayer(layerNodes.getUuid());
//          nodeB.setIsSelectable(false);
          nodeContainer.setUuid("nodeContainer");
          nodeContainer.setLabel("Container");
          nodeContainer.setFillColor(MxConstants.NONE);
          nodeContainer.setGeometry(300, 100, 60, 100);
          nodeContainer.setCellLayer(layerNodes.getUuid());
          nodeContainer.setAnimateOnSelect(true);
          nodeContainer.addCellAddedListener(eventListener-> {
            nodeContainer.setTooltip(UUID.randomUUID().toString());
            nodeContainer.addOverlayButton("zoomIn", "Zoom In", "images/zoom_in.png", MxConstants.ALIGN_LEFT, MxConstants.ALIGN_TOP, 0, 0); 
          });
          nodeContainer.addCellAddedListener(eventListener-> {
            nodeContainer.addOverlayButton("zoomOut", "Zoom Out", "images/zoom_out.png", MxConstants.ALIGN_LEFT, MxConstants.ALIGN_BOTTOM, 0, 0); 
          });
          nodeContainer.addClickOverlayButtonListener(eventListener -> {
              if (eventListener.getButtonId().equals("zoomIn"))
                 mxGraph.zoomIn();
              if (eventListener.getButtonId().equals("zoomOut"))
                 mxGraph.zoomOut();
          });
          nodeC.setUuid("nodeC");
          nodeC.setLabel("Sub Cell");
          nodeC.setGeometry(10, 0, 30, 60); 
          nodeC.setCellParent("nodeContainer");
          nodeC.setShape(MxConstants.SHAPE_ELLIPSE);
          //in this way we can append some style properties to the current cell style without using the stylesheet
          nodeC.setRawStyle(MxConstants.STYLE_STROKECOLOR  + "=" + "red;" + MxConstants.STYLE_STROKEWIDTH + "=" + 3);
          nodeD.setUuid("nodeD");
          nodeD.setRawStyle("text");
          nodeD.setLabel("Sub Cell 2");
          nodeD.setGeometry(10, 0, 30, 60); 
          nodeD.setCellParent("nodeContainer");
          nodeD.setVerticalLabelPosition(MxConstants.ALIGN_TOP);
          
          nodeE.setUuid("nodeE");
          nodeE.setLabel("Sub Cell 2");
          nodeE.setShape(MxConstants.SHAPE_LABEL);
          nodeE.setGeometry(10, 30, 30, 20); 
          nodeE.setCellParent("nodeContainer");
          
         //set the edge layer
          layerEdge.setUuid("edgeLayer");
          
        //set ethe edge info          
          edge.setSourceLabel("Source Label");
          edge.setTargetLabel("Target Label");
          edge.setSource(nodeA.getUuid());
          edge.setTarget(nodeB.getUuid());
//          edge.setLabelBackgroundColor("gray");
          edge.setStrokeWidth(1);
          edge.setStrokeColor("blue");
          edge.setPerimeterSpacing(2);
          edge.setIsCurved(true);
          edge.setIsDashed(true);
//          edge.setFontColor("white");
          edge.setCellLayer("edgeLayer");

         

         // ArrayList<Point> points = new ArrayList<>();
          JsonArray points = Json.createArray();
          JsonObject point = Json.createObject();
          point.put("x", 100);
          point.put("y", 200);
           points.set(0, point);
          point = Json.createObject();
          point.put("x", 200);
          point.put("y", 100);
          points.set(1, point);
//          points.add(new Point(10,10));
//          points.add(new Point(100,100));

          edge.setPoints(points.toJson());
       
          VerticalLayout lytGraph = new VerticalLayout(mxGraph); // Used to create a scroll bar
          lytGraph.setMaxHeight("400px");
          add(lytGraph);
          mxGraph.addGraphLoadedListener(evt -> { // always add styles, executeLayouts, align cells etc                                                         //when the graph is already loaded
                mxGraph.addCellStyle(customStyle);
                mxGraph.addCellStyle(customStyle2);
//                nodeB.setStyleName("customStyle");
          });
//          mxGraph.refreshGraph();


          mxGraph.addLayer(layerNodes);     // remember the order in which objects are added
          mxGraph.addLayer(layerEdge);     // add layers first that his childrens
          
          mxGraph.addNode(nodeA);
          mxGraph.executeStackLayout("nodeA", Boolean.TRUE, 1);
          mxGraph.addNode(nodeB);
          mxGraph.addNode(nodeContainer);
          mxGraph.addNode(nodeC);
          mxGraph.addNode(nodeD);
          mxGraph.addNode(nodeE);               
          mxGraph.addEdge(edge);
          edge.addCellAddedListener(evt -> {
             mxGraph.endUpdate();
          });
//          Button addPoint = new Button("Add Demo Point Edge"); // (3)
//
//        addPoint.addClickListener(click -> {
//     // (1)
//          MxGraphPoint pointA = new MxGraphPoint();          
//          pointA.setX(105);
//          pointA.setY(50);        
//          edge.addPoint(pointA);
//    
//     }
//  );               
        mxGraph.setGrid("images/grid.gif");
        
        Button btnShowObjectsData = new Button("Show Updated Data", click -> {      

          Notification.show("Points edge: "+ edge.getPoints());
          Notification.show("Position Vertex Press: X: " + nodeA.getX() + " Y: " + nodeA.getY());         
          Notification.show("Position Vertex Print: X: " + nodeB.getX() + " Y: " + nodeB.getY());
          Notification.show("label Vertex Press: " + nodeA.getLabel());
          Notification.show("label Vertex Print: " + nodeB.getLabel());
          Notification.show("label edge: " + edge.getLabel());
          Notification.show("Source label edge: " + edge.getSourceLabel());
          Notification.show("Target label edge: " + edge.getTargetLabel());   
          Notification.show("Scale: " + mxGraph.getScale());   
     }); 
        
     Button btnToggleVisivilityEdgeLager = new Button("Hide/Show Edge Layer", evt -> {
         layerEdge.toggleVisibility();
     });
     
     Button btnToggleVisivilityNodesLager = new Button("Hide/Show Nodes Layer", evt -> {
         layerNodes.toggleVisibility();
     });
     
     
     Button btnExecLayoutNodeContainer = new Button("Execute Horizontal Layout in Container node", evt -> {
//         mxGraph.setCellsMovable(true);
//         mxGraph.executeStackLayout("nodeContainer", false, 10);
         mxGraph.executeStackLayout("nodeContainer", true, 10,30);
         mxGraph.alignCells(MxConstants.ALIGN_CENTER, new String [] {"nodeContainer"}, 0);
//         mxGraph.setCellsMovable(false);
     });

     Button btnCustomStyle1Node = new Button("Add Custom Style 1 to Node Print", evt -> {
         nodeB.setStyleName("customStyle");
     });
      
     Button btnCustomStyle2Node = new Button("Add Custom Style 2 to Node Print", evt -> {
         nodeB.setStyleName("customStyle2");
     });
     
     Button btnRemoveContainerNode = new Button("Remove Container Node", evt -> {
         mxGraph.removeNode(nodeContainer);
     });
     
//     mxGraph.setConnectable(true);
//     mxGraph.addEdgeCompleteListener(event -> {
//         Notification.show(String.format("sourceId=%s targetId=%s", event.getSourceId(), event.getTargetId()));
//     });
     
     Button btnZoomIn = new Button("Zoom In", evt -> {
         mxGraph.zoomIn();
     });
     
     Button btnZoomOut = new Button("Zoom Out", evt -> {
         mxGraph.zoomOut();
     });
     
     Button btnAlignCenter = new Button("Align Cells Center", evt -> {
         mxGraph.alignCells(MxConstants.ALIGN_MIDDLE, new String [] {"nodeA", "nodeB", "nodeContainer"});
     });
     
     Button btnAlignBottom = new Button("Align Cells Bottm", evt -> {
         mxGraph.alignCells(MxConstants.ALIGN_BOTTOM, new String [] {"nodeA", "nodeB", "nodeContainer"});
     });
    
     Button btnRemoveOverlayButton = new Button("Remove Zoom In Overlay", evt -> {
         nodeContainer.removeOverlayButton("zoomIn");
     });
     
     Button btnRemoveOverlayButtons = new Button("Remove all Overlay buttons", evt -> {
         nodeContainer.removeOverlayButtons();
     });
     
     Button btnChangeChildrenPos = new Button("Change Children Position", evt -> {
         if (tooglePosition)
            nodeContainer.setChildrenCellPosition("nodeC", 0);
         else
            nodeContainer.setChildrenCellPosition("nodeD", 0);
         tooglePosition();
         mxGraph.executeStackLayout("nodeContainer", true, 10);
     });
     
     add(new HorizontalLayout(btnToggleVisivilityNodesLager, btnToggleVisivilityEdgeLager, btnExecLayoutNodeContainer, btnShowObjectsData));
     add(new HorizontalLayout(btnCustomStyle1Node, btnCustomStyle2Node, btnRemoveContainerNode, btnChangeChildrenPos));
     add(new HorizontalLayout(btnZoomIn, btnZoomOut, btnAlignBottom, btnAlignCenter, btnRemoveOverlayButton, btnRemoveOverlayButtons));

    }
    
    public void tooglePosition() {
        tooglePosition = !tooglePosition;
    }

}
