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

import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider.OSPNode;
import org.neotropic.util.visual.tools.Tool;

/**
 * Outside plant toolset
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OspToolset {
    /**
     * Outside plant tool hand
     */
    public static class ToolHand extends Tool { }
    /**
     * Outside plant tool marker
     */
    public static class ToolMarker extends Tool { }
    /**
     * Outside plant tool polygon
     */
    public static class ToolPolygon extends Tool { }
    /**
     * Outside plant tool polyline
     */
    public static class ToolPolyline extends Tool { }
    /**
     * Outside plant tool to bounce a marker
     */
    public static class ToolBounceMarker extends Tool {
        /**
         * Node to bounce
         */
        private OSPNode bounceNode;
        
        public OSPNode getBounceNode() {
            return bounceNode;
        }
        
        public void setBounceNode(OSPNode bounceNode) {
            this.bounceNode = bounceNode;
        }
    }
    /**
     * Outside plant tool to change the map center
     */
    public static class ToolChangeMapCenter extends Tool {
        /**
         * New map center
         */
        private GeoCoordinate newCenter;
        
        public GeoCoordinate getNewCenter() {
            return newCenter;
        }
        
        public void setNewCenter(GeoCoordinate newCenter) {
            this.newCenter = newCenter;
        }
    }
}
