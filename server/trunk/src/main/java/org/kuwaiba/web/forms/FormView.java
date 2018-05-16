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
package org.kuwaiba.web.forms;

import com.vaadin.cdi.CDIView;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;
import org.kuwaiba.apis.forms.FormRenderer;
import org.kuwaiba.apis.forms.elements.FormLoader;
import org.kuwaiba.interfaces.ws.toserialize.application.RemoteForm;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
@CDIView("form")
public class FormView extends CustomComponent implements View {
    public static String VIEW_NAME = "form";
        
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        RemoteForm remoteForm = (RemoteForm) getSession().getAttribute("currentform");
        
        if (remoteForm == null || remoteForm.getStructure() == null)
            return;
                
        FormLoader formBuilder = new FormLoader(remoteForm.getStructure());            
        formBuilder.build();

        FormRenderer formRenderer = new FormRenderer(formBuilder);
        formRenderer.render();
//        Panel pnlForm = new Panel();
//        pnlForm.setContent(formRenderer);
//        pnlForm.setSizeUndefined();
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.addComponent(formRenderer);
        verticalLayout.setComponentAlignment(formRenderer, Alignment.BOTTOM_CENTER);
                                
        setCompositionRoot(verticalLayout);
    }
    
}

