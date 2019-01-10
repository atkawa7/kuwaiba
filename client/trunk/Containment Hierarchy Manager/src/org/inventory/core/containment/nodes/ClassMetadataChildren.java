/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
 *  under the License.
 */
package org.inventory.core.containment.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.Children.Array;
import org.openide.nodes.Node;
/**
 * Represents a class node.
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ClassMetadataChildren extends Array {

    protected boolean main;
    protected List<LocalClassMetadataLight> keys;

    public ClassMetadataChildren(List<LocalClassMetadataLight> lcm) {
        this.main = true;
        this.keys= new ArrayList(lcm);
    }

    public ClassMetadataChildren(){
        this.main = false;
        this.keys= new ArrayList<>();
    }

    @Override
    protected Collection<Node> initCollection () {
        List<Node> myNodes = new ArrayList<>();
        for (LocalClassMetadataLight lcml : keys)
            if (main) // This is kinda weird, because
                myNodes.add(new ClassMetadataNode(lcml,main));
            else
                myNodes.add(new ClassMetadataNode(lcml));
        return myNodes;
    }

    @Override
    public void addNotify() {
        if (this.getNode() instanceof ClassMetadataNode){ //Ignores the root node
            LocalClassMetadataLight lcm = ((ClassMetadataNode)this.getNode()).getObject();
            
            List<LocalClassMetadataLight> children = CommunicationsStub.getInstance().getPossibleChildrenNoRecursive(lcm.getClassName());
            
            if (children == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            } else {
                keys = new ArrayList<>();
                keys.addAll(children);
            }
        }
    }
}