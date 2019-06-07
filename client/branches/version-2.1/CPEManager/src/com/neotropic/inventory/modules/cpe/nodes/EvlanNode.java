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
 */
package com.neotropic.inventory.modules.cpe.nodes;

import com.neotropic.inventory.modules.cpe.nodes.actions.CpeManagerActionFactory;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class EvlanNode extends ObjectNode {
    public EvlanNode(LocalObjectLight evlan) {
        super(evlan);
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { CpeManagerActionFactory.getDeleteEvlanAction() };
    }
    
    public static class EvlanChildren extends Children.Keys<LocalObjectLight> {
        
        @Override
        public void addNotify() {
            EvlanNode evlanNode = (EvlanNode) getNode();
            List<LocalObjectLight> egvlans = CommunicationsStub.getInstance().getObjectChildren(evlanNode.getObject().getId(), evlanNode.getObject().getClassName());
            if (egvlans != null) {
                Collections.sort(egvlans);
                setKeys(egvlans);
            }
            else {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), // NOI18N
                    NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            }
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_LIST);
        }
        
        @Override
        protected Node[] createNodes(LocalObjectLight key) {
            return new Node[] { new EgvlanNode(key) };
        }
    }
}
