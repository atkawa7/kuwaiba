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
package com.neotropic.kuwaiba.modules.commercial.ospman.api;

import com.neotropic.flow.component.mxgraph.MxConstants;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Objects;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;

/**
 * Class to configure the nodes in the map
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MapNode extends MxBusinessObjectNode {
        
    public MapNode(BusinessObjectViewNode viewNode, double x, double y, MapProvider mapProvider, MapOverlay mapOverlay, ResourceFactory resourceFactory) {
        super(viewNode.getIdentifier());
        Objects.requireNonNull(viewNode);
        Objects.requireNonNull(resourceFactory);
        Objects.requireNonNull(mapProvider);
        Objects.requireNonNull(mapOverlay);
        
        setUuid(viewNode.getIdentifier().getId());
        setGeometry((int) x, (int) y, 16, 16);
        
        LinkedHashMap<String, String> styles = new LinkedHashMap();
        styles.put(
            MxConstants.STYLE_IMAGE, 
            StreamResourceRegistry.getURI(resourceFactory.getClassIcon(viewNode.getIdentifier().getClassName())).toString()
        );
        styles.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_IMAGE);
        styles.put(MxConstants.STYLE_RESIZABLE, String.valueOf(0));
        if (viewNode.getProperties().containsKey(MapConstants.PROPERTY_CELL_EDITABLE) && 
            !(boolean) viewNode.getProperties().get(MapConstants.PROPERTY_CELL_EDITABLE))
            styles.put(MxConstants.STYLE_EDITABLE, String.valueOf(0));
        
        if (viewNode.getProperties().containsKey(MapConstants.PROPERTY_CELL_MOVABLE) && 
            !(boolean) viewNode.getProperties().get(MapConstants.PROPERTY_CELL_MOVABLE))
            styles.put(MxConstants.STYLE_MOVABLE, String.valueOf(0));
        
        this.setRawStyle(styles);
        
        if (mapProvider.getZoom() >= mapProvider.getMinZoomForLabels())
            setLabel(viewNode.getIdentifier().getName());
        
        addCellPositionChangedListener(event -> {
            mapOverlay.getProjectionFromLatLngToDivPixel(
                Arrays.asList(mapProvider.getBounds().getNortheast(), mapProvider.getBounds().getSouthwest()),
                pixelCoordinates -> {
                    GeoPoint ne = pixelCoordinates.get(0);
                    GeoPoint sw = pixelCoordinates.get(1);

                    mapOverlay.getProjectionFromDivPixelToLatLng(
                        new GeoPoint(getX() + sw.getX(), getY() + ne.getY()), geoCoordinate -> {
                        viewNode.getProperties().put(MapConstants.ATTR_LAT, geoCoordinate.getLatitude());
                        viewNode.getProperties().put(MapConstants.ATTR_LON, geoCoordinate.getLongitude());
                    });
                }
            );
        });
    }
}
