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

import com.neotropic.flow.component.googlemap.GoogleMapMarker;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.ClickEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapConstants;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapNode;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.PositionChangedEvent;
import com.neotropic.kuwaiba.modules.commercial.ospman.api.RightClickEvent;
import com.vaadin.flow.server.StreamResourceRegistry;
import elemental.json.Json;
import elemental.json.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewNode;
import org.neotropic.kuwaiba.visualization.api.resources.ResourceFactory;

/**
 * A node wrapper to Google Map Marker
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class GoogleMapNode extends GoogleMapMarker implements MapNode {
    private final BusinessObjectViewNode viewNode;
    private boolean clickable = true;
    
    private final List<ClickEvent.ClickEventListener> clickEventListeners = new ArrayList();
    private final List<RightClickEvent.RightClickEventListener> rightClickEventListeners = new ArrayList();
    private final List<PositionChangedEvent.PositionChangedEventListener> positionChangedEventListeners = new ArrayList();
    
    public GoogleMapNode(BusinessObjectViewNode viewNode, ResourceFactory resourceFactory) {
        super(
            (double) viewNode.getProperties().get(MapConstants.ATTR_LAT), 
            (double) viewNode.getProperties().get(MapConstants.ATTR_LON)
        );
        Objects.requireNonNull(resourceFactory);
        
        this.viewNode = viewNode;
        JsonObject icon = Json.createObject();
        JsonObject labelOrigin = Json.createObject();
        labelOrigin.put("x", 20); //NOI18N
        labelOrigin.put("y", 40); //NOI18N
        
        icon.put("url", StreamResourceRegistry.getURI(resourceFactory.getClassIcon(viewNode.getIdentifier().getClassName())).toString()); //NOI18N
        icon.put("labelOrigin", labelOrigin); //NOI18N
        setIcon(icon);
        
        addMarkerPositionChangedListener(event -> new ArrayList<>(positionChangedEventListeners).forEach(listener -> {
            if (positionChangedEventListeners.contains(listener))
                listener.accept(new PositionChangedEvent(getLat(), getLng(), listener));
        }));
                
        addMarkerClickListener(event -> new ArrayList<>(clickEventListeners).forEach(listener -> {
            if (clickable && clickEventListeners.contains(listener))
                listener.accept(new ClickEvent(listener));
        }));
        addMarkerRightClickListener(event ->  new ArrayList<>(rightClickEventListeners).forEach(listener -> {
            if (clickable && rightClickEventListeners.contains(listener))
                listener.accept(new RightClickEvent(listener));
        }));
    }

    @Override
    public BusinessObjectViewNode getViewNode() {
        return viewNode;
    }
    
    @Override
    public boolean getClickableNode() {
        return clickable;
    }
    
    @Override
    public void setClickableNode(boolean clickable) {
        this.clickable = clickable;
    }
    
    @Override
    public boolean getDraggableNode() {
        return getDraggable();
    }
    
    @Override
    public void setDraggableNode(boolean draggable) {
        setDraggable(draggable);
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
    public void addPositionChangedEventListener(PositionChangedEvent.PositionChangedEventListener positionChangedEventListener) {
        positionChangedEventListeners.add(positionChangedEventListener);
    }
    
    @Override
    public void removePositionChangedEventListener(PositionChangedEvent.PositionChangedEventListener positionChangedEventListener) {
        positionChangedEventListeners.removeIf(l -> l.equals(positionChangedEventListener));
    }
    
    @Override
    public void removeAllPositionChangedEventListeners() {
        positionChangedEventListeners.clear();
    }
}
