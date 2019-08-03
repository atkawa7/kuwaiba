/*
 *  Copyright 2010-2018, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.templates.layouts;

import java.awt.Color;
import java.awt.Image;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.model.CircleShape;
import org.inventory.core.templates.layouts.model.CustomShape;
import org.inventory.core.templates.layouts.model.LabelShape;
import org.inventory.core.templates.layouts.model.PolygonShape;
import org.inventory.core.templates.layouts.model.RectangleShape;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.nodes.CategoryChildren;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 * Class to represent an instance of palette controller
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeviceLayoutPalette {
    public static final HashMap<String, Shape []> shapes = new HashMap();
    
    private static DeviceLayoutPalette instance;
    private PaletteController paletteController;
    
    private DeviceLayoutPalette() {
    }
    
    public static DeviceLayoutPalette getInstance() {
        return instance == null ? instance = new DeviceLayoutPalette() : instance;
    }
    
    public PaletteController getPalette() {
        return paletteController == null ? createPalette() : paletteController;
    }
    
    public PaletteController createPalette() {
        shapes.put(I18N.gm("palette_category_display_name_general_shapes"), getGenericShapes());
        shapes.put(I18N.gm("palette_category_display_name_custom_shapes"), getCustomShapes());
                
        AbstractNode paletteRoot = new AbstractNode(new CategoryChildren());
        paletteRoot.setDisplayName(I18N.gm("palette_root_display_name"));
        paletteController = PaletteFactory.createPalette(paletteRoot, new DeviceLayoutPalette.CustomPaletteActions(), null, new DeviceLayoutPalette.CustomDragAndDropHandler());
        
        return paletteController;
    }
    
    private Shape [] getCustomShapes() {
        List<LocalObjectListItem> customShapes = CommunicationsStub.getInstance().getList(Constants.CLASS_CUSTOMSHAPE, false, true);
        if (customShapes == null) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            return new Shape[0];
        }
                        
        List<Shape> items = new ArrayList();
                
        for (LocalObjectListItem item : customShapes) {
            
            LocalObject object = CommunicationsStub.getInstance().getObjectInfo(item.getClassName(), item.getId());
                        
            if (object == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return new Shape[0];
            }
            Image icon = Utils.getIconFromByteArray(null, Color.BLACK, 30, 30);
            CustomShape customShape = new CustomShape(item, icon);
            customShape.setName(item.getDisplayName() != null ? item.getDisplayName() : item.getName());
            items.add(customShape);
        }
        return items.toArray(new Shape[0]);
    }
    
    private Shape [] getGenericShapes() {
        LabelShape labelShape = new LabelShape("org/inventory/core/templates/res/label.png");
        labelShape.setName(I18N.gm("basic_shape_label_name"));
        
        RectangleShape rectangleShape = new RectangleShape("org/inventory/core/templates/res/rectangle.png");
        rectangleShape.setName(I18N.gm("basic_rectangle_label_name"));
        
        PolygonShape polygonShape = new PolygonShape("org/inventory/core/templates/res/polygon.png");
        polygonShape.setName(I18N.gm("basic_polygon_label_name"));
        
        CircleShape circleShape = new CircleShape("org/inventory/core/templates/res/ellipse.png");
        circleShape.setName(I18N.gm("basic_circle_label_name"));
        
        return new Shape [] {
            labelShape,
            rectangleShape,
            polygonShape,
            circleShape,
        };
    }
    
    public class CustomPaletteActions extends PaletteActions {

        @Override
        public Action[] getImportActions() {
            return null;
        }

        @Override
        public Action[] getCustomPaletteActions() {
            return null;
        }

        @Override
        public Action[] getCustomCategoryActions(Lookup lkp) {
            return null;
        }

        @Override
        public Action[] getCustomItemActions(Lookup lkp) {
            return null;
        }
        
        @Override
        public Action getPreferredAction(Lookup lkp) {
            return null;
        }
    }
    
    public class CustomDragAndDropHandler extends DragAndDropHandler {

        @Override
        public void customize(ExTransferable et, Lookup lkp) {
            final Shape shape = lkp.lookup(Shape.class);
                        
            et.put(new ExTransferable.Single(Shape.DATA_FLAVOR) {
                
                @Override
                protected Object getData() throws IOException, UnsupportedFlavorException {
                    return shape;
                }
            });
        }
    };
}
