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

import com.ait.lienzo.client.core.event.NodeDragMoveEvent;
import com.ait.lienzo.client.core.event.NodeDragMoveHandler;
import com.ait.lienzo.client.core.shape.Circle;
import com.ait.lienzo.client.core.shape.Layer;
import com.ait.lienzo.client.core.shape.Rectangle;

/**
 * 
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
public class FrameWidget extends Rectangle {
    public interface ClntFrameWidgetUpdateListener {
        public void frameWidgetUpdated(FrameWidget frameWidgetUpdated);
    }
    private List<ClntFrameWidgetUpdateListener> listeners;
    
    private String caption;
    
    private final double radius = 10;
    private List<Circle> controlPoints;
    
    private Layer frameLayer;
    
    public FrameWidget(Layer frameLayer, double width, double height) {
        super(width, height);
        this.frameLayer = frameLayer; 
    }
    
    public void removeControlPoints() {
        for (Circle controlPoint : controlPoints)
            frameLayer.remove(controlPoint);
        frameLayer.batch();
    }
    
    public FrameWidget(Layer frameLayer, String caption, double x, double y, double width, double height) {
        this(frameLayer, width, height);
        setX(x);
        setY(y);
        setDraggable(true);
        
        addControlPoints();
        
        addNodeDragMoveHandler(new NodeDragMoveHandler() {
            
            @Override
            public void onNodeDragMove(NodeDragMoveEvent event) {
                updateControlPoints();
                fireFrameWidgetUpdate();
            }
        });
        setEditable(false);
    }
    	
    @Override
    public Rectangle setEditable(boolean editable) {
        super.setEditable(editable);
        for (Circle controlPoint : controlPoints)
            controlPoint.setVisible(editable);
        frameLayer.batch();
        return this;        
    }
    
    public void moveControlPoint(int index, double x, double y) {
        switch(index) {
            case 0:
                if (x >= controlPoints.get(2).getX()) {
                    controlPoints.get(0).setX(controlPoints.get(2).getX() - 32);
                    x = controlPoints.get(0).getX();
                }
                if (y >= controlPoints.get(2).getY()) {
                    controlPoints.get(0).setY(controlPoints.get(2).getY() - 32);
                    y = controlPoints.get(0).getY();
		}
            break;
            case 1:
                if (x <= controlPoints.get(3).getX()) {
                    controlPoints.get(1).setX(controlPoints.get(3).getX() + 32);
                    x = controlPoints.get(1).getX();
                }
                if (y >= controlPoints.get(3).getY()) {
                    controlPoints.get(1).setY(controlPoints.get(3).getY() - 32);
                    y = controlPoints.get(1).getY();
                }
            break;
            case 2:
                if(x <= controlPoints.get(0).getX()) {
                    controlPoints.get(2).setX(controlPoints.get(0).getX() + 32);
                    x = controlPoints.get(2).getX();
		}				
                if (y <= controlPoints.get(0).getY()) {
                    controlPoints.get(2).setY(controlPoints.get(0).getY() + 32);
                    y = controlPoints.get(2).getY();
		}
            break;
            case 3: 
                if (x >= controlPoints.get(1).getX()) {
                    controlPoints.get(3).setX(controlPoints.get(1).getX() - 32);
                    x = controlPoints.get(3).getX();
                }
                if (y <= controlPoints.get(1).getY()) {
                    controlPoints.get(3).setY(controlPoints.get(1).getY() + 32);
                    y = controlPoints.get(3).getY();
                }
            break;
	}
        double width = 0;
	double height = 0;
        
        switch(index) {
            case 0:
                controlPoints.get(1).setY(y);
                controlPoints.get(3).setX(x);
                
                width = controlPoints.get(1).getX() - x;
                height = controlPoints.get(3).getY() - y;
                
                setX(x);
                setY(y);
            break;
            case 1:
                controlPoints.get(2).setX(x);
                controlPoints.get(0).setY(y);
                
                width = x - controlPoints.get(0).getX();
                height = controlPoints.get(2).getY() - y;
                
                setY(y);		
            break;
            case 2:
                controlPoints.get(1).setX(x);
                controlPoints.get(3).setY(y);
                
                width = x - controlPoints.get(3).getX();
		height = y - controlPoints.get(1).getY();
            break;
            case 3:
                controlPoints.get(0).setX(x);
                controlPoints.get(2).setY(y);
                
		width = controlPoints.get(2).getX() - x;
                height = y - controlPoints.get(0).getY();
                
		setX(x);			
		break;		
	}
	setWidth(width);
	setHeight(height);
	
        frameLayer.batch();
	fireFrameWidgetUpdate();
    }
    
    private void addControlPoints() {
        if (controlPoints != null)
            return;
        
        controlPoints = new ArrayList<>();
        
        Circle controlPoint0 = new Circle(radius);
        controlPoint0.setVisible(false);
        controlPoint0.setDraggable(true);
        controlPoint0.setX(getX());
        controlPoint0.setY(getY());
        controlPoint0.addNodeDragMoveHandler(new NodeDragMoveHandler() {
            
            @Override
            public void onNodeDragMove(NodeDragMoveEvent event) {
                moveControlPoint(0, event.getX(), event.getY());
            }
        });
	Circle controlPoint1 = new Circle(radius);
	controlPoint1.setVisible(false);
	controlPoint1.setDraggable(true);
	controlPoint1.setX(getX() + getWidth());
	controlPoint1.setY(getY());
	controlPoint1.addNodeDragMoveHandler(new NodeDragMoveHandler() {
            
            @Override
            public void onNodeDragMove(NodeDragMoveEvent event) {
                moveControlPoint(1, event.getX(), event.getY());
            }
        });
        Circle controlPoint2 = new Circle(radius);
        controlPoint2.setVisible(false);
	controlPoint2.setDraggable(true);
	controlPoint2.setX(getX() + getWidth());
	controlPoint2.setY(getY() + getHeight());
	controlPoint2.addNodeDragMoveHandler(new NodeDragMoveHandler() {
            
            @Override
            public void onNodeDragMove(NodeDragMoveEvent event) {
                moveControlPoint(2, event.getX(), event.getY());
            }			
	});
	Circle controlPoint3 = new Circle(radius);
        controlPoint3.setVisible(false);
        controlPoint3.setDraggable(true);
	controlPoint3.setX(getX());
	controlPoint3.setY(getY() + getHeight());
	controlPoint3.addNodeDragMoveHandler(new NodeDragMoveHandler() {
            
            @Override
            public void onNodeDragMove(NodeDragMoveEvent event) {
                moveControlPoint(3, event.getX(), event.getY());
            }
	});
        
        controlPoints.add(controlPoint0);
	controlPoints.add(controlPoint1);
	controlPoints.add(controlPoint2);
	controlPoints.add(controlPoint3);
	
	frameLayer.add(controlPoint0);
	frameLayer.add(controlPoint1);
	frameLayer.add(controlPoint2);
	frameLayer.add(controlPoint3);
		
	frameLayer.batch();
    }
    
    private void updateControlPoints() {
        if (controlPoints == null)
            return;
        
        controlPoints.get(0).setX(getX());
	controlPoints.get(0).setY(getY());
	
	controlPoints.get(1).setX(getX() + getWidth());
	controlPoints.get(1).setY(getY());
	
	controlPoints.get(2).setX(getX() + getWidth());
	controlPoints.get(2).setY(getY() + getHeight());
	
	controlPoints.get(3).setX(getX());
	controlPoints.get(3).setY(getY() + getHeight());
	
	frameLayer.batch();		
    }
    
    public String getCaption() {
        return caption;		
    }
    
    public void setCaption(String caption) {
        this.caption = caption;
    }
    
    public void addFrameWidgetUpdateListener(ClntFrameWidgetUpdateListener listener) {
        if (listeners == null)
            listeners = new ArrayList<>();
        listeners.add(listener);					
    }
    
    public void removeFrameWidgetUpdateListener(ClntFrameWidgetUpdateListener listener) {
        if (listeners == null)
            return;
        listeners.remove(listener);
    }
    
    private void fireFrameWidgetUpdate() {
        if (listeners == null)
            return;
	for (ClntFrameWidgetUpdateListener listener : listeners)
            listener.frameWidgetUpdated(this);
    }
}
