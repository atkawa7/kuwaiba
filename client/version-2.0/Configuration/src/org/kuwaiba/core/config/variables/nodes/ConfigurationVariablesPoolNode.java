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
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalConfigurationVariable;
import org.inventory.communications.core.LocalPool;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.kuwaiba.core.config.variables.nodes.actions.ConfigurationVariablesActionFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.lookup.Lookups;

/**
 * A node representing a pool of configuration variables
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class ConfigurationVariablesPoolNode extends AbstractNode {

    private static final Image ICON = ImageUtilities.loadImage("org/kuwaiba/core/config/res/poolNodeIcon.png");
    
    public ConfigurationVariablesPoolNode(LocalPool configVariablesPool) {
        super(new ConfigurationVariablesPoolNodeChildren(), Lookups.singleton(configVariablesPool));
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[] { ConfigurationVariablesActionFactory.getAddConfigurationVariableAction(), 
                              null,
                              ConfigurationVariablesActionFactory.getDeleteConfigurationVariablesPoolAction()};
        
    }

    @Override
    public String getName() {
        return getLookup().lookup(LocalPool.class).getName();
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ICON;
    }

    @Override
    public Image getIcon(int type) {
        return ICON;
    }
    
    public static class ConfigurationVariablesPoolNodeChildren extends Children.Keys<LocalConfigurationVariable> {
        
        @Override
        public void addNotify() {
            List<LocalConfigurationVariable> configurationVariablesInPool = 
                    CommunicationsStub.getInstance().getConfigurationVariablesInPool(getNode().getLookup().lookup(LocalPool.class).getId());
            if (configurationVariablesInPool == null)
                NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), NotificationUtil.ERROR_MESSAGE, CommunicationsStub.getInstance().getError());
            else
                setKeys(configurationVariablesInPool);
        }
        
        @Override
        public void removeNotify() {
            setKeys(Collections.EMPTY_SET);
        }
        
        @Override
        protected Node[] createNodes(LocalConfigurationVariable key) {
            return new ConfigurationVariableNode[] { new ConfigurationVariableNode(key) };
        }
    }

}
