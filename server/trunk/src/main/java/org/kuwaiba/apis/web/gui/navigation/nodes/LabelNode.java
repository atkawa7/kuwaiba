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

package org.kuwaiba.apis.web.gui.navigation.nodes;

import java.awt.Color;
import java.util.List;
import org.kuwaiba.apis.web.gui.actions.AbstractAction;

/**
 * String a node that displays a simple text string
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class LabelNode extends AbstractNode<String>{
    /** 
     * The color of the icon to be used for the node
     */
    private Color color;
    /**
     * The default children
     */
    private List<AbstractNode> children;
    /**
     * The default constructor
     * @param label The label to be displayed
     * @param children The default children. Please note, then, that label node children are not retrieved lazily
     * @param color The color to generate the 12x12 square icon
     */
    public LabelNode(String label, List<AbstractNode> children, Color color) {
        super(label);
        this.children = children;
        this.color = color;
    }
    
    /**
     * Constructor without a default set of children
     * @param label The label to be displayed
     * @param color The color to generate the 12x12 square icon
     */
    public LabelNode(String label, Color color) {
        super(label);
        this.color = color;
    }
    
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public List<AbstractNode> getChildren() {
        return children;
    }

    public void setChildren(List<AbstractNode> children) {
        this.children = children;
    }
    
    @Override
    public String toString() {
        return object;
    }

    @Override
    public AbstractAction[] getActions() {
        return new AbstractAction[0];
    }

    @Override
    public void refresh(boolean recursive) { }
}
