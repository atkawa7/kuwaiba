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
package org.kuwaiba.apis.forms;

import org.kuwaiba.apis.forms.elements.FormLoader;
import org.kuwaiba.apis.forms.elements.XMLUtil;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 *
 * @author Johny Andres Ortega Ruiz <johny.ortega@kuwaiba.org>
 */
public class FormDisplayer {
    private static FormDisplayer instance;
    
    private FormDisplayer() {
    }
    
    public static FormDisplayer getInstance() {
        return instance == null ? instance = new FormDisplayer() : instance;        
    }
    
    public void display(File file, boolean sizeFull) {
        
        byte[] structure = XMLUtil.getFileAsByteArray(file);
        
        if (structure == null)
            return;
                
        Window subWindow = new Window(file.getName());
        subWindow.setModal(true);

        FormLoader formBuilder = new FormLoader(structure);            
        formBuilder.build();

        FormRenderer formRenderer = new FormRenderer(formBuilder);

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
