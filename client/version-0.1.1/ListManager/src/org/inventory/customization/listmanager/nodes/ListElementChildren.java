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

package org.inventory.customization.listmanager.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.interfaces.LocalClassMetadataLight;
import org.inventory.core.services.interfaces.LocalObjectLight;
import org.inventory.core.services.interfaces.NotificationUtil;
import org.inventory.navigation.navigationtree.nodes.ObjectChildren;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * These children represent the items within the list
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class ListElementChildren extends ObjectChildren{

    @Override
    protected Collection<Node> initCollection(){
        List<Node> myNodes = new ArrayList<Node>();
        for (LocalObjectLight lol : keys)
            myNodes.add(new ListElementNode(lol));
        return myNodes;
    }

    @Override
    public void addNotify(){
        LocalClassMetadataLight lcml = ((ListTypeNode)this.getNode()).getObject();
        LocalObjectLight[] myObjects = CommunicationsStub.getInstance().searchForObjects(lcml.getPackageName()+"."+lcml.getClassName(),
                new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
        if (myObjects == null)
            Lookup.getDefault().lookup(NotificationUtil.class).
                    showSimplePopup("List Generation", NotificationUtil.ERROR, CommunicationsStub.getInstance().getError());
        for (LocalObjectLight lol : myObjects)
            keys.add(lol);

        initCollection();
    }
}
