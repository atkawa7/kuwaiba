/**
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
 *
 */
package org.inventory.core.templates.layouts.scene.widgets.actions;

import java.awt.event.ActionEvent;
import static javax.swing.Action.NAME;
import static javax.swing.Action.SMALL_ICON;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.core.templates.layouts.model.Shape;
import org.inventory.core.templates.layouts.scene.DeviceLayoutScene;
import org.openide.util.actions.Presenter;

/**
 * Action used to delete a widget in the scene
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeleteShapeAction extends GenericShapeAction implements Presenter.Popup {
    private static DeleteShapeAction instance;
    private final JMenuItem popupPresenter;
    
    private DeleteShapeAction() {
        putValue(NAME, I18N.gm("delete"));
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }
    
    public static DeleteShapeAction getInstance() {
        return instance == null ? instance = new DeleteShapeAction() : instance;                
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedWidget != null) {
            DeviceLayoutScene scene = (DeviceLayoutScene) selectedWidget.getScene();
            Object obj = scene.findObject(selectedWidget);
            if (obj != null && obj instanceof Shape) {
                Shape shape = (Shape) obj;
                shape.removeAllPropertyChangeListeners();
                scene.removeNode((Shape) obj);
                
                scene.validate();
                scene.paint();
                
                scene.fireChangeEvent(new ActionEvent(this, DeviceLayoutScene.SCENE_CHANGE, "Shape deleted"));
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
    
}
