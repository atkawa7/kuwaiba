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
package com.neotropic.flow.component.mxgraph;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.shared.Registration;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Orlando Paz Duarte {@literal <orlando.paz@kuwaiba.org>}
 */
@Tag("mx-graph-cell")
@JsModule("./mx-graph/mx-graph-cell.js")
public class MxGraphCell extends Component implements HasComponents {
    
    public static final String PROPERTY_UUID = "uuid";   
    public static final String PROPERTY_SOURCE = "source";
    public static final String PROPERTY_TARGET = "target";   
    public static final String PROPERTY_EDGE = "edge";
    public static final String PROPERTY_LAYER = "layer";
    public static final String PROPERTY_VERTEX = "vertex";
    public static final String PROPERTY_SOURCE_LABEL = "sourceLabel";
    public static final String PROPERTY_TARGET_LABEL = "targetLabel";
    public static final String PROPERTY_IMAGE = "image";
    public static final String PROPERTY_LABEL = "label";
    public static final String PROPERTY_WIDTH = "width";
    public static final String PROPERTY_HEIGHT = "height";
    public static final String PROPERTY_X = "x";    
    public static final String PROPERTY_Y = "y";
    public static final String PROPERTY_POINTS = "points";    
    public static final String PROPERTY_STROKE_WIDTH = "strokeWidth";    
    public static final String PROPERTY_LABEL_BACKGROUND_COLOR = "labelBackgroundColor";
    public static final String PROPERTY_PERIMETER_SPACING = "perimeterSpacing";
    public static final String PROPERTY_STROKE_COLOR = "strokeColor";    
    public static final String PROPERTY_FONT_COLOR = "fontColor";
    public static final String PROPERTY_CURVED = "curved";
    public static final String PROPERTY_DASHED = "dashed";
    public static final String PROPERTY_CELL_LAYER = "cellLayer";
    public static final String PROPERTY_CELL_PARENT = "cellParent";
    public static final String PROPERTY_STYLE_NAME = "styleName";
        
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
        
    public void setSource(String sourceId) {
        getElement().setProperty(PROPERTY_SOURCE, sourceId);
    }
    
    public String getTarget() {
        return getElement().getProperty(PROPERTY_TARGET);
    }
        
    public void setTarget(String targetId){
        getElement().setProperty(PROPERTY_TARGET, targetId);
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
    
    public boolean getIsLayer() {
        return getElement().getProperty(PROPERTY_LAYER,false);
    }
        
    public void setIsLayer(boolean layerId) {
        getElement().setProperty(PROPERTY_LAYER, layerId);
    }
    
    public String getCellLayer() {
        return getElement().getProperty(PROPERTY_CELL_LAYER);
    }
        
    public void setCellLayer(String layerId) {
        getElement().setProperty(PROPERTY_CELL_LAYER, layerId);
    }
    
    public String getCellParent() {
        return getElement().getProperty(PROPERTY_CELL_PARENT);
    }
        
    public void setCellParent(String cellId) {
        getElement().setProperty(PROPERTY_CELL_PARENT, cellId);
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
    
    @Synchronize(property = "points", value = "points-changed")
    public List<Point> getPointList() {
        String points = getElement().getProperty(PROPERTY_POINTS);
        if (points != null){
        Gson gson = new Gson();
        Type pointType = new TypeToken<ArrayList<Point>>() {}.getType(); 
        ArrayList<Point> listPoints = gson.fromJson(points, pointType);
        return listPoints == null ? new ArrayList<>() : listPoints;
        } else
            return new ArrayList<>();
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
    
    public boolean isDashed() {
        return getElement().getProperty(PROPERTY_DASHED).equals("1");
    }
        
    public void setIsDashed(Boolean prop) {
        getElement().setProperty(PROPERTY_DASHED, prop ? "1" : "0");
    }
    
    public boolean isCurved() {
        return getElement().getProperty(PROPERTY_CURVED).equals("1");
    }
        
    public void setIsCurved(Boolean prop) {
        getElement().setProperty(PROPERTY_CURVED, prop ? "1" : "0");
    }
    
    public String getStyleName() {
        return getElement().getProperty(PROPERTY_STYLE_NAME);
    }
        
    public void setStyleName(String prop) {
        getElement().setProperty(PROPERTY_STYLE_NAME, prop);
    }
    
    public void setFillColor(String prop) {
        getElement().setProperty(MxConstants.STYLE_FILLCOLOR, prop);
    }
    
    public void setShape(String prop) {
        getElement().setProperty(MxConstants.STYLE_SHAPE, prop);
    }
    
     public void setVerticalLabelPosition(String prop) {
        getElement().setProperty(MxConstants.STYLE_VERTICAL_LABEL_POSITION, prop);
    }
     
    public boolean isMovable() {
        return getElement().getProperty(MxConstants.STYLE_MOVABLE).equals("1");
    }
        
    public void setIsMovable(Boolean prop) {
        getElement().setProperty(MxConstants.STYLE_MOVABLE, prop ? "1" : "0");
    }
    
    public void setMovable(Boolean prop) {
           getElement().callJsFunction("setMovable", prop ? "1" : "0");
    }
    
    public void setGeometry(int x, int y, int width, int height) {
        setX(x);
        setY(y);
        setHeight(height);
        setWidth(width);
    }
    
    public void addCell(MxGraphCell mxGraphCell) {
//        getElement().appendChild(mxGraphCell.getElement());   
        add(mxGraphCell);
    }
    
    public Registration addClickEdgeListener(ComponentEventListener<MxGraphClickEdgeEvent> clickEdgeListener) {
        return super.addListener(MxGraphClickEdgeEvent.class, clickEdgeListener);
    }
    
    public Registration addCellPositionChangedListener(ComponentEventListener<MxGraphCellPositionChanged> eventListener) {
        return super.addListener(MxGraphCellPositionChanged .class, eventListener);
    }
    
    public Registration addCellAddedListener(ComponentEventListener<MxGraphCellAddedEvent> eventListener) {
        return super.addListener(MxGraphCellAddedEvent.class, eventListener);
    }
    
    public void addPoint(MxGraphPoint mxGraphPoint) {
        getElement().appendChild(mxGraphPoint.getElement());     
    }
     
    public void updatePosition() {
         getElement().callJsFunction("updatePosition");
     }
     
    public void toggleVisibility() {
         getElement().callJsFunction("toggleVisibility");
     }
}
