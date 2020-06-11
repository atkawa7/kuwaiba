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

import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider.OSPEdge;
import com.neotropic.kuwaiba.modules.commercial.ospman.AbstractMapProvider.OSPNode;
import java.util.List;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.modules.core.navigation.commands.Command;
import org.neotropic.util.visual.tools.ToolRegister.ToolRegisterEvent;

/**
 * Class to wrap tool register events
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OspToolRegisterEvents {
    /**
     * Event to fire on node position changed
     */
    public static class NodePositionChangedEvent extends ToolRegisterEvent {
        private final BusinessObjectLight bussinesObject;
        private final GeoCoordinate newPosition;
        
        public NodePositionChangedEvent(BusinessObjectLight bussinesObject, GeoCoordinate newPosition) {
            this.bussinesObject = bussinesObject;
            this.newPosition = newPosition;
        }
        
        public BusinessObjectLight getBusinessObject() {
            return bussinesObject;
        }
        
        public GeoCoordinate getNewPosition() {
            return newPosition;
        }
    }
    /**
     * Event to fire on edge path changed
     */
    public static class EdgePathChangedEvent extends ToolRegisterEvent {
        private final BusinessObjectLight businessObject;
        private final List<GeoCoordinate> newPath;
        
        public EdgePathChangedEvent(BusinessObjectLight businessObject, List<GeoCoordinate> newPath) {
            this.businessObject = businessObject;
            this.newPath = newPath;
        }
        
        public BusinessObjectLight getBussinesObject() {
            return businessObject;
        }
        
        public List<GeoCoordinate> getNewPath() {
            return newPath;
        }
    }
    /**
     * This event is fired in an node info window request
     */
    public static class NodeInfoWindowRequestEvent extends ToolRegisterEvent {
        /**
         * The business object
         */
        private final OSPNode ospNode;
        
        public NodeInfoWindowRequestEvent(OSPNode ospNode) {
            this.ospNode = ospNode;
        }
        
        public OSPNode getOspNode() {
            return ospNode;
        }
    }
    /**
     * This event is fired in an edge info window request
     */
    public static class EdgeInfoWindowRequestEvent extends ToolRegisterEvent {
        /**
         * The business object
         */
        private final OSPEdge ospEdge;
        /**
         * Info window position
         */
        private final GeoCoordinate position;
        
        public EdgeInfoWindowRequestEvent(OSPEdge ospEdge, GeoCoordinate position) {
            this.ospEdge = ospEdge;
            this.position = position;
        }
        
        public OSPEdge getOspEdge() {
            return ospEdge;
        }
        
        public GeoCoordinate getPosition() {
            return position;
        }
    }
    /**
     * This event is fired when a marker is added
     */
    public static class NodeAddedEvent extends ToolRegisterEvent {
        private final GeoCoordinate position;
        
        public NodeAddedEvent(GeoCoordinate position) {
            this.position = position;
        }
        
        public GeoCoordinate getPosition() {
            return position;
        }
    }
    /**
     * This event is fired when an edge is added
     */
    public static class EdgeAddedEvent extends ToolRegisterEvent {
        private final OSPNode source;
        private final OSPNode target;
        private final List<GeoCoordinate> path;
        private final Command cmdDeleteDummyEdge;
        
        public EdgeAddedEvent(OSPNode source, OSPNode target, 
            List<GeoCoordinate> path, Command cmdDeleteDummyEdge) {
            this.source = source;
            this.target = target;
            this.path = path;
            this.cmdDeleteDummyEdge = cmdDeleteDummyEdge;
        }
        
        public OSPNode getSource() {
            return source;
        }
        
        public OSPNode getTarget() {
            return target;
        }
        
        public List<GeoCoordinate> getPath() {
            return path;
        }
        
        public Command getDeleteDummyEdgeCommand() {
            return cmdDeleteDummyEdge;
        }
    }
}
