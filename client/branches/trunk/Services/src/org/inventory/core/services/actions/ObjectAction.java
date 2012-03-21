/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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
 * A generic object action
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface ObjectAction extends Action{

    public static final int CREATE = 0;
    public static final int EDIT = 1;
    public static final int DELETE = 2;

    public void setObject(LocalObjectLight lol) throws ObjectActionException;
    public int getType();
}
