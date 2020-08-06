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
package com.neotropic.kuwaiba.modules.commercial.ospman;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Dialog to the node tool set
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class DialogNode extends Dialog {
    public DialogNode(AbstractViewNode<BusinessObjectLight> node, TranslationService ts) {
        HorizontalLayout lytNodeTools = new HorizontalLayout();
        lytNodeTools.add(new Button(ts.getTranslatedString("module.ospman.view-node.tool.connect"), new Icon(VaadinIcon.PLUG), 
            event -> {
                close();
            }
        ));
        lytNodeTools.add(new Button(ts.getTranslatedString("module.ospman.view-node.tool.view-content"), new Icon(VaadinIcon.EYE),
            event -> {
                close();
            }
        ));
        lytNodeTools.add(new Button(ts.getTranslatedString("module.ospman.view-node.tool.remove"), new Icon(VaadinIcon.TRASH), 
            event -> {
                close();
            }
        ));
        add(lytNodeTools);
    }
}
