/**
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
package com.neotropic.kuwaiba.modules.reporting.img.rackview;

import org.kuwaiba.interfaces.ws.toserialize.business.RemoteObjectLight;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DeviceLayoutScene extends GraphScene<RemoteObjectLight, RemoteObjectLight> {
    
    public DeviceLayoutScene() {
        setLayout(LayoutFactory.createAbsoluteLayout());
    }

    @Override
    protected Widget attachNodeWidget(RemoteObjectLight node) {
        Widget widget = new Widget(this);
        return widget;
    }

    @Override
    protected Widget attachEdgeWidget(RemoteObjectLight e) {
        return null;
    }

    @Override
    protected void attachEdgeSourceAnchor(RemoteObjectLight edge, RemoteObjectLight oldSourceNode, RemoteObjectLight newSourceNode) {
        /*
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget sourceWidget = findWidget(newSourceNode);
        connectionWidget.setSourceAnchor(sourceWidget != null ? AnchorFactory.createCircularAnchor(sourceWidget, 3) : null);
        */
    }

    @Override
    protected void attachEdgeTargetAnchor(RemoteObjectLight edge, RemoteObjectLight oldTargetNode, RemoteObjectLight newTargetNode) {
        /*
        ConnectionWidget connectionWidget = (ConnectionWidget)findWidget(edge);
        Widget targetWidget = findWidget(newTargetNode);
        connectionWidget.setTargetAnchor(targetWidget != null ? AnchorFactory.createCircularAnchor(targetWidget, 3) : null);
        */
    }
    
}
