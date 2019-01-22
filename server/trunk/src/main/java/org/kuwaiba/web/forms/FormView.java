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
package org.kuwaiba.web.forms;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import javax.inject.Inject;
import org.kuwaiba.apis.forms.FormRenderer;
import org.kuwaiba.apis.forms.elements.FormDefinitionLoader;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteForm;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteSession;
import org.kuwaiba.beans.WebserviceBean;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@CDIView("form")
public class FormView extends CustomComponent implements View {
    public static String VIEW_NAME = "form";
    
    @Inject
    private WebserviceBean wsBean;
        
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        RemoteForm remoteForm = (RemoteForm) getSession().getAttribute("currentform"); //NOI18N
        
        if (remoteForm == null || remoteForm.getStructure() == null)
            return;
                
        FormDefinitionLoader formBuilder = new FormDefinitionLoader(remoteForm.getStructure());            
        formBuilder.build();

        FormRenderer formRenderer = new FormRenderer(formBuilder, null);
        
        RemoteSession remoteSession = (RemoteSession) getSession().getAttribute("session"); //NOI18N
        formRenderer.render(wsBean, remoteSession);
        
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(formRenderer);
        verticalLayout.setComponentAlignment(formRenderer, Alignment.BOTTOM_CENTER);
                                
        setCompositionRoot(verticalLayout);
    }
    
}

