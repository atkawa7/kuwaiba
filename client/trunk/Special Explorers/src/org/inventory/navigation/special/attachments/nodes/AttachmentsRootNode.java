/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.special.attachments.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalFileObjectLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * The root node of the Attachment Explorer TC. This node is not visible and its lookup contains the currently selected inventory object as a LocalObjectLight instance 
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class AttachmentsRootNode extends AbstractNode {

    public AttachmentsRootNode(LocalObjectLight inventoryObject) {
        super(new AttachmentsRootNodeChildren(), Lookups.singleton(inventoryObject));
        setDisplayName(PROP_NAME);
    }
    
    public static class AttachmentsRootNodeChildren extends Children.Keys<LocalFileObjectLight> {

        @Override
        public void addNotify() {
            LocalObjectLight inventoryObject = getNode().getLookup().lookup(LocalObjectLight.class);
            List<LocalFileObjectLight> attachedFiles = CommunicationsStub.getInstance().getFilesForObject(inventoryObject.getClassName(), inventoryObject.getId());
            
            if (attachedFiles == null) {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
            else {
                Collections.sort(attachedFiles);
                setKeys(attachedFiles);
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }
        
        @Override
        protected Node[] createNodes(LocalFileObjectLight t) {
            return new Node[] { new FileObjectNode(t) };
        }
    }
}
