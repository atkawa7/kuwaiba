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

import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import org.kuwaiba.apis.forms.FormInstanceCreator;
import org.kuwaiba.apis.forms.FormInstanceLoader;
import org.kuwaiba.apis.forms.FormRenderer;
import org.kuwaiba.apis.forms.elements.FormDefinitionLoader;
import org.kuwaiba.apis.forms.elements.AbstractFormInstanceLoader;
import org.kuwaiba.beans.WebserviceBeanLocal;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FormArtifactRenderer implements ArtifactRenderer {
    private final RemoteArtifactDefinition artifactDefinition;
    private final RemoteArtifact artifact;
    private final WebserviceBeanLocal wsBean;
    private final RemoteSession session;
    
    private FormRenderer formRenderer;
    
    public FormArtifactRenderer(RemoteArtifactDefinition artifactDefinition, RemoteArtifact artifact, WebserviceBeanLocal wsBean, RemoteSession session) {
        this.artifactDefinition = artifactDefinition;
        this.artifact = artifact;
        this.wsBean = wsBean;
        this.session = session;
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
                                        
                    formRenderer = new FormRenderer(formLoader);
                    formRenderer.render(wsBean, session);

                    return formRenderer;
                    
                } else {
                    return new Label("The Artifact Content is empty");
                }
            } else {
                if (artifactDefinition.getDefinition() != null) {
                    FormDefinitionLoader formLoader = new FormDefinitionLoader(artifactDefinition.getDefinition());
                    formLoader.build();
                    
                    formRenderer = new FormRenderer(formLoader);
                    formRenderer.render(wsBean, session);

                    return formRenderer;
                } else
                    return new Label("The Artifact Definition is empty");
            }
        }
        return null;
    }

    @Override
    public byte[] getContent() {
        return new FormInstanceCreator(formRenderer.getFormStructure(), wsBean, session).getStructure();
    }
    
}
