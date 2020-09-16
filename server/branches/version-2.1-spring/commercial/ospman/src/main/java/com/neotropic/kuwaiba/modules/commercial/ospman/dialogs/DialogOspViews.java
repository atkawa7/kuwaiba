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

import com.neotropic.flow.component.paperdialog.PaperDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.shared.Registration;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.persistence.application.ApplicationEntityManager;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObject;
import org.neotropic.kuwaiba.core.apis.persistence.application.ViewObjectLight;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.ApplicationObjectNotFoundException;
import org.neotropic.kuwaiba.core.apis.persistence.exceptions.InvalidArgumentException;
import org.neotropic.kuwaiba.core.i18n.TranslationService;
import org.neotropic.util.visual.notifications.SimpleNotification;

/**
 * Shows a list of all the Outside Plant Views available
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>} 
 */
public class DialogOspViews extends PaperDialog {
    private final String WIDTH = "350px";
    /**
     * Registration of grid selection listener
     */
    private Registration registration;
    
    public DialogOspViews(Component positionTarget, ApplicationEntityManager aem, TranslationService ts, Consumer<ViewObject> consumerSelectedView) {
        Objects.requireNonNull(positionTarget);
        try {
            positionTarget(positionTarget);
            setNoOverlap(true);
            setHorizontalAlign(PaperDialog.HorizontalAlign.LEFT);
            setVerticalAlign(PaperDialog.VerticalAlign.TOP);
            setMargin(false);

            Grid<ViewObjectLight> grid = new Grid();
            grid.setMinWidth(WIDTH);
            grid.setMaxWidth(WIDTH);
            grid.addComponentColumn(item -> {
                Label lblName = new Label(item.getName() != null ? item.getName() : "");
                lblName.setWidthFull();
                return lblName;
            });
            grid.setItems(aem.getOSPViews());
            registration = grid.addSelectionListener(event -> {
                registration.remove();
                if (event.getFirstSelectedItem().isPresent()) {
                    ViewObjectLight viewObject = event.getFirstSelectedItem().get();
                    try {
                        consumerSelectedView.accept(aem.getOSPView(viewObject.getId()));
                    } catch (ApplicationObjectNotFoundException ex) {
                        new SimpleNotification(
                            ts.getTranslatedString("module.general.messages.error"), 
                            ex.getLocalizedMessage()
                        ).open();
                    }
                }
            });
            
            add(grid);
            dialogConfirm(grid);
        } catch (InvalidArgumentException ex) {
            new SimpleNotification(
                ts.getTranslatedString("module.general.messages.error"),
                ex.getLocalizedMessage()
            ).open();
        }
    }
}
