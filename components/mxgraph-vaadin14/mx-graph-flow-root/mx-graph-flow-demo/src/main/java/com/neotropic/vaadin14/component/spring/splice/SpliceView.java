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

import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphEdge;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Route(value="splicing")
public class SpliceView extends Div {
    public SpliceView() {
        setSizeFull();
        MxGraph graph = new MxGraph();
        graph.setSizeFull();
        
        MxTree tree = new MxTree(graph, 
            () -> Arrays.asList("a-" + UUID.randomUUID().toString(), "b-" + UUID.randomUUID().toString(), "c-" + UUID.randomUUID().toString()), 
            key -> Arrays.asList("1-" + UUID.randomUUID().toString(), "2-" + UUID.randomUUID().toString(), "3-" + UUID.randomUUID().toString()), 
            key -> "!-" + key.toString(),
            (key, graph0) -> new MyLabelNode((MxGraph) graph0, (String) key, (String) key, "Blue"),
            key -> key
        );
        tree.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            graph.executeStackLayout(null, true, 200, 50);
            graph.setCellsLocked(true);
        });
        
        graph.setConnectable(true);
        graph.addEdgeCompleteListener(event -> {
            MxGraphEdge edge = new MxGraphEdge();
            edge.setSource(event.getSourceId());
            edge.setTarget(event.getTargetId());
            edge.setStrokeWidth(1);
            edge.setStrokeColor("blue");
            graph.addEdge(edge);
        });
        
        LinkedHashMap<String, String> inOutPorts = new LinkedHashMap();
        inOutPorts.put("in-1a", "out-1" + UUID.randomUUID().toString());
        inOutPorts.put("in-2" + UUID.randomUUID().toString(), "out-2a");
        inOutPorts.put("in-3a", "out-3" + UUID.randomUUID().toString());
        inOutPorts.put("in-4" + UUID.randomUUID().toString(), "out-4a");
        inOutPorts.put("in-5" + UUID.randomUUID().toString(), "out-5" + UUID.randomUUID().toString());
        
        MxSpliceBox<String> spliceBox = new MxSpliceBox(graph, inOutPorts, "Splice Box", "#708090", port -> UUID.randomUUID().toString(), null, port -> "#87CEEB");
        
        spliceBox.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            graph.executeStackLayout(null, true, 200, 100);
            graph.setCellsLocked(true);
        });
        
        add(graph);
    }
}
