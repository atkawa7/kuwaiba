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
import java.util.UUID;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Route(value = "tree")
public class TreeView extends Div {
    public TreeView() {
        setSizeFull();
        
        MxGraph graph = new MxGraph();
        graph.setSizeFull();
        
        MxTree tree = new MxTree(graph, 
            () -> Arrays.asList("a-" + UUID.randomUUID().toString(), "b-" + UUID.randomUUID().toString(), "c-" + UUID.randomUUID().toString()), 
            key -> Arrays.asList("1-" + UUID.randomUUID().toString(), "2-" + UUID.randomUUID().toString(), "3-" + UUID.randomUUID().toString()), 
            key -> "!-" + key.toString(),
            null,
            key -> key
        );
        tree.addCellAddedListener(event -> 
            graph.executeStackLayout(null, false, 10, 10, 0, 0, 0)
        );
        
        graph.setConnectable(true);
        graph.addEdgeCompleteListener(event -> {
            MxGraphEdge edge = new MxGraphEdge();
            edge.setSource(event.getSourceId());
            edge.setTarget(event.getTargetId());
            edge.setStrokeWidth(1);
            edge.setStrokeColor("blue");
            graph.addEdge(edge);
        });
     
        add(graph);
    }
}
