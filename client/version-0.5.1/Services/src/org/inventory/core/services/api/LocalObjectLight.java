/**
 *  Copyright 2010, 2011, 2012 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.services.api;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeListener;


/**
 * This interface expose the business object shown in the explorer views (typically beanTreeView)
 * It only has basic information about the object (complete information is contained into a LocalObject instance)
 * 
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface LocalObjectLight extends Transferable {
    public static final DataFlavor DATA_FLAVOR =
            new DataFlavor(LocalObjectLight.class,"Object/LocalObjectLight");
    public String getClassName();
    public long getOid();
    public void setOid(long id);
    public String getName();
    public void setName(String text);
    public int getValidator(String label);
    public void firePropertyChangeEvent(String property, Object oldValue, Object newValue);
    public void addPropertyChangeListener(PropertyChangeListener listener);
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
