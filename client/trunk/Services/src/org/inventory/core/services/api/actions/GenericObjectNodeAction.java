/*
 *  Copyright 2010-2014 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.services.api.actions;

import javax.swing.AbstractAction;
import org.inventory.communications.core.LocalObjectLight;


/**
 * Superclass to all actions related to a node
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public abstract class GenericObjectNodeAction extends AbstractAction {
    protected LocalObjectLight object;

    public void setObject(LocalObjectLight object) {
        this.object = object;
    }
    
    /**
     * What instances support this action
     * @return A validator. See class Constants (in Application Nodes) for possible values so far.
     * You can add your own if the server supports them
     */
    public abstract String getValidator();
}
