/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.vaadin14.component.spring.splice;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphNode;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MyLabelNode extends MxGraphNode {
    private final String FONT_COLOR = "#000000";
    private final int CHARACTER_LIMIT = 30;
    private final int LABEL_WIDTH = 210;
    private final int LABEL_HEIGHT = 16;
    private final int SPACING = 0;
    LinkedHashMap<String, String> NODE_STYLE = new LinkedHashMap();
    {
        NODE_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_RECTANGLE);
        NODE_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        NODE_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
    }
    LinkedHashMap<String, String> COLOR_STYLE = new LinkedHashMap();
    {
        COLOR_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_ELLIPSE);
        COLOR_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        COLOR_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
    }
    LinkedHashMap<String, String> LABEL_STYLE = new LinkedHashMap();
    {
        LABEL_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_LABEL);
        LABEL_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        LABEL_STYLE.put(MxConstants.STYLE_STROKECOLOR, MxConstants.NONE);
        LABEL_STYLE.put(MxConstants.STYLE_FONTCOLOR, FONT_COLOR);
        LABEL_STYLE.put(MxConstants.STYLE_FONTSIZE, "10");
        LABEL_STYLE.put(MxConstants.STYLE_ALIGN, MxConstants.ALIGN_LEFT);
        LABEL_STYLE.put(MxConstants.STYLE_SPACING_LEFT, "10");
    }
    private final MxGraph graph;
    private final String id;
    private final String label;
    private final String color;
    
    public MyLabelNode(MxGraph graph, String id, String label, String color) {
        super();
        Objects.requireNonNull(graph);
        this.graph = graph;
        this.id = id;
        this.label = label;
        this.color = color;
        setGeometry(0, 0, LABEL_WIDTH + LABEL_HEIGHT + SPACING, LABEL_HEIGHT);
        setRawStyle(NODE_STYLE);
        addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            overrideStyle();
            setIsSelectable(false);
            setConnectable(false);
            addColorNode();
            graph.setCellsLocked(true);
            event.unregisterListener();
        });
    }
    
    private void addColorNode() {
        MxGraphNode colorNode = new MxGraphNode();
        colorNode.setGeometry(0, 0, LABEL_HEIGHT, LABEL_HEIGHT);
        LinkedHashMap<String, String> rawStyle = new LinkedHashMap(COLOR_STYLE);
        if (color != null)
            rawStyle.put(MxConstants.STYLE_FILLCOLOR, color);
        
        colorNode.setRawStyle(rawStyle);
        colorNode.setCellParent(this.getUuid());
        colorNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            colorNode.overrideStyle();
            colorNode.setIsSelectable(false);
            colorNode.setConnectable(false);
            addLabelNode();
            graph.setCellsLocked(true);
            event.unregisterListener();
        });
        graph.add(colorNode);
    }
    
    private void addLabelNode() {
        MxGraphNode labelNode = new MxGraphNode();
        labelNode.setGeometry(0, 0, LABEL_WIDTH, LABEL_HEIGHT);
        labelNode.setRawStyle(LABEL_STYLE);
        if (id != null)
            labelNode.setUuid(id);
        if (getLabel(label) != null)
            labelNode.setLabel(getLabel(label));
        labelNode.setCellParent(this.getUuid());
        labelNode.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            labelNode.overrideStyle();
            graph.executeStackLayout(this.getUuid(), true, SPACING);
            graph.setCellsLocked(true);
            event.unregisterListener();
        });
        graph.add(labelNode);
    }
    
    private String getLabel(String l) {
        String label = null;
        if  (l != null) {
            if (l.length() > CHARACTER_LIMIT)
                label = String.format("%s ...", l.substring(0, CHARACTER_LIMIT + 1));
            return label;
        }
        return label;
    }
}
