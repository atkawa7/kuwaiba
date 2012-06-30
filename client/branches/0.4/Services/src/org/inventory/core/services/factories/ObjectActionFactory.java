/*
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.services.factories;

import org.inventory.core.services.actions.ObjectAction;
import org.inventory.core.services.exceptions.ObjectActionException;
import org.inventory.core.services.api.LocalObjectLight;
import org.openide.util.Lookup;

/**
 * This factory is used to get common actions over object nodes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class ObjectActionFactory {

    public static ObjectAction createEditAction(LocalObjectLight lol) throws ObjectActionException{
         for (ObjectAction action : Lookup.getDefault().lookupAll(ObjectAction.class)){
             if (action.getType() == ObjectAction.EDIT){
                 action.setObject(lol);
                 return action;
             }
         }
         return null;
    }

    public static ObjectAction createCreateBusinessObjectAction(LocalObjectLight lol){
         for (ObjectAction action : Lookup.getDefault().lookupAll(ObjectAction.class)){
             if (action.getType() == ObjectAction.CREATE_BUSINESS_OBJECT)
                 return action;
         }
         return null;
    }

    public static ObjectAction createDeleteBusinessObjectAction(LocalObjectLight lol) throws ObjectActionException{
         for (ObjectAction action : Lookup.getDefault().lookupAll(ObjectAction.class)){
             if (action.getType() == ObjectAction.DELETE_BUSINESS_OBJECT){
                 action.setObject(lol);
                 return action;
             }
         }
         return null;
    }

    public static ObjectAction createCreateListTypeObjectAction(LocalObjectLight lol){
         for (ObjectAction action : Lookup.getDefault().lookupAll(ObjectAction.class)){
             if (action.getType() == ObjectAction.CREATE_LIST_TYPE)
                 return action;
         }
         return null;
    }

    public static ObjectAction createDeleteListTypeAction(LocalObjectLight lol) throws ObjectActionException{
         for (ObjectAction action : Lookup.getDefault().lookupAll(ObjectAction.class)){
             if (action.getType() == ObjectAction.DELETE_LIST_TYPE){
                 action.setObject(lol);
                 return action;
             }
         }
         return null;
    }
}
