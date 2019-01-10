/*
 *  Copyright 2010-2019 Neotropic SAS <contact@neotropic.co>
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
 */
package org.kuwaiba.apis.forms.components.impl;

import org.kuwaiba.apis.forms.elements.EventDescriptor;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.ElementImage;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Image;
import java.io.File;
import org.kuwaiba.apis.persistence.PersistenceService;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentImage extends GraphicalComponent {
    
    public ComponentImage() {
        super(new Image());
    }
    
    @Override
    public Image getComponent() {
        return (Image) super.getComponent();
    }

    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementImage) {
            ElementImage image = (ElementImage) element;
                                                            
            String processEnginePath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processEnginePath"));
            FileResource resource = new FileResource(new File(processEnginePath + "/form/img/" + image.getValue()));
                        
            getComponent().setSource(resource);
            
            if (image.getHeight() != null)
                getComponent().setWidth(image.getHeight());
            
            if (image.getWidth() != null)
                getComponent().setWidth(image.getWidth());
        }
        
    }
    
    @Override
    public void onElementEvent(EventDescriptor event) {
        
    }
    
}
