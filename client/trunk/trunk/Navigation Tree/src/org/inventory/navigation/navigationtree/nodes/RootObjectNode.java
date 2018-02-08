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
 *  under the License.
 */
package org.inventory.navigation.navigationtree.nodes;

import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.util.Constants;
import org.inventory.navigation.navigationtree.nodes.actions.CreateBusinessObjectAction;
import org.inventory.navigation.navigationtree.nodes.actions.CreateBusinessObjectFromTemplateAction;
import org.inventory.navigation.navigationtree.nodes.actions.CreateMultipleBusinessObjectAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;

import org.openide.util.lookup.Lookups;

/**
 * Simple class to represent the root node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class RootObjectNode extends AbstractNode {

   public static final String DEFAULT_ICON_PATH = "org/inventory/navigation/navigationtree/res/root.png";

    public RootObjectNode(Children children) {
        super(children, Lookups.singleton(new LocalObjectLight(-1, Constants.DUMMYROOT, Constants.DUMMYROOT))); //Dummy object
        setDisplayName("Navigation Tree Root");
        setIconBaseWithExtension(DEFAULT_ICON_PATH);
    }

    @Override
    public Action[] getActions(boolean context){
        return new Action[]{
            CreateBusinessObjectAction.getInstance(this), 
            CreateMultipleBusinessObjectAction.getInstance(),
            CreateBusinessObjectFromTemplateAction.getInstance()/*, null, SystemAction.get(PasteAction.class)*/};
    }
}