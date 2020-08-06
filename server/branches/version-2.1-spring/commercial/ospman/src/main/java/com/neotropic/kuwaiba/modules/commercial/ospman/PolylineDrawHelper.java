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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.mxgraph.MxGraphCell;
import com.neotropic.flow.component.mxgraph.Point;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.shared.Registration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class PolylineDrawHelper {
    private MxGraphCell source;
    private List<GeoCoordinate> coordinates;
    private List<Point> points;
    private MxGraphCell target;
    private final Map map;
    private final MapOverlay mapOverlay;
    private final MxGraph graph;
    private Registration selectListener;
    private Consumer<PolylineDrawHelper> consumer;
    
    public PolylineDrawHelper(Map map, MapOverlay mapOverlay, MxGraph graph, Consumer<PolylineDrawHelper> consumer) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(mapOverlay);
        Objects.requireNonNull(graph);
        this.map = map;
        this.mapOverlay = mapOverlay;
        this.graph = graph;
        this.consumer = consumer;
    }
    
    public void start() {
        cancel();
        selectListener = graph.addCellSelectedListener(event -> {
            Iterator<Component> children = graph.getChildren().iterator();
            MxGraphCell vertex = null;
            while (children.hasNext()) {
                Component child = children.next();
                if (child instanceof MxGraphCell) {
                    MxGraphCell cell = (MxGraphCell) child;
                    if ("true".equals(cell.getIsVertex()) && event.getCellId().equals(cell.getUuid()))
                        vertex = cell;
                }
            }
            if (vertex != null) {
                if (source == null) {
                    source = vertex;
                    map.setDrawingPolylineMode(coordinates -> {
                        this.coordinates = new ArrayList(coordinates);
                        mapOverlay.getProjectionFromLatLngToDivPixel(mapOverlay.getBounds().getSouthwest(), sw -> {
                            mapOverlay.getProjectionFromLatLngToDivPixel(mapOverlay.getBounds().getNortheast(), ne -> {
                                graph.getElement().executeJs("return this.graph.view.scale").then(Double.class, scale -> {
                                    List<Point> newPoints = new ArrayList();
                                    setPoints(newPoints, coordinates, 
                                        new Point(sw.getX(), sw.getY()), 
                                        new Point(ne.getX(), ne.getY()), 
                                        scale, () -> {
                                            
                                        this.points = newPoints;
                                        map.setHandMode();
                                    });
                                });
                            });
                        });

                    });
                } else if (target == null) {
                    target = vertex;
                    consumer.accept(this);
                    cancel();
                }
            }
        });
    }
    
    public void cancel() {
        source = null;
        points = null;
        target = null;
        if (selectListener != null) {
            selectListener.remove();
            selectListener = null;
        }
        map.setHandMode();
    }
    
    public MxGraphCell getSource() {
        return source;
    }
    
    public List<GeoCoordinate> getCoordintates() {
        return coordinates;
    }
    
    public List<Point> getPoints() {
        return points;
    }
    
    public MxGraphCell getTarget() {
        return target;
    }
    
    private void setPoints(List<Point> points, List<GeoCoordinate> coordinates, Point sw, Point ne, double scale, Command cmd) {
        if (points != null && !coordinates.isEmpty()) {
            mapOverlay.getProjectionFromLatLngToDivPixel(coordinates.remove(0), point -> {
                double x = (point.getX() - sw.getX()) / scale;
                double y = (point.getY() - ne.getY()) / scale;
                points.add(new Point(x, y));
                setPoints(points, coordinates, sw, ne, scale, cmd);
            });
        }
        else
            cmd.execute();
    }
}
