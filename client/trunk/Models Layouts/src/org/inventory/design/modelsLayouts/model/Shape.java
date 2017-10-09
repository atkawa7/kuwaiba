/**
 *  Copyright 2010-2017, Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.inventory.design.modelsLayouts.model;

import java.awt.Color;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class Shape implements Transferable {    
    public static String PROPERTY_NAME = "name";
    public static String PROPERTY_X = "x";
    public static String PROPERTY_Y = "y";
    public static String PROPERTY_WIDTH = "width";
    public static String PROPERTY_HEIGHT = "height";
    public static String PROPERTY_COLOR = "color";
    public static String PROPERTY_BORDER_WIDTH = "borderWidth";
    public static String PROPERTY_BORDER_COLOR = "borderColor";
    public static Integer DEFAULT_WITH = 10;
    public static Integer DEFAULT_HEIGHT = 10;
    private static long counter = 0;
    public static DataFlavor DATA_FLAVOR = new DataFlavor(Shape.class, "DrawObject/Shape");
        
    private String urlIcon;
    private Shape parent;
    private long id;
    
    private String name = null;
    private Integer x = null;
    private Integer y = null;
    private Integer width = null;
    private Integer height = null;
    private Color color = Color.WHITE;
    private Integer borderWidth = 4;
    private Color borderColor = Color.BLACK;
    
    private final List<PropertyChangeListener> propertyChangeListeners;
    
    public Shape() {
        propertyChangeListeners = new ArrayList();
        
        id = counter;
        counter += 1;
    }
    
    public Shape(Shape parent) {
        this();
        this.parent = parent;
    }
    
    public Shape(String urlIcon) {
        this();
        this.urlIcon = urlIcon;
    }
        
    public Shape(String urlIcon, Shape parent) {
        this(urlIcon);
        this.parent = parent;
    }
    
    public Shape shapeCopy() {
        Shape shapeCpy = new Shape();
        shapeCopy(shapeCpy);
        return shapeCpy;
    }
    
    protected void shapeCopy(Shape shapeCpy) {        
        shapeCpy.setColor(this.getColor());
        shapeCpy.setHeight(this.getHeight());
        shapeCpy.setParent(this.getParent());
        shapeCpy.setWidth(this.getWidth());
        shapeCpy.setX(this.getX());
        shapeCpy.setY(this.getY());
    }
        
    public long getId() {
        return id;
    }
    
    public Shape getParent() {
        return parent;        
    }
    
    public void setParent(Shape parent) {
        this.parent = parent;        
    }
    
    public String getUrlIcon() {
        return urlIcon;
    }

    public static long getCounter() {
        return counter;
    }

    public static void setCounter(long counter) {
        Shape.counter = counter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getX() {        
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    
    public Integer getBorderWidth() {
        return borderWidth;
    }
    
    public void setBorderWidth(Integer borderWidth) {
        this.borderWidth = borderWidth;                
    }
    
    public Color getBorderColor() {
        return borderColor;
    }
    
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeListeners.add(pcl);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener pcl) {
        propertyChangeListeners.remove(pcl);
    }
    
    public void removeAllPropertyChangeListeners() {
        propertyChangeListeners.clear();
    }
    
    public void firePropertyChange(Object source, String property, Object oldValue, Object newValue) {
        synchronized(propertyChangeListeners) {            
            for (PropertyChangeListener listener : propertyChangeListeners) {
                if (!listener.equals(source))
                    listener.propertyChange(new PropertyChangeEvent(this, property, oldValue, newValue));
            }
        }
    }
    
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor == DATA_FLAVOR;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if(flavor == DATA_FLAVOR)
            return this;
        else
            throw new UnsupportedFlavorException(flavor);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Shape))
            return false;
        
        return this.getId() == ((Shape) obj).getId();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (int) (this.id ^ (this.id >>> 32));
        return hash;
    }
}
