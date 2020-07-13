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

@Route
@PWA(name = "Project Base for Vaadin Flow with Spring", shortName = "Project Base")
public class MainView extends VerticalLayout {

    public MainView(@Autowired MessageBean bean) {
        
        MxGraph mxGraph = new MxGraph();
  
        mxGraph.setWidth("400px");
        mxGraph.setHeight("400px");
       
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
        
        MxGraphNode nodeA = new MxGraphNode();          
        MxGraphNode nodeB = new MxGraphNode();
        MxGraphNode nodeContainer = new MxGraphNode();
        MxGraphNode nodeC = new MxGraphNode();
        MxGraphNode nodeD = new MxGraphNode();
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
        
        nodeA.addRightClickEdgeListener(t-> {
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
        
        edge.addClickEdgeListener(new ComponentEventListener<MxGraphClickCellEvent>() {
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
          nodeA.setGeometry(20, 100, 80, 20);
          nodeA.setCellLayer(layerNodes.getUuid());
          nodeB.setUuid("nodeB");
          nodeB.setImage("images/print32.png");
          nodeB.setShape(MxConstants.SHAPE_IMAGE);
          nodeB.setLabel("print");
          nodeB.setGeometry(200, 100, 80, 20);
          nodeB.setCellLayer(layerNodes.getUuid());
          nodeContainer.setUuid("nodeContainer");
          nodeContainer.setLabel("Container");
          nodeContainer.setGeometry(300, 100, 80, 200);
          nodeContainer.setCellLayer(layerNodes.getUuid());
          nodeContainer.setAnimateOnSelect(true);
          nodeC.setUuid("nodeC");
          nodeC.setLabel("Sub Cell");
          nodeC.setGeometry(10, 30, 30, 60); 
          nodeC.setCellParent("nodeContainer");
          nodeD.setUuid("nodeD");
          nodeD.setLabel("Sub Cell 2");
          nodeD.setGeometry(10, 30, 30, 60); 
          nodeD.setCellParent("nodeContainer");

          
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
       
          add(mxGraph);
                mxGraph.addGraphLoadedListener(evt -> { // always add styles, executeLayouts, align cells etc 
                                                        //when the graph is already loaded
                mxGraph.addCellStyle(customStyle);
                mxGraph.addCellStyle(customStyle2);
          });
          mxGraph.refreshGraph();
          mxGraph.addLayer(layerNodes);     // remember the order in which objects are added
          mxGraph.addLayer(layerEdge);     // add layers first that his childrens
          
          mxGraph.addNode(nodeA);
          mxGraph.executeStackLayout("nodeA", Boolean.TRUE, 1);
          mxGraph.addNode(nodeB);
          mxGraph.addNode(nodeContainer);
          mxGraph.addNode(nodeC);
          mxGraph.addNode(nodeD);
          mxGraph.addEdge(edge);

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
     }); 
        
     Button btnToggleVisivilityEdgeLager = new Button("Hide/Show Edge Layer", evt -> {
         layerEdge.toggleVisibility();
     });
     
     Button btnToggleVisivilityNodesLager = new Button("Hide/Show Nodes Layer", evt -> {
         layerNodes.toggleVisibility();
     });
     
     Button btnToggleLayoutNodePrint = new Button("Execute Horizontal Layout in Container node", evt -> {
         mxGraph.executeStackLayout("nodeContainer", true, 10);
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

     add(new HorizontalLayout(btnToggleVisivilityNodesLager, btnToggleVisivilityEdgeLager, btnShowObjectsData, btnToggleLayoutNodePrint));
     add(new HorizontalLayout(btnCustomStyle1Node, btnCustomStyle2Node, btnRemoveContainerNode));

    }

}
