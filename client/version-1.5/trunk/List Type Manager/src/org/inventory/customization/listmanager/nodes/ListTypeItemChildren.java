/**
 * Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
 *
 * Licensed under the EPL License, Version 1.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.inventory.customization.listmanager.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectChildren;
import org.openide.nodes.Node;

/**
 * These children represent the items within the list
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ListTypeItemChildren extends ObjectChildren {

    @Override
    public void addNotify() {
        LocalClassMetadataLight lcml = ((ListTypeNode)this.getNode()).getObject();
        List<LocalObjectListItem> theListTypeItems = CommunicationsStub.getInstance().getList(lcml.getClassName(), false, true);

        if (theListTypeItems == null) {
            setKeys(Collections.EMPTY_LIST);
            NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
        }
        else {
            Collections.sort(theListTypeItems);
            setKeys(theListTypeItems);
        }
    }

    @Override
    protected Node[] createNodes(LocalObjectLight key) {
        return new ListTypeItemNode[] { new ListTypeItemNode(key) };
    }
    
    
}
