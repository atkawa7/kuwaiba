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
import com.vaadin.ui.Tree;
import org.kuwaiba.apis.persistence.metadata.ClassMetadataLight;

/**
 *
 * @author Adrian Martinez <adrian.martinez@kuwaiba.org>
 */
public class ClassMetadataNode extends AbstractNode<ClassMetadataLight> {

    public ClassMetadataNode(ClassMetadataLight object, Tree tree) {
        super(object, tree);
    }

    @Override
    public AbstractAction[] getActions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(boolean recursive) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
