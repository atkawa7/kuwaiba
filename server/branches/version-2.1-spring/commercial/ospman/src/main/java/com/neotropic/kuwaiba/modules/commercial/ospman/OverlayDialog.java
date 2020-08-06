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

import com.neotropic.flow.component.mxgraph.MxGraph;
import com.neotropic.flow.component.paperdialog.PaperDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.Command;
import java.util.HashMap;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Dialog to show map overlay information
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class OverlayDialog extends PaperDialog {
    private final String WIDTH = "350px";
    
    public OverlayDialog(Component positionTarget, TranslationService ts, 
        MapOverlay selectedOverlay,
        HashMap<MapOverlay, MxGraph> overlays, 
        Command cmdAddOverlay, 
        Consumer<MapOverlay> consumerSelectedOverlay) {
        
        positionTarget(positionTarget);
        setNoOverlap(true);
        setHorizontalAlign(HorizontalAlign.LEFT);
        setVerticalAlign(VerticalAlign.TOP);
        setMargin(false);
        if (!overlays.isEmpty()) {
            Grid<MapOverlay> grid = new Grid();
            grid.setMinWidth(WIDTH);
            grid.setMaxWidth(WIDTH);
            grid.addComponentColumn(overlay -> new OverlayComponentColumn(overlay, overlays.get(overlay), ts));
            grid.setItems(overlays.keySet());
            grid.addSelectionListener(event -> {
                if (event.getFirstSelectedItem().isPresent())
                    consumerSelectedOverlay.accept(event.getFirstSelectedItem().get());
                else
                    consumerSelectedOverlay.accept(null);
            });
            if (selectedOverlay != null)
                grid.select(selectedOverlay);
            add(grid);
        }
        Button btnAddOverlay = new Button(ts.getTranslatedString("module.ospman.add-overlay"), new Icon(VaadinIcon.PLUS));
        btnAddOverlay.addClickListener(event -> cmdAddOverlay.execute());
        add(btnAddOverlay);
        
        dialogConfirm(btnAddOverlay);
    }
}
