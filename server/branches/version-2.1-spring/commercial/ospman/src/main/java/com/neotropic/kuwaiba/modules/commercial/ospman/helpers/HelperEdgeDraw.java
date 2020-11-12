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

import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapGraph;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapNode;
import com.vaadin.flow.shared.Registration;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapProvider;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;

/**
 * Helps to store the overlay, source, target and edge points/coordinates.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class HelperEdgeDraw {
    private MxBusinessObjectNode source;
    private MxBusinessObjectNode target;
    private List<GeoCoordinate> coordinates;
    private Registration selectListener;
    
    private final MapProvider mapProvider;
    private final MapGraph mapGraph;
    private final Consumer<HelperEdgeDraw> consumer;
    
    public HelperEdgeDraw(MapProvider mapProvider, MapGraph mapGraph, Consumer<HelperEdgeDraw> consumer) {
        Objects.requireNonNull(mapProvider);
        Objects.requireNonNull(mapGraph);
        Objects.requireNonNull(consumer);
        this.mapProvider = mapProvider;
        this.mapGraph = mapGraph;
        this.consumer = consumer;
    }
    
    public void start() {
        cancel();
        selectListener = mapGraph.addCellSelectedListener(event -> {
            MapNode mapNode = mapGraph.findNode(new BusinessObjectLight(null, event.getCellId(), null));
            if (mapNode != null) {
                if (source == null) {
                    source = mapNode;
                    mapProvider.setDrawingPolylineMode(coordinates -> 
                        this.coordinates = coordinates
                    );
                } else if (target == null) {
                    target = mapNode;
                    consumer.accept(this);
                    cancel();
                }
            }
        });
    }
    
    public void cancel() {
        mapProvider.setHandMode();
        source = null;
        target = null;
        coordinates = null;
        if (selectListener != null) {
            selectListener.remove();
            selectListener = null;
        }
    }
    
    public MxBusinessObjectNode getSource() {
        return source;
    }
    
    public MxBusinessObjectNode getTarget() {
        return target;
    }
    
    public List<GeoCoordinate> getCoordintates() {
        return coordinates;
    }
}
