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

package com.neotropic.tools.dbmigration.views;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.neo4j.graphdb.RelationshipType;

/**
 * This class contains useful enumerations and definitions that are shared by all the migrators.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ViewUtil {
    public static final RelationshipType RELTYPE_HASVIEW = new RelationshipType() {
                    @Override
                    public String name() {
                        return "HAS_VIEW";
                    }
                };
    
    public static final RelationshipType RELTYPE_INSTANCEOF = new RelationshipType() {
                    @Override
                    public String name() {
                        return "INSTANCE_OF";
                    }
                };
    
    /**
     * Class that represents the nodes and edges structure of the view as a Java object.
     */
    public static class ViewMap {
        /**
         * The class of the detected view. The class differentiates Object, End to End, Topology, etc views.
         */
        private String viewClass;
        /**
         * The nodes in the view as Java objects.
         */
        private List<ViewNode> nodes;
        /**
         * The list of connections in the view.
         */
        private List<ViewEdge> edges;

        public ViewMap() {
            this.nodes = new ArrayList<>();
            this.edges = new ArrayList<>();
        }

        public List<ViewNode> getNodes() {
            return nodes;
        }

        public void setNodes(List<ViewNode> nodes) {
            this.nodes = nodes;
        }

        public List<ViewEdge> getEdges() {
            return edges;
        }

        public void setEdges(List<ViewEdge> edges) {
            this.edges = edges;
        }

        public String getViewClass() {
            return viewClass;
        }

        public void setViewClass(String viewClass) {
            this.viewClass = viewClass;
        }
    }
    
    /**
     * A class representing a node in the view.
     */    
    public static class ViewNode { 
        /**
         * Legacy business object id.
         */
        private long id;
        /**
         * business object uuid.
         */
        private String uuid;
        /**
         * Business object class.
         */
        private String className;
        /**
         * The Cartesian position of the node.
         */
        private Point position;

        public ViewNode(long id, String className, Point position) {
            this.id = id;
            this.className = className;
            this.position = position;
        }

        public ViewNode(String uuid, String className, Point position) {
            this.uuid = uuid;
            this.className = className;
            this.position = position;
        }
        
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public Point getPosition() {
            return position;
        }

        public void setPosition(Point position) {
            this.position = position;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
    }
    
    /**
     * A class representing a connection in the view.
     */
    public static class ViewEdge {
        /**
         * Legacy business object id.
         */
        private long id;
        /**
         * business object uuid.
         */
        private String uuid;
        /**
         * Business object class.
         */
        private String className;
        /**
         * The list of controlpoints of this connection.
         */
        private List<Point> controlPoints;
        /**
         * The legacy id of one end of the connection.
         */
        private long aSide;
        /**
         * The legacy id of the other end of the connection.
         */
        private long bSide;
        /**
         * uuid of one end of the connection.
         */
        private String aSideUuid;
        /**
         * uuid of the other end of the connection.
         */
        private String bSideUuid;

        public ViewEdge(long id, String className, long aSide, long bSide) {
            this.id = id;
            this.className = className;
            this.aSide = aSide;
            this.bSide = bSide;
            this.controlPoints = new ArrayList<>();
        }

        public ViewEdge(String uuid, String className, String aSideUuid, String bSideUuid) {
            this.uuid = uuid;
            this.className = className;
            this.aSideUuid = aSideUuid;
            this.bSideUuid = bSideUuid;
            this.controlPoints = new ArrayList<>();
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getClassName() {
            return className;
        }

        public void setClassName(String className) {
            this.className = className;
        }

        public List<Point> getControlPoints() {
            return controlPoints;
        }

        public void setControlPoints(List<Point> controlPoints) {
            this.controlPoints = controlPoints;
        }

        public long getaSide() {
            return aSide;
        }

        public void setaSide(long aSide) {
            this.aSide = aSide;
        }

        public long getbSide() {
            return bSide;
        }

        public void setbSide(long bSide) {
            this.bSide = bSide;
        }

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getaSideUuid() {
            return aSideUuid;
        }

        public void setaSideUuid(String aSideUuid) {
            this.aSideUuid = aSideUuid;
        }

        public String getbSideUuid() {
            return bSideUuid;
        }

        public void setbSideUuid(String bSideUuid) {
            this.bSideUuid = bSideUuid;
        }
    }

    /**
     * Dummy exception that is thrown when a view that is not an Object View is detected.
     */
    public static class OtherKinfOfViewException extends Exception {

        public OtherKinfOfViewException(String msg) {
            super(msg);
        }
        
    }
    
    /**
     * Dummy exception that is thrown when a a node that is reusing the id of an old object has been detected.
     */
    public static class NodeIdReusedException extends Exception {

        public NodeIdReusedException(String msg) {
            super(msg);
        }
        
    }
    
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
