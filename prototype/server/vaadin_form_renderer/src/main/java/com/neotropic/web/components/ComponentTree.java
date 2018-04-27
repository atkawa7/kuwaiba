/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.neotropic.web.components;

import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.Constants;
import com.neotropic.api.forms.ElementTree;
import com.neotropic.api.forms.EventDescriptor;
import com.vaadin.ui.Tree;
import com.vaadin.ui.Tree.ItemClickListener;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentTree extends GraphicalComponent {

    public ComponentTree(TreeWrapper treeWrapper) {
        super(treeWrapper.getTree());
    }
    
    @Override
    public Tree getComponent() {
        return (Tree) super.getComponent();
    }

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementTree) {
            ElementTree tree = (ElementTree) element;
            
            getComponent().addItemClickListener(new ItemClickListener() {
                
                @Override
                public void itemClick(Tree.ItemClick event) {
                    
                    fireComponentEvent(new EventDescriptor(
                        Constants.EventAttribute.ONPROPERTYCHANGE, 
                        Constants.Property.VALUE, event.getItem(), null));
                }
            });
        }
    }

    @Override
    public void onElementEvent(EventDescriptor event) {
        //TODO: implements events
    }
    
}
