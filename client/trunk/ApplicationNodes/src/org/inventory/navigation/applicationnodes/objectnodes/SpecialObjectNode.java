/*
 *  Copyright 2010-2015 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.navigation.applicationnodes.objectnodes;

import java.util.ArrayList;
import javax.swing.Action;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.core.services.api.actions.GenericObjectNodeAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.CreateSpecialBusinessObjectAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.DeleteBusinessObjectAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.EditObjectAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.RefreshObjectAction;
import org.inventory.navigation.applicationnodes.objectnodes.actions.ShowObjectIdAction;
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
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new CreateSpecialBusinessObjectAction(this));
        actions.add(refreshAction == null ? refreshAction = new RefreshObjectAction(this) : refreshAction);
        actions.add(editAction == null ? editAction = new EditObjectAction(this) : editAction);
        actions.add(deleteAction == null ? deleteAction = new DeleteBusinessObjectAction(this) : deleteAction);
        actions.add(null); //Separator
                for (GenericObjectNodeAction action : Lookup.getDefault().lookupAll(GenericObjectNodeAction.class)){
            if (action.getValidator() == null){
                action.setObject(object);
                actions.add(action);
            }else{
                if (com.getMetaForClass(object.getClassName(), false).getValidator(action.getValidator()) == 1){
                    action.setObject(object);
                    actions.add(action);
                }
            }
        }
        actions.add(null); //Separator
        actions.add(showObjectIdAction == null ? showObjectIdAction = new ShowObjectIdAction(object.getOid(), object.getClassName()) : showObjectIdAction);
        
        return actions.toArray(new Action[]{});
    }
}
