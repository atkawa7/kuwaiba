/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
import org.inventory.communications.core.LocalClassMetadataLight;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Utils;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.templates.nodes.actions.TemplateActionsFactory;
import org.inventory.navigation.navigationtree.nodes.AbstractChildren;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 * A simple node representing an instanceable inventory class
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TemplatesModuleClassNode extends AbstractNode {

    private static final Image defaultIcon = Utils.createRectangleIcon(Utils.DEFAULT_CLASS_ICON_COLOR, 
            Utils.DEFAULT_ICON_WIDTH, Utils.DEFAULT_ICON_HEIGHT);
    
    public TemplatesModuleClassNode(LocalClassMetadataLight aClass) {
        super(new ClassChildren(), Lookups.singleton(aClass));
        setDisplayName(aClass.getClassName());
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] {TemplateActionsFactory.getCreateTemplateAction()};
    }
    
    @Override
    public Image getOpenedIcon(int type) {
        return defaultIcon;
    }

    @Override
    public Image getIcon(int type) {
        return defaultIcon;
    }
    
    
    
    public static class ClassChildren extends AbstractChildren {

        @Override
        public void addNotify() {
            LocalClassMetadataLight classMetadata = getNode().getLookup().lookup(LocalClassMetadataLight.class);
            List<LocalObjectLight> templatesForClass = CommunicationsStub.getInstance().
                    getTemplatesForClass(classMetadata.getClassName());
            
            if (templatesForClass == null) {
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
                setKeys(Collections.EMPTY_SET);
            } else
                setKeys(templatesForClass);
        }
    
        @Override
        protected Node[] createNodes(LocalObjectLight t) {
            return new Node[] {new TemplateElementNode(t)};
        }
    }
}
