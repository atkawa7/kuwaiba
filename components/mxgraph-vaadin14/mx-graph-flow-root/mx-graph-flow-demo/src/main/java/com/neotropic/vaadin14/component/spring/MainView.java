package com.neotropic.vaadin14.component.spring;

import com.neotropic.vaadin14.component.MxGraph;
import com.neotropic.vaadin14.component.MxGraphCell;
import com.neotropic.vaadin14.component.MxGraphCellPositionChanged;
import com.neotropic.vaadin14.component.MxGraphClickEdgeEvent;
import com.neotropic.vaadin14.component.MxGraphPoint;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
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
        MxGraph myElement = new MxGraph();
  
        myElement.setWidth("400px");
        myElement.setHeight("400px");
       
       
        Button addButton = new Button("Add Cell"); // (3)

        addButton.addClickListener(click -> {
     // (1)
     MxGraphCell mxGraphCell = new MxGraphCell();
     
     myElement.addCell(mxGraphCell);
     }
  );
        
          MxGraphCell nodeA = new MxGraphCell();          
          MxGraphCell nodeB = new MxGraphCell();
          MxGraphCell edge = new MxGraphCell();
          
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
        
        
        Button addVerticesEdge = new Button("Add Demo Vertices Edge"); // (3)

        addVerticesEdge.addClickListener(click -> {
     // (1)

          
          nodeA.setUuid("1");
          nodeA.setImage("images/press32.png");
          nodeA.setLabel("Press");
          nodeA.setGeometry(20, 100, 80, 20);
          nodeA.setIsVertex(true);
          nodeB.setUuid("2");
          nodeB.setImage("images/print32.png");
          nodeB.setLabel("print");
          nodeB.setGeometry(200, 100, 80, 20);
          nodeB.setIsVertex(true);

          nodeB.setUuid("2");
          edge.setIsEdge(true);
          edge.setSourceLabel("Source Label");
          edge.setTargetLabel("Target Label");
          edge.setSource(nodeA.getUuid());
          edge.setTarget(nodeB.getUuid());
//          edge.setLabelBackgroundColor("gray");
          edge.setStrokeWidth(2);
          edge.setStrokeColor("blue");
          edge.setPerimeterSpacing(2);
//          edge.setFontColor("white");

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
       
          myElement.addCell(nodeA);
          myElement.addCell(nodeB);
          myElement.addCell(edge);
     }
  );
          Button addPoint = new Button("Add Demo Point Edge"); // (3)

        addPoint.addClickListener(click -> {
     // (1)
          MxGraphPoint pointA = new MxGraphPoint();          

          pointA.setX(105);
          pointA.setY(50);

         
          edge.addPoint(pointA);

          
     }
  );              
        myElement.setGrid("images/grid.gif");
        
        Button btnPointsChanged = new Button("Show Updated Data", click -> {      

          Notification.show("Points edge: "+ edge.getPoints());
          Notification.show("Position Vertex Press: X: " + nodeA.getX() + " Y: " + nodeA.getY());         
          Notification.show("Position Vertex Print: X: " + nodeB.getX() + " Y: " + nodeB.getY());
          Notification.show("label Vertex Press: " + nodeA.getLabel());
          Notification.show("label Vertex Print: " + nodeB.getLabel());
          Notification.show("label edge: " + edge.getLabel());
          Notification.show("Source label edge: " + edge.getSourceLabel());
          Notification.show("Target label edge: " + edge.getTargetLabel());


          
        
     });
        
        add(myElement);
        add(btnPointsChanged);
        add(addButton);
        add(addVerticesEdge);
        add(addPoint);
    }

}
