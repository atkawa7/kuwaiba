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
package org.inventory.design.modelsLayouts.scene.widgets;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.inventory.design.modelsLayouts.lookup.SharedContent;
import org.inventory.design.modelsLayouts.lookup.SharedContentLookup;
import org.inventory.design.modelsLayouts.model.Shape;
import org.inventory.design.modelsLayouts.nodes.ShapeNode;
import org.inventory.design.modelsLayouts.scene.ModelLayoutScene;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.spi.palette.PaletteController;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Widget used to represent a generic shape in the scene
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class SelectableShapeWidget extends Widget implements PropertyChangeListener, SharedContentLookup {
    ShapeNode shapeNode;
    Lookup lookup;

    public SelectableShapeWidget(Scene scene, Shape shape) {
        super(scene);
        shape.addPropertyChangeListener(this);
        lookup = Lookups.fixed(shape);
        shapeNode = new ShapeNode(shape);        
    }
    
    @Override
    public Lookup fixLookup() {
        PaletteController pallete = SharedContent.getInstance().getAbstractLookup().lookup(PaletteController.class);
        
        List lst = new ArrayList();
        lst.add(shapeNode);
        lst.add(pallete);
        
        SharedContent.getInstance().getInstanceContent().set(lst, null);
                
        return SharedContent.getInstance().getAbstractLookup();
    }
    
    @Override
    public Lookup getLookup() {
        fixLookup();
        return super.getLookup();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == null)
            return;
        else if (Shape.PROPERTY_NAME.equals(evt.getPropertyName())) {
        }
        else if (Shape.PROPERTY_X.equals(evt.getPropertyName())) {
            int x = (Integer) evt.getNewValue();
            int y = lookup.lookup(Shape.class).getY();
            
            setPreferredLocation(new Point(x, y));                        
        }
        else if (Shape.PROPERTY_Y.equals(evt.getPropertyName())) {
            int x = lookup.lookup(Shape.class).getX();
            int y = (Integer) evt.getNewValue();
            
            setPreferredLocation(new Point(x, y)); 
        }
        else if (Shape.PROPERTY_WIDTH.equals(evt.getPropertyName())) {
            Rectangle bounds = getBounds();
            if (bounds == null)
                return;
            
            int width = (Integer) evt.getNewValue();
            double widthPercentage = Double.parseDouble(Integer.toString((Integer) evt.getNewValue())) / Double.parseDouble(Integer.toString((Integer) evt.getOldValue()));
            
            ((ModelLayoutScene) getScene()).changeWidget(new Dimension(width, bounds.height), this, this.getBounds(), widthPercentage, 1, ResizeProvider.ControlPoint.BOTTOM_RIGHT);
        }
        else if (Shape.PROPERTY_HEIGHT.equals(evt.getPropertyName())) {
            Rectangle bounds = getBounds();
            if (bounds == null)
                return;
            
            int height = (Integer) evt.getNewValue();
            double heightPercentage = Double.parseDouble(Integer.toString((Integer) evt.getNewValue())) / Double.parseDouble(Integer.toString((Integer) evt.getOldValue()));
            
            ((ModelLayoutScene) getScene()).changeWidget(new Dimension(bounds.width, height), this, this.getBounds(), 1, heightPercentage, ResizeProvider.ControlPoint.BOTTOM_RIGHT);
        }
        else if (Shape.PROPERTY_COLOR.equals(evt.getPropertyName())) {
            setBackground((Color) evt.getNewValue());
        }
        getScene().validate();
        getScene().paint();
        
        ((ModelLayoutScene) getScene()).fireChangeEvent(new ActionEvent(this, ModelLayoutScene.SCENE_CHANGE, evt.getPropertyName() + " Property Change"));
    }
}
