/*
 * Copyright (c) 2018 johnyortega.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    johnyortega - initial API and implementation and/or initial documentation
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
import org.inventory.core.templates.layouts.widgets.ContainerShapeWidget;
import org.inventory.core.templates.layouts2.scene.EquipmentLayoutScene;
import org.openide.util.actions.Presenter;

/**
 *
 * @author johnyortega
 */
public class DeleteContainerShapeAction extends GenericShapeAction implements Presenter.Popup {
    private static DeleteContainerShapeAction instance;
    private final JMenuItem popupPresenter;
    
    private DeleteContainerShapeAction() {
        putValue(NAME, I18N.gm("delete"));
        putValue(SMALL_ICON, ImageIconResource.WARNING_ICON);
                
        popupPresenter = new JMenuItem();
        popupPresenter.setName((String) getValue(NAME));
        popupPresenter.setText((String) getValue(NAME));
        popupPresenter.setIcon((ImageIcon) getValue(SMALL_ICON));
        popupPresenter.addActionListener(this);
    }
    
    public static DeleteContainerShapeAction getInstance() {
        return instance == null ? instance = new DeleteContainerShapeAction() : instance;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedWidget != null) {
            if (selectedWidget instanceof ContainerShapeWidget) {
                EquipmentLayoutScene scene = (EquipmentLayoutScene) selectedWidget.getScene();
                Object obj = scene.findObject(selectedWidget);
                
                if (obj != null && obj instanceof Shape) {
                    Shape shape = (Shape) obj;
                    
                    ((ContainerShapeWidget) selectedWidget).clearShapesSet();
                                        
                    shape.removeAllPropertyChangeListeners();
                    scene.removeNode((Shape) obj);

                    scene.validate();
                    scene.paint();

                    scene.fireChangeEvent(new ActionEvent(this, EquipmentLayoutScene.SCENE_CHANGE, "Shape deleted"));
                }
            }
        }
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return popupPresenter;
    }
        
}
