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
package org.inventory.design.modelsLayouts;

import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.inventory.design.modelsLayouts.scene.ModelLayoutScene;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.core.views.LocalObjectView;
import org.inventory.communications.core.views.LocalObjectViewLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.design.modelsLayouts.model.LabelShape;
import org.inventory.design.modelsLayouts.nodes.CategoryChildren;
import org.inventory.design.modelsLayouts.model.Shape;
import org.netbeans.spi.palette.DragAndDropHandler;
import org.netbeans.spi.palette.PaletteActions;
import org.netbeans.spi.palette.PaletteController;
import org.netbeans.spi.palette.PaletteFactory;
import org.openide.nodes.AbstractNode;
import org.openide.util.Lookup;
import org.openide.util.datatransfer.ExTransferable;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ModelLayoutService {
    public static final HashMap<String, Shape []> shapes = new HashMap();
    
    private final ModelLayoutScene scene;
    private PaletteController palette;
    
    private LocalObjectView currentView;
    
    private LocalObjectListItem listItem;
            
    public ModelLayoutService(LocalObjectListItem listItem) {
        this.listItem = listItem;
        scene = new ModelLayoutScene(listItem);
        shapes.put("General", new Shape [] {
            new Shape("org/inventory/design/modelsLayouts/res/rectangle.png"),
            new LabelShape("org/inventory/design/modelsLayouts/res/label.png")
        });
    }
    
    public LocalObjectListItem getListItem() {
        return listItem;
    }
        
    public void renderView() {
        LocalObjectListItem listItem = scene.getListItem();
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
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
    }
    
    public void saveView() {
        LocalObjectListItem listItem = scene.getListItem();
        
        byte[] structure = scene.getAsXML();
        if (currentView == null) {
            long viewId = CommunicationsStub.getInstance().createListTypeItemRelateView(
                listItem.getId(), listItem.getClassName(), "ModelTypeLayoutView", null, null, structure, scene.getBackgroundImage()); //NOI18N
            
            if (viewId != -1) { //Success
                currentView = new LocalObjectView(viewId, "ModelTypeLayoutView", null, null, structure, scene.getBackgroundImage()); //NOI18N
                NotificationUtil.getInstance().showSimplePopup("Information", 
                    NotificationUtil.INFO_MESSAGE, "The view was saved successfully");
            } else
                NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        } else {
            if (CommunicationsStub.getInstance().updateListTypeItemRelatedView(listItem.getId(), listItem.getClassName(), 
                currentView.getId(), null, null, structure, scene.getBackgroundImage()))
                NotificationUtil.getInstance().showSimplePopup("Information", NotificationUtil.INFO_MESSAGE, "The view was saved successfully");
            else
                NotificationUtil.getInstance().showSimplePopup("Error", 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
    }
        
    public ModelLayoutScene getScene() {
        return scene;
    }
    
    public PaletteController getPalette() {
        return createPalette();
    }
    
    private PaletteController createPalette() {
        if (palette == null) {
            AbstractNode paletteRoot = new AbstractNode(new CategoryChildren());
            paletteRoot.setName("Palette");
            palette = PaletteFactory.createPalette(paletteRoot, new CustomPaletteActions(), null, new CustomDragAndDropHandler());
        }
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
