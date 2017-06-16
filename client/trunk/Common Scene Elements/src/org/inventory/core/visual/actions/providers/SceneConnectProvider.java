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
 */

package org.inventory.core.visual.actions.providers;

import java.awt.Point;
import org.inventory.core.visual.scene.ObjectNodeWidget;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 * A general purpose provider that allows to connect AbstractNodeWidgets. Subclasses must implement the createConnection method according to their needs
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public abstract class SceneConnectProvider implements ConnectProvider {

    @Override
    public boolean isSourceWidget(Widget sourceWidget) {
        return sourceWidget instanceof ObjectNodeWidget; 
    }

    @Override
    public ConnectorState isTargetWidget(Widget sourceWidget, Widget targetWidget) {
        if (targetWidget instanceof ObjectNodeWidget) {
            if (sourceWidget.equals(targetWidget)) //A widget can not connect to itself
                return ConnectorState.REJECT;
            return  ConnectorState.ACCEPT;
        }
        
        return ConnectorState.REJECT;
    }

    @Override
    public boolean hasCustomTargetWidgetResolver(Scene scene) {
       return false;
    }

    @Override
    public Widget resolveTargetWidget(Scene scene, Point sceneLocation) {
        return null;
    }

    @Override
    public abstract void createConnection(Widget sourceWidget, Widget targetWidget);

}
