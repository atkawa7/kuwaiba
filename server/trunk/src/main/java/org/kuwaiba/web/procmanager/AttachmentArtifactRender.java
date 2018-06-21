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
package org.kuwaiba.web.procmanager;

import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Upload;
import com.vaadin.ui.themes.ValoTheme;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.forms.components.impl.ComponentUpload;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;

/**
 * Renders a Attachment Artifact
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class AttachmentArtifactRender implements ArtifactRenderer {
    private final RemoteArtifactDefinition remoteArtifactDefinition;
    private final RemoteArtifact remoteArtifact;
    private AttachmentArtifactUploader uploader;
    private Upload upload;
    private Link link;
    
    private File file;
        
    AttachmentArtifactRender(RemoteArtifactDefinition remoteArtifactDefinition, RemoteArtifact remoteArtifact) {
        this.remoteArtifact = remoteArtifact;
        this.remoteArtifactDefinition = remoteArtifactDefinition;                
    }
    
    
    private void setAttachmentArtifactContent() {
        if (remoteArtifact != null) {
            try {
                byte[] content = remoteArtifact.getContent();

                XMLInputFactory xif = XMLInputFactory.newInstance();
                ByteArrayInputStream bais = new ByteArrayInputStream(content);
                XMLStreamReader reader = xif.createXMLStreamReader(bais);

                QName tagValue = new QName("value"); //NOI18N

                while (reader.hasNext()) {

                    int event = reader.next();

                    if (event == XMLStreamConstants.START_ELEMENT) {

                        if (reader.getName().equals(tagValue))
                            file = new File(reader.getElementText());
                    }
                }

            } catch (Exception ex) {
                Notification.show("Error", "Attachment cannot be found", Notification.Type.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public Component renderArtifact() {
        String strDefinition = remoteArtifactDefinition != null ? new String(remoteArtifactDefinition.getDefinition()) : null;
        if (strDefinition != null) {
            Label lbl = new Label(strDefinition);
            lbl.addStyleName(ValoTheme.LABEL_BOLD);
            
            uploader = new AttachmentArtifactUploader(); 
            
            upload= new Upload();
            upload.setButtonCaption("Upload File");
            
            upload.setReceiver(uploader);
            upload.addSucceededListener(uploader);
            
            link = new Link();
            link.setIcon(VaadinIcons.DOWNLOAD_ALT);
            link.setVisible(false);
                                    
            VerticalLayout vl = new VerticalLayout();            
            vl.setWidth("100%");
            
            HorizontalLayout hl = new HorizontalLayout();
            hl.addComponent(link);
            hl.addComponent(upload);
            
            vl.setSpacing(false);
            vl.addComponent(lbl);
            vl.addComponent(hl);
            
            if (remoteArtifact != null) {
                setAttachmentArtifactContent();
                setArtifactContentView();
            }
            return vl;
        }
        Label lbl = new Label("Error: The Artifact Definition not set");
        lbl.addStyleName(ValoTheme.LABEL_FAILURE);
        return lbl;
    }

    @Override
    public byte[] getContent() throws Exception {
        if (file == null)
            throw new Exception("The process can not continue to next activity meanwhile the attachment is not uploaded");
        String strContent = "<artifact type=\"attachment\"><value>" + file.getAbsolutePath() + "</value></artifact>";
        return strContent.getBytes();
    }

    @Override
    public List<StringPair> getSharedInformation() {
        return new ArrayList();
    }
    
    private class AttachmentArtifactUploader implements Upload.Receiver, Upload.SucceededListener {
        
        public AttachmentArtifactUploader() {
        }

        @Override
        public OutputStream receiveUpload(String filename, String mimeType) {
            FileOutputStream fileOutputStream = null;
            try {
                //TODO:
                file = new File("/data/attachments" + "/" + filename);
                fileOutputStream = new FileOutputStream(file);

            } catch (FileNotFoundException ex) {
                Logger.getLogger(ComponentUpload.class.getName()).log(Level.SEVERE, null, ex);
            }
            return fileOutputStream;
        }

        @Override
        public void uploadSucceeded(Upload.SucceededEvent event) {
            Notification.show("Success", String.format("File %s uploaded", file.getName()), Notification.Type.HUMANIZED_MESSAGE);
            setArtifactContentView();
        }
    }
    
    public void setArtifactContentView() {
        upload.setButtonCaption("Update File");
        link.setVisible(true);
        link.setResource(getStreamResource());
        link.setCaption(file.getName());
    }
    
    public StreamResource getStreamResource() {
        StreamResource streamResource = new StreamResource(new StreamResource.StreamSource() {
            
                @Override
                public InputStream getStream() {
                    try {
                        return new FileInputStream(file);
                    } catch (FileNotFoundException ex) {
                        Notification.show("Error", "File cannot be found", Notification.Type.ERROR_MESSAGE);
                        return null;
                    }
                }
            }, file.getName());
        
        streamResource.setCacheTime(0);
        
        return streamResource;
    }
    
}