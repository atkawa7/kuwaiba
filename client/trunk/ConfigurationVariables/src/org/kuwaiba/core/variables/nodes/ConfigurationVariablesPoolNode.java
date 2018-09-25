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
 */

package org.kuwaiba.core.variables.nodes;

import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalConfigurationVariable;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * A node representing a pool of configuration variables
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class ConfigurationVariablesPoolNode extends AbstractNode {

    public ConfigurationVariablesPoolNode(Children children) {
        super(children);
    }
    
    
    private class ConfigurationVariablesPoolNodeChildren extends Children.Keys<LocalConfigurationVariable> {

        @Override
        public void addNotify() {
            //CommunicationsStub.getInstance().getChildrenOfClass(0, PROP_NAME, PROP_DISPLAY_NAME)
        }
        
        @Override
        protected Node[] createNodes(LocalConfigurationVariable key) {
            return new ConfigurationVariableNode[] { new ConfigurationVariableNode(key) };
        }
    }

}
