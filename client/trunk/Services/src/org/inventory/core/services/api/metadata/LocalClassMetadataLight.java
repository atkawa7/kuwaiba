/**
 *  Copyright 2010, 2011, 2012, 2013 Neotropic SAS <contact@neotropic.co>.
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

package org.inventory.core.services.api.metadata;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeListener;

/**
 * Represents the basic information related to a class useful to render nodes
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface LocalClassMetadataLight extends Transferable{
    public static final DataFlavor DATA_FLAVOR =
            new DataFlavor(LocalClassMetadataLight.class,"Object/LocalClassMetadataLight");
    
    public String getClassName();
    public void setClassName(String className);
    public String getDisplayName();
    public void setDisplayName(String displayName);
    public String getParentName();
    public boolean isAbstract();
    public void setAbstract(boolean _abstract);
    public boolean isViewable();
    public void setViewable(boolean viewable);
    public boolean isCustom();
    public void setCustom(boolean custom);
    public boolean isInDesign();
    public void setInDesign(boolean inDesign);
    public boolean isListType();
    public void setListType(boolean listType);
    public long getOid();
    public void setOid(long oid);
    public Image getSmallIcon();
    public void setSmallIcon(Image newIcon);
    /**
     * Retrieves the value of a given validator
     * @param validatorName validator's name
     * @return value for the given validator. false if the validator is not present
     */
    public int getValidator(String validatorName);
    public void firePropertyChangeEvent(String property, Object oldValue, Object newValue);
    public void addPropertyChangeListener(PropertyChangeListener listener);
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
