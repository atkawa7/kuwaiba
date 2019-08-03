/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.kuwaiba.web.modules.osp;

import com.vaadin.ui.AbstractComponent;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;
import org.kuwaiba.apis.web.gui.views.ViewEventListener;

/**
 * All map provider components must extend from this class. This way, using Google Maps, Bing Maps or OpenStreet Map will be transparent for the OSP module
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractMapProvider {
    /**
     * Sets all the relevant configuration parameters so the underlying component can work properly, such as API keys, default languages, etc.
     * @param properties The configuration parameters.
     */
    public abstract void initialize(Properties properties);
    /**
     * Reconfigures the map with a new set of properties. In a way is very similar to {@link #initialize(java.util.Properties)}, but the latter is called 
     * to actually create the map, while this method assumes that the map component already exists and make sure all the provisions are taken so 
     * the new settings are reflected in the map.
     * @param properties The new set of properties.
     */
    public abstract void reload(Properties properties);
    /**
     * Adds a marker to the map.
     * @param businessObject The business object behind the marker.
     * @param position The default position of the marker.
     * @param iconUrl The URL of the marker icon.
     */
    public abstract void addMarker(BusinessObjectLight businessObject, GeoCoordinate position, String iconUrl);
    /**
     * Removes a marker from the map.
     * @param businessObject The business object behind the marker to be removed.
     */
    public abstract void removeMarker(BusinessObjectLight businessObject);
    /**
     * Adds a poly line to the map (not necessarily connected to any endpoint). No line will be added to the map if any of the endpoints are missing.
     * @param businessObject The business object behind the poly line;
     * @param sourceObject The business object behind the source marker.
     * @param targetObject The business object behind the source marker.
     * @param controlPoints The route of the poly line.
     * @param properties Misc properties used mainly to format the appearance of the edge (color, stroke, etc)
     */
    public abstract void addPolyline(BusinessObjectLight businessObject, BusinessObjectLight sourceObject, BusinessObjectLight targetObject, 
            List<GeoCoordinate> controlPoints, Properties properties);
    /**
     * Removes a poly line from the map.
     * @param businessObject The business object behind the poly line to be removed.
     */
    public abstract void removePolyline(BusinessObjectLight businessObject);
    /**
     * Removes all nodes, connections and annotations in the map.
     */
    public abstract void clear();
    /**
     * Fetches the existing markers.
     * @return The markers.
     */
    public abstract List<OSPNode> getMarkers();
    /**
     * Fetches the existing poly lines.
     * @return 
     */
    public abstract List<OSPEdge> getPolylines();
    /**
     * Gets the current zoom of the map.
     * @return The zoom value.
     */
    public abstract int getZoom();
    /**
     * Gets the current center of the map.
     * @return The coordinates of the current center of the map.
     */
    public abstract GeoCoordinate getCenter();
    /**
     * Gets the embeddable component that can be placed in a view or a dashboard widget.
     * @return The component,
     */
    public abstract AbstractComponent getComponent();
    /**
     * Adds a listener to the node click event.
     * @param ev The listener object
     */
    public abstract void addMarkerClickListener(ViewEventListener ev);
    /**
     * Adds a listener to the node right click event.
     * @param ev The listener object
     */
    public abstract void addMarkerRightClickListener(ViewEventListener ev);
    /**
     * Adds a listener to the edge click event.
     * @param ev The listener object
     */
    public abstract void addPolylineClickListener(ViewEventListener ev);
    /**
     * Adds a listener to the edge right click event.
     * @param ev The listener object
     */
    public abstract void addPolylineRightClickListener(ViewEventListener ev);
    /**
     * Clears the current lists of node and edge click listeners
     */
    public abstract void removeListeners();
    /**
     * A wrapper of a marker object in a map.
     */
    public static class OSPNode {
        /**
         * The object behind the marker.
         */
        private BusinessObjectLight businessObject;
        /**
         * The geolocation of the marker.
         */
        private GeoCoordinate location;

        public OSPNode(BusinessObjectLight businessObject, GeoCoordinate location) {
            this.businessObject = businessObject;
            this.location = location;
        }

        public BusinessObjectLight getBusinessObject() {
            return businessObject;
        }

        public void setBusinessObject(BusinessObjectLight businessObject) {
            this.businessObject = businessObject;
        }

        public GeoCoordinate getLocation() {
            return location;
        }

        public void setLocation(GeoCoordinate location) {
            this.location = location;
        }
    }
    
    /**
     * A wrapper of a poly line object in a map.
     */
    public static class OSPEdge {
        /**
         * The object behind the marker.
         */
        private BusinessObjectLight businessObject;
        /**
         * The business object behind the source node of the present connection.
         */
        private BusinessObjectLight sourceObject;
        /**
         * The business object behind the target node of the present connection.
         */
        private BusinessObjectLight targetObject;
        /**
         * The geolocation of route of the poly line.
         */
        private List<GeoCoordinate> controlPoints;

        public OSPEdge(BusinessObjectLight businessObject, BusinessObjectLight sourceObject, 
                BusinessObjectLight targetObject, List<GeoCoordinate> controlPoints) {
            this.businessObject = businessObject;
            this.sourceObject = sourceObject;
            this.targetObject = targetObject;
            this.controlPoints = controlPoints;
        }

        public BusinessObjectLight getBusinessObject() {
            return businessObject;
        }

        public void setBusinessObject(BusinessObjectLight businessObject) {
            this.businessObject = businessObject;
        }

        public List<GeoCoordinate> getControlPoints() {
            return controlPoints;
        }

        public void setControlPoints(List<GeoCoordinate> controlPoints) {
            this.controlPoints = controlPoints;
        }

        public BusinessObjectLight getSourceObject() {
            return sourceObject;
        }

        public void setSourceObject(BusinessObjectLight sourceObject) {
            this.sourceObject = sourceObject;
        }

        public BusinessObjectLight getTargetObject() {
            return targetObject;
        }

        public void setTargetObject(BusinessObjectLight targetObject) {
            this.targetObject = targetObject;
        }
    }
}
