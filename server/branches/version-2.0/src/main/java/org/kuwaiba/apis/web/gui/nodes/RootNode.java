/*
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
package org.kuwaiba.apis.web.gui.nodes;

import org.kuwaiba.apis.web.gui.nodes.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.nodes.actions.AddObjectAction;
import com.vaadin.ui.Tree;

/**
 * Represent the root node in a tree
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public class RootNode extends AbstractNode {

    public RootNode(Tree tree) {
        super(tree);
        setDisplayName("Companies All Over the World");
    }

    @Override
    public AbstractAction[] getActions() {
        return new AbstractAction[] { new AddObjectAction() };
    }

    @Override
    public void refresh(boolean recursive) {}
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof RootNode; //There should be only one root node
    }

    @Override
    public int hashCode() {
        int hash = 3;
        return hash;
    }
    
}
