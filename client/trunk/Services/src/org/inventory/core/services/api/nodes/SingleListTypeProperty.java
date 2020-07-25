/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
package org.inventory.core.services.api.nodes;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.inventory.communications.core.LocalObjectListItem;
import org.inventory.core.services.api.notifications.NotificationUtil;
import org.inventory.core.services.i18n.I18N;
import org.openide.nodes.PropertySupport;

/**
 * A dedicated property class for list type attributes
 * @author Charles Edward Bedon Cortazar {@literal <charles.bedon@kuwaiba.org>}
 */
public class SingleListTypeProperty extends PropertySupport.ReadWrite<LocalObjectListItem> {
    private PropertyEditor propertyEditor;
    private LocalObjectListItem value;
    private VetoableChangeListener listener;

    public SingleListTypeProperty(String name, String displayName, String toolTextTip, 
            List<LocalObjectListItem> list, VetoableChangeListener listener, LocalObjectListItem value) {
        super(name, LocalObjectListItem.class, displayName, toolTextTip);
        this.propertyEditor = new SingleListTypePropertyEditor(list, this);
        this.value = value == null ? new LocalObjectListItem() : value;
        this.listener = listener;
    }

    @Override
    public LocalObjectListItem getValue() throws IllegalAccessException, InvocationTargetException {        
       return value;
    }

    @Override
    public void setValue(LocalObjectListItem t) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try {
            this.listener.vetoableChange(new PropertyChangeEvent(this, getName(), value, t == null ? null : t.getId()));
            value = t;
        } catch (PropertyVetoException ex) {
            NotificationUtil.getInstance().showSimplePopup(I18N.gm("error"), 
                    NotificationUtil.ERROR_MESSAGE, ex.getLocalizedMessage());
        }
    }

    @Override
    public PropertyEditor getPropertyEditor(){
        return propertyEditor;
    }
}