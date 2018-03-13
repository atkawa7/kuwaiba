/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.views.rackview.scene;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.util.Collections;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadata;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.views.rackview.widgets.RackViewConnectionWidget;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class RackConnectionSelectProvider implements SelectProvider {

    public RackConnectionSelectProvider() {        
    }

    @Override
    public boolean isAimingAllowed (Widget widget, Point localLocation, boolean invertSelection) {
        return false;
    }

    @Override
    public boolean isSelectionAllowed (Widget widget, Point localLocation, boolean invertSelection) {
        return ((RackViewScene) widget.getScene()).findObject(widget) != null;
    }

    @Override
    public void select (Widget widget, Point localLocation, boolean invertSelection) {
        RackViewScene scene = ((RackViewScene) widget.getScene());

        Object object = scene.findObject (widget);

        scene.setFocusedObject (object);
        if (object != null) {
            if (!invertSelection && scene.getSelectedObjects().contains(object))
                return;
            scene.userSelectionSuggested (Collections.singleton(object), invertSelection);

            for (LocalObjectLight edge : scene.getEdges()) {
                Widget edgeWidget = scene.findWidget(edge);

                if (edgeWidget != null && edgeWidget instanceof RackViewConnectionWidget) {

                    LocalClassMetadata connectionClass = CommunicationsStub.getInstance().getMetaForClass(edge.getClassName(), false);
                    if (connectionClass == null) {
                        NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                            NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                        continue;
                    }
                    ((RackViewConnectionWidget )edgeWidget).setLineColor(connectionClass.getColor());
                    ((RackViewConnectionWidget) edgeWidget).setStroke(new BasicStroke(RackViewScene.STROKE_WIDTH));
                }
            }
            if (widget instanceof RackViewConnectionWidget) {
                ((RackViewConnectionWidget) widget).setLineColor(Color.CYAN);
                ((RackViewConnectionWidget) widget).setStroke(new BasicStroke(RackViewScene.SELECTED_STROKE_WIDTH));
            }
        } else
            scene.userSelectionSuggested (Collections.emptySet(), invertSelection);
    }
}