/**
 *  Copyright 2010-2017 Neotropic SAS <contact@neotropic.co>.
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
package org.inventory.core.templates.layouts.customshapes;

import org.inventory.communications.CommunicationsStub;
import org.inventory.core.services.GenericCacheInitializer;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class used to set the calls in the communications stub to initialize the cache to custom shapes
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@ServiceProvider(service=GenericCacheInitializer.class, position=2)
public class CustomShapeChacheInitializer extends GenericCacheInitializer {

    @Override
    public void initCache() {
        CommunicationsStub.getInstance().getCustomShapes(false);
    }
    
}
