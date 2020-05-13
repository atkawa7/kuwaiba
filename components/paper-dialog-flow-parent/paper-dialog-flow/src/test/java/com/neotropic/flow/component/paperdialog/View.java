package com.neotropic.flow.component.paperdialog;

import com.neotropic.flow.component.paperdialog.PaperDialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("")
public class View extends Div {

    public View() {
        PaperDialog paperDialog = new PaperDialog();
        add(paperDialog);
    }
}
