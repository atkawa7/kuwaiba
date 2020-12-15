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

import java.util.List;
import org.neotropic.kuwaiba.visualization.api.BusinessObjectViewEdge;

/**
 * Edge to add in the Outside Plant View.
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public interface MapEdge {
    /**
     * Gets the view edge.
     * @return the view edge.
     */
    BusinessObjectViewEdge getViewEdge();
    /**
     * Sets the edge control points.
     * @param controlPoints The edge control points.
     */
    void setControlPoints(List<GeoCoordinate> controlPoints);
    /**
     * Gets if the edge can receives mouse events.
     * @return If true, the edge can receives mouse events.
     */
    boolean getClickableEdge();
    /**
     * Sets if the edge can receives mouse events.
     * @param clickable True to receives mouse events.
     */
    void setClickableEdge(boolean clickable);
    /**
     * Gets if the edge can be edited.
     * @return If true, the edge can be edited.
     */
    boolean getEditableEdge();
    /**
     * Sets if the edge can be edited.
     * @param editable True to edit the edge.
     */
    void setEditableEdge(boolean editable);
    /**
     * Adds a click event listener.
     * @param clickEventListener Callback executed on edge click.
     */
    void addClickEventListener(ClickEvent.ClickEventListener clickEventListener);
    /**
     * Removes a click event listener.
     * @param clickEventListener Callback executed on edge click.
     */
    void removeClickEventListener(ClickEvent.ClickEventListener clickEventListener);
    /**
     * Removes all click event listener.
     */
    void removeAllClickEventListeners();
    /**
     * Adds a right click event listener.
     * @param rightClickEventListener Callback executed on edge right click.
     */
    void addRightClickEventListener(RightClickEvent.RightClickEventListener rightClickEventListener);
    /**
     * Removes a right click event listener.
     * @param rightClickEventListener Callback executed on edge right click.
     */
    void removeRightClickEventListener(RightClickEvent.RightClickEventListener rightClickEventListener);
    /**
     * Removes all right click event listener.
     */
    void removeAllRightClickEventListeners();
    /**
     * Adds a path changed event listener.
     * @param pathChangedEventListener Callback executed on edge path changed.
     */
    void addPathChangedEventListener(PathChangedEvent.PathChangedEventListener pathChangedEventListener);
    /**
     * Removes a path changed event listener.
     * @param pathChangedEventListener Callback executed on edge path changed.
     */
    void removePathChangedEventListener(PathChangedEvent.PathChangedEventListener pathChangedEventListener);
    /**
     * Removes all path changed event listener.
     */
    void removeAllPathChangedEventListeners();
}
