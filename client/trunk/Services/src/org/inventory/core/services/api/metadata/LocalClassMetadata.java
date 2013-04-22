/*
 *  Copyright 2010 Charles Edward Bedon Cortazar <charles.bedon@zoho.com>.
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

/**
 * Classes implementing this interface are proxy classes, whose instances represent the metadata information associated to a class
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public interface LocalClassMetadata extends LocalClassMetadataLight{
    
    public String getDescription();
    public void setDescription(String description);
    public Image getIcon();
    public void setIcon(Image icon);
    public boolean isCountable();
    public void setCountable(boolean countable);
    //Attributes section
    public boolean isVisible(String att);
    public String getDisplayNameForAttribute(String att);
    public String getTypeForAttribute(String att);
    public String getDescriptionForAttribute(String att);
    public int getMappingForAttribute(String att);
    public LocalAttributeMetadata[] getAttributes();
    
    public LocalClassMetadataLight asLocalClassMetadataLight();
}
