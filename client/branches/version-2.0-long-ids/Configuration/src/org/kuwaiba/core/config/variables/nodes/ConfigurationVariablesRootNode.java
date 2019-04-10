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
package org.kuwaiba.core.config.variables.nodes;

import java.awt.Image;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalPool;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.kuwaiba.core.config.variables.nodes.actions.ConfigurationVariablesActionFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

/**
 * Node representing the configuration variables manager root
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ConfigurationVariablesRootNode extends AbstractNode {
    private static Image ICON = ImageUtilities.loadImage("org/kuwaiba/core/config/res/root.png");
  
    public ConfigurationVariablesRootNode() {
        super(new ConfigurationVariablesRootChildren());
        setDisplayName("Configuration Variables");
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{ ConfigurationVariablesActionFactory.getAddConfigurationVariablesPoolAction() };
    }
    
    @Override
    public Image getIcon(int i){
        return ICON;
    }
    
    @Override
    public Image getOpenedIcon(int i){
        return ICON;
    }
    
    public static class ConfigurationVariablesRootChildren extends Children.Keys <LocalPool> {

        @Override
        public void addNotify() {
            List<LocalPool> customerPools = CommunicationsStub.getInstance().getConfigurationVariablesPools();

            if (customerPools == null)
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, 
                        CommunicationsStub.getInstance().getError());
            else 
                setKeys(customerPools);
        }
        
        @Override
        protected Node[] createNodes(LocalPool key) {
            return new Node[] { new ConfigurationVariablesPoolNode(key) };
        }
    }
}
