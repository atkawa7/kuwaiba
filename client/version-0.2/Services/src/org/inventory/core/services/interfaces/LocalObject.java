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

import java.util.HashMap;

/**
 * Instances of this class are proxy objects representing entities
 * @author Charles Edward Bedon Cortazar <charles.bedon@zoho.com>
 */
public interface LocalObject extends LocalObjectLight{
    public HashMap<String,Object> getAttributes();
    public String getClassName();
    public Long getOid();
    public Object getAttribute(String name);
    public LocalClassMetadata getObjectMetadata();
    public void setLocalObject(String className, String[] attributes, Object[] values);
    public void setObjectMetadata(LocalClassMetadata metaForClass);
    public void setOid(Long oid);
}
