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
package org.inventory.core.services.interfaces;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

/**
 * Represents the basic information related to a class useful to render nodes
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface LocalClassMetadataLight extends Transferable{
    public static final DataFlavor DATA_FLAVOR =
            new DataFlavor(LocalClassMetadataLight.class,"Object/LocalClassMetadataLight");
    public String getClassName();
    public String getDisplayName();
    public String getDescription();
    public Boolean getIsAbstract();
    public String getPackageName();
    public Long getOid();
    public Image getSmallIcon();
    public Boolean isPhysicalNode();
    public Boolean isPhysicalEndpoint();
}
