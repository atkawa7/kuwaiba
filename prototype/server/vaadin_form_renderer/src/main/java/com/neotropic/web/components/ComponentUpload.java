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

import com.neotropic.api.forms.AbstractElement;
import com.neotropic.api.forms.Constants;
import com.neotropic.api.forms.ElementUpload;
import com.neotropic.api.forms.EventDescriptor;
import com.neotropic.forms.Variable;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ComponentUpload extends GraphicalComponent {
    
    public ComponentUpload() {
        super(new Upload());
    }
    
    @Override
    public Upload getComponent() {
        return (Upload) super.getComponent();
    }
    
    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementUpload) {
            ElementUpload upload = (ElementUpload) element;
            
            getComponent().setCaption(upload.getCaption());
            
            Uploader uploader = new Uploader();
            
            getComponent().setReceiver(uploader);
            getComponent().addSucceededListener(uploader);
        }
    }

    @Override
    public void onElementEvent(EventDescriptor event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private class Uploader implements Receiver, SucceededListener {
        private File file;
        
        public Uploader() {
        }
        
        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            FileOutputStream fileOutputStream = null;
            try {
                file = new File(Variable.FORM_FILES + "/" + filename);
                fileOutputStream = new FileOutputStream(file);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(ComponentUpload.class.getName()).log(Level.SEVERE, null, ex);
            }
            return fileOutputStream;
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {
            if (file != null) {
                fireComponentEvent(new EventDescriptor(
                    Constants.EventAttribute.ONPROPERTYCHANGE, 
                    Constants.Property.VALUE, file.getPath(), null));
                
                getComponent().setCaption(file.getName());
                getComponent().setButtonCaption("Change file");                
            }
        }
    }
    
}
