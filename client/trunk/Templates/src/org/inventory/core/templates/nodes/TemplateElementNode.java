/**
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.templates.nodes;

import java.awt.Image;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.templates.nodes.actions.TemplateActionsFactory;
import org.inventory.navigation.applicationnodes.objectnodes.AbstractChildren;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a template element.
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TemplateElementNode extends AbstractNode {

    private final Image defaultIcon = Utils.createRectangleIcon(Utils.DEFAULT_ICON_COLOR, 
            Utils.DEFAULT_ICON_WIDTH, Utils.DEFAULT_ICON_HEIGHT);
    
    public TemplateElementNode(LocalObjectLight object) {
        super(new TemplateElementChildren(), Lookups.singleton(object));
        setDisplayName(object.toString());
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {TemplateActionsFactory.getCreateTemplateElementAction(), 
                             null,
                             TemplateActionsFactory.getDeleteTemplateElementAction()};
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return defaultIcon;
    }

    @Override
    public Image getIcon(int type) {
        return defaultIcon;
    }
    
    public static class TemplateElementChildren extends AbstractChildren {
        @Override
        public void addNotify() {
            LocalObjectLight templateElement = getNode().getLookup().lookup(LocalObjectLight.class);
            List<LocalObjectLight> templateElementChildren = CommunicationsStub.getInstance().
                    getTemplateElementChildren(templateElement.getClassName(), templateElement.getOid());
            
            if (templateElementChildren == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                setKeys(Collections.EMPTY_SET);
            } else
                setKeys(templateElementChildren);
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        @Override
        protected Node[] createNodes(LocalObjectLight t) {
            return new Node[] {new TemplateElementNode(t)};
        }
    }
}
