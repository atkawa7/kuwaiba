/*
 *  Copyright 2010-2020 Neotropic SAS <contact@neotropic.co>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       https://apache.org/licenses/LICENSE-2.0.txt
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.neotropic.kuwaiba.modules.commercial.ospman.dialogs;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 *
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class WindowPortTools extends Dialog {
    
    public WindowPortTools(BusinessObjectLight port, BusinessEntityManager bem, TranslationService ts, Consumer<BusinessObjectLight> consumerReleaseFiber) {
        super();
        Label lblPortTools = new Label("Port Tools");
        Button btnReleasePort = new Button("Release Port", event -> {
            if (consumerReleaseFiber != null)
                consumerReleaseFiber.accept(port);
            close();
        });
        
        Button btnShowPhysicalPath = new Button("Show Physical Path", event -> close());
        
        Button btnShowPhysicalTree = new Button("Show Physical Tree", event -> close());
        
        Button btnCancel = new Button("Cancel", event -> close());
        
        VerticalLayout lyt = new VerticalLayout(
            lblPortTools, btnReleasePort, btnShowPhysicalPath, btnShowPhysicalTree, btnCancel
        );
        lyt.setSpacing(false);
        lyt.setMargin(false);
        lyt.setPadding(false);
        lyt.setHorizontalComponentAlignment(FlexComponent.Alignment.CENTER, btnCancel);
        
        add(lyt);
    }
}
