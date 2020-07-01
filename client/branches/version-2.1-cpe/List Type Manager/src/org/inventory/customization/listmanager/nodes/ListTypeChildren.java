/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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

package org.inventory.customization.listmanager.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Represents the children corresponding to list type classes
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ListTypeChildren extends Children.Keys<LocalClassMetadataLight> {

    public ListTypeChildren() {
        setKeys(Collections.EMPTY_SET);
    }
    
    @Override
    protected void removeNotify() {
        setKeys(Collections.EMPTY_LIST);
    }

    @Override
    protected void addNotify() {
        refreshList();
    }

    @Override
    protected Node[] createNodes(LocalClassMetadataLight key) {
        return new Node[] { new ListTypeNode(key)};
    }
    
    public void refreshList() {
        List<LocalClassMetadataLight> theListTypes = CommunicationsStub.getInstance().getInstanceableListTypes();

        if (theListTypes == null) {
            setKeys(Collections.EMPTY_LIST);
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, 
                    CommunicationsStub.getInstance().getError());
        }
        else {
            Collections.sort(theListTypes);
            setKeys(theListTypes);
        }        
    }
}
