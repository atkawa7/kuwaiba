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

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.AbstractClientConnector;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.kuwaiba.apis.forms.elements.AbstractElement;
import org.kuwaiba.apis.forms.elements.Constants;
import org.kuwaiba.apis.forms.elements.ElementUpload;
import org.kuwaiba.apis.forms.elements.EventDescriptor;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.kuwaiba.apis.forms.components.ComponentEventListener;
import org.kuwaiba.apis.persistence.PersistenceService;
import org.kuwaiba.util.i18n.I18N;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentUpload extends GraphicalComponent {
    private Upload upload;
    private Link link;
    
    public ComponentUpload() {
        super(new HorizontalLayout());
    }
    
    @Override
    public HorizontalLayout getComponent() {
        return (HorizontalLayout) super.getComponent();
    }
    
    @Override
    public void initFromElement(AbstractElement element) {
        if (element instanceof ElementUpload) {
            ElementUpload elementUpload = (ElementUpload) element;
            
            getComponent().addComponent(link = new Link());
            link.setIcon(VaadinIcons.DOWNLOAD_ALT);
            
            getComponent().addComponent(upload = new Upload());
            
            Uploader uploader = new Uploader(elementUpload);
            upload.setReceiver(uploader);
            upload.addSucceededListener(uploader);
            
            configureComponent(elementUpload, true);
        }
    }

    @Override
    public void onElementEvent(EventDescriptor event) {
        if (Constants.EventAttribute.ONPROPERTYCHANGE.equals(event.getEventName())) {
            
            if (Constants.Property.VALUE.equals(event.getPropertyName()) || 
                Constants.Property.CAPTION.equals(event.getPropertyName())) {
                
                ComponentEventListener componentEventListener = getComponentEventListener();
                                                
                if (componentEventListener instanceof ElementUpload)
                    configureComponent((ElementUpload) componentEventListener, true);
            }
        }
    }
    
    private class Uploader implements Receiver, SucceededListener {
                      
        private File file;
        private String fileName;
        private final ElementUpload elementUpload;
                
        public Uploader(ElementUpload elementUpload) {
            this.elementUpload = elementUpload;
        }
        
        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            FileOutputStream fileOutputStream = null;
            try {
                String processesPath = String.valueOf(PersistenceService.getInstance().getApplicationEntityManager().getConfiguration().get("processesPath"));
                fileName = filename;
                file = new File(processesPath + "/" + UUID.randomUUID());                
                fileOutputStream = new FileOutputStream(file);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(ComponentUpload.class.getName()).log(Level.SEVERE, null, ex);
            }
            return fileOutputStream;
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {
            if (file != null) {
                String oldFilePath = elementUpload.getValue() != null ? elementUpload.getValue().toString() : null;
                
                fireComponentEvent(new EventDescriptor(
                    Constants.EventAttribute.ONPROPERTYCHANGE, 
                    Constants.Property.CAPTION, fileName, null));
                
                fireComponentEvent(new EventDescriptor(
                    Constants.EventAttribute.ONPROPERTYCHANGE, 
                    Constants.Property.VALUE, file.getPath(), null));
                                                
                ComponentEventListener componentEventListener = getComponentEventListener();
                                
                if (componentEventListener instanceof ElementUpload)
                    configureComponent((ElementUpload) componentEventListener, false);
                
                fireComponentEvent(new EventDescriptor(Constants.EventAttribute.ONUPLOADSUCCEEDED));
                
                if (oldFilePath != null) 
                {
                    File oldfile = new File(oldFilePath);
                    oldfile.delete();
                }
            }
        }
    }
    
    private void configureComponent(ElementUpload elementUpload, boolean fromInit) {
        
        if (elementUpload != null) {
            
            if (elementUpload.getCaption() != null && elementUpload.getValue() != null) {
                
                link.setVisible(true);
                link.setCaption(elementUpload.getCaption());
                StreamResource streamResource = getStreamResource(elementUpload);
                link.setResource(streamResource);
                upload.setButtonCaption(I18N.gm("update_file")); 
                
                if (!fromInit) {
                    
                    String anUrl = getResourceURL(link, streamResource);

                    fireComponentEvent(new EventDescriptor(
                        Constants.EventAttribute.ONPROPERTYCHANGE, 
                        ElementUpload.ELEMENT_UPLOAD_URL, anUrl, null));
                }
            }
            else {                
                link.setVisible(false);
                upload.setButtonCaption(I18N.gm("upload_file"));
            }
        }
    }
    
    public String getUploadUrl() {
        try {
            return getResourceURL(link, (StreamResource) link.getResource());
        } catch(Exception ex) {
            return "";
        }
    }
    
    private String getResourceURL(AbstractClientConnector connector,StreamResource resource){
        String path = UI.getCurrent().getPage().getLocation().getPath();
        String protocol = UI.getCurrent().getPage().getLocation().getScheme();
        String currentUrl = UI.getCurrent().getPage().getLocation().getAuthority();
        String cid = connector.getConnectorId();
        Integer uiId = connector.getUI().getUIId();
        String filename = resource.getFilename();

        return protocol+"://"+currentUrl + path +"/APP/connector/"+uiId+"/"+cid+"/href/"+filename;
    }
    
    private StreamResource getStreamResource(ElementUpload elementUpload) {
        StreamResource streamResource = new StreamResource(new StreamResource.StreamSource() {
            
            @Override
            public InputStream getStream() {
                try {
                    File file = new File((String) elementUpload.getValue());
                    return new FileInputStream(file);
                } catch (FileNotFoundException ex) {
                    Notification.show(I18N.gm("error"), "File cannot be found", Notification.Type.ERROR_MESSAGE);
                    return null;
                }
            }
        }, elementUpload.getCaption());
                
        streamResource.setCacheTime(0);
                
        return streamResource;
    }
    
}
