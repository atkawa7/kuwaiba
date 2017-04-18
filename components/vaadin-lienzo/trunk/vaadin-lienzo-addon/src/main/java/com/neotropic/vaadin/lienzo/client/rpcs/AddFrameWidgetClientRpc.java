/*
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
package com.neotropic.vaadin.lienzo.client.rpcs;

import com.neotropic.vaadin.lienzo.client.core.shape.SrvFrameWidget;
import com.vaadin.shared.communication.ClientRpc;

/**
 * ClientRpc used to receive AddFrameWidget RPC events from server
 * @author Johny Andres Ortega Ruiz johny.ortega@kuwaiba.org
 */
public interface AddFrameWidgetClientRpc extends ClientRpc {
    public void addFrameWidget(SrvFrameWidget frameWidget);    
}
