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

import com.vaadin.ui.Component;
import java.util.List;
import java.util.Properties;
import org.kuwaiba.apis.persistence.business.BusinessObjectLight;

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
     * Adds a marker to the map.
     * @param businessObject The business object behind the marker.
     * @param position The default position of the marker.
     * @param iconUrl The URL of the marker icon.
     */
    public abstract void addMarker(BusinessObjectLight businessObject, GeoCoordinate position, String iconUrl);
    /**
     * Adds a poly line to the map (not necessarily connected to any endpoint).
     * @param businessObject The business object behind the poly line;
     * @param controlPoints The route of the poly line.
     */
    public abstract void addPolyline(BusinessObjectLight businessObject, List<GeoCoordinate> controlPoints);
    /**
     * Sets the source of an existing poly line.
     * @param edge The object behind the poly line to be connected.
     * @param sourceObject The object behind the source node.
     */
    public abstract void connectPolylineSource(BusinessObjectLight edge, BusinessObjectLight sourceObject);
    /**
     * Sets the target of an existing poly line.
     * @param edge The object behind the poly line to be connected.
     * @param targetObject The object behind the target node.
     */
    public abstract void connectPolylineTarget(BusinessObjectLight edge, BusinessObjectLight targetObject);
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
    public abstract Component getComponent();
    
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
         * The geolocation of route of the poly line.
         */
        private List<GeoCoordinate> controlPoints;

        public OSPEdge(BusinessObjectLight businessObject, List<GeoCoordinate> controlPoints) {
            this.businessObject = businessObject;
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
    }
}
