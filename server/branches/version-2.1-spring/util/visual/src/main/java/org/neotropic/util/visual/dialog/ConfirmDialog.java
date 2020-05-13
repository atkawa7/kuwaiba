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
 * Generic Dialog that provides the capability to confirm specific actions.
 * @author Orlando Paz  {@literal <orlando.paz@kuwaiba.org>}
 */
public class ConfirmDialog extends Dialog {
    
    private Button btnConfirm;

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

    public Button getBtnConfirm() {
        return btnConfirm;
    }

    public void setBtnConfirm(Button btnConfirm) {
        this.btnConfirm = btnConfirm;
    }
    
    

}
