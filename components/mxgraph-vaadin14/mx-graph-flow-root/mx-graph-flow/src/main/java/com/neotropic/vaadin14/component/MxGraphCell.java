/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS, 
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *  See the License for the specific language governing permissions and 
 *  limitations under the License.
 */
package com.neotropic.vaadin14.component;

import com.google.gson.Gson;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import java.util.List;

/**
 * 
 * @author Orlando Paz Duarte {@literal <orlando.paz@kuwaiba.org>}
 */
@Tag("mx-graph-cell")
@JsModule("./mx-graph/mx-graph-cell.js")
public class MxGraphCell extends Component {
    
    private static final String PROPERTY_UUID = "uuid";   
    private static final String PROPERTY_SOURCE = "source";
    private static final String PROPERTY_TARGET = "target";   
    private static final String PROPERTY_EDGE = "edge";
    private static final String PROPERTY_VERTEX = "vertex";
    private static final String PROPERTY_SOURCE_LABEL = "sourceLabel";
    private static final String PROPERTY_TARGET_LABEL = "targetLabel";
    private static final String PROPERTY_IMAGE = "image";
    private static final String PROPERTY_LABEL = "label";
    private static final String PROPERTY_WIDTH = "width";
    private static final String PROPERTY_HEIGHT = "height";
    private static final String PROPERTY_X = "x";    
    private static final String PROPERTY_Y = "y";
    private static final String PROPERTY_POINTS = "points";    
    private static final String PROPERTY_STROKE_WIDTH = "strokeWidth";    
    private static final String PROPERTY_LABEL_BACKGROUND_COLOR = "labelBackgroundColor";
    private static final String PROPERTY_PERIMETER_SPACING = "perimeterSpacing";
    private static final String PROPERTY_STROKE_COLOR = "strokeColor";    
    private static final String PROPERTY_FONT_COLOR = "fontColor";




    public MxGraphCell() {
    }
    
    public String getUuid() {
        return getElement().getProperty(PROPERTY_UUID);
    }
        
    public void setUuid(String prop) {
        getElement().setProperty(PROPERTY_UUID, prop);
    }
    
    public String getSource() {
        return getElement().getProperty(PROPERTY_SOURCE);
    }
        
    public void setSource(String prop) {
        getElement().setProperty(PROPERTY_SOURCE, prop);
    }
    
    public String getTarget() {
        return getElement().getProperty(PROPERTY_TARGET);
    }
        
    public void setTarget(String prop) {
        getElement().setProperty(PROPERTY_TARGET, prop);
    }
    
    public String getIsVertex() {
        return getElement().getProperty(PROPERTY_VERTEX);
    }
        
    public void setIsVertex(boolean prop) {
        getElement().setProperty(PROPERTY_VERTEX, prop);
    }
    
    public boolean getIsEdge() {
        return getElement().getProperty(PROPERTY_EDGE,false);
    }
        
    public void setIsEdge(boolean prop) {
        getElement().setProperty(PROPERTY_EDGE, prop);
    }
    @Synchronize(property = "targetLabel", value = "target-label-changed")
    public String getTargetLabel() {
        return getElement().getProperty(PROPERTY_TARGET_LABEL);
    }
        
    public void setTargetLabel(String prop) {
        getElement().setProperty(PROPERTY_TARGET_LABEL, prop);
    }
    @Synchronize(property = "sourceLabel", value = "source-label-changed")
    public String getSourceLabel() {
        return getElement().getProperty(PROPERTY_SOURCE_LABEL);
    }
        
    public void setSourceLabel(String prop) {
        getElement().setProperty(PROPERTY_SOURCE_LABEL, prop);
    }
    
    public String getImage() {
        return getElement().getProperty(PROPERTY_IMAGE);
    }
        
    public void setImage(String prop) {
        getElement().setProperty(PROPERTY_IMAGE, prop);
    }
    @Synchronize(property = "label", value = "label-changed")
    public String getLabel() {
        return getElement().getProperty(PROPERTY_LABEL);
    }
        
    public void setLabel(String prop) {
        getElement().setProperty(PROPERTY_LABEL, prop);
    }
    
    public int getWidth() {
        return getElement().getProperty(PROPERTY_WIDTH,0);
    }
        
    public void setWidth(int prop) {
        getElement().setProperty(PROPERTY_WIDTH, prop);
    }
    
    public int getHeight() {
        return getElement().getProperty(PROPERTY_HEIGHT,0);
    }
        
    public void setHeight(int prop) {
        getElement().setProperty(PROPERTY_HEIGHT, prop);
    }
    @Synchronize(property = "x", value = "x-changed")
    public int getX() {
        return getElement().getProperty(PROPERTY_X, 0);
    }
        
    public void setX(int prop) {
        getElement().setProperty(PROPERTY_X, prop);
    }
    @Synchronize(property = "y", value = "y-changed")
    public int getY() {
        return getElement().getProperty(PROPERTY_Y,0);
    }
        
    public void setY(int prop) {
        getElement().setProperty(PROPERTY_Y, prop);
    }
    
    @Synchronize(property = "points", value = "points-changed")
    public String getPoints() {
        return getElement().getProperty(PROPERTY_POINTS);
    }
        
    public void setPoints(String prop) {
        getElement().setProperty(PROPERTY_POINTS, prop);
    }
    
    public void setPoints(List<Point> points) {
        if(points != null && points.size()>0) {
            String strPoints = new Gson().toJson(points);
            getElement().setProperty(PROPERTY_POINTS, strPoints);
        }      
    } 
    
    public int getStrokeWidth() {
        return getElement().getProperty(PROPERTY_STROKE_WIDTH,0);
    }
        
    public void setStrokeWidth(int prop) {
        getElement().setProperty(PROPERTY_STROKE_WIDTH, prop);
    }
    
    public String getLabelBackgroundColor() {
        return getElement().getProperty(PROPERTY_LABEL_BACKGROUND_COLOR);
    }
        
    public void setLabelBackgroundColor(String prop) {
        getElement().setProperty(PROPERTY_LABEL_BACKGROUND_COLOR, prop);
    }
    
    public int getPerimeterSpacing() {
        return getElement().getProperty(PROPERTY_PERIMETER_SPACING,0);
    }
        
    public void setPerimeterSpacing(int prop) {
        getElement().setProperty(PROPERTY_PERIMETER_SPACING, prop);
    }
    
    public String getStrokeColor() {
        return getElement().getProperty(PROPERTY_STROKE_COLOR);
    }
        
    public void setStrokeColor(String prop) {
        getElement().setProperty(PROPERTY_STROKE_COLOR, prop);
    }
    
    public String getFontColor() {
        return getElement().getProperty(PROPERTY_FONT_COLOR);
    }
        
    public void setFontColor(String prop) {
        getElement().setProperty(PROPERTY_FONT_COLOR, prop);
    }
    
    public void setGeometry(int x, int y, int width, int height) {
        setX(x);
        setY(y);
        setHeight(height);
        setWidth(width);
    }
    
    public Registration addClickEdgeListener(ComponentEventListener<MxGraphClickEdgeEvent> clickEdgeListener) {
        return super.addListener(MxGraphClickEdgeEvent.class, clickEdgeListener);
    }
    
    public Registration addCellPositionChangedListener(ComponentEventListener<MxGraphCellPositionChanged> eventListener) {
        return super.addListener(MxGraphCellPositionChanged .class, eventListener);
    }
    
     public void addPoint(MxGraphPoint mxGraphPoint) {
        getElement().appendChild(mxGraphPoint.getElement());     
    }
}
