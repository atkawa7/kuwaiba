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
package org.inventory.core.templates.layouts.scene;

import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.visual.scene.AbstractScene;
import org.inventory.core.visual.scene.SelectableNodeWidget;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectProvider;
import org.netbeans.api.visual.widget.Widget;

/**
 * Scene used to show a model layout
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ShowDeviceLayoutScene extends AbstractScene<LocalObjectLight, LocalObjectLight> {
    
    public ShowDeviceLayoutScene() {
        
        getActions().addAction(ActionFactory.createZoomAction());
        getInputBindings().setZoomActionModifiers(0); //No keystroke combinations
        getActions().addAction(ActionFactory.createPanAction());
        
        initSelectionListener();
    }

    @Override
    public byte[] getAsXML() {
        return null;
    }

    @Override
    public void render(byte[] structure) throws IllegalArgumentException {
    }

    @Override
    public void render(LocalObjectLight root) {
    }

    @Override
    public ConnectProvider getConnectProvider() {
        return null;
    }

    @Override
    public boolean supportsConnections() {
        return false;
    }

    @Override
    public boolean supportsBackgrounds() {
        return false;
    }

    @Override
    protected Widget attachNodeWidget(LocalObjectLight node) {
        Widget widget = new SelectableNodeWidget(this, node) {};
        widget.getActions().addAction(createSelectAction());
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(LocalObjectLight edge) {
        return null;
    }
    
}
