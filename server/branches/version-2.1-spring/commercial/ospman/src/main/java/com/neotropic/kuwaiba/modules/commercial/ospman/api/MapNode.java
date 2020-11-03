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
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.server.StreamResourceRegistry;
import java.util.LinkedHashMap;
import java.util.Objects;
import org.neotropic.kuwaiba.modules.core.navigation.resources.ResourceFactory;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectNode;

/**
 * Class to configure the nodes in the map
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MapNode extends MxBusinessObjectNode {
    private boolean detached = false;
    private boolean added = false;
    
    public MapNode(BusinessObjectViewNode viewNode, double x, double y, MapOverlay mapOverlay, ResourceFactory resourceFactory) {
        super(viewNode.getIdentifier());
        Objects.requireNonNull(viewNode);
        Objects.requireNonNull(resourceFactory);
        Objects.requireNonNull(mapOverlay);
        
        setUuid(viewNode.getIdentifier().getId());
        setLabel(viewNode.getIdentifier().getName());
        setGeometry((int) x, (int) y, 16, 16);
        
        LinkedHashMap<String, String> styles = new LinkedHashMap();
        styles.put(
            MxConstants.STYLE_IMAGE, 
            StreamResourceRegistry.getURI(resourceFactory.getClassIcon(viewNode.getIdentifier().getClassName())).toString()
        );
        styles.put(MxConstants.STYLE_SHAPE, MxConstants.SHAPE_IMAGE);
        styles.put(MxConstants.STYLE_RESIZABLE, String.valueOf(0));
        this.setRawStyle(styles);
        
        addCellAddedListener(event -> {
            added = true;
            event.unregisterListener();
        });
        addCellPositionChangedListener(event -> {
            if (mapOverlay != null) {
                mapOverlay.getProjectionFromLatLngToDivPixel(mapOverlay.getBounds().getSouthwest(), sw -> {
                    mapOverlay.getProjectionFromLatLngToDivPixel(mapOverlay.getBounds().getNortheast(), ne -> {
                        mapOverlay.getProjectionFromDivPixelToLatLng(new GeoPoint(sw.getX() + getX(), ne.getY() + getY()), coordinate -> {
                            viewNode.getProperties().put(MapConstants.ATTR_LAT, coordinate.getLatitude());
                            viewNode.getProperties().put(MapConstants.ATTR_LON, coordinate.getLongitude());
                        });
                    });
                });
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        this.detached = true;
    }
    
    public boolean getAdded() {
        return added;
    }
    
    public boolean getDetached() {
        return detached;
    }
}
