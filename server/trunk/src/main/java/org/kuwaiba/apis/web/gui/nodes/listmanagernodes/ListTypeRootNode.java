/**
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
package org.kuwaiba.apis.web.gui.nodes.listmanagernodes;

import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.nodes.AbstractRootNode;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;

/**
 * This node should be used as root in all trees composed by list type nodes (e.g. List Manager)
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ListTypeRootNode extends AbstractRootNode {
    private final List<RemoteClassMetadataLight> initialChildren;
    
    public ListTypeRootNode(String caption, List<RemoteClassMetadataLight> initialChildren) {
        super(caption);
        this.initialChildren = initialChildren;
    }
    
    @Override
    public void expand() {
        
        if (getTree() == null) //If the tree has not been set previously, do nothing
            return;
        
        for (RemoteClassMetadataLight child : initialChildren) {
            ListTypeNode objectNode = new ListTypeNode(child);
            objectNode.setTree(getTree());
//            getTree().addItem(objectNode);
//            getTree().setParent(objectNode, this);
        }
    }

    @Override
    public void collapse() {}

    @Override
    public AbstractAction[] getActions() {
        return new AbstractAction[0];
    }

    @Override
    public void refresh(boolean recursive) {}
}
