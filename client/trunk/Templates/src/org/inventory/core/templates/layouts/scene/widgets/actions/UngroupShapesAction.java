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
import org.inventory.core.services.i18n.I18N;
import org.inventory.core.templates.layouts.model.ContainerShape;
import org.inventory.core.templates.layouts.widgets.ContainerShapeWidget;
import org.inventory.core.templates.layouts2.scene.EquipmentLayoutScene;

/**
 *
 * @author johnyortega
 */
public class UngroupShapesAction extends GenericShapeAction {
    private static UngroupShapesAction instance;
    
    private UngroupShapesAction() {
        putValue(NAME, I18N.gm("lbl_ungroup_action"));
    }
    
    public static UngroupShapesAction getInstance() {
        return instance == null ? instance = new UngroupShapesAction() : instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (selectedWidget instanceof ContainerShapeWidget) {
            EquipmentLayoutScene scene = (EquipmentLayoutScene) selectedWidget.getScene();
            
            Object obj = scene.findObject(selectedWidget);
            if (obj instanceof ContainerShape) {
                
                ((ContainerShape) obj).removeAllPropertyChangeListeners();
                scene.removeNode((ContainerShape) obj);
                
                scene.validate();
                scene.paint();
                
                scene.fireChangeEvent(new ActionEvent(this, EquipmentLayoutScene.SCENE_CHANGE, "Shape deleted"));
            }
        }
    }
    
}
