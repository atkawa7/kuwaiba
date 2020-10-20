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
package org.neotropic.util.visual.dialog;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import org.neotropic.kuwaiba.core.i18n.TranslationService;

/**
 * Generic Dialog that provides the capability to confirm specific actions.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>}
 */
public class ConfirmDialog extends Dialog {
    
    private Button btnConfirm;
    private VerticalLayout container;

    public ConfirmDialog(String caption, String text, String confirmButtonText) {

        final HorizontalLayout buttons = new HorizontalLayout();

        btnConfirm = new Button(confirmButtonText);
        btnConfirm.setClassName("primary-button");
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnConfirm.focus();
        buttons.add(btnConfirm);

        final Button btnCancel = new Button("Cancel", e -> close());
        buttons.add(btnCancel);
        
        VerticalLayout content = new VerticalLayout(new H3(caption), new Span(text), buttons);
        content.setPadding(true);
        add(content);
    }
    /**
     * Creates a confirm dialog with a component as content.
     * @param ts An instance of the translation service
     * @param title The title of the confirm dialog
     * @param content The component used as content of the confirm dialog
     * @param confirmButtonText Text to show in the confirm button
     * @param confirmAction Action to execute on confirm
     */
    public ConfirmDialog(TranslationService ts, String title, Component content, String confirmButtonText, Command confirmAction) {
        this(ts, content, confirmButtonText, confirmAction);
        H3 hdntitle = new H3(title);
        container.addComponentAsFirst(hdntitle);
        container.setHorizontalComponentAlignment(FlexComponent.Alignment.START, hdntitle);
    }
    /**
     * Creates a confirm dialog with a component as content.
     * @param ts An instance of the translation service
     * @param content The component used as content of the confirm dialog
     * @param confirmButtonText Text to show in the confirm button
     * @param confirmAction Action to execute on confirm
     */
    public ConfirmDialog(TranslationService ts, Component content, String confirmButtonText, Command confirmAction) {
        HorizontalLayout buttons = new HorizontalLayout();
        
        Button btnCancel = new Button(
            ts.getTranslatedString("module.general.messages.cancel"), 
            event -> { close(); event.unregisterListener();});
        
        Button btnOk = new Button(confirmButtonText, 
            event -> { confirmAction.execute(); event.unregisterListener(); close();});
        btnOk.setClassName("primary-button"); //NOI18N
        btnOk.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        btnOk.focus();
        
        buttons.add(btnCancel, btnOk);
        
        container = new VerticalLayout();
        container.add(content);
        container.add(buttons);
        container.setHorizontalComponentAlignment(FlexComponent.Alignment.START, content);
        container.setHorizontalComponentAlignment(FlexComponent.Alignment.END, buttons);
        add(container);
    }
    
    public Button getBtnConfirm() {
        return btnConfirm;
    }

    public void setBtnConfirm(Button btnConfirm) {
        this.btnConfirm = btnConfirm;
    }
}
