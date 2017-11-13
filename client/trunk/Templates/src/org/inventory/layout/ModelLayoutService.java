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
package org.inventory.layout;

import java.awt.Color;
import java.awt.Image;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.inventory.layout.scene.ModelLayoutScene;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObject;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.communications.util.Binary;
import org.inventory.communications.util.Constants;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.layout.model.CircleShape;
import org.inventory.layout.model.LabelShape;
import org.inventory.layout.model.PolygonShape;
import org.inventory.layout.model.PredefinedShape;
import org.inventory.layout.model.RectangleShape;
import org.inventory.layout.nodes.CategoryChildren;
import org.inventory.layout.model.Shape;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 * Service used to get/set information from/to the server for the model layout view
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ModelLayoutService {
    public static final HashMap<String, Shape []> shapes = new HashMap();
    
    private final ModelLayoutScene scene;
    private PaletteController palette;
    
    private LocalObjectView currentView;
    
    private final LocalObjectListItem listItem;
            
    public ModelLayoutService(LocalObjectListItem listItem) {
        this.listItem = listItem;
        scene = new ModelLayoutScene(listItem);
    }
    
    private Shape [] getGenericShape() {
        LabelShape labelShape = new LabelShape("org/inventory/layout/res/label.png");
        labelShape.setName(I18N.gm("basic_shape_label_name"));
        
        RectangleShape rectangleShape = new RectangleShape("org/inventory/layout/res/rectangle.png");
        rectangleShape.setName(I18N.gm("basic_rectangle_label_name"));
        
        PolygonShape polygonShape = new PolygonShape("org/inventory/layout/res/polygon.png");
        polygonShape.setName(I18N.gm("basic_polygon_label_name"));
        
        CircleShape circleShape = new CircleShape("org/inventory/layout/res/ellipse.png");
        circleShape.setName(I18N.gm("basic_circle_label_name"));
        
        return new Shape [] {
            labelShape,
            rectangleShape,
            polygonShape,
            circleShape
        };
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
            
            LocalObject object = CommunicationsStub.getInstance().getObjectInfo(item.getClassName(), item.getOid());
                        
            if (object == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return new Shape[0];
            }
            Binary binaryIcon = (Binary) object.getAttribute("icon"); //NOI18N
            
            if (binaryIcon == null)
                continue;
            
            Image icon = Utils.getIconFromByteArray(binaryIcon.getByteArray(), Color.BLACK, 30, 30);
            
            PredefinedShape predefinedShape = new PredefinedShape(item, icon);
            predefinedShape.setName(item.getDisplayName() != null ? item.getDisplayName() : item.getName());
            items.add(predefinedShape);
        }
        return items.toArray(new Shape[0]);
    }
    
    public LocalObjectListItem getListItem() {
        return listItem;
    }
    
    public LocalObjectView getCurrentView() {
        return currentView;        
    }
        
    public void renderView() {
        List<LocalObjectViewLight> relatedViews = CommunicationsStub.getInstance().getListTypeItemRelatedViews(listItem.getId(), listItem.getClassName());
        if (relatedViews != null) {
            if (relatedViews.isEmpty()) {
                currentView = null;
                scene.render((byte[]) null);
            } else {
                currentView = CommunicationsStub.getInstance().getListTypeItemRelatedView(listItem.getId(), listItem.getClassName(), relatedViews.get(0).getId());
                scene.render(currentView.getStructure());
            }            
        } else
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }
    
    public boolean saveView() {        
        byte[] structure = scene.getAsXML();
        if (currentView == null) {
            long viewId = CommunicationsStub.getInstance().createListTypeItemRelateView(
                listItem.getId(), listItem.getClassName(), "EquipmentModelLayoutView", null, null, structure, scene.getBackgroundImage()); //NOI18N
            
            if (viewId != -1) { //Success
                currentView = new LocalObjectView(viewId, "EquipmentModelLayoutView", null, null, structure, scene.getBackgroundImage()); //NOI18N
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), 
                    NotificationUtil.INFO_MESSAGE, I18N.gm("view_save_successfully"));
                return true;
            } else {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return false;
            }
        } else {
            if (CommunicationsStub.getInstance().updateListTypeItemRelatedView(listItem.getId(), listItem.getClassName(), 
                currentView.getId(), null, null, structure, scene.getBackgroundImage())) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("information"), NotificationUtil.INFO_MESSAGE, "The view was saved successfully");
                return true;
            } else {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                return false;
            }
        }
    }
    
    public boolean deleteView() {
        if (currentView == null)
            return false;
        if (listItem == null)
            return false;
        boolean deleted = CommunicationsStub.getInstance().deleteListTypeItemRelatedView(
            listItem.getId(), listItem.getClassName(), currentView.getId());
        
        if (deleted)
            currentView = null;
        return deleted;
    }
        
    public ModelLayoutScene getScene() {
        return scene;
    }
    
    public PaletteController getPalette() {
        return palette == null ? createPalette() : palette;
    }
    
    public PaletteController createPalette() {
        shapes.put(I18N.gm("palette_category_display_name_predefined_shapes"), getCustomShapes());
        shapes.put(I18N.gm("palette_category_display_name_general_shapes"), getGenericShape());
        
        AbstractNode paletteRoot = new AbstractNode(new CategoryChildren());
        paletteRoot.setDisplayName(I18N.gm("palette_root_display_name"));
        palette = PaletteFactory.createPalette(paletteRoot, new CustomPaletteActions(), null, new CustomDragAndDropHandler());
        
        return palette;
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
