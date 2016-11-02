/*
 *  Copyright 2010-2016 Neotropic SAS <contact@neotropic.co>.
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
 *  under the License.
 */
package org.inventory.core.templates.nodes.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 * Listener that commits a change performed to a template element. It's a singleton, and you should add it to the listeners list of your LocalObject if you want the changes to be properly saved into the data base
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class TemplateElementPropertyListener implements PropertyChangeListener {
    private static CommunicationsStub com = CommunicationsStub.getInstance();
    private static TemplateElementPropertyListener instance;
    
    private TemplateElementPropertyListener() {}
    
    public static TemplateElementPropertyListener getInstance() {
        if (instance == null)
            instance = new TemplateElementPropertyListener();
        return instance;
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        LocalObjectLight affectedObject = (LocalObjectLight)evt.getSource();
        if (!com.updateTemplateElement(affectedObject.getClassName(), affectedObject.getOid(),
            new String[] {evt.getPropertyName()}, 
            new String[] {evt.getNewValue() == null ? null : (evt.getNewValue() instanceof LocalObjectListItem ? String.valueOf(((LocalObjectListItem)evt.getNewValue()).getId()) : String.valueOf(evt.getNewValue())) }))
                NotificationUtil.getInstance().showSimplePopup("Error", NotificationUtil.ERROR_MESSAGE, com.getError());
        
    }
    
}
