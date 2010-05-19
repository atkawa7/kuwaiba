/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
package org.inventory.navigation.navigationtree.nodes;

import javax.swing.Action;
import org.inventory.navigation.navigationtree.actions.Create;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

/**
 * Simple class to represent the root node
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public class RootObjectNode extends AbstractNode{

    public RootObjectNode(Children _children) {
        super(_children);
    }

    @Override
    public Action[] getActions(boolean context){
        Create createAction = new Create(this);
        return new Action[]{createAction};
    }
}
