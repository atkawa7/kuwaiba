package com.neotropic.vaadin14.component.spring;

import com.neotropic.flow.component.mxgraph.MxCellStyle;
import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.neotropic.flow.component.mxgraph.MxGraphCellPositionChanged;
import com.neotropic.flow.component.mxgraph.MxGraphCellUnselectedEvent;
import com.neotropic.flow.component.mxgraph.MxGraphClickEdgeEvent;
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
     }
  );
        
        MxGraphCell nodeA = new MxGraphCell();          
        MxGraphCell nodeB = new MxGraphCell();
        MxGraphCell nodeC = new MxGraphNode();
        MxGraphCell nodeD = new MxGraphNode();
        MxGraphCell edge = new MxGraphCell();
        MxGraphLayer layerEdge = new MxGraphLayer();
        MxGraphLayer layerNodes = new MxGraphLayer();
          
        MxCellStyle customStyle = new MxCellStyle("customStyle");
        customStyle.addProperty(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        customStyle.addProperty(MxConstants.STYLE_STROKECOLOR, "red");
        customStyle.addProperty(MxConstants.STYLE_FILLCOLOR, "blue");
                              
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
        
        edge.addClickEdgeListener(new ComponentEventListener<MxGraphClickEdgeEvent>() {
            @Override
            public void onComponentEvent(MxGraphClickEdgeEvent t) {
                Notification.show("mxgraph click edge");
            }
        });
        
        mxGraph.addCellUnselectedListener(new ComponentEventListener<MxGraphCellUnselectedEvent>() {
            @Override
            public void onComponentEvent(MxGraphCellUnselectedEvent t) {
               Notification.show("Cell Unselected Id:" + t.getCellId() + " is Vertex:" + t.isVertex());
            }
        });
        
        
        Button addVerticesEdge = new Button("Add Demo Vertices Edge"); // (3)

          layerNodes.setUuid("layerNodes");
          nodeA.setUuid("nodeA");
          nodeA.setImage("images/press32.png");
          nodeA.setLabel("Press");
          nodeA.setGeometry(20, 100, 80, 20);
          nodeA.setIsVertex(true);
          nodeA.setCellLayer(layerNodes.getUuid());
          nodeB.setUuid("nodeB");
          nodeB.setImage("images/print32.png");
          nodeB.setLabel("print");
          nodeB.setGeometry(200, 100, 80, 200);
          nodeB.setIsVertex(true);
          nodeB.setCellLayer(layerNodes.getUuid());
          nodeC.setUuid("nodeC");
          nodeC.setLabel("Sub Cell");
          nodeC.setGeometry(10, 30, 30, 60); 
          nodeC.setCellParent("nodeB");
          nodeD.setUuid("nodeD");
          nodeD.setLabel("Sub Cell 2");
          nodeD.setGeometry(10, 30, 30, 60); 
          nodeD.setCellParent("nodeB");

          
         //set the edge layer
          layerEdge.setUuid("edgeLayer");
          
        //set ethe edge info          
          edge.setIsEdge(true);
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
          mxGraph.refreshGraph();
          mxGraph.addLayer(layerNodes);     // remember the order in which objects are added
          mxGraph.addLayer(layerEdge);    
          
          mxGraph.addCell(nodeA);
          mxGraph.executeStackLayout("nodeA", Boolean.TRUE, 1);
          mxGraph.addCell(nodeB);
          mxGraph.addCell(nodeC);
          mxGraph.addCell(nodeD);
          mxGraph.addCell(edge);
//          nodeB.addCell(nodeC); // add the nodeC as children of the nodeB
//          nodeB.addCell(nodeD); // add the nodeC as children of the nodeB


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
     
     Button btnToggleLayoutNodePrint = new Button("Toggle Vertical/horizontal Layout Node Print", evt -> {
         mxGraph.executeStackLayout("nodeB", true, 10);
     });
     
     Button btnCustomStyle = new Button("Add Custom Style to Sheet", evt -> {
         mxGraph.addCellStyle(customStyle);
     });
     
      Button btnCustomStyleNode = new Button("Add Custom Style to Node Print", evt -> {
         nodeB.setStyleName("customStyle");
     });

     add(new HorizontalLayout(addVerticesEdge, btnToggleVisivilityNodesLager, btnToggleVisivilityEdgeLager, btnShowObjectsData, btnToggleLayoutNodePrint));
     add(new HorizontalLayout(btnCustomStyle, btnCustomStyleNode));
//        add(addButton);
    }

}
