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

import com.vaadin.data.HasValue;
import com.vaadin.data.HasValue.ValueChangeListener;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import org.kuwaiba.apis.forms.ScriptQueryExecutorImpl;
import org.kuwaiba.apis.forms.elements.ElementScript;
import org.kuwaiba.apis.forms.elements.FormDefinitionLoader;
import org.kuwaiba.apis.forms.elements.FunctionRunner;
import org.kuwaiba.beans.WebserviceBean;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifact;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteArtifactDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteConditionalActivityDefinition;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteProcessInstance;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;

/**
 * Renders a Conditional Artifact
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ConditionalArtifactRender extends ArtifactRenderer {
    private final RemoteActivityDefinition activityDefinition;
    private final RemoteArtifactDefinition artifactDefinition;
    private final RemoteArtifact remoteArtifact;
    private CheckBox chkYes;
    
    private final WebserviceBean wsBean;
    private final RemoteSession session;
    private final RemoteProcessInstance processInstance;
    
    public ConditionalArtifactRender(RemoteActivityDefinition activityDefinition, RemoteArtifactDefinition remoteArtifactDefinition, RemoteArtifact remoteArtifact, WebserviceBean wsBean, RemoteSession session, RemoteProcessInstance processInstance) {
        this.activityDefinition = activityDefinition;
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

            ElementScript elementScript = FormDefinitionLoader.loadExternalScripts(artifactDefinition.getExternalScripts());
            
            FunctionRunner functionRunner = new FunctionRunner("precondition", null, script, elementScript);
            functionRunner.setScriptQueryExecutor(scriptQueryExecutorImpl);
            
            Object result = functionRunner.run(null);

            if (!Boolean.valueOf(result.toString()))
                return new Label(result.toString());
        }        
        // Artifact renderer to the Information Artifact
        ArtifactRenderer artifactRenderer = null;
        
        if (activityDefinition != null && activityDefinition instanceof RemoteConditionalActivityDefinition) {
            RemoteArtifactDefinition informationArtifact = ((RemoteConditionalActivityDefinition) activityDefinition).getInformationArtifact();
            
            if (informationArtifact != null) {
                ArtifactView artifactView = new ArtifactView(null, informationArtifact, null, wsBean, session, processInstance);
                artifactRenderer = artifactView.getArtifactRenderer(informationArtifact, null);
            }
        }
        boolean yes = getConditionalArtifactContent();
        
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSpacing(false);
        verticalLayout.setWidth(99, Unit.PERCENTAGE);
        verticalLayout.setHeightUndefined();

        Label lblQuestion = new Label(artifactDefinition != null ? new String(artifactDefinition.getDefinition()) : "<Not Set>");
        chkYes = new CheckBox("Yes");
        CheckBox chkNo = new CheckBox("No");
                
        if (remoteArtifact != null) {
            chkYes.setValue(yes);
            chkNo.setValue(!yes);
        }        
        chkYes.addValueChangeListener(new ValueChangeListener<Boolean>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<Boolean> event) {
                if (event.getValue())
                    chkNo.setValue(false);
                else
                    chkNo.setValue(true);
            }
        });
        chkNo.addValueChangeListener(new ValueChangeListener<Boolean>() {
            @Override
            public void valueChange(HasValue.ValueChangeEvent<Boolean> event) {
                if (event.getValue())
                    chkYes.setValue(false);
                else
                    chkYes.setValue(true);
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        horizontalLayout.addComponent(chkYes);
        horizontalLayout.addComponent(chkNo);

        verticalLayout.addComponent(lblQuestion);
        verticalLayout.addComponent(horizontalLayout);

        verticalLayout.setComponentAlignment(lblQuestion, Alignment.MIDDLE_CENTER);
        verticalLayout.setComponentAlignment(horizontalLayout, Alignment.MIDDLE_CENTER);
        
        if (artifactRenderer != null) {
            VerticalSplitPanel verticalSplitPanel = new VerticalSplitPanel();
            verticalSplitPanel.setSizeFull();
            verticalSplitPanel.setSplitPosition(20, Unit.PERCENTAGE);
            
            verticalSplitPanel.setFirstComponent(verticalLayout);
            verticalSplitPanel.setSecondComponent(artifactRenderer.renderArtifact());
            return verticalSplitPanel;
        }
        return verticalLayout;        
    }

    @Override
    public byte[] getContent() throws Exception {
        String strContent = "<artifact type=\"conditional\"><value>" + chkYes.getValue() + "</value></artifact>";
        return strContent.getBytes();
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
