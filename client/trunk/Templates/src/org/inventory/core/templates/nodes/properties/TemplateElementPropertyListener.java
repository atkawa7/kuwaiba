/*
 * Copyright (c) 2016 gir.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    gir - initial API and implementation and/or initial documentation
 */
package org.inventory.core.templates.nodes.properties;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.inventory.communications.CommunicationsStub;
import org.inventory.communications.core.LocalObjectLight;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.core.services.api.notifications.NotificationUtil;

/**
 *
 * @author gir
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
