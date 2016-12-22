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
 */
package org.kuwaiba.apis.web.gui.nodes.actions;

import com.vaadin.event.Action;
import com.vaadin.server.Resource;

/**
 * Root of all actions in the system
 * @author Charles Bedon <charles.bedon@kuwaiba.org>
 */
public abstract class AbstractAction extends Action {
    public AbstractAction(String caption) {
        super(caption);
    }
    
    public AbstractAction(String caption, Resource icon) {
        super(caption, icon);
    }
    
    /**
     * What to do when the action is triggered 
     * @param sourceComponent The parent component that
     * @param targetObject
     */
    public abstract void actionPerformed (Object sourceComponent, Object targetObject);
}
