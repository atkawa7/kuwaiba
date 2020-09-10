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
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import java.util.LinkedHashMap;
import java.util.UUID;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@Route(value="splice")
public class SpliceBoxView extends Div {
    public SpliceBoxView() {
        setSizeFull();
        
        MxGraph graph = new MxGraph();
        graph.setSizeFull();
        
        LinkedHashMap<String, String> inOutPorts = new LinkedHashMap();
        inOutPorts.put("in-1a", "out-1" + UUID.randomUUID().toString());
        inOutPorts.put("in-2" + UUID.randomUUID().toString(), "out-2a");
        inOutPorts.put("in-3a", "out-3" + UUID.randomUUID().toString());
        inOutPorts.put("in-4" + UUID.randomUUID().toString(), "out-4a");
        inOutPorts.put("in-5" + UUID.randomUUID().toString(), "out-5" + UUID.randomUUID().toString());
        
        MxSpliceBox<String> spliceBox = new MxSpliceBox(graph, inOutPorts, "Splice Box", "#708090", port -> UUID.randomUUID().toString(), null, port -> "#87CEEB");
        
        spliceBox.addCellAddedListener(event -> {
            graph.setCellsLocked(false);
            graph.executeStackLayout(null, false, 0, 200);
            graph.setCellsLocked(true);
        });
        
        add(graph);
    }
}
