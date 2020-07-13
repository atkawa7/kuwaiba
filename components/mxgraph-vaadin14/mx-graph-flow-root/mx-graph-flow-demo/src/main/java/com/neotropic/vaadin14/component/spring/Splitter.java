package com.neotropic.vaadin14.component.spring;

import com.neotropic.flow.component.mxgraph.MxCellStyle;
import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route(value = "splitter")
public class Splitter extends VerticalLayout {

    public Splitter(@Autowired MessageBean bean) {
        
        MxGraph mxGraph = new MxGraph();
  
        mxGraph.setWidth("900px");
        mxGraph.setHeight("400px");
               
        MxGraphCell mainBox = new MxGraphNode();   
        MxGraphCell groupOut = new MxGraphNode();    
        MxGraphCell groupEndPointsOut = new MxGraphNode();    
        MxGraphCell startInA = new MxGraphNode();           
        MxGraphCell endOutA = new MxGraphNode();   
        MxGraphCell endOutB = new MxGraphNode();   
        MxGraphCell endOutC = new MxGraphNode(); 
        MxGraphCell nodeInA = new MxGraphNode();          
        MxGraphCell nodeOutA = new MxGraphNode();
        MxGraphCell nodeOutB = new MxGraphNode();
        MxGraphCell nodeOutC = new MxGraphNode();
        MxGraphEdge edgeInA = new MxGraphEdge();
        MxGraphEdge edgeAA = new MxGraphEdge();
        MxGraphEdge edgeAB = new MxGraphEdge();
        MxGraphEdge edgeAcC = new MxGraphEdge();
        MxGraphEdge edgeOutA = new MxGraphEdge();
        MxGraphEdge edgeOutB = new MxGraphEdge();
        MxGraphEdge edgeOutC = new MxGraphEdge();
          
        MxCellStyle customStyle = new MxCellStyle("customStyle");
        customStyle.addProperty(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        customStyle.addProperty(MxConstants.STYLE_STROKECOLOR, "red");
        customStyle.addProperty(MxConstants.STYLE_FILLCOLOR, "blue");
        
        mainBox.setUuid("main");
        mainBox.setLabel("MAIN");
        mainBox.setGeometry(180, 30, 200, 100); 
        mainBox.setImage("images/print32.png");
        
        Button addDemo = new Button("Add Demo Splice Box", ev -> {
                      
        startInA.setUuid("s1");
        startInA.setLabel("");
        startInA.setGeometry(10, 80, 30, 30);
        startInA.setFillColor("white");
        startInA.setShape(MxConstants.SHAPE_ELLIPSE);
        edgeInA.setSource("s1");
        edgeInA.setTarget("i1");
        edgeInA.setStrokeWidth(1);
        edgeInA.setLabel("END POINT A");
        edgeInA.setStrokeColor("blue");    
        nodeInA.setUuid("i1");
        nodeInA.setLabel("IN-01");
        nodeInA.setGeometry(0, 0, 100, 50);
        nodeInA.setFillColor("green");
        nodeInA.setCellParent("main");
        
        groupOut.setUuid("g2");
        groupOut.setLabel("");
        groupOut.setGeometry(0, 0, 50, 150);
        groupOut.setCellParent("main");
        
        nodeOutA.setUuid("o1");
        nodeOutA.setLabel("OUT-01");
        nodeOutA.setGeometry(0, 0, 30, 30);
        nodeOutA.setFillColor("#CC9900");
        nodeOutA.setCellParent("g2");
        edgeAA.setSource("i1");
        edgeAA.setTarget("o1");
        edgeAA.setStrokeWidth(1);
        edgeAA.setLabel("MIRROR");     
               
        nodeOutB.setUuid("o2");
        nodeOutB.setLabel("OUT-02");
        nodeOutB.setGeometry(0, 0, 30, 30);
        nodeOutB.setCellParent("g2");
        nodeOutB.setFillColor("orange");
        edgeAB.setSource("i1");
        edgeAB.setTarget("o2");
        edgeAB.setStrokeWidth(1);
        edgeAB.setLabel("MIRROR");     
                
        groupEndPointsOut.setUuid("gepout");
        groupEndPointsOut.setLabel("");
        groupEndPointsOut.setGeometry(500, 20, 70, 50);
         
        endOutA.setUuid("e1");
        endOutA.setLabel("");
        endOutA.setGeometry(0, 30, 30, 30);
        endOutA.setFillColor("white");
        endOutA.setShape(MxConstants.SHAPE_ELLIPSE);
        endOutA.setCellParent("gepout");        
        edgeOutA.setSource("o1");
        edgeOutA.setTarget("e1");
        edgeOutA.setStrokeWidth(1);
        edgeOutA.setLabel("END POINT D");
        edgeOutA.setStrokeColor("blue");
        
        endOutB.setUuid("e2");
        endOutB.setLabel("");
        endOutB.setGeometry(0, 50, 30, 30);
        endOutB.setFillColor("red");
        endOutB.setShape(MxConstants.SHAPE_ELLIPSE);
        endOutB.setCellParent("gepout");
        edgeOutB.setSource("o2");
        edgeOutB.setTarget("e2");
        edgeOutB.setStrokeWidth(1);
        edgeOutB.setLabel("END POINT D");
        edgeOutB.setStrokeColor("blue");
        

        mxGraph.addCell(startInA);
        mxGraph.addCell(nodeInA);
        mxGraph.addCell(edgeInA);
        mxGraph.addCell(groupOut);
        mxGraph.addCell(nodeOutA); 
        mxGraph.addCell(nodeOutB); 
//        mxGraph.addCell(nodeOutC); 
        mxGraph.addEdge(edgeAA); 
        mxGraph.addCell(edgeAB); 
//        mxGraph.addCell(edgeAC); 
        mxGraph.addCell(groupEndPointsOut);
        mxGraph.addCell(endOutA);
        mxGraph.addCell(endOutB);  
        mxGraph.addEdge(edgeOutA); 
        mxGraph.addEdge(edgeOutB); 

        edgeOutB.addCellAddedListener(eventListener -> {
             mxGraph.executeStackLayout("gepout", false, 20);
             mxGraph.executeStackLayout("g2", false, 20);
             mxGraph.executeStackLayout("main", true, 100);
        });
        
        });     
        mxGraph.setGrid("images/grid.gif");
            
    
     Button btnAlign = new Button("Align middle", evt -> {
        mxGraph.alignCells(MxConstants.ALIGN_MIDDLE, new String [] {"s1" , "main", "gepout"}, null);
        mxGraph.alignCells(MxConstants.ALIGN_MIDDLE, new String [] {"i1" , "g2"}, null);
     });
        
     add(mxGraph);  
     mxGraph.addCell(mainBox);
     add(new HorizontalLayout(addDemo, btnAlign));
//        add(addButton);
    }

}
