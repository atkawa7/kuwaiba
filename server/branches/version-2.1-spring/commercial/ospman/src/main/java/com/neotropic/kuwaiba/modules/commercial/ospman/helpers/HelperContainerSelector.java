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
package com.neotropic.kuwaiba.modules.commercial.ospman.helpers;

import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Wires builder helper
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class HelperContainerSelector {
    private final MxGraph graph;
    private Registration selectedListener;
    private final List<MxGraphCell> edges = new ArrayList();
    
    public HelperContainerSelector(MxGraph graph) {
        Objects.requireNonNull(graph);
        this.graph = graph;
    }
    
    public List<MxGraphCell> getEdges() {
        return edges;
    }
    
    public void start() {
        selectedListener = graph.addCellSelectedListener(event -> {
            MxGraphCell edge = getEdge(event.getCellId());
            if (edge != null) {
                edges.add(edge);
            }
        });
    }
    
    public void cancel() {
        if (selectedListener != null) {
            try {
            selectedListener.remove();
            } catch(Exception ex) {
                //Nothing to do
            }
        }
        edges.clear();
    }
    
    private MxGraphCell getEdge(String id) {
        if (id != null) {
            Iterator<Component> children = graph.getChildren().iterator();
            while (children.hasNext()) {
                Component child = children.next();
                if (child instanceof MxGraphCell) {
                    MxGraphCell cell = (MxGraphCell) child;
                    if (cell.getIsEdge() && id.equals(cell.getUuid()))
                        return cell;
                }
            }
        }
        return null;
    }
}
