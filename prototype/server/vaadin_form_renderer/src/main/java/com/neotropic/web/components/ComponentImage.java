/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>
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
package com.neotropic.web.components;

import com.neotropic.api.forms.EventDescriptor;
import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.ElementImage;
import com.neotropic.forms.Variable;
import com.vaadin.server.FileResource;
import com.vaadin.ui.Image;
import java.io.File;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
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
            
            FileResource resource = new FileResource(new File(Variable.FORM_RESOURCE_IMAGES + "/" + image.getValue()));
                        
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
