/*
 *  Copyright 2010-2019, Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.customization.classhierarchy.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Represents the children for the navigation tree
 * @author Adrian Martinez Molina {@literal <charles.bedon@kuwaiba.org>}
 */
public class ClassMetadataChildren extends Children.Keys<LocalClassMetadataLight> {
    
    /**
     * This constructor is used to create a node with no children
     *  since they're going to be created on demand (see method addNotify)
     */
    public ClassMetadataChildren(){
    }   
    
    public ClassMetadataChildren(LocalClassMetadataLight[] lcls) {        
        setKeys(lcls);
    }
   
    /**
     * Creates children nodes on demand
     */
    @Override
    public void addNotify(){
        refreshList();
    }
    
    @Override
    public void removeNotify() {
        setKeys(Collections.EMPTY_LIST);
    }

    @Override
    protected Node[] createNodes(LocalClassMetadataLight key) {
        return new Node[] { new ClassMetadataNode(key) };
    }
    
    public void refreshList() {
        if (!(this.getNode() instanceof ClassMetadataNode))
            return;
        
        CommunicationsStub com = CommunicationsStub.getInstance();
        ClassMetadataNode node = ((ClassMetadataNode)this.getNode());
        List<LocalClassMetadataLight> subClasses = com.getLightSubclassesNoRecursive(node.getClassMetadata().getClassName(), true, false);

        if (subClasses == null) {
            setKeys(Collections.EMPTY_LIST);
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, com.getError());
        }
        else {
            Collections.sort(subClasses);
            setKeys(subClasses);
        }
    }
}
