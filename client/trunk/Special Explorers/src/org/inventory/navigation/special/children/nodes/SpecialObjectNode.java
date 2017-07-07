/*
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
 * 
 */
package org.inventory.navigation.special.children.nodes;

import java.util.ArrayList;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.navigation.navigationtree.nodes.ObjectNode;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.navigation.special.children.nodes.actions.CreateSpecialBusinessObjectAction;
import org.inventory.navigation.navigationtree.nodes.actions.EditObjectAction;
import org.inventory.navigation.navigationtree.nodes.actions.RefreshObjectAction;
import org.inventory.navigation.navigationtree.nodes.actions.ShowMoreInformationAction;
import org.inventory.navigation.special.children.nodes.actions.CreateMultipleSpecialBusinessObjectAction;
import org.inventory.navigation.special.children.nodes.actions.CreateSpecialBusinessObjectFromTemplateAction;
import org.openide.util.Lookup;

/**
 * It's like an ObjectNode, but you can filter what actions would be shown. Its children
 * are "special children", that is, they're not children as in the containment hierarchy but 
 * children as defined by a particular model
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class SpecialObjectNode extends ObjectNode {
    
    public SpecialObjectNode(LocalObjectLight anObject) {
        super(anObject);
        setChildren(new SpecialChildren());
    }
    
    @Override
    public Action[] getActions(boolean context) {
        ArrayList<Action> actions = new ArrayList<>();
        actions.add(new CreateSpecialBusinessObjectAction(this)); //This changes from ObjectNode
//        actions.add(new CreateMultipleSpecialBusinessObjectAction(this)); //This changes from ObjectNode
        actions.add(new CreateSpecialBusinessObjectFromTemplateAction()); //This changes from ObjectNode
        actions.add(refreshAction == null ? refreshAction = new RefreshObjectAction(this) : refreshAction);
        actions.add(editAction == null ? editAction = new EditObjectAction(this) : editAction);
        actions.add(explorerAction);
        actions.add(null); //Separator
        for (GenericObjectNodeAction action : Lookup.getDefault().lookupAll(GenericObjectNodeAction.class)){
            if (action.getValidator() == null){
                actions.add(action);
            }else{
                if (com.getMetaForClass(getObject().getClassName(), false).getValidator(action.getValidator()) == 1){
                    actions.add(action);
                }
            }
        }
        actions.add(null); //Separator
        actions.add(showMoreInformationAction == null ? showMoreInformationAction = new ShowMoreInformationAction(getObject().getOid(), getObject().getClassName()) : showMoreInformationAction);
        
        return actions.toArray(new Action[]{});
    }
}
