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

import com.neotropic.flow.component.paperdialog.PaperDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Emphasis;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.Objects;
import java.util.function.Consumer;
import org.neotropic.kuwaiba.core.apis.integration.views.AbstractViewNode;
import org.neotropic.kuwaiba.core.apis.persistence.business.BusinessObjectLight;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Component used for the marker rendering
 * @author Johny Andres Ortega Ruiz {@literal <johny.ortega@kuwaiba.org>}
 */
public class MarkerComponentColumn extends HorizontalLayout {
    public MarkerComponentColumn(
        TranslationService ts, 
        BusinessObjectLight businessObject, 
        AbstractViewNode<BusinessObjectLight> node, 
        PaperDialog paperDialog, 
        Consumer<BusinessObjectLight> consumerAddMarker) {

        Objects.requireNonNull(paperDialog);
        Objects.requireNonNull(consumerAddMarker);

        setPadding(false);
        setMargin(false);

        Icon iconMarker = new Icon(VaadinIcon.MAP_MARKER);

        VerticalLayout lyt = new VerticalLayout();
        lyt.setMargin(false);
        lyt.setPadding(false);
        lyt.setSpacing(false);

        Label lblName = new Label(businessObject.getName());
        Emphasis empClass = new Emphasis(businessObject.getClassName());

        lyt.add(lblName, empClass);

        Button btnAdd = new Button(new Icon(VaadinIcon.PLUS));
        paperDialog.dialogConfirm(btnAdd);
        btnAdd.addClickListener(event -> consumerAddMarker.accept(businessObject));
        if (node != null) {
            iconMarker.setColor("#E74C3C");
            btnAdd.setVisible(false);
        }
        else
            iconMarker.setColor("#737373");

        add(iconMarker, lyt, btnAdd);
        setJustifyContentMode(FlexComponent.JustifyContentMode.START);
        setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
    }
}
