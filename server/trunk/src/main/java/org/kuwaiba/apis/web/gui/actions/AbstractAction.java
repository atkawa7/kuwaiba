/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.kuwaiba.apis.web.gui.actions;

import com.vaadin.event.Action;
import com.vaadin.server.Resource;
import org.kuwaiba.beans.WebserviceBean;

/**
 * Root of all actions in the system
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public abstract class AbstractAction extends Action {
    /**
     * Reference to the backend bean
     */
    protected WebserviceBean wsBean;
    
    public AbstractAction(String caption, WebserviceBean wsBean) {
        super(caption);
        this.wsBean = wsBean;
    }
    
    public AbstractAction(String caption, Resource icon) {
        super(caption, icon);
    }
    
    /**
     * What to do when the action is triggered 
     * @param sourceComponent The parent component that contains the object related to the action
     * @param targetObject The object related to the action (usually a node)
     */
    public abstract void actionPerformed (Object sourceComponent, Object targetObject);
    /**
     * What to do when the action has no context
     */
    public abstract void actionPerformed ();
    
    @Override
    public String toString() {
        return getCaption();
    }
}
