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

import com.neotropic.flow.component.mxgraph.Point;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.kuwaiba.visualization.mxgraph.MxBusinessObjectEdge;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * Class to configure the edges in the map
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MapEdge extends MxBusinessObjectEdge {
    
    public MapEdge(BusinessObjectViewEdge viewEdge, BusinessObjectLight source, BusinessObjectLight target, List<Point> points,
        MetadataEntityManager mem, TranslationService ts, MapProvider mapProvider, MapOverlay mapOverlay, MapGraph mapGraph) {
        
        super(viewEdge.getIdentifier());
        Objects.requireNonNull(viewEdge);
        Objects.requireNonNull(source);
        Objects.requireNonNull(target);
        Objects.requireNonNull(mapOverlay);
        Objects.requireNonNull(mapProvider);
        
        setUuid(viewEdge.getIdentifier().getId());
        if (mapProvider.getZoom() >= mapProvider.getMinZoomForLabels())
            setLabel(viewEdge.getIdentifier().getName());
        setStrokeWidth(2);
        if (points != null)
            setPoints(points);
        
        try {
            setStrokeColor(UtilHtml.toHexString(new Color(mem.getClass(viewEdge.getIdentifier().getClassName()).getColor())));
        } catch (MetadataObjectNotFoundException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"), 
                ts.getTranslatedString("module.general.messages.unexpected-error"), 
                AbstractNotification.NotificationType.ERROR, ts
            ).open();
        }
        setSource(source.getId());
        setTarget(target.getId());
                
        addCellAddedListener(event -> {
            orderCell(true);
            event.unregisterListener();
        });
        addPointsChangedListener(event -> {
            mapOverlay.getProjectionFromLatLngToDivPixel(
                Arrays.asList(mapProvider.getBounds().getNortheast(), mapProvider.getBounds().getSouthwest()),
                pixelCoordinates -> {
                    GeoPoint sw = pixelCoordinates.remove(1);
                    GeoPoint ne = pixelCoordinates.remove(0);
                    List<GeoPoint> newGeoPoints = new ArrayList();
                    getPointList().forEach(point -> 
                        newGeoPoints.add(new GeoPoint(point.getX() + sw.getX(), point.getY() + ne.getY()))
                    );
                    mapOverlay.getProjectionFromDivPixelToLatLng(newGeoPoints, geoCoordinates -> 
                        viewEdge.getProperties().put(MapConstants.PROPERTY_CONTROL_POINTS, geoCoordinates)
                    );
                }
            );
        });
    }
    
}
