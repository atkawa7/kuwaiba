/**
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.inventory.navigation.applicationnodes.classmetadatanodes;

import javax.swing.Action;
import org.inventory.communications.LocalStuffFactory;
import org.inventory.navigation.applicationnodes.classmetadatanodes.action.CreateClassAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.lookup.Lookups;

/**
 * Class to represent the root for class metadata nodes
 * @author Adrian Martinez Molina <adrian.martinez@kuwaiba.org>
 */
public class RootClassMetadataNode extends AbstractNode{
    static final String DEFAULT_ICON_PATH = "org/inventory/navigation/applicationnodes/res/root.png";

    public RootClassMetadataNode(Children children) {
        super(children, Lookups.singleton(LocalStuffFactory.createLocalClassMetadataLight()));
        setDisplayName(java.util.ResourceBundle.getBundle("org/inventory/navigation/applicationnodes/Bundle").getString("LBL_ROOT"));
        setIconBaseWithExtension(DEFAULT_ICON_PATH);     
    }
    
    @Override
    public Action[] getActions(boolean context){
        CreateClassAction createAction = new CreateClassAction(this);
        return new Action[]{createAction};
    }
}
