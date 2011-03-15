/*
 *  Copyright 2011 Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>.
 * 
 *   Licensed under the EPL License, Version 1.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *        http://www.eclipse.org/legal/epl-v10.html
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 */

package org.inventory.communications;

import org.inventory.communications.core.LocalAttributeWrapperImpl;
import org.inventory.communications.core.LocalClassWrapperImpl;
import org.inventory.communications.core.LocalObjectImpl;
import org.inventory.communications.core.LocalObjectLightImpl;
import org.inventory.core.services.api.LocalObject;
import org.inventory.core.services.api.LocalObjectLight;
import org.inventory.core.services.api.metadata.LocalAttributeWrapper;
import org.inventory.core.services.api.metadata.LocalClassWrapper;

/**
 * This is a factory used to provide implementations for all the interfaces implemented in this module
 * @author Charles Edward Bedon Cortazar <charles.bedon@kuwaiba.org>
 */
public class LocalStuffFactory {
    public static LocalObject createLocalObject(){
        return new LocalObjectImpl();
    }

    public static LocalObjectLight createLocalObjectLight(){
        return new LocalObjectLightImpl();
    }

    public static LocalAttributeWrapper createLocalAttributeWrapper() {
        return new LocalAttributeWrapperImpl();
    }

    public static LocalClassWrapper createLocalClassWrapper() {
        return new LocalClassWrapperImpl();
    }
}
