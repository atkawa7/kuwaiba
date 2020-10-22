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

import com.neotropic.kuwaiba.modules.commercial.ospman.api.MapOverlay;
import com.neotropic.flow.component.mxgraph.MxGraph;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Component used for the map overlay rendering
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class ComponentColumnOverlay extends HorizontalLayout {
    public ComponentColumnOverlay(MapOverlay overlay, MxGraph graph, TranslationService ts) {
        Checkbox chkEnabled = new Checkbox();
        Label lblTitle = new Label();
        
        chkEnabled.addValueChangeListener(event -> {
            overlay.setEnabled(event.getValue());
            graph.setVisible(event.getValue());
        });
        if (overlay.getTitle() != null)
            lblTitle.setText(overlay.getTitle());
        else
            lblTitle.setText(ts.getTranslatedString("module.ospman.untitled-overlay"));
        
        chkEnabled.setValue(overlay.getEnabled());
        add(chkEnabled);
        add(lblTitle);
    }
}
