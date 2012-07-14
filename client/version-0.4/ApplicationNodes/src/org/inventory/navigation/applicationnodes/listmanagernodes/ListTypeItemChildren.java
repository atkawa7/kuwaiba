/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

package org.inventory.navigation.applicationnodes.listmanagernodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.api.metadata.LocalClassMetadataLight;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.LocalObjectListItem;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.navigation.applicationnodes.objectnodes.ObjectChildren;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * These children represent the items within the list
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ListTypeItemChildren extends ObjectChildren{

    public ListTypeItemChildren() {
        keys = new ArrayList<LocalObjectLight>();
    }

    //This is basically the same code as in ObjectChildren but changes ObjectNodes for ListElementNodes
    @Override
    protected Collection<Node> initCollection(){
        List<Node> myNodes = new ArrayList<Node>();

        for (LocalObjectLight lol : keys)
            myNodes.add(new ListTypeItemNode(lol));
        return myNodes;
    }

    @Override
    public void addNotify(){
        if (this.getNode() instanceof ListTypeNode){
            LocalClassMetadataLight lcml = ((ListTypeNode)this.getNode()).getObject();
            List<LocalObjectListItem> myObjects = CommunicationsStub.getInstance().getList(lcml.getClassName(), false, false);

            if (myObjects == null){
                Lookup.getDefault().lookup(NotificationUtil.class).
                        showSimplePopup("List Generation", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
            }else{
                for (LocalObjectListItem child : myObjects){
                    ListTypeItemNode newNode = new ListTypeItemNode(child);
                    // Remove it if it already exists (if this is not done,
                    // it will duplicate the nodes created when the parent was collapsed)
                    keys.remove(child);
                    keys.add(child);
                    remove(new Node[]{newNode});
                    add(new Node[]{newNode});
               }
            }
        }
    }
}
