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
package org.kuwaiba.core.config.validators.nodes;

import java.util.Collections;
import java.util.List;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.util.Constants;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Node representing the validators manager root
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ValidatorsRootNode extends AbstractNode {
    public ValidatorsRootNode() {
        super(new ValidatorsRootChildren());
        setDisplayName("Validators");
    }
    
    public static class ValidatorsRootChildren extends Children.Keys <LocalClassMetadataLight> {

        @Override
        public void addNotify() {
            List<LocalClassMetadataLight> lightSubclasses = CommunicationsStub.getInstance().getLightSubclasses(Constants.CLASS_INVENTORYOBJECT, true, true);
            
            if (lightSubclasses == null) {
                setKeys(Collections.EMPTY_LIST);
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            }
            else 
                setKeys(lightSubclasses);
        }
        
        @Override
        protected Node[] createNodes(LocalClassMetadataLight key) {
            return new Node[] { new ClassNode(key) };
        }
    }
}
