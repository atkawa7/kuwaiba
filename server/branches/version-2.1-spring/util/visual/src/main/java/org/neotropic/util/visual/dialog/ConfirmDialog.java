/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.neotropic.util.visual.dialog;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

/**
 *
 * @author Orlando Paz
 */
public class ConfirmDialog extends Dialog {
    
    private Button btnConfirm;

    public ConfirmDialog(String caption, String text, String confirmButtonText) {

        final VerticalLayout content = new VerticalLayout();
        content.setPadding(false);
        add(content);

        add(new H3(caption));
        add(new Span(text));

        final HorizontalLayout buttons = new HorizontalLayout();
        buttons.setPadding(false);
        add(buttons);

        btnConfirm = new Button(confirmButtonText);
        btnConfirm.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttons.add(btnConfirm);

        final Button btnCancel = new Button("Cancel", e -> close());
        buttons.add(btnCancel);

    }

    public Button getBtnConfirm() {
        return btnConfirm;
    }

    public void setBtnConfirm(Button btnConfirm) {
        this.btnConfirm = btnConfirm;
    }
    
    

}
