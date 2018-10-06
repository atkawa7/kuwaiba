/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package com.neotropic.vaadin.lienzo.client.core.shape;

import java.util.ArrayList;
import java.util.List;

import com.ait.lienzo.client.core.event.NodeDragEndEvent;
import com.ait.lienzo.client.core.event.NodeDragEndHandler;
import com.ait.lienzo.client.core.event.NodeDragMoveEvent;
import com.ait.lienzo.client.core.event.NodeDragMoveHandler;
import com.ait.lienzo.client.core.event.NodeMouseDoubleClickEvent;
import com.ait.lienzo.client.core.event.NodeMouseDoubleClickHandler;
import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.PolyLine;
import com.ait.lienzo.client.core.types.Point2DArray;

/**
 *
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
public class EdgeWidget {
    /**
     * Listener to edge change in client side.
     */
    public interface ClntEdgeWidgetUpdateListener {
        public void connectionWidgetUpdated(EdgeWidget edgeWidgetUpdated);
    }
    
    private final double radius = 10;
    private List<ClntEdgeWidgetUpdateListener> listeners;
    
    private String caption;
    private String strokeColor = "BLACK";
    private double strokeWidth = 2;
    
    private Layer edgeLayer;
    private PolyLine polyLine;
    // The length of the lists of control points and points are the same
    private List<Circle> controlPoints = new ArrayList<>();
    private List<Point> points = new ArrayList<>();
    
    private boolean editable = true;
    
    private double selectionStrokeOffset = 10;
    
    public EdgeWidget(Layer edgeLayer) {
        this.edgeLayer = edgeLayer;
    }
    
    public EdgeWidget(Layer edgeLayer, String color, List<Point> coordinates) {
        this(edgeLayer);
        strokeColor = color;
        
        double sourceX = coordinates.get(0).getX();
	double sourceY = coordinates.get(0).getY();
        
        for (Point coordinate : coordinates) {		
            double x = coordinate.getX();
            double y = coordinate.getY();
            
            Circle circle = createCircleControlPoint(x, y);
            circle.setVisible(false);
            this.controlPoints.add(circle);
            
            edgeLayer.add(circle);
            
            Point point = createPointFromCircleControlPoint(circle, controlPoints);
            points.add(point);
	}
        polyLine = new PolyLine(Point2DArray.fromArrayOfDouble(arrayOfDoubleFromListOfPoints(points)));
	polyLine.setX(sourceX);
	polyLine.setY(sourceY);
	polyLine.setStrokeColor(strokeColor);
	polyLine.setStrokeWidth(strokeWidth);
        polyLine.setSelectionStrokeOffset(selectionStrokeOffset + strokeWidth);
				
	polyLine.addNodeMouseDoubleClickHandler(new NodeMouseDoubleClickHandler() {

            @Override
            public void onNodeMouseDoubleClick(NodeMouseDoubleClickEvent event) {
                newControlPoint(event.getX(), event.getY(), controlPoints);
                fireEdgeWidgetUpdate();
            }
	});
        edgeLayer.add(polyLine);
        polyLine.batch();
    }
    
    public void removePolyLine() {
        edgeLayer.remove(polyLine);
        for (Circle controlPoint : controlPoints)
            edgeLayer.remove(controlPoint);
        edgeLayer.batch();
    }
    
    public String getStrokeColor() {
        return strokeColor;
    }
    
    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
        polyLine.setStrokeColor(strokeColor);
        polyLine.batch();
    }
    
    public double getStrokeWidth() {
        return strokeWidth;
    }
    
    public void setStrokeWidth(double strokeWidth) {
        this.strokeWidth = strokeWidth;
        polyLine.setStrokeWidth(strokeWidth);
        polyLine.setSelectionStrokeOffset(selectionStrokeOffset + strokeWidth);
        polyLine.batch();
    }
    
    public boolean isEditable() {
        return editable;
    }
    
    public void setEditable(boolean editable) {
        this.editable = editable;
        
        for (Circle controlPoint : controlPoints)
            controlPoint.setVisible(editable);
        edgeLayer.batch();
    }
    
    public String getCaption() {
        return caption;
    }
    
    public void setCaption(String caption) {
        this.caption = caption;		
    }
    
    public PolyLine getPolyLine() {
        return polyLine;
    }
    
    public List<Circle> getControlPoints() {
        return controlPoints;
    }
    
    public Circle createCircleControlPoint(double x, double y) {
	Circle circle = new Circle(radius);
	circle.setX(x);
	circle.setY(y);
	circle.setDraggable(true);
	
	circle.addNodeMouseDoubleClickHandler(new NodeMouseDoubleClickHandler () {
            
            @Override
            public void onNodeMouseDoubleClick(NodeMouseDoubleClickEvent event) {
                Circle controlPoint = (Circle) event.getSource();
                edgeLayer.remove(controlPoint);
                edgeLayer.batch();
                
		int index = controlPoints.indexOf(controlPoint);
                
                controlPoints.remove(index);
                points.remove(index);
                updatePolyLine();
                fireEdgeWidgetUpdate();
		}
	});
        		
	circle.addNodeDragMoveHandler(new NodeDragMoveHandler() {
            
            @Override
            public void onNodeDragMove(NodeDragMoveEvent event) {
                ((Circle) event.getSource()).setX(event.getX());
		((Circle) event.getSource()).setY(event.getY());
				
		int circleIndex = controlPoints.indexOf(event.getSource());
				
		if (circleIndex == 0) {
                    updateHeadConnection(event.getX(), event.getY());					
                } 
                else {
                    double x0 = controlPoints.get(0).getX();
                    double y0 = controlPoints.get(0).getY();
                    
                    points.get(circleIndex).setX(event.getX() - x0);
                    points.get(circleIndex).setY(event.getY() - y0);
		}
		updatePolyLine();
            }
        });
		
	circle.addNodeDragEndHandler(new NodeDragEndHandler() {
            
            @Override
            public void onNodeDragEnd(NodeDragEndEvent event) {
                fireEdgeWidgetUpdate();																
            }
	});
		
	return circle;
    }
    
    public void newControlPoint(double cpX, double cpY, List<Circle> controlPoints) {
        
        for (int i = 0; i < controlPoints.size() - 1; i += 1) {
            Circle cp1 = controlPoints.get(i);
            Circle cp2 = controlPoints.get(i + 1);
            
            double x = cpX;
            double y = cpY;
            
            double x1 = cp1.getX();
            double y1 = cp1.getY();
            
            double x2 = cp2.getX();
            double y2 = cp2.getY();	
            
            // r = ax + by + c = 0
            // ((y2 - y1)/(x2 - x1))*x - y - ((y2 - y1)/(x2 - x1))*x1  + y1 = 0
            double a = (y2 - y1)/(x2 - x1);
            double b = -1 * 1;
            double c = -1*((y2 - y1)/(x2 - x1))*x1 + y1;
            // Distance from a point to a line
            double d = Math.abs(x*a + y*b + c)/Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
            // Is the point near to line?
            if (d <= polyLine.getStrokeWidth() + polyLine.getSelectionStrokeOffset()) {
                // bounds of line with control points cp1  and cp2
                double left, right, top, bottom;
                
                if (x1 < x2) {
                    left = x1;
                    right = x2;
                }
                else {
                    left = x2;	
                    right = x1;
                }
		
                if (y1 < y2) {
                    bottom = y1;
                    top = y2;
                }
                else {
                    bottom = y2;
                    top = y1;
                }
                // control point inside bounds of line
                if (left <= x && x <= right && bottom <= y && y <= top) {
                    Circle controlPoint = createCircleControlPoint(cpX, cpY);
                    
                    controlPoints.add(i + 1, controlPoint);
                    
                    Point point = createPointFromCircleControlPoint(controlPoint, controlPoints);
                    points.add(i + 1, point);
                    
                    updatePolyLine();
                    
                    edgeLayer.add(controlPoint);
                    controlPoint.batch();
                    return;
		}
            }
        }
    }
    
    private void updatePolyLine() {
        polyLine.setPoints(Point2DArray.fromArrayOfDouble(arrayOfDoubleFromListOfPoints(points)));
        polyLine.batch();
    }
    
    
    private Point createPointFromCircleControlPoint(Circle controlPoint, List<Circle> controlPoints) {
        Circle controlPoint0 = controlPoints.get(0);
        
        Point point = new Point();
        point.setX(controlPoint.getX() - controlPoint0.getX());
        point.setY(controlPoint.getY() - controlPoint0.getY());
        
        return point;
    }
    
    public double [] arrayOfDoubleFromListOfPoints(List<Point> points) {		
        
        double [] arrayOfDouble = new double [points.size() * 2];
        
        int i = -1;
        
        for (Point point : points) {
            i += 1;
            arrayOfDouble[i] = point.getX();
            i += 1;            
            arrayOfDouble[i] = point.getY();
        }
	
	return arrayOfDouble;
    }
    // Update first point and control point 
    public void updateHeadConnection(double x, double y) {
        // Changing the source of coordinates
	polyLine.setX(x);
	polyLine.setY(y);
		
	Circle controlPoint = controlPoints.get(0);
	controlPoint.setX(x);
	controlPoint.setY(y);

	// Moving points
	for (int i = 0; i < controlPoints.size(); i += 1) {
            points.get(i).setX(controlPoints.get(i).getX() - x);
            points.get(i).setY(controlPoints.get(i).getY() - y);	
	}
		
	updatePolyLine();
	fireEdgeWidgetUpdate();
    }
    // Update last point and control point
    public void updateTailConnection(double x, double y) {
        int lastIndex = controlPoints.size() - 1;
        updateConnection(x, y, lastIndex);	
        fireEdgeWidgetUpdate();
    }
    
    private void updateConnection(double x, double y, int index) {
        Circle controlPoint = controlPoints.get(index); 
        controlPoint.setX(x);
        controlPoint.setY(y);
        Point newPoint = createPointFromCircleControlPoint(controlPoint, controlPoints);
        points.set(index, newPoint);
        updatePolyLine();
    }
    
    public Point connectionMidControlPoint() {
        int size = controlPoints.size();
        
        Point midCp = new Point();
		
	double x2 = ((Circle) controlPoints.get((size / 2))).getX();
	double y2 = ((Circle) controlPoints.get((size / 2))).getY();
		
	if (size % 2 == 0) {
            double x1 = ((Circle) controlPoints.get((size / 2) - 1)).getX();
            double y1 = ((Circle) controlPoints.get((size / 2) - 1)).getY();
            
            midCp.setX((x1 + x2) / 2);
            midCp.setY((y1 + y2) / 2);
	}
        else {
            midCp.setX(x2);
            midCp.setY(y2);
	}
        return midCp;
    }
    
    public void addEdgeWidgetUpdateListener(ClntEdgeWidgetUpdateListener listener) {
        if (listeners == null)
            listeners = new ArrayList<>();
        
        listeners.add(listener);
    }
    
    public void removeEdgetWidgetUpdateListener(ClntEdgeWidgetUpdateListener listener) {
        if (listener == null)
            return;
        listeners.remove(listener);
    }
    
    private void fireEdgeWidgetUpdate() {
        if (listeners == null)
            return;
        
        for (ClntEdgeWidgetUpdateListener listener : listeners)
            listener.connectionWidgetUpdated(this);
    }
}

