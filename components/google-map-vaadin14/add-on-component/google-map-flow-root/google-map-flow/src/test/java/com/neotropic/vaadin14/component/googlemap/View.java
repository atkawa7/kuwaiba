package com.neotropic.vaadin14.component.googlemap;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("")
public class View extends Div {

    public View() {
        Button button = new Button();
        add(button);
    }
}
