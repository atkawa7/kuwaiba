/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.web.modules.lists.actions;

import org.kuwaiba.apis.web.gui.actions.AbstractAction;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ActionsFactory {
    static CreateListTypeChildAction createListTypeChildAction;
    static DeleteListTypeAction deleteListTypeAction;
    
    public static AbstractAction createCreateListTypeChildAction(){
        if (createListTypeChildAction == null)
            createListTypeChildAction = new CreateListTypeChildAction();
        return createListTypeChildAction;
    }
    
    public static AbstractAction createDeleteListTypeAction(){
        if (deleteListTypeAction == null)
            deleteListTypeAction = new DeleteListTypeAction();
        return deleteListTypeAction;
    }
}
