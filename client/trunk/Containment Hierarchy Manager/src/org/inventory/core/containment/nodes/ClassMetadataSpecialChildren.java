/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>
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
 */
package org.inventory.core.containment.nodes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.Node;

/**
 * Represents a class node.
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ClassMetadataSpecialChildren extends ClassMetadataChildren {
    public ClassMetadataSpecialChildren(List<LocalClassMetadataLight> lcm) {
        super(lcm);
    }
    
    public ClassMetadataSpecialChildren() {
        super();
    }
    
    @Override
    protected Collection<Node> initCollection() {
        List<Node> myNodes = new ArrayList<>();
        for (LocalClassMetadataLight lcml : keys) {
            if (main)
                myNodes.add(new ClassMetadataSpecialNode(lcml, main));
            else
                myNodes.add(new ClassMetadataSpecialNode(lcml));
        }
        return myNodes;        
    }
        
    @Override
    public void addNotify() {
        if (getNode() instanceof ClassMetadataSpecialNode) { // Ignores the root node
            LocalClassMetadataLight lcm = ((ClassMetadataSpecialNode) getNode()).getObject();
            List<LocalClassMetadataLight> children = CommunicationsStub.getInstance().getPossibleSpecialChildrenNoRecursive(lcm.getClassName());
            
            if (children == null) {
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            } else {
                keys = new ArrayList<>();
                keys.addAll(children);
            }
        }
    }
}
