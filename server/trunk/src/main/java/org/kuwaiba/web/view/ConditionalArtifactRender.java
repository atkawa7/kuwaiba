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
package org.kuwaiba.web.view;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;

/**
 * Renders a Conditional Artifact
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ConditionalArtifactRender implements ArtifactRenderer {
    private final RemoteArtifactDefinition remoteArtifactDefinition;
    private final RemoteArtifact remoteArtifact;
    private CheckBox checkBox;
    
    public ConditionalArtifactRender(RemoteArtifactDefinition remoteArtifactDefinition, RemoteArtifact remoteArtifact) {
        this.remoteArtifact = remoteArtifact;
        this.remoteArtifactDefinition = remoteArtifactDefinition;
    }

    @Override
    public Component renderArtifact() {
        checkBox = new CheckBox(remoteArtifactDefinition != null ? new String(remoteArtifactDefinition.getDefinition()) : "<Not Set>");
        checkBox.setValue(getConditionalArtifactContent());
        
        if (remoteArtifact != null)
            checkBox.setEnabled(false);
                    
        return checkBox;
    }

    @Override
    public byte[] getContent() throws Exception {
        String strContent = "<artifact type=\"conditional\"><value>" + checkBox.getValue() + "</value></artifact>";
        return strContent.getBytes();
    }

    @Override
    public List<StringPair> getSharedInformation() {
        return new ArrayList();
    }
    
    private boolean getConditionalArtifactContent() {
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
                            return Boolean.valueOf(reader.getElementText());
                    }
                }

            } catch (Exception ex) {
                return false;
            }
        }
        return false;
    }
    
}
