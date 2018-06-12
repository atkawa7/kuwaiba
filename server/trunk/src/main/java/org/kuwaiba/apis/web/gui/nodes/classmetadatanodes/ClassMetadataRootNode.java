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
package org.kuwaiba.apis.web.gui.nodes.classmetadatanodes;

import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;
import org.kuwaiba.apis.web.gui.nodes.AbstractRootNode;
import org.kuwaiba.interfaces.ws.toserialize.metadata.RemoteClassMetadataLight;
import org.kuwaiba.web.custom.tree.DynamicTree;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ClassMetadataRootNode extends AbstractRootNode {
    private final List<RemoteClassMetadataLight> children;

    public ClassMetadataRootNode(String caption, List<RemoteClassMetadataLight> children) {
        super(caption);
        this.children = children;
    }
    
    @Override
    public void setTree(DynamicTree tree) {
        super.setTree(tree);        
    }

    @Override
    public void expand() {
        if (getTree() == null)
            return;
        
        for (RemoteClassMetadataLight child: children) {
            ClassMetadataNode classNode = new ClassMetadataNode(child);
            classNode.setTree(getTree());
            //getTree().addItem(classNode);
            //getTree().setParent(classNode, this);
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
