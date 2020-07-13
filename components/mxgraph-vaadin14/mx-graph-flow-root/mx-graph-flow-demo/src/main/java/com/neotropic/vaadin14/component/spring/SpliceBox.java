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

@Route(value = "splicebox")
public class SpliceBox extends VerticalLayout {

    public SpliceBox(@Autowired MessageBean bean) {
        
        MxGraph mxGraph = new MxGraph();
  
        mxGraph.setWidth("700px");
        mxGraph.setHeight("400px");
  
        MxGraphCell mainBox = new MxGraphNode();   
        MxGraphCell groupA = new MxGraphNode();   
        MxGraphCell groupB = new MxGraphNode();     
        MxGraphCell startInA = new MxGraphNode();   
        MxGraphCell startInB = new MxGraphNode();   
        MxGraphCell endOutA = new MxGraphNode();   
        MxGraphCell endOutB = new MxGraphNode();   
        MxGraphCell nodeInA = new MxGraphNode();          
        MxGraphCell nodeInB = new MxGraphNode();
        MxGraphCell nodeOut1 = new MxGraphNode();
        MxGraphCell nodeOut2 = new MxGraphNode();
        MxGraphEdge edgeInA = new MxGraphEdge();
        MxGraphEdge edgeInB = new MxGraphEdge();
        MxGraphEdge edgeOutA = new MxGraphEdge();
        MxGraphEdge edgeOutB = new MxGraphEdge();
//        MxGraphLayer layerEdge = new MxGraphLayer();
//        MxGraphLayer layerNodes = new MxGraphLayer();
          
        MxCellStyle customStyle = new MxCellStyle("customStyle");
        customStyle.addProperty(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        customStyle.addProperty(MxConstants.STYLE_STROKECOLOR, "red");
        customStyle.addProperty(MxConstants.STYLE_FILLCOLOR, "blue");
        
        mainBox.setUuid("main");
        mainBox.setLabel("MAIN");
        mainBox.setGeometry(180, 30, 200, 300); 
        mainBox.setImage("images/print32.png");
        
        Button addDemo = new Button("Add Demo Splice Box", ev -> {
                      
        groupA.setUuid("g1");
        groupA.setLabel("");
        groupA.setGeometry(0, 100, 200, 50);   
        groupA.setCellParent("main");
        nodeInA.setUuid("i1");
        nodeInA.setLabel("IN-01");
        nodeInA.setGeometry(0, 0, 100, 50);
        nodeInA.setFillColor("green");
        nodeInA.setCellParent("g1");
        nodeOut1.setUuid("o1");
        nodeOut1.setLabel("OUT-01");
        nodeOut1.setGeometry(0, 0, 100, 50);
        nodeOut1.setFillColor("#CC9900");
        nodeOut1.setCellParent("g1");
        startInA.setUuid("s1");
        startInA.setLabel("");
        startInA.setGeometry(10, 40, 30, 30);
        startInA.setFillColor("white");
        startInA.setShape(MxConstants.SHAPE_ELLIPSE);
        edgeInA.setSource("s1");
        edgeInA.setTarget("i1");
        edgeInA.setStrokeWidth(1);
        edgeInA.setLabel("END POINT A");
        edgeInA.setStrokeColor("blue");
        endOutA.setUuid("e1");
        endOutA.setLabel("");
        endOutA.setGeometry(500, 40, 30, 30);
        endOutA.setFillColor("white");
        endOutA.setShape(MxConstants.SHAPE_ELLIPSE);
        edgeOutA.setSource("o1");
        edgeOutA.setTarget("e1");
        edgeOutA.setStrokeWidth(1);
        edgeOutA.setLabel("END POINT C");
        edgeOutA.setStrokeColor("blue");
        
        groupB.setUuid("g2");
        groupB.setLabel("");
        groupB.setGeometry(0, 100, 200, 50);
        groupB.setCellParent("main");
        nodeInB.setUuid("i2");
        nodeInB.setLabel("IN-02");
        nodeInB.setGeometry(0, 0, 100, 50);
        nodeInB.setFillColor("#77DA33");
        nodeInB.setCellParent("g2");
        nodeOut2.setUuid("o2");
        nodeOut2.setLabel("OUT-02");
        nodeOut2.setGeometry(0, 0, 100, 50);
        nodeOut2.setCellParent("g2");
        startInB.setUuid("s2");
        startInB.setLabel("");
        startInB.setGeometry(10, 90, 30, 30);
        edgeInB.setSource("s2");
        edgeInB.setTarget("i2");
        edgeInB.setStrokeWidth(1);
        edgeInB.setStrokeColor("blue");
        edgeInB.setFillColor("red");
        edgeInB.setPerimeterSpacing(2);
        edgeInB.setIsCurved(true);
        edgeInB.setIsDashed(true);
        edgeInB.setIsEdge(true);
        edgeInB.setLabel("END POINT B");
        endOutB.setUuid("e2");
        endOutB.setLabel("");
        endOutB.setGeometry(500, 90, 30, 30);
        endOutB.setFillColor("white");
        endOutB.setShape(MxConstants.SHAPE_ELLIPSE);
        edgeOutB.setSource("o2");
        edgeOutB.setTarget("e2");
        edgeOutB.setStrokeWidth(1);
        edgeOutB.setLabel("END POINT D");
        edgeOutB.setStrokeColor("blue");

        mxGraph.addCell(groupA);
        mxGraph.addCell(groupB);
        mxGraph.addCell(nodeInA);
        mxGraph.addCell(nodeOut1); 
        mxGraph.addCell(nodeInB); 
        mxGraph.addCell(nodeOut2); 
        mxGraph.addCell(startInA);
        mxGraph.addCell(startInB);
        mxGraph.addCell(endOutA);
        mxGraph.addCell(endOutB);
        endOutB.addCellAddedListener(evt -> { // when the last cell is added, then execute the layouts
            mxGraph.executeStackLayout("main", false, 1);
            mxGraph.executeStackLayout("g1", true, 1);
            mxGraph.executeStackLayout("g2", true, 1);
        });
        mxGraph.addEdge(edgeInA);
        mxGraph.addEdge(edgeInB);
        mxGraph.addEdge(edgeOutA);
        mxGraph.addEdge(edgeOutB);
        
        });     
        mxGraph.setGrid("images/grid.gif");
        
     mxGraph.addCell(mainBox);
     add(mxGraph);  
     add(new HorizontalLayout(addDemo));
//        add(addButton);
    }

}
