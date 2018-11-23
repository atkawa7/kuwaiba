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

import com.vaadin.ui.Panel;
import org.kuwaiba.apis.persistence.application.process.ArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;

/**
 * View to render the different type of artifacts
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ArtifactView extends Panel {
    private final RemoteActivityDefinition activityDefinition;
    private final RemoteArtifactDefinition artifactDefinition;
    private final RemoteArtifact artifact;
    private final WebserviceBean wsBean;
    private final RemoteSession session;
    private ArtifactRenderer artifactRenderer;
    private final RemoteProcessInstance processInstance;
    
    public ArtifactView(RemoteActivityDefinition activityDefinition, RemoteArtifactDefinition artifactDefinition, RemoteArtifact artifact, WebserviceBean wsBean, RemoteSession session, RemoteProcessInstance processInstance/*, List<RemoteArtifact> remoteArtifacts*/) {
        this.activityDefinition = activityDefinition;
        this.artifactDefinition = artifactDefinition;
        this.artifact = artifact;
        this.wsBean = wsBean;
        this.session = session;
        
        this.processInstance = processInstance;
        
        setStyleName("formmanager");
        setSizeFull();
        initView();
    }
        
    public void initView() {        
        switch (artifactDefinition.getType()) {
            case ArtifactDefinition.TYPE_ATTACHMENT: 
                artifactRenderer = new AttachmentArtifactRender(artifactDefinition, artifact);
                setContent(artifactRenderer.renderArtifact());
            break;
            case ArtifactDefinition.TYPE_CONDITIONAL: 
                artifactRenderer = new ConditionalArtifactRender(activityDefinition, artifactDefinition, artifact, wsBean, session, processInstance);
                setContent(artifactRenderer.renderArtifact());
            break;
            case ArtifactDefinition.TYPE_FORM: 
                artifactRenderer = new FormArtifactRenderer(artifactDefinition, artifact, wsBean, session, processInstance);
                setContent(artifactRenderer.renderArtifact());
                
                if (getContent() != null) {
                    getContent().setWidthUndefined();
                    getContent().setHeightUndefined();
                }
            break;
        }
    }
    
    public ArtifactRenderer getArtifactRenderer(RemoteArtifactDefinition artifactDefinition, RemoteArtifact artifact) {
        switch (artifactDefinition.getType()) {
            case ArtifactDefinition.TYPE_ATTACHMENT: 
                return new AttachmentArtifactRender(artifactDefinition, artifact);
            case ArtifactDefinition.TYPE_CONDITIONAL: 
                return new ConditionalArtifactRender(activityDefinition, artifactDefinition, artifact, wsBean, session, processInstance);
            case ArtifactDefinition.TYPE_FORM: 
                return new FormArtifactRenderer(artifactDefinition, artifact, wsBean, session, processInstance);
        }
        return null;
    }
    
    public ArtifactRenderer getArtifactRenderer() {
        return artifactRenderer;        
    }
    
}
