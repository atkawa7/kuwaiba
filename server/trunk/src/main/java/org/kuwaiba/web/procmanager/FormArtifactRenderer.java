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

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import java.util.ArrayList;
import java.util.List;
import org.kuwaiba.apis.forms.FormInstanceCreator;
import org.kuwaiba.apis.forms.FormInstanceLoader;
import org.kuwaiba.apis.forms.FormRenderer;
import org.kuwaiba.apis.forms.elements.FormDefinitionLoader;
import org.kuwaiba.apis.forms.elements.AbstractFormInstanceLoader;
import org.kuwaiba.apis.persistence.util.StringPair;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FormArtifactRenderer implements ArtifactRenderer {
    private final RemoteArtifactDefinition artifactDefinition;
    private final RemoteArtifact artifact;
    private final WebserviceBean wsBean;
    private final RemoteSession session;
    
    private FormRenderer formRenderer;
    private FormInstanceCreator formInstanceCreator;
////    private final List<RemoteArtifact> remoteArtifacts;
    //TODO: only to test the concept the communicator share the information of one form
    //in the future it can be suport many forms
//    public class FormInformationCommunicator {
//        private HashMap<String, String> publicInformation;   
//        
//        public FormInformationCommunicator(HashMap<String, String> publicInformation) {
//            this.publicInformation = publicInformation;
//        }
//        
//        public HashMap<String, String> getPublicInformation() {
//            return publicInformation;
//        }
//        
//        public void getPublicInformation(HashMap<String, String> publicInformation) {
//            this.publicInformation = publicInformation;
//        }
//    }
    private RemoteProcessInstance processInstance;
    
    public FormArtifactRenderer(RemoteArtifactDefinition artifactDefinition, RemoteArtifact artifact, WebserviceBean wsBean, RemoteSession session, RemoteProcessInstance processInstance/*, List<RemoteArtifact> remoteArtifacts*/) {
        this.artifactDefinition = artifactDefinition;
        this.artifact = artifact;
        this.wsBean = wsBean;
        this.session = session;
        this.processInstance = processInstance;
////        this.remoteArtifacts = remoteArtifacts;
    }
        
    @Override
    public Component renderArtifact() {
        
        if (artifactDefinition != null) {
                        
            if (artifact != null) {
                
                if (artifact.getContent() != null) {
                    
                    if (artifactDefinition.getDefinition() == null)
                        return new Label("The Artifact Definition is empty");
                                        
                    AbstractFormInstanceLoader fil = new FormInstanceLoader(wsBean, session);
                    
                    FormDefinitionLoader formLoader = fil.load(artifactDefinition.getDefinition(), artifact.getContent());
                                        
                    formRenderer = new FormRenderer(formLoader, processInstance);
                    formRenderer.render(wsBean, session);

                    return formRenderer;
                    
                } else {
                    return new Label("The Artifact Content is empty");
                }
            } else {
                if (artifactDefinition.getDefinition() != null) {
                    FormDefinitionLoader formLoader = new FormDefinitionLoader(artifactDefinition.getDefinition());
                    formLoader.build();
                    
                    formRenderer = new FormRenderer(formLoader, processInstance);
                    formRenderer.render(wsBean, session);

                    return formRenderer;
                } else
                    return new Label("The Artifact Definition is empty");
            }
        }
        return null;
    }

    @Override
    public byte[] getContent() throws Exception {
        formInstanceCreator = new FormInstanceCreator(formRenderer.getFormStructure(), wsBean, session);
        return formInstanceCreator.getStructure();
    }
    
    @Override
    public List<StringPair> getSharedInformation() {
        
        List<StringPair> pairs = new ArrayList();
        
        for (String pair : formInstanceCreator.getSharedInformation().keySet()) {
            
            String key = pair;
            String value = formInstanceCreator.getSharedInformation().get(pair);
            
            pairs.add(new StringPair(key, value));
        }
        return pairs;
    }
    
}
