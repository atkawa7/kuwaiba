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

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * The data model elements necessary to parse a layout.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LayoutUtil {
    /**
     * A set of shapes forming a real-life representation of a device.
     */
    public static class LayoutMap {
        /**
         * Layout relative coordinates.
         */
        private Point coordinates;
        /**
         * Layout relative dimensions.
         */
        private Dimension dimensions;
        /**
         * The shapes in the layout.
         */
        private List<Shape> shapes;

        public LayoutMap() {
            this.shapes = new ArrayList<>();
        }

        public Point getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Point coordinates) {
            this.coordinates = coordinates;
        }

        public Dimension getDimensions() {
            return dimensions;
        }

        public void setDimensions(Dimension dimensions) {
            this.dimensions = dimensions;
        }

        public List<Shape> getShapes() {
            return shapes;
        }

        public void setShapes(List<Shape> shapes) {
            this.shapes = shapes;
        }
    }
    
    /**
     * A reusable shape.
     */
    public abstract static class Shape {
        /**
         * Relative coordinates.
         */
        protected Point coordinates;
        /**
         * Relative dimensions.
         */
        protected Dimension dimensions;
        /**
         * Is the shape opaque?
         */
        protected boolean opaque;
        /**
         * Is the current shape a whole chassis/device or a part of it?
         */
        protected boolean isEquipment;
        /**
         * The optional name of the shape.
         */
        protected String name;
        /**
         * Shape background color.
         */
        protected int color;
        /**
         * Shape border color.
         */
        protected int borderColor;

        public Shape(Point coordinates, Dimension dimensions, boolean opaque, boolean isEquipment, String name, int color, int borderColor) {
            this.coordinates = coordinates;
            this.dimensions = dimensions;
            this.opaque = opaque;
            this.isEquipment = isEquipment;
            this.name = name;
            this.color = color;
            this.borderColor = borderColor;
        }

        public Point getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(Point coordinates) {
            this.coordinates = coordinates;
        }

        public Dimension getDimensions() {
            return dimensions;
        }

        public void setDimensions(Dimension dimensions) {
            this.dimensions = dimensions;
        }

        public boolean isOpaque() {
            return opaque;
        }

        public void setOpaque(boolean opaque) {
            this.opaque = opaque;
        }

        public boolean isIsEquipment() {
            return isEquipment;
        }

        public void setIsEquipment(boolean isEquipment) {
            this.isEquipment = isEquipment;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getBorderColor() {
            return borderColor;
        }

        public void setBorderColor(int borderColor) {
            this.borderColor = borderColor;
        }
        
        public abstract String getType();
    }
    
    public static class Rectangle extends Shape {
        /**
         * Shape type.
         */
        public static final String TYPE = "rectangle";
        /**
         * Is this shape a slot? That is, will it be able to hold things inside?
         */
        protected boolean isSlot; 

        public Rectangle(Point coordinates, Dimension dimensions, boolean opaque, boolean isEquipment, 
                String name, int color, int borderColor, boolean isSlot) {
            super(coordinates, dimensions, opaque, isEquipment, name, color, borderColor);
            this.isSlot = isSlot;
        }        
        
        public boolean isIsSlot() {
            return isSlot;
        }

        public void setIsSlot(boolean isSlot) {
            this.isSlot = isSlot;
        }

        @Override
        public String getType() {
            return TYPE;
        }
    }
    
    public static class Polygon extends Shape {
        /**
         * Shape type.
         */
        public static final String TYPE = "polygon";
        
        public Polygon(Point coordinates, Dimension dimensions, boolean opaque, boolean isEquipment, 
                String name, int color, int borderColor) {
            super(coordinates, dimensions, opaque, isEquipment, name, color, borderColor);
        }               
        
        @Override
        public String getType() {
            return TYPE;
        }
    }
    
    public static class Ellipse extends Shape {
        /**
         * Shape type.
         */
        public static final String TYPE = "ellipse";
        /**
         * Shape ellipse color.
         */
        protected int ellipseColor;
        /**
         * Shape oval color.
         */
        protected int ovalColor;

        public Ellipse(Point coordinates, Dimension dimensions, boolean opaque, boolean isEquipment, 
                String name, int color, int borderColor, int ellipseColor, int ovalColor) {
            super(coordinates, dimensions, opaque, isEquipment, name, color, borderColor);
            this.ellipseColor = ellipseColor;
            this.ovalColor = ovalColor;
        }
        
        public int getEllipseColor() {
            return ellipseColor;
        }

        public void setEllipseColor(int ellipseColor) {
            this.ellipseColor = ellipseColor;
        }

        public int getOvalColor() {
            return ovalColor;
        }

        public void setOvalColor(int ovalColor) {
            this.ovalColor = ovalColor;
        }
        
        @Override
        public String getType() {
            return TYPE;
        }
    }
    
    public static class Label extends Shape {
        /**
         * Shape type.
         */
        public static final String TYPE = "label";
        /**
         * Label text.
         */
        protected String label;
        /**
         * Text color.
         */
        protected int textColor;
        /**
         * Font size.
         */
        protected int fontSize;

        public Label(Point coordinates, Dimension dimensions, boolean opaque, boolean isEquipment, 
                String name, int color, int borderColor, String label, int textColor, int fontSize) {
            super(coordinates, dimensions, opaque, isEquipment, name, color, borderColor);
            this.label = label;
            this.textColor = textColor;
            this.fontSize = fontSize;
        }
        
        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public int getTextColor() {
            return textColor;
        }

        public void setTextColor(int textColor) {
            this.textColor = textColor;
        }

        public int getFontSize() {
            return fontSize;
        }

        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
        }
        
        @Override
        public String getType() {
            return TYPE;
        }
    }
    
    /**
     * @deprecated This shape should be reformulated or removed
     */
    public static class Container extends Shape {
        /**
         * Shape type.
         */
        public static final String TYPE = "container";

        public Container(Point coordinates, Dimension dimensions, boolean opaque, boolean isEquipment, 
                String name) {
            super(coordinates, dimensions, opaque, isEquipment, name, -1, -1);
        }
        
        @Override
        public String getType() {
            return TYPE;
        }
    }
    
    public static class CustomShape extends Shape {
        /**
         * Shape type.
         */
        public static final String TYPE = "custom";
        /**
         * The legacy id of the node that contains the custom shape information.
         */
        protected long id;
        /**
         * The class name of the shape that contains the custom shape information.
         */
        protected String className;

        public CustomShape(Point coordinates, Dimension dimensions, boolean opaque, boolean isEquipment, 
                String name, long id, String className) {
            super(coordinates, dimensions, opaque, isEquipment, name, -1, -1);
            this.id = id;
            this.className = className;
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
        
        @Override
        public String getType() {
            return TYPE;
        }
    }
}