/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package org.inventory.views.rackview.scene;

import org.inventory.views.rackview.NestedDevice;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.inventory.core.visual.scene.ObjectConnectionWidget;
import org.inventory.views.rackview.widgets.EquipmentWidget;
import org.inventory.views.rackview.widgets.PortWidget;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.router.Router;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 * A router implementation for the rack view, to define the control points in a connection
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RackViewConnectionRouter implements Router {
    /**
    An edge are build using six points, and the coordinates are given by (xA, yA, ..., xF, yF)
          a(0) -source-
          |
    c(2)__b(1)
    |
    d(3)__e(4)
          |
          f(5) -target-
    */
    private static final int RIGHT_MARGIN = 10;
    private static final int LEFT_MARGIN = 10;
    private static final int BOTTOM_MARGIN = 2;
    private static final int SPACING_EDGE = RackViewScene.STROKE_WIDTH + 2;
    
    private final LayerWidget connectionLayer;
    private final int rackUnitHeight;
    
    public RackViewConnectionRouter(LayerWidget connectionLayer, int rackUnitHeight) {
        this.connectionLayer = connectionLayer;
        this.rackUnitHeight = rackUnitHeight;
    }
    
    @Override
    public List<Point> routeConnection(ConnectionWidget connectionWidget) {
        Anchor sourceAnchor = connectionWidget.getSourceAnchor();
        Anchor targetAnchor = connectionWidget.getTargetAnchor();
                        
        if (sourceAnchor == null || targetAnchor == null)
            return Collections.emptyList(); // If the endpoints are not fixed
        
        PortWidget sourcePort = (PortWidget) sourceAnchor.getRelatedWidget();
        PortWidget targetPort = (PortWidget) targetAnchor.getRelatedWidget();
        
        Point sourcePoint = new Point(sourceAnchor.getRelatedSceneLocation().x, 
            getSceneLocation(sourcePort).y + sourcePort.getBounds().height);
        
        Point targetPoint = new Point(targetAnchor.getRelatedSceneLocation().x, 
            getSceneLocation(targetPort).y + targetPort.getBounds().height);
        // Search the source parent equipment 
        NestedDevice sourceEquipment = sourcePort;
        while (sourceEquipment.getParent() != null)
            sourceEquipment = sourceEquipment.getParent();
        // Search the target parent equipment 
        NestedDevice targetEquipment= targetPort;
        while (targetEquipment.getParent() != null)
            targetEquipment = targetEquipment.getParent();
                
        EquipmentWidget sourceEquipmentWidget = (EquipmentWidget) sourceEquipment;
        EquipmentWidget targetEquipmentWidget = (EquipmentWidget) targetEquipment;
                
        Point srcEquipmentPoint = getSceneLocation(sourceEquipmentWidget);
        Point trgEquipmentPoint = getSceneLocation(targetEquipmentWidget);
                
        List<Point> points = new ArrayList<>();
        
        if (srcEquipmentPoint != null && trgEquipmentPoint != null) {
            Rectangle srcEquipmentBounds = sourceEquipmentWidget.getBounds();
            Rectangle trgEquipmentBounds = targetEquipmentWidget.getBounds();
            
            int xC = srcEquipmentPoint.x;
            int xD = trgEquipmentPoint.x;
            
            boolean leftSide = false;
                        
            if (sourcePoint.x > (srcEquipmentPoint.x + srcEquipmentBounds.width / 2) || 
                targetPoint.x > (trgEquipmentPoint.x + trgEquipmentBounds.width / 2)) {
                
                xC += srcEquipmentBounds.width;
                xD += trgEquipmentBounds.width;
                
                xC += RIGHT_MARGIN;
                xD += RIGHT_MARGIN;
            } else {
                xC += -1 * LEFT_MARGIN;
                xD += -1 * LEFT_MARGIN;
                
                leftSide = true;
            }
            
            int yB, yC, yE, yD;
            //  TODO: change yB, yC, yE, yD with the total height when exist a implementation
            // of a view by equipments, e.g. A router load an xml configuration of the 
            // appearance of the device
            yB = yC = srcEquipmentPoint.y + (srcEquipmentBounds.height <= 2 * rackUnitHeight || !sourcePort.isNested() ? srcEquipmentBounds.height : srcEquipmentBounds.height / 2) + BOTTOM_MARGIN;
            yE = yD = trgEquipmentPoint.y + (trgEquipmentBounds.height <= 2 * rackUnitHeight || !targetPort.isNested() ? trgEquipmentBounds.height : trgEquipmentBounds.height / 2) + BOTTOM_MARGIN;
            
            Point pointB = new Point(sourcePoint.x, yB);        
            Point pointC = new Point(xC ,yC);
            Point pointD = new Point(xD ,yD);
            Point pointE = new Point(targetPoint.x, yE);

            points.add(sourcePoint);
            points.add(pointB);
            points.add(pointC);
            points.add(pointD);
            points.add(pointE);
            points.add(targetPoint);
            // Resolve collisions with other connections
            while (existCollisions(points, connectionWidget)) {
                getControlPoints(points, connectionWidget, leftSide);
            }
        } else {
            points.add(sourcePoint);
            points.add(targetPoint);
        }        
        return points;
    }
    
    private Point getSceneLocation(Widget widget) {
        if (widget != null) {
            Rectangle bounds = widget.getBounds();
            if (bounds == null)
                return null;
            Rectangle rectangle = widget.convertLocalToScene(bounds);
            return new Point(rectangle.x, rectangle.y);
        }
        return null;
    }
    
    private boolean existCollisions(List<Point> controlPoints, ConnectionWidget connectionWidget) {
        List<Widget> children = connectionLayer.getChildren();
        for (Widget child : children) {
            if (child instanceof ObjectConnectionWidget) {
                // Skip the current connection                                
                if (child.equals(connectionWidget))
                    continue;
                
                List<Point> childControlPoints = ((ObjectConnectionWidget) child).getControlPoints();
                // If the size no match is because the connection is not drawn yet                
                if (childControlPoints.size() != controlPoints.size())
                    continue;
                
                for (Point pointI : controlPoints) {
                    int indexI = controlPoints.indexOf(pointI);
                    // The first and the last point not change
                    if (indexI == 0 || indexI == controlPoints.size() - 1)
                        continue;
                    
                    for (Point pointJ : childControlPoints) {                        
                        int indexJ = childControlPoints.indexOf(pointJ);
                        if (indexJ == 0 || indexJ == childControlPoints.size() - 1)
                            continue;
                        
                        Point controlPoint = child.convertLocalToScene(pointJ);
                        
                        if (indexI == 1 || indexI == 4) {
                            if (pointI.y == controlPoint.y)
                                return true;
                        }
                        if (indexI == 2 || indexI == 3) {
                            if (pointI.x == controlPoint.x)
                                return true;
                        }
                    }
                }
            }
        }
        return false;
    }
    
    // Resolves collisions with other connections
    private void getControlPoints(List<Point> controlPoints, ConnectionWidget connectionWidget, boolean leftSide) {
        List<Widget> children = connectionLayer.getChildren();
        for (Widget child : children) {
            if (child instanceof ObjectConnectionWidget) {
                // Skip the current connection                                
                if (child.equals(connectionWidget))
                    continue;
                
                List<Point> childControlPoints = ((ObjectConnectionWidget) child).getControlPoints();
                // If the size no match is because the connection is not drawn yet                
                if (childControlPoints.size() != controlPoints.size())
                    continue;
                
                for (Point pointI : controlPoints) {
                    int indexI = controlPoints.indexOf(pointI);
                    // The first and the last point not change
                    if (indexI == 0 || indexI == controlPoints.size() - 1)
                        continue;
                    
                    for (Point pointJ : childControlPoints) {                        
                        int indexJ = childControlPoints.indexOf(pointJ);
                        if (indexJ == 0 || indexJ == childControlPoints.size() - 1)
                            continue;
                        
                        Point controlPoint = child.convertLocalToScene(pointJ);
                        
                        if (indexI == 1 || indexI == 4) {
                            if (pointI.y == controlPoint.y) {
                                pointI.setLocation(pointI.x, pointI.y + SPACING_EDGE);
                                
                                if (indexI == 1) {
                                    Point p = controlPoints.get(2);
                                    p.setLocation(
                                        p.x, 
                                        pointI.y);
                                }
                                if (indexI == 4) {
                                    Point p = controlPoints.get(3);
                                    p.setLocation(
                                        p.x, 
                                        pointI.y);
                                }
                            }
                        }
                        
                        if (indexI == 2 || indexI == 3) {
                            if (pointI.x == controlPoint.x) {
                                pointI.setLocation(
                                    pointI.x + (leftSide ? -1 * SPACING_EDGE : SPACING_EDGE), 
                                    pointI.y);
                                
                                if (indexI == 2) {
                                    Point p = controlPoints.get(3);
                                    p.setLocation(pointI.x, p.y);
                                }
                                if (indexI == 3) {
                                    Point p = controlPoints.get(2);
                                    p.setLocation(pointI.x, p.y);
                                }
                            }
                        }
                    }
                    
                }
            }
        }
    }
}
