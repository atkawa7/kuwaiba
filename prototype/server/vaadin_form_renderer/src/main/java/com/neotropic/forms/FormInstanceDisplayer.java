/*
 *  Copyright 2010-2018 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the EPL License, Version 1.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.eclipse.org/legal/epl-v10.html
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.forms;

import com.neotropic.api.forms.FormInstanceLoader;
import com.neotropic.api.forms.FormLoader;
import com.neotropic.api.forms.XMLUtil;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import java.io.File;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FormInstanceDisplayer {
    private static FormInstanceDisplayer instance;
    
    private FormInstanceDisplayer() {
    }
    
    public static FormInstanceDisplayer getInstance() {
        return instance == null ? instance = new FormInstanceDisplayer() : instance;        
    }
    
    public void display(File file, boolean sizeFull) {
        
        byte[] structure = XMLUtil.getFileAsByteArray(file);
        
        if (structure == null)
            return;
                
        Window subWindow = new Window(file.getName());
        subWindow.setModal(true);
        
        FormInstanceLoader fil = new FormInstanceLoader();
        FormLoader formLoader = fil.load(structure);
        
        FormRenderer formRenderer = new FormRenderer(formLoader);
        
        Panel pnlForm = new Panel();
        pnlForm.setContent(formRenderer);
        pnlForm.setSizeUndefined();
        subWindow.setContent(pnlForm);

        formRenderer.render();
        subWindow.setResizable(true);
        subWindow.center();

        if (sizeFull)
            subWindow.setSizeFull();

        UI.getCurrent().addWindow(subWindow);
    }
    
}
