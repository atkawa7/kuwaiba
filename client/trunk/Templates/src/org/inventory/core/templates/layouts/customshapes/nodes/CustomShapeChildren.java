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
package org.inventory.core.templates.layouts.customshapes.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.nodes.Node;

/**
 * These children represent the predefined shape within the list
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class CustomShapeChildren extends AbstractChildren {
    
    public CustomShapeChildren() {
       setKeys(Collections.EMPTY_LIST);
    }
    
    public CustomShapeChildren(List<LocalObjectLight> customShapes) {
        setKeys(customShapes);
    }

    @Override
    public void addNotify() {
        List<LocalObjectListItem> customShapes = CommunicationsStub.getInstance().getCustomShapes(true);
        if (customShapes == null) {
            setKeys(Collections.EMPTY_LIST);
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                CommunicationsStub.getInstance().getError());
        } else {
            Collections.sort(customShapes);
            setKeys(customShapes);
        }
    }

    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new CustomShapeNode [] {new CustomShapeNode(key)};
    }
    
}
