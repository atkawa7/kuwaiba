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
import org.inventory.navigation.navigationtree.nodes.actions.ActionGroupActionsFactory;
import org.inventory.navigation.navigationtree.nodes.actions.ActionsGroupType;
import org.inventory.navigation.navigationtree.nodes.actions.GenericObjectNodeAction;
import org.inventory.navigation.special.children.nodes.actions.CreateSpecialBusinessObjectAction;
import org.inventory.navigation.navigationtree.nodes.actions.EditObjectAction;
import org.inventory.navigation.navigationtree.nodes.actions.UpdateNodeAction;
import org.inventory.navigation.navigationtree.nodes.actions.ShowMoreInformationAction;
import org.inventory.navigation.special.children.nodes.actions.CreateMultipleSpecialBusinessObjectAction;
import org.inventory.navigation.special.children.nodes.actions.CreateSpecialBusinessObjectFromTemplateAction;
import org.openide.util.Lookup;

/**
 * It's like an ObjectNode, but you can filter what actions would be shown. Its children
 * are "special children", that is, they're not children as in the standard containment hierarchy but 
 * children as defined by a particular model.
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
        actions.add(CreateSpecialBusinessObjectAction.getInstance()); //This changes from ObjectNode       
        actions.add(CreateMultipleSpecialBusinessObjectAction.getInstance()); //This changes from ObjectNode
        actions.add(CreateSpecialBusinessObjectFromTemplateAction.getInstance()); //This changes from ObjectNode
        actions.add(UpdateNodeAction.getInstance(this));
        actions.add(EditObjectAction.getInstance(this));
        actions.add(explorerAction);
        actions.add(null); //Separator
        for (GenericObjectNodeAction action : Lookup.getDefault().lookupAll(GenericObjectNodeAction.class)){
            if (action.getClass().getAnnotation(ActionsGroupType.class) != null)
                continue;
            
            if (action.appliesTo() != null) {
                for (String className : action.appliesTo()) {
                    if (com.isSubclassOf(getObject().getClassName(), className)) {
                        actions.add(action);
                        break;
                    }
                }
            } else {
                if (action.getValidators() != null) {
                    for (String validator : action.getValidators()) {
                        if (com.getMetaForClass(getObject().getClassName(), false).getValidator(validator) == 1) {
                            actions.add(action);
                            break;
                        }
                    }                                                
                } else {
                    actions.add(action);
                }                
            }
        }
        actions.add(ActionGroupActionsFactory.getInstanceOfOpenViewGroupActions());
        actions.add(ActionGroupActionsFactory.getInstanceOfRelateToGroupActions());
        actions.add(ActionGroupActionsFactory.getInstanceOfReleaseFromGroupActions());
        
        actions.add(null); //Separator
        actions.add(ShowMoreInformationAction.getInstance(getObject().getOid(), getObject().getClassName()));
        
        return actions.toArray(new Action[]{});
    }
}