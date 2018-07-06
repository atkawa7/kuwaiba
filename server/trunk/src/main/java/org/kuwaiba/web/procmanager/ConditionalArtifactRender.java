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

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import java.io.ByteArrayInputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.forms.ScriptQueryExecutorImpl;
import org.kuwaiba.apis.forms.elements.FunctionRunner;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * Renders a Conditional Artifact
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class ConditionalArtifactRender extends ArtifactRenderer {
    private final RemoteArtifactDefinition artifactDefinition;
    private final RemoteArtifact remoteArtifact;
    private CheckBox checkBox;
    
    private final WebserviceBean wsBean;
    private final RemoteSession session;
    private final RemoteProcessInstance processInstance;
    
    public ConditionalArtifactRender(RemoteArtifactDefinition remoteArtifactDefinition, RemoteArtifact remoteArtifact, WebserviceBean wsBean, RemoteSession session, RemoteProcessInstance processInstance) {
        this.remoteArtifact = remoteArtifact;
        this.artifactDefinition = remoteArtifactDefinition;
        
        this.wsBean = wsBean;
        this.session = session;
        this.processInstance = processInstance;
    }

    @Override
    public Component renderArtifact() {
        if (artifactDefinition.getPreconditionsScript() != null) {
            ScriptQueryExecutorImpl scriptQueryExecutorImpl = new ScriptQueryExecutorImpl(wsBean, session, processInstance);
            String script = new String(artifactDefinition.getPreconditionsScript());
            FunctionRunner functionRunner = new FunctionRunner("precondition", null, script);
            functionRunner.setScriptQueryExecutor(scriptQueryExecutorImpl);

            Object result = functionRunner.run(null);

            if (!Boolean.valueOf(result.toString()))
                return new Label(result.toString());
        }
        
        checkBox = new CheckBox(artifactDefinition != null ? new String(artifactDefinition.getDefinition()) : "<Not Set>");
        checkBox.setValue(getConditionalArtifactContent());
        
////        if (remoteArtifact != null)
////            checkBox.setEnabled(false);
                    
        return checkBox;
    }

    @Override
    public byte[] getContent() throws Exception {
        String strContent = "<artifact type=\"conditional\"><value>" + checkBox.getValue() + "</value></artifact>";
        return strContent.getBytes();
    }

////    @Override
////    public List<StringPair> getSharedInformation() {
////        return new ArrayList();
////    }
    
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
