/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>.
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
package org.neotropic.kuwaiba.web.icons;


import com.vaadin.flow.server.AbstractStreamResource;
import org.neotropic.util.visual.resources.AbstractResourceFactory;
import org.neotropic.util.visual.icons.IconGenerator;
import org.neotropic.util.visual.tree.nodes.AbstractNode;

/**
 * Implementation that provides the ability to generate small icons for kuwaiba objects
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 * @author Orlando Paz {@literal <orlando.paz@kuwaiba.org>}
 */

public class BasicIconGenerator extends IconGenerator<AbstractNode> {
        
    public BasicIconGenerator(AbstractResourceFactory resourceFactory) {
        super(resourceFactory);
    }  
    
    @Override
    public AbstractStreamResource apply(AbstractNode item) {
       
       return resourceFactory.getClassSmallIcon(item.getClassName()); 
       
    }
    
}
