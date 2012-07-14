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

package org.inventory.core.services.actions;

import javax.swing.Action;
import org.inventory.core.services.exceptions.ObjectActionException;
import org.inventory.core.services.api.LocalObjectLight;

/**
 * The template for all actions that could be associated to a node (representing a business or application object)
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface ObjectAction extends Action{

    public static final int CREATE_BUSINESS_OBJECT = 1;
    public static final int EDIT = 2;
    public static final int DELETE_BUSINESS_OBJECT = 3;
    public static final int CREATE_LIST_TYPE = 4;
    public static final int DELETE_LIST_TYPE = 5;

    public void setObject(LocalObjectLight lol) throws ObjectActionException;
    public int getType();
}
