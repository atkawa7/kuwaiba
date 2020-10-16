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
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.neotropic.flow.component.mxgraph.MxGraph;
import java.util.LinkedHashMap;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;

/**
 * Show a circle with a color
 * +---+
 * | O |
 * +---+
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class CableNode extends MxBusinessObjectNode {
    private final String FONT_COLOR = "Black";
    private final String FONT_SIZE = String.valueOf(11);
    private final String STROKE_COLOR = "#A9A9A9";
    private final int WIDTH = 16;
    private final int HEIGHT = WIDTH;
    private final String FOLDABLE = String.valueOf(0);
    
    private final LinkedHashMap<String, String> COLOR_STYLE = new LinkedHashMap();
    {
        COLOR_STYLE.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_ELLIPSE);
        COLOR_STYLE.put(MxConstants.STYLE_FILLCOLOR, MxConstants.NONE);
        COLOR_STYLE.put(MxConstants.STYLE_STROKECOLOR, STROKE_COLOR);
        COLOR_STYLE.put(MxConstants.STYLE_FOLDABLE, FOLDABLE);
        COLOR_STYLE.put(MxConstants.STYLE_LABEL_POSITION, MxConstants.ALIGN_MIDDLE);
        COLOR_STYLE.put(MxConstants.STYLE_VERTICAL_LABEL_POSITION, MxConstants.ALIGN_BOTTOM);
        COLOR_STYLE.put(MxConstants.STYLE_FONTCOLOR, FONT_COLOR);
        COLOR_STYLE.put(MxConstants.STYLE_FONTSIZE, FONT_SIZE);
    }
    
    public CableNode(MxGraph graph, BusinessObjectLight businessObject, String color) {
        super(businessObject);
        Objects.requireNonNull(graph);
        Objects.requireNonNull(businessObject);
        setGeometry(0, 0, WIDTH, HEIGHT);
        setLabel(businessObject.getName());
        LinkedHashMap<String, String> rawStyle = new LinkedHashMap(COLOR_STYLE);
        if (color != null)
            rawStyle.put(MxConstants.STYLE_FILLCOLOR, color);
        
        setRawStyle(rawStyle);
        addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            
            overrideStyle();
            setIsSelectable(false);
            setConnectable(false);
            setTooltip(businessObject.getName());
            
            graph.setCellsLocked(true);
            event.unregisterListener();
        });
        graph.addNode(this);
    }
}
