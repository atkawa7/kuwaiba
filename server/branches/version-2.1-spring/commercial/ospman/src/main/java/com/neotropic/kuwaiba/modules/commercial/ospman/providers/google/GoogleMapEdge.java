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
package com.neotropic.kuwaiba.modules.commercial.ospman.providers.google;

import com.neotropic.flow.component.googlemap.GoogleMapPolyline;
import com.neotropic.flow.component.googlemap.LatLng;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.ClickEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.GeoCoordinate;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapConstants;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.PathChangedEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.RightClickEvent;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.MetadataObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.metadata.MetadataEntityManager;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;
import org.neotropic.util.visual.notifications.AbstractNotification;
import org.neotropic.util.visual.notifications.SimpleNotification;
import org.neotropic.util.visual.views.util.UtilHtml;

/**
 * An edge wrapper to Google Map Polyline
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapEdge extends GoogleMapPolyline implements MapEdge {
    private final BusinessObjectViewEdge viewEdge;
    private boolean clickable = true;
    
    private final List<ClickEvent.ClickEventListener> clickEventListeners = new ArrayList();
    private final List<RightClickEvent.RightClickEventListener> rightClickEventListeners = new ArrayList();
    private final List<PathChangedEvent.PathChangedEventListener> pathChangedEventListeners = new ArrayList();
        
    public GoogleMapEdge(BusinessObjectViewEdge viewEdge, MetadataEntityManager mem, TranslationService ts) {
        Objects.requireNonNull(viewEdge);
        Objects.requireNonNull(mem);
        Objects.requireNonNull(ts);
        
        this.viewEdge = viewEdge;
        List<GeoCoordinate> controlPoints = (List) viewEdge.getProperties().get(MapConstants.PROPERTY_CONTROL_POINTS);
        
        if (controlPoints != null && !controlPoints.isEmpty()) {
            try {
                setStrokeColor(UtilHtml.toHexString(new Color(mem.getClass(viewEdge.getIdentifier().getClassName()).getColor())));
            } catch (MetadataObjectNotFoundException ex) {
                new SimpleNotification(
                    ts.getTranslatedString("module.general.messages.error"), 
                    ts.getTranslatedString("module.general.messages.unexpected-error"), 
                    AbstractNotification.NotificationType.ERROR, ts
                ).open();
            }
            List<LatLng> path = new ArrayList();
            controlPoints.forEach(controlPoint -> 
                path.add(new LatLng(controlPoint.getLatitude(), controlPoint.getLongitude()))
            );
            setPath(path);
            setStrokeWeight(MapConstants.EDGE_STROKE_WEIGHT);
            
            addPolylineMouseOverListener(event -> setStrokeWeight(MapConstants.EDGE_STROKE_WEIGHT_MOUSE_OVER));
            addPolylineMouseOutListener(event -> setStrokeWeight(MapConstants.EDGE_STROKE_WEIGHT));
            
            addPolylinePathChangedListener(event -> {
                List<GeoCoordinate> geoCoordinates = new ArrayList();
                getPath().forEach(latLng -> geoCoordinates.add(
                    new GeoCoordinate(latLng.getLat(), latLng.getLng()))
                );
                new ArrayList<>(pathChangedEventListeners).forEach(listener -> {
                    if (pathChangedEventListeners.contains(listener))
                        listener.accept(new PathChangedEvent(geoCoordinates, listener));
                });
            });
            addPolylineClickListener(event -> new ArrayList<>(clickEventListeners).forEach(listener -> {
                if (clickable && clickEventListeners.contains(listener))
                    listener.accept(new ClickEvent(listener));
            }));
            addPolylineRightClickListener(event -> new ArrayList<>(rightClickEventListeners).forEach(listener -> {
                if (clickable && rightClickEventListeners.contains(listener))
                    listener.accept(new RightClickEvent(listener));
            }));
        }
    
    }
    
    @Override
    public BusinessObjectViewEdge getViewEdge() {
        return viewEdge;
    }
    
    @Override
    public void setControlPoints(List<GeoCoordinate> controlPoints) {
        List<LatLng> path = new ArrayList();
        if (controlPoints != null) {
            controlPoints.forEach(controlPoint -> 
                path.add(new LatLng(controlPoint.getLatitude(), controlPoint.getLongitude()))
            );
        }
        setPath(path);
    }
    
    @Override
    public boolean getClickableEdge() {
        return clickable;
    }
    
    @Override
    public void setClickableEdge(boolean clickable) {
        this.clickable = clickable;
    }
    
    @Override
    public boolean getEditableEdge() {
        return getEditable();
    }
    
    @Override
    public void setEditableEdge(boolean editable) {
        setEditable(editable);
    }

    @Override
    public void addClickEventListener(ClickEvent.ClickEventListener clickEventListener) {
        clickEventListeners.add(clickEventListener);
    }

    @Override
    public void removeClickEventListener(ClickEvent.ClickEventListener clickEventListener) {
        clickEventListeners.removeIf(l -> l.equals(clickEventListener));
    }

    @Override
    public void removeAllClickEventListeners() {
        clickEventListeners.clear();
    }

    @Override
    public void addRightClickEventListener(RightClickEvent.RightClickEventListener rightClickEventListener) {
        rightClickEventListeners.add(rightClickEventListener);
    }

    @Override
    public void removeRightClickEventListener(RightClickEvent.RightClickEventListener rightClickEventListener) {
        rightClickEventListeners.removeIf(l -> l.equals(rightClickEventListener));
    }

    @Override
    public void removeAllRightClickEventListeners() {
        rightClickEventListeners.clear();
    }
    
    @Override            
    public void addPathChangedEventListener(PathChangedEvent.PathChangedEventListener pathChangedEventListener) {
        pathChangedEventListeners.add(pathChangedEventListener);
    }
    
    @Override
    public void removePathChangedEventListener(PathChangedEvent.PathChangedEventListener pathChangedEventListener) {
        pathChangedEventListeners.removeIf(l -> l.equals(pathChangedEventListener));
    }
    
    @Override
    public void removeAllPathChangedEventListeners() {
        pathChangedEventListeners.clear();
    }
}
