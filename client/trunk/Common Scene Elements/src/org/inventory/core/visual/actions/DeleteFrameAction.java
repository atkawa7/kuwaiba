/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */
package org.inventory.core.visual.actions;

import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalPrivilege;
import org.inventory.core.services.api.actions.GenericInventoryAction;
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.services.utils.ImageIconResource;
import org.inventory.core.visual.scene.AbstractScene;
import org.openide.util.actions.Presenter;

/**
 * Action to delete a frame from topology designer scene
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeleteFrameAction extends GenericInventoryAction implements Presenter.Popup {
    private static DeleteFrameAction instance;
    private final AbstractScene scene;
    private final JMenuItem popupPresenter;
    
    private DeleteFrameAction(AbstractScene scene) {
        putValue(NAME, I18N.gm("delete_frame"));
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
        this.scene = scene;
        
        popupPresenter = new JMenuItem();
        popupPresenter.setName(I18N.gm("delete_frame"));
        popupPresenter.setText(I18N.gm("delete_frame"));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }
    
    public static DeleteFrameAction getInstance(AbstractScene scene) {
        if (scene == null)
            return null;
        
        return instance == null ? instance = new DeleteFrameAction(scene) : instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Set<?> selectedObjects = scene.getSelectedObjects();
        for (Object selectedObject : selectedObjects) {
            LocalObjectLight lol = (LocalObjectLight)selectedObject;
            if (lol.getName().contains(AbstractScene.FREE_FRAME))
                scene.removeNodeWithEdges(lol);
            scene.fireChangeEvent(new ActionEvent(selectedObject, AbstractScene.SCENE_CHANGE, "manualDelete"));
        }
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }

    @Override
    public LocalPrivilege getPrivilege() {
        return new LocalPrivilege(LocalPrivilege.PRIVILEGE_TOPOLOGY_DESIGNER, LocalPrivilege.ACCESS_LEVEL_READ_WRITE);
    }
}
