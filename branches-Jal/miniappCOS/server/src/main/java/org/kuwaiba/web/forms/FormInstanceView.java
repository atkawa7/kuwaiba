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
import com.vaadin.ui.CustomComponent;
import javax.inject.Inject;
import org.kuwaiba.beans.WebserviceBean;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
@CDIView("forminstance")
public class FormInstanceView extends CustomComponent implements View {
    public static String VIEW_NAME = "forminstance";  
    
    @Inject
    private WebserviceBean wsBean;
        
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
//        RemoteFormInstance remoteFormInstance = (RemoteFormInstance) getSession().getAttribute("currentforminstance");
//        
//        if (remoteFormInstance == null || remoteFormInstance.getStructure() == null)
//            return;
//        
//        FormInstanceLoader fil = new FormInstanceLoader();
//        FormDefinitionLoader formLoader = fil.load(remoteFormInstance.getStructure());
//        
//        FormRenderer formRenderer = new FormRenderer(formLoader);
//        
//        RemoteSession remoteSession = (RemoteSession) getSession().getAttribute("session");
//        formRenderer.render(wsBean, remoteSession);
//        
//        setCompositionRoot(formRenderer);        
    }
}
